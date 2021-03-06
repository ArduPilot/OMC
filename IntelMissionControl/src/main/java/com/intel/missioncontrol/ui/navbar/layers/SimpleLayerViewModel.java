package com.intel.missioncontrol.ui.navbar.layers;

import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyProperty;

class SimpleLayerViewModel extends LayerViewModel {

    private final UIAsyncBooleanProperty visible =
        new UIAsyncBooleanProperty(this, new UIPropertyMetadata.Builder<Boolean>().initialValue(true).create());

    public SimpleLayerViewModel(String name) {
        super(name);
    }

    public SimpleLayerViewModel(String name, BooleanBinding visible) {
        super(name);
        this.visible.bind(visible);
    }

    public SimpleLayerViewModel(ILayer layer, ILanguageHelper languageHelper) {
        super(layer, languageHelper);
        visible.bind(layer.internalProperty().not());
    }

    @Override
    public ReadOnlyProperty<Boolean> visibleProperty() {
        return visible;
    }

}
