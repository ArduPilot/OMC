/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import eu.mavinci.geo.GeoReferencedHelper;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Sector;
import java.util.OptionalDouble;

/**
 * This MapLayer implements the ISectorReferenced Interface, by asking every IGeoReferenced Sublayer for his extend
 *
 * @author caller
 */
public class MapLayerSectorReferenced extends MapLayer implements ISectorReferenced {

    public MapLayerSectorReferenced(boolean isVisible) {
        super(isVisible);
    }

    public MapLayerSectorReferenced(boolean isVisible, IMapLayer parent) {
        super(isVisible, parent);
    }

    @Override
    public OptionalDouble getMaxElev() {
        return GeoReferencedHelper.getMaxElev(subLayers);
    }

    @Override
    public OptionalDouble getMinElev() {
        return GeoReferencedHelper.getMinElev(subLayers);
    }

    @Override
    public Sector getSector() {
        return GeoReferencedHelper.getSector(subLayers);
    }

}
