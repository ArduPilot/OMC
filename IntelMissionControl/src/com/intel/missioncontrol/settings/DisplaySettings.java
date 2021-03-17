/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.StereoMode;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@SettingsMetadata(section = "display")
public class DisplaySettings implements ISettings {

    public class WorkflowHints {

        private AsyncBooleanProperty addAreaOfInterest =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

        private AsyncBooleanProperty srsCheckEnabled =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

        private AsyncBooleanProperty dataTransfer =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

        public AsyncBooleanProperty addAreaOfInterestProperty() {
            return this.addAreaOfInterest;
        }

        public AsyncBooleanProperty srsCheckEnabledProperty() {
            return srsCheckEnabled;
        }

        public AsyncBooleanProperty dataTransferProperty() {
            return dataTransfer;
        }
    }

    private final WorkflowHints workflowHints = new WorkflowHints();

    private final AsyncObjectProperty<StereoMode> stereoMode =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<StereoMode>().initialValue(StereoMode.NONE).create());

    private final AsyncBooleanProperty realisticLightEnabled =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty showPreviewImagesEnabled =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty introductionEnabled =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty showNews =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncStringProperty newsHash = new SimpleAsyncStringProperty(this);

    private final transient BooleanProperty restartRequired = new SimpleBooleanProperty(this, "restartRequired");

    public DisplaySettings() {
        this.stereoMode.addListener(
            (observable, oldValue, newValue) -> {
                this.restartRequired.set(true);
            });
    }

    @Override
    public void onLoaded() {
        this.restartRequired.set(false);
    }

    public WorkflowHints getWorkflowHints() {
        return workflowHints;
    }

    public AsyncObjectProperty<StereoMode> stereoModeProperty() {
        return this.stereoMode;
    }

    public AsyncBooleanProperty realisticLightEnabledProperty() {
        return this.realisticLightEnabled;
    }

    public AsyncBooleanProperty showPreviewImagesEnabledProperty() {
        return this.showPreviewImagesEnabled;
    }

    public AsyncBooleanProperty introductionEnabledProperty() {
        return introductionEnabled;
    }

    public AsyncBooleanProperty showNewsProperty() {
        return showNews;
    }

    public AsyncStringProperty newsHashProperty() {
        return newsHash;
    }

    public BooleanProperty restartRequiredProperty() {
        return this.restartRequired;
    }

}
