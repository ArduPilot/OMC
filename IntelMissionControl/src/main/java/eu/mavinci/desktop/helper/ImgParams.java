/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

public class ImgParams {
    final int width;
    final int height;
    final int bytePerPixel;
    final int OFFSET_R;
    final int OFFSET_G;
    final int OFFSET_B;
    final int OFFSET_A;
    final int max;
    final DataBuffer buffer;
    final BufferedImage img;
    final int pixCount;
    final File file;

    public File getFile() {
        return file;
    }

    public ImgParams(BufferedImage img, File file) throws IOException {
        this.img = img;
        this.file = file;
        width = img.getWidth();
        height = img.getHeight();

        buffer = img.getRaster().getDataBuffer();
        switch (img.getType()) {
        case BufferedImage.TYPE_BYTE_GRAY:
            bytePerPixel = 1;
            OFFSET_R = 0;
            OFFSET_G = 0;
            OFFSET_B = 0;
            OFFSET_A = -1;
            break;
        case BufferedImage.TYPE_INT_RGB:
            bytePerPixel = 3;
            OFFSET_R = 0;
            OFFSET_G = 1;
            OFFSET_B = 2;
            OFFSET_A = -1;
            break;
        case BufferedImage.TYPE_INT_BGR:
        case BufferedImage.TYPE_3BYTE_BGR:
            bytePerPixel = 3;
            OFFSET_B = 0;
            OFFSET_G = 1;
            OFFSET_R = 2;
            OFFSET_A = -1;
            break;
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
            bytePerPixel = 4;
            OFFSET_A = 0;
            OFFSET_R = 1;
            OFFSET_G = 2;
            OFFSET_B = 3;
            break;
        case BufferedImage.TYPE_4BYTE_ABGR:
        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            bytePerPixel = 4;
            OFFSET_A = 0;
            OFFSET_B = 1;
            OFFSET_G = 2;
            OFFSET_R = 3;
            break;
        default:
            throw new IOException("could not load image, wrong colormodel: " + img.getType());
        }

        max = buffer.getSize();
        pixCount = max / bytePerPixel;
    }

    public ImgParams(BufferedImage img) throws IOException {
        this(img, null);
    }

    public ImgParams(File file) throws IOException {
        this(ImageHelper.loadImage(file), file);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + OFFSET_A;
        result = prime * result + OFFSET_B;
        result = prime * result + OFFSET_G;
        result = prime * result + OFFSET_R;
        result = prime * result + bytePerPixel;
        result = prime * result + height;
        result = prime * result + max;
        result = prime * result + pixCount;
        result = prime * result + width;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        ImgParams other = (ImgParams)obj;
        if (OFFSET_A != other.OFFSET_A) {
            return false;
        }

        if (OFFSET_B != other.OFFSET_B) {
            return false;
        }

        if (OFFSET_G != other.OFFSET_G) {
            return false;
        }

        if (OFFSET_R != other.OFFSET_R) {
            return false;
        }

        if (bytePerPixel != other.bytePerPixel) {
            return false;
        }

        if (height != other.height) {
            return false;
        }

        if (max != other.max) {
            return false;
        }

        if (pixCount != other.pixCount) {
            return false;
        }

        if (width != other.width) {
            return false;
        }

        return true;
    }

    public boolean equalDimension(ImgParams other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (height != other.height) {
            return false;
        }

        if (width != other.width) {
            return false;
        }

        return true;
    }

}
