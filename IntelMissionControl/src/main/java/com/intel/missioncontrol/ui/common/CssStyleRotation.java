/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common;

import com.intel.missioncontrol.ui.controls.Button;
import java.time.Duration;
import java.util.Arrays;
import java.util.WeakHashMap;
import javafx.css.Styleable;
import org.asyncfx.concurrent.Dispatcher;

public class CssStyleRotation {
    private static final WeakHashMap<Styleable, CssStyleRotation> rotations = new WeakHashMap<>();
    private static int index = 0;

    static {
        Dispatcher.platform()
            .runLaterAsync(
                () -> {
                    index++;
                    for (CssStyleRotation rotation : rotations.values()) {
                        rotation.rotate();
                    }
                },
                Duration.ofMillis(500),
                Duration.ofMillis(500));
    }

    public static void setRotation(Styleable styleable, String... styles) {
        Dispatcher.platform()
            .runLater(
                () -> {
                    CssStyleRotation old = rotations.get(styleable);
                    if (old != null) {
                        if (Arrays.equals(old.styles, styles)) return;
                        rotations.remove(styleable);
                        old.dropCurrent();
                    }

                    if (styles.length > 0) {
                        CssStyleRotation rotation = new CssStyleRotation(styleable, styles);
                        rotations.put(styleable, rotation);
                        rotation.addCurrent();
                    }
                });
    }

    public static void setCritical(Button button) {
        setRotation(button, "critical", "transparent");
    }

    public static void stop(Styleable styleable) {
        setRotation(styleable);
    }

    private final Styleable styleable;
    private final String[] styles;
    private String currentStyle;

    public CssStyleRotation(Styleable styleable, String... styles) {
        this.styleable = styleable;
        this.styles = styles;
    }

    public void rotate() {
        dropCurrent();
        addCurrent();
    }

    private void dropCurrent() {
        styleable.getStyleClass().remove(currentStyle);
    }

    private void addCurrent() {
        currentStyle = styles[index % styles.length];
        styleable.getStyleClass().add(currentStyle);
    }

    @Override
    public String toString() {
        return "CssStyleRotation{"
            + "styleable="
            + styleable
            + ", styles="
            + Arrays.toString(styles)
            + ", currentStyle='"
            + currentStyle
            + '\''
            + '}';
    }
}
