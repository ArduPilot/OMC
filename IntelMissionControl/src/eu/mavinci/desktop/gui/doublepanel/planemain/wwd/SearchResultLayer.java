/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.property.AsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncIntegerProperty;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.ISelectionManager;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.IconRendererCentered;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import eu.mavinci.desktop.gui.wwext.search.ISearchManagerListener;
import eu.mavinci.desktop.gui.wwext.search.SearchManager;
import eu.mavinci.desktop.gui.wwext.search.SearchResult;
import gov.nasa.worldwind.avlist.AVKey;

public class SearchResultLayer extends IconLayerCentered implements ISearchManagerListener {

    private final SearchManager searchManager;
    private SearchResult selectedResult;
    private final SynchronizationRoot syncRoot;

    private final AsyncIntegerProperty resultCount = new SimpleAsyncIntegerProperty(this);

    @Inject
    public SearchResultLayer(
            SearchManager searchManager, SynchronizationRoot syncRoot, ISelectionManager selectionManager) {
        this.searchManager = searchManager;
        searchManager.addListener(this);
        this.syncRoot = syncRoot;
        setName("SearchResults");
        searchResultChanged();
        setAlwaysUseAbsoluteElevation(false);
        setRenderAlwaysOverGround(true);
        setReferencePoint(IconRendererCentered.ReferencePoint.BOTTOM_CENTER);
        setPickEnabled(true);
        selectionManager
            .currentSelectionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue instanceof SearchResult) {
                        setSelectedResult((SearchResult)newValue);
                    } else {
                        setSelectedResult(null);
                    }
                },
                syncRoot);
    }

    @Override
    public void searchResultChanged() {
        removeAllIcons();
        int i = 0;
        for (SearchResult result : searchManager.getSearchResult()) {
            UserFacingIconWithUserData icon =
                UserFacingIconWithUserData.getOnTerrainRelativeHeightInLayer(
                    "com/intel/missioncontrol/gfx/map_location.svg", result.getLatLon(), result);
            icon.setSelectable(true);
            icon.setHighlightScale(1.5);
            icon.setToolTipText(result.getLocationName());
            icon.setToolTipTextColor(java.awt.Color.YELLOW);
            if (selectedResult == result) {
                icon.setSize(UserFacingIconWithUserData.d48);
            }

            addIcon(icon);
            i++;
        }

        resultCount.setAsync(i);

        syncRoot.dispatch(() -> firePropertyChange(AVKey.LAYER, null, SearchResultLayer.this));
    }

    public void setSelectedResult(SearchResult selectedResult) {
        this.selectedResult = selectedResult;
        searchResultChanged();
    }

    @Override
    public void removeAllIcons() {
        super.removeAllIcons();
        resultCount.setAsync(0);
    }

    public ReadOnlyAsyncIntegerProperty resultCountProperty() {
        return resultCount;
    }
}
