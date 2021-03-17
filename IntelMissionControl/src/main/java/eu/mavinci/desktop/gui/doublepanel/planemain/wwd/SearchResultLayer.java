/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.map.ISelectionManager;
import eu.mavinci.desktop.gui.wwext.IconLayerCentered;
import eu.mavinci.desktop.gui.wwext.IconRendererCentered;
import eu.mavinci.desktop.gui.wwext.UserFacingIconWithUserData;
import eu.mavinci.desktop.gui.wwext.search.ISearchManagerListener;
import eu.mavinci.desktop.gui.wwext.search.SearchManager;
import eu.mavinci.desktop.gui.wwext.search.SearchResult;
import gov.nasa.worldwind.avlist.AVKey;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.concurrent.Dispatcher;

public class SearchResultLayer extends IconLayerCentered implements ISearchManagerListener {

    private final SearchManager searchManager;
    private SearchResult selectedResult;
    private final Dispatcher dispatcher;

    private final AsyncIntegerProperty resultCount = new SimpleAsyncIntegerProperty(this);

    public SearchResultLayer(SearchManager searchManager, Dispatcher dispatcher, ISelectionManager selectionManager) {
        this.searchManager = searchManager;
        searchManager.addListener(this);
        this.dispatcher = dispatcher;
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
                this.dispatcher);
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

        PropertyHelper.setValueSafe(resultCount, i);
        dispatcher.runLater(() -> firePropertyChange(AVKey.LAYER, null, SearchResultLayer.this));
    }

    public void setSelectedResult(SearchResult selectedResult) {
        this.selectedResult = selectedResult;
        searchResultChanged();
    }

    @Override
    public void removeAllIcons() {
        super.removeAllIcons();
        PropertyHelper.setValueSafe(resultCount, 0);
    }

    public ReadOnlyAsyncIntegerProperty resultCountProperty() {
        return resultCount;
    }
}
