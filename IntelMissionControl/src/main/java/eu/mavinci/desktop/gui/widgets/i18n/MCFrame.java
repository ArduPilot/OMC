/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.widgets.i18n;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import eu.mavinci.desktop.main.core.Application;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JRootPane;

@SuppressWarnings("serial")
public class MCFrame extends JFrame implements MComponent {

    public static String KEY_MCFRAME = "eu.mavinci.desktop.gui.widgets.i18n.MFrame";
    protected String KEY = KEY_MCFRAME;

    public static final int DEFAULT_WINDOW_DECORATION_STYLE = JRootPane.FRAME; // JRootPane.PLAIN_DIALOG

    protected boolean isFullscreen = false;
    protected boolean wasMaximizedBoth = false;

    /** Workarount to make toFront() working plattform independent! */
    public void forceToFront() {
        if (getExtendedState() == ICONIFIED) {
            //			System.out.println("toFront called while iconifiyed:"+extendedStateTarget);
            setExtendedState(extendedStateTarget);
        }
        //		System.out.println("toFront");
        super.toFront();
        requestFocus();
        //		setAlwaysOnTop(true);
        //		setAlwaysOnTop(false);
    }

    int extendedStateTarget = NORMAL;

    @Override
    public void setExtendedState(int state) {
        if (state != ICONIFIED) extendedStateTarget = state;
        super.setExtendedState(state);
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        if (fullscreen == isFullscreen) return;
        //		System.out.println("set fullscreen to " + fullscreen);
        isFullscreen = fullscreen;
        //		menuTogFullscreen.setState(fullscreen);
        if (fullscreen) {
            MCFrame.this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
            wasMaximizedBoth = getExtendedState() == MAXIMIZED_BOTH;
            setExtendedState(MAXIMIZED_BOTH);
        } else {
            MCFrame.this.getRootPane().setWindowDecorationStyle(DEFAULT_WINDOW_DECORATION_STYLE);
            if (!wasMaximizedBoth) setExtendedState(NORMAL);
        }
    }

    protected boolean isLoaded = false;

    public MCFrame() {
        super();

        setLayout(new BorderLayout());

        getRootPane().setWindowDecorationStyle(DEFAULT_WINDOW_DECORATION_STYLE);

        addComponentListener(
            new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    if (!isLoaded) return;
                    if (getExtendedState() != MAXIMIZED_BOTH) {
                        //				if (currentWindowState != MAXIMIZED_BOTH) {
                        if (MCFrame.this.getSize().height != 0 && MCFrame.this.getSize().width != 0) {
                            sizeBeforeFullscreen = MCFrame.this.getSize();
                            //						Debug.getLog().info("save size : " + MFrame.this.getSize());
                        } else {
                            //						Debug.getLog().info("component resized to 0");
                        }
                    }
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    if (!isLoaded) return;
                    if (getExtendedState() != MAXIMIZED_BOTH) {
                        //				if (currentWindowState != MAXIMIZED_BOTH) {
                        locationBeforeFullscreen = MCFrame.this.getLocation();
                    }
                }

                @Override
                public void componentShown(ComponentEvent e) {
                    if (isFullscreen) setExtendedState(MAXIMIZED_BOTH);
                    else setExtendedState(extendedStateTarget); // since fullscreen is not restored properly!
                }

            });
    }

    protected Point locationBeforeFullscreen = null;
    protected Dimension sizeBeforeFullscreen = null;

    public static final int DISABLE_DIVIDER_LOCATION = -1;

    public MCFrame(String iconResource) {
        this();
        setIcon(iconResource);
    }

    protected Dimension minimumSize = ScaleHelper.scaleDimension(new Dimension(100, 100));

    /** if a changing of language occurs, this function relabels all text elements on the frame */
    public void languageChanged() {
        ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);
        setTitle(languageHelper.getString(KEY + KEY_TITLE));
    }

    public void setKey(String KEY) {
        this.KEY = KEY;
        languageChanged();
    }

    public String getKey() {
        return KEY;
    }

    String iconResource;

    public void setIcon(String resourcePath) {
        iconResource = resourcePath;
        ImageIcon icon = Application.getImageIconFromResource(resourcePath);
        if (icon != null) {
            setIconImage(icon.getImage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
