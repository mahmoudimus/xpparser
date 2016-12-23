package fr.lsv.xpparser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import javax.xml.transform.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;

/**
 * Utility class for applying XSLT transformations and printing DOM nodes.
 */
public class XMLPrinter {

    private Transformer transformer;

    private XMLPrinter (Transformer t) {
        this.transformer = t;
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty
            ("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    }        
    /**
     * An XMLPrinter that just prints.
     */
    public XMLPrinter ()
        throws TransformerConfigurationException {

        this(TransformerFactory.newInstance().newTransformer());
    }

    /**
     * An XMLPrinter that applies an XSLT stylesheet.
     * @param stylestream An XSLT stylesheet.
     */
    public XMLPrinter (Reader stylestream) 
        throws TransformerConfigurationException {

        this(TransformerFactory.newInstance()
             .newTransformer(new StreamSource(stylestream)));
    }

    protected void transform(org.w3c.dom.Node node, Result result)
        throws TransformerException {

        DOMSource source = new DOMSource(node);
        transformer.transform(source, result);
    }

    /**
     * Output the result in a DOM node.
     * @param node The node to print.
     * @param resultnode The node where the result should be printed.
     */
    public void transform(org.w3c.dom.Node node,
                          org.w3c.dom.Node resultnode)
        throws TransformerException {

        DOMResult result = new DOMResult(resultnode);
        this.transform(node, result);
    }

    /**
     * Output the result on an OutputStream.
     * @param node The node to print.
     * @param os The output stream where the result should be printed.
     */
    public void transform(org.w3c.dom.Node node, OutputStream os)
        throws TransformerException {

        StreamResult result = new StreamResult(os);
        this.transform(node, result);
    }

}
