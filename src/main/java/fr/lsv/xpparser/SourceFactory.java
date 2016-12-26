package fr.lsv.xpparser;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Map;
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

    // stuff required by XML sources
    private String filter;
    private PositionalXMLReader xmlreader;
    private XPath xpinterpreter;

    // XML validation
    private XMLValidator validator;

    // XSLT translation
    private XMLPrinter xslt;

    public SourceFactory() throws ParserConfigurationException {
        this.st = SourceType.XQUERY;
        
        // Get a DOM document builder
        DocumentBuilderFactory dbf
            = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(true);
        db = dbf.newDocumentBuilder();
    }

    public SourceFactory(String filter) throws ParserConfigurationException {
        this();
        this.st = SourceType.XML;
        this.filter = filter;
    }

    // Basic accessors
    public void setValidator(XMLValidator v) {
        this.validator = v;
    }
    public void setTransformer(XMLPrinter x) {
        this.xslt = x;
    }
    
    protected Iterable<Map.Entry<String,String>> validate(Node n) {
        if (validator != null)
            try {
                return validator.validate(n);
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
    
    public Iterable<XPathEntry> getSource(String filename, Reader stream)
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
    public Iterable<XPathEntry> getSource(Map.Entry<String, Reader> stream)
        throws Exception {

        return this.getSource(stream.getKey(), stream.getValue());
    }

}
