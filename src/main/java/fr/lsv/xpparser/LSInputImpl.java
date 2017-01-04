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

import java.io.*;
import org.w3c.dom.ls.LSInput;


public class LSInputImpl implements LSInput {

    private String publicId;
    
    private String systemId;
    
    public String getPublicId() {
        return publicId;
    }
    
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
    
    public String getBaseURI() {
        return null;
    }
    
    public InputStream getByteStream() {
        return null;
    }
    
    public boolean getCertifiedText() {
        return false;
    }
    
    public Reader getCharacterStream() {
        return reader;
    }
    
    public String getEncoding() {
        return null;
    }
    
    public String getStringData() {
        return null;
    }
    
    public void setBaseURI(String baseURI) {
    }
    
    public void setByteStream(InputStream byteStream) {
    }
    
    public void setCertifiedText(boolean certifiedText) {
    }
    
    public void setCharacterStream(Reader characterStream) {
        this.reader = characterStream;
    }
    
    public void setEncoding(String encoding) {
    }
    
    public void setStringData(String stringData) {
    }
    
    public String getSystemId() {
        return systemId;
    }
    
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public BufferedInputStream getInputStream() {
        return null;
    }
    
    public void setInputStream(BufferedInputStream inputStream) {
    }
    
    private Reader reader;
    
    public LSInputImpl(String publicId, String sysId, Reader reader) {
        this.publicId = publicId;
        this.systemId = sysId;
        this.reader = reader;
    }
}
