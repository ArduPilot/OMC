/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking.proxy;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Like {@link ProxySelector} but maintains a single meta ProxySelector. This allows networking code to use the default
 * proxy selector (e.g. by calling {@link ProxySelector#getDefault()}) and automatically update
 */
class ProxySelectorDelegator {

    private static final Logger LOG = Logger.getLogger(ProxySelectorDelegator.class.getSimpleName());

    private static final class LazyHolder {
        static final ProxySelectorDelegator INSTANCE = new ProxySelectorDelegator();
    }

    /** also installs proxy holder */
    public static ProxySelectorDelegator getInstance() {
        return LazyHolder.INSTANCE;
    }

    /** installs proxy holder, should be called very early */
    static void install() {
        getInstance();
    }

    private static boolean installed = false;

    private final Delegator delegator;

    private ProxySelectorDelegator() {
        LOG.log(Level.INFO, "Installing " + ProxySelectorDelegator.class.getName());
        ProxySelector.setDefault(delegator = new Delegator());
        installed = true;
    }

    private static void checkInstall() {
        if (!installed)
            throw new IllegalStateException("ProxySelectorDelegator: not installed, you forgot to call install()");
    }

    /** @param selector or <code>null</code> to use system default */
    public static void setDefaultLazy(ProxySelector selector) {
        checkInstall();
        getInstance().delegator.currentSelector.lazySet(selector);
    }

    /** @param selector or <code>null</code> to use system default */
    // should be called from the background thread otherwise probably use the lazy version
    public static void setDefault(ProxySelector selector) {
        checkInstall();
        getInstance().delegator.currentSelector.set(selector);
    }

    public static ProxySelector getDefault() {
        checkInstall();
        return getInstance().delegator.currentSelector.get();
    }

    /** Delegates to whatever ProxySelector is set to currentSelector */
    private static final class Delegator extends ProxySelector {
        private final AtomicReference<ProxySelector> currentSelector;

        Delegator(ProxySelector selector) {
            currentSelector = new AtomicReference<>(selector);
        }

        Delegator() {
            this(new DirectSelector());
        }

        @Override
        public List<Proxy> select(URI uri) {
            return currentSelector.get().select(uri);
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            currentSelector.get().connectFailed(uri, sa, ioe);
        }
    }

}
