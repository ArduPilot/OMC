/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

public interface IResolveAction {

    String getMessage();

    boolean canResolve();

    void resolve();

}
