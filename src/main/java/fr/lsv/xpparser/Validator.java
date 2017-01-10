package fr.lsv.xpparser;

import java.io.IOException;
import javax.xml.transform.Source;
import org.xml.sax.SAXException;

/**
 * A generic Validator object.
 */
public interface Validator {

    public void validate(Source source) throws IOException, SAXException;

    public void reset();
}
