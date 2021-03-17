/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageMask extends ImgParams {

    public ImageMask(BufferedImage img, File file) throws IOException {
        super(img, file);
    }

    public ImageMask(BufferedImage img) throws IOException {
        super(img);
    }

    public ImageMask(File file) throws IOException {
        super(file);
    }

    public final BufferedImage applyMask(BufferedImage inImg) throws IOException {
        final ImgParams paramsIn = new ImgParams(inImg);

        if (paramsIn.equalDimension(this)) {
            final BufferedImage outImg =
                new BufferedImage(paramsIn.width, paramsIn.height, BufferedImage.TYPE_4BYTE_ABGR);
            final ImgParams paramsOut = new ImgParams(outImg);

            int iIn = 0;
            int iOut = 0;
            int iMask = this.OFFSET_G; // just sample any channel
            for (int i = 0; i != paramsIn.pixCount; i++) {
                if (this.buffer.getElem(iMask) == 0) {
                    paramsOut.buffer.setElem(iOut + paramsOut.OFFSET_A, 0);
                } else {
                    paramsOut.buffer.setElem(
                        iOut + paramsOut.OFFSET_R, paramsIn.buffer.getElem(iIn + paramsIn.OFFSET_R));
                    paramsOut.buffer.setElem(
                        iOut + paramsOut.OFFSET_G, paramsIn.buffer.getElem(iIn + paramsIn.OFFSET_G));
                    paramsOut.buffer.setElem(
                        iOut + paramsOut.OFFSET_B, paramsIn.buffer.getElem(iIn + paramsIn.OFFSET_B));
                    paramsOut.buffer.setElem(iOut + paramsOut.OFFSET_A, 255);
                }

                iOut += paramsOut.bytePerPixel;
                iMask += this.bytePerPixel;
                iIn += paramsIn.bytePerPixel;
            }

            return outImg;
        } else {
            final BufferedImage outImg =
                new BufferedImage(paramsIn.width, paramsIn.height, BufferedImage.TYPE_4BYTE_ABGR);
            final ImgParams paramsOut = new ImgParams(outImg);

            int iIn = 0;
            int iOut = 0;
            for (int y = 0; y != paramsIn.height; y++) {
                for (int x = 0; x != paramsIn.width; x++) {
                    int iMask = this.OFFSET_G;
                    // System.out.println("x:"+x+"\ty:"+y+"\tiMask:"+iMask);
                    iMask += (int)(this.width * ((double)x) / paramsIn.width) * this.bytePerPixel;
                    // System.out.println("x:"+x+"\ty:"+y+"\tiMask:"+iMask);
                    iMask += (int)(this.height * ((double)y) / paramsIn.height) * this.width * this.bytePerPixel;
                    // System.out.println("x:"+x+"\ty:"+y+"\tiMask:"+iMask);

                    if (this.buffer.getElem(iMask) == 0) {
                        paramsOut.buffer.setElem(iOut + paramsOut.OFFSET_A, 0);
                    } else {
                        paramsOut.buffer.setElem(
                            iOut + paramsOut.OFFSET_R, paramsIn.buffer.getElem(iIn + paramsIn.OFFSET_R));
                        paramsOut.buffer.setElem(
                            iOut + paramsOut.OFFSET_G, paramsIn.buffer.getElem(iIn + paramsIn.OFFSET_G));
                        paramsOut.buffer.setElem(
                            iOut + paramsOut.OFFSET_B, paramsIn.buffer.getElem(iIn + paramsIn.OFFSET_B));
                        paramsOut.buffer.setElem(iOut + paramsOut.OFFSET_A, 255);
                    }

                    iIn += paramsIn.bytePerPixel;
                    iOut += paramsOut.bytePerPixel;
                }
            }

            return outImg;
        }
    }

    public final BufferedImage applyMask(File imgFile) throws IOException {
        BufferedImage inImg = ImageHelper.loadImage(imgFile);

        return applyMask(inImg);
    }
}
