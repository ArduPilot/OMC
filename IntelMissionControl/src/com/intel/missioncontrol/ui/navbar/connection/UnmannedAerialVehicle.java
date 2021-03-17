/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.desktop.rs232.Rs232Params;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UnmannedAerialVehicle {
    public final AirplaneType model;
    public final String name;
    public final UavInFlightInfo uavInFlightInfo;
    public final UavInfo info;
    public final UavConnectionInfo connectionInfo;
    public final Rs232Params connectionParams;

    public UnmannedAerialVehicle(
            @NonNull AirplaneType model,
            @NonNull String name,
            @NonNull UavInFlightInfo uavInFlightInfo,
            @NonNull UavInfo info,
            @NonNull UavConnectionInfo connectionInfo,
            Rs232Params connectionParams) {
        this.model = model;
        this.name = name;
        this.uavInFlightInfo = uavInFlightInfo;
        this.info = info;
        this.connectionInfo = connectionInfo;
        this.connectionParams = connectionParams;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object != null && object instanceof UnmannedAerialVehicle) {
            UnmannedAerialVehicle that = (UnmannedAerialVehicle)object;

            return model == that.model
                && name.equals(that.name)
                && info.equals(that.info)
                && connectionInfo.equals(that.connectionInfo);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = model.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + info.hashCode();
        result = 31 * result + connectionInfo.hashCode();
        return result;
    }

    public UavInFlightInfo getUavInFlightInfo() {
        return uavInFlightInfo;
    }
}
