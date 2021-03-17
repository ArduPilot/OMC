/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;

public interface ICamera {
    enum Status {
        UNKNOWN,
        PARAMETER_ERROR,
        OK
    }

    ReadOnlyAsyncStringProperty nameProperty();

    default String getName() {
        return nameProperty().get();
    }

    ReadOnlyAsyncObjectProperty<? extends IGenericCameraDescription> cameraDescriptionProperty();

    default IGenericCameraDescription getCameraDescription() {
        return cameraDescriptionProperty().get();
    }

    ReadOnlyAsyncObjectProperty<? extends IVideoStream> videoStreamProperty();

    default IVideoStream getVideoStream() {
        return videoStreamProperty().get();
    }

    ReadOnlyAsyncObjectProperty<Status> statusProperty();

    default Status getStatus() {
        return statusProperty().get();
    }
}
