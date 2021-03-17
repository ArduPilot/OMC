/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.concurrent.Dispatcher;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerWW;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AerialPinholeImageLayer;
import eu.mavinci.desktop.gui.widgets.MProgressMonitor;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.geo.GeoReferencedHelper;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Sector;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.logging.Level;

public class MapLayerPics extends MapLayer implements IMapLayerWW, ISectorReferenced, IMatchingRelated {

    final AerialPinholeImageLayer layer;

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPics";

    AMapLayerMatching matching;

    public MapLayerPics(AMapLayerMatching matching, boolean showOnlyOutlines) {
        super(true);
        this.showOnlyOutlines = showOnlyOutlines;
        this.matching = matching;
        layer = new AerialPinholeImageLayer(this);
    }

    private boolean showOnlyOutlines = false;
    private boolean showImageLocations = false;

    public boolean isShowOnlyOutlines() {
        return showOnlyOutlines;
    }

    public boolean isShowingImages() {
        return isVisible && !showOnlyOutlines;
    }

    public boolean isShowImageLocations() {
        return showImageLocations;
    }

    public void setShowImageLocations(boolean showImageLocations) {
        if (this.showImageLocations == showImageLocations) {
            return;
        }

        this.showImageLocations = showImageLocations;
        mapLayerValuesChanged(this);
        this.layer.resetVisibility();
    }

    public boolean isProjectingToGround() {
        return matching.getProjectionType() == ProjectionType.SURVEYS_2D;
    }

    public void setShowOnlyOutlines(boolean showOnlyOutlines) {
        if (this.showOnlyOutlines == showOnlyOutlines) {
            return;
        }

        this.showOnlyOutlines = showOnlyOutlines;
        if (!showOnlyOutlines && isVisible) {
            generatePreview();
        }

        mapLayerValuesChanged(this);
        this.layer.resetVisibility();
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (isVisible == this.isVisible) {
            return;
        }

        if (!showOnlyOutlines && isVisible) {
            generatePreview();
        }

        super.setVisible(isVisible);
        this.layer.resetVisibility();
    }

    public void setTristate(boolean isVisible, boolean showOnlyOutlines) {
        if (this.isVisible != isVisible || this.showOnlyOutlines != showOnlyOutlines) {
            this.showOnlyOutlines = showOnlyOutlines;
            this.isVisible = isVisible;
            if (!showOnlyOutlines && isVisible) {
                generatePreview();
            }

            mapLayerVisibilityChanged(this, isVisible);
            mapLayerValuesChanged(this);
            this.layer.resetVisibility();
        }
    }

    @Override
    public IMapLayer removeMapLayer(int i) {
        MapLayerMatch match = (MapLayerMatch)getMapLayer(i);
        if (match.img != null) {
            layer.removeRenderable(match.img);
        }

        return super.removeMapLayer(i);
    }

    public void removeAllLayers(boolean doDispose) {
        if (sizeMapLayer() == 0) {
            return;
        }

        layer.removeAllRenderables();
        super.removeAllLayers(doDispose);
    }

    @Override
    public void addMapLayer(int i, IMapLayer layer) {
        super.addMapLayer(
            i, layer); // try to do this first, in order to have at least one image added at the moment where the first
        // time
        // the starting elevation is requested
        this.layer.addRenderable(((MapLayerMatch)layer).img);
    }

    @Override
    public void setMute(boolean mute) {
        super.setMute(mute);
        layer.setMute(mute);
    }

    @Override
    public void setSilentUnmute() {
        super.setSilentUnmute();
        layer.setSilentUnmute();
    }

    private static final String KEY_SUFFIX_ShowOnlyOutlines = ".showOnlyOutlines";
    private static final String SELECTED = ".selected";
    private static final String SHOW_LOCATIONS = ".showLocations";

    @Override
    public void mapLayerValuesChanged(IMapLayer layer) {
        if (mute) {
            return;
        }

        super.mapLayerValuesChanged(layer);
    }

    @Override
    public AerialPinholeImageLayer getWWLayer() {
        return layer;
    }

