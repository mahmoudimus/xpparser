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

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.prop.rng.RngProperty;
import java.io.IOException;
import java.io.Reader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Wraps a RelaxNG validator inside a Validator.
 */
public class RNCValidator implements Validator {

    private com.thaiopensource.validate.Validator v;
    
    private ErrorHandlerImpl eh;

    private TransformerFactory f;
    
    public RNCValidator(Reader reader,
                        SchemaReader factory,
                        PropertyMap pm)
        throws SAXException {        

        try {
            this.v = factory.createSchema
                (new InputSource(reader), pm)
                .createValidator(pm);
            eh = (ErrorHandlerImpl) pm.get
                (ValidateProperty.ERROR_HANDLER);
        }
        catch (IncorrectSchemaException|IOException e) {
            throw new SAXException(e);
        }

        this.f = TransformerFactory.newInstance();
    }

    public void validate(Source source) throws IOException, SAXException {
        //this.f = TransformerFactory.newInstance();
        eh.reset();
        try {
            f.newTransformer().transform
                (source, new SAXResult(v.getContentHandler()));
            if (eh.hasFailed())
                throw new SAXException(eh.getDiagnostic());
        } catch (TransformerException e) {
            throw new SAXException(e.getMessage());
        }
    }

    public void reset() {
        v.reset();
    }
}
