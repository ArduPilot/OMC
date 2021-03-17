/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.insight;

import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
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

    private static ProxyConfig proxyConfig = new ProxyConfig();

    public static ProxyConfig getProxyConfig() {
        return proxyConfig;
    }
}
