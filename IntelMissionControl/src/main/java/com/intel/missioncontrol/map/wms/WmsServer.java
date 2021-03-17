/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.wms;

import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.settings.WmsLayerEnabledSettings;
import com.intel.missioncontrol.settings.WmsServerSettings;
import java.net.URI;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.concurrent.Dispatcher;

public class WmsServer {

    private final WmsServerSettings wmsSettings;

    private WmsCapabilitiesWrapper wmsCapabilitiesWrapper;

    private String serverUrl;

    private final AsyncBooleanProperty hasWarning = new SimpleAsyncBooleanProperty(this);
    private final AsyncStringProperty warning = new SimpleAsyncStringProperty(this);

    // wms server name
    private final AsyncObjectProperty<LayerName> serverName = new SimpleAsyncObjectProperty<>(this);

    // collection of maps provided by this WMS server
    private final AsyncListProperty<WmsMap> wmsMaps =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WmsMap>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    /**
     * @param uri must be valid
     * @param pathProvider
     */
    public WmsServer(URI uri, IPathProvider pathProvider) {
        this.wmsSettings = new WmsServerSettings();
        this.serverUrl = uri.toString();

        serverName.set(new LayerName(serverUrl));
        wmsSettings.urlProperty().set(serverUrl);
        wmsSettings.enabledProperty().set(true);
        wmsSettings.nameProperty().bind(serverName, value -> serverName.get().toString());

        bindFields(pathProvider, serverUrl);
    }

    public WmsServer(WmsServerSettings settings, IPathProvider pathProvider) {
        this.wmsSettings = settings;
        this.serverUrl = wmsSettings.getUri();
        this.serverName.set(new LayerName(wmsSettings.nameProperty().get()));

        bindFields(pathProvider, wmsSettings.getUri());
    }

    private void bindFields(IPathProvider pathProvider, String uri) {
        wmsCapabilitiesWrapper = new WmsCapabilitiesWrapper(pathProvider, uri, serverName.get());
        serverName.bind(wmsCapabilitiesWrapper.serverNameProperty());
        wmsMaps.bindContent(wmsCapabilitiesWrapper.wmsMapsProperty());
        bindSubLayers();
    }

    public WmsServerSettings getWmsSettings() {
        return wmsSettings;
    }

    public AsyncListProperty<WmsMap> wmsMapsProperty() {
        return wmsMaps;
    }

    public String getUrl() {
        return serverUrl;
    }

    public AsyncObjectProperty<LayerName> serverNameProperty() {
        return serverName;
    }

    public void retrieveAndLoadCapabilities() throws Exception {
        wmsCapabilitiesWrapper.retrieveAndLoadCapabilities();
    }

    public void deleteFiles() {
        wmsCapabilitiesWrapper.deleteFiles();
    }

    /**
     * in this method a listener is added to the sublayers once the info about the layer is downloaded this listener
     * checks if the layer is in the settings if yes - bind the visibility, if not - create a new settings entry
     */
    private void bindSubLayers() {
        wmsMaps.addListener(
            (ListChangeListener<? super WmsMap>)
                c -> {
                    while (c.next()) {
                        for (WmsMap addedInfo : c.getAddedSubList()) {
                            String name = addedInfo.getTitle();
                            WmsLayerEnabledSettings enabledSettings = wmsSettings.get(name);
                            if (enabledSettings == null) {
                                WmsLayerEnabledSettings enabledSetting = new WmsLayerEnabledSettings();
                                enabledSetting.nameProperty().set(name);
                                enabledSetting.enabledProperty().bind(addedInfo.enabledProperty());

                                // to avoid a possible situation that on startup WmsServer is initialized from the
                                // settings which are locked
                                // and changing the setting can potentially cause a deadlock
                                Dispatcher.platform()
                                    .run(() -> wmsSettings.enabledImageryLayersProperty().add(enabledSetting));
                            } else {
                                addedInfo.enabledProperty().set(enabledSettings.enabledProperty().get());
                                enabledSettings.enabledProperty().bind(addedInfo.enabledProperty());
                            }
                        }

                        for (WmsMap removedInfo : c.getRemoved()) {
                            String name = removedInfo.getName();
                            WmsLayerEnabledSettings enabledSettings = wmsSettings.get(name);
                            if (enabledSettings != null) {
                                enabledSettings.enabledProperty().unbindBidirectional(removedInfo.enabledProperty());
                            }
                        }
                    }
                });
    }

    public AsyncBooleanProperty hasWarningProperty() {
        return hasWarning;
    }

    public AsyncStringProperty warningProperty() {
        return warning;
    }
}
