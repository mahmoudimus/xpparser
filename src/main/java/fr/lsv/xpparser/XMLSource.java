package fr.lsv.xpparser;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
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
                org.w3c.dom.Node n = nodeList.item(counter++);
                
                // attempt to extract its XPath contents
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
                    throw new NoSuchElementException
                        ("Couldn't process node "
                         + n.getTextContent() + " in "
                         + filename);
                }

                XPathEntry ret;
                try {
                    ret = new XMLXPathEntry(filename, sf, n, q);
                } catch (ParseException e) {
                    throw new NoSuchElementException(e.toString());
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
