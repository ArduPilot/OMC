/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import com.intel.missioncontrol.airmap.layer.AirMapTileLoader2;
import com.intel.missioncontrol.airmap.layer.TileMapper;
import com.intel.missioncontrol.airmap.network.AirMapConfig2;
import com.intel.missioncontrol.airspace.LayerConfigurator;
import com.intel.missioncontrol.airspace.render.TiledRenderableLayer;
import com.intel.missioncontrol.networking.OkHttpUtil;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Test TiledRenderableLayer and stuff
 *
 * To test with Fiddler (man-in-the-middle) set something like this into Java VM options
 *
 * <pre>
 * -Dhttp.proxyHost=127.0.0.1
 * -Dhttp.proxyPort=8888
 * -Dhttps.proxyHost=127.0.0.1
 * -Dhttps.proxyPort=8888
 * -Djavax.net.ssl.trustStore=C:\Users\Max\Dev\Java\Fiddler\FiddlerKeystore
 * -Djavax.net.ssl.trustStorePassword=password
 * </pre>
 *
 */
public class AirspaceTestApp extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {
        AirMapTileLoader2 tileLoader2;

        public AppFrame() {
            super(true, true, true);
//            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

            AirMapConfig2 config2 = new AirMapConfig2(LayerConfigurator.getDefaultAirmapKey());
            config2.cacheSize = 20 * 1024 * 1024;
            config2.cacheDir = TestUtils.getTempDir("airmap-test-cache");
            config2.networkListener = new OkHttpUtil.NetworkActivityListener() {
                @Override
                public void onNetworkActivityStarted(Object tag) {
                    System.out.println("MapBoxOrth2: downloading");
                }

                @Override
                public void onNetworkActivityStopped(Object tag) {
                    System.out.println("MapBoxOrth2: idle");
                }
            };

            AirMap.init(config2);
            System.out.printf(">>>> AirMap test cache dir: %s\n", config2.cacheDir);


            LevelSet tileLevels = LayerConfigurator.createDefaultLevelSet();
            TileMapper tileMapper = new TileMapper(tileLevels);
            tileLoader2 = new AirMapTileLoader2();

            //
            // DEBUG TILE LOADER
            //
            tileLoader2.setDrawDebug(true);

            TiledRenderableLayer airmapTileLayer = new TiledRenderableLayer(tileLoader2, tileMapper);
            airmapTileLayer.setName("AirMap Tiles");

            insertBeforePlacenames(this.getWwd(), airmapTileLayer);

            this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);
        }

        protected JPanel makeControlPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 15, 10), new EtchedBorder()));

            JCheckBox modeSwitch = new JCheckBox(new AbstractAction(" Online") {
                public void actionPerformed(ActionEvent actionEvent) {
                    // Get the current status
                    boolean offline = WorldWind.getNetworkStatus().isOfflineMode();

                    // Change it to its opposite
                    offline = !offline;
                    WorldWind.getNetworkStatus().setOfflineMode(offline);

                    // Cause data retrieval to resume if now online
                    if (!offline)
                        getWwd().redraw();
                }
            });
            modeSwitch.setSelected(true); // WW starts out online
            panel.add(modeSwitch, BorderLayout.NORTH);


            // debug render
            final JCheckBox debugSwitch = new JCheckBox(" Draw Debug", true);
            debugSwitch.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    tileLoader2.setDrawDebug(debugSwitch.isSelected());
                    getWwd().redraw();
                }
            });
            debugSwitch.setSelected(true); // WW starts out online
            panel.add(debugSwitch, BorderLayout.SOUTH);

            return panel;
        }


    }

    final static Sector BayArea = Sector.fromDegrees(
            37.233442685843016, 37.59317082004017,
            -121.72677386705263, -122.17967594289738);

    public static void main(String[] args) {
        TestUtils.fixWWJScaling();
        TestUtils.setupInitialZoom(BayArea);

        ApplicationTemplate.start("Airspace Loading Test", AirspaceTestApp.AppFrame.class);
    }
}
