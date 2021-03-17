/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FramebufferTexture;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.WWTexture;

public class ProjectiveImage extends SurfaceImage {

    public ProjectiveImage(Object imageSource, Iterable<? extends LatLon> corners) {
        super(imageSource, corners);
    }

    public ProjectiveImage() {
        super();
    }

    @Override
    protected WWTexture initializeGeneratedTexture(DrawContext dc) {
        // If this SurfaceImage's is configured with a sector there's no need to generate a texture; we can
        // use the source texture to render the SurfaceImage.
        if (Sector.isSector(this.corners) && getSector().isSameSector(this.corners)) {
            if (this.sourceTexture.bind(dc)) {
                this.generatedTexture = this.sourceTexture;
                return this.generatedTexture;
            } else {
                return null;
            }
        } else {
            FramebufferTexture t = new ProjectiveTexture(this.sourceTexture, getSector(), this.corners);

            t.bind(dc);

            return t;
        }
    }
}
