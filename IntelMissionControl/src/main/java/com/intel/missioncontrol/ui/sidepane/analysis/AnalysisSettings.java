/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncLongProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncLongProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.SettingsMetadata;
import javafx.beans.binding.Bindings;
import org.apache.commons.lang3.StringUtils;

/** Settings related to Analysis module */
@SettingsMetadata(section = "analysis")
public class AnalysisSettings implements ISettings {
    public static final int DEFAULT_BACKGROUND_TASK_HINT_TTL = 5;

    private final AsyncBooleanProperty dataTransferPopupEnabled =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncIntegerProperty backgroundTaskHintTtl =
        new SimpleAsyncIntegerProperty(
            this, new PropertyMetadata.Builder<Number>().initialValue(DEFAULT_BACKGROUND_TASK_HINT_TTL).create());
    private final AsyncObjectProperty<ExportTypes> datasetExportType = new SimpleAsyncObjectProperty<ExportTypes>(this);
    private final AsyncStringProperty insightUsername = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty insightPassword = new SimpleAsyncStringProperty(this);
    private final AsyncLongProperty insightLatestDataOrderDownload = new SimpleAsyncLongProperty(this);

    private transient AsyncBooleanProperty insightLoggedIn = new SimpleAsyncBooleanProperty(this);

    @Override
    public void onLoaded() {
        if (datasetExportType.get() == null) {
            datasetExportType.set(ExportTypes.CSV);
        }

        // this later on needs more advanced handling, but this is good for now
        insightLoggedIn.bind(
            Bindings.createBooleanBinding(
                () -> StringUtils.isNotBlank(getInsightUsername()) && StringUtils.isNotBlank(getInsightPassword()),
                insightLoggedIn,
                insightUsername));
    }

    public AsyncObjectProperty<ExportTypes> datasetExportTypeProperty() {
        return datasetExportType;
    }

    public AsyncBooleanProperty dataTransferPopupEnabledProperty() {
        return dataTransferPopupEnabled;
    }

    public int getBackgroundTaskHintTtl() {
        return backgroundTaskHintTtl.get();
    }

    public AsyncIntegerProperty backgroundTaskHintTtlProperty() {
        return backgroundTaskHintTtl;
    }

    public AsyncStringProperty insightPasswordProperty() {
        return insightPassword;
    }

    public AsyncStringProperty insightUsernameProperty() {
        return insightUsername;
    }

    public String getInsightPassword() {
        return insightPassword.get();
    }

    public String getInsightUsername() {
        return insightUsername.get();
    }

    public AsyncBooleanProperty insightLoggedInProperty() {
        return insightLoggedIn;
    }

    public Boolean getInsightLoggedIn() {
        return insightLoggedIn.get();
    }

    public AsyncLongProperty insightLatestDataOrderDownloadProperty() {
        return insightLatestDataOrderDownload;
    }

    public Number getInsightLatestDataOrderDownload() {
        return insightLatestDataOrderDownload.get();
    }

}
