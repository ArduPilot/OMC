/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Dimension.Time;
import eu.mavinci.core.flightplan.camera.LensTypes;

public interface IMutableLensDescription extends ILensDescription {

    void setId(String id);

    void setName(String name);

    void setMaxTimeVariation(Quantity<Time> maxTimeVariation);

    void setIsLensManual(boolean isLensManual);

    void setIsLensApertureNotAvailable(boolean isLensApertureNotAvailable);

    void setLensType(LensTypes lensType);

    void setFocalLength(Quantity<Length> focalLength);

    void setMinRepTime(Quantity<Time> minRepTime);

}
