/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.wwext.PolylineWithUserData;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.Recomputer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.ArrayList;

public class DatasetTrackLayer extends RenderableLayer implements IRecomputeListener {

    PolylineWithUserData renderable = null;
    AMapLayerMatching matching;

    public DatasetTrackLayer(AMapLayerMatching matching) {
        setPickEnabled(false);
        this.matching = matching;
        renderable = new PolylineWithUserData();
        renderable.setPathType(AVKey.LINEAR);
        addRenderable(renderable);
        matching.getCoverage().addRecomputeCornersListener(this);
        recomputeReady(null, false, -1); // force initialization now
    }

    @Override
    public void recomputeReady(Recomputer recomputer, boolean anotherRecomputeIsWaiting, long runNo) {
        ArrayList<ComputeCornerData> list = new ArrayList<>();
        ArrayList<Position> posList = new ArrayList<>();
        for (IMapLayer layer : matching.getPictures()) {
            if (layer instanceof MapLayerMatch) {
                MapLayerMatch match = (MapLayerMatch)layer;

                AerialPinholeImage img = match.getScreenImage();
                if (img == null) {
                    continue;
                }

                ComputeCornerData computeCornerData = img.getComputeCornerData();
                if (computeCornerData == null) {
                    continue;
                }

                posList.add(computeCornerData.getShiftedPosOnLevel());
            }
        }

        renderable.setPositions(posList);
    }
}
