/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.config;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;

public class CloudSync {

    private static class ProxyConfig {
        private final AsyncStringProperty httpProxyProperty = new SimpleAsyncStringProperty(this);
        private final AsyncIntegerProperty httpProxyPortProperty = new SimpleAsyncIntegerProperty(this);

        private final AsyncStringProperty httpsProxyProperty = new SimpleAsyncStringProperty(this);
        private final AsyncIntegerProperty httpsProxyPortProperty = new SimpleAsyncIntegerProperty(this);

        private final AsyncStringProperty socksProxyProperty = new SimpleAsyncStringProperty(this);
        private final AsyncIntegerProperty socksProxyPortProperty = new SimpleAsyncIntegerProperty(this);

        public void setHttpProxy(String host, int port) {
            httpProxyProperty.setValue(host);
            httpProxyPortProperty.setValue(port);
        }

        public void setHttpsProxy(String host, int port) {
            httpsProxyProperty.setValue(host);
            httpsProxyPortProperty.setValue(port);
        }

        public void setSocksProxy(String host, int port) {
            socksProxyProperty.setValue(host);
            socksProxyPortProperty.setValue(port);
        }
    }

    public static class InsightConfig {

        // TODO several credentials ??
        private final AsyncStringProperty insightUsername =
            new SimpleAsyncStringProperty(this, new PropertyMetadata.Builder<String>().initialValue("test.user1-c1@intel.com").create());
        //.initialValue("admin1").create());
        private final AsyncStringProperty insightPassword =
            new SimpleAsyncStringProperty(
                this, new PropertyMetadata.Builder<String>().initialValue("password").create());
        private final AsyncStringProperty insightHost =
            new SimpleAsyncStringProperty(
                this, new PropertyMetadata.Builder<String>().initialValue("https://imc-insight-dev.tm.intel.com/").create());
                //.initialValue("http://192.168.23.129:81/").create());

        private transient AsyncBooleanProperty insightLoggedIn =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

        public AsyncStringProperty insightPasswordProperty() {
            return insightPassword;
        }

        public AsyncStringProperty insightUsernameProperty() {
            return insightUsername;
        }

        public String getInsightPassword() {
            return insightPassword.get();
        }

        public String getInsightUsername() {
            return insightUsername.get();
        }

        public AsyncStringProperty insightHostProperty() {
            return insightHost;
        }

        public String getInsightHost() {
            return insightHost.get();
        }

        public AsyncBooleanProperty insightLoggedInProperty() {
            return insightLoggedIn;
        }

        public Boolean getInsightLoggedIn() {
            return insightLoggedIn.get();
        }

    }

    private static ProxyConfig proxyConfig = new ProxyConfig();
    private static InsightConfig insightConfig = new InsightConfig();

    public static InsightConfig getInsightConfig() {
        return insightConfig;
    }

    public static ProxyConfig getProxyConfig() {
        return proxyConfig;
    }
}
