/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import java.nio.ByteBuffer;

public interface IUILiveVideoStream {
    interface IUILiveVideoStreamListener {
        void updateFrame(ImageFrame frame);

        // FIXME: is this function ever called?
        void cancelled();
    }

    class ImageFrame {
        final ByteBuffer pixelBuffer;
        public final int width;
        public final int height;
        public Event event;
        public enum Event {
            NEW_DATA,
            CONNECTING,
            RECONNECTING,
            CONNECTION_LOST
        }

        public ImageFrame(ByteBuffer pixelBuffer, int width, int height, Event event) {
            this.pixelBuffer = pixelBuffer;
            this.width = width;
            this.height = height;
            this.event = event;
        }
    }

    void addVideoStreamListener(IUILiveVideoStreamListener handler);

    void removeVideoStreamListener(IUILiveVideoStreamListener handler);

    boolean isDefaultStream();

    boolean isSameLiveVideoStream(IUILiveVideoStream other);
}
