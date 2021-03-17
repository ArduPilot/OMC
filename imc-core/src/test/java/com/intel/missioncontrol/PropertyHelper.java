/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.project.property.TrackingAsyncDoubleProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncIntegerProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncObjectProperty;
import com.intel.missioncontrol.project.property.TrackingAsyncStringProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;

public class PropertyHelper {

    public static TrackingAsyncDoubleProperty asMutable(ReadOnlyAsyncDoubleProperty property) {
        return (TrackingAsyncDoubleProperty)property;
    }

    public static TrackingAsyncIntegerProperty asMutable(ReadOnlyAsyncIntegerProperty property) {
        return (TrackingAsyncIntegerProperty)property;
    }

    public static <T> TrackingAsyncObjectProperty<T> asMutable(ReadOnlyAsyncObjectProperty<T> property) {
        return (TrackingAsyncObjectProperty<T>)property;
    }

    public static TrackingAsyncStringProperty asMutable(ReadOnlyAsyncStringProperty property) {
        return (TrackingAsyncStringProperty)property;
    }

}
