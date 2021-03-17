/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;

@SettingsMetadata(section = "falconDataExportSettings")
public class FalconDataExportSettings implements ISettings {

    private static final String NO_PATH_DEFINED_STRING = "-";

    private AsyncStringProperty falcon8PathProperty =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(NO_PATH_DEFINED_STRING).create());

    private AsyncStringProperty falcon8MapExportPathProperty =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(NO_PATH_DEFINED_STRING).create());

    private AsyncStringProperty falcon8CsvExportPathProperty =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(NO_PATH_DEFINED_STRING).create());

    private AsyncStringProperty falcon8PlusPathProperty =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(NO_PATH_DEFINED_STRING).create());

    private AsyncStringProperty falcon8PlusAcpExportProperty =
        new SimpleAsyncStringProperty(
            this, new PropertyMetadata.Builder<String>().initialValue(NO_PATH_DEFINED_STRING).create());

    public AsyncStringProperty falcon8PathProperty() {
        return falcon8PathProperty;
    }

    public AsyncStringProperty falcon8MapExportPathProperty() {
        return falcon8MapExportPathProperty;
    }

    public AsyncStringProperty falcon8CsvExportPathProperty() {
        return falcon8CsvExportPathProperty;
    }

    public AsyncStringProperty falcon8PlusPathProperty() {
        return falcon8PlusPathProperty;
    }

    public AsyncStringProperty falcon8PlusAcpExportProperty() {
        return falcon8PlusAcpExportProperty;
    }

    public String getFalcon8Path() {
        return falcon8PathProperty.get();
    }

    public String getFalcon8MapExportPath() {
        return falcon8MapExportPathProperty.get();
    }

    public String getFalcon8CsvExportPath() {
        return falcon8CsvExportPathProperty.get();
    }

    public String getFalcon8PlusPath() {
        return falcon8PlusPathProperty.get();
    }

    public String getFalcon8PlusAcpExportPath() {
        return falcon8PlusAcpExportProperty.get();
    }

}
