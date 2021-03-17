/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.desktop.main.debug.Debug;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class KMLReader {

    public KMLReader(Double defaultAltitude) {
        this.defaultAltitude = defaultAltitude;
    }

    double defaultAltitude;

    public CFlightplan readKML(CFlightplan plan, InputStream is) throws SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(true);

        SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();

            KMLHandler handler = new KMLHandler(plan, defaultAltitude);

            saxParser.parse(is, handler);
            CFlightplan fp = handler.getFlightplan();
            // fp.setEndMode(FlightplanEndModes.DESCENDING);
            if (fp.sizeOfFlightplanContainer() != 0) {
                CWaypoint wp = (CWaypoint)fp.getLastElement();
                Ensure.notNull(wp, "wp");
                fp.getLandingpoint().setLatLon(wp.getLat(), wp.getLon());
            }

            return fp;
        } catch (ParserConfigurationException e) {
            Debug.getLog().log(Level.SEVERE, "cant read KML file", e);
        }

        return null;
    }

    public CFlightplan readKML(CFlightplan fp, File filename) throws SAXException, IOException {
        try (InputStream is = new FileInputStream(filename)) {
            return readKML(fp, is);
        }
    }

    public interface Tokens {
        public static final String KML = "kml";
        public static final String DOCUMENT = "Document";
        public static final String PLACEMARK = "Placemark";
        public static final String LINESTRING = "LineString";
        public static final String COORDINATES = "coordinates";
    }
}
