package fr.lsv.xpparser;

import java.io.PrintStream;
import java.util.Map;
import javax.xml.XMLConstants;

public abstract class XPathEntry {

    /**
     * The name of the file from which this entry was extracted.
     */
    private String filename;
    private SourceFactory sf;
    
    protected XPathEntry(String filename, SourceFactory sf) {
        this.filename   = filename;
        this.sf         = sf;
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
    
    public void print(PrintStream os) throws Exception {
        os.println("  <xpath file=\""+ getFilename() +"\"");
        os.print("    line=\""+ getLine()
                   +"\" column=\""+ getColumn() +"\"");
        for (Map.Entry<String,String> pair : getNamespaces().entrySet())
            if (pair.getKey().equals(XMLConstants.DEFAULT_NS_PREFIX))
                os.print("\n    defaultns=\""+ pair.getValue() +"\"");
            else
                os.print("\n    "+ XMLConstants.XMLNS_ATTRIBUTE
                           +":"+ pair.getKey() +"=\""+ pair.getValue() +"\"");
        os.println(">");
        try {
            os.println("    <query>"+ getEntryText() +"</query>");
        } catch (UnsupportedOperationException e) {}

        os.println("    <ast>");
        XPathXPrinter xpp = sf.getXPathPrinter();
        xpp.transform(getASTNode(), os);
        os.println("    </ast>");
        os.println("  </xpath>");
    }

    public void print() throws Exception {
        this.print(System.out);
    }

}
