/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.INotificationObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IGenericCameraConfiguration extends IPayloadConfiguration, INotificationObject {

    String DESCRIPTION_PROPERTY = "description";
    String LENS_PROPERTY = "lens";

    IGenericCameraDescription getDescription();

    void setDescription(IGenericCameraDescription description);

    <T extends ILensConfiguration> T getLens(Class<T> lensClass);

    @Nullable
    ILensConfiguration getLens();

    void setLens(ILensConfiguration lens);

}
