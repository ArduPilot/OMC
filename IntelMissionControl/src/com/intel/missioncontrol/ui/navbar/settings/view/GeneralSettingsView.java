/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.MapRotationStyle;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.Theme;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.GeneralSettingsViewModel;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class GeneralSettingsView extends ViewBase<GeneralSettingsViewModel> {

    private final ISettingsManager settingsManager;

    @InjectViewModel
    private GeneralSettingsViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Pane layoutRoot;

    @FXML
    private ComboBox<Theme> themesBox;

    @FXML
    private ComboBox<Locale> localesBox;

    //@FXML
    //private ToggleSwitch autoUpdateSwitch;

    @FXML
    private ToggleSwitch updateMapTilesSwitch;

    @FXML
    private ComboBox<OperationLevel> operationLevelsBox;

    @FXML
    private ComboBox<SystemOfMeasurement> unitSystemsBox;

    @FXML
    private Label restartHintLabel;

    @FXML
    private Label labelSpatial;

    @FXML
    private ComboBox<AngleStyle> angleStyleBox;

    @FXML
    private ComboBox<MapRotationStyle> mapInteractionModeBox;

    private final IDialogContextProvider dialogContextProvider;
    private final ILanguageHelper languageHelper;

    @Inject
    public GeneralSettingsView(
            IDialogContextProvider dialogContextProvider,
            ILanguageHelper languageHelper,
            ISettingsManager settingsManager) {
        this.dialogContextProvider = dialogContextProvider;
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        themesBox.setItems(viewModel.availableThemesProperty());
        themesBox.valueProperty().bindBidirectional(viewModel.selectedThemeProperty());
        themesBox.setConverter(new EnumConverter<>(languageHelper, Theme.class));
        operationLevelsBox.itemsProperty().bind(viewModel.availableOperationLevelsProperty());
        operationLevelsBox.valueProperty().bindBidirectional(viewModel.selectedOperationLevelProperty());
        operationLevelsBox.setConverter(new EnumConverter<>(languageHelper, OperationLevel.class));
        operationLevelsBox
            .valueProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // New async properties cause a newValue = null here, which apparently does not work nice
                    // with the ButtonCell
                    if (newValue == null) {
                        var cell = new ComboBoxListCell<>(viewModel.selectedOperationLevelProperty().getValue());
                        cell.setConverter(new EnumConverter<>(languageHelper, OperationLevel.class));
                        operationLevelsBox.setButtonCell(cell);
                    }
                });
        localesBox.itemsProperty().bind(viewModel.availableLocalesProperty());
        localesBox.valueProperty().bindBidirectional(viewModel.selectedLocaleProperty());
        localesBox.setConverter(
            new StringConverter<>() {
                @Override
                public String toString(Locale locale) {
                    if (locale == Locale.ENGLISH) {
                        return locale.getDisplayName(Locale.ENGLISH) + " (Default)";
                    }

                    return locale.getDisplayName(Locale.ENGLISH) + " - " + locale.getDisplayLanguage(locale);
                }

                @Override
                public Locale fromString(String string) {
                    return new Locale(string);
                }
            });

        unitSystemsBox.itemsProperty().bind(viewModel.availableSystemsOfMeasurementProperty());
        unitSystemsBox.valueProperty().bindBidirectional(viewModel.selectedSystemOfMeasurementProperty());
        unitSystemsBox.setConverter(new EnumConverter<>(languageHelper, SystemOfMeasurement.class));

        BindingUtils.bindVisibility(restartHintLabel, viewModel.restartRequiredProperty());

        // autoUpdateSwitch.selectedProperty().bindBidirectional(viewModel.softwareUpdateEnabledProperty());
        updateMapTilesSwitch.selectedProperty().bindBidirectional(viewModel.mapUpdateEnabledProperty());

        labelSpatial.textProperty().bind(viewModel.spatialReferenceProperty());

        angleStyleBox.itemsProperty().bind(viewModel.availableAngleStylesProperty());
        angleStyleBox.valueProperty().bindBidirectional(viewModel.selectedAngleStyleProperty());
        angleStyleBox.setConverter(new EnumConverter<>(languageHelper, AngleStyle.class));

        mapInteractionModeBox.itemsProperty().bind(viewModel.availableMapRotationStylesProperty());
        mapInteractionModeBox.valueProperty().bindBidirectional(viewModel.selectedMapRotationStyleProperty());
        mapInteractionModeBox.setConverter(new EnumConverter<>(languageHelper, MapRotationStyle.class));
    }

    @FXML
    private void onSrsChangeAction() {
        viewModel.getChangeSrsCommand().execute();
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }
}
