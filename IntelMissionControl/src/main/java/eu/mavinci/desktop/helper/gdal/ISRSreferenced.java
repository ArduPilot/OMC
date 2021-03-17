/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.gdal;

import eu.mavinci.core.helper.IProperties;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.core.helper.IProperties;

public interface ISRSreferenced {

    public static final String KEY_SRS = Application.KEY + ".SRS";

    public static final String KEY_SRS_NAME = Application.KEY + ".SRS.name";
    public static final String KEY_SRS_WKT = Application.KEY + ".SRS.wkt";
    public static final String KEY_SRS_TABLE = Application.KEY + ".SRS.table";
    public static final String KEY_SRS_IDSOURCE = Application.KEY + ".SRS.idSource";
    public static final String KEY_SRS_ORIGIN = Application.KEY + ".SRS.origin";

    public MSpatialReference getSRS();

    public void setSRS(MSpatialReference srs);

    public boolean isSrsReadOnly();

}
