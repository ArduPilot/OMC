/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicArea;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AreaFilter {

    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty enabled = new SimpleBooleanProperty();
    private final BooleanProperty deleteDisabledProperty = new SimpleBooleanProperty();

    private final MapLayerPicArea legacyArea;
    private final Matching parent;

    AreaFilter(Matching parent, MapLayerPicArea legacyArea) {
        this.parent = parent;
        this.legacyArea = legacyArea;
        enabled.setValue(legacyArea.isVisible());
        enabled.addListener((observable, oldValue, newValue) -> legacyArea.setVisible(newValue));
        deleteDisabledProperty.bindBidirectional(legacyArea.getDeleteDisabled());
        name.set(legacyArea.getName());
        name.addListener((observable, oldValue, newValue) -> legacyArea.setName(newValue));
    }

    public void delete() {
        AMapLayerMatching legacyMatching = parent.getLegacyMatching();
        if (legacyMatching instanceof MapLayerMatching) {
            MapLayerMatching mapLayerMatching = (MapLayerMatching)legacyMatching;
            mapLayerMatching.getPicAreasLayer().removeMapLayer(legacyArea);
        }
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public BooleanProperty deleteDisabledPropery(){
        return deleteDisabledProperty;
    }

    public StringProperty nameProperty() {
        return name;
    }

}
