package org.w3c.xqparser;

import java.util.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import javax.xml.namespace.*;
import javax.xml.XMLConstants;
import org.w3c.dom.*;

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
            List<BufferedReader> streams = new LinkedList<BufferedReader>();
            // the corresponding list of file names
            List<String> filenames = new LinkedList<String>();
            
            if (i == args.length) {
                // we are processing the standard input
                streams.add(new BufferedReader
                            (new InputStreamReader(System.in)));
                filenames.add("stdin");
            }
            else
                for (int j = i; j < args.length; j++) {
                    Path path = Paths.get(args[j]);
                    BufferedReader br = Files.newBufferedReader(path);
                    streams.add(br);
                    filenames.add(path.toString());
                }
            
            // if `--xml' or `--xslt' was used
            if (xml) {
                final Iterator<String> filename = filenames.iterator();

                // boilerplate
                final PositionalXMLReader read = new PositionalXMLReader();
                // final XPath xp = XPathFactory.newInstance().newXPath();
                // // there's no default implementation for
                // // NamespaceContext... seems kind of silly, no?
                // xp.setNamespaceContext(new NamespaceContext() {
                //         public String getNamespaceURI(String prefix) {
                //             return read.getNamespaceURI(prefix);
                //         }

                //         // This method isn't necessary for XPath processing.
                //         public String getPrefix(String uri) {
                //             throw new UnsupportedOperationException();
                //         }
                        
                //         // This method isn't necessary for XPath processing.
                //         public Iterator getPrefixes(String uri) {
                //             throw new UnsupportedOperationException();
                //         }
                //     });
                // final XPathExpression e = xp.compile(filter);
                // final XPathExpression ns = xp.compile("namespace::*");


                
                for (BufferedReader stream : streams) {
                    String file = filename.next();
                    // parse the input XML
                    Document d = read.parse(stream);

                    // recover namespace information from the document
                    System.out.println(file+": ");
                    System.out.print(read.ns.toString()+"\n");
                    
                    // apply filter
                    // NodeList nl =
                    //     (NodeList) e.evaluate(d, XPathConstants.NODESET);
                    // for (int j = 0; j < nl.getLength(); j++) {
                    //     // our element
                    //     org.w3c.dom.Node n = nl.item(j);

                    //     // its XPath contents
                    //     String s;
                    //     switch (n.getNodeType()) {
                    //     case org.w3c.dom.Node.ATTRIBUTE_NODE:
                    //     case org.w3c.dom.Node.TEXT_NODE:
                    //         s = n.getNodeValue();
                    //         break;
                            
                    //     case org.w3c.dom.Node.ELEMENT_NODE:
                    //         s = n.getTextContent();
                    //         break;

                    //     default:
                    //         throw new XPathExpressionException("Couldn't process node "+n.getTextContent()+" in "+ file);
                    //     }       
                    //   System.out.println(s);                 
                    // }
                }
            }
            // if `--xquery' or no option was used
            else if (xquery) {
                Iterator<String> filename = filenames.iterator();
                for (BufferedReader stream : streams) {
                    System.out.println(filename.next());
                    
                }
            }            
        }
        catch (Exception e) {
            System.err.println("xqparser: " + e.toString());
            System.exit(0);
        }
        System.exit(1);
    }

}
