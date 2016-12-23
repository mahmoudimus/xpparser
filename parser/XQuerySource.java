package org.w3c.xqparser;

import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class XQuerySource implements Iterable<XPathEntry> {

    final private Iterator<SimpleNode> nodeList;

    final private String filename;

    final private SourceFactory sf;

    public XQuerySource (String filename, Reader stream, SourceFactory sf)
        throws Exception {
        
        this.filename = filename;
        this.sf = sf;
        
        // parse the input XQuery
        XParser parser = new XParser(stream);
        SimpleNode ast = parser.START();
        nodeList = XPathVisitor.visit(ast).iterator();
    }

    public Iterator<XPathEntry> iterator() {
        return new Iterator<XPathEntry> () {
         
            public boolean hasNext() {
                return nodeList.hasNext();
            }
            
            public XPathEntry next() throws NoSuchElementException {
                return new XQueryXPathEntry(filename, sf, nodeList.next());
            }
            
            public void remove () throws UnsupportedOperationException {
                throw new UnsupportedOperationException
                    ("Cannot remove() from a Source.");
            }
        };
    }
}
