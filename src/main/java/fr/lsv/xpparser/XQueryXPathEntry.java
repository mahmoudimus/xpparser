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

import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import org.w3c.xqparser.SimpleNode;
import org.w3c.xqparser.XParserTreeConstants;

public class XQueryXPathEntry extends XPathEntry {
    /**
     * Internal AST representation obtained from JavaCC.
     */
    private SimpleNode astnode;

    /**
     * Normalised query text obtained through an XSLT translation on
     * the DOM node.
     */
    private String query;

    /**
     * Mapping from prefixes to URIs.
     */
    private Map<String,String> namespaces;

    
    public XQueryXPathEntry(String filename,
                            SourceFactory sf,
                            SimpleNode node) {
        super(filename, sf);
        this.astnode = node;
        this.query = query;

        // recover the namespace information from the Prolog
        SimpleNode n = this.astnode;
        this.namespaces = new HashMap<String,String>();

        // go up the AST to the Prolog
        while (n.id != XParserTreeConstants.JJTSTART
               && n.id != XParserTreeConstants.JJTMAINMODULE
               && n.id != XParserTreeConstants.JJTQUERYBODY)
            n = n.getParent();
        
        if (n.id == XParserTreeConstants.JJTQUERYBODY) {
            // recover sibling Prolog node
            n = n.getParent().getChild(0);
            assert(n.id == XParserTreeConstants.JJTPROLOG);
            
            SimpleNode c;
            for (int i = 0; i < n.jjtGetNumChildren(); i++)
                if ((c = n.getChild(i)).id ==
                    XParserTreeConstants.JJTNAMESPACEDECL) {
                    String uri = c.getChild(1).getChild(0).getValue();
                    uri = uri.substring(1,uri.length() - 1);
                    namespaces.put(c.getChild(0).getValue(), uri);
                }
                else if (c.id ==
                         XParserTreeConstants.JJTDEFAULTNAMESPACEDECL) {
                    String uri = c.getChild(0).getChild(0).getValue();
                    uri = uri.substring(1,uri.length() - 1);
                    namespaces.put(XMLConstants.DEFAULT_NS_PREFIX,
                                   uri);
                }
        }
    }

    public SimpleNode getASTNode() {
        return astnode;
    }        

    public Map<String,String> getNamespaces() {
        return namespaces;
    }

    public String getEntryText() {
        if (query == null) {
            try {
                query = sf.transform(getDOMNode());
            } catch (Exception e) { }                
        }
        return query;
    }
    
    public String getLine() {
        return String.valueOf(astnode.beginLine);
    }

    public String getColumn() {
        return String.valueOf(astnode.beginColumn);
    }
}
