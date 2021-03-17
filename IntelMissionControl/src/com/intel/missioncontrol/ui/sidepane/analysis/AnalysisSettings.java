/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.SettingsMetadata;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/** Settings related to Analysis module */
@Deprecated(since = "Switch to async properties")
@SettingsMetadata(section = "analysis")
public class AnalysisSettings implements ISettings {
    public static final int DEFAULT_BACKGROUND_TASK_HINT_TTL = 5;

    private final BooleanProperty dataTransferPopupEnabled = new SimpleBooleanProperty(true);

    private final IntegerProperty backgroundTaskHintTtl = new SimpleIntegerProperty(DEFAULT_BACKGROUND_TASK_HINT_TTL);
    private final ObjectProperty<ExportTypes> datasetExportType = new SimpleObjectProperty<>();

    public ObjectProperty<ExportTypes> datasetExportTypeProperty() {
        return datasetExportType;
    }

    public BooleanProperty dataTransferPopupEnabledProperty() {
        return dataTransferPopupEnabled;
    }


    public int getBackgroundTaskHintTtl() {
        return backgroundTaskHintTtl.get();
    }

    public IntegerProperty backgroundTaskHintTtlProperty() {
        return backgroundTaskHintTtl;
    }

    @Override
    public void onLoaded() {
        if (datasetExportType.get() == null) {
            datasetExportType.set(ExportTypes.CSV);
        }
    }

}
