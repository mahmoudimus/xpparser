/*
 * Add position information to the DOM tree.
 */
package org.w3c.xqparser;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The basic DOM implementation in Java does not provide access to
 * line numbers and column numbers.  However, in DOM 3, we can add
 * user data to org.w3c.dom.Node.  This class uses the SAX parser to
 * parse the input and add line and column user information.
 *
 * We also take this opportunity to extract the namespace information
 * from the document, which can be later queried through the
 * getNamespaceURI() method.
 *
 * Based on
 * http://stackoverflow.com/questions/4915422/get-line-number-from-xml-node-java.
 */
public class PositionalXMLReader {
    public final static String LINE_NUMBER_KEY_NAME = "lineNumber";
    public final static String COL_NUMBER_KEY_NAME = "colNumber";

    private SAXParser parser;
    private DocumentBuilder db;
    public Map<String,String> ns;

    /**
     * Basic constructor.
     */
    public PositionalXMLReader () throws SAXException {
        ns = new HashMap<String,String>();
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(true);
            parser = factory.newSAXParser();
            final DocumentBuilderFactory dbf
                = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            db = dbf.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new
                RuntimeException("Can't create SAX parser / DOM builder.",
                                 e);
        }
    }

    /**
     * Get the matching URI for the prefix.
     * @param prefix a string like `xml', `xsl', etc.
     * @return the URI for this prefix.
     */
     public String getNamespaceURI(final String prefix) {
         if (prefix == null)
             throw new NullPointerException("No prefix");
         else if (prefix.equals(XMLConstants.XML_NS_PREFIX))
             return XMLConstants.XML_NS_URI;
         else if (ns.containsKey(prefix))
             return ns.get(prefix);
         return XMLConstants.NULL_NS_URI;
     }

    /**
     * Parse the input document.
     * @param stream The input document.
     * @return A DOM tree with user data.
     */
    public Document parse(final Reader stream)
        throws IOException, SAXException {
        final Document doc = db.newDocument();
        final Stack<Element> elementStack = new Stack<Element>();
        final StringBuilder textBuffer = new StringBuilder();
        final DefaultHandler handler = new DefaultHandler() {
                private Locator locator;

                @Override
                public void setDocumentLocator(final Locator locator) {
                    // Save the locator, so that it can be used later
                    // for line tracking when traversing nodes.
                    this.locator = locator;
                }
                
                @Override
                public void startElement(final String uri,
                                         final String localName,
                                         final String qName,
                                         final Attributes attributes)
                    throws SAXException {
                    addTextIfNeeded();

                    // create element
                    final Element el = doc.createElementNS(uri, qName);

                    // update namespace information
                    int i;
                    if (uri != null
                        && !uri.isEmpty()
                        && (i = qName.indexOf(':')) >= 0)
                        ns.put(qName.substring(0,i), uri);
                    
                    // add attributes
                    for (i = 0; i < attributes.getLength(); i++) {
                        el.setAttribute(attributes.getQName(i),
                                        attributes.getValue(i));
                    }
                    
                    // add line/column information to the element
                    el.setUserData(LINE_NUMBER_KEY_NAME,
                                   String.valueOf(locator.getLineNumber()),
                                   null);
                    el.setUserData(COL_NUMBER_KEY_NAME,
                                   String.valueOf(locator.getColumnNumber()),
                                   null);

                    // add line/column information to the attributes
                    NamedNodeMap nm = el.getAttributes();
                    for (i = 0; i < nm.getLength(); i++) {
                        nm.item(i).setUserData(LINE_NUMBER_KEY_NAME,
                                   String.valueOf(locator.getLineNumber()),
                                   null);
                        nm.item(i).setUserData(COL_NUMBER_KEY_NAME,
                                   String.valueOf(locator.getColumnNumber()),
                                   null);
                    }
                    elementStack.push(el);
                }

            @Override
            public void endElement(final String uri,
                                   final String localName,
                                   final String qName) {
                addTextIfNeeded();
                final Element closedEl = elementStack.pop();
                if (elementStack.isEmpty()) {
                    // Is this the root element?
                    doc.appendChild(closedEl);
                } else {
                    final Element parentEl = elementStack.peek();
                    parentEl.appendChild(closedEl);
                }
            }
                
            @Override
            public void characters(final char ch[],
                                   final int start,
                                   final int length)
                throws SAXException {
                textBuffer.append(ch, start, length);
            }

            // Outputs text accumulated under the current node
            private void addTextIfNeeded() {
                if (textBuffer.length() > 0) {
                    final Element el = elementStack.peek();
                    // create text node
                    final org.w3c.dom.Node tn =
                        doc.createTextNode(textBuffer.toString());
                    // add line/column information to the text node
                    tn.setUserData(LINE_NUMBER_KEY_NAME,
                                   String.valueOf(locator.getLineNumber()),
                                   null);
                    tn.setUserData(COL_NUMBER_KEY_NAME,
                                   String.valueOf(locator.getColumnNumber()),
                                   null);
                    // insert the text node
                    el.appendChild(tn);
                    textBuffer.delete(0, textBuffer.length());
                }
            }
        };
        parser.parse(new InputSource(stream), handler);

        return doc;
    }
}
