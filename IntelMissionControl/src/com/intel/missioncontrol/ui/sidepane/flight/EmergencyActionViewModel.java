/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** @author Vladimir Iordanov */
public class EmergencyActionViewModel extends AlertAwareViewModel {

    public static final String TITLE_DETAILS_PART_2_KEY =
        "com.intel.missioncontrol.ui.flight.EmergencyActionView.titleDetails.part2";
    public static final String TITLE_DETAILS_PART_2_UNKNOWN_KEY =
        "com.intel.missioncontrol.ui.flight.EmergencyActionView.titleDetails.part2.unknown";

    private final StringProperty titleDetailsPart2 = new SimpleStringProperty();

    public String getTitleDetailsPart2() {
        return titleDetailsPart2.get();
    }

    public StringProperty titleDetailsPart2Property() {
        return titleDetailsPart2;
    }
}
