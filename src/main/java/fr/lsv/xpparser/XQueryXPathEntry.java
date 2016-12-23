package fr.lsv.xpparser;

import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;

public class XQueryXPathEntry extends XPathEntry {
    /**
     * Internal AST representation obtained from JavaCC.
     */
    private SimpleNode astnode;

    /**
     * Mapping from prefixes to URIs.
     */
    private Map<String,String> namespaces;

    
    public XQueryXPathEntry(String filename,
                            SourceFactory sf,
                            SimpleNode node) {
        super(filename, sf);
        this.astnode = node;

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

    public String getEntryText() throws UnsupportedOperationException {
        throw new UnsupportedOperationException
            ("XPath fragments in XQuery source cannot be recovered.\n");
    }
    
    public String getLine() {
        return String.valueOf(astnode.beginLine);
    }

    public String getColumn() {
        return String.valueOf(astnode.beginColumn);
    }
}
