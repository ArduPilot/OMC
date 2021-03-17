/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

public class MatchingsTableRowData {

    private final ObjectProperty<File> matchingFolder = new SimpleObjectProperty<>();
    private final StringProperty matchingName = new SimpleStringProperty();

    private ObjectProperty<MatchingImagesUsage> matchingImageUsage = new SimpleObjectProperty<>();

    public MatchingsTableRowData(String matchingName, File matchingFolder) {
        this.matchingName.setValue(matchingName);
        this.matchingFolder.setValue(matchingFolder);
    }

    public StringProperty matchingNameProperty() {
        return matchingName;
    }

    public ObjectProperty<MatchingImagesUsage> matchingImagesUsageProperty() {
        return matchingImageUsage;
    }

    public MatchingImagesUsage getMatchingImagesUsage() {
        return matchingImageUsage.get();
    }

    public File getMatchingFolder() {
        return matchingFolder.get();
    }

}
