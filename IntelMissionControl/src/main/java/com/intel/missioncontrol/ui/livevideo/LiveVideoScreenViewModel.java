/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;

enum State {
    UNSELECTED,
    CONNECTING,
    RUNNING,
    RECONNECTING,
    FAILED
}

public class LiveVideoScreenViewModel extends ViewModelBase {
    private final ObjectProperty<IUILiveVideoStream> stream = new SimpleObjectProperty<>();
    private final IUILiveVideoStream.IUILiveVideoStreamListener streamListener;
    private final Property<WritableImage> currentFrame = new SimpleObjectProperty<>();

    private ObjectProperty<State> state = new SimpleObjectProperty<>();

    public LiveVideoScreenViewModel() {
        state.set(State.UNSELECTED);
        streamListener =
            new IUILiveVideoStream.IUILiveVideoStreamListener() {
                @Override
                public void updateFrame(IUILiveVideoStream.ImageFrame frame) {
                    LiveVideoScreenViewModel.this.updateFrame(frame);
                }

                @Override
                public void cancelled() {
                    LiveVideoScreenViewModel.this.cancelled();
                }
            };

        stream.addListener(
            (obs, oldValue, newValue) -> {
                if (newValue != null) newValue.addVideoStreamListener(streamListener);
                if (oldValue != null) oldValue.removeVideoStreamListener(streamListener);
                if (newValue == null) {
                    state.set(State.UNSELECTED);
                }
            });
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
    }

    Property<IUILiveVideoStream> streamProperty() {
        return stream;
    }

    private void updateFrame(IUILiveVideoStream.ImageFrame frame) {
        switch (frame.event) {
            case CONNECTING:
                state.set(State.CONNECTING);
                break;
            case RECONNECTING:
                state.set(State.RECONNECTING);
                break;
            case CONNECTION_LOST:
                state.set(State.FAILED);
                break;
            case NEW_DATA:
                state.set(State.RUNNING);
                break;
        }

        if (frame.pixelBuffer == null) return;

        final int width = frame.width;
        final int height = frame.height;
        WritableImage wi = currentFrame.getValue();
        if (wi == null || wi.getWidth() != width || wi.getHeight() != height) {
            wi = new WritableImage(width, height);
            currentFrame.setValue(wi);
        }

        wi.getPixelWriter()
            .setPixels(0, 0, width, height, PixelFormat.getByteBgraPreInstance(), frame.pixelBuffer, 4 * width);
    }

    private void cancelled() {}

    ReadOnlyObjectProperty<State> stateProperty() {
        return state;
    }

    ReadOnlyProperty<WritableImage> currentFrameProperty() {
        return currentFrame;
    }

}
