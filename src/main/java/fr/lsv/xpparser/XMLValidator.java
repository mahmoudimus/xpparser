/*
Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License in `LICENCE-GPL` for more details.
 */
package fr.lsv.xpparser;

import java.io.IOException;
import java.io.Reader;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/**
 * Utility class for validating against multiple XML Schemas.
 */
public class XMLValidator {

    public static final String VALID = "valid";

    private List<Map.Entry<String,Validator>> schemas;

    private SchemaFactory sf;

    public XMLValidator() {
        this.schemas = new LinkedList<Map.Entry<String,Validator>>();
        this.sf = 
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    /**
     * Add an XML Schema to validate against.
     * @param filename The path to the XML Schema.
     * @param schema   The input stream for this Schema.
     */
    public void addSchema (String filename, Reader schema)
        throws SAXException {
        
        schemas.add(new AbstractMap.SimpleEntry
                    (filename, 
                     sf.newSchema(new StreamSource(schema)).newValidator()));
    }

    /**
     * Run a bunch of validation checks.  Each diagnostic holds the
     * name of the schema (to be obtained via Map.Entry#getKey()) and
     * either VALID or the error message.
     */
    public Iterable<Map.Entry<String,String>> 
        validate (Node node) throws IOException {
    
        DOMSource source = new DOMSource(node);
        LinkedList<Map.Entry<String,String>> ret = 
            new LinkedList<Map.Entry<String,String>>();

        for (Map.Entry<String,Validator> v : schemas) {
            try {
                v.getValue().validate(source);
                ret.add(new AbstractMap.SimpleEntry
                        (v.getKey(), VALID));
            } catch (SAXException e) {
                ret.add(new AbstractMap.SimpleEntry
                        (v.getKey(), e.getMessage()));
            }
            v.getValue().reset();
        }
        return ret;
    }
}
