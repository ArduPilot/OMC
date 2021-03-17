/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

public class AirMapResponse<T> {
    String status;
    T data;

    public T getData() {
        return data;
    }
}
