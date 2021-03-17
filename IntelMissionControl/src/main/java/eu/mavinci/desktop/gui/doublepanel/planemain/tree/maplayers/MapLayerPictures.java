/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenter;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenterListener;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPhoto;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeImage;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeImageLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeUnknownImage;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ComputeCornerData;
import eu.mavinci.desktop.gui.wwext.IAerialPinholeImageLayerListener;
import eu.mavinci.desktop.helper.IVectorAccess;
import eu.mavinci.geo.ISectorReferenced;
import eu.mavinci.plane.IAirplane;
import java.util.LinkedList;

public class MapLayerPictures extends MapLayerSectorReferenced
        implements ISectorReferenced,
            IAerialPinholeImageLayerListener,
            IAirplaneListenerPhoto,
            IMapLayerWW,
            IVectorAccess,
            ComputeCornerData.IAerialPinholeImageContext {

    // private AirplaneSession session;
    IAirplane plane;

    IMapClearingCenterListener listener =
        new IMapClearingCenterListener() {
            @Override
            public void clearUavImageCache() {
                removeAllPicture();
            }

            @Override
            public void clearTrackLog() {}

            @Override
            public void clearOldTrackCache() {
                clearTrackCacheOld();
            }
        };

    public MapLayerPictures(AerialPinholeImageLayer wwdLayer, IAirplane plane) {
        super(false);
        this.wwdLayer = wwdLayer;
        this.plane = plane;
        // session = plane.getSession();
        wwdLayer.addListener(this);
        plane.addListener(this);
        // plane.getPlatformDescription().addListener(this);
        // colToSave = ColorHelper.removeAlpha(wwdLayer.getColor());

        StaticInjector.getInstance(IMapClearingCenter.class).addWeakListener(listener);
    }

    public MapLayerPictures(IAirplane plane) {
        this(new AerialPinholeImageLayer("PICTURE_LAYER_NAME"), plane);
    }

    private AerialPinholeImageLayer wwdLayer;

    /** remove all loaded images */
    public void clear() {
        wwdLayer.removeAllRenderables();
    }

    @Override
    public void imageLayerChanged() {
        // the underlying WW Layer changed... (maybe the number of pictures)

        // so here we have to adjust the number of sublayers (=images)
        /*WrapperListSync.sync(
        this,
        new AWrappedListSyncSourceHandler() {

            Iterator<AerialPinholeImage> iter = wwdLayer.getImages().iterator();

            AerialPinholeImage last = null;

            @Override
            public Object getWrapperObjectForLastObject() {
                if (last instanceof AerialPinholeUnknownImage) {
                    AerialPinholeUnknownImage img = (AerialPinholeUnknownImage)last;
                    return new MapLayerPictureUnknown(img, plane);
                }

                return new MapLayerPicture(last, plane);
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Object next() {
                last = iter.next();
                return last;
            }

        },
        0,
        sizeMapLayer());
        */

        // subLayers = new LinkedList<IMapLayer>();
        // LinkedList<AerialPinholeImage> images = wwdLayer.getImages();
        // for(AerialPinholeImage image : images){
        // MapLayer layer = new MapLayerPicture(image);
        // layer.setParent(this);
        // subLayers.add(layer);
        // }
        //
        // //imform gui about other layer
        // mapLayerValuesChanged(this);
    }

    @Override
    public void recv_photo(PhotoData photo) {
        // System.out.println("new photo:" + photo.toString());
        try {
            if (plane.getAirplaneCache().getFlightPhase().isGroundTarget()) {
                return;
            }
        } catch (AirplaneCacheEmptyException e2) {
        }

        // if (session.getProperty(AirplaneSession.KEY_AUTO_DISPLAYING_PICTURES, "false").equals("false")) return;
        if (photo.groundspeed <= PlaneConstants.UNDEFINED_THREASHOLD) {
            try {
                photo.groundspeed = plane.getAirplaneCache().getPosition().groundspeed;
            } catch (AirplaneCacheEmptyException e) {
                try {
                    photo.groundspeed = plane.getAirplaneCache().getDebugData().groundspeed;
                } catch (AirplaneCacheEmptyException e1) {
                }
            }
        }

        if (photo.heading <= PlaneConstants.UNDEFINED_THREASHOLD) {
            try {
                photo.heading = plane.getAirplaneCache().getDebugData().heading;
            } catch (AirplaneCacheEmptyException e) {
                try {
                    photo.heading = (float)plane.getAirplaneCache().getHeadingEstimate();
                } catch (AirplaneCacheEmptyException e1) {
                }
            }
        }

        AerialPinholeImage img = new AerialPinholeUnknownImage(photo, this);
        // System.out.println("addingimg:"+img);
        wwdLayer.addRenderable(img);
    }

    private synchronized void clearTrackCacheOld() {
        long expire =
            System.currentTimeMillis()
                - StaticInjector.getInstance(GeneralSettings.class).getAutoClearingIntervallInMS();
        // what is 1 minute older than
        // required
        // System.out.println("dropping everythign what expires somewhere here: " + expire);
        LinkedList<AerialPinholeUnknownImage> toDrop = new LinkedList<AerialPinholeUnknownImage>();
        for (AerialPinholeImage r : wwdLayer.getImages()) {
            if (r instanceof AerialPinholeUnknownImage) {
                AerialPinholeUnknownImage img = (AerialPinholeUnknownImage)r;
                // System.out.println("this line expires:" + img.timestamp);
                if (img.timestamp <= expire) {
                    toDrop.add(img);
                }
            }
        }

        if (toDrop.isEmpty()) {
            return;
        }

        for (AerialPinholeUnknownImage r : toDrop) {
            wwdLayer.removeRenderable(r);
            // System.out.println("dropping old renderable:"+r.timestamp);
        }
        // wwdLayer.firePropertyChange(AVKey.LAYER, null, wwdLayer);
    }

    public void addPicture(MapLayerPicture pic) {
        wwdLayer.addRenderable(pic.getRenderable());
    }

    public void removePicture(MapLayerPicture pic) {
        wwdLayer.removeRenderable(pic.getRenderable());
    }

    public void removeAllPicture() {
        clear();
    }

    @Override
    public AerialPinholeImageLayer getWWLayer() {
        // System.out.println("MapLayerPictures is readed out");
        // (new Exception()).printStackTrace();
        return wwdLayer;
    }

    @Override
    public Object getUserObject(int i) {
        return ((MapLayerPicture)subLayers.get(i)).img;
    }

    @Override
    public void insertWrapped(Object o, int i) {
        addMapLayer(i, (MapLayer)o);
    }

    @Override
    public void removeWrapped(int i) {
        removeMapLayer(i);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (plane != null) {
            plane.removeListener(this);
        }
    }

    public Double getStartingElevationOverWgs84() {
        try {
            return plane.getAirplaneCache().getStartElevOverWGS84();
        } catch (AirplaneCacheEmptyException e) {
            return null;
        }
    }

    public double getProjectionDistance() {
        return 5; // currently this value is not in use... so it doesnt matter
    }

    public IHardwareConfiguration getHardwareConfiguration() {
        return plane.getHardwareConfiguration();
    }

    public gov.nasa.worldwind.geom.LatLon getStartingPosition() {
        try {
            return plane.getAirplaneCache().getStartPosBaro();
        } catch (AirplaneCacheEmptyException e) {
            return null;
        }
    }

    public gov.nasa.worldwind.geom.Vec4 getRtkOffset() {
        return null;
    }

    @Override
    public double getStartingElevationOverWgs84WithOffset() {
        return getStartingElevationOverWgs84();
    }

    @Override
    public double getElevationOffset() {
        return 0;
    }
}
