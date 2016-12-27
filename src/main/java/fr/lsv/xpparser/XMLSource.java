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
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.xqparser.ParseException;
import org.xml.sax.SAXException;

public class XMLSource implements Iterable<XPathEntry> {

    final private NodeList nodeList;

    final private String filename;

    final private SourceFactory sf;

    public XMLSource (String filename, Reader stream, SourceFactory sf)
        throws SAXException, IOException, XPathExpressionException {
        
        this.filename = filename;
        this.sf = sf;
        
        // parse the input XML
        Document d = sf.getXMLReader().parse(stream);

        // apply filter
        this.nodeList = (NodeList)
            sf.getXPathInterpreter().evaluate(sf.getFilter(), d,
                                              XPathConstants.NODESET);
    }
    
    public Iterator<XPathEntry> iterator() {
        return new Iterator<XPathEntry> () {

            private int counter = 0;
            
            public boolean hasNext() {
                return counter < nodeList.getLength();
            }

            public XPathEntry next()
                throws NoSuchElementException {
                
                if (counter >= nodeList.getLength())
                    throw new NoSuchElementException("");
                
                // the DOM node we are working on
                Node n = nodeList.item(counter++);
                
                // attempt to extract its XPath contents
                String q;
                switch (n.getNodeType()) {
                case Node.ATTRIBUTE_NODE:
                    q = n.getNodeValue().trim();
                    n = ((Attr)n).getOwnerElement();
                    break;
                case Node.TEXT_NODE:
                    q = n.getNodeValue().trim();
                    n = n.getParentNode();
                    break;
                    
                case Node.ELEMENT_NODE:
                    q = n.getTextContent().trim();
                    break;
                    
                default:
                    throw new NoSuchElementException
                        ("Couldn't process node "
                         + n.getTextContent() + " in "
                         + filename);
                }

                XPathEntry ret;
                try {
                    ret = new XMLXPathEntry(filename, sf, n, q);
                } catch (ParseException e) {
                    // we have to treat this error here
                    throw new NoSuchElementException
                        (": "+ filename +" line "
                         + (String)n.getUserData
                           (PositionalXMLReader.LINE_NUMBER_KEY_NAME) +": "
                         + "Could not parse XPath expression `"
                         + q +"`: "
                         + System.getProperty("line.separator", "\n")
                         + e.getMessage());
                }
                return ret;
            }
            
            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException
                    ("Cannot remove() from a Source.");
            }
        };
    }
}
