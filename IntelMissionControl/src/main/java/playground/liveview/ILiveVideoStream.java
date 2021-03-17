/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import java.nio.ByteBuffer;

public interface ILiveVideoStream {
    interface ILiveVideoStreamListener {
        void updateFrame(ImageFrame frame);

        void cancelled();
    }

    class ImageFrame {
        public final ByteBuffer pixelBuffer;
        public final int scaledWidth;
        public final int scaledHeight;
        public final int origWidth;
        public final int origHeight;

        public ImageFrame(ByteBuffer pixelBuffer, int scaledWidth, int scaledHeight, int origWidth, int origHeight) {
            this.pixelBuffer = pixelBuffer;
            this.scaledWidth = scaledWidth;
            this.scaledHeight = scaledHeight;
            this.origWidth = origWidth;
            this.origHeight = origHeight;
        }
    }

    void addVideoStreamListener(ILiveVideoStreamListener handler);

    void removeVideoStreamListener(ILiveVideoStreamListener handler);

    void requestVideoSize(int width, int height);

    ImageFrame getLastFrame();
}
