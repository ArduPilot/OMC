/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import org.asyncfx.beans.property.AsyncProperty;

public interface TrackingAsyncProperty<T> extends AsyncProperty<T>, Mergeable<T> {

    /**
     * Initializes the current and clean value of this property to reflect the current and clean value of the supplied
     * property.
     */
    void init(TrackingAsyncProperty<T> newValue);

    /** Sets the current and clean value at the same time. */
    void init(T newValue);

    /** Initializes the current and clean value. */
    void init(T newValue, T cleanValue);

    /**
     * Sets the clean value to the current value. After calling this method, the property is locally clean, but {@link
     * Mergeable#isDirty()} might still return false in case the value itself is dirty (this can only be the case for
     * object properties and collection properties).
     */
    void clean();

    /**
     * Gets the clean value. If this value is equal to the current value, then the property will be considered locally
     * clean. {@link Mergeable#isDirty()} might still return false in case the value itself is dirty (this can only be
     * the case for object properties and collection properties).
     */
    T getCleanValue();

}
