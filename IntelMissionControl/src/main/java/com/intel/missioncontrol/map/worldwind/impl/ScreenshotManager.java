package com.intel.missioncontrol.map.worldwind.impl;

import static eu.mavinci.desktop.helper.MathHelper.extendSector;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
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
import gov.nasa.worldwind.javafx.WWGLNode;
import gov.nasa.worldwind.layers.LayerList;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.util.Pair;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

public class ScreenshotManager implements IScreenshotManager {

    private static final Angle MIN_DELTA = Angle.fromDegrees(1e-4); // less than 10 m is too few
    private static final double MIN_SIZE = 100; // 100 meters is better

    private final Dispatcher dispatcher;
    private final Provider<IWWGlobes> globesProvider;
    private final WWMapModel mapModel;
    private WorldWindow wwd;

    @Inject
    public ScreenshotManager(
            @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
            Provider<IWWGlobes> globesProvider,
            WorldWindowProvider worldWindowProvider,
            WWMapModel mapModel) {
        worldWindowProvider.whenAvailable(this::setWorldWindow);
        this.dispatcher = dispatcher;
        this.mapModel = mapModel;
        this.globesProvider = globesProvider;
    }

    public void setWorldWindow(WorldWindow worldWindow) {
        wwd = worldWindow;
    }

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    @Override
    public Future makeBackgroundScreenshotAsync(Sector minSector) {
        return dispatcher.getLaterAsync(
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
                return new Pair<>(screenshotModel, screenshotView);
            })
            .thenApplyAsync(pair -> snapshotAsync(WIDTH, HEIGHT, pair.getKey(), pair.getValue())
                .thenApply(image -> new Pair<>(pair.getValue(), image)))
            .thenApply(pair -> {
                // compute true captured sector
                Rectangle viewport = pair.getKey().getViewport();
                Position topLeft = pair.getKey().computePositionFromScreenPoint(viewport.x, viewport.y);
                Position bottomRight =
                    pair.getKey().computePositionFromScreenPoint(
                        viewport.x + viewport.width, viewport.y + viewport.height);

                Sector jpgSector = Sector.boundingSector(topLeft, bottomRight);

                return new Pair<>(pair.getValue(), jpgSector);
            });
    }

    @Override
    public Future<BufferedImage> makeAllLayersScreenshotAsync() {
        int width = (int)(((Node)wwd).getLayoutBounds().getWidth());
        int height = (int)(((Node)wwd).getLayoutBounds().getHeight());
        return snapshotAsync(width, height, wwd.getModel(), wwd.getView());
    }

    private Future<BufferedImage> snapshotAsync(int width, int height, Model model, View view) {
        FutureCompletionSource<BufferedImage> futureCompletionSource = new FutureCompletionSource<>();
        ((WWGLNode)wwd).snapshot(width, height, model, view, fxImage -> {
            BufferedImage image = SwingFXUtils.fromFXImage(fxImage, null);
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = newImage.createGraphics();

            try {
                graphics.drawImage(image, 0, 0, java.awt.Color.WHITE, null);
            } finally {
                graphics.dispose();
            }

            futureCompletionSource.setResult(newImage);
        });

        return futureCompletionSource.getFuture();
    }

}
