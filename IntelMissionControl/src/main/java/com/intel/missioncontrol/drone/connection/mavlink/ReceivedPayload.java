/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

public class ReceivedPayload<T> {
    private T payload;
    private MavlinkEndpoint senderEndpoint;

    ReceivedPayload(T payload, MavlinkEndpoint senderEndpoint) {
        this.payload = payload;
        this.senderEndpoint = senderEndpoint;
    }

    public T getPayload() {
        return payload;
    }

    public MavlinkEndpoint getSenderEndpoint() {
        return senderEndpoint;
    }
}
