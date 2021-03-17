/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.search;

import com.google.inject.Inject;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Location;
import com.intel.missioncontrol.measure.LocationFormat;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.wwext.search.ISearchManagerListener;
import eu.mavinci.desktop.gui.wwext.search.SearchManager;
import eu.mavinci.desktop.gui.wwext.search.SearchResult;
import gov.nasa.worldwind.geom.Sector;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import org.apache.commons.lang3.StringUtils;

public class SearchViewModel extends ViewModelBase {
    private final int SEARCH_RESULTS_LENGTH = 6;

    private final BooleanProperty isSearching = new SimpleBooleanProperty();
    private final StringProperty searchText = new SimpleStringProperty();
    private final ListProperty<IResultViewModel> searchResults =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    // stores the last 3 search results (and adds them to the top of the list(depending on the search query))
    private final MapProperty<String, Integer> lastSearchResults =
        new SimpleMapProperty<>(
            FXCollections.observableMap(
                new LinkedHashMap<>() {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                        if (lastSearchResults.size() > SEARCH_RESULTS_LENGTH) {
                            lastSearchResults.remove(eldest.getKey());
                            return true;
                        }

                        return false;
                    }
                }));
    private final ObjectProperty<IResultViewModel> selectedSearchResult = new SimpleObjectProperty<>();
    private final ICommand clearCommand;
    private final ICommand goToCommand;
    private final IMapView mapView;
    private final SearchManager searchManager;
    private final GeneralSettings generalSettings;
    private AtomicBoolean searchLater = new AtomicBoolean();
    private boolean suppressSearch = false;
    private boolean getCoordinates = false;

    @Inject
    public SearchViewModel(IMapView mapView, SearchManager searchManager, ISettingsManager settingsManager) {
        this.mapView = mapView;
        this.searchManager = searchManager;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.lastSearchResults.putAll(generalSettings.lastSearchResultsProperty());
        generalSettings.lastSearchResultsProperty().bindContent(this.lastSearchResults);

        searchManager.addListener(searchResultChangedListener);

        clearCommand = new DelegateCommand(() -> searchText.set(""), searchText.isEmpty().not());

        goToCommand =
            new DelegateCommand(
                () -> goToLocation(true), Bindings.createBooleanBinding(this::canGoToLocation, searchResults));

        searchText.addListener((observable, oldValue, newValue) -> searchTextChanged(newValue));
        searchTextChanged(""); // initialize history
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        selectedSearchResult.addListener(
            (observable, oldValue, newValue) ->
                searchManager.setSelectedResult(newValue != null ? (SearchResult)newValue.getSearchResult() : null));
    }

    public ICommand getClearCommand() {
        return clearCommand;
    }

    public ICommand getGoToCommand() {
        return goToCommand;
    }

    public BooleanProperty isSearchingProperty() {
        return isSearching;
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public ReadOnlyListProperty<IResultViewModel> searchResultsProperty() {
        return searchResults;
    }

    public ObjectProperty<IResultViewModel> selectedSearchResultProperty() {
        return selectedSearchResult;
    }

    private ISearchManagerListener searchResultChangedListener = this::searchResultsChanged;

    private void searchTextChanged(String newValue) {
        if (suppressSearch) {
            return;
        }

        if (StringUtils.isBlank(searchText.get())) {
            selectedSearchResult.set(null);
            searchResults.clear();
            searchResults.addAll(lastSearchResults());
            isSearching.set(false);
            getCoordinates = false;
            searchManager.reset();
        } else {
            if (searchManager.isSearching()) {
                searchLater.set(true);
            } else {
                searchManager.setGetCoordinates(getCoordinates);
                searchManager.search(newValue);
                isSearching.set(true);
            }
        }
    }

    private void searchResultsChanged() {
        if (searchManager.isSearching()) {
            return;
        }

        if (searchManager.getSearchKeyword() == null) {
            return;
        }

        LocationFormat locationFormat = new LocationFormat();
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
        final List<PlaceResultViewModel> places = new ArrayList<>();
        for (SearchResult result : searchManager.getSearchResult()) {
            places.add(
                new PlaceResultViewModel(
                    new Location(result.getLatLon()), result.getLocationName(), "", result.getSector(), result));
        }

        List<IResultViewModel> newSearchResults = new ArrayList<>();

        /*try {
            Location location = new LocationFormat().parse(searchText.get());
            newSearchResults.add(new LocationResultViewModel(location));
        } catch (IllegalArgumentException e) {
            // don't process this exception
        }*/

        newSearchResults.addAll(places);

        if (searchLater.compareAndSet(true, false)) {
            searchManager.search(searchText.get());
            isSearching.set(true);
        } else {
            isSearching.set(false);
        }

        if (newSearchResults.isEmpty()) {
            newSearchResults.add(new NoResultViewModel(searchText.get()));
        }

        searchResults.clear();
        // adding appropriate last search results on top of the list
        searchResults.addAll(lastSearchResults());
        searchResults.addAll(newSearchResults);

        // if the search history item was used for the search - automatically go to the remembered index
        if (selectedSearchResult.get() != null && selectedSearchResult.get().isLazyLoaded()) {
            int index = ((SearchHistoryItemViewModel)selectedSearchResult.get()).getSelectedOption();
            // just a sanity check
            if (newSearchResults.size() > index) {
                selectedSearchResult.set(newSearchResults.get(index));
                goToLocation(false);
            }
        }

        if (getCoordinates) {
            getCoordinates = false;
            selectedSearchResult.set(searchResults.get(0));
            goToLocation(true);
        }

        selectedSearchResult.set(null);
    }

    private void goToLocation(boolean saveLocationToHistory) {
        IResultViewModel result = selectedSearchResult.get();
        if (result == null && !searchResults.isEmpty()) {
            result = searchResults.get(0);
            if (!isBuzy()) {
                selectedSearchResult.set(result);
            }
        }

        if (result.isLazyLoaded() && !isBuzy()) {
            // when a user clicks on one of the last searched items
            // - it initiates the search (and completes the text in the field)
            searchManager.reset();
            searchManager.search(result.getText());
            isSearching.set(true);
            return;
        }

        if (canGoToLocation(result)) {

            // do not zoom to any location until the latest search is finished
            if (!isBuzy()) {
                // before original search text is overridden, put it into the history list
                if (saveLocationToHistory) {
                    addLastSearch(result);
                }

                suppressSearch = true;
                searchText.set(result.getText());
                suppressSearch = false;

                Sector sector = result.getSector();
                if (sector != null) {
                    mapView.goToSectorAsync(sector, OptionalDouble.empty());
                } else {
                    mapView.goToPositionAsync(
                        DependencyInjector.getInstance()
                            .getInstanceOf(IElevationModel.class)
                            .getPositionOverGround(result.getLocation().toPosition()));
                }
            }
        } else {
            suppressSearch = true;
            searchText.set(result.getText());
            suppressSearch = false;
        }
    }

    private boolean isBuzy() {
        return isSearching.get() || searchLater.get();
    }

    private void addLastSearch(IResultViewModel result) {
        synchronized (lastSearchResults) {
            lastSearchResults.put(
                searchText.get(),
                searchResults
                    .stream()
                    .filter((vm -> !(vm instanceof SearchHistoryItemViewModel)))
                    .collect(Collectors.toList())
                    .indexOf(result));
        }
    }

    private Collection<? extends IResultViewModel> lastSearchResults() {
        synchronized (lastSearchResults) {
            return lastSearchResults
                .keySet()
                .stream()
                .filter((text -> searchText.get() == null || StringUtils.startsWithIgnoreCase(text, searchText.get())))
                .map(text -> new SearchHistoryItemViewModel(text, lastSearchResults.get(text)))
                .collect(Collectors.toList());
        }
    }

    private boolean canGoToLocation() {
        IResultViewModel result = selectedSearchResult.get();
        if (result == null && !searchResults.isEmpty()) {
            result = searchResults.get(0);
        }

        return canGoToLocation(result);
    }

    private boolean canGoToLocation(IResultViewModel result) {
        return result != null && !(result instanceof NoResultViewModel);
    }

}
