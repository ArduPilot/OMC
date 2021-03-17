/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.search;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Location;
import gov.nasa.worldwind.geom.Sector;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LocationResultViewModel implements IResultViewModel {

    private final Location location;
    private final Sector sector = null;

    LocationResultViewModel(Location location) {
        Expect.notNull(location, "location");
        this.location = location;
    }

    @Override
    public String getText() {
        return location.toString();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public @Nullable Sector getSector() {
        return sector;
    }

    @Override
    public @Nullable Object getSearchResult() {
        return null;
    }

    @Override
    public boolean isLazyLoaded() {
        return false;
    }

}
