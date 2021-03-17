/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import static javafx.beans.binding.Bindings.createObjectBinding;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.livevideo.ILiveVideoService;
import com.intel.missioncontrol.ui.livevideo.IUILiveVideoStream;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.collections.LockedList;

public class VideoStreamValidator implements IFlightValidator {

    public interface Factory {
        VideoStreamValidator create();
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncListProperty<IUILiveVideoStream> videoStreamList = new UIAsyncListProperty<>(this);
    private ObjectProperty<IUILiveVideoStream> testStream = new SimpleObjectProperty<>();
    private enum State {
        BELOW_10FPS,
        ABOVE_10FPS,
        CONNECTING,
        RECONNECTING,
        CONNECTION_LOST
    }
    private ObjectProperty<State> streamState = new SimpleObjectProperty<>(State.CONNECTION_LOST);


    @Inject
    public VideoStreamValidator(
            ILanguageHelper languageHelper,
            ILiveVideoService liveVideoService
    ) {
        IUILiveVideoStream.IUILiveVideoStreamListener streamListener =
            new IUILiveVideoStream.IUILiveVideoStreamListener() {
                private long lastSeen = System.currentTimeMillis();
                @Override
                public void updateFrame(IUILiveVideoStream.ImageFrame frame) {
                    switch (frame.event) {
                    case NEW_DATA:
                        long current = System.currentTimeMillis();
                        // FIXME: what if the selected stream is below 10 FPS by implementation?
                        if (current - lastSeen > 100) streamState.set(State.BELOW_10FPS);
                        else streamState.set(State.ABOVE_10FPS);
                        lastSeen = current;
                        break;
                    case CONNECTING:
                        streamState.set(State.CONNECTING);
                        break;
                    case CONNECTION_LOST:
                        streamState.set(State.CONNECTION_LOST);
                        break;
                    case RECONNECTING:
                        streamState.set(State.RECONNECTING);
                        break;
                    }
                }

                @Override
                public void cancelled() {}
            };

        testStream.addListener( ((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeVideoStreamListener(streamListener);
            }

            if (newValue != null) {
                newValue.addVideoStreamListener(streamListener);
            }
        }));

        videoStreamList.bind(liveVideoService.streamListProperty());

        testStream.bind(
            createObjectBinding( () -> {
                if (videoStreamList.isEmpty()) return null;
                else {
                    try(LockedList<IUILiveVideoStream> lockedList = videoStreamList.lock()){
                        return lockedList
                                .stream()
                                .filter(IUILiveVideoStream::isDefaultStream)
                                .findFirst()
                                .orElse(lockedList.get(0));
                    }
                }
            }, videoStreamList)
        );

        validationStatus.bind(
            createObjectBinding( () -> {
                if (testStream.get() == null) {
                    return new FlightValidationStatus(AlertType.WARNING,
                            languageHelper.getString(VideoStreamValidator.class, "noStream"));
                } else {
                    switch (streamState.get()) {
                    case BELOW_10FPS:
                        return new FlightValidationStatus(AlertType.WARNING,
                                languageHelper.getString(VideoStreamValidator.class, "fpsLow"));
                    case ABOVE_10FPS:
                        return new FlightValidationStatus(AlertType.COMPLETED,
                                languageHelper.getString(VideoStreamValidator.class, "streamOk"));
                    case CONNECTION_LOST:
                        return new FlightValidationStatus(AlertType.WARNING,
                                languageHelper.getString(VideoStreamValidator.class, "connLost"));
                    case CONNECTING:
                        return new FlightValidationStatus(AlertType.LOADING,
                                languageHelper.getString(VideoStreamValidator.class, "tryConnect"));
                    case RECONNECTING:
                        return new FlightValidationStatus(AlertType.LOADING,
                                languageHelper.getString(VideoStreamValidator.class, "tryReconnect"));
                    }
                }
                return new FlightValidationStatus(AlertType.WARNING, "Unhandled error");
            },
            testStream,
            streamState
            )
        );
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.VIDEO_STREAM;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IResolveAction> getFirstResolveAction() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IResolveAction> getSecondResolveAction() {
        return null;
    }
}
