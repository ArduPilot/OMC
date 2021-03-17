/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.gui.widgets.i18n.MCFrame;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;

public class RikolaBandPatternWindow extends MCFrame {

    private static final long serialVersionUID = -720235730287332043L;

    public static String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.RikolaBandPatternWindow";
    private static final ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);

    public RikolaBandPatternWindow(ITaggingAlgorithm alg, File file, boolean shouldBeVisible) {
        super("com/intel/missioncontrol/icons/icon_chart.svg");

        XYSeries serImg = new XYSeries(languageHelper.getString(KEY + ".ImageImg"), false, true);
        MinMaxPair minMaxX = new MinMaxPair();

        int lastImgID = -1;
        double lastImgFirstTime = -1;

        int i = 0;

        int maxBandNo = -1;
        int imgCnt = 0;

        for (CPhotoLogLine line : alg.getLogsAll()) {
            if (lastImgID != line.imageNumber) {
                lastImgID = line.imageNumber;
                lastImgFirstTime = line.getTimestamp();
                i = 0;
                imgCnt++;
            }

            i++;
            if (i > maxBandNo) {
                maxBandNo = i;
            }

            double dt = line.getTimestamp() - lastImgFirstTime;
            if (dt >= 2) {
                continue; // initial calibration phase
            }

            serImg.add(dt, 0);
            serImg.add(dt, i);
            serImg.add(dt, 0);
            minMaxX.update(dt);
        }

        XYSeriesCollection xyDatasetImg = new XYSeriesCollection();

        JFreeChart chart =
            ChartFactory.createXYLineChart(
                languageHelper.getString(
                    KEY + ".chartTitle",
                    alg.getLogsAll().size(),
                    maxBandNo,
                    imgCnt,
                    maxBandNo * imgCnt,
                    (double)(100 * alg.getLogsAll().size()) / (maxBandNo * imgCnt)), // Title = Orientation Log
                languageHelper.getString(KEY + ".xAxis") + "  dtMax: " + minMaxX.max + "s", // Time [s]
                languageHelper.getString(KEY + ".yAxis"), // Angle [°]
                xyDatasetImg, // Dataset
                PlotOrientation.VERTICAL,
                true // Show legend
                ,
                true,
                false // no tooltips, no urls
                );
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.getDomainAxis(); // .setAutoRangeMinimumSize(5);
        ((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);

        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        XYItemRenderer renderer1 = new XYLineAndShapeRenderer();
        Shape cross = ShapeUtilities.createDiagonalCross(3, 0.5f);
        renderer1.setSeriesShape(0, cross);
        renderer1.setSeriesStroke(
            0,
            new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {2.0f, 2.0f}, 0.0f));
        plot.setRenderer(0, renderer1);
        chart.setNotify(false);

        chart.setAntiAlias(false); // anti-alias is too slow
        ChartPanel chartPan =
            new ChartPanel(
                chart,
                140, // ChartPanel.DEFAULT_WIDTH,
                90, // ChartPanel.DEFAULT_HEIGHT,
                140, // ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH,
                90, // ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
                ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
                ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
                ChartPanel.DEFAULT_BUFFER_USED,
                true,
                true, // save
                true, // print
                true, // zoom
                true); // tooltips

        xyDatasetImg.addSeries(serImg);

        NumberAxis domain = (NumberAxis)plot.getDomainAxis();
        domain.setRange(0.00, 2);

        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(chartPan, BorderLayout.CENTER);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setKey(KEY);
        try {
            ChartUtilities.saveChartAsPNG(file, chart, 2000, 1000);
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "could not store chart", e);
        }

        if (shouldBeVisible) {
            setVisible(true);
        } else {
            dispose();
        }
    }

}
