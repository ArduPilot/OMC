/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.imageio;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.image.BufferedImage;

public class BufferedImageTranscoder extends ImageTranscoder {

    @Nullable
    private BufferedImage img = null;

    private final int type;

    public BufferedImageTranscoder(int type) {
        this.type = type;
    }

    @Override
    protected void setImageSize(float width, float height) {
        if (width > 0 && height > 0) {
            super.setImageSize(width, height);
        }
    }

    @Override
    public BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, type);
    }

    @Override
    public void writeImage(BufferedImage img, TranscoderOutput to) throws TranscoderException {
        this.img = img;
    }

    @Nullable
    public BufferedImage getBufferedImage() {
        return img;
    }
}