    public List<MapLayerMatch> getMatchesFiltered() {
        ArrayList<MapLayerMatch> list = new ArrayList<MapLayerMatch>();
        for (IMapLayer subLayer : getLayers()) {
            if (subLayer instanceof MapLayerMatch) {
                MapLayerMatch match = (MapLayerMatch)subLayer;
                if (match.isPassFilter()) {
                    list.add(match);
                }
            }
        }

        return list;
    }

    public List<MapLayerMatch> getMatches() {
        ArrayList<MapLayerMatch> list = new ArrayList<MapLayerMatch>();
        for (IMapLayer subLayer : getLayers()) {
            if (subLayer instanceof MapLayerMatch) {
                MapLayerMatch match = (MapLayerMatch)subLayer;
                list.add(match);
            }
        }

        return list;
    }

    @Override
    public OptionalDouble getMaxElev() {
        return GeoReferencedHelper.getMaxElev(getMatchesFiltered());
    }

    @Override
    public OptionalDouble getMinElev() {
        return GeoReferencedHelper.getMinElev(getMatchesFiltered());
    }

    @Override
    public Sector getSector() {
        return GeoReferencedHelper.getSector(getMatchesFiltered());
    }

    private boolean isJustNowGeneratingPreview = false;

    public void generatePreview() {
        if (isJustNowGeneratingPreview) {
            return;
        }
        // (new Exception()).printStackTrace();
        // precheck if their would be something to do
        boolean nothingToDo = true;
        for (int i = 0; i != sizeMapLayer(); i++) {
            IMapLayer layer = getMapLayer(i);
            if (layer instanceof MapLayerMatch) {
                final MapLayerMatch match = (MapLayerMatch)layer;
                if (match.img != null
                        && match.getCurPhotoFile() != null
                        && !match.getCurPhotoFile().thumpFileExists()) {
                    nothingToDo = false;
                    break;
                }
            }
        }

        if (nothingToDo) {
            return;
        }

        isJustNowGeneratingPreview = true;
        Runnable r =
            new Runnable() {

                @Override
                public void run() {
                    MProgressMonitor mon = null;
                    try {
                        //                    mon = new MProgressMonitor(null, Language.getString(KEY + ".mon.title"),
                        // Language.getString(KEY + ".mon.initNote"), 0,
                        //                        sizeMapLayer());
                        for (int i = 0; i != sizeMapLayer(); i++) {
                            //                        if (mon.isCanceled()) {
                            //                            break;
                            //                        }
                            //                        mon.setProgressNote(Language.getFormat(KEY + ".mon.note", i,
                            // sizeMapLayer()), i);
                            IMapLayer layer = getMapLayer(i);
                            if (layer instanceof MapLayerMatch) {
                                final MapLayerMatch match = (MapLayerMatch)layer;

                                try {
                                    if (!match.getCurPhotoFile().thumpFileExists()) {
                                        match.getCurPhotoFile().generateThumpFile();
                                        // MapLayerPics.this.layer.resetVisibility(); //will be too slow!
                                    }
                                } catch (Throwable e) {
                                    Debug.getLog()
                                        .log(
                                            Level.WARNING,
                                            "could not generate preview Image of "
                                                + match.getCurPhotoFile().getThumpFile(),
                                            e);
                                }
                                // ThreadingHelper.getThreadingHelper().invokeOnUiThread(new Runnable() {
                                // @Override
                                // public void run() {
                                // match.setVisible(true);
                                // }
                                // });

                            }
                        }

                        //                    if (!mon.isCanceled()) {
                        Dispatcher.postToUI(
                            new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i != sizeMapLayer(); i++) {
                                        IMapLayer layer = getMapLayer(i);
                                        if (layer instanceof MapLayerMatch) {
                                            final MapLayerMatch match = (MapLayerMatch)layer;
                                            match.setVisible(true);
                                        }
                                    }
                                }
                            });
                    } finally {
                        isJustNowGeneratingPreview = false;
                        MapLayerPics.this.layer.resetVisibility();
                    }
                }
            };
        Dispatcher.post(r);
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

}
