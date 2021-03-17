/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.impl;

import static eu.mavinci.desktop.helper.MathHelper.extendSector;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.map.worldwind.WWMapModel;
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import com.intel.missioncontrol.modules.MapModule;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.javafx.WWImage;
import gov.nasa.worldwind.layers.LayerList;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import javafx.scene.Node;
import javafx.util.Pair;

public class ScreenshotManager implements IScreenshotManager {

    private static final Angle MIN_DELTA = Angle.fromDegrees(1e-4); // less than 10 m is too few
    private static final double MIN_SIZE = 100; // 100 meters is better

    private final SynchronizationRoot synchronizationRoot;
    private final Provider<IWWGlobes> globesProvider;
    private WorldWindow wwd;
    private final WWMapModel mapModel;
    private final IWWMapView mapView;

    @Inject
    public ScreenshotManager(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            Provider<IWWGlobes> globesProvider,
            WorldWindowProvider worldWindowProvider,
            WWMapModel mapModel,
            IWWMapView mapView) {
        worldWindowProvider.whenAvailable(this::setWorldWindow);
        this.synchronizationRoot = syncRoot;
        this.mapModel = mapModel;
        this.mapView = mapView;
        this.globesProvider = globesProvider;
    }

    public void setWorldWindow(WorldWindow worldWindow) {
        wwd = worldWindow;
    }

    @Override
    public FluentFuture makeBackgroundScreenshot(Sector minSector) {
        final int WIDTH = 1920 * 2;
        final int HEIGHT = 1080 * 2;

        return synchronizationRoot.post(
            () -> {
                List<gov.nasa.worldwind.layers.Layer> background = mapModel.getBackgroundLayers();
                LayerList layerList = new LayerList();
                layerList.addAll(background);

                Globe globe = globesProvider.get().getFlatGlobe();
                Model screenshotModel = new BasicModel(globe, layerList);

                MView screenshotView = new ScreenshotView();
                screenshotView.setGlobe(globe);
                screenshotView.setFlatEarth(true);
                screenshotView.setViewPort(WIDTH, HEIGHT);
                Sector sector = minSector;
                if (minSector.getDeltaLon().degrees < MIN_DELTA.degrees
                        || minSector.getDeltaLat().degrees < MIN_DELTA.degrees) {
                    // if a sector is too small (like for panorama) - then take a bigger screenshot
                    sector = extendSector(minSector, MIN_SIZE);
                }

                screenshotView.flyToSector(sector, 0, true);
                screenshotView.setZoom(screenshotView.getZoom() * 1.5);
                // 4k res
                BufferedImage screenshot = takeScreenshot(screenshotModel, screenshotView, WIDTH, HEIGHT);

                // compute true captured sector
                Rectangle viewport = screenshotView.getViewport();
                Position topLeft = screenshotView.computePositionFromScreenPoint(viewport.x, viewport.y);
                Position bottomRight =
                    screenshotView.computePositionFromScreenPoint(
                        viewport.x + viewport.width, viewport.y + viewport.height);

                Sector jpgSector = Sector.boundingSector(topLeft, bottomRight);

                return new Pair<>(screenshot, jpgSector);
            });
    }

    @Override
    public BufferedImage makeAllLayersScreenshot() {
        int width = (int)(((Node)wwd).getLayoutBounds().getWidth());
        int height = (int)(((Node)wwd).getLayoutBounds().getHeight());
        return takeScreenshot(mapModel.getWWModel(), mapView, width, height);
    }

    private BufferedImage takeScreenshot(Model model, View view, int width, int height) {
        WWImage image = new WWImage(width, height);
        image.snapshot(model, view);
        BufferedImage image1 = javafx.embed.swing.SwingFXUtils.fromFXImage(image, null);
        BufferedImage image2 =
            new java.awt.image.BufferedImage(
                image1.getWidth(), image1.getHeight(), java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
        java.awt.Graphics g = image2.getGraphics();
        g.drawImage(image1, 0, 0, null);
        g.dispose();

        return image2;
    }
}
