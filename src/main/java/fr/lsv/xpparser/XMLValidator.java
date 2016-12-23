package fr.lsv.xpparser;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.LinkedList;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 * Utility class for validating against multiple XML Schemas.
 */
public class XMLValidator {

    private List<Validator> schemas;

    public XMLValidator(Iterable<Reader> schemas) 
        throws SAXException {
        
        this.schemas = new LinkedList<Validator>();
        SchemaFactory sf = 
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        for (Reader file : schemas)
            this.schemas.add
                (sf.newSchema(new StreamSource(file)).newValidator());
    }

    public Iterable<Boolean> validate (org.w3c.dom.Node node)
        throws IOException {
    
        DOMSource source = new DOMSource(node);
        LinkedList<Boolean> ret = new LinkedList<Boolean>();
        for (Validator v : schemas)
            try {
                v.validate(source);
                v.reset();
                ret.add(new Boolean(true));
            } catch (SAXException e) {
                v.reset();
                ret.add(new Boolean(false));
            }
        return ret;
    }
}
