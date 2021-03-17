/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

public enum ContinuationOption {
    ALWAYS,
    ONLY_ON_SUCCESS,
    ONLY_ON_FAILURE,
    ONLY_ON_CANCELLED,
    NOT_ON_SUCCESS,
    NOT_ON_FAILURE,
    NOT_ON_CANCELLED;

    boolean continueOnSuccess() {
        return this == ALWAYS || this == ONLY_ON_SUCCESS || this == NOT_ON_FAILURE || this == NOT_ON_CANCELLED;
    }

    boolean continueOnFailure() {
        return this == ALWAYS || this == ONLY_ON_FAILURE || this == NOT_ON_SUCCESS || this == NOT_ON_CANCELLED;
    }

    boolean continueOnCancelled() {
        return this == ALWAYS || this == ONLY_ON_CANCELLED || this == NOT_ON_SUCCESS || this == NOT_ON_FAILURE;
    }
}
