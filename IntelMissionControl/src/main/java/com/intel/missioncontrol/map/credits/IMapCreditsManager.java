/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.credits;

public interface IMapCreditsManager extends IMapCreditsSource {

    void register(IMapCreditsSource source);
}
