/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.test.utils.MockLanguage;
import de.saxsys.mvvmfx.MvvmFX;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.ContrastEnhancer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.junit.Test;

public class PhotoFileTest {

    static String filename = new String("C:\\Users\\jtroseme\\Downloads\\villetest\\img.tiff");

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new LanguageModule());
        MvvmFX.setCustomDependencyInjector(key -> injector.getInstance(key));

        File original_f = new File(filename);
        //        var original = ImageIO.read(original_f);
        //        var equalized = original;
        //
        //        BufferedImage img = ImageHelper.loadImage(original_f);
        //
        //        img = PhotoFile.normalizeRGBImageHistogram(img);

        var img = IJ.openImage(filename);
        var e = new ContrastEnhancer();
        e.equalize(img);

        var showImageJ = false;
        if (showImageJ) {
            final ImageJ ij = new ImageJ();
            javax.swing.SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        ij.pack();
                        ij.setVisible(true);
                    }
                });
        } else {
            javax.swing.SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        (new PhotoFileTest()).createAndShowGUI(img.getImage());
                    }
                });
        }
    }

    private void createAndShowGUI(Image imgLabel) {
        JFrame frame = new JFrame("hi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new PolyPane(imgLabel));
        frame.setSize(600, 600);
        frame.pack();
        frame.setVisible(true);
    }

    @Test
    public void normalizeImageHistogram() {}

    public static class LanguageModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ILanguageHelper.class).to(MockLanguage.class);
        }
    }

    protected class PolyPane extends JPanel {

        private Polygon poly;
        private Point lastPoint;
        private Image bgImage;

        public PolyPane(Image bgImage) {
            this.bgImage = bgImage;
            poly = new Polygon();

            InputMap im = getInputMap();
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "clear");
            ActionMap am = getActionMap();
            am.put(
                "clear",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        poly = new Polygon();
                        repaint();
                    }
                });

            addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        lastPoint = e.getPoint();
                        poly.addPoint(e.getX(), e.getY());
                        repaint();
                    }
                });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bgImage, 0, 0, null);
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.draw(poly);
            if (lastPoint != null) {
                g2d.setColor(Color.RED);
                g2d.fillOval(lastPoint.x - 5, lastPoint.y - 5, 10, 10);
            }

            g2d.dispose();
        }
    }

}
