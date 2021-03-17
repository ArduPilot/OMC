package com.intel.dronekitsample;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.util.StatisticsPanel;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import java.awt.*;

public class DroneTestAppGlobe extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private Dimension canvasSize = new Dimension(1200, 760);
        private GlobeFxApp javaFxRootManager;

        public AppFrame()
        {
            super(true, true, false);
            setSize(canvasSize);
        }

        //@Override
        protected void initialize(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel) {
            // Create the WorldWindow.
            this.wwjPanel = this.createAppPanel(this.canvasSize, includeStatusBar);
            this.wwjPanel.setPreferredSize(canvasSize);

            final JFXPanel leftPanel = new JFXPanel();
            final JFXPanel topPanel = new JFXPanel();

            // Put the pieces together.
            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);

            // add left panel
            this.getContentPane().add(leftPanel, BorderLayout.WEST);

            // add top panel
            this.getContentPane().add(topPanel, BorderLayout.NORTH);

            javaFxRootManager = new GlobeFxApp();
            GlobeFxApp.Params params = new GlobeFxApp.Params();
            params.leftPanel = leftPanel;
            params.topPanel = topPanel;
            params.wwd = getWwd();
            params.appFrame = AppFrame.this;
            javaFxRootManager.init(params);

            if (includeStatsPanel || System.getProperty("gov.nasa.worldwind.showStatistics") != null)
            {
                this.statsPanel = new StatisticsPanel(this.wwjPanel.getWwd(), new Dimension(250, canvasSize.height));
                this.getContentPane().add(this.statsPanel, BorderLayout.EAST);
            }

            // Create and install the view controls layer and register a controller for it with the WorldWindow.
            ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
            insertBeforeCompass(getWwd(), viewControlsLayer);
            this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), viewControlsLayer));

            // Register a rendering exception listener that's notified when exceptions occur during rendering.
            this.wwjPanel.getWwd().addRenderingExceptionListener(new RenderingExceptionListener()
            {
                public void exceptionThrown(Throwable t)
                {
                    if (t instanceof WWAbsentRequirementException)
                    {
                        String message = "Computer does not meet minimum graphics requirements.\n";
                        message += "Please install up-to-date graphics driver and try again.\n";
                        message += "Reason: " + t.getMessage() + "\n";
                        message += "This program will end when you press OK.";

                        JOptionPane.showMessageDialog(AppFrame.this, message, "Unable to Start Program",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(-1);
                    }
                }
            });

            // Search the layer list for layers that are also select listeners and register them with the World
            // Window. This enables interactive layers to be included without specific knowledge of them here.
            for (Layer layer : this.wwjPanel.getWwd().getModel().getLayers())
            {
                if (layer instanceof SelectListener)
                {
                    this.getWwd().addSelectListener((SelectListener) layer);
                }
            }

            this.pack();

            // Center the application on the screen.
            WWUtil.alignComponent(null, this, AVKey.CENTER);
            this.setResizable(true);
        }

    }

    public static void main(String[] args) {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        ApplicationTemplate.start("Drone Test App", AppFrame.class);
    }
}
