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

import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.xqparser.SimpleNode;

public abstract class XPathEntry {

    /**
     * The name of the file from which this entry was extracted.
     */
    String filename;
    
    private Document doc;

    private Node domnode;

    SourceFactory sf;
    

    protected XPathEntry(String filename, SourceFactory sf) {
        this.filename   = filename;
        this.sf         = sf;
        this.doc        = sf.newDocument();
        this.domnode    = null;
    }

    public abstract SimpleNode getASTNode();

    public abstract Map<String,String> getNamespaces();

    public abstract String getEntryText();
    
    public abstract String getLine();

    public abstract String getColumn();

    public String getFilename() {
        return filename;
    }
    
    public org.w3c.dom.Node getDOMNode() {
        if (domnode == null) {
            Element ast = doc.createElementNS
                (XMLConstants.DEFAULT_NS_PREFIX, "ast");
            // obtain XQueryX DOM tree
            XPathXConverter xxc = new XPathXConverter();
            xxc.transform(getASTNode(), ast);
            domnode = ast.getFirstChild();
        }
        return domnode;
    }

    public void print(PrintStream os) throws Exception {
        XMLPrinter printer = new XMLPrinter();
        org.w3c.dom.Node ast = getDOMNode().getParentNode();
        Element xpath = doc.createElementNS
            (XMLConstants.NULL_NS_URI, "xpath");
        doc.appendChild(xpath);
            
        xpath.setAttributeNS(XMLConstants.NULL_NS_URI,
                             "filename", getFilename());
        xpath.setAttributeNS(XMLConstants.NULL_NS_URI,
                             "line", getLine());
        xpath.setAttributeNS(XMLConstants.NULL_NS_URI,
                             "column", getColumn());
        for (Map.Entry<String,String> pair : getNamespaces().entrySet()) {
            if (pair.getKey().equals(XMLConstants.DEFAULT_NS_PREFIX))
                xpath.setAttributeNS
                    (XMLConstants.NULL_NS_URI,
                     "defaultns",
                     pair.getValue());
            else
                xpath.setAttributeNS
                    (XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                     XMLConstants.XMLNS_ATTRIBUTE+":"+pair.getKey(),
                     pair.getValue());      
        }
        if (!getEntryText().isEmpty()) {
            Text    text  = doc.createTextNode(getEntryText());
            Element query = doc.createElementNS
                (XMLConstants.NULL_NS_URI, "query");
            query.appendChild(text);
            xpath.appendChild(query);
        }
        xpath.appendChild(ast);

        // validate
        for (Map.Entry<String,String> result : sf.validate(domnode)) {
            Element val = doc.createElementNS
                (XMLConstants.NULL_NS_URI, "validation");
            val.setAttributeNS
                (XMLConstants.NULL_NS_URI,
                 "schema",
                 Paths.get(result.getKey()).getFileName().toString());
            if (result.getValue().equals(ValidationFarm.VALID))
                val.setAttributeNS
                    (XMLConstants.DEFAULT_NS_PREFIX,
                     "valid", "yes");
            else {
                String msg = result.getValue();
                int i = msg.indexOf(':');
                int j = msg.indexOf(';');
                if (j >= 0 && i < j)
                    msg = msg.substring(i+2, j);
                else if (i >= 0 && j < i)
                    msg = msg.substring(j+2);
                val.setAttributeNS
                    (XMLConstants.DEFAULT_NS_PREFIX,
                     "valid", "no");
                val.appendChild(doc.createTextNode(msg));
            }
            xpath.appendChild(val);
        }
        
        // print
        printer.transform(doc, os);
    }

    public void print() throws Exception {
        this.print(System.out);
    }

}
