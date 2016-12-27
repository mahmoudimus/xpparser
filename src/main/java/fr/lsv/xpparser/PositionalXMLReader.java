/*
Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

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

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
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
public class PositionalXMLReader implements NamespaceContext {
    public final static String LINE_NUMBER_KEY_NAME = "lineNumber";
    public final static String COL_NUMBER_KEY_NAME = "colNumber";
    public final static String NAMESPACES_KEY_NAME = "namespaces";

    private SAXParser parser;
    private DocumentBuilder db;
    private Map<String,String> ns;
    private Map<String,String> local;
    private Map<String,LinkedList<String>> invert;

    /**
     * Basic constructor.
     */
    public PositionalXMLReader (DocumentBuilder db)
        throws SAXException {

        this.db = db;

        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(true);
            parser = factory.newSAXParser();
        } catch (final ParserConfigurationException e) {
            throw new
                RuntimeException("Can't create SAX parser.", e);
        }
    }

    /**
     * Get the matching URI for the prefix.
     * @param prefix a string like `xml', `xsl', etc.
     * @return the URI for this prefix.
     */
     public String getNamespaceURI(final String prefix)
         throws IllegalArgumentException {

         if (prefix == null)
             throw new IllegalArgumentException("No prefix");
         else if (prefix.equals(XMLConstants.XML_NS_PREFIX))
             return XMLConstants.XML_NS_URI;
         else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
             return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
         else if (ns.containsKey(prefix))
             return ns.get(prefix);
         return XMLConstants.NULL_NS_URI;
     }
    
    /**
     * Get some matching prefix for the URI.
     * @param uri An URI.
     * @return a prefix.
     */
     public String getPrefix(final String uri)
         throws IllegalArgumentException {

         if (uri == null)
             throw new IllegalArgumentException("No URI");
         else if (uri.equals(XMLConstants.XML_NS_PREFIX))
             return XMLConstants.XML_NS_URI;
         else if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE))
             return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
         else if (invert.containsKey(uri))
             return invert.get(uri).element();
         return null;
     }
    
    /**
     * Get an Iterator over the prefixes matching the URI.
     * @param uri
     * @return An Iterator
     */
    public Iterator getPrefixes(final String uri)
        throws IllegalArgumentException {

        if (uri == null)
            throw new IllegalArgumentException("No URI");
        else if (uri.equals(XMLConstants.XML_NS_PREFIX)) {
            LinkedList<String> l = new LinkedList<String>();
            l.add(XMLConstants.XML_NS_URI);
            return l.iterator();
        }
        else if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            LinkedList<String> l = new LinkedList<String>();
            l.add(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
            return l.iterator();
        }
        else if (invert.containsKey(uri))
            return invert.get(uri).iterator();
        
        return new LinkedList<String>().iterator();        
    }
    
    /**
     * Parse the input document.
     * @param stream The input document.
     * @return A DOM tree with user data.
     */
    public Document parse(final Reader stream)
        throws IOException, SAXException {
        // empty the namespace information
        ns = new HashMap<String,String>();
        local = new HashMap<String,String>();
        invert = new HashMap<String,LinkedList<String>>();
        
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
                public void startPrefixMapping(final String prefix,
                                               final String uri) {
                    // process namespace declarations
                    if (!invert.containsKey(uri)) {
                        LinkedList<String> l = new LinkedList<String>();
                        l.add(prefix);
                        invert.put(uri, l);
                    }
                    else
                        invert.get(uri).add(prefix);
                    ns.put(prefix, uri);
                    local.put(prefix, uri);
                }

                @Override
                public void endPrefixMapping(final String prefix) {
                    local.remove(prefix);
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
                    
                    // add attributes
                    for (int i = 0; i < attributes.getLength(); i++) {
                        el.setAttributeNS(attributes.getURI(i),
                                          attributes.getQName(i),
                                          attributes.getValue(i));
                    }
                    
                    // add line/column information to the element
                    el.setUserData(LINE_NUMBER_KEY_NAME,
                                   String.valueOf(locator.getLineNumber()),
                                   null);
                    el.setUserData(COL_NUMBER_KEY_NAME,
                                   String.valueOf(locator.getColumnNumber()),
                                   null);

                    // add namespace information to the element
                    el.setUserData(NAMESPACES_KEY_NAME,
                                   local.toString(),
                                   null);
                    
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
