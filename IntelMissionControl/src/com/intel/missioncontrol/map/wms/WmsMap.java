/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

/*
This class is mostly copied from WW examples... So what should be here ?
 */
package com.intel.missioncontrol.map.wms;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwind.util.WWUtil;
import java.util.Objects;

/**
 * Map as in geographical map, not a data structure. Contains information about a map contained in the WMS Server's
 * WmsCapabilities. WmsServer manages a list of maps
 */
public class WmsMap {

    private WMSCapabilities caps;
    private WMSLayerCapabilities capsLayer;
    private AVListImpl params = new AVListImpl();
    private final AsyncBooleanProperty enabled = new SimpleAsyncBooleanProperty(this);

    public String getTitle() {
        return params.getStringValue(AVKey.DISPLAY_NAME);
    }

    public String getName() {
        return params.getStringValue(AVKey.LAYER_NAMES);
    }

    public String getAbstract() {
        return params.getStringValue(AVKey.LAYER_ABSTRACT);
    }

    public static WmsMap createMapInfo(WMSCapabilities caps, WMSLayerCapabilities layerCaps, WMSLayerStyle style) {
        // Create the layer info specified by the layer's capabilities entry and the selected style.
        WmsMap linfo = new WmsMap();
        linfo.caps = caps;
        linfo.capsLayer = layerCaps;
        linfo.params = new AVListImpl();
        linfo.params.setValue(AVKey.LAYER_NAMES, layerCaps.getName());
        if (style != null) {
            linfo.params.setValue(AVKey.STYLE_NAMES, style.getName());
        }

        String abs = layerCaps.getLayerAbstract();
        if (!WWUtil.isEmpty(abs)) {
            linfo.params.setValue(AVKey.LAYER_ABSTRACT, abs);
        }

        linfo.params.setValue(AVKey.DISPLAY_NAME, makeTitle(caps, linfo));
        return linfo;
    }

    private static String makeTitle(WMSCapabilities caps, WmsMap layerInfo) {
        String layerNames = layerInfo.params.getStringValue(AVKey.LAYER_NAMES);
        String styleNames = layerInfo.params.getStringValue(AVKey.STYLE_NAMES);
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            String layerName = lNames[i];
            WMSLayerCapabilities lc = caps.getLayerByName(layerName);
            String layerTitle = lc.getTitle();
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i) {
                continue;
            }

            String styleName = sNames[i];
            WMSLayerStyle style = lc.getStyleByName(styleName);
            if (style == null) {
                continue;
            }

            sb.append(" : ");
            String styleTitle = style.getTitle();
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }

    public WMSCapabilities getCapabilities() {
        return caps;
    }

    public AVListImpl getParameters() {
        return params;
    }

    public WMSLayerCapabilities getCapabilitiesLayer() {
        return capsLayer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof WmsMap) {
            return this.getTitle().equals(((WmsMap)obj).getTitle());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTitle());
    }

    public AsyncBooleanProperty enabledProperty() {
        return enabled;
    }
}
