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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GoogleGazetteer implements Gazetteer, IReaderFromCache {

    String url;

    private static final String URL_GOOGLE = "https://maps.googleapis.com/maps/api/geocode/xml";
    final String MAVINCI_API_KEY;

    // TODO language e.g. de, en-AU, see https://developers.google.com/maps/faq?hl=de#languagesupport
    // what's the limit for our API-Key? 2500 // Web service APIs: 2500, Web APIs: 25000
    // Region-Spezifisch: &region=es

    // components, die gefiltert werden können, gehören:
    //
    // route: Übereinstimmung mit dem langen oder dem Kurznamen einer Route.
    // locality: Übereinstimmung mit den Typen locality und sublocality.
    // administrative_area: Übereinstimmung mit allen Ebenen von administrative_area.
    // postal_code: Übereinstimmung mit postal_code und postal_code_prefix.
    // country: Übereinstimmung mit einem Ländernamen oder einem Ländercode aus zwei Buchstaben nach ISO 3166-1.

    public GoogleGazetteer(WWLayerSettings mapSettings) {
        this(mapSettings, URL_GOOGLE);
    }

    public GoogleGazetteer(WWLayerSettings mapSettings, String url) {
        MAVINCI_API_KEY = mapSettings.googleAccessTokenProperty().get();
        this.url = url;
    }

    private ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();

    @Override
    public List<PointOfInterest> findPlaces(String placeInfo)
            throws NoItemException, ServiceException {
        try {
            String uri;
            uri = this.url + "?address=" + URLEncoder.encode(placeInfo, "UTF-8") + "&key="
                    + MAVINCI_API_KEY;
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
        result.clear();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            // TODO parse the return data!
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("result");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Node type = null;
                Node name = null;
                Node lat = null;
                Node lon = null;
                for (int temp2 = 0; temp2 < nNode.getChildNodes().getLength(); temp2++) {
                    Node nNode2 = nNode.getChildNodes().item(temp2);

                    if (nNode2.getNodeName().equals("formatted_address")) {
                        name = nNode2;
                    } else if (nNode2.getNodeName().equals("type") && type == null) {
                        type = nNode2;
                    } else if (nNode2.getNodeName().equals("geometry")) {
                        for (int temp3 = 0; temp3 < nNode2.getChildNodes().getLength(); temp3++) {
                            Node nNode3 = nNode2.getChildNodes().item(temp3);
                            if (nNode3.getNodeName().equals("location")) {
                                for (int temp4 = 0;
                                     temp4 < nNode3.getChildNodes().getLength(); temp4++) {
                                    Node nNode4 = nNode3.getChildNodes().item(temp4);
                                    if (nNode4.getNodeName().equals("lat")) {
                                        lat = nNode4;
                                    } else if (nNode4.getNodeName().equals("lng")) {
                                        lon = nNode4;
                                    }
                                }
                            }
                        }
                    }
                }

                if (name == null || lat == null || lon == null) {
                    continue;
                }

                BasicPointOfInterest poi = new BasicPointOfInterest(LatLon
                        .fromDegrees(Double.parseDouble(lat.getTextContent()),
                                Double.parseDouble(lon.getTextContent())));
                poi.setValue(AVKey.DISPLAY_NAME, name.getTextContent());
                poi.setValue(SEARCH_PRIO, 0);
                result.add(poi);
            }

            return true;
        } finally {
            try {
                is.close();
            } catch (Exception e) {

            }
        }
    }
}
