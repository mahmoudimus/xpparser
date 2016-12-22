package org.w3c.xqparser;

import java.util.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;
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
            final DocumentBuilderFactory dbf
                = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            
            XMLWriter xw = new XMLWriter();
            XPathXPrinter xpp = new XPathXPrinter(xw);
            xw.putXMLDecl();

            // testing stuff
            Document doc = db.newDocument();
            Element root = doc.createElementNS
                (XMLConstants.DEFAULT_NS_PREFIX, "benchmark");
            doc.appendChild(root);
            
            //------------------------------ if `--xml' or `--xslt' was used
            if (xml) {
                final Iterator<String> filename = filenames.iterator();

                // We use a special XML parser that handles line/col numbers
                // and also processes the namespace information
                final PositionalXMLReader read = new PositionalXMLReader(db);
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
                        String q;
                        switch (n.getNodeType()) {
                        case org.w3c.dom.Node.ATTRIBUTE_NODE:
                            q = n.getNodeValue().trim();
                            n = ((Attr)n).getOwnerElement();
                            break;
                        case org.w3c.dom.Node.TEXT_NODE:
                            q = n.getNodeValue().trim();
                            n = n.getParentNode();
                            break;
                            
                        case org.w3c.dom.Node.ELEMENT_NODE:
                            q = n.getTextContent().trim();
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
                        
                        // parse the XPath string
                        Element astElement = append
                            (root,
                             doc,
                             q,
                             file,
                             (String)n.getUserData
                             (PositionalXMLReader.LINE_NUMBER_KEY_NAME),
                             (String)n.getUserData
                             (PositionalXMLReader.COL_NUMBER_KEY_NAME),
                             ns);
                        Reader r = new StringReader(q);
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
                    String file = filename.next();
                    XParser parser = new XParser(stream);
                    SimpleNode ast = parser.START();

                    List<SimpleNode> nl = XPathVisitor.visit(ast);
                    for (SimpleNode n : nl) {
                        Map<String,String> ns = declaredNamespaces(n);
                        Element astElement = append
                            (root,
                             doc,
                             "",
                             file,
                             String.valueOf(n.beginLine),
                             String.valueOf(n.beginColumn),
                             ns);
                        
                        xpp.transform(n, System.out);
                    }
                }
            }
            print(doc);
            System.out.flush();
        }
        catch (Exception e) {
            System.err.println("xqparser: " + e.toString());
            e.printStackTrace(System.err);
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
                    XParserTreeConstants.JJTNAMESPACEDECL) {
                    String uri = c.getChild(1).getChild(0).getValue();
                    uri = uri.substring(1,uri.length() - 1);
                    ret.put(c.getChild(0).getValue(), uri);
                }
            /* We do not care about default namespaces! */
            // else if (c.id ==
            //          XParserTreeConstants.JJTDEFAULTNAMESPACEDECL) {
            //     String uri = c.getChild(0).getChild(0).getValue();
            //     uri = uri.substring(1,uri.length() - 1);                    
            //     ret.put(XMLConstants.DEFAULT_NS_PREFIX,
            //             uri);
            // }
        }
        return ret;
    }

    /**
     * Pretty-print a DOM Node.
     * @param doc The document to print.
     * @param os The chosen output.
     */
    private static void print(org.w3c.dom.Node node, OutputStream os)
        throws IOException, TransformerConfigurationException,
               TransformerException {
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(os);
        transformer.transform(source, result);
    }
    private static void print(org.w3c.dom.Node node)
        throws IOException, TransformerConfigurationException,
               TransformerException  {
        print(node, System.out);
    }

    /**
     * Append a new `xpath' Element to our output document.
     * @param root The root element to which this xpath element should
     *             be appended.
     * @param doc  The DOM Document.
     * @param q    The XPath query text
     * @param f    The name of the input file.
     * @param l    The line number (as a String).
     * @param c    The column number (as a String).
     * @param ns   A namespace mapping from prefixes to URIs.
     * @return The XQueryX subelement.
     */
    private static Element append(Element root,
                                  Document doc,
                                  String q,
                                  String f,
                                  String l,
                                  String c,
                                  Map<String,String> ns) {
        System.out.println("\n"+q);
        System.out.println(f);
        System.out.println(l);
        System.out.println(c);
        System.out.println(ns+"\n");
        
        Element ast   = doc.createElementNS
            (XMLConstants.DEFAULT_NS_PREFIX, "ast");
        Element xpath = doc.createElementNS
            (XMLConstants.DEFAULT_NS_PREFIX, "xpath");
            
        xpath.setAttributeNS(XMLConstants.DEFAULT_NS_PREFIX,
                             "filename", f);
        xpath.setAttributeNS(XMLConstants.DEFAULT_NS_PREFIX,
                             "line", l);
        xpath.setAttributeNS(XMLConstants.DEFAULT_NS_PREFIX,
                             "column", c);
        for (Map.Entry<String,String> pair : ns.entrySet()) {
            /* We do not care about default namespaces! */
            // if (pair.getKey().equals(XMLConstants.DEFAULT_NS_PREFIX))
            //     xpath.setAttributeNS
            //         (XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
            //          XMLConstants.XMLNS_ATTRIBUTE,
            //          pair.getValue());
            // else
            xpath.setAttributeNS
                (XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                 XMLConstants.XMLNS_ATTRIBUTE+":"+pair.getKey(),
                 pair.getValue());                
        }

        if (!q.isEmpty()) {
            Element query = doc.createElementNS
                (XMLConstants.DEFAULT_NS_PREFIX, "query");
            Text    text  = doc.createTextNode(q);
            query.appendChild(text);        
            xpath.appendChild(query);
        }
        
        xpath.appendChild(ast);
        root.appendChild(xpath);
        return ast;
    }
}
