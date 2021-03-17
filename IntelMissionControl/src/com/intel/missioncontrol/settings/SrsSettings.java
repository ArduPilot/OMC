/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;

@SettingsMetadata(section = "srs")
public class SrsSettings implements ISettings {

    private final transient AsyncObjectProperty<MSpatialReference> applicationSrs =
        new SimpleAsyncObjectProperty<>(this);

    private final AsyncStringProperty id = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty wkt = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty origin = new SimpleAsyncStringProperty(this);

    public SrsSettings() {
        applicationSrs.addListener(
            ((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.id.equals(getId())) {
                    id.setValue(newValue.id);
                    name.set(newValue.name);
                    wkt.set(newValue.wkt);
                    origin.set(newValue.getOrigin().toString());
                }
            }));
    }

    public AsyncObjectProperty<MSpatialReference> applicationSrsProperty() {
        return applicationSrs;
    }

    public MSpatialReference getApplicationSrs() {
        return applicationSrs.get();
    }

    public AsyncStringProperty idProperty() {
        return id;
    }

    public String getId() {
        return id.get();
    }
}
