/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.settings.MapRotationStyle;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import java.util.OptionalDouble;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.concurrent.Future;

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

    default double getZoom() {
        return zoomProperty().get();
    }

    default void setZoom(double value) {
        zoomProperty().set(value);
    }

    default Angle getHeading() {
        return headingProperty().get();
    }

    default Position getEyePosition() {
        return eyePositionProperty().get();
    }

    Position getCenterPosition();

    Future<Void> goToPositionAsync(Position center);

    Future<Void> goToSectorAsync(Sector sector, OptionalDouble maxElev);

}
