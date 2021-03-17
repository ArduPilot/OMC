/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.gui.widgets.i18n.MCFrame;
import eu.mavinci.desktop.helper.MathHelper;
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
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;

public class TaggingSyncVisWindow extends MCFrame {

    private static final long serialVersionUID = -720235730287332043L;

    public static String KEY = "eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingSyncVisWindow";
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    public TaggingSyncVisWindow(
            ITaggingAlgorithm alg,
            File file,
            boolean shouldBeVisible,
            IGenericCameraConfiguration cameraConfiguration,
            IHardwareConfiguration hardwareConfiguration) { // hw descr
        super("com/intel/missioncontrol/icons/icon_chart.svg");

        XYSeries serDiff = new XYSeries(languageHelper.getString(KEY + ".timeDiff"), false, true);
        XYSeries serLossLog = new XYSeries(languageHelper.getString(KEY + ".ImageLossLog"), false, true);
        XYSeries serLossImg = new XYSeries(languageHelper.getString(KEY + ".ImageLossImg"), false, true);
        XYSeries serCalibImg = new XYSeries(languageHelper.getString(KEY + ".CalibImage"), false, true);
        XYSeries serHeading = new XYSeries(languageHelper.getString(KEY + ".GPS-heading"), false, true);
        MinMaxPair minMaxY = new MinMaxPair();
        MinMaxPair minMaxX = new MinMaxPair();
        for (PhotoCube photo : alg.getPhotosMatched()) {
            double diff = photo.getTimestamp() - photo.logTmp.getTimestamp();
            minMaxY.update(diff);
            minMaxX.update(photo.logTmp.getTimestamp());
        }
        // System.out.println("minMaxX:"+minMaxX + " size="+minMaxX.size());
        // double lastX = Double.NEGATIVE_INFINITY;
        for (PhotoCube photo : alg.getPhotosMatched()) {
            double diff = photo.getTimestamp() - photo.logTmp.getTimestamp();
            double x = photo.logTmp.getTimestamp() - minMaxX.min;
            // System.out.print(photo.getPhotoLogLine().imageNumber + " ");
            // System.out.print(x+ " diff=");
            // if (lastX >= x){
            // System.out.println("============== Ahhhhhhh=======");
            // lastX = x;
            // }

            // System.out.println(diff-minMaxY.min);
            serDiff.add(x, diff - minMaxY.min);
            serHeading.add(x, photo.logTmp.heading);
        }

        // double last = 0;

        double y2 = minMaxY.size() * 1.1;
        double y1 = minMaxY.size() * 0.5;
        double y0 = minMaxY.size() * -0.1;

        for (CPhotoLogLine line : alg.getLogsUnmatched()) {
            double time = line.getTimestamp() - minMaxX.min;
            serLossLog.add(time, y1);
            serLossLog.add(time, y2);
            serLossLog.add(time, y1);
        }

        for (PhotoCube pic : alg.getPhotosUnmatched()) {
            double time = pic.getTimestamp() + alg.getShift() - minMaxX.min;
            serLossImg.add(time, y1);
            serLossImg.add(time, y0);
            serLossImg.add(time, y1);
        }

        for (PhotoCube pic : alg.getPhotosCalib()) {
            double time = pic.getTimestamp() + alg.getShift() - minMaxX.min;
            serCalibImg.add(time, y1);
            serCalibImg.add(time, y0);
            serCalibImg.add(time, y1);
        }

        XYSeriesCollection xyDatasetPics = new XYSeriesCollection();
        XYSeriesCollection xyDatasetMissingPics = new XYSeriesCollection();

        XYSeriesCollection xyDatasetHeading = new XYSeriesCollection();

        JFreeChart chart =
            ChartFactory.createXYLineChart(
                languageHelper.getString(
                    KEY + ".chartTitle",
                    alg.getPhotosMatched().size(),
                    alg.getPhotosUnmatched().size(),
                    alg.getLogsUnmatched().size(),
                    "Algorithm: " + alg.getAlgName(),
                    hardwareConfiguration.getPlatformDescription().getName(),
                    cameraConfiguration.getDescription().getName(),
                    cameraConfiguration.getLens().getDescription().getName()),
                languageHelper.getString(KEY + ".xAxis")
                    + " - "
                    + minMaxX.min
                    + "s  (cam clock shift: "
                    + alg.getShift()
                    + "s)", // Time [s]
                languageHelper.getString(
                    KEY + ".yAxis",
                    MathHelper.round(minMaxY.size(), 3),
                    cameraConfiguration.getLens().getDescription().getMaxTimeVariation()),
                xyDatasetPics, // Dataset
                PlotOrientation.VERTICAL,
                true // Show legend
                ,
                true,
                false // no tooltips, no urls
                );
        chart.setBackgroundPaint(Color.white);

        final NumberAxis axis2 = new NumberAxis(languageHelper.getString(KEY + ".y2Axis"));
        axis2.setAutoRangeIncludesZero(false);

        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.getDomainAxis(); // .setAutoRangeMinimumSize(5);
        ((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);

        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        plot.setRangeAxis(1, axis2);
        plot.setDataset(0, xyDatasetPics);
        plot.setDataset(1, xyDatasetMissingPics);
        plot.setDataset(2, xyDatasetHeading);
        plot.mapDatasetToRangeAxis(2, 1);

        XYItemRenderer renderer1 = new XYLineAndShapeRenderer();
        Shape cross = ShapeUtilities.createDiagonalCross(3, 0.5f);
        renderer1.setSeriesShape(0, cross);
        renderer1.setSeriesStroke(
            0,
            new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {2.0f, 2.0f}, 0.0f));
        plot.setRenderer(0, renderer1);

        Shape triangelUp = ShapeUtilities.createUpTriangle(3);
        Shape triangelDown = ShapeUtilities.createDownTriangle(3);
        Shape diamong = ShapeUtilities.createDiagonalCross(3, 3);

        final XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        renderer2.setSeriesPaint(0, Color.blue);
        renderer2.setSeriesPaint(1, Color.ORANGE);
        renderer2.setSeriesPaint(2, Color.GREEN);
        renderer2.setSeriesShape(0, triangelUp);
        renderer2.setSeriesShape(1, triangelDown);
        renderer2.setSeriesShape(2, diamong);

        plot.setRenderer(1, renderer2);

        final StandardXYItemRenderer renderer3 = new StandardXYItemRenderer();
        renderer3.setSeriesPaint(0, Color.black);
        // renderer2.setPlotShapes(true);
        // renderer.setToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
        plot.setRenderer(2, renderer3);

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

        xyDatasetPics.addSeries(serDiff);
        xyDatasetMissingPics.addSeries(serLossLog);
        xyDatasetMissingPics.addSeries(serLossImg);
        xyDatasetMissingPics.addSeries(serCalibImg);
        xyDatasetHeading.addSeries(serHeading);

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
