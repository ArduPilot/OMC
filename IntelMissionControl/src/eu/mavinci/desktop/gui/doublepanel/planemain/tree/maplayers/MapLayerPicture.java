/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import eu.mavinci.desktop.gui.doublepanel.mapmanager.IResourceFileReferenced;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeImage;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeKnownImage;
import eu.mavinci.geo.ISectorReferenced;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.util.OptionalDouble;

public class MapLayerPicture extends MapLayer implements ISectorReferenced, IResourceFileReferenced {

    public final AerialPinholeImage img;
    IAirplane plane;

    public MapLayerPicture(AerialPinholeImage img, IAirplane plane) {
        super(true);
        this.plane = plane;
        img.setUserData(this);
        this.img = img;
    }

    public IAirplane getAirplane() {
        return plane;
    }

    /**
     * Returns the underlying renderable of this layer
     *
     * @return
     */
    public AerialPinholeImage getRenderable() {
        return img;
    }

    @Override
    public Sector getSector() {
        // System.out.println("getSector");
        if (img.isMatchable()) {
            return img.getSector();
        } else {
            return null;
        }
    }

    @Override
    public OptionalDouble getMaxElev() {
        return img.getMaxElev();
    }

    @Override
    public OptionalDouble getMinElev() {
        return img.getMinElev();
    }

    @Override
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
        img.setEnabled(isVisible);
    }

    @Override
    public File getResourceFile() {
        if (img instanceof AerialPinholeKnownImage) {
            AerialPinholeKnownImage knownImg = (AerialPinholeKnownImage)img;
            return knownImg.getPhotoFile().getResourceFile();
        }

        return null;
    }

    Double lastLat = null;
    Double lastLon = null;

}
