/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import org.asyncfx.concurrent.Future;

public interface IVideoStream {
    ReadOnlyAsyncBooleanProperty isRunningProperty();

    default boolean isRunning() {
        return isRunningProperty().get();
    }

    ReadOnlyAsyncStringProperty streamNameProperty();

    default String getStreamName() {
        return streamNameProperty().get();
    }

    ReadOnlyAsyncStringProperty streamURIProperty();

    default String getStreamURI() {
        return streamURIProperty().get();
    }

    Future<Void> StartStreamingAsync();

    Future<Void> StopStreamingAsync();
}
