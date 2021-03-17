/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

import com.google.inject.Inject;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.WWLayerSettings;
import eu.mavinci.coordinatetransform.EcefCoordinate;
import eu.mavinci.coordinatetransform.Ellipsoid;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.exception.NoItemException;
import gov.nasa.worldwind.exception.ServiceException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.poi.Gazetteer;
import gov.nasa.worldwind.poi.PointOfInterest;
import gov.nasa.worldwindx.examples.GoToCoordinatePanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javafx.application.Platform;

public class SearchManager implements ISectorReferenced, IKeepAll {

    private List<SearchResult> searchResult = new ArrayList<>();

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.SearchManager";
    public static final String BOUND_BOX = "boundingBox";

    private final Gazetteer gazetteer;

    private double maxLat;
    private double minLat;
    private double maxLon;
    private double minLon;
    private Sector sector;

    private String searchKeyWord;

    private boolean isSearching;

    public boolean isSearching() {
        return isSearching;
    }

    private boolean getCoordinates = false;

    public void setGetCoordinates(boolean getCoordinates) {
        this.getCoordinates = getCoordinates;
    }

    private final ILanguageHelper languageHelper;
    private final IWWGlobes globes;
    private final ISelectionManager selectionManager;

    @Inject
    public SearchManager(IWWGlobes globes, ILanguageHelper languageHelper, ISelectionManager selectionManager, WWLayerSettings wwLayerSettings) {
        this.globes = globes;
        this.languageHelper = languageHelper;
        this.selectionManager = selectionManager;
        gazetteer = CombinedGazetteer.createAllGazeteer(wwLayerSettings);
        reset();
    }

