/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.search;

import com.intel.missioncontrol.measure.Location;
import de.saxsys.mvvmfx.ViewModel;
import gov.nasa.worldwind.geom.Sector;
import org.checkerframework.checker.nullness.qual.Nullable;

interface IResultViewModel extends ViewModel {

    String getText();

    Location getLocation();

    @Nullable
    Sector getSector();

    Object getSearchResult();

    boolean isLazyLoaded();
}
