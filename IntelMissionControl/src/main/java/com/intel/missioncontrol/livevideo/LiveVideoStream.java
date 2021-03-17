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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveVideoStream implements IUILiveVideoStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveVideoStream.class);

    private final Set<IUILiveVideoStreamListener> handlerList = new HashSet<>();
    private String description;
    private String uri;
    private Pointer minAvCtx = null;
    private Boolean active = false;

    private static final int KEEP_ALIVE_TIME_MS = 2000;
    private static final int DECODE_THREAD_TIMEOUT_MS = 1000;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int RECONNECT_TIMEOUT_MS = 5000;
    private static final int RUNNING_TIMEOUT_MS = 1000;
    private long lastSeenTimestamp = 0;
    private long lastNewDataTimestamp = 0;
    private long lastActivateTimestamp = 0;

    private ImageFrame lastFrame = null;

    private final IVideoStream videoStream;

    enum TakeAction {
        NONE,
        ACTIVATE,
        DEACTIVATE,
        REMOVE,
        POLL
    }

    enum StreamState {
        NOT_CONNECTED,
        CONNECTING,
        RUNNING,
        RECONNECTING,
        CONNECTION_LOST,
        RELEASE
    }

    private StreamState streamState = StreamState.NOT_CONNECTED;

    class StartSettings {
        String uri;
        int timeout;

        StartSettings(String uri, int timeout) {
            this.uri = uri;
            this.timeout = timeout;
        }
    }

    public class GetSettings {
        private Memory buffer;
        private int width;
        private int height;
        private int format;

        GetSettings(int width, int height) {
            this.buffer = new Memory(width * height * 4).align(4);
            this.width = width;
            this.height = height;
            this.format = MinAvProtobuf.PixelFormat.BGRA_VALUE;
        }

        public Memory getMemory() {
            return buffer;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getFormat() {
            return format;
        }

        private ByteBuffer getByteBuffer() {
            return buffer.getByteBuffer(0, width * height * 4);
        }
    }

    private GetSettings getSettings = null;

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    LiveVideoStream(IVideoStream droneVideoStream) {
        if (droneVideoStream != null) setUri(droneVideoStream.getStreamURI());
        videoStream = droneVideoStream;
    }

    public TakeAction getAction() {
        // FIXME: if (release) return TakeAction.REMOVE; are there still references from the handlers?

        if (streamState == StreamState.RELEASE) {
            streamState = StreamState.NOT_CONNECTED;
            return TakeAction.REMOVE;
        }

        synchronized (handlerList) {
            if (handlerList.isEmpty()) {
                if (streamState == StreamState.NOT_CONNECTED) return TakeAction.NONE;
                if (lastSeenTimestamp + KEEP_ALIVE_TIME_MS < System.currentTimeMillis()) {
                    streamState = StreamState.NOT_CONNECTED;
                    // if (videoStream != null) videoStream.StopStreamingAsync();
                    return TakeAction.DEACTIVATE;
                }
            }
        }

        switch (streamState) {
        case NOT_CONNECTED:
            lastActivateTimestamp = System.currentTimeMillis();
            lastNewDataTimestamp = 0;
            notifyImageFrameEvent(ImageFrame.Event.CONNECTING);
            streamState = StreamState.CONNECTING;
            return TakeAction.ACTIVATE;
        case CONNECTING:
            if (lastActivateTimestamp + CONNECT_TIMEOUT_MS < System.currentTimeMillis()) {
                notifyImageFrameEvent(ImageFrame.Event.CONNECTION_LOST);
                streamState = StreamState.CONNECTION_LOST;
                lastActivateTimestamp = 0;
                return TakeAction.DEACTIVATE;
            }

            if (lastNewDataTimestamp != 0) {
                streamState = StreamState.RUNNING;
            }

            return TakeAction.POLL;
        case RUNNING:
            if (lastNewDataTimestamp + RUNNING_TIMEOUT_MS < System.currentTimeMillis()) {
                notifyImageFrameEvent(ImageFrame.Event.RECONNECTING);
                streamState = StreamState.RECONNECTING;
                lastActivateTimestamp = 0;
                return TakeAction.DEACTIVATE;
            }

            return TakeAction.POLL;
        case RECONNECTING:
            if (lastActivateTimestamp == 0) {
                lastActivateTimestamp = System.currentTimeMillis();
                lastNewDataTimestamp = 0;
                return TakeAction.ACTIVATE;
            }

            if (lastActivateTimestamp + RECONNECT_TIMEOUT_MS < System.currentTimeMillis()) {
                notifyImageFrameEvent(ImageFrame.Event.CONNECTION_LOST);
                streamState = StreamState.CONNECTION_LOST;
                lastActivateTimestamp = 0;
                return TakeAction.DEACTIVATE;
            }

            if (lastNewDataTimestamp != 0) {
                streamState = StreamState.RUNNING;
            }

            return TakeAction.POLL;
        case CONNECTION_LOST:
            if (lastActivateTimestamp == 0) {
                lastActivateTimestamp = System.currentTimeMillis();
                lastNewDataTimestamp = 0;
                return TakeAction.ACTIVATE;
            }

            if (lastActivateTimestamp + RECONNECT_TIMEOUT_MS < System.currentTimeMillis()) {
                lastActivateTimestamp = 0;
                return TakeAction.DEACTIVATE;
            }

            if (lastNewDataTimestamp != 0) {
                streamState = StreamState.RUNNING;
            }

            return TakeAction.POLL;
        }

        return TakeAction.NONE;
    }

    StartSettings getStartSettings() {
        return new StartSettings(this.uri, DECODE_THREAD_TIMEOUT_MS);
    }

    void release() {
        streamState = StreamState.RELEASE;
    }

    Pointer getMinAvCtx() {
        return minAvCtx;
    }

    void setMinAvCtx(Pointer minAvCtx) {
        this.minAvCtx = minAvCtx;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    boolean stateUpdate(MinAvProtobuf.StateDescription desc) {
        LOGGER.info(
            "MinAv: received state update: "
                + desc.getWidth()
                + "x"
                + desc.getHeight()
                + ", state: "
                + desc.getState());
        if (desc.getState() == MinAvProtobuf.State.RUNNING) {
            getSettings = new GetSettings(desc.getWidth(), desc.getHeight());
            return true;
        }

        return false;
    }

    private void updateHandlers() {
        synchronized (handlerList) {
            for (IUILiveVideoStreamListener handler : handlerList) {
                Platform.runLater(() -> handler.updateFrame(lastFrame));
            }
        }
    }

    GetSettings getGetSettings() {
        return getSettings;
    }

    boolean newData() {
        ByteBuffer pixelBuffer = getSettings.getByteBuffer();
        lastFrame =
            new ImageFrame(pixelBuffer, getSettings.getWidth(), getSettings.getHeight(), ImageFrame.Event.NEW_DATA);
        lastNewDataTimestamp = System.currentTimeMillis();
        updateHandlers();

        return true;
    }

    private void notifyImageFrameEvent(ImageFrame.Event event) {
        // we'll only notify if event changes
        if (lastFrame != null && lastFrame.event == event) return;

        if (lastFrame == null) lastFrame = new ImageFrame(null, 0, 0, event);
        else lastFrame.event = event;

        updateHandlers();
    }

    @Override
    public void addVideoStreamListener(IUILiveVideoStreamListener handler) {
        synchronized (handlerList) {
            if (handlerList.isEmpty() && videoStream != null) videoStream.StartStreamingAsync();
            handlerList.add(handler);
            if (lastFrame != null) handler.updateFrame(lastFrame);
        }
    }

    @Override
    public void removeVideoStreamListener(IUILiveVideoStreamListener handler) {
        synchronized (handlerList) {
            handlerList.remove(handler);
            if (handlerList.isEmpty()) lastSeenTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isDefaultStream() {
        return videoStream.isDefaultStream();
    }

    @Override
    public boolean isSameLiveVideoStream(IUILiveVideoStream other) {
        if (!(other instanceof LiveVideoStream)) return false;

        LiveVideoStream o = (LiveVideoStream)other;

        return o.videoStream.isSameVideoStream(this.videoStream);
    }

    @Override
    public String toString() {
        return description;
    }
}
