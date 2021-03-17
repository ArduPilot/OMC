/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncObjectProperty;
import com.intel.missioncontrol.mission.WayPoint;

public interface ISelectionManager {

    void setSelection(Object newSelection);

    Object getSelection();

    ReadOnlyAsyncObjectProperty<Object> currentSelectionProperty();

    // highlighting is for bulk operations that originate from, e.g., the edit waypoints dialog
    AsyncListProperty<WayPoint> getHighlighted();

}
