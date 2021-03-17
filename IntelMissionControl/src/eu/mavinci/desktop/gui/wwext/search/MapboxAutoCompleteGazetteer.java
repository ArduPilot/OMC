/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

import static eu.mavinci.desktop.gui.wwext.search.CombinedGazetteer.SEARCH_PRIO;
import static eu.mavinci.desktop.gui.wwext.search.SearchManager.BOUND_BOX;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.intel.missioncontrol.map.worldwind.WWLayerSettings;
import eu.mavinci.desktop.helper.IReaderFromCache;
import eu.mavinci.desktop.helper.UrlCachingHelper;
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

public class MapboxAutoCompleteGazetteer implements Gazetteer, IReaderFromCache {

    String url;

    public static final String URL_MAPBOX = "https://api.mapbox.com/geocoding/v5/mapbox.places/";
    final String ACCESS_TOKEN;
    private ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();

    public MapboxAutoCompleteGazetteer(WWLayerSettings mapSettings) {
        ACCESS_TOKEN = mapSettings.mapboxAccessTokenProperty().get();
        url = URL_MAPBOX;
    }

    @Override
    public List<PointOfInterest> findPlaces(String placeInfo) throws NoItemException, ServiceException {
        try {
            String uri;
            uri =
                    this.url
                            + URLEncoder.encode(placeInfo, "UTF-8")
                            + ".json?access_token="
                            + ACCESS_TOKEN
                            + "&autocomplete=true"
                            + "&limit=10"
                            + "&language="
                            + Locale.getDefault().getLanguage();

            if (UrlCachingHelper.processURLresultsWithoutCache(uri, this)) {
                return result;
            }

            throw new ServiceException("could not fetch results from " + this.url + " for query " + placeInfo);
        } catch (Exception e) {
            throw new ServiceException(
                    "could not fetch results from " + this.url + " for query " + placeInfo + "\n" + e);
        }
    }

    @Override
    public boolean readInputFromCache(InputStream is) {
        result.clear();
        boolean success = true;
        try {
            Gson gson = new Gson();

            JsonReader jsonReader;
            jsonReader = new JsonReader(new InputStreamReader(is, "UTF-8"));

            LocationList features = gson.fromJson(jsonReader, LocationList.class);

            List<LocationData> data = features.getFeatures();
            Iterator<LocationData> iterator = data.iterator();
            LocationData temp;
            while (iterator.hasNext()) {
                temp = iterator.next();
                if (temp == null) {
                    continue;
                }

                if (temp.getPlace_name() == null || temp.getPlace_name().trim().isEmpty()) {
                    continue;
                }

                String name = temp.getPlace_name();
                double tempLat;
                double tempLong;

                if (temp.getGeometry().getCoordinates().get(1) == null
                        || temp.getGeometry().getCoordinates().get(1).trim().isEmpty()) {
                    continue;
                } else {
                    tempLat = Double.parseDouble(temp.getGeometry().getCoordinates().get(1));
                }

                if (temp.getGeometry().getCoordinates().get(0) == null
                        || temp.getGeometry().getCoordinates().get(0).trim().isEmpty()) {
                    continue;
                } else {
                    tempLong = Double.parseDouble(temp.getGeometry().getCoordinates().get(0));
                }

                BasicPointOfInterest poi = new BasicPointOfInterest(LatLon.fromDegrees(tempLat, tempLong));

                poi.setValue(AVKey.DISPLAY_NAME, name);

                poi.setValue(SEARCH_PRIO, temp.getRelevance());

                if (temp.getBbox() != null) {
                    Sector sector =
                            Sector.fromDegrees(
                                    Double.parseDouble(temp.getBbox().get(1)),
                                    Double.parseDouble(temp.getBbox().get(3)),
                                    Double.parseDouble(temp.getBbox().get(0)),
                                    Double.parseDouble(temp.getBbox().get(2)));

                    poi.setValue(BOUND_BOX, sector);
                }

                result.add(poi);
            }

        } catch (UnsupportedEncodingException e) {
            Debug.getLog().log(Debug.WARNING, "Could not read the input stream while initializing the jsonReader");
            success = false;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                Debug.getLog().log(Debug.WARNING, "Could not close the input stream");
            }
        }

        return success;
    }
}
