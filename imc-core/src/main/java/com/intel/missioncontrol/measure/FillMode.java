/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

/**
 * Controls how {@link QuantityFormat} handles cases when a formatted number has less digits than the value returned by
 * {@link QuantityFormat#getSignificantDigits()}.
 */
public enum FillMode {
    NO_FILL,
    ZERO_FILL
}
