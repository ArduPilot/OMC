/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class SvgUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SvgUtils.class);

    @Nullable
    public static BufferedImage getImageFromFile(String resourcePath) {
        return getImageFromFile(resourcePath, 0, 0);
    }

    /**
     * Get image from SVG.
     *
     * @param resourcePath The path to the SVG image.
     * @return {@link ImageIcon}. If something goes wrong, returns null.
     */
    @Nullable
    public static BufferedImage getImageFromFile(String resourcePath, int width, int height) {
        TranscodingHints hints = new TranscodingHints();

        if (width > 0) {
            hints.put(ImageTranscoder.KEY_WIDTH, (float)width);
        }

        if (height > 0) {
            hints.put(ImageTranscoder.KEY_HEIGHT, (float)height);
        }

        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, SVGDOMImplementation.getDOMImplementation());
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
        hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);

        ImageTranscoderHelper transcoderHelper = new ImageTranscoderHelper();
        transcoderHelper.setTranscodingHints(hints);
        TranscoderInput input = new TranscoderInput(ClassLoader.getSystemResource(resourcePath).toString());

        try {
            transcoderHelper.transcode(input, null);
        } catch (TranscoderException e) {
            LOGGER.error("Error to transcode svg", e);
            return null;
        }

        return transcoderHelper.getImage();
    }
}
