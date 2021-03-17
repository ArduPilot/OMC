/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.INotificationObject;

public interface IPayloadConfiguration extends INotificationObject {

    IPayloadDescription getDescription();

    IPayloadConfiguration deepCopy();

}
