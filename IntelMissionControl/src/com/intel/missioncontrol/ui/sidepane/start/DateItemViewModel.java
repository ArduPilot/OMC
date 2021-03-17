/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import de.saxsys.mvvmfx.ViewModel;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;

public class DateItemViewModel implements ViewModel {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("MMMM yyyy").withZone(ZoneId.systemDefault());

    private StringProperty text;

    public DateItemViewModel(Instant date) {
        text = new ReadOnlyStringWrapper(FORMATTER.format(date));
    }

    public ReadOnlyStringProperty textProperty() {
        return text;
    }

}
