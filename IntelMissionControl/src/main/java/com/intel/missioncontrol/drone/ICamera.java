/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import java.util.List;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;

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

    ReadOnlyAsyncListProperty<? extends IVideoStream> videoStreamsProperty();

    default List<? extends IVideoStream> getVideoStreams() {
        return videoStreamsProperty().get();
    }

    ReadOnlyAsyncObjectProperty<Status> statusProperty();

    default Status getStatus() {
        return statusProperty().get();
    }

    /** The number of images taken by this camera. Reset to 0 when starting a mission. */
    ReadOnlyAsyncIntegerProperty imageCountProperty();

    default int getImageCount()
    {
        return imageCountProperty().get();
    }
}
