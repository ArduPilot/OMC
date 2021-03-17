/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.intel.missioncontrol.hardware.MavlinkParam;
import io.dronefleet.mavlink.common.MavParamExtType;
import io.dronefleet.mavlink.common.MavParamType;
import java.util.Arrays;
import java.util.Optional;

public class MavlinkParameterFactory {
    public static IMavlinkParameter createFromMavlinkParam(MavlinkParam mavlinkParam) {
        Optional<MavParamType> mavParamType =
            Arrays.stream(MavParamType.values())
                .filter(t -> t.toString().equalsIgnoreCase(mavlinkParam.getType()))
                .findFirst();

        if (mavParamType.isPresent()) {
            double value = Double.parseDouble(mavlinkParam.getValue());
            return Parameter.create(mavlinkParam.getId(), value, mavParamType.get());
        }

        Optional<MavParamExtType> mavParamExtType =
            Arrays.stream(MavParamExtType.values())
                .filter(t -> t.toString().equalsIgnoreCase(mavlinkParam.getType()))
                .findFirst();

        if (mavParamExtType.isPresent()) {
            return ExtendedParameter.createRaw(mavlinkParam.getId(), mavlinkParam.getValue(), mavParamExtType.get());
        }

        throw new IllegalArgumentException("Invalid MavlinkParam type: " + mavlinkParam.getType());
    }

}
