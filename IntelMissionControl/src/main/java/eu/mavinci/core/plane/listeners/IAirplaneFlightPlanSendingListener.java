/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneFlightPlanSendingListener extends IAirplaneListener {
    /** flight Phase */
    public class FlightPlanStaus {
        public int currentWPNumber;
        public int maxAmountToSend;
    }

    public void recv_fpSendingStatusChange(FlightPlanStaus fpStatus);

}
