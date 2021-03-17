/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.inject.Inject;
import org.asyncfx.beans.binding.BidirectionalValueConverter;
import org.asyncfx.beans.binding.ConversionBindings;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.InternetConnectivitySettings;
import com.intel.missioncontrol.settings.InternetConnectivitySettings.ProxyType;
import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

public class InternetConnectivityViewModel extends ViewModelBase {

    private final BooleanProperty useProxy = new SimpleBooleanProperty();
    private final BooleanProperty autoProxy = new SimpleBooleanProperty();
    private final UIAsyncObjectProperty<ProxyType> proxyType = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty httpHost = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty httpsHost = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty ftpHost = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty socksHost = new UIAsyncStringProperty(this);
    private final UIAsyncIntegerProperty httpPort = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty httpsPort = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty ftpPort = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty socksPort = new UIAsyncIntegerProperty(this);

    @Inject
    public InternetConnectivityViewModel(ISettingsManager settingsManager) {
        InternetConnectivitySettings settings = settingsManager.getSection(InternetConnectivitySettings.class);

        proxyType.bindBidirectional(settings.proxyTypeProperty());
        httpHost.bindBidirectional(settings.httpHostProperty());
        httpsHost.bindBidirectional(settings.httpsHostProperty());
        ftpHost.bindBidirectional(settings.ftpHostProperty());
        socksHost.bindBidirectional(settings.socksHostProperty());
        httpPort.bindBidirectional(settings.httpPortProperty());
        httpsPort.bindBidirectional(settings.httpsPortProperty());
        ftpPort.bindBidirectional(settings.ftpPortProperty());
        socksPort.bindBidirectional(settings.socksPortProperty());

        ConversionBindings.bindBidirectional(
            useProxy,
            proxyType,
            new BidirectionalValueConverter<ProxyType, Boolean>() {
                @Override
                public Boolean convert(ProxyType value) {
                    return value != ProxyType.NONE;
                }

                @Override
                public ProxyType convertBack(Boolean value) {
                    return value ? (autoProxy.get() ? ProxyType.AUTO : ProxyType.MANUAL) : ProxyType.NONE;
                }
            });

        ConversionBindings.bindBidirectional(
            autoProxy,
            proxyType,
            new BidirectionalValueConverter<ProxyType, Boolean>() {
                @Override
                public Boolean convert(ProxyType value) {
                    return value == ProxyType.AUTO;
                }

                @Override
                public ProxyType convertBack(Boolean value) {
                    return value ? ProxyType.AUTO : (useProxy.get() ? ProxyType.MANUAL : ProxyType.NONE);
                }
            });
    }

    public Property<Boolean> useProxyProperty() {
        return useProxy;
    }

    public Property<Boolean> useAutoProxyProperty() {
        return autoProxy;
    }

    public Property<String> httpHostProperty() {
        return httpHost;
    }

    public Property<Number> httpPortProperty() {
        return httpPort;
    }

    public Property<String> httpsHostProperty() {
        return httpsHost;
    }

    public Property<Number> httpsPortProperty() {
        return httpsPort;
    }

    public Property<String> ftpHostProperty() {
        return ftpHost;
    }

    public Property<Number> ftpPortProperty() {
        return ftpPort;
    }

    public Property<String> socksHostProperty() {
        return socksHost;
    }

    public Property<Number> socksPortProperty() {
        return socksPort;
    }

}
