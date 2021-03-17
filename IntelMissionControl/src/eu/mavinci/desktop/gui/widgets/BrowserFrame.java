/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.widgets;

import eu.mavinci.desktop.gui.widgets.i18n.MCFrame;

public class BrowserFrame extends MCFrame {

    private static final long serialVersionUID = -2678979143286828251L;
    public final BrowserWidget widget;

    public BrowserFrame(String startURL) {
        setIcon("eu/mavinci/icons/32x32/browser.png");
        setKey(BrowserWidget.KEY);
        setMinimumSize(minimumSize);

        widget =
            new BrowserWidget(startURL) {

                private static final long serialVersionUID = 970807755520454043L;

                @Override
                public void setTitle(String title) {
                    BrowserFrame.this.setTitle(title);
                    ;
                }
            };

        getContentPane().add(widget);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

}
