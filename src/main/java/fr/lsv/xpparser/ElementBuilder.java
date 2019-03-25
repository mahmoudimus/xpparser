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

This software or document includes material copied from or derived
from the XPath/XQuery Applets
(https://www.w3.org/2013/01/qt-applets/), in particular on the file
`XQueryXConverterBase-xquery30.java`.  Copyright (C)2013 W3C(R) (MIT,
ERCIM, Keio, Beihang).  See `LICENSE-W3C` for more details.
 */
package fr.lsv.xpparser;

import org.w3c.dom.*;

public class ElementBuilder {

    private int count;

    private int maxdepth;

    private int curdepth;

    private Document doc;

    public ElementBuilder (Element parent) {
        this.doc = parent.getOwnerDocument();
        this.count = 0;
        this.curdepth = 0;
        this.maxdepth = 0;
    }

    public Element createElementNS (String namespaceURI, String qualifiedName) {
        count++;
        return doc.createElementNS(namespaceURI, qualifiedName);
    }

    public Element createElement (String tagName) {
        count++;
        return doc.createElement(tagName);
    }

    public void enter() {
        curdepth++;
        maxdepth = (curdepth > maxdepth)? curdepth: maxdepth;
    }

    public void leave() {
        curdepth--;
    }
    
    public int getCount() {
        return count;
    }

    public int getDepth() {
        return maxdepth;
    }
}
