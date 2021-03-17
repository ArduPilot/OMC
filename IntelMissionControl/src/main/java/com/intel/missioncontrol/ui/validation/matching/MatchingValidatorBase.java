/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.matching;

import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

abstract class MatchingValidatorBase extends ValidatorBase<Matching> {

    private final InvalidationListener dependenciesListener = observable -> invalidate();

    MatchingValidatorBase(Matching matching, IQuantityStyleProvider quantityStyleProvider) {
        super(matching, quantityStyleProvider);
    }

    void addDependencies(Observable... dependencies) {
        for (Observable o : dependencies) {
            o.addListener(dependenciesListener);
        }
    }

}
