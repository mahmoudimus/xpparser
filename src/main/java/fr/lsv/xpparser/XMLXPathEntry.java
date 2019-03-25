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

import java.io.Reader;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.xqparser.ParseException;
import org.w3c.xqparser.XParser;
import org.w3c.xqparser.SimpleNode;


public class XMLXPathEntry extends XPathEntry {
    /**
     * Internal AST representation obtained from JavaCC.
     */
    private SimpleNode astnode;

    /**
     * Mapping from prefixes to URIs.
     */
    private Map<String,String> namespaces;

    /**
     * DOM element containing the XPath expression.
     */
    private org.w3c.dom.Node domnode;

    /**
     * XPath entry text, to be compiled.
     */
    private String entry;

    public XMLXPathEntry(String filename,
                         SourceFactory sf,
                         Node domnode,
                         String entry) {
        super(filename, sf);
        this.domnode = domnode;
        this.entry = entry;
        this.astnode = null;
        this.namespaces = null;
    }

    public SimpleNode getASTNode() throws ParseException {
        if (astnode == null) {
            // parse `entry' to get an AST
            Reader r = new StringReader(entry);
            XParser parser = new XParser(r);
            this.astnode = parser.START();
            
            // check that it's correctly identified as an XPath node
            boolean assertsEnabled = false;
            assert assertsEnabled = true;
            if (assertsEnabled) {
                List<SimpleNode> check = XPathVisitor.visit(astnode);
                if (check.size() != 1)
                    {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        PrintStream ps = new PrintStream(os);
                        astnode.dump("  ", ps);
                        throw new ParseException("Multiple XPath expressions found: "
                                                 +check.toString()
                                                 +"; AST:"+os.toString());
                    }
            }
        }
        return astnode;
    }        

    public Map<String,String> getNamespaces() throws ParseException {
        if (namespaces == null) {
            getASTNode();
            // recover the namespace information from the DOM
            String s = (String)domnode.getUserData
                (PositionalXMLReader.NAMESPACES_KEY_NAME);
            this.namespaces = new HashMap<String,String>();
            String[] split = s.substring(1,s.length() - 1).split("=|, ");
            for (int i = 0; i < split.length - 1; i += 2)
                this.namespaces.put(split[i], split[i+1]);
        }
        return namespaces;
    }

    public String getEntryText() {
        return entry;
    }
    
    public String getLine() {
        return (String)domnode.getUserData
            (PositionalXMLReader.LINE_NUMBER_KEY_NAME);
    }

    public String getColumn() {
        return (String)domnode.getUserData
            (PositionalXMLReader.COL_NUMBER_KEY_NAME);
    }
}
