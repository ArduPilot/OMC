/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

public interface Mergeable<T> {

    /**
     * Merges the new value with the current value. If a merge conflict occurs, the specified {@link MergeStrategy}
     * determines how the conflict will be handled.
     */
    void merge(T newValue, MergeStrategy strategy);

    /**
     * Indicates whether any of this object's properties was changed since the last call to {@link
     * Mergeable#merge(Object, MergeStrategy)}.
     */
    boolean isDirty();

}
