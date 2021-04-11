package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.SuppressLinter;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

@SuppressLinter(
    value = "IllegalViewModelMethod",
    reviewer = "mstrauss",
    justification = "Class needs to make methods accessible to subclasses."
)
abstract class LayerViewModel implements ViewModel {

    final UIAsyncStringProperty tooltip = new UIAsyncStringProperty(this);
    final UIAsyncBooleanProperty selected = new UIAsyncBooleanProperty(this);

    private final UIAsyncStringProperty name = new UIAsyncStringProperty(this);
    private final AsyncBooleanProperty canShowOnMap = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty canDelete = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty hasSettings = new SimpleAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty hasWarning = new UIAsyncBooleanProperty(this);
    private final UIAsyncStringProperty warning = new UIAsyncStringProperty(this);

    private final Command showOnMapCommand = new DelegateCommand(this::onZoomTo, canShowOnMap);
    private final Command deleteCommand = new DelegateCommand(this::onDelete, canDelete);
    private final Command openSettingsCommand = new DelegateCommand(this::onOpenSettings, hasSettings);
    private final Command resolveWarningCommand = new DelegateCommand(this::onResolveWarning, hasWarning);

    LayerViewModel(String name) {
        this.name.overrideMetadata(new UIPropertyMetadata.Builder<String>().initialValue(name).create());
    }

    LayerViewModel(ILayer layer, ILanguageHelper languageHelper) {
        name.bind(layer.nameProperty(), layerName -> layerName.toString(languageHelper));
        selected.bindBidirectional(layer.enabledProperty());
    }

    public ReadOnlyProperty<String> nameProperty() {
        return name;
    }

    public ReadOnlyProperty<String> tooltipProperty() {
        return tooltip;
    }

    public Property<Boolean> selectedProperty() {
        return selected;
    }

    public abstract ReadOnlyProperty<Boolean> visibleProperty();

    public final Command getDeleteCommand() {
        return deleteCommand;
    }

    public final Command getShowOnMapCommand() {
        return showOnMapCommand;
    }

    public final Command getOpenSettingsCommand() {
        return openSettingsCommand;
    }

    public final Command getResolveWarningCommand() {
        return resolveWarningCommand;
    }

    void onZoomTo() {}

    void onDelete() {}

    void onOpenSettings() {}

    void onResolveWarning() {}

    void setCanShowOnMap(boolean b) {
        canShowOnMap.set(b);
    }

    void setCanDelete(boolean b) {
        canDelete.set(b);
    }

    void setHasSettings(boolean b) {
        hasSettings.set(b);
    }

    public UIAsyncStringProperty warningProperty() {
        return warning;
    }

    public UIAsyncBooleanProperty hasWarningProperty() {
        return hasWarning;
    }

}
