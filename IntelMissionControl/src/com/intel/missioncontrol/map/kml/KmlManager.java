/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.kml;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.map.worldwind.KmlLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.KmlsSettings;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwindx.examples.util.OpenStreetMapShapefileLoader;
import gov.nasa.worldwindx.examples.util.ShapefileLoader;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KmlManager implements IKmlManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KmlManager.class);

    private final SimpleAsyncListProperty<ILayer> layers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<ILayer>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    @Inject
    public KmlManager(ISettingsManager settingsManager, @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot) {
        layers.overrideMetadata(
            new PropertyMetadata.Builder<AsyncObservableList<ILayer>>().synchronizationContext(syncRoot).create());

        layers.bindContent(
            settingsManager.getSection(KmlsSettings.class).kmlsProperty(),
            kmlSettings -> {
                Layer layer = null;
                String resource = kmlSettings.resourceProperty().get();
                if (resource == null) {
                    return null;
                }

                try {
                    switch (kmlSettings.typeProperty().get()) {
                    case KML:
                        layer = new RenderableLayer();
                        RenderableLayer rRayer = (RenderableLayer)layer;
                        KMLRoot kmlRoot = KMLRoot.create(resource);

                        if (kmlRoot == null) {
                            String message =
                                Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource", resource);
                            throw new IllegalArgumentException(message);
                        }

                        kmlRoot.parse();
                        KMLController kmlController = new KMLController(kmlRoot);
                        rRayer.addRenderable(kmlController);
                        break;

                    case SHP:
                        if (OpenStreetMapShapefileLoader.isOSMPlacesSource(resource)) {
                            layer = OpenStreetMapShapefileLoader.makeLayerFromOSMPlacesSource(resource);
                        } else {
                            try (Shapefile shp = new Shapefile(resource)) {
                                ShapefileLoader loader = new ShapefileLoader();
                                layer = loader.createLayerFromShapefile(shp);

                                // TODO MAPDEVELOP make this sector avaliable for flyTo actions
                                Sector sector = Sector.fromDegrees(shp.getBoundingRectangle());
                            }
                        }

                        break;
                    }
                } catch (Exception e) {
                    LOGGER.error("cant load KML/SHP: " + kmlSettings, e);

                    // just provide an empty layer as return
                    layer = new RenderableLayer();
                }

                ILayer wwLayerWrapper = new KmlLayerWrapper(layer, syncRoot, kmlSettings);
                wwLayerWrapper.enabledProperty().bindBidirectional(kmlSettings.enabledProperty());
                wwLayerWrapper.setNameAsync(new LayerName(new File(resource).getName()));
                return wwLayerWrapper;
            });
    }

    @Override
    public AsyncObservableList<ILayer> imageryLayersProperty() {
        return layers;
    }

}
