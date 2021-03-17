/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.gdal;

import java.util.TreeSet;
import javafx.collections.ObservableMap;

public interface ISrsManager {

    int ID_PRIVATE_MIN = 32768;
    int ID_PRIVATE_MAX = 6000000;

    void loadFromApp();

    void storeToApp();

    int getMaxId();

    MSpatialReference getSrsByIdOrDefault(String srsId);

    MSpatialReference getSrsByIdOrNull(String srsId);

    MSpatialReference getDefault();

    ObservableMap<String, MSpatialReference> getReferences();

    ObservableMap<String, TreeSet<MSpatialReference>> getReferencesSorted();

    void delete(String id);
}
