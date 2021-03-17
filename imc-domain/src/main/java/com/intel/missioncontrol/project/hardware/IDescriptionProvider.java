/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import java.util.List;

public interface IDescriptionProvider {

    List<PlatformDescription> getPlatformDescriptions();

    List<IGenericCameraDescription> getCameraDescriptions();

    List<LensDescription> getLensDescriptions();

    PlatformDescription getPlatformDescriptionById(String id);

    IPayloadDescription getPayloadDescriptionById(String id);

    IGenericCameraDescription getGenericCameraDescriptionById(String id);

    LensDescription getLensDescriptionById(String id);
}
