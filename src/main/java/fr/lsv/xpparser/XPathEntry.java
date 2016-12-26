package fr.lsv.xpparser;

import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public abstract class XPathEntry {

    /**
     * The name of the file from which this entry was extracted.
     */
    private String filename;
    
    private Document doc;

    private org.w3c.dom.Node domnode;

    private SourceFactory sf;
    

    protected XPathEntry(String filename, SourceFactory sf) {
        this.filename   = filename;
        this.sf         = sf;
        this.doc        = sf.newDocument();
        this.domnode    = null;
    }

    public abstract SimpleNode getASTNode();

    public abstract Map<String,String> getNamespaces();

    public abstract String getEntryText()
        throws UnsupportedOperationException;
    
    public abstract String getLine();

    public abstract String getColumn();

    public String getFilename() {
        return filename;
    }
    
    public org.w3c.dom.Node getDOMNode() throws IOException {
        if (domnode == null) {
            // benchmark information setup
            Element ast   = doc.createElementNS
                (XMLConstants.DEFAULT_NS_PREFIX, "ast");
            Element xpath = doc.createElementNS
                (XMLConstants.DEFAULT_NS_PREFIX, "xpath");
            
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
            try {
                Text    text  = doc.createTextNode(getEntryText());
                Element query = doc.createElementNS
                    (XMLConstants.DEFAULT_NS_PREFIX, "query");
                query.appendChild(text);
                xpath.appendChild(query);
            } catch (UnsupportedOperationException e) {}
            xpath.appendChild(ast);
            doc.appendChild(xpath);

            // obtain XQueryX DOM tree
            XPathXConverter xxc = new XPathXConverter();
            xxc.transform(getASTNode(), ast);
            domnode = ast.getFirstChild();

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
        }
        return domnode;
    }

    public void print(PrintStream os) throws Exception {
        XMLPrinter printer = new XMLPrinter();
        getDOMNode();
        printer.transform(doc, os);
    }

    public void print() throws Exception {
        this.print(System.out);
    }

}
