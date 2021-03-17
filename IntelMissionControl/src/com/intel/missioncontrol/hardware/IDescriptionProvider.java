/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import java.util.List;

public interface IDescriptionProvider {

    List<IPlatformDescription> getPlatformDescriptions();

    List<IGenericCameraDescription> getCameraDescriptions();

    List<ILensDescription> getLensDescriptions();

}
