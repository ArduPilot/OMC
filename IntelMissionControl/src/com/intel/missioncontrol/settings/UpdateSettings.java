/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.ui.update.DownloadedUpdate;
import eu.mavinci.core.update.EnumUpdateTargets;
import java.time.LocalDateTime;
import java.util.Map;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

@Deprecated(since = "Switch to async properties")
@SettingsMetadata(section = "updates")
public class UpdateSettings implements ISettings {

    private MapProperty<EnumUpdateTargets, Integer> skippedVersions =
        new SimpleMapProperty<>(FXCollections.observableHashMap());

    public MapProperty<EnumUpdateTargets, Integer> skippedVersionsProperty() {
        return skippedVersions;
    }

    private MapProperty<EnumUpdateTargets, DownloadedUpdate> downloadedFiles =
        new SimpleMapProperty<>(FXCollections.observableHashMap());

    public Map<EnumUpdateTargets, DownloadedUpdate> downloadedFilesProperty() {
        return downloadedFiles;
    }

    private SimpleObjectProperty<LocalDateTime> lastCheckedDate = new SimpleObjectProperty<>();

    public SimpleObjectProperty<LocalDateTime> lastCheckedDatePropertry() {
        return lastCheckedDate;
    }

}
