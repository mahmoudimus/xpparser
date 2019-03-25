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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Result;
import org.w3c.dom.Node;

/**
 * Utility class for applying XSLT transformations and printing DOM nodes.
 */
public class XMLPrinter {

    private static final String defaultTransform =
 "<xsl:stylesheet version=\"2.0\""
+"  xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\""
+"  xmlns:xqx=\"http://www.w3.org/2005/XQueryX\">"
+"<xsl:output method=\"xml\" omit-xml-declaration=\"yes\" indent=\"yes\"/>"
+"<xsl:strip-space elements=\"*\"/>"
+"<xsl:template match=\"@*|node()\">"
+"    <xsl:copy>"
+"      <xsl:apply-templates select=\"@*|node()\"/>"
+"    </xsl:copy>"
+"  </xsl:template>"
+"  <xsl:template match=\"xqx:stringConcatenateOp"
+"                       |xqx:addOp"
+"                       |xqx:subtractOp"
+"                       |xqx:multiplyOp"
+"                       |xqx:divOp"
+"                       |xqx:idivOp"
+"                       |xqx:modOp"
+"                       |xqx:unionOp"
+"                       |xqx:intersectOp"
+"                       |xqx:exceptOp"
+"                       |andOp"
+"                       |xqx:orOp"
+"                       |xqx:eqOp"
+"                       |xqx:neOp"
+"                       |xqx:ltOp"
+"                       |xqx:leOp"
+"                       |xqx:gtOp"
+"                       |xqx:geOp"
+"                       |xqx:equalOp"
+"                       |xqx:notEqualOp"
+"                       |xqx:lessThanOp"
+"                       |xqx:lessThanOrEqualOp"
+"                       |xqx:lessThanOrEqualOp"
+"                       |xqx:greaterThanOp"
+"                       |xqx:greaterThanOrEqualOp"
+"                       |xqx:isOp"
+"                       |xqx:nodeBeforeOp"
+"                       |xqx:nodeAfterOp\">"
+"    <xsl:element name=\"{name(.)}\">"
+"      <xqx:firstOperand>"
+"        <xsl:apply-templates select=\"*[1]\"/>"
+"      </xqx:firstOperand>"
+"      <xqx:secondOperand>"
+"        <xsl:apply-templates select=\"*[2]\"/>"
+"      </xqx:secondOperand>"
+"    </xsl:element>"
+"  </xsl:template>"
+"</xsl:stylesheet>";

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

        this(TransformerFactory.newInstance()
             .newTransformer(// new StreamSource
                             // (new StringReader(defaultTransform))
                             ));
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

    protected void transform(Node node, Result result)
        throws TransformerException {

        DOMSource source = new DOMSource(node);
        transformer.transform(source, result);
    }

    /**
     * Output the result in a DOM node.
     * @param node The node to print.
     * @return The string output
     */
    public String transform(Node node)
        throws TransformerException {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        this.transform(node, result);
        return writer.toString();
    }

    /**
     * Output the result on an OutputStream.
     * @param node The node to print.
     * @param os The output stream where the result should be printed.
     */
    public void transform(Node node, OutputStream os)
        throws TransformerException {

        StreamResult result = new StreamResult(os);
        this.transform(node, result);
    }

}
