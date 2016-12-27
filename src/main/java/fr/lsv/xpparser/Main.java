/*
Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License in `LICENCE-GPL` for more details.
 */
package fr.lsv.xpparser;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.xqparser.ParseException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/**
 * Command-line processing.
 *
 * @author Sylvain Schmitz <schmitz@lsv.fr>
 */
public class Main {
    
    /** The name of the program, to be used in messages. */
    private final static String progname = "xpparser";
    
    /** 
     * Command-line processor.
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        boolean legit = true;
        boolean xquery = true;
        boolean xml = false;
        boolean help = false;
        String filter = null;
        String fileXSLT = null;
        
        //--------------------------------------------- process options
        int i = 0; int end = args.length;
        if (i < args.length) {
            if (args[0].equals("-h") || args[0].equals("--help")) {
                help = true;
                i++;
            }
            else if (args[0].equals("--xquery")) {
                i++;
                if (i < args.length) {
                    fileXSLT = args[i];
                    i++;
                }
                else
                    legit = false;
            }
            else if (args[0].equals("--xslt")) {
                xml = true;
                xquery = false;
                filter = "//@match | //@select | //@test";
                i++;
            }
            else if (args[0].equals("--xml")) {
                i++;
                if (i < args.length) {
                    xml = true;
                    xquery = false;
                    filter = args[i];
                    i++;
                }
                else
                    legit = false;
            }
            end = i;
            while (end < args.length && !args[end].equals("--validate"))
                end++;
        }
        
        //--------------------------------- display command-line syntax
        if (!legit || help) {
            System.out.println
                ("Usage: "+ progname +" [OPTION] [FILE]... [--validate SCHEMA...]");
            System.out.println
                ("Extract and parse XPath expressions, then print them in XQueryX format");
            System.out.println("on standard output.\n");
            System.out.println
                ("      --xml PATTERN         parse input files as XML documents, and extract");
            System.out.println
                ("                            contents using the provided XPath 1.0 PATTERN");
            System.out.println
                ("      --xquery STYLESHEET   parse input files as XQuery documents and");
            System.out.println
                ("                            apply the provided XSLT stylesheet.");
            System.out.println
                ("      --xslt                parse input files as XSLT documents; equivalent");
            System.out.println
                ("                            to --xml '//@match | //@select | //@test'");
            System.out.println
                ("  -h, --help                display this help and exit");
            System.out.println("");
            System.out.println
                ("With no OPTION, expects XQuery input.  With no FILE, read from standard input.");
            System.out.println("");
            System.out.println
                ("      --validate SCHEMA...  validate output XQueryX against all the");
            System.out.println
                ("                            provided XML Schemas");

            System.exit(legit? 1: 0);
        }
        //----------------------------------------------- process input
        try {
            // the list of input streams
            List<Map.Entry<String,Reader>> sources
                = new LinkedList<Map.Entry<String,Reader>>();
            // the SourceFactory            
            final SourceFactory sf;
            if (xml)
                sf = new SourceFactory(filter);
            else //if (xquery)
                sf = new SourceFactory();

            // prepare input sources
            if (i == end) {
                sources.add(new AbstractMap.SimpleEntry
                            ("stdin",
                             new BufferedReader
                             (new InputStreamReader(System.in))));
            }
            else
                for (int j = i; j < end; j++)
                    addInput(args[j], sources);

            // process validation schemas
            List<Map.Entry<String,Reader>> schemas
                = new LinkedList<Map.Entry<String,Reader>>();
            for (int j = end + 1; j < args.length; j++)
                addInput(args[j], schemas);
            XMLValidator validator = new XMLValidator();
            for (Map.Entry<String,Reader> schema : schemas)
                try {
                    validator.addSchema(schema.getKey(), schema.getValue());
                } catch (SAXParseException e) {
                    System.err.println(progname +": "+ schema.getKey() 
                                       +":could not parse XML Schema:");
                    System.err.println(e.getMessage());
                }
            sf.setValidator(validator);

            // process XSLT stylesheet
            if (fileXSLT != null) {
                Reader xslt = getInput(fileXSLT);
                try {
                    XMLPrinter transformer = new XMLPrinter(xslt);
                    sf.setTransformer(transformer);
                } catch (TransformerConfigurationException e) {
                    // error parsing an XSLT file
                    System.err.println
                        (progname +": "+ fileXSLT
                         +": could not parse XSLT stylesheet:");
                    System.err.println(e.getMessage());
                }
            }

            // print XML
            System.out.println("<?xml version=\"1.0\"?>");
            System.out.println("<benchmark>");

            // process sources
            for (Map.Entry<String,Reader> source : sources)
                try {
                    for (XPathEntry entry : sf.getSource(source))
                        entry.print();
                   
                    
                //------------------------------------- error handling
                } catch (ParseException e) {
                    // error parsing an XQuery file
                    System.err.println
                        (progname +": "+ source.getKey()
                         +": could not parse as XQuery:");
                    System.err.println(e.getMessage());
                } catch (SAXParseException e) {
                    // error parsing an XML file
                    System.err.println
                        (progname +": "+ source.getKey() 
                         +":could not parse as XML:");
                    System.err.println(e.getMessage());
                } catch (NoSuchElementException e) {
                    // error parsing the XPath inside an XML file
                    System.err.println(progname + e.getMessage());
                } catch (XPathExpressionException e) {
                    // error parsing the provided XPath filter
                    if (xml) {
                        System.err.println
                            (progname +": --xml pattern `"
                             + filter +"`:");
                        System.err.println(e.getMessage());
                        System.exit(0);
                    }
                    else 
                        abort(e);
                }                    
            

            // finish printing XML
            System.out.println("</benchmark>");
            System.out.flush();

        // abort in case of uncaught exception
        } catch (Exception e) {
            abort(e);
        }
        
        System.exit(1);
    }

    /**
     * Add a pair comprising a file name and a reader to the provided
     * list.
     * @param filename The path to the file to open.
     * @param list     The list where to add the result.
     */
    private static BufferedReader getInput(String filename)
        throws IOException {
        
        Path path = Paths.get(filename);
        BufferedReader br = null;
        /* should we use MIME/types? */
        //System.out.println(path.toString()+":
        //"+Files.probeContentType(path));
        try {
            if (Files.isDirectory(path))
                throw new NotDirectoryException(path.toString());

            /* what to do with other charsets???? */
            br = Files.newBufferedReader
                (path, Charset.defaultCharset());
        } catch (NoSuchFileException e) {
            System.err.println
                (progname +": "+ e.getMessage()
                 +": No such file");
        } catch (AccessDeniedException e) {
            System.err.println
                (progname +": "+ e.getMessage() 
                 +": Permission denied");
        } catch (NotDirectoryException e) {
            System.err.println
                (progname +": "+ e.getMessage() 
                 +": Is a directory");
        }
        return br;
    }
        

    /**
     * Add a pair comprising a file name and a reader to the provided
     * list.
     * @param filename The path to the file to open.
     * @param list     The list where to add the result.
     */
    private static void addInput(String filename,
                                 List<Map.Entry<String,Reader>> list)
        throws IOException {

        BufferedReader br = getInput(filename);
        if (br != null)
            list.add(new AbstractMap.SimpleEntry
                        (filename, br));
    }


    /** * Print stack trace and abort.
     * @param e The fatal error.
     */
    private static void abort(Exception e) {
        System.err.println(progname +": fatal error!");
        System.err.println(e.toString());
        e.printStackTrace(System.err);
        System.exit(0);
    }
}