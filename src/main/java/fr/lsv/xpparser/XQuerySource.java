/*
Copyright (C) 2016-2019 Sylvain Schmitz (ENS Paris-Saclay).

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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.w3c.xqparser.SimpleNode;
import org.w3c.xqparser.XParser;

public class XQuerySource implements Iterable<XPathEntry> {

    final private Iterator<SimpleNode> nodeList;

    final private String filename;

    final private SourceFactory sf;

    public XQuerySource (String filename, BufferedReader str, SourceFactory sf)
        throws Exception {
        
        this.filename = filename;
        this.sf = sf;
        
        // parse the input XQuery
        XParser parser = new XParser(str);
        SimpleNode ast = parser.START();
        // for debugging the visitor: ast.dump("");
        nodeList = XPathVisitor.visit(ast).iterator();
    }

    public Iterator<XPathEntry> iterator() {
        return new Iterator<XPathEntry> () {
         
            public boolean hasNext() {
                return nodeList.hasNext();
            }
            
            public XPathEntry next() throws NoSuchElementException {
                SimpleNode node = nodeList.next();
                return new XQueryXPathEntry(filename, sf, node);
            }
            
            public void remove () throws UnsupportedOperationException {
                throw new UnsupportedOperationException
                    ("Cannot remove() from a Source.");
            }
        };
    }
}
