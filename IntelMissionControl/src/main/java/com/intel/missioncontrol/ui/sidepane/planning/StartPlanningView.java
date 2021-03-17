/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ElevationModelSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.controls.ActivityButton;
import com.intel.missioncontrol.ui.controls.MenuButton;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.concurrent.Dispatcher;

public class StartPlanningView extends FancyTabView<StartPlanningViewModel> {
    private static final double TERRAIN_MODE_ICON_SIZE = 16;

    @InjectViewModel
    private StartPlanningViewModel viewModel;

    @InjectContext
    private Context context;

    @Inject
    private IDialogContextProvider dialogContextProvider;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private IApplicationContext applicationContext;

    @FXML
    private Label projectNameLabel;

    @FXML
    private ActivityButton startPlanningButton;

    @FXML
    private ComboBox<FlightPlanTemplate> templateComboBox;

    @FXML
    private ComboBox<AltitudeAdjustModes> terrainModeCombobox;

    @FXML
    private MenuButton flightPlansMenuButton;

    @FXML
    private HBox notUsingSurfaceElevationInSettings;

    @FXML
    private Button templateSettingsButton;

    private ChangeListener<Boolean> useSurfaceDataForPlanningListener;

    @Override
    protected void initializeView() {
        super.initializeView();

        templateSettingsButton
            .visibleProperty()
            .bind(
                settingsManager
                    .getSection(GeneralSettings.class)
                    .operationLevelProperty()
                    .isEqualTo(OperationLevel.DEBUG));
        templateSettingsButton.managedProperty().bind(templateSettingsButton.visibleProperty());

        initTerrainModeCombobox();
        dialogContextProvider.setContext(viewModel, context);
        this.applicationContext = applicationContext;
        projectNameLabel.textProperty().bind(viewModel.missionNameProperty());
        startPlanningButton.isBusyProperty().bind(viewModel.isBusyProperty());

        SimpleListProperty<FlightPlanTemplate> availableTemplates =
            new SimpleListProperty<>(FXCollections.observableArrayList());
        templateComboBox.setItems(availableTemplates);
        availableTemplates.bindContent(viewModel.availableTemplatesProperty());
        templateComboBox.valueProperty().bindBidirectional(viewModel.selectedTemplateProperty());
        templateComboBox.getSelectionModel().selectFirst();
        templateComboBox.setConverter(
            new StringConverter<FlightPlanTemplate>() {

                @Override
                public String toString(FlightPlanTemplate template) {
                    FlightPlanTemplateManagementItem templateItem =
                        new FlightPlanTemplateManagementItem(template, languageHelper);

                    return templateItem.getUav()
                        + " - "
                        + templateItem.getCamera()
                        + " - "
                        + templateItem.getLens()
                        + (StringUtils.isBlank(templateItem.getFpName())
                            ? ""
                            : (" (" + templateItem.getFpName() + ")"));
                }

                @Override
                public FlightPlanTemplate fromString(String string) {
                    return null;
                }
            });

        templateComboBox.setOnShowing(
            new EventHandler<Event>() {
                public void handle(Event e) {
                    ComboBoxListViewSkin skin = (ComboBoxListViewSkin)templateComboBox.getSkin();
                    if (skin != null) {
                        ListView<?> list = (ListView<?>)skin.getPopupContent();
                        if (list != null) {
                            list.prefWidthProperty().bind(templateComboBox.widthProperty());
                        }
                    }
                }
            });

        flightPlansMenuButton.setModel(viewModel.getFlightPlanMenuModel());
    }

    private void initTerrainModeCombobox() {
        EnumConverter<AltitudeAdjustModes> terrainModeConverter =
            new EnumConverter<>(languageHelper, AltitudeAdjustModes.class);
        terrainModeCombobox
            .getItems()
            .addAll(Arrays.asList(AltitudeAdjustModes.CONSTANT_OVER_R, AltitudeAdjustModes.FOLLOW_TERRAIN));
        terrainModeCombobox.setConverter(terrainModeConverter);
        terrainModeCombobox.setCellFactory(
            param ->
                new ListCell<AltitudeAdjustModes>() {
                    private final ImmutableMap<AltitudeAdjustModes, String> itemIcons =
                        ImmutableMap.<AltitudeAdjustModes, String>builder()
                            .put(
                                AltitudeAdjustModes.CONSTANT_OVER_R,
                                "/com/intel/missioncontrol/icons/icon_alt_constant.svg")
                            .put(
                                AltitudeAdjustModes.FOLLOW_TERRAIN,
                                "/com/intel/missioncontrol/icons/icon_alt_linear.svg")
                            .build();

                    @Override
                    protected void updateItem(AltitudeAdjustModes item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            try (InputStream iconInput =
                                Thread.currentThread()
                                    .getContextClassLoader()
                                    .getResourceAsStream(itemIcons.get(item))) {
                                ImageView icon = new ImageView(new Image(iconInput));
                                icon.setFitHeight(TERRAIN_MODE_ICON_SIZE);
                                icon.setFitWidth(TERRAIN_MODE_ICON_SIZE);
                                icon.setPreserveRatio(true);
                                setGraphic(icon);
                            } catch (IOException e) {
                                Debug.getLog().log(Debug.WARNING, "GeneralSettingsSectionView: ", e);
                            }

                            String text = terrainModeConverter.toString(item);
                            setText(text);
                        }
                    }
                });

        AsyncBooleanProperty useSurfaceModel =
            settingsManager.getSection(ElevationModelSettings.class).useSurfaceDataForPlanningProperty();

        terrainModeCombobox.valueProperty().bindBidirectional(viewModel.selectedTerrainModeProperty());

        useSurfaceDataForPlanningListener =
            (obj, oldVal, newVal) -> {
                setupTerrainModeCombo(newVal);
            };
        useSurfaceModel.addListener(new WeakChangeListener<>(useSurfaceDataForPlanningListener), Dispatcher.platform());

        notUsingSurfaceElevationInSettings
            .managedProperty()
            .bind(settingsManager.getSection(ElevationModelSettings.class).useSurfaceDataForPlanningProperty().not());
        notUsingSurfaceElevationInSettings
            .visibleProperty()
            .bind(settingsManager.getSection(ElevationModelSettings.class).useSurfaceDataForPlanningProperty().not());
        setupTerrainModeCombo(useSurfaceModel.get());
    }

    private void setupTerrainModeCombo(Boolean newVal) {
        if (!newVal.booleanValue()) {
            terrainModeCombobox.getSelectionModel().select(AltitudeAdjustModes.CONSTANT_OVER_R);
            terrainModeCombobox.setDisable(true);
        } else {
            terrainModeCombobox.getSelectionModel().select(AltitudeAdjustModes.FOLLOW_TERRAIN);
            terrainModeCombobox.setDisable(false);
        }
    }

    @Override
    protected StartPlanningViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    private void showFpTemplateManagement() {
        viewModel.getShowTemplateManagementCommand().execute();
    }

    @FXML
    public void handleTemplateButton(ActionEvent event) {
        viewModel.getOpenAoiScreenCommand().execute();
    }

    public void navigateToElevationSettings(ActionEvent actionEvent) {
        viewModel.navigateToElevationSettings();
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }

}
