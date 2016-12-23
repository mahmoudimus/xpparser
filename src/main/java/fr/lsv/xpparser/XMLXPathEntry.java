package fr.lsv.xpparser;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;


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
                         org.w3c.dom.Node domnode,
                         String entry) throws ParseException {
        super(filename, sf);
        this.domnode = domnode;
        this.entry = entry;

        // parse `entry' to get an AST
        Reader r = new StringReader(entry);
        XParser parser = new XParser(r);
        this.astnode = parser.START();        

        // recover the namespace information from the DOM
        String s = (String)domnode.getUserData
            (PositionalXMLReader.NAMESPACES_KEY_NAME);
        this.namespaces = new HashMap<String,String>();
        String[] split = s.substring(1,s.length() - 1).split("=|, ");
        for (int i = 0; i < split.length; i += 2)
            this.namespaces.put(split[i], split[i+1]);
    }

    public SimpleNode getASTNode() {
        return astnode;
    }        

    public Map<String,String> getNamespaces() {
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
