/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.serialization.BinarySerializable;
import com.intel.missioncontrol.serialization.PrimitiveSerializable;

public interface IConnectionProperties extends PrimitiveSerializable, BinarySerializable {
    String getDroneType();

    double getLinkLostTimeoutSeconds();
}
