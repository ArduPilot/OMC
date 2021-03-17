/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import javafx.beans.binding.Bindings;

@SettingsMetadata(section = "internetSettings")
public class InternetConnectivitySettings implements ISettings {

    public enum ProxyType {
        NONE,
        MANUAL,
        AUTO
    }

    private static final String DEFAULT_ALL_PROXY_HOST = "";
    private static final int DEFAULT_ALL_PROXY_PORT = 0;

    private final AsyncObjectProperty<ProxyType> proxyType =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<ProxyType>().initialValue(ProxyType.AUTO).create());

    private final AsyncIntegerProperty httpPort =
        new SimpleAsyncIntegerProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(DEFAULT_ALL_PROXY_PORT).create());

    private final AsyncIntegerProperty httpsPort =
        new SimpleAsyncIntegerProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(DEFAULT_ALL_PROXY_PORT).create());

    private final AsyncIntegerProperty ftpPort =
        new SimpleAsyncIntegerProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(DEFAULT_ALL_PROXY_PORT).create());

    private final AsyncIntegerProperty socksPort =
        new SimpleAsyncIntegerProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(DEFAULT_ALL_PROXY_PORT).create());

    private final AsyncStringProperty httpHost =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(DEFAULT_ALL_PROXY_HOST).create());

    private final AsyncStringProperty httpsHost =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(DEFAULT_ALL_PROXY_HOST).create());

    private final AsyncStringProperty ftpHost =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(DEFAULT_ALL_PROXY_HOST).create());

    private final AsyncStringProperty socksHost =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(DEFAULT_ALL_PROXY_HOST).create());

    private final transient AsyncBooleanProperty isIntelProxy =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    public InternetConnectivitySettings() {
        isIntelProxy.bind(
            Bindings.createBooleanBinding(
                () -> {
                    String host = httpHost.get();
                    return proxyType.get() != ProxyType.NONE
                        && host != null
                        && host.toLowerCase().contains("intel.com");
                },
                httpHost,
                proxyType));
    }

    public AsyncObjectProperty<ProxyType> proxyTypeProperty() {
        return proxyType;
    }

    public AsyncStringProperty httpHostProperty() {
        return httpHost;
    }

    public AsyncIntegerProperty httpPortProperty() {
        return httpPort;
    }

    public AsyncStringProperty httpsHostProperty() {
        return httpsHost;
    }

    public AsyncIntegerProperty httpsPortProperty() {
        return httpsPort;
    }

    public AsyncStringProperty ftpHostProperty() {
        return ftpHost;
    }

    public AsyncIntegerProperty ftpPortProperty() {
        return ftpPort;
    }

    public AsyncStringProperty socksHostProperty() {
        return socksHost;
    }

    public AsyncIntegerProperty socksPortProperty() {
        return socksPort;
    }

    public AsyncBooleanProperty isIntelProxyProperty() {
        return isIntelProxy;
    }
}
