/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

public class RoundedBorder extends AbstractBorder {

    private static final long serialVersionUID = 1L;
    private Color color;
    private int thickness = 1;
    private int arch = 4;
    private Insets insets = null;
    private BasicStroke stroke = null;
    RenderingHints hints;

    RoundedBorder(Color color) {
        this(color, 1, 4);
    }

    RoundedBorder(Color color, int thickness, int arch) {
        this.color = color;
        this.thickness = thickness;
        this.arch = arch;
        stroke = new BasicStroke(thickness);
        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int pad = arch + (thickness / 2);
        int bottomPad = pad + (thickness / 2);
        insets = new Insets(pad, pad, bottomPad, pad);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getBorderInsets(c);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D graphics = (Graphics2D)g;

        RoundRectangle2D.Double roundRectangle =
            new RoundRectangle2D.Double(
                thickness / 2, thickness / 2, width - thickness, height - thickness, arch, arch);

        Area area = new Area(roundRectangle);

        graphics.setRenderingHints(hints);
        graphics.setColor(color);
        graphics.setStroke(stroke);

        Component parent = c.getParent();
        if (parent != null) {
            Color bg = parent.getBackground();
            Rectangle rect = new Rectangle(0, 0, width, height);
            Area borderRegion = new Area(rect);
            borderRegion.subtract(area);
            graphics.setClip(borderRegion);
            graphics.setColor(bg);
            graphics.fillRect(0, 0, width, height);
            graphics.setClip(null);
        }

        graphics.draw(area);
    }
}
