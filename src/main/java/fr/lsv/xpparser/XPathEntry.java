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
    private String filename;
    
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
            (XMLConstants.DEFAULT_NS_PREFIX, "xpath");
        doc.appendChild(xpath);
            
        xpath.setAttributeNS(XMLConstants.DEFAULT_NS_PREFIX,
                             "filename", getFilename());
        xpath.setAttributeNS(XMLConstants.DEFAULT_NS_PREFIX,
                             "line", getLine());
        xpath.setAttributeNS(XMLConstants.DEFAULT_NS_PREFIX,
                             "column", getColumn());
        for (Map.Entry<String,String> pair : getNamespaces().entrySet()) {
            if (pair.getKey().equals(XMLConstants.DEFAULT_NS_PREFIX))
                xpath.setAttributeNS
                    (XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
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
                (XMLConstants.DEFAULT_NS_PREFIX, "query");
            query.appendChild(text);
            xpath.appendChild(query);
        }
        xpath.appendChild(ast);

        // validate
        for (Map.Entry<String,String> result : sf.validate(domnode)) {
            Element val = doc.createElementNS
                (XMLConstants.DEFAULT_NS_PREFIX, "validation");
            val.setAttributeNS
                (XMLConstants.DEFAULT_NS_PREFIX,
                 "schema",
                 Paths.get(result.getKey()).getFileName().toString());
            if (result.getValue().equals(XMLValidator.VALID))
                val.setAttributeNS
                    (XMLConstants.DEFAULT_NS_PREFIX,
                     "valid", "yes");
            else {
                val.setAttributeNS
                    (XMLConstants.DEFAULT_NS_PREFIX,
                     "valid", "no");
                val.appendChild(doc.createTextNode(result.getValue()));
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
