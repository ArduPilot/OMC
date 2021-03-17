/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.intel.missioncontrol.drone.IVideoStream;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;

public class LiveVideoStream implements ILiveVideoStream {
    private final Set<ILiveVideoStreamListener> handlerList = new HashSet<>();
    private String description;
    private String uri;
    private Pointer minavCtx = null;
    private LiveVideoService.IMinAv minavHandle = null;
    private Boolean requestActive = false;
    private Object requestToken = new Object();

    private int requestWidth = INITIAL_WIDTH_REQUEST;
    private int requestHeight = INITIAL_HEIGHT_REQUEST;
    private Memory requestBuffer;
    private boolean requestChanged = true;

    private static final int KEEP_ALIVE_TIME_MS = 2000;
    private final int DECODE_THREAD_TIMEOUT_MS = 2000;
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

    public LiveVideoStream(IVideoStream droneVideoStream) {
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
    }

    void release() {
        if (minavHandle != null) stop();
    }

    public Pointer getMinavCtx() {
        return minavCtx;
    }

    public void request() {
        if (requestActive) return;

        synchronized (requestToken) {
            if (requestChanged) {
                requestBuffer = new Memory(requestWidth * requestHeight * 4 + 4 * 4 + 4).align(4);
                requestChanged = false;
            }

            minavHandle.get(minavCtx, requestBuffer, requestWidth, requestHeight, LiveVideoService.IMinAv.PIXFMT_BGRA);
        }

        requestActive = true;
    }

    public void feed() {
        // extract data
        // requestBuffer should be filled by now
        int origWidth = requestBuffer.getInt(0);
        int origHeight = requestBuffer.getInt(4);
        int scaledWidth = requestBuffer.getInt(8);
        int scaledHeight = requestBuffer.getInt(12);
        ByteBuffer pixelBuffer = requestBuffer.getByteBuffer(16, scaledWidth * scaledHeight * 4);

        final ImageFrame frame = new ImageFrame(pixelBuffer, scaledWidth, scaledHeight, origWidth, origHeight);
        lastFrame = frame;

        //        System.out.println(frame);
        synchronized (handlerList) {
            for (ILiveVideoStreamListener handler : handlerList) {
                Platform.runLater(() -> handler.updateFrame(frame));
            }
        }

        requestActive = false;
    }

    public Boolean isRequestActive() {
        return requestActive;
    }

    @Override
    public void addVideoStreamListener(ILiveVideoStreamListener handler) {
        synchronized (handlerList) {
            handlerList.add(handler);
        }
    }

    @Override
    public void removeVideoStreamListener(ILiveVideoStreamListener handler) {
        synchronized (handlerList) {
            handlerList.remove(handler);
            if (handlerList.isEmpty()) lastSeenTimeStamp = System.currentTimeMillis();
        }
    }

    @Override
    public void requestVideoSize(int width, int height) {
        if (width == 0 || height == 0) return;
        synchronized (requestToken) {
            requestWidth = width;
            requestHeight = height;
            requestChanged = true;
        }
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public ImageFrame getLastFrame() {
        return lastFrame;
    }
}
