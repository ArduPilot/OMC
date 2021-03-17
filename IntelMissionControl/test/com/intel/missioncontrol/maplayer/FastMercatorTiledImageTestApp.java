/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.maplayer;

import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.worldwind.layers.MapboxLayer;
import com.intel.missioncontrol.map.worldwind.layers.mercator.FancyDownloadQueue;
import com.intel.missioncontrol.map.worldwind.layers.mercator.FastMercatorTiledImageLayer;
import com.intel.missioncontrol.networking.NetworkInterceptor;
import com.intel.missioncontrol.networking.OkHttpUtil;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class FastMercatorTiledImageTestApp extends ApplicationTemplate {

    public static File getTestMapTileCacheDir() {
        return new File(System.getProperty("java.io.tmpdir"), "mercator-map-test");
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame {
        FastMercatorTiledImageLayer mapboxOrtho2;

        public AppFrame() {
            super(true, true, true);

            this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);
            // disable other layers
            LayerList layers = getWwd().getModel().getLayers();
            for (Layer l : layers) {
                if (l instanceof TiledImageLayer) l.setEnabled(false);
            }

            // temp directory
            File baseDir = getTestMapTileCacheDir();
            long size = 200 * 1024 * 1024;

            System.err.printf(">>> using cache directory: %s\n", baseDir.getPath());

            FancyDownloadQueue queue = new FancyDownloadQueue(true, 3);
            FancyDownloadQueue.DebugLayer debug = queue.getDebugLayer();
            //debug.setPickEnabled(false);


            MapboxLayer.Options options = new MapboxLayer.Options(baseDir, null);
            options.tileQueue = queue;
            options.maxRequests = 8;

            options.networkInterceptor = new NetworkInterceptor() {

            };

//                new OkHttpUtil.NetworkActivityListener() {
//                    @Override
//                    public void onNetworkActivityStarted(Object tag) {
//                        System.out.println("MapBoxOrth2: downloading");
//                    }
//
//                    @Override
//                    public void onNetworkActivityStopped(Object tag) {
//                        System.out.println("MapBoxOrth2: idle");
//                    }
//                });

            FastMercatorTiledImageLayer mapboxOrtho21 = new MapboxLayer(MapboxLayer.MapType.HYBRID, options, "TODO my mapbox map id goes here", "TODO my mapbox access token goes here");
            mapboxOrtho2 = mapboxOrtho21;
            mapboxOrtho2.setName("Fast MapboxOrtho");

            // test drop cache
            // mapboxOrtho2.dropOldCache(Duration.ofDays(3));

            //            MapboxOrtho mapboxOrtho = new MapboxOrtho(MapboxOrtho.defMapID_pureSat,
            // MapboxOrtho.defAccessToken);
            //            mapboxOrtho.setEnabled(false);
            //            mapboxOrtho.setName("Old MapboxOrtho");

            LatLonGraticuleLayer latLonGraticuleLayer = new LatLonGraticuleLayer();
            latLonGraticuleLayer.setEnabled(false);

            insertBeforeCompass(getWwd(), latLonGraticuleLayer);
            //            insertBeforeCompass(getWwd(), mapboxOrtho);
            insertBeforeCompass(getWwd(), mapboxOrtho2);
            insertBeforeCompass(getWwd(), debug);
        }

        protected JPanel makeControlPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 15, 10), new EtchedBorder()));

            JCheckBox modeSwitch =
                new JCheckBox(
                    new AbstractAction(" Online") {
                        public void actionPerformed(ActionEvent actionEvent) {
                            // Get the current status
                            boolean offline = WorldWind.getNetworkStatus().isOfflineMode();

                            // Change it to its opposite
                            offline = !offline;
                            WorldWind.getNetworkStatus().setOfflineMode(offline);

                            // Cause data retrieval to resume if now online
                            if (!offline) getWwd().redraw();
                        }
                    });
            modeSwitch.setSelected(true); // WW starts out online
            panel.add(modeSwitch, BorderLayout.NORTH);

            JButton cacheDumpButton =
                new JButton(
                    new AbstractAction("Drop cache") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Object[] possibilities = {"10 second", "1 minute", "10 minutes", "10 hours", "B.K."};
                            Duration[] times = {
                                Duration.ofSeconds(10),
                                Duration.ofMinutes(1),
                                Duration.ofMinutes(10),
                                Duration.ofHours(10),
                                Duration.ofDays(10000)
                            };
                            String s =
                                (String)
                                    JOptionPane.showInputDialog(
                                        AppFrame.this,
                                        "Drop all cache entries older than...\n",
                                        "Dump MapBox cache",
                                        JOptionPane.PLAIN_MESSAGE,
                                        null,
                                        possibilities,
                                        "ham");

                            // If a string was returned, say so.
                            //                    if ((s != null) && (s.length() > 0)) {
                            //                        setLabel("Green eggs and... " + s + "!");
                            //                        return;
                            //                    }
                            //
                            //// If you're here, the return value was null/empty.
                            //                    setLabel("Come on, finish the sentence!");
                            ////                    mapboxOrtho2.dropOldCache();
                        }
                    });
            panel.add(cacheDumpButton, BorderLayout.SOUTH);

            return panel;
        }
    }

    public static boolean proxyWorking() {
        URL url = null;
        try {
            url = new URL("https://example.com");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");

            InputStream is = con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }

            rd.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        // if (!proxyWorking()) return;
        ApplicationTemplate.start("MapboxOrthoTestApp", FastMercatorTiledImageTestApp.AppFrame.class);
    }
}
