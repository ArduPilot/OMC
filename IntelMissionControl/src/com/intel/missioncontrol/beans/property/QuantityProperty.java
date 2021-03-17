/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.UnitInfo;
import javafx.beans.property.Property;

public interface QuantityProperty<Q extends Quantity<Q>> extends Property<Quantity<Q>> {

    UnitInfo<Q> getUnitInfo();

    void set(Quantity<Q> newValue);

    Quantity<Q> get();

}
