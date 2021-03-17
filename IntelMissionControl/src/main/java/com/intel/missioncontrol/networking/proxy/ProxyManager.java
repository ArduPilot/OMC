/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.networking.proxy;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.search.wpad.WpadProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.misc.BufferedProxySelector;
import com.github.markusbernhardt.proxy.selector.whitelist.DefaultWhiteListParser;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.google.inject.Inject;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.settings.InternetConnectivitySettings;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.concurrent.Strand;
import org.slf4j.LoggerFactory;

/**
 * Manage proxies in a way that is transparent to most networking code.
 *
 * <p>Most networking code grabs a {@link ProxySelector} reference once via {@link ProxySelector#getDefault()} upon
 * initialization. To avoid having to reinitialize all networking code whenever proxy settings change, we install a
 * single meta ProxySelector (see {@link ProxySelectorDelegator}) which acts as a facade and forwards all requests (i.e.
 * {@link ProxySelector#select(URI) select()} to a delegate ProxySelector. The delegate can then be swapped at runtime.
 */
public class ProxyManager {
    private static final Logger LOG = Logger.getLogger(ProxyManager.class.getSimpleName());

    private final InternetConnectivitySettings settings;
    private final INetworkInformation networkInformation;
    private final Strand strand = new Strand();
    private final AsyncBooleanProperty proxyInitialized =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    /**
     * Install ProxySelectorDelegator into ProxySelector.setDefault() so that network code can automagically continue
     * working after proxy settings are changed
     *
     * <p><b>Must be called early in the Application lifecycle, before network clients are crated!</b>
     */
    public static void install() {
        ProxySelectorDelegator.install();
    }

    private final InvalidationListener proxySettingsInvalidated = observable -> initializationProcedure();

    @Inject
    public ProxyManager(
            InternetConnectivitySettings internetConnectivitySettings, INetworkInformation networkInformation) {
        this.networkInformation = networkInformation;
        this.settings = internetConnectivitySettings;
        this.settings.proxyTypeProperty().addListener(proxySettingsInvalidated);
        this.settings.httpHostProperty().addListener(proxySettingsInvalidated);
        this.settings.httpsHostProperty().addListener(proxySettingsInvalidated);
        this.settings.ftpHostProperty().addListener(proxySettingsInvalidated);
        this.settings.socksHostProperty().addListener(proxySettingsInvalidated);
        this.settings.httpPortProperty().addListener(proxySettingsInvalidated);
        this.settings.httpsPortProperty().addListener(proxySettingsInvalidated);
        this.settings.ftpPortProperty().addListener(proxySettingsInvalidated);
        this.settings.socksPortProperty().addListener(proxySettingsInvalidated);

        initializationProcedure();

        // in case if IMC started without internet and it appeared after
        networkInformation
            .networkAvailableProperty()
            .addListener(
                (object, oldVal, newVal) -> {
                    if (newVal) {
                        LoggerFactory.getLogger(ProxyManager.class)
                            .info("Reinitializing proxy after the network status has changed");
                        initializationProcedure();
                    }
                });
    }

    private void initializationProcedure() {
        strand.runLater(
            () -> {
                ProxySelector proxySelector = getProxySelectorForSettings();
                updateProxySelector(proxySelector);
                updateSettingsFromProxy(proxySelector);
                if (networkInformation.isNetworkAvailable()) {
                    proxyInitialized.set(true);
                }
            });
    }

    private void updateSettingsFromProxy(ProxySelector proxySelector) {
        updateHttpSettingsFromProxy(proxySelector);
        updateHttpsSettingsFromProxy(proxySelector);
        updateFtpSettingsFromProxy(proxySelector);
    }

    private void updateFtpSettingsFromProxy(ProxySelector proxySelector) {
        String protocol = "ftp";
        URI home = URI.create(protocol + "://www.msftncsi.com/ncsi.txt");

        var proxyList = proxySelector.select(home);
        if (proxyList != null && !proxyList.isEmpty()) {
            for (java.net.Proxy item : proxyList) {
                if ("socks".equalsIgnoreCase(protocol) || item.type() == java.net.Proxy.Type.SOCKS) {
                    continue;
                }

                SocketAddress address = item.address();
                if (address instanceof InetSocketAddress) {
                    String host = ((InetSocketAddress)address).getHostName();
                    String port = Integer.toString(((InetSocketAddress)address).getPort());
                    settings.ftpHostProperty().set(host);
                    settings.ftpPortProperty().set(Integer.parseInt(port));
                }
            }
        }
    }

    private void updateHttpsSettingsFromProxy(ProxySelector proxySelector) {
        String protocol = "https";
        URI home = URI.create(protocol + "://www.msftncsi.com/ncsi.txt");

        var proxyList = proxySelector.select(home);
        if (proxyList != null && !proxyList.isEmpty()) {
            System.out.println(proxyList);
            for (java.net.Proxy item : proxyList) {
                System.out.println(item);
                if ("socks".equalsIgnoreCase(protocol) || item.type() == java.net.Proxy.Type.SOCKS) {
                    continue;
                }

                SocketAddress address = item.address();
                if (address instanceof InetSocketAddress) {
                    String host = ((InetSocketAddress)address).getHostName();
                    String port = Integer.toString(((InetSocketAddress)address).getPort());
                    settings.httpsHostProperty().set(host);
                    settings.httpsPortProperty().set(Integer.parseInt(port));
                }
            }
        }
    }

