/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

import eu.mavinci.desktop.helper.IReaderFromCache;
import eu.mavinci.desktop.helper.UrlCachingHelper;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.NoItemException;
import gov.nasa.worldwind.exception.ServiceException;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.poi.BasicPointOfInterest;
import gov.nasa.worldwind.poi.Gazetteer;
import gov.nasa.worldwind.poi.PointOfInterest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static eu.mavinci.desktop.gui.wwext.search.CombinedGazetteer.SEARCH_PRIO;

public class HereGazetteer implements Gazetteer, IReaderFromCache {

    private String url;

    public static final String URL_HERE = "https://geocoder.cit.api.here.com/6.2/geocode.xml?";
    public static final String HERE_APP_ID = HereAutoCompleteGazetteer.HERE_APP_ID;
    public static final String HERE_APP_CODE = HereAutoCompleteGazetteer.HERE_APP_CODE;

    public HereGazetteer() {
        url = URL_HERE;
    }

    public HereGazetteer(String url) {
        this.url = url;
    }

    ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();

    @Override public List<PointOfInterest> findPlaces(String placeInfo)
        throws NoItemException, ServiceException {
        try {
            String uri;
            uri = this.url + "app_id=" + HERE_APP_ID + "&app_code=" + HERE_APP_CODE + "&searchtext="
                + URLEncoder.encode(placeInfo, "UTF-8");
            if (UrlCachingHelper.processURLresultsWithoutCache(uri, this)) {
                return result;
            }

            throw new ServiceException(
                "Could not fetch results from " + this.url + " for query " + placeInfo);
        } catch (Exception e) {
            throw new ServiceException(
                "could not fetch results from " + this.url + " for query " + placeInfo + "\n" + e);
        }
    }

    @Override public boolean readInputFromCache(InputStream is)
        throws ParserConfigurationException, SAXException, IOException {
        ArrayList<PointOfInterest> result_NonNode = new ArrayList<PointOfInterest>();
        result.clear();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            // parse the return data
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("Result");

            for (int i = 0; i < nList.getLength(); i++) {
                Node resultNode = nList.item(i);
                if (resultNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element resultElement = (Element) resultNode;

                    Node type = null;
                    Node name = null;
                    Node lat = null;
                    Node lon = null;
                    // TODO: Verify if lat and lon values from NavigationPosition is fine or should it be taken from
                    // Display Position??
                    NodeList navPosList = resultElement.getElementsByTagName("NavigationPosition");
                    if (navPosList.getLength() > 0) {
                        Node navPos = navPosList.item(0);
                        NodeList navNodes = navPos.getChildNodes();
                        for (int j = 0; j < navNodes.getLength(); j++) {
                            Node navChild = navNodes.item(j);
                            if (navChild.getNodeName().equals("Latitude")) {
                                lat = navChild;
                            } else if (navChild.getNodeName().equals("Longitude")) {
                                lon = navChild;
                            }
                        }
                    }

                    NodeList addList = resultElement.getElementsByTagName("Address");
                    if (addList.getLength() > 0) {
                        Node addNode = addList.item(0);
                        NodeList addNodes = addNode.getChildNodes();
                        for (int j = 0; j < addNodes.getLength(); j++) {
                            Node addChild = addNodes.item(j);
                            if (addChild.getNodeName().equals("Label")) {
                                name = addChild;
                            }
                        }
                    }

                    NodeList typeList = resultElement.getElementsByTagName("LocationType");
                    if (typeList.getLength() > 0) {
                        type = typeList.item(0);
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
