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

import java.io.StringWriter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

public class ErrorHandlerImpl implements ErrorHandler {
    private com.thaiopensource.xml.sax.ErrorHandlerImpl eh;
    private StringWriter diagnostic;
    private boolean fail;

    public ErrorHandlerImpl() {
        this.diagnostic = new StringWriter();
        this.eh =
            new com.thaiopensource.xml.sax.ErrorHandlerImpl(diagnostic);
        this.fail = false;
    }
    
    public void reset() {
        diagnostic = new StringWriter();
        eh = new com.thaiopensource.xml.sax.ErrorHandlerImpl(diagnostic);
        fail = false;
    }
    
    public boolean hasFailed() {
        return fail;
    }
    
    public String getDiagnostic() {
        return diagnostic.toString();
    }
        
    public void warning(SAXParseException exception)
        throws SAXException {
        eh.warning(exception);
    }
        
    public void error(SAXParseException exception)
        throws SAXException {
        // parse error message to get rid of bug with namespace decls
        String msg = exception.getMessage();        
        if (msg.indexOf("found attribute \"xmlns:") < 0) {
            fail = true;
            eh.error(exception);
        }
    }

    public void fatalError(SAXParseException exception)
        throws SAXException {
        fail = true;
        eh.fatalError(exception);
    }
}
