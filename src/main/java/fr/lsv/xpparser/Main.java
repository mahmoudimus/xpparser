package fr.lsv.xpparser;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXParseException;

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
        
        //--------------------------------------------- process options
        int i = 0;
        if (i < args.length) {
            if (args[0].equals("-h") || args[0].equals("--help")) {
                help = true;
                i++;
            }
            else if (args[0].equals("--xquery")) {
                i++;
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
        }
        
        //--------------------------------- display command-line syntax
        if (!legit || help) {
            System.out.println
                ("Usage: "+ progname +"[OPTION] [FILE]...");
            System.out.println
                ("Extract and parse XPath expressions, then print them in XQueryX format");
            System.out.println("on standard output.\n");
            System.out.println
                ("      --xml PATTERN  parse input files as XML documents, and extract");
            System.out.println
                ("                     contents using the provided XPath 1.0 PATTERN");
            System.out.println
                ("      --xquery       parse input files as XQuery documents");
            System.out.println
                ("      --xslt         parse input files as XSLT documents; equivalent");
            System.out.println
                ("                     to --xml '//@match | //@select | //@test'");
            System.out.println
                ("  -h, --help         display this help and exit");
            System.out.println("");
            System.out.println
                ("With no OPTION, behaves as if --xquery was provided.  With no FILE, read");
            System.out.println("from standard input.");

            System.exit(legit? 1: 0);
        }
        //----------------------------------------------- process input
        try {
            // the list of input streams
            List<Map.Entry<String,Reader>> streams
                = new LinkedList<Map.Entry<String,Reader>>();
            // the SourceFactory            
            final SourceFactory sf;
            if (xml)
                sf = new SourceFactory(filter);
            else //if (xquery)
                sf = new SourceFactory();
            
            if (i == args.length) {
                // we are processing the standard input
                streams.add(new AbstractMap.SimpleEntry
                            ("stdin",
                             new BufferedReader
                             (new InputStreamReader(System.in))));
            }
            else
                for (int j = i; j < args.length; j++) {
                    Path path = Paths.get(args[j]);
                    /* should we use MIME/types? */
                    //System.out.println(path.toString()+":
                    //"+Files.probeContentType(path));
                    try {
                        if (Files.isDirectory(path))
                            throw new NotDirectoryException
                                (path.toString());

                        /* what to do with other charsets???? */
                        BufferedReader br = Files.newBufferedReader
                            (path, Charset.defaultCharset());
                        streams.add(new AbstractMap.SimpleEntry
                                    (path.toString(), br));
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
                    
                }

            // print XML
            System.out.println("<?xml version=\"1.0\"?>");
            System.out.println("<benchmark>");

            // process sources
            for (Map.Entry<String,Reader> stream : streams)
                try {
                    for (XPathEntry entry : sf.getSource(stream))
                            entry.print();
                    
                //------------------------------------- error handling
                } catch (ParseException e) {
                    // error parsing an XQuery file
                    System.err.println
                        (progname +": "+ stream.getKey()
                         +": could not parse as XQuery:");
                    System.err.println(e.getMessage());
                } catch (SAXParseException e) {
                    // error parsing an XML file
                    System.err.println
                        (progname +": "+ stream.getKey() 
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
     * Print stack trace and abort.
     * @param e The fatal error.
     */
    private static void abort(Exception e) {
        System.err.println(progname +": fatal error!");
        System.err.println(e.toString());
        e.printStackTrace(System.err);
        System.exit(0);
    }
        


// import javax.xml.transform.*;
// import javax.xml.transform.dom.DOMSource; 
// import javax.xml.transform.stream.StreamResult;
    // /**
    //  * Pretty-print a DOM Node.
    //  * @param doc The document to print.
    //  * @param os The chosen output.
    //  */
    // private static void print(org.w3c.dom.Node node, OutputStream os)
    //     throws IOException, TransformerConfigurationException,
    //            TransformerException {
        
    //     TransformerFactory tf = TransformerFactory.newInstance();
    //     Transformer transformer = tf.newTransformer();
    //     transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    //     transformer.setOutputProperty
    //         ("{http://xml.apache.org/xslt}indent-amount", "2");
    //     transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    //     transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    //     transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
    //     DOMSource source = new DOMSource(node);
    //     StreamResult result = new StreamResult(os);
    //     transformer.transform(source, result);
    // }
    // private static void print(org.w3c.dom.Node node)
    //     throws IOException, TransformerConfigurationException,
    //            TransformerException  {
    //     print(node, System.out);
    // }
}
