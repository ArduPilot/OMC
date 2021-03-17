/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.UIAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.UIAsyncStringProperty;
import com.intel.missioncontrol.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

abstract class LayerViewModel implements ViewModel {

    final UIAsyncStringProperty tooltip = new UIAsyncStringProperty(this);
    final UIAsyncBooleanProperty selected = new UIAsyncBooleanProperty(this);

    private final UIAsyncStringProperty name = new UIAsyncStringProperty(this);
    private final AsyncBooleanProperty canShowOnMap = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty canDelete = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty hasSettings = new SimpleAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty hasWarning = new UIAsyncBooleanProperty(this);
    private final UIAsyncStringProperty warning = new UIAsyncStringProperty(this);

    private final ICommand showOnMapCommand = new DelegateCommand(this::onZoomTo, canShowOnMap);
    private final ICommand deleteCommand = new DelegateCommand(this::onDelete, canDelete);
    private final ICommand openSettingsCommand = new DelegateCommand(this::onOpenSettings, hasSettings);
    private final ICommand resolveWarningCommand = new DelegateCommand(this::onResolveWarning, hasWarning);

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

    public final ICommand deleteCommand() {
        return deleteCommand;
    }

    public final ICommand showOnMapCommand() {
        return showOnMapCommand;
    }

    public final ICommand openSettingsCommand() {
        return openSettingsCommand;
    }

    public final ICommand resolveWarningCommand() {
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
