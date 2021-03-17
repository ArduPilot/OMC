/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.credits;

import com.intel.missioncontrol.beans.property.AsyncListProperty;

public interface IMapCreditsSource {

    AsyncListProperty<MapCreditViewModel> mapCreditsProperty();
}