    public synchronized void search(String input) {
        if (isSearching) {
            return;
        }

        isSearching = true;

        /*
        if (getCoordinates) {
            Application.addToLastSearches(input);
        } */
        // reset();
        searchKeyWord = input.trim();
        fireResultChanged(); // to inform about state change (isSearching)
        // System.out.println("do search " + input);
        // (new Exception()).printStackTrace();

        // try to resolve it offline as LatLon
        // e.g. 156,53 13,51
        // e.g. 156,53 13,51 Name of Somewhere
        // e.g. 49.80428° N 8.682222° E

        try {
            LatLon latLon =
                    GoToCoordinatePanel.computeLatLonFromString(
                            searchKeyWord.replaceAll("°", " "), globes.getDefaultGlobe());
            if (latLon != null) {
                searchResult.clear();
                SearchResult res = new SearchResult(latLon, searchKeyWord);
                searchResult.add(res);
                isSearching = false;

                Dispatcher.postToUI(
                        () -> {
                            fireResultChanged();
                            // controller.setSelectionAsync(res);
                        });
                return;
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.FINE, "could not parse as LatLon, so try online", e);
        }

        Dispatcher.post(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            searchResult.clear();
                            ((CombinedGazetteer) gazetteer).setGetCoordinates(getCoordinates);
                            List<PointOfInterest> results = gazetteer.findPlaces(searchKeyWord);
                            addSearchResult(searchResult, 0, results);

                        } catch (NoItemException e) {
                            Debug.getLog()
                                    .log(
                                            Level.CONFIG,
                                            "could not find and place with name \"" + searchKeyWord + "\" in combined Gazetteer",
                                            e);
                        } catch (ServiceException e) {
                            Debug.getLog().log(Level.WARNING, "could not connect Gazetteer service", e);
                        }

                        calcSector();
                        Platform.runLater(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        isSearching = false;
                                        fireResultChanged();
                                        if (searchResult.size() > 0) {
                                            // controller.setSelectionAsync(searchResult.get(0));
                                            // System.out.println("scroll to last result");
                                            int idx = Math.min(3, SearchManager.this.getSearchResult().size() - 1);
                                            SearchResult lastMinVisResult = SearchManager.this.getSearchResult().get(idx);
                                        }
                                    }
                                });
                    }

                    private void addSearchResult(
                            List<SearchResult> searchResult, int index, List<PointOfInterest> results) {
                        for (int i = 0; i < results.size(); i++) {
                            // System.out.println("res#:"+i);
                            SearchResult res;
                            PointOfInterest poi = results.get(i);
                            if (poi.hasKey(BOUND_BOX)) {
                                res = new SearchResult(poi, (Sector) poi.getValue(BOUND_BOX));
                            } else {
                                res = new SearchResult(poi);
                            }

                            if (!searchResult.isEmpty()) {
                                for (int j = 0; j < searchResult.size(); j++) {
                                    SearchResult poi2 = searchResult.get(j);
                                    if (poi2.getLatLon().equals(res.getLatLon())) {
                                        // System.out.println("remove "+poi2.getLatLon().toString()+"
                                        // "+poi2.getSearch()+" /
                                        // "+res.getLatLon().toString()+" "+res.getSearch());
                                        if (poi2.getSearchPrio() < res.getSearchPrio()) {
                                            res = poi2;
                                        }

                                        searchResult.remove(poi2);
                                        j--;
                                    } else {
                                        EcefCoordinate origECEF =
                                                EcefCoordinate.fromLongLatH(
                                                        poi2.getLongitude(), poi2.getLatitude(), 0, Ellipsoid.wgs84Ellipsoid);
                                        EcefCoordinate targECEF =
                                                EcefCoordinate.fromLongLatH(
                                                        res.getLongitude(), res.getLatitude(), 0, Ellipsoid.wgs84Ellipsoid);
                                        double x_diff = origECEF.x - targECEF.x;
                                        double y_diff = origECEF.y - targECEF.y;
                                        if (Math.abs(x_diff) < 100 && Math.abs(y_diff) < 100) {
                                            // System.out.println("remove "+poi2.getLocationName()+"
                                            // "+poi2.getLatLon().toString()+"
                                            // "+poi2.getSearch()+" / "+res.getLocationName()+"
                                            // "+res.getLatLon().toString() +" "+res.getSearch()+"
                                            // // "+x_diff+" / "+y_diff);
                                            if (poi2.getSearchPrio() < res.getSearchPrio()) {
                                                res = poi2;
                                            }

                                            searchResult.remove(poi2);
                                            j--;
                                        }
                                    }
                                }
                            }

                            searchResult.add(index, res);
                        }

                        Collections.sort(
                                searchResult,
                                new Comparator<SearchResult>() {

                                    @Override
                                    public int compare(SearchResult o1, SearchResult o2) {
                                        if (o2.getSearchPrio().equals(o1.getSearchPrio())) {
                                            return (o2.getLocationName() + o2.getLatitude())
                                                    .compareTo(o1.getLocationName() + o1.getLatitude());
                                        } else {
                                            return (o2.getSearchPrio()).compareTo(o1.getSearchPrio());
                                        }
                                    }

                                });
                    }
                });
    }

    public void setSelectedResult(SearchResult selectedResult) {
        selectionManager.setSelection(selectedResult);
    }

    private void calcSector() {
        if (searchResult != null) {
            minLon = Double.POSITIVE_INFINITY;
            minLat = Double.POSITIVE_INFINITY;
            maxLon = Double.NEGATIVE_INFINITY;
            maxLat = Double.NEGATIVE_INFINITY;
            for (SearchResult res : searchResult) {
                maxLat = Math.max(maxLat, res.getLatitude());
                maxLon = Math.max(maxLon, res.getLongitude());

                minLat = Math.min(minLat, res.getLatitude());
                minLon = Math.min(minLon, res.getLongitude());
            }
        }

        sector =
                new Sector(
                        Angle.fromDegreesLatitude(minLat),
                        Angle.fromDegreesLatitude(maxLat),
                        Angle.fromDegreesLongitude(minLon),
                        Angle.fromDegreesLongitude(maxLon));
    }

    /**
     * @return the searchResult
     */
    public List<SearchResult> getSearchResult() {
        return searchResult;
    }

    /**
     * @param searchResult the searchResult to set
     */
    public void setSearchResult(List<SearchResult> searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    public String toString() {
        if (searchKeyWord == null) {
            return languageHelper.getString(KEY + ".NullSearchResultLabel", searchKeyWord);
        } else {
            if (isSearching) {
                return languageHelper.getString(KEY + ".IsSearchingLabel", searchKeyWord);
            } else {
                return languageHelper.getString(KEY + ".SearchResultLabel", searchKeyWord);
            }
        }
    }

    public String getSearchKeyword() {
        return searchKeyWord;
    }

    @Override
    public OptionalDouble getMaxElev() {
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getMinElev() {
        return OptionalDouble.empty();
    }

    @Override
    public Sector getSector() {
        return sector;
    }

    WeakListenerList<ISearchManagerListener> listeners =
            new WeakListenerList<ISearchManagerListener>("SearchManagerListeners");

    public void addListener(ISearchManagerListener listener) {
        listeners.add(listener);
    }

    public void addListener(ISearchManagerListener listener, Executor executor) {
        listeners.add(listener, ISearchManagerListener.class, executor);
    }

    public void removeListener(ISearchManagerListener listener) {
        listeners.remove(listener);
    }

    private void fireResultChanged() {
        for (ISearchManagerListener listener : listeners) {
            listener.searchResultChanged();
        }
    }

    public void removeResult(SearchResult res) {
        searchResult.remove(res);
        calcSector();
        fireResultChanged();
    }

    public void removeResults(List<SearchResult> results) {
        searchResult.removeAll(results);
        calcSector();
        fireResultChanged();
    }

    /**
     * Clear all search Results
     */
    public void reset() {
        isSearching = false;
        searchKeyWord = null;
        this.searchResult.clear();
        calcSector();
        fireResultChanged();
    }

}
