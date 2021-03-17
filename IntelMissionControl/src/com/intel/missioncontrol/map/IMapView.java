/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.google.common.util.concurrent.ListenableFuture;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncObjectProperty;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.settings.MapRotationStyle;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import java.util.OptionalDouble;

public interface IMapView {

    AsyncBooleanProperty flatEarthProperty();

    AsyncObjectProperty<ViewMode> viewModeProperty();

    AsyncObjectProperty<MapRotationStyle> mapRotationStyleProperty();

    AsyncDoubleProperty zoomProperty();

    ReadOnlyAsyncObjectProperty<Position> eyePositionProperty();

    ReadOnlyAsyncObjectProperty<Angle> headingProperty();

    default boolean isFlatEarth() {
        return flatEarthProperty().get();
    }

    default void setFlatEarth(boolean value) {
        flatEarthProperty().set(value);
    }

    default ListenableFuture<Void> setFlatEarthAsync(boolean value) {
        return flatEarthProperty().setAsync(value);
    }

    default ViewMode getViewMode() {
        return viewModeProperty().get();
    }

    default void setViewMode(ViewMode value) {
        viewModeProperty().set(value);
    }

    default MapRotationStyle getMapRotationStyle() {
        return mapRotationStyleProperty().get();
    }

    default void setMapRotationStyle(MapRotationStyle mapRotationStyle) {
        mapRotationStyleProperty().set(mapRotationStyle);
    }

    default ListenableFuture<Void> setViewModeAsync(ViewMode value) {
        return viewModeProperty().setAsync(value);
    }

    default double getZoom() {
        return zoomProperty().get();
    }

    default void setZoom(double value) {
        zoomProperty().set(value);
    }

    default ListenableFuture<Void> setZoomAsync(double value) {
        return zoomProperty().setAsync(value);
    }

    default Angle getHeading() {
        return headingProperty().get();
    }

    default Position getEyePosition() {
        return eyePositionProperty().get();
    }

    Position getCenterPosition();

    FluentFuture<Void> goToPositionAsync(Position center);

    FluentFuture<Void> goToSectorAsync(Sector sector, OptionalDouble maxElev);

}
