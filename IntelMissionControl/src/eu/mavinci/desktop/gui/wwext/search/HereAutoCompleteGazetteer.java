/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

import static eu.mavinci.desktop.gui.wwext.search.CombinedGazetteer.SEARCH_PRIO;

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

public class HereAutoCompleteGazetteer implements Gazetteer, IReaderFromCache {

    String url;

    // TODO: The url, app_id and app_code are all related to the 90 day trial account. Need to be changed later.
    // Also it does not matter what you request for xml or json, it returns only json.
    public static final String URL_HERE =
            "http://autocomplete.geocoder.cit.api.here.com/6.2/suggest.xml?";
    final String HERE_APP_ID;
    final String HERE_APP_CODE;
    private ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();

    public HereAutoCompleteGazetteer(WWLayerSettings mapSettings) {
        this(mapSettings, URL_HERE);
    }

    public HereAutoCompleteGazetteer(WWLayerSettings mapSettings, String url) {
        HERE_APP_CODE = mapSettings.hereAppCodeProperty().get();
        HERE_APP_ID = mapSettings.hereAppIdProperty().get();
        this.url = url;
    }

    // TODO language e.g. de, en-AU. Currently using langage=en
    @Override
    public List<PointOfInterest> findPlaces(String placeInfo)
            throws NoItemException, ServiceException {
        try {
            String uri;
            uri = this.url + "language=en" + "&query=" + URLEncoder.encode(placeInfo, "UTF-8")
                    + "&maxresults=5" + "&app_id=" + HERE_APP_ID + "&app_code=" + HERE_APP_CODE;
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
    public boolean readInputFromCache(InputStream is) {
        result.clear();
        try {
            Gson gson = new Gson();

            JsonReader jsonReader;
            jsonReader = new JsonReader(new InputStreamReader(is, "UTF-8"));

            Suggestions suggestions = gson.fromJson(jsonReader, Suggestions.class);

            String name = null;
            // Giving fake latitude and longitude values as autocomplete does not return these values.
            double lati = 37.4132;
            double longi = -121.9988;
            AutoCompleteData data;
            Iterator<AutoCompleteData> iterator = suggestions.getSuggestions().iterator();
            while (iterator.hasNext()) {
                data = iterator.next();
                if (data == null) {
                    continue;
                }

                String[] temp = data.getLabel().split(",");
                String resultStr = "";
                for (int index = temp.length - 1; index > 0; index--) {
                    resultStr = resultStr + temp[index] + ", ";
                }

                resultStr = resultStr + temp[0];

                BasicPointOfInterest poi =
                        new BasicPointOfInterest(LatLon.fromDegrees(lati, longi));
                lati = lati + 0.001;
                longi = longi + 0.001;

                poi.setValue(AVKey.DISPLAY_NAME, resultStr);

                poi.setValue(SEARCH_PRIO, 0);

                result.add(poi);
            }

        } catch (UnsupportedEncodingException e) {
            Debug.getLog().log(Debug.WARNING,
                    "Could not read the input stream while initializing the jsonReader");
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                Debug.getLog().log(Debug.WARNING, "Could not close the input stream");
            }
        }

        return true;
    }
}


class Suggestions {

    private List<AutoCompleteData> suggestions;

    List<AutoCompleteData> getSuggestions() {
        return suggestions;
    }

    void setSuggestions(List<AutoCompleteData> places) {
        suggestions = places;
    }

}


class AutoCompleteData {

    private String label;

    public AutoCompleteData() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String name) {
        label = name;
    }

    @Override
    public String toString() {
        return label;
    }
}
