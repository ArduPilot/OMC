/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.wms;

import com.intel.missioncontrol.beans.binding.LifecycleValueConverter;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.map.elevation.ElevationLayerWrapper;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.settings.WmsServersSettings;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.OGCBoundingBox;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Converts from WorldWind's WmsLayerInfo to imagery ILayer */
public class WmsMapConverter implements LifecycleValueConverter<WmsMap, ILayer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WmsMapConverter.class);

    private final SynchronizationRoot syncRoot;
    private final String url;

    public WmsMapConverter(SynchronizationRoot syncRoot, String url) {
        this.syncRoot = syncRoot;
        this.url = url;
    }

    @Override
    public ILayer convert(WmsMap wmsMap) {
        if (url.startsWith("http://services.sentinel-hub.com")) {
            setDefaults(wmsMap);
        }

        WMSCapabilities caps = wmsMap.getCapabilities();
        AVList params = wmsMap.getParameters();
        WMSLayerCapabilities wmsLayerCapabilities = wmsMap.getCapabilitiesLayer();
        AVList configParams = params.copy(); // Copy to insulate changes from the caller.

        // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
        configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

        try {
            String factoryKey = getFactoryKeyForCapabilities(caps);
            Factory factory = (Factory)WorldWind.createConfigurationComponent(factoryKey);

            if (wmsLayerCapabilities.getCRS().size() > 0) {
                for (String epsg : wmsLayerCapabilities.getCRS()) {
                    if (epsg.equalsIgnoreCase("EPSG:4326") || epsg.equalsIgnoreCase("CRS:84")) {
                        configParams.setValue(AVKey.PROJECTION_EPSG_CODE, epsg);
                        break;
                    }
                }

                if (!configParams.hasKey(AVKey.PROJECTION_EPSG_CODE)) {
                    configParams.setValue(AVKey.PROJECTION_EPSG_CODE, wmsLayerCapabilities.getCRS().iterator().next());
                }
            } else if (wmsLayerCapabilities.getBoundingBoxes().iterator().hasNext()) {
                for (OGCBoundingBox bb : wmsLayerCapabilities.getBoundingBoxes()) {
                    if (bb.getCRS() != null
                            && (bb.getCRS().equalsIgnoreCase("EPSG:4326") || bb.getCRS().equalsIgnoreCase("CRS:84"))) {
                        configParams.setValue(AVKey.PROJECTION_EPSG_CODE, bb.getCRS());
                        break;
                    }
                }

                if (!configParams.hasKey(AVKey.PROJECTION_EPSG_CODE)) {
                    configParams.setValue(
                        AVKey.PROJECTION_EPSG_CODE, wmsLayerCapabilities.getBoundingBoxes().iterator().next().getCRS());
                }
            }

            Object component = factory.createFromConfigSource(caps, configParams);
            // if (o instanceof AVList) {
            //    AVList component = (AVList)o;
            //    sector = (Sector)configParams.getValue(AVKey.SECTOR);
            // }

            if (component instanceof Layer) {
                Layer layer = (Layer)component;
                layer.setValue(WWFactory.KEY_AV_Description, wmsMap.getAbstract());

                // nicing layer names e.g. for sentinel stuff
                if (layer.getName().endsWith(" : Default")) {
                    layer.setName(layer.getName().substring(0, layer.getName().length() - " : Default".length()));
                }

                ILayer imageLayer = new WWLayerWrapper(layer, syncRoot);
                String name = layer.getName();
                imageLayer.setName(new LayerName(name));
                imageLayer.enabledProperty().bindBidirectional(wmsMap.enabledProperty());

                return imageLayer;
            } else if (component instanceof ElevationModel) {
                LOGGER.warn(
                    "Unfortunately WMS elevation layers are not currently supported, the layer "
                        + wmsMap.getTitle()
                        + " is not shown");
            }
        } catch (Exception e) {
            LOGGER.warn("Problems creating WMS Layer", e);
        }

        return null;
    }

    private void setDefaults(WmsMap layerInfo) {
        if (layerInfo.getName().equals("ID")
                || layerInfo.getName().equals("DATE")
                || layerInfo.getName().equals("FILL")
                || layerInfo.getName().equals("OUTLINE")
                || layerInfo.getName().equals("CLOUDS")
                || layerInfo.getTitle().endsWith(" : Index")
                || layerInfo.getTitle().endsWith(" : Reflectance")
                || layerInfo.getTitle().endsWith(" : Sentinel DN")) {
            return;
        }

        layerInfo
            .getParameters()
            .setValue(AVKey.NUM_EMPTY_LEVELS, 5); // level 0..4=5Levels are NOT provided by sentinel-hub!
        layerInfo
            .getParameters()
            .setValue(AVKey.NUM_LEVELS, 12); // it seem that 0..10=11 level are perfect fot 10m resolution data, but
        // to be on the save side, lets go up to 12 here
        long expireTime = 1 * 24 * 60 * 60 * 1000L; // 1 day
        layerInfo
            .getParameters()
            .setValue(AVKey.EXPIRY_TIME, System.currentTimeMillis() - expireTime); // all files older than this will
        // be redownloaded
        layerInfo.getParameters().setValue(AVKey.IMAGE_FORMAT, "image/jpeg");
    }

    @Override
    public void update(WmsMap sourceValue, ILayer targetValue) {}

    @Override
    public void remove(ILayer layer) {}

    private static String getFactoryKeyForCapabilities(WMSCapabilities caps) {
        boolean hasApplicationBilFormat = false;

        Set<String> formats = caps.getImageFormats();
        for (String s : formats) {
            if (s.contains("application/bil")) {
                hasApplicationBilFormat = true;
                break;
            }
        }

        return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
    }

}
