/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerExpertSimulatedFails extends IAirplaneListener {

    public static final int MASK_EngineOff = 0x0001;
    public static final int MASK_GPSLoss = 0x0002;
    public static final int MASK_RCLinkLoss = 0x0004;
    public static final int MASK_DataLinkLoss = 0x0008;
    public static final int MASK_LowBattery = 0x0010; // battery lower equal than 1%

    public void recv_simulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3);

}
