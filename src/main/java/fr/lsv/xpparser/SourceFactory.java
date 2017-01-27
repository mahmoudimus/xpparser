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
import java.io.BufferedReader;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SourceFactory {

    // at the moment, we can build two types of sources
    public enum SourceType {
        XML, XQUERY
    };
    private SourceType st;

    // stuff that is needed by all the sources
    private DocumentBuilder db;
    protected boolean unique;

    // stuff required by XML sources
    private String filter;
    private PositionalXMLReader xmlreader;
    private XPath xpinterpreter;
    private Set<String> queries;

    // XML validation
    private ValidationFarm farm;

    // XSLT translation
    private XMLPrinter xslt;

    public SourceFactory(boolean unique)
        throws ParserConfigurationException {
        
        this.st = SourceType.XQUERY;
        
        // Get a DOM document builder
        DocumentBuilderFactory dbf
            = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(true);
        db = dbf.newDocumentBuilder();

        this.queries = new HashSet<String>(1000);
        this.unique = unique;
    }

    public SourceFactory(String filter, boolean unique)
        throws ParserConfigurationException {
        
        this(unique);
        this.st = SourceType.XML;
        this.filter = filter;
    }

    // Basic accessors
    public void setValidator(ValidationFarm v) {
        this.farm = v;
    }
    public void setTransformer(XMLPrinter x) {
        this.xslt = x;
    }
    
    protected Iterable<Map.Entry<String,String>> validate(Node n) {
        if (farm != null)
            try {
                return farm.validate(n);
            } catch (IOException e) {
                return new LinkedList<Map.Entry<String,String>>();
            }
        return new LinkedList<Map.Entry<String,String>>();
    }

    protected String transform(Node node) {
        if (xslt != null)
            try {
                return xslt.transform(node);
            } catch (Exception e) {
                return "";
            }
        return "";
    }

    protected Set<String> getQueries() {
        return queries;
    }

    protected PositionalXMLReader getXMLReader() throws SAXException {
        // We use a special XML parser that handles line/col numbers
        // and also processes the namespace information
        if (xmlreader == null)
            xmlreader = new PositionalXMLReader(db);
        return xmlreader;
    }

    protected Document newDocument() {
        return db.newDocument();
    }
        
    protected XPath getXPathInterpreter() throws SAXException {
        if (xpinterpreter == null)  {
            xpinterpreter = XPathFactory.newInstance().newXPath();
            xpinterpreter.setNamespaceContext(getXMLReader());
        }
        return xpinterpreter;
    }    

    protected String getFilter() {
        return filter;
    }
    
    public Iterable<XPathEntry> getSource(String filename, BufferedReader stream)
        throws Exception {
        
        switch (st) {
        case XML:
            return new XMLSource(filename, stream, this);
        case XQUERY:
            return new XQuerySource(filename, stream, this);
        default: // the compiler should know this is exhaustive!!!
            return null;
        }
    }
    public Iterable<XPathEntry> getSource(Map.Entry<String, BufferedReader> stream)
        throws Exception {

        return this.getSource(stream.getKey(), stream.getValue());
    }

}
