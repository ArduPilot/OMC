/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.search;

import com.intel.missioncontrol.measure.Location;
import gov.nasa.worldwind.geom.Sector;

public class PlaceResultViewModel implements IResultViewModel {

    private final Location location;
    private final String name;
    private final String detail;
    private final Sector sector;
    private final Object searchResult;

    PlaceResultViewModel(Location location, String name, String detail, Sector sector, Object searchResult) {
        this.location = location;
        this.name = name;
        this.detail = detail;
        this.sector = sector;
        this.searchResult = searchResult;
    }

    @Override
    public String getText() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Sector getSector() {
        return sector;
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public Object getSearchResult() {
        return searchResult;
    }

    @Override
    public boolean isLazyLoaded() {
        return false;
    }
}
