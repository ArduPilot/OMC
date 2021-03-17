/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.image.BufferedImage;

/**
 * Helper class to create BufferedImage from SVG image.
 *
 * @author Sérgio
 */
public class ImageTranscoderHelper extends ImageTranscoder {

    private BufferedImage image = null;

    /** @see org.apache.batik.transcoder.image.ImageTranscoder#createImage(int, int) */
    @Override
    public BufferedImage createImage(int weight, int height) {
        image = new BufferedImage(weight, height, BufferedImage.TYPE_INT_ARGB);
        return image;
    }

    /**
     * @see org.apache.batik.transcoder.image.ImageTranscoder#writeImage(java.awt.image.BufferedImage,
     *     org.apache.batik.transcoder.TranscoderOutput)
     */
    @Override
    public void writeImage(BufferedImage arg0, TranscoderOutput arg1) throws TranscoderException {}

    /**
     * Get BufferedImage.
     *
     * @return The {@link BufferedImage}
     */
    public BufferedImage getImage() {
        return this.image;
    }

}
