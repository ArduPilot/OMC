/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerListener;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeImage;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ComputeCornerData;
import gov.nasa.worldwind.geom.LatLon;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MapLayerCoverageMatching extends AMapLayerCoverage implements IMatchingRelated {

    AMapLayerMatching matching;

    @Override
    public void resolutionChanged() {
        super.resolutionChanged();
        matching.setChanged(true);
    };

    IMapLayerListener parentListener =
        new IMapLayerListener() {

            @Override
            public void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility) {
                if (layer instanceof MapLayerPicArea) {
                    recomputeCoverage();
                }
                // System.out.println("mapLayerVisibilityChanged" + layer + " new Vis="+newVisibility);
            }

            @Override
            public void mapLayerValuesChanged(IMapLayer layer) {
                if (layer instanceof MapLayerPicArea) {
                    recomputeCoverage();
                }

                if (layer instanceof MapLayerPics) {
                    recomputeCoverage();
                }
                // System.out.println("mapLayerValuesChanged" + layer);
            }

            @Override
            public void mapLayerStructureChanged(IMapLayer layer) {
                // System.out.println("mapLayerStructureChanged" + layer);
                if (!(layer instanceof MapLayerMatch) && !(layer instanceof MapLayerPicArea)) {
                    return;
                }

                recomputeCoverage();
            }

            @Override
            public void childMapLayerRemoved(int i, IMapLayer layer) {
                // System.out.println("childMapLayerRemoved" + layer);
                if (!(layer instanceof MapLayerMatch) && !(layer instanceof MapLayerPicArea)) {
                    return;
                }

                recomputeCoverage();
            }

            @Override
            public void childMapLayerInserted(int i, IMapLayer layer) {
                // System.out.println("childMapLayerInserted" + layer);
                if (!(layer instanceof MapLayerMatch) && !(layer instanceof MapLayerPicArea)) {
                    return;
                }

                recomputeCoverage();
            }
        };

    public MapLayerCoverageMatching(final AMapLayerMatching matching) {
        super(true);
        this.matching = matching;
        matching.addMapListener(parentListener);
    }

    @Override
    public AMapLayerMatching getMatching() {
        return matching;
    }

    @Override
    //    public ArrayList<ImagePolygone> computeCorners() {
    public ArrayList<ComputeCornerData> computeCorners() {
        if (!getMatching().getPicsLayer().isProjectingToGround()) return null;
        //        ArrayList<ImagePolygone> list = new ArrayList<>();
        ArrayList<ComputeCornerData> list = new ArrayList<>();
        for (IMapLayer layer : matching.getPictures()) {
            if (layer instanceof MapLayerMatch) {
                MapLayerMatch match = (MapLayerMatch)layer;
                if (!match.isPassFilter()) {
                    continue;
                }

                AerialPinholeImage img = match.img;
                if (img == null) {
                    continue;
                }

                ComputeCornerData computeCornerData = img.getComputeCornerData();
                if (computeCornerData == null) {
                    continue;
                }

                computeCornerData.updateLineNumber(match.line.lineNumber);
                if (!computeCornerData.isElevationDataReady()) {
                    elevationDataAvaliableTmp = false;
                }

                ArrayList<LatLon> corners = computeCornerData.getGroundProjectedCorners();
                if (corners == null) {
                    continue;
                }

                list.add(computeCornerData);
            }
        }

        return list;
    }

    @Override
    public ArrayList<CornerMask> computeMaskCorners() {
        ArrayList<CornerMask> masks = new ArrayList<>();
        for (MapLayerPicArea picArea : matching.getVisiblePicAreas()) {
            CornerMask mask = new CornerMask();
            mask.corners = new Vector<LatLon>(picArea.getCorners());
            mask.gsd = picArea.gsd;
            masks.add(mask);
        }

        return masks;
    }

    @Override
    public boolean containsNonCoverageAbleAOIs() {
        return false;
    }

    @Override
    public SectorType getSectorType() {
        List<MapLayerPicArea> tmp = matching.getVisiblePicAreas();
        if (tmp != null) {
            for (MapLayerPicArea picArea : tmp) {
                if (picArea.getSector() != null) {
                    return SectorType.truncated_redInside;
                }
            }
        }

        return SectorType.auto_redInside;
    }

    public static String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerCoverageMatching";

    @Override
    public String getTooltipAddon() {
        return StaticInjector.getInstance(ILanguageHelper.class).getString(KEY + ".tooltipAddon");
    }

    @Override
    public String toString() {
        return "Matching Coverage of: " + matching + " " + super.toString();
    }

    @Override
    protected void updateCameraCornersBlocking() {
        matching.getPictures()
            .stream() // .parallelStream() //with this paralelism is actually working, but in fact its 20% slower
            .forEach(
                layer -> {
                    if (layer instanceof MapLayerMatch) {
                        MapLayerMatch match = (MapLayerMatch)layer;
                        match.img.recomputeCornersSync();
                    }
                });
    }

    @Override
    protected void updateCameraCornersBlockingDone() {
        // since the filter might not fire a change and inform the coverage that everything is done: just to be save,
        // trigger filter update and coverage update both here
        matching.recompute();
        matching.getPicsLayer().layer.fireImageLayerChanged();
        super.updateCameraCornersBlockingDone();
    }

    @Override
    public boolean shouldShowCoverage() {
        return true;
    }
}
