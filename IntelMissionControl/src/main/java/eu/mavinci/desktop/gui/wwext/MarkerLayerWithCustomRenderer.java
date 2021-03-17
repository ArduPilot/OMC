/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

public class MarkerLayerWithCustomRenderer extends MarkerLayer {

    // in 2D mode this constant is added to "0" height of the marker and gives fewer eyedistance, which gives visibility
    // over raster map layers
    // better solution would be to reorder layers (to have markers always on top)

    // Similar solution was suggested in AerialPinholeImage.shiftedPosOnLevelPlus2 - which can lead to side effects
    // TODO refactor

    public static final double CUSTOM_CONSTANT = 0.02;

    private MarkerRenderer customMarkerRenderer =
        new MarkerRenderer() {

            /**
             * Overrides parent method - add a CUSTOM_CONSTANT in 2D mode
             *
             * @param dc
             * @param pos
             * @return
             */
            @Override
            protected Vec4 computeSurfacePoint(DrawContext dc, Position pos) {
                double ve = dc.getVerticalExaggeration();
                if (!this.isOverrideMarkerElevation()) {
                    Vec4 posV4 =
                        dc.getGlobe().computePointFromPosition(pos, dc.is2DGlobe() ? 0.0D : pos.getElevation() * ve);
                    if (dc.is2DGlobe()) {
                        return new Vec4(posV4.x, posV4.y, CUSTOM_CONSTANT, posV4.w);
                    } else {
                        return posV4;
                    }
                } else {
                    double effectiveElevation = dc.is2DGlobe() ? 0.0D : this.getElevation();
                    Vec4 point =
                        dc.getSurfaceGeometry()
                            .getSurfacePoint(pos.getLatitude(), pos.getLongitude(), effectiveElevation * ve);
                    return point != null
                        ? point
                        : dc.getGlobe()
                            .computePointFromPosition(pos.getLatitude(), pos.getLongitude(), effectiveElevation * ve);
                }
            }
        };

    public MarkerLayerWithCustomRenderer(Iterable<Marker> markers) {
        super(markers);
        super.setMarkerRenderer(customMarkerRenderer);
    }

    @Override
    public void setMarkerRenderer(MarkerRenderer markerRenderer) {
        // do nothing
    }

}
