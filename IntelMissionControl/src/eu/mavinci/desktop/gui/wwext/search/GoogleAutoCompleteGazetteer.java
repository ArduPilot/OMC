/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

import static eu.mavinci.desktop.gui.wwext.search.CombinedGazetteer.SEARCH_PRIO;

import com.intel.missioncontrol.map.worldwind.WWLayerSettings;
import eu.mavinci.desktop.helper.IReaderFromCache;
import eu.mavinci.desktop.helper.UrlCachingHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.NoItemException;
import gov.nasa.worldwind.exception.ServiceException;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.poi.BasicPointOfInterest;
import gov.nasa.worldwind.poi.Gazetteer;
import gov.nasa.worldwind.poi.PointOfInterest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GoogleAutoCompleteGazetteer implements Gazetteer, IReaderFromCache {

    String url;

    private static final String URL_GOOGLE =
            "https://maps.googleapis.com/maps/api/place/autocomplete/xml";
    final String MAVINCI_API_KEY;

    // TODO language support e.g. de, en-AU, see https://developers.google.com/maps/faq?hl=de#languagesupport


    public GoogleAutoCompleteGazetteer(WWLayerSettings mapSettings) {
        this(mapSettings, URL_GOOGLE);
    }

    public GoogleAutoCompleteGazetteer(WWLayerSettings mapSettings, String url) {
        MAVINCI_API_KEY = mapSettings.googleAccessTokenProperty().get();
        this.url = url;
        Debug.getLog()
                .log(Level.INFO, "------- Google autocomplete geocoder is called. ----------");

    }

    private ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();

    @Override
    public List<PointOfInterest> findPlaces(String placeInfo)
            throws NoItemException, ServiceException {
        try {
            String uri;
            uri = this.url + "?input=" + URLEncoder.encode(placeInfo, "UTF-8") + "&types=geocode"
                    + "&key=" + MAVINCI_API_KEY;
            if (UrlCachingHelper.processURLresultsWithoutCache(uri, this)) {
                return result;
            }

            throw new ServiceException(
                    "could not fetch results from " + this.url + " for query " + placeInfo);
        } catch (Exception e) {
            throw new ServiceException(
                    "could not fetch results from " + this.url + " for query " + placeInfo + "\n" + e);
        }
    }

    @Override
    public boolean readInputFromCache(InputStream is)
            throws ParserConfigurationException, SAXException, IOException {
        ArrayList<PointOfInterest> result_NonNode = new ArrayList<PointOfInterest>();
        result.clear();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            // parse the return data
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("prediction");

            Node type = null;
            Node name = null;

            // Adding filler values as autocomplete does not return lat, long.
            double lati = 37.4132;
            double longi = -121.9988;
            for (int i = 0; i < nList.getLength(); i++) {
                Node resultNode = nList.item(i);
                if (resultNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element resultElement = (Element) resultNode;

                    NodeList typeList = resultElement.getElementsByTagName("description");
                    if (typeList.getLength() > 0) {
                        name = typeList.item(0);
                    }

                    if (name == null) {
                        continue;
                    }

                    BasicPointOfInterest poi =
                            new BasicPointOfInterest(LatLon.fromDegrees(lati, longi));
                    lati = lati + 0.001;
                    longi = longi + 0.001;
                    poi.setValue(AVKey.DISPLAY_NAME, name.getTextContent().trim());

                    poi.setValue(SEARCH_PRIO, 0);

                    result.add(poi);
                }
            }

            return true;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                Debug.getLog().log(Debug.WARNING, "Could not close the input stream");
            }
        }
    }
}
