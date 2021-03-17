/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.livevideo;

import com.intel.missioncontrol.drone.IVideoStream;
import com.intel.missioncontrol.ui.livevideo.IUILiveVideoStream;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;

public class LiveVideoStream implements IUILiveVideoStream {
    private final Set<IUILiveVideoStreamListener> handlerList = new HashSet<>();
    private String description;
    private String uri;
    private Pointer minavCtx = null;
    private LiveVideoService.IMinAv minavHandle = null;
    private Boolean requestActive = false;

    private int currentWidth = INITIAL_WIDTH_REQUEST;
    private int currentHeight = INITIAL_HEIGHT_REQUEST;
    private Memory requestBuffer = null;

    private static final int KEEP_ALIVE_TIME_MS = 2000;
    private static final int DECODE_THREAD_TIMEOUT_MS = 2000;
    private long lastSeenTimeStamp = 0;
    private static final int INITIAL_WIDTH_REQUEST = 160;
    private static final int INITIAL_HEIGHT_REQUEST = 90;

    private ImageFrame lastFrame = null;

    enum TakeAction {
        NONE,
        ACTIVATE,
        DEACTIVATE,
        POLL
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    LiveVideoStream(IVideoStream droneVideoStream) {
        if (droneVideoStream != null) setUri(droneVideoStream.getStreamURI());
    }

    public TakeAction getAction() {
        if (minavHandle == null) {
            if (!handlerList.isEmpty()) return TakeAction.ACTIVATE;
        } else {
            if (!handlerList.isEmpty()) return TakeAction.POLL;
            else if (lastSeenTimeStamp + KEEP_ALIVE_TIME_MS < System.currentTimeMillis()) return TakeAction.DEACTIVATE;
        }

        return TakeAction.NONE;
    }

    void activate(LiveVideoService.IMinAv handle) {
        minavCtx = handle.start(this.uri, DECODE_THREAD_TIMEOUT_MS);
        if (minavCtx != Pointer.NULL) minavHandle = handle;
    }

    void stop() {
        minavHandle.stop(minavCtx);
        minavHandle = null;
        requestActive = false;
    }

    void release() {
        if (minavHandle != null) stop();
    }

    Pointer getMinavCtx() {
        return minavCtx;
    }

    public void request() {
        if (requestActive) return;

        if (requestBuffer == null) {
            requestBuffer = new Memory(currentWidth * currentHeight * 4 + 4 * 4 + 4).align(4);
        }

        minavHandle.get(minavCtx, requestBuffer, currentWidth, currentHeight, LiveVideoService.IMinAv.PIXFMT_BGRA);

        requestActive = true;
    }

    void feed() {
        // extract data
        // requestBuffer should be filled by now

        int origWidth = requestBuffer.getInt(0);
        int origHeight = requestBuffer.getInt(4);
        int requestedWidth = requestBuffer.getInt(8);
        int requestedHeight = requestBuffer.getInt(12);

        // check for change in resolution
        if (origWidth != requestedWidth || origHeight != requestedHeight) {
            // TODO: sanity checks on dimensions?
            currentWidth = origWidth;
            currentHeight = origHeight;
            requestBuffer = null;
        } else {
            ByteBuffer pixelBuffer = requestBuffer.getByteBuffer(16, requestedWidth * requestedHeight * 4);

            final ImageFrame frame = new ImageFrame(pixelBuffer, requestedWidth, requestedHeight);
            lastFrame = frame;

            synchronized (handlerList) {
                for (IUILiveVideoStreamListener handler : handlerList) {
                    Platform.runLater(() -> handler.updateFrame(frame));
                }
            }
        }

        requestActive = false;
    }

    Boolean isRequestActive() {
        return requestActive;
    }

    @Override
    public void addVideoStreamListener(IUILiveVideoStreamListener handler) {
        synchronized (handlerList) {
            handlerList.add(handler);
            if (lastFrame != null) handler.updateFrame(lastFrame);
        }
    }

    @Override
    public void removeVideoStreamListener(IUILiveVideoStreamListener handler) {
        synchronized (handlerList) {
            handlerList.remove(handler);
            if (handlerList.isEmpty()) lastSeenTimeStamp = System.currentTimeMillis();
        }
    }

    @Override
    public String toString() {
        return description;
    }
}
