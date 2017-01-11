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

import java.io.IOException;
import javax.xml.transform.Source;
import org.xml.sax.SAXException;

/**
 * Wraps a XML Schema validator inside a Validator.
 */
public class XSDValidator implements Validator {

    private javax.xml.validation.Validator v;
    
    public XSDValidator(javax.xml.validation.Validator v)
        throws SAXException {        

        this.v = v;
    }

    public void validate(Source source) throws IOException, SAXException {
        v.validate(source);
    }

    public void reset() {
        v.reset();
    }
}
