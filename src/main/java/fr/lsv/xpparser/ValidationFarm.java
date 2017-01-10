/*
Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

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

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/**
 * Utility class for validating against multiple XML Schemas.
 */
public class ValidationFarm {

    public static final String VALID = "valid";

    private List<Map.Entry<String,Validator>> schemas;

    private SchemaFactory xsdFactory;

    private SchemaReader rncFactory;

    private PropertyMapBuilder pb;

    public ValidationFarm () {
        this.schemas = new LinkedList<Map.Entry<String,Validator>>();
        this.xsdFactory = 
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // we are using Relax NG compact format
        this.rncFactory = CompactSchemaReader.getInstance();
        //ErrorHandler seh = new ErrorHandlerImpl();  
        this.pb = new PropertyMapBuilder();
        //pb.put(ValidateProperty.ERROR_HANDLER, seh);
    }

    /**
     * Add an XML Schema to validate against.
     * @param filename The path to the XML Schema.
     * @param schema   The input stream for this Schema.
     */
    public void addXMLSchema (String filename, Reader schema)
        throws SAXException {

        final Path path = Paths.get(filename);
        this.xsdFactory.setResourceResolver(new LSResourceResolver () {
                @Override
                public LSInput resolveResource(String type,
                                               String namespaceURI,
                                               String publicId,
                                               String systemId,
                                               String baseURI) {
        // The base resource that includes this current resource
                    Path resourcePath =
                        path.resolveSibling(systemId).normalize();
                    try {
                        return new LSInputImpl(publicId, systemId,
                                               Main.getInput(resourcePath));
                    } catch (IOException e) {
                        System.err.println(e.toString());
                        return null;
                    }
                }
            });
        schemas.add(new AbstractMap.SimpleEntry
                    (filename, 
                     new XSDValidator
                     (xsdFactory.newSchema
                      (new StreamSource(schema)).newValidator())));
    }
    
    /**
     * Add a RelaxNG Schema to validate against.
     * @param filename The path to the RelaxNG schema.
     * @param schema   The input stream for this schema.
     */
    public void addRNCSchema (String filename, Reader schema)
        throws SAXException {
        final Path path = Paths.get(filename);
        this.pb.put
            (ValidateProperty.ENTITY_RESOLVER,
             new EntityResolver () {
                 private Path resourcePath = path;
                        
                 @Override
                 public InputSource resolveEntity(String publicId,
                                                  String systemId) {
                     System.err.println("resolved: "+publicId+", "+systemId);
                    // The base resource that includes this current resource
                     resourcePath =
                        resourcePath.resolveSibling(systemId).normalize();
                    try {
                        return new InputSource(Main.getInput(resourcePath));
                    } catch (IOException e) {
                        System.err.println(e.toString());
                        return null;
                    }
                }
            });

        schemas.add(new AbstractMap.SimpleEntry
                    (filename,
                     new RNCValidator
                     (schema, rncFactory, pb.toPropertyMap())));
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
