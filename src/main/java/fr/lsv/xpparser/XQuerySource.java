/*
Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License in `LICENCE-GPL` for more details.
 */
package fr.lsv.xpparser;

import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.w3c.xqparser.SimpleNode;
import org.w3c.xqparser.XParser;

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
