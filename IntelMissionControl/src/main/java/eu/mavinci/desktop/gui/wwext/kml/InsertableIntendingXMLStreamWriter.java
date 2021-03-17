/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.kml;

import gov.nasa.worldwind.avlist.AVList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class InsertableIntendingXMLStreamWriter implements XMLStreamWriter {

    XMLStreamWriter writer;

    public InsertableIntendingXMLStreamWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }

    private int curDepth = 0;

    boolean structureInsideCurrent = true;

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        intend();
        writer.writeStartElement(localName);
        curDepth++;
        afterStartElement();
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        intend();
        writer.writeStartElement(namespaceURI, localName);
        curDepth++;
        structureInsideCurrent = true;
        afterStartElement();
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        intend();
        writer.writeStartElement(prefix, localName, namespaceURI);
        structureInsideCurrent = true;
        curDepth++;
        afterStartElement();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        curDepth--;
        if (structureInsideCurrent) {
            intend();
        }

        structureInsideCurrent = true;

        writer.writeEndElement();
    }

    void intend() throws XMLStreamException {
        writer.writeCharacters("\r\n");
        for (int i = 0; i != curDepth; i++) {
            writer.writeCharacters("\t");
        }
    }

    private AVList fieldsToDump = null;
    private int fieldsDumpLevel = -1;
    boolean dontWriteName = false;

    public void writeFieldsAtNextLevel(AVList fields) {
        fieldsToDump = fields;
        fieldsDumpLevel = curDepth + 1;
        dontWriteName = true;
    }

    public void writeFieldsAtNextLevelDisable() {
        fieldsDumpLevel = -1;
        fieldsToDump = null;
        dontWriteName = false;
    }

    void afterStartElement() throws XMLStreamException {
        if (fieldsToDump != null && curDepth == fieldsDumpLevel) {
            writeFileds(fieldsToDump);
        }
    }

    protected void writeFileds(AVList fieldsToDump2) throws XMLStreamException {}

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        intend();
        writer.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        intend();
        writer.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        intend();
        writer.writeEmptyElement(localName);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        writer.flush();
    }

    @Override
    public void flush() throws XMLStreamException {
        writer.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writer.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        writer.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writer.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        writer.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        writer.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        writer.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        writer.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        writer.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        structureInsideCurrent = false;
        writer.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        writer.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        writer.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        writer.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        writer.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        writer.writeStartDocument(encoding, version);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        structureInsideCurrent = false;
        escapeString(text);
        // writer.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        structureInsideCurrent = false;
        escapeString(new String(text, start, len));
        // writer.writeCharacters(text,start,len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return writer.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        writer.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        writer.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        writer.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return writer.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return writer.getProperty(name);
    }

    // unicode ranges and valid/invalid characters
    private static final char LOWER_RANGE = 0x20;
    private static final char UPPER_RANGE = 0x7f;
    private static final char[] VALID_CHARS = {0x9, 0xA, 0xD};

    private static final char[] INVALID = {'<', '>', '"', '\'', '&'};
    private static final String[] VALID = {"&lt;", "&gt;", "&quot;", "&apos;", "&amp;"};

    /**
     * Escape a string such that it is safe to use in an XML document.
     *
     * @param str the string to escape
     * @throws XMLStreamException
     */
    protected void escapeString(String str) throws XMLStreamException {
        if (str == null) {
            writer.writeCharacters("null");
            return;
        }

        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);

            if ((c < LOWER_RANGE && c != VALID_CHARS[0] && c != VALID_CHARS[1] && c != VALID_CHARS[2])
                    || (c > UPPER_RANGE)) {
                // character out of range, escape with character value
                writer.writeCharacters("&#");
                writer.writeCharacters(Integer.toString(c));
                writer.writeCharacters(";");
            } else {
                boolean valid = true;
                // check for invalid characters (e.g., "<", "&", etc)
                for (int j = INVALID.length - 1; j >= 0; --j) {
                    if (INVALID[j] == c) {
                        valid = false;
                        writer.writeCharacters(VALID[j]);
                        break;
                    }
                }
                // if character is valid, don't escape
                if (valid) {
                    writer.writeCharacters("" + c);
                }
            }
        }
    }

}
