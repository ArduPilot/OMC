/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.rendering;

import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.ISelectionManager;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerCoverageMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerDatasetTrack;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicArea;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicAreas;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPics;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerRTKPosition;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayerListener;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import java.awt.Point;
import java.util.ArrayList;
import org.asyncfx.concurrent.Dispatcher;

public class MatchingLayer extends AbstractLayer {

    private ArrayList<Layer> innerLayers = new ArrayList<>();
    private ArrayList<Layer> innterLayersInRendering = innerLayers;

    private final IMapController mapController;
    private final AMapLayerMatching matching;
    private final Dispatcher dispatcher;
    private final ISelectionManager selectionManager;

    IMapLayerListener listener =
        new IMapLayerListener() {
            @Override
            public void mapLayerValuesChanged(IMapLayer layer) {
                flagRedrawNeeded();
            }

            @Override
            public void mapLayerVisibilityChanged(IMapLayer layer, boolean newVisibility) {
                reconstruct();
            }

            @Override
            public void childMapLayerInserted(int i, IMapLayer layer) {
                if (layer instanceof MapLayerPicArea) {
                    reconstruct();
                } else {
                    flagRedrawNeeded();
                }
            }

            @Override
            public void childMapLayerRemoved(int i, IMapLayer layer) {
                if (layer instanceof MapLayerPicArea) {
                    reconstruct();
                } else {
                    flagRedrawNeeded();
                }
            }

            @Override
            public void mapLayerStructureChanged(IMapLayer layer) {
                if (layer instanceof MapLayerPicAreas || layer instanceof AMapLayerMatching) {
                    reconstruct();
                } else {
                    flagRedrawNeeded();
                }
            }

        };

    public MatchingLayer(
            AMapLayerMatching matching,
            IMapController mapController,
            ISelectionManager selectionManager,
            Dispatcher dispatcher) {
        this.matching = matching;
        this.mapController = mapController;
        this.dispatcher = dispatcher;
        this.selectionManager = selectionManager;
        matching.addMapListener(listener);
        reconstruct();
    }

    private void reconstruct() {
        dispatcher.run(
            () -> {
                // not needed to do this on synchronisation root
                ArrayList<Layer> innerLayers = new ArrayList<>();
                MapLayerCoverageMatching coverageLayer = matching.getCoverage();
                if (coverageLayer != null && coverageLayer.isVisibleIncludingParent()) {
                    innerLayers.add(coverageLayer.getWWLayer());
                }

                MapLayerPics picsLayer = matching.getPicsLayer();
                if (picsLayer != null && picsLayer.isVisibleIncludingParent()) {
                    innerLayers.add(picsLayer.getWWLayer());
                }

                if (matching instanceof MapLayerMatching) {
                    MapLayerMatching matchingR = (MapLayerMatching)matching;

                    MapLayerRTKPosition rtkLayer = matchingR.getMayLayerRTKPosition();
                    if (rtkLayer != null && rtkLayer.isVisibleIncludingParent()) {
                        innerLayers.add(rtkLayer.getWWLayer());
                    }

                    MapLayerDatasetTrack trackLayer = matchingR.getTrackLayer();
                    if (trackLayer != null && trackLayer.isVisibleIncludingParent()) {
                        innerLayers.add(trackLayer.getWWLayer());
                    }
                }

                for (MapLayerPicArea picArea : matching.getPicAreas()) {
                    if (picArea != null && picArea.isVisibleIncludingParent()) {
                        innerLayers.add(new TaggingPicAreaLayer(picArea, mapController, selectionManager, dispatcher));
                    }
                }

                MatchingLayer.this.innerLayers = innerLayers;
                flagRedrawNeeded();
            });
    }

    private void flagRedrawNeeded() {
        dispatcher.run(
            () -> {
                firePropertyChange(AVKey.LAYER, null, this);
            });
    }

    @Override
    protected void doRender(DrawContext dc) {
        for (Layer layer : innterLayersInRendering) {
            layer.render(dc);
        }
    }

    @Override
    protected void doPick(DrawContext dc, Point point) {
        for (Layer layer : innterLayersInRendering) {
            layer.pick(dc, point);
        }
    }

    @Override
    protected void doPreRender(DrawContext dc) {
        // snapshotting whats current to render
        innterLayersInRendering = innerLayers;
        for (Layer layer : innterLayersInRendering) {
            layer.preRender(dc);
        }
    }
}
