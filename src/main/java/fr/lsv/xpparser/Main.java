/*
Copyright (C) 2016-2019 Sylvain Schmitz (ENS Paris-Saclay).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License in `LICENSE` for more details.
 */
package fr.lsv.xpparser;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.*;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.xqparser.ParseException;
import org.w3c.xqparser.TokenMgrError;
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
        boolean unique = false;
        
        //--------------------------------------------- process options
        int i = 0; int end = args.length;
        while (i < args.length) {
            if (args[i].equals("-h") || args[i].equals("--help")) {
                help = true;
                i++;
            }
            else if (args[i].equals("--unique")) {
                unique = true;
                i++;
            }
            else if (args[i].equals("--xquery")) {
                i++;
                if (i < args.length) {
                    fileXSLT = args[i];
                    i++;
                }
                else {
                    legit = false;
                    break;
                }
            }
            else if (args[i].equals("--xslt")) {
                xml = true;
                xquery = false;
                filter = "//@match | //@select | //@test";
                i++;
            }
            else if (args[i].equals("--xml")) {
                i++;
                if (i < args.length) {
                    xml = true;
                    xquery = false;
                    filter = args[i];
                    i++;
                }
                else {
                    legit = false;
                    break;
                }
            }
            else if (args[i].equals("--xsd") || args[i].equals("--rnc")) {
                end = i;
                break;
            }
            else if (args[i].startsWith("--")) { // unknown option
                System.err.println(progname+": unrecognized option '"+args[i]+"'");
                legit = false;
                break;
            }
            else { // file names 
                end = i;
                break;
            }
        }
        while (end < args.length && !args[end].equals("--xsd")
               && !args[end].equals("--rnc"))
            end++;
        
        //--------------------------------- display command-line syntax
        if (!legit || help) {
            System.out.println
                ("Usage: "+ progname +" [MODE] [OPTION...] [FILE]... [VALIDATION...]");
            System.out.println
                ("Extract and parse XPath expressions, then print them in XQueryX format");
            System.out.println("on standard output.");
            System.out.println("");
            System.out.println("MODES:");
            System.out.println
                ("      --xml PATTERN         parse input files as XML documents, and extract");
            System.out.println
                ("                            contents using the provided XPath 1.0 pattern");
            System.out.println
                ("      --xquery STYLESHEET   parse input files as XQuery documents and");
            System.out.println
                ("                            apply the provided XSLT stylesheet.");
            System.out.println
                ("      --xslt                parse input files as XSLT documents; equivalent");
            System.out.println
                ("                            to --xml '//@match | //@select | //@test'");
            System.out.println("");
            System.out.println("OPTIONS:");
            System.out.println
                ("  -h, --help                display this help and exit");
            System.out.println
                ("      --unique              remove redundant queries");
            System.out.println("");
            System.out.println("VALIDATION:");
            System.out.println
                ("      --xsd SCHEMA...       validate output XQueryX against all the");
            System.out.println
                ("                            provided XML Schemas");
            System.out.println
                ("      --rnc SCHEMA...       validate output XQueryX against all the");
            System.out.println
                ("                            provided RelaxNG Compact Schemas");
            System.out.println("");
            System.out.println
                ("With no MODE, expects XQuery input.  With no FILE, read from standard input.");

            System.exit(legit? 0: 1);
        }
        //----------------------------------------------- process input
        try {
            // the list of input streams
            List<Map.Entry<String,BufferedReader>> sources
                = new LinkedList<Map.Entry<String,BufferedReader>>();
            // the SourceFactory            
            final SourceFactory sf;
            if (xml)
                sf = new SourceFactory(filter);
            else //if (xquery)
                sf = new SourceFactory();

            // checking for uniqueness
            HashSet<String> queries = new HashSet<String>();
            
            // prepare input sources
            if (i == end) {
                sources.add(new AbstractMap.SimpleEntry
                            ("stdin",
                             new BufferedReader
                             (new InputStreamReader(System.in))));
            }
            else
                for (int j = i; j < end; j++)
                    addInput(new File(args[j]).getAbsolutePath(), sources);

            // process validation schemas
            ValidationFarm farm = new ValidationFarm();
            while (end < args.length)
                end = processSchemas(end, args, farm);
            sf.setValidator(farm);

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
            for (Map.Entry<String,BufferedReader> source : sources)
                try {
                    for (XPathEntry entry : sf.getSource(source))
                        if (!unique || queries.add(entry.getEntryText()))
                            entry.print();
                   
                    
                //------------------------------------- error handling
                } catch (ParseException|TokenMgrError e) {
                    // error parsing an XQuery file
                    System.err.println
                        (progname +": "+ source.getKey()
                         +": could not parse as XQuery:");
                    System.err.println(e.getMessage());
                } catch (SAXParseException|FileNotFoundException|MalformedInputException e) {
                    // error parsing an XML file
                    System.err.println
                        (progname +": "+ source.getKey() 
                         +": could not parse as XML:");
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
                        System.exit(1);
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
        
        System.exit(0);
    }

    /**
     * Open a file while performing various checks and error
     * reporting.
     * @param filename The path to the file to open.
     * @return A handle on the file.
     */
    public static BufferedReader getInput(String filename)
        throws IOException {
        
        return getInput(Paths.get(filename));
    }
    
    /**
     * Open a file while performing various checks and error
     * reporting.
     * @param filename The path to the file to open.
     * @return A handle on the file.
     */
    public static BufferedReader getInput(Path path)
        throws IOException {
        
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
                                 List<Map.Entry<String,BufferedReader>> list)
        throws IOException {

        BufferedReader br = getInput(filename);
        if (br != null)
            list.add(new AbstractMap.SimpleEntry
                        (filename, br));
    }

    /**
     * Process schemas passed on the command line and add them to the farm.
     * @param index The starting index on the command line.
     * @param args The command line.
     * @param farm The validation farm
     * @return The new index inside the command line.
     */
    private static int processSchemas(int index,
                                      String[] args,
                                      ValidationFarm farm)
        throws IOException {
        
        String type = args[index];
        int end = index + 1;
        while (end < args.length && !args[end].equals("--xsd")
                                 && !args[end].equals("--rnc"))
            end++;

        List<Map.Entry<String,BufferedReader>> schemas
            = new LinkedList<Map.Entry<String,BufferedReader>>();
        for (int j = index + 1; j < end; j++)
            addInput(args[j], schemas);
        for (Map.Entry<String,BufferedReader> schema : schemas)
            try {
                if (type.equals("--xsd"))
                    farm.addXMLSchema(schema.getKey(), schema.getValue());
                if (type.equals("--rnc"))
                    farm.addRNCSchema(schema.getKey(), schema.getValue());
            } catch (SAXException e) {
                System.err.println(progname +": "+ schema.getKey() 
                                   +(type.equals("--xsd")?
                                     ": could not parse XML Schema:":
                                     ": could not parse RelaxNG Compact Schema:"));
                System.err.println(e.getMessage());
            }

        return end;
    }
    

    /** * Print stack trace and abort.
     * @param e The fatal error.
     */
    private static void abort(Exception e) {
        System.err.println(progname +": fatal error!");
        System.err.println(e.toString());
        e.printStackTrace(System.err);
        System.exit(1);
    }
}
