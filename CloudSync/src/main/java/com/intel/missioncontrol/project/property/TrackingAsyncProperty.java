/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.AsyncProperty;

public interface TrackingAsyncProperty<T> extends AsyncProperty<T>, Mergeable<T> {

    /** Sets the current value of the property, and resets the tracking marker. */
    void update(T newValue);

}
