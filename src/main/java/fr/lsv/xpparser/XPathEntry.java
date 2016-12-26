package fr.lsv.xpparser;

import java.io.PrintStream;
import java.util.Map;
import javax.xml.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    
    public org.w3c.dom.Node getDOMNode() {
        if (domnode == null) {
            Element e = doc.createElementNS(XMLConstants.NULL_NS_URI, "ast");
            doc.appendChild(e);
            XPathXConverter xxc = new XPathXConverter();
            xxc.transform(getASTNode(), e);
            domnode = e.getFirstChild();
        }
        return domnode;
    }

    public void print(PrintStream os) throws Exception {
        XMLPrinter printer = new XMLPrinter();
        printer.transform(getDOMNode(), os);
        // String eol = System.getProperty("line.separator", "\n");
        // os.println("  <xpath file=\""+ getFilename() +"\"");
        // os.print("    line=\""+ getLine()
        //            +"\" column=\""+ getColumn() +"\"");
        // for (Map.Entry<String,String> pair : getNamespaces().entrySet())
        //     if (pair.getKey().equals(XMLConstants.DEFAULT_NS_PREFIX))
        //         os.print(eol+"    defaultns=\""+ pair.getValue() +"\"");
        //     else
        //         os.print(eol+"    "+ XMLConstants.XMLNS_ATTRIBUTE
        //                    +":"+ pair.getKey() +"=\""+ pair.getValue() +"\"");
        // os.println(">");
        // try {
        //     os.println("    <query>"+ getEntryText() +"</query>");
        // } catch (UnsupportedOperationException e) {}

        // os.println("    <ast>");
        // XPathXPrinter xpp = sf.getXPathPrinter();
        // xpp.transform(getASTNode(), os);
        // os.println("    </ast>");
        // os.println("  </xpath>");
    }

    public void print() throws Exception {
        this.print(System.out);
    }

}
