/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.flightplan.KMLReader.Tokens;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

public class KMLHandler extends DefaultHandler implements Tokens {
    protected Stack<String> currentPath = new Stack<String>();

    private Stack<String> nessesaryPath = new Stack<String>();

    protected CFlightplan plan;

    protected double defaultAltitude;

    protected StringBuffer m_sbuf = new StringBuffer();

    KMLHandler(CFlightplan plan, double defaultAltitude) {
        super();
        this.plan = plan;
        this.defaultAltitude = defaultAltitude;
        nessesaryPath.add(KML);
        nessesaryPath.add(DOCUMENT);
        nessesaryPath.add(PLACEMARK);
        nessesaryPath.add(LINESTRING);
        nessesaryPath.add(COORDINATES);
    }

    public void startDocument() {}

    public void endDocument() throws SAXException {}

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        // first clear the character buffer
        m_sbuf.delete(0, m_sbuf.length());

        currentPath.add(qName);
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (currentPath.equals(nessesaryPath)) {
            String value = m_sbuf.toString();
            String[] coordinate = value.split(" ");
            for (int i = 0; i != coordinate.length; i++) {
                String[] elem = coordinate[i].split(",");
                Double lon = Double.parseDouble(elem[0]);
                Double lat = Double.parseDouble(elem[1]);
                Double alt = Double.parseDouble(elem[2]);
                if (alt == 0.) {
                    alt = defaultAltitude;
                }

                CWaypoint wp =
                    FlightplanFactory.getFactory()
                        .newCWaypoint(lon, lat, (int)Math.round(alt * 100), CWaypoint.DEFAULT_ASSERT_ALT, 0, "", plan);
                try {
                    plan.addToFlightplanContainer(wp);
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            }
        }

        currentPath.pop();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        m_sbuf.append(ch, start, length);
    }

    public CFlightplan getFlightplan() {
        return plan;
    }

}
