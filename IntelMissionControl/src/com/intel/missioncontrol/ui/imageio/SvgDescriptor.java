/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.imageio;

import com.sun.javafx.iio.common.ImageDescriptor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("restriction")
public class SvgDescriptor extends ImageDescriptor {

    private static final String formatName = "SVG";

    private static final String[] extensions = {"svg"};

    private static final Signature[] signatures = {
        new Signature("<svg".getBytes(StandardCharsets.UTF_8)), new Signature("<?xml".getBytes(StandardCharsets.UTF_8))
    };

    @MonotonicNonNull
    private static ImageDescriptor theInstance = null;

    private SvgDescriptor() {
        super(formatName, extensions, signatures);
    }

    public static synchronized ImageDescriptor getInstance() {
        if (theInstance == null) {
            theInstance = new SvgDescriptor();
        }

        return theInstance;
    }
}
