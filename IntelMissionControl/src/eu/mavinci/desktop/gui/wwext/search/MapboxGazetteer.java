/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.intel.missioncontrol.map.worldwind.WWLayerSettings;
import eu.mavinci.desktop.helper.IReaderFromCache;
import eu.mavinci.desktop.helper.UrlCachingHelper;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.NoItemException;
import gov.nasa.worldwind.exception.ServiceException;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.poi.BasicPointOfInterest;
import gov.nasa.worldwind.poi.Gazetteer;
import gov.nasa.worldwind.poi.PointOfInterest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static eu.mavinci.desktop.gui.wwext.search.CombinedGazetteer.SEARCH_PRIO;
import static eu.mavinci.desktop.gui.wwext.search.SearchManager.BOUND_BOX;

public class MapboxGazetteer implements Gazetteer, IReaderFromCache {

    String url;

    public static final String URL_MAPBOX = MapboxAutoCompleteGazetteer.URL_MAPBOX;
    final String ACCESS_TOKEN;
    private ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();
    private String tempString;

    public MapboxGazetteer(WWLayerSettings mapSettings) {
        ACCESS_TOKEN = mapSettings.mapboxAccessTokenProperty().get();
        url = URL_MAPBOX;
    }

    public MapboxGazetteer(WWLayerSettings mapSettings, String url) {
        ACCESS_TOKEN = mapSettings.mapboxAccessTokenProperty().get();
        this.url = url;
    }

    @Override public List<PointOfInterest> findPlaces(String placeInfo)
        throws NoItemException, ServiceException {
        try {
            String uri;
            uri = this.url + URLEncoder.encode(placeInfo, "UTF-8") + ".json?access_token="
                + ACCESS_TOKEN + "&limit=10" + "&language=" + Locale.getDefault().getLanguage();
            tempString = placeInfo;
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

    @Override public boolean readInputFromCache(InputStream is) {
        result.clear();
        try {
            Gson gson = new Gson();

            JsonReader jsonReader;
            jsonReader = new JsonReader(new InputStreamReader(is, "UTF-8"));

            LocationList features = gson.fromJson(jsonReader, LocationList.class);

            String name = null;
            List<LocationData> data = features.getFeatures();
            Iterator<LocationData> iterator = data.iterator();
            LocationData temp;
            while (iterator.hasNext()) {
                temp = iterator.next();
                if (temp == null) {
                    continue;
                }

                if (temp.getPlace_name() == null || temp.getPlace_name().trim().isEmpty()
                    || temp.getGeometry().getCoordinates().get(0) == null
                    || temp.getGeometry().getCoordinates().get(1) == null) {
                    continue;
                }

                name = temp.getPlace_name();

                BasicPointOfInterest poi = new BasicPointOfInterest(LatLon
                    .fromDegrees(Double.parseDouble(temp.getGeometry().getCoordinates().get(1)),
                        Double.parseDouble(temp.getGeometry().getCoordinates().get(0))));
                poi.setValue(AVKey.DISPLAY_NAME, name);

                poi.setValue(SEARCH_PRIO, 0);

                if (temp.getBbox() != null) {
                    Sector sector = Sector.fromDegrees(Double.parseDouble(temp.getBbox().get(1)),
                        Double.parseDouble(temp.getBbox().get(3)),
                        Double.parseDouble(temp.getBbox().get(0)),
                        Double.parseDouble(temp.getBbox().get(2)));

                    poi.setValue(BOUND_BOX, sector);
                }

                result.add(poi);
            }

        } catch (UnsupportedEncodingException e) {
            Debug.getLog().log(Debug.WARNING,
                "Could not read the input stream while initializing the jsonReader");
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                Debug.getLog().log(Debug.WARNING, "Could not close the input stream");
                e.printStackTrace();
            }
        }

        return true;
    }
}


class LocationList {

    private List<LocationData> features;

    List<LocationData> getFeatures() {
        return features;
    }

    void setFeatures(List<LocationData> places) {
        features = places;
    }
}


class Geometry {
    private String type;
    private List<String> coordinates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<String> coordinates) {
        this.coordinates = coordinates;
    }

    @Override public String toString() {
        return "coordinate type: " + type + ", Lat: " + coordinates.get(0) + ", Long: "
            + coordinates.get(1);
    }
}


class LocationData {

    public LocationData() {
    }

    private String type;
    private List<String> place_type;
    private String id;
    private String place_name;
    private String text;
    private Geometry geometry;
    private List<String> bbox;
    private Double relevance;

    public List<String> getBbox() {
        return bbox;
    }

    public void setBbox(List<String> bbox) {
        this.bbox = bbox;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getPlace_type() {
        return place_type;
    }

    public void setPlace_type(List<String> place_type) {
        this.place_type = place_type;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public Double getRelevance() {
        return relevance;
    }

    public void setRelevance(Double relevance) {
        this.relevance = relevance;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override public String toString() {
        return "Result text: " + text + ", name: " + place_name + "type of result: " + place_type
            + ", " + geometry.toString();
    }

}
