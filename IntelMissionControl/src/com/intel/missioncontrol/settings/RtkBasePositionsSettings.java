/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

@SettingsMetadata(section = "rtkBasePositions")
public class RtkBasePositionsSettings implements ISettings {

    private ListProperty<RtkBasePosition> rtkBasePositions =
        new SimpleListProperty<RtkBasePosition>(FXCollections.observableArrayList(RtkBasePosition::getObservables));

    public ListProperty<RtkBasePosition> rtkBasePositionsProperty() {
        return rtkBasePositions;
    }

}
