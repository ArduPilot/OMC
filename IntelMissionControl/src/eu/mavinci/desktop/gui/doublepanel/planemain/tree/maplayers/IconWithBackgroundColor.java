/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

/** */
package eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers;

import eu.mavinci.desktop.helper.ColorHelper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;

public class IconWithBackgroundColor implements Icon {

    // private int width = 16;
    // private int height = 16;

    private Color background;
    private Icon foreground;

    public IconWithBackgroundColor(Icon foregroundIcon, Color background) {
        if (foregroundIcon == null) {
            throw new NullPointerException("icon should not be NULL");
        }

        foreground = foregroundIcon;
        if (background != null) {
            this.background = ColorHelper.removeAlpha(background);
        }
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g.create();

        if (background != null) {
            g2d.setColor(background);
            g2d.fillRect(x, y, foreground.getIconWidth(), foreground.getIconHeight());
        }

        foreground.paintIcon(c, g2d, x, y);
        g2d.dispose();
    }

    public int getIconWidth() {
        return foreground.getIconWidth();
    }

    public int getIconHeight() {
        return foreground.getIconHeight();
    }

}
