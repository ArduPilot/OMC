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

public class NoResultViewModel implements IResultViewModel {

    private final String searchText;

    NoResultViewModel(String searchText) {
        Expect.notNull(searchText, "searchText");
        this.searchText = searchText;
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public Location getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Sector getSector() {
        throw new UnsupportedOperationException();
    }

    public String getSearchText() {
        return searchText;
    }

    @Override
    public Object getSearchResult() {
        return null;
    }

    @Override
    public boolean isLazyLoaded() {
        return false;
    }
}
