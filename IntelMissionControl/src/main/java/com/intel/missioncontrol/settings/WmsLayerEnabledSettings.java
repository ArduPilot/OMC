package com.intel.missioncontrol.settings;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;

@Serializable
public class WmsLayerEnabledSettings {

    private final AsyncStringProperty name;
    private AsyncBooleanProperty enabled;

    public WmsLayerEnabledSettings() {
        this.name = new SimpleAsyncStringProperty(this);
        this.enabled =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    }

    public WmsLayerEnabledSettings(String name, AsyncBooleanProperty enabledProperty) {
        this.name = new SimpleAsyncStringProperty(this);
        this.name.setValue(name);
        this.enabled = enabledProperty;
    }

    public AsyncStringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public AsyncBooleanProperty enabledProperty() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

}
