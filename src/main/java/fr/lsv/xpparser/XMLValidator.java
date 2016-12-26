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
        validate (org.w3c.dom.Node node) throws IOException {
    
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
