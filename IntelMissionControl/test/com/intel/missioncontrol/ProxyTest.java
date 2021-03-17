/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.search.browser.ie.IEProxyConfig;
import com.github.markusbernhardt.proxy.search.browser.ie.IEProxySearchStrategy;
import com.github.markusbernhardt.proxy.search.wpad.WpadProxySearchStrategy;
import com.github.markusbernhardt.proxy.selector.pac.PacProxySelector;
import com.github.markusbernhardt.proxy.ui.ProxyTester;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.ProxyUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.objecthunter.exp4j.function.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jfree.util.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.manipulation.Filter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ProxyTest {

    public static class TestClient {
        final OkHttpClient client;

        TestClient() {
            this.client = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS).build();
        }

        public Response get(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            return client.newCall(request).execute();
        }

        public void testUrl(String url) throws IOException {
            Response response = get(url);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            System.out.println("got response: "+response);
            response.close();
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setupLogger() {
        Logger.getAnonymousLogger().addHandler(new ConsoleHandler());
        Logger.getGlobal().addHandler(new ConsoleHandler());
    }

    public static class TestProxySelector extends ProxySelector {
        List<Proxy> list = Collections.singletonList(Proxy.NO_PROXY);

        @Override
        public List<Proxy> select(URI uri) {
            return list;
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

        }
    }

    public static class ProxySelectorDelegator extends ProxySelector {
        final AtomicReference<ProxySelector> currentSelector;

        ProxySelectorDelegator(ProxySelector selector) {
            currentSelector = new AtomicReference<>(selector);
        }

        ProxySelectorDelegator() {
            this(ProxySelector.getDefault());
        }


        public void setCurrentSelector(ProxySelector selector) {
            currentSelector.lazySet(selector);
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



    @Test
    @Ignore
    public void testDefault() throws IOException {
        final TestClient client = new TestClient();
        String url = "http://example.com";
        client.testUrl(url);

        ProxySelector.setDefault(new TestProxySelector());
        client.testUrl(url);
    }

    @Test
    @Ignore
    public void testSetDefault() throws IOException {
        ProxySelector.setDefault(new TestProxySelector());
        final TestClient client = new TestClient();

        String url = "http://example.com";
        client.testUrl(url);

        client.testUrl(url);
    }

    @Test
    @Ignore
    public void getAutoProxy() throws InterruptedException {
        ProxyTester.main(new String[]{});
        Thread.sleep(1000 * 60);


    }

    @Test
    @Ignore
    public void testAuto() {
        Logger.getGlobal().setLevel(Level.ALL);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);

        Logger.getGlobal().addHandler(consoleHandler);
        ProxySearch proxySearch = new ProxySearch();
        proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
        //proxySearch.addStrategy(ProxySearch.Strategy.BROWSER);

        ProxySelector proxySelector = proxySearch.getProxySelector();
        assertNotNull(proxySelector);
        List<URI> uris = List.of(URI.create("http://www.example.com"),
        URI.create("https://www.example.com"),
        URI.create("ftp://www.example.com"));

        for (URI uri : uris) {
            List<Proxy> select = proxySelector.select(uri);
            System.out.println("proxySelector "+proxySelector+ " proxy "+select);

        }


    }

    public static class Wpad2Selector extends ProxySelector {
        final PacProxySelector pacProxySelector = ProxyUtil.buildPacSelectorForUrl("http://wpad/wpad.dat");
        final LoadingCache<URI, List<Proxy>> cache;


        public Wpad2Selector() {

            cache = CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<>() {
                @Override
                public List<Proxy> load(URI uri) throws Exception {
                    return pacProxySelector.select(uri);
                }
            });
        }

        @Override
        public List<Proxy> select(URI uri) {
            String scheme = uri.getScheme();

            if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
                try {
                    URI uri2 = new URI(uri.getScheme(), uri.getHost(), null, null);
                    return cache.get(uri2);
                } catch (URISyntaxException e) {
                    return null;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
//                cache.get()
            }
            return pacProxySelector.select(uri);
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            pacProxySelector.connectFailed(uri, sa, ioe);
        }
    }

    @Test
    @Ignore
    public void testFoo() throws URISyntaxException {
        URI uri = new URI("http://example.com");


        URI uri2 = new URI("https://www.intel.com");

        URI uri3 = new URI("https://webmail.intel.com/owa/?ae=Item&a=New&t=IPM.Note&cc=MTQuMy4zMTkuMixlbi1VUywxMixIVE1MLDAsMA==&pspid=_1532971221236_420369690");
        URI uri4 = new URI("HTTPS://www.intel.com/");

        URI edit = new URI(uri3.getScheme(), uri3.getHost(), null,null);
        String host = uri2.getHost();

    }

    @Test
    @Ignore
    public void testHostname() throws UnknownHostException {
        byte[] ipAddress = new byte[] {(byte)127, (byte)0, (byte)0, (byte)1 };
        InetAddress address = InetAddress.getByAddress(ipAddress);
        String hostnameCanonical = address.getCanonicalHostName();
        System.out.println(hostnameCanonical);
    }

    @Test
    @Ignore
    public void testSearch() throws IOException, ProxyException {
        ProxySelectorDelegator delegator = new ProxySelectorDelegator();
        ProxySelector.setDefault(delegator);

//        WpadProxySearchStrategy searchStrategy = new WpadProxySearchStrategy();
//        ProxySelector proxySelector = searchStrategy.getProxySelector();
        //PacProxySelector pacProxySelector = ProxyUtil.buildPacSelectorForUrl("http://wpad/wpad.dat");

        delegator.setCurrentSelector(new Wpad2Selector());

        final TestClient client = new TestClient();

        for (int i = 0; i < 100; i++) {
            final String url = "http://example.com";

            client.testUrl(url);

            client.testUrl("http://intel.com");


        }
    }

    @Test
    @Ignore
    public void testDefaultProxy() throws URISyntaxException {
        ProxySelector aDefault = ProxySelector.getDefault();

        String url = "http://example.com";
        List<Proxy> select = aDefault.select(new URI(url));
        int size = select.size();


    }

    @Test
    @Ignore
    public void testwindows() throws ProxyException {

        IEProxySearchStrategy serach = new IEProxySearchStrategy();
        IEProxyConfig ieProxyConfig = serach.readIEProxyConfig();

        ProxySelector proxySelector = serach.getProxySelector();

        List<URI> uris = List.of(URI.create("http://google.com"),
        URI.create("http://circuit.intel.com"));

        System.out.println("proxySelector" + proxySelector);
        for (URI uri : uris) {

            List<Proxy> select = proxySelector.select(uri);
            Proxy proxy = select.get(0);

            System.out.println("uri " + uri + " proxy="+proxy);

        }


    }

    @Test
    @Ignore
    public void testDelegator() throws IOException {
        ProxySelectorDelegator delegator = new ProxySelectorDelegator();
        ProxySelector.setDefault(delegator);

        final TestClient client = new TestClient();
        String url = "http://example.com";

        client.testUrl(url);

        // swap proxy Selector
        delegator.setCurrentSelector(new TestProxySelector());

        exception.expect(Exception.class);
        client.testUrl(url);


    }

    String script_text = "var a = 1; function foo() { return (++a) + poop('a'); }";



    @Test
    @Ignore
    public void javaTest() throws ScriptException, NoSuchMethodException, IOException {
//        ScriptEngineManager manager = new ScriptEngineManager();
//
//        ScriptEngine engine = manager.getEngineByName("js");
        final ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine(new String[] { "--no-java" });

//        String path = "C:\\Users\\Max\\tmp\\wpad.dat";
//    byte[] encoded = Files.readAllBytes(Paths.get(path));
//
//    String text =  new String(encoded, Charset.forName("utf-8"));

    String text = script_text;
        CompiledScript script = ((Compilable) engine).compile(text);

        java.util.function.Function<String, String> fun = (v) -> "hello!";

        //Bindings bindings = engine.createBindings();
        engine.put("poop", fun);

        Invocable inv = (Invocable) engine;

        script.eval();

        for (int i = 0; i < 10000; i++) {
            Object foo = inv.invokeFunction("foo");
        }
//        engine.eval("var File = Java.type('java.io.File'); File;");


        Object foo = inv.invokeFunction("foo");
        System.out.println("called foo, got "+foo);




    //script.eval(bindings); // switch to this line and feel the

//        System.out.println(System.currentTimeMillis() - dt);
    }

}
