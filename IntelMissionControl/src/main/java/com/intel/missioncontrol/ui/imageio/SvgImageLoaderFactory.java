/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.imageio;

import com.intel.missioncontrol.helper.Expect;
import com.sun.javafx.iio.ImageFormatDescription;
import com.sun.javafx.iio.ImageLoader;
import com.sun.javafx.iio.ImageLoaderFactory;
import com.sun.javafx.iio.ImageStorage;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("restriction")
public class SvgImageLoaderFactory implements ImageLoaderFactory {

    private static final ImageLoaderFactory INSTANCE = new SvgImageLoaderFactory();
    private static ColorCache colorCache;

    public static void install(ColorCache colorCache) {
        ImageStorage.addImageLoaderFactory(INSTANCE);
        SvgImageLoaderFactory.colorCache = colorCache;
    }

    public static ImageLoaderFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public ImageFormatDescription getFormatDescription() {
        return SvgDescriptor.getInstance();
    }

    @Override
    public ImageLoader createImageLoader(InputStream input) throws IOException {
        Expect.notNull(colorCache);
        return new SvgImageLoader(input, colorCache);
    }

}
