/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import org.asyncfx.concurrent.Future;

interface IPayloadReceivedDelegate {
    boolean invoke(ReceivedPayload<?> receivedPayload);

    Future<?> getResultFuture();
}