    private void updateHttpSettingsFromProxy(ProxySelector proxySelector) {
        String protocol = "http";
        URI home = URI.create(protocol + "://www.msftncsi.com/ncsi.txt");

        var proxyList = proxySelector.select(home);
        if (proxyList != null && !proxyList.isEmpty()) {
            System.out.println(proxyList);
            for (java.net.Proxy item : proxyList) {
                // System.out.println(item);
                if ("socks".equalsIgnoreCase(protocol) || item.type() == java.net.Proxy.Type.SOCKS) {
                    continue;
                }

                SocketAddress address = item.address();
                if (address instanceof InetSocketAddress) {
                    String host = ((InetSocketAddress)address).getHostName();
                    String port = Integer.toString(((InetSocketAddress)address).getPort());
                    settings.httpHostProperty().set(host);
                    settings.httpPortProperty().set(Integer.parseInt(port));
                }
            }
        }
    }

    ProxySelector getProxySelectorForSettings() {
        final String whitelist = "localhost, 127.0.0.1, 192.168.0.1/16, 172.16.0.1/12., 10.0.0.0/8, *.sc.intel.com";

        switch (settings.proxyTypeProperty().get()) {
        case MANUAL:
            return ManualProxySelector.createManualProxySelector(
                settings, new DefaultWhiteListParser().parseWhiteList(whitelist));
        case AUTO:
            var autoProxy = makeAutoProxy();

            if (autoProxy == null) {
                // makeAutoProxy may return null if auto proxy search does not succeed
                // if the search fails, we shall return a DirectSelector. We could think about
                // defaulting to the manual proxy instead.
                return new DirectSelector();
            }

            URI home = URI.create("http://www.msftncsi.com/ncsi.txt");
            var proxyList = autoProxy.select(home);
            if (proxyList != null && !proxyList.isEmpty()) {
                for (java.net.Proxy item : proxyList) {
                    SocketAddress address = item.address();
                    if (address instanceof InetSocketAddress) {
                        String host = ((InetSocketAddress)address).getHostName();
                        if (host != null && host.contains(".intel.com")) {
                            LOG.warning("SETTING INTEL INTRANET SPECIFIC SETTINGS because hostname=" + host);
                            // we are injecting this manual proxy in order to be able to supply socks auto detected
                            // settings at least in intel network
                            settings.httpHostProperty().setValue("proxy-chain.intel.com");
                            settings.httpPortProperty().setValue(911);
                            settings.httpsHostProperty().setValue("proxy-chain.intel.com");
                            settings.httpsPortProperty().setValue(912);
                            settings.ftpHostProperty().setValue("proxy-chain.intel.com");
                            settings.ftpPortProperty().setValue(911);
                            settings.socksHostProperty().setValue("proxy-us.intel.com");
                            settings.socksPortProperty().setValue(1080);
                            // Don't set SOCKS proxy, because we probably don't want random TCP connections going
                            // through proxy, (e.g. vehicle connections...)
                            System.setProperty("socksNonProxyHosts", "localhost|127.0.0.1");

                            return ManualProxySelector.createManualProxySelector(
                                settings, new DefaultWhiteListParser().parseWhiteList(whitelist));
                        }
                    }
                }
            }

            return autoProxy;

        default:
            {
                // might be needed, but probably not, if seen after 2018/09/30, feel free to remove
                //            System.clearProperty("http.proxyHost");
                //            System.clearProperty("http.proxyPort");
                //            System.clearProperty("https.proxyHost");
                //            System.clearProperty("https.proxyPort");
                //            System.clearProperty("ftp.proxyHost");
                //            System.clearProperty("ftp.proxyPort");
                //            System.clearProperty("socksProxyHost");
                //            System.clearProperty("socksProxyPort");
                return new DirectSelector();
            }
        }
    }

    private void updateProxySelector(ProxySelector selector) {
        LOG.info("Updating ProxySelector: " + selector);
        ProxySelectorDelegator.setDefault(selector);
        networkInformation.invalidate();
    }

    private ProxySelector makeAutoProxy() {
        ProxySearch proxySearch = new ProxySearch();

        // cache per host
        // todo: fix WPAD proxy JavaxPacScriptParser is very very slow on Win 10 JRE 10
        proxySearch.setPacCacheSettings(1000, 60 * 1000, BufferedProxySelector.CacheScope.CACHE_SCOPE_HOST);
        proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
        proxySearch.addStrategy(ProxySearch.Strategy.FIREFOX);
        // proxySearch.addStrategy(ProxySearch.Strategy.BROWSER);
        ProxySelector selector = null;
        try {
            selector = proxySearch.getProxySelector();
        } catch (Throwable t) {
            LOG.severe("Error while proxySearch.getProxySelector," + t);
        }

        if (selector == null) {
            try {
                selector = new WpadProxySearchStrategy().getProxySelector();
            } catch (ProxyException e) {
                LOG.log(Level.WARNING, "problems searching a proxy", e);
            }
        }

        return selector;
    }

    public ReadOnlyAsyncBooleanProperty proxyInitializedProperty() {
        return proxyInitialized;
    }
}
