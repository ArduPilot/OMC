package AutoProxyService

import com.github.markusbernhardt.proxy.ProxySearch
import com.github.markusbernhardt.proxy.search.wpad.WpadProxySearchStrategy
import com.github.markusbernhardt.proxy.selector.misc.BufferedProxySelector
import com.github.markusbernhardt.proxy.util.ProxyException
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI

/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

fun makeAutoProxy(): ProxySelector? {
    val proxySearch = ProxySearch()

    // cache per host
    // todo: fix WPAD proxy JavaxPacScriptParser is very very slow on Win 10 JRE 10
    proxySearch.setPacCacheSettings(1000, (60 * 1000).toLong(), BufferedProxySelector.CacheScope.CACHE_SCOPE_HOST)
    proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT)
    proxySearch.addStrategy(ProxySearch.Strategy.FIREFOX)
    // proxySearch.addStrategy(ProxySearch.Strategy.BROWSER);
    var selector: ProxySelector? = null
    try {
        selector = proxySearch.getProxySelector()
    } catch (t: Throwable) {
        println("[InsightCLI]:\t Error while proxySearch.getProxySelector,$t")
    }

    if (selector == null) {
        try {
            selector = WpadProxySearchStrategy().getProxySelector()
        } catch (e: ProxyException) {
            println("[InsightCLI]:\t problems searching a proxy, $e")
        }

    }

    return selector
}


fun autoDiscoverAndSet() {
    var autoProxy = makeAutoProxy()
    val home = URI.create("http://www.msftncsi.com/ncsi.txt")
    val proxyList = autoProxy?.select(home)
    if (proxyList != null && !proxyList!!.isEmpty()) {
        for (item in proxyList!!) {
            val address = item.address()
            if (address is InetSocketAddress) {
                val host = (address as InetSocketAddress).hostName
                if (host != null && host.contains(".intel.com")) {
                    println("[InsightCLI]:\t using intel proxy settings because auto proxy found hostname=$host")
                    // we are injecting this manual proxy in order to be able to supply socks auto detected
                    // settings at least in intel network
                    System.setProperty("http.proxyHost", "http://proxy-chain.intel.com")
                    System.setProperty("http.proxyPort", "911")
                    System.setProperty("https.proxyHost", "http://proxy-chain.intel.com")
                    System.setProperty("https.proxyPort", "912")
                    System.setProperty("socksProxyHost", "proxy-us.intel.com");
                    System.setProperty("socksProxyPort", "1080");

                    System.setProperty("socksNonProxyHosts", "localhost|127.0.0.1")

                } else {

                    if (autoProxy != null) {
                        updateHttpSettingsFromProxy(autoProxy)
                        updateHttpsSettingsFromProxy(autoProxy)
                    }

                }
            }
        }
    }
}


private fun updateHttpsSettingsFromProxy(proxySelector: ProxySelector) {
    val protocol = "https"
    val home = URI.create("$protocol://www.msftncsi.com/ncsi.txt")

    val proxyList = proxySelector.select(home)
    if (proxyList != null && !proxyList.isEmpty()) {
        println(proxyList)
        for (item in proxyList) {
            println(item)
            if ("socks".equals(protocol, ignoreCase = true) || item.type() == java.net.Proxy.Type.SOCKS) {
                continue
            }

            val address = item.address()
            if (address is InetSocketAddress) {
                val host = address.hostName
                val port = Integer.toString(address.port)
                System.setProperty("https.proxyHost", host)
                System.setProperty("https.proxyPort", port)
                println("[InsightCLI]:\t Set https proxy: $host,$port")
            }
        }
    }
}

private fun updateHttpSettingsFromProxy(proxySelector: ProxySelector) {
    val protocol = "http"
    val home = URI.create("$protocol://www.msftncsi.com/ncsi.txt")

    val proxyList = proxySelector.select(home)
    if (proxyList != null && !proxyList.isEmpty()) {
        println(proxyList)
        for (item in proxyList) {
            if ("socks".equals(protocol, ignoreCase = true) || item.type() == java.net.Proxy.Type.SOCKS) {
                continue
            }

            val address = item.address()
            if (address is InetSocketAddress) {
                val host = address.hostName
                val port = Integer.toString(address.port)
                System.setProperty("http.proxyHost", host)
                System.setProperty("http.proxyPort", port)
                println("[InsightCLI]:\t Set http proxy: $host,$port")
            }
        }
    }
}


