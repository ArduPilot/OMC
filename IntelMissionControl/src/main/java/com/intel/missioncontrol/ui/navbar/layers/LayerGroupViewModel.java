package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.LayerGroupType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

class LayerGroupViewModel extends LayerViewModel {

    final UIAsyncListProperty<LayerViewModel> subLayerItems =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<LayerViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final UIAsyncBooleanProperty visible =
        new UIAsyncBooleanProperty(this, new UIPropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final boolean initiallyExpanded;
    private final LayerGroup.ToggleHint toggleHint;

    public LayerGroupViewModel(ILayer layer, ILanguageHelper languageHelper, boolean initiallyExpanded) {
        super(layer, languageHelper);
        this.initiallyExpanded = initiallyExpanded;
        this.toggleHint = LayerGroup.ToggleHint.ANY;
    }

    public LayerGroupViewModel(
            LayerGroup layerGroup, ILanguageHelper languageHelper, boolean bindSubLayers, boolean initiallyExpanded) {
        super(layerGroup, languageHelper);
        this.initiallyExpanded = initiallyExpanded;
        this.toggleHint = layerGroup.getSelectionBehavior();

        // if you start the app first time with no cache and no internet, default WMS has 0 sublayers,
        // but later when you are online - you can refresh the wms
        visible.bind(
            Bindings.createBooleanBinding(
                () ->
                    !layerGroup.internalProperty().get()
                        && (!subLayerItems.isEmpty() || layerGroup.getType().equals(LayerGroupType.WMS_SERVER_GROUP)),
                layerGroup.internalProperty(),
                subLayerItems));

        if (bindSubLayers) {
            subLayerItems.bindContent(
                layerGroup.subLayersProperty(), layer -> new SimpleLayerViewModel(layer, languageHelper));
        }
    }

    @Override
    public ReadOnlyProperty<Boolean> visibleProperty() {
        return visible;
    }

    public ReadOnlyListProperty<LayerViewModel> subLayerItemsProperty() {
        return subLayerItems.getReadOnlyProperty();
    }

    public final boolean isInitiallyExpanded() {
        return initiallyExpanded;
    }

    public LayerGroup.ToggleHint getToggleHint() {
        return toggleHint;
    }

}
