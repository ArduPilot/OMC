/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import java.time.LocalDateTime;

@Serializable
public class KmlSettings {

    public enum ResourceType {
        KML,
        SHP
    }

    private final AsyncObjectProperty<ResourceType> type = new SimpleAsyncObjectProperty<>(this);
    private final AsyncStringProperty resource = new SimpleAsyncStringProperty(this);
    private final AsyncBooleanProperty enabled = new SimpleAsyncBooleanProperty(this);
    private final AsyncObjectProperty<LocalDateTime> loadTime = new SimpleAsyncObjectProperty<>(this);

    public AsyncStringProperty resourceProperty() {
        return resource;
    }

    public AsyncObjectProperty<ResourceType> typeProperty() {
        return type;
    }

    public AsyncObjectProperty<LocalDateTime> loadTimeProperty() {
        return loadTime;
    }

    public AsyncBooleanProperty enabledProperty() {
        return enabled;
    }

    @Override
    public boolean equals(Object obj) {
        return resource.get().equals(((KmlSettings)obj).resourceProperty().get());
    }

}
