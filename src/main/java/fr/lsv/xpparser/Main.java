package fr.lsv.xpparser;

import java.util.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

/**
 * Command-line processing.
 *
 * @author Sylvain Schmitz <schmitz@lsv.fr>
 */
public class Main {
    
    public static void main(String[] args) {
        boolean legit = true;
        boolean xquery = true;
        boolean xml = false;
        boolean help = false;
        String filter = null;
        
        // process options
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
        
        //----------------------------------- display command-line syntax
        if (!legit || help) {
            System.out.println("Yada yada");

            System.exit(0);
        }
        //------------------------------------------------- process input
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
                    BufferedReader br = Files.newBufferedReader(path);
                    streams.add(new AbstractMap.SimpleEntry
                                (path.toString(), br));
                }

            // print XML
            System.out.println("<?xml version=\"1.0\"?>");
            System.out.println("<benchmark>");

            // process sources
            for (Map.Entry<String,Reader> stream : streams)
                for (XPathEntry entry : sf.getSource(stream))
                    entry.print();

            // finish printing XML
            System.out.println("</benchmark>");
            System.out.flush();
        }
        catch (Exception e) {
            System.err.println("xpparser: " + e.toString());
            e.printStackTrace(System.err);
            System.exit(0);
        }
        
        System.exit(1);
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
