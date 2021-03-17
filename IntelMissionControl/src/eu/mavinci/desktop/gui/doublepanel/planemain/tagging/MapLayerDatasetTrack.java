/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerWW;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.DatasetTrackLayer;
import eu.mavinci.geo.GeoReferencedHelper;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import java.util.OptionalDouble;

public class MapLayerDatasetTrack extends MapLayer implements IMapLayerWW, ISectorReferenced, IMatchingRelated {

    DatasetTrackLayer layer;
    AMapLayerMatching matching;

    public MapLayerDatasetTrack(AMapLayerMatching matching) {
        super(true);
        this.matching = matching;
        layer = new DatasetTrackLayer(matching);
    }

    public OptionalDouble getMaxElev() {
        return GeoReferencedHelper.getMaxElev(matching.pics.getMatchesFiltered());
    }

    @Override
    public OptionalDouble getMinElev() {
        return GeoReferencedHelper.getMinElev(matching.pics.getMatchesFiltered());
    }

    @Override
    public Sector getSector() {
        return GeoReferencedHelper.getSector(matching.pics.getMatchesFiltered());
    }

    @Override
    public AMapLayerMatching getMatching() {
        return matching;
    }

    @Override
    public void dispose() {
        layer.dispose();
        super.dispose();
    }

    @Override
    public Layer getWWLayer() {
        return layer;
    }

}
