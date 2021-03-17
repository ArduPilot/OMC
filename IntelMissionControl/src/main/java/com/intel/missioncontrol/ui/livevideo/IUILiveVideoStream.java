/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import java.nio.ByteBuffer;
import javafx.beans.property.Property;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

public interface IUILiveVideoStream {
    interface IUILiveVideoStreamListener {
        void updateFrame(ImageFrame frame);

        void cancelled();
    }

    class ImageFrame {
        final ByteBuffer pixelBuffer;
        public final int width;
        public final int height;

        public ImageFrame(ByteBuffer pixelBuffer, int width, int height) {
            this.pixelBuffer = pixelBuffer;
            this.width = width;
            this.height = height;
        }
    }

    void addVideoStreamListener(IUILiveVideoStreamListener handler);

    void removeVideoStreamListener(IUILiveVideoStreamListener handler);

    static IUILiveVideoStreamListener createStreamListener(Property<WritableImage> frameBuffer) {
        return new IUILiveVideoStream.IUILiveVideoStreamListener() {
            @Override
            public void updateFrame(IUILiveVideoStream.ImageFrame frame) {
                final int width = frame.width;
                final int height = frame.height;
                WritableImage wi = frameBuffer.getValue();
                if (wi == null || wi.getWidth() != width || wi.getHeight() != height) {
                    wi = new WritableImage(width, height);
                    frameBuffer.setValue(wi);
                }

                wi.getPixelWriter()
                        .setPixels(0, 0, width, height, PixelFormat.getByteBgraPreInstance(), frame.pixelBuffer, 4 * width);
            }

            @Override
            public void cancelled() {}
        };
    }
}
