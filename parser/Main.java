package org.w3c.xqparser;

import java.util.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
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

            // build the translator and output            
            XMLWriter xw = new XMLWriter();
            XPathXPrinter xpp = new XPathXPrinter(xw);
            xw.putXMLDecl();
            
            //------------------------------ if `--xml' or `--xslt' was used
            if (xml) {
                final Iterator<String> filename = filenames.iterator();

                // We use a special XML parser that handles line/col numbers
                // and also processes the namespace information
                final PositionalXMLReader read = new PositionalXMLReader();
                final XPath xp = XPathFactory.newInstance().newXPath();
                xp.setNamespaceContext(read);
                
                for (BufferedReader stream : streams) {
                    String file = filename.next();
                    // parse the input XML
                    Document d = read.parse(stream);

                    // apply filter (using namespace info from `read')
                    NodeList nl =
                        (NodeList) xp.evaluate(filter, d,
                                               XPathConstants.NODESET);
                    for (int j = 0; j < nl.getLength(); j++) {
                        // our element
                        org.w3c.dom.Node n = nl.item(j);

                        // its XPath contents
                        String s;
                        switch (n.getNodeType()) {
                        case org.w3c.dom.Node.ATTRIBUTE_NODE:
                            s = n.getNodeValue().trim();
                            n = ((Attr)n).getOwnerElement();
                            break;
                        case org.w3c.dom.Node.TEXT_NODE:
                            s = n.getNodeValue().trim();
                            n = n.getParentNode();
                            break;
                            
                        case org.w3c.dom.Node.ELEMENT_NODE:
                            s = n.getTextContent().trim();
                            break;

                        default:
                            throw new XPathExpressionException
                                ("Couldn't process node "
                                 + n.getTextContent() + " in " + file);
                        }
                        
                        // its namespace information
                        Map<String,String> ns =
                            parseNamespaces
                            ((String)n.getUserData
                             (PositionalXMLReader.NAMESPACES_KEY_NAME));
                        
                        System.out.println(s+" at line "+
                                           n.getUserData(PositionalXMLReader.LINE_NUMBER_KEY_NAME)+" and column "+n.getUserData(PositionalXMLReader.COL_NUMBER_KEY_NAME)+"\n  namespaces: "+ns.toString());

                      // parse the XPath string
                      Reader r = new StringReader(s);
                      XParser parser = new XParser(r);
                      SimpleNode ast = parser.START();
                      xpp.transform(ast, System.out);
                    }
                }
            }
            
            //-------------------------- if `--xquery' or no option was used
            else if (xquery) {
                Iterator<String> filename = filenames.iterator();
                for (BufferedReader stream : streams) {
                    System.out.println(filename.next());
                    XParser parser = new XParser(stream);
                    SimpleNode ast = parser.START();

                    List<SimpleNode> nl = XPathVisitor.visit(ast);
                    for (SimpleNode n : nl) {
                        Map<String,String> ns = declaredNamespaces(n);
                        System.out.println(n.toString()+" at line "+
                                           n.beginLine+" and column "+n.beginColumn+"\n  namespaces: "+ns.toString());
                        xpp.transform(n, System.out);
                    }
                }
            }
            xw.flush();
        }
        catch (Exception e) {
            System.err.println("xqparser: " + e.toString());
            System.exit(0);
        }
        System.exit(1);
    }

    /**
     * Helper method to parse a string obtained by HashMap.toString().
     * @param s The string to parse.  There are no checks at all!
     * @return A HashMap
     */
    private static Map<String,String> parseNamespaces(final String s) {
        Map<String,String> ret = new HashMap<String,String>();
        String[] split = s.substring(1,s.length() - 1).split("=|, ");
        for (int i = 0; i < split.length; i += 2)
            ret.put(split[i], split[i+1]);
        return ret;
    }

    /**
     * Helper method to recover namespace information from an XQuery
     * AST.
     * @param node  A node of the AST.
     * @return A HashMap
     */
    private static Map<String,String> declaredNamespaces
        (final SimpleNode node) {
        
        SimpleNode n = node;
        Map<String,String> ret = new HashMap<String,String>();

        // go up the AST to the Prolog
        while (n.id != XParserTreeConstants.JJTSTART
               && n.id != XParserTreeConstants.JJTMAINMODULE
               && n.id != XParserTreeConstants.JJTQUERYBODY)
            n = n.getParent();

        if (n.id == XParserTreeConstants.JJTQUERYBODY) {
            // recover sibling Prolog node
            n = n.getParent().getChild(0);
            assert(n.id == XParserTreeConstants.JJTPROLOG);

            SimpleNode c;
            for (int i = 0; i < n.jjtGetNumChildren(); i++)
                if ((c = n.getChild(i)).id ==
                    XParserTreeConstants.JJTNAMESPACEDECL)
                    ret.put(c.getChild(0).getValue(),
                            c.getChild(1).getChild(0).getValue());
                else if (c.id ==
                         XParserTreeConstants.JJTDEFAULTNAMESPACEDECL)
                    ret.put(XMLConstants.DEFAULT_NS_PREFIX,
                            c.getChild(0).getChild(0).getValue());
        }
        return ret;
    }
}
