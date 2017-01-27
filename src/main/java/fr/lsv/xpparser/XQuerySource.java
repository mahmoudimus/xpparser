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

    final private BufferedReader stream;

    final private Counter line;
    final private Counter column;

    final private SourceFactory sf;

    public XQuerySource (String filename, BufferedReader str, SourceFactory sf)
        throws Exception {
        
        this.filename = filename;
        this.sf = sf;
        BufferedReader br = null;
        try {
            br = Files.newBufferedReader
                (Paths.get(filename), Charset.defaultCharset());
        } catch (Exception e) {
            assert false;
        }
        this.stream = br;
        System.err.println("file: "+filename+"; "+br);
        
        // parse the input XQuery
        XParser parser = new XParser(str);
        SimpleNode ast = parser.START();
        nodeList = XPathVisitor.visit(ast).iterator();

        this.line = new Counter();
        this.column = new Counter();
    }

    public Iterator<XPathEntry> iterator() {
        return new Iterator<XPathEntry> () {
         
            public boolean hasNext() {
                return nodeList.hasNext();
            }
            
            public XPathEntry next() throws NoSuchElementException {
                SimpleNode node = nodeList.next();
                String query = "";

                if (stream != null)
                    try {
                        // recover query from the stream
                        assert (line.get() <= node.beginLine);
                        while (line.get() < node.beginLine) {
                            column.reset();
                            stream.readLine();
                            line.increment();
                        }
                        stream.skip(node.beginColumn - column.get());
                        while (line.get() < node.endLine) {
                            column.reset();
                            query += stream.readLine();
                            line.increment();
                        }
                        int size = node.endColumn - column.get();
                        char[] buf = new char[size];
                        stream.read(buf, 0, size);
                        query += new String(buf);
                        column.set(node.endColumn);
                        System.err.println("query: "+query);
                    } catch (IOException e) {
                        throw new NoSuchElementException(e.getMessage());
                    }                   
                    
                return new XQueryXPathEntry(filename, sf, node, query);
            }
            
            public void remove () throws UnsupportedOperationException {
                throw new UnsupportedOperationException
                    ("Cannot remove() from a Source.");
            }
        };
    }

    private class Counter {
        private int value;

        public Counter () {
            this.value = 0;
        }

        public void increment() {
            value++;
        }

        public void reset() {
            value = 0;
        }

        public int get() {
            return value;
        }
        
        public void set(int v) {
            value = v;
        }
    };
}
