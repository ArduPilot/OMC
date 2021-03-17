/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.settings;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterestCorner;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Speed;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityArithmetic;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.ReferencePointType;
import com.intel.missioncontrol.settings.ElevationModelSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.controls.QuantitySpinnerValueFactory;
import com.intel.missioncontrol.ui.controls.ToggleButton;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.controls.VariantQuantitySpinnerValueFactory;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.CPhotoSettings;
import eu.mavinci.core.flightplan.FlightplanSpeedModes;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.IOException;
import java.io.InputStream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.asyncfx.beans.property.AsyncBooleanProperty;

public class GeneralSettingsSectionView extends ViewBase<GeneralSettingsSectionViewModel> {

    private static final double SPEED_STEP = 1.0;
    private static final double PERCENT_MIN = 0.0;
    private static final double PERCENT_MAX = 100.0;
    private static final double PERCENT_STEP = 1.0;
    private static final String PREFIX =
        "com.intel.missioncontrol.ui.sidepane.planning.settings.GeneralSettingsSectionView.";
    private static final double MIN_GROUND_SPEED = CPhotoSettings.MIN_SPEED;

    @InjectViewModel
    private GeneralSettingsSectionViewModel viewModel;

    @FXML
    private Control rootNode;

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private Spinner<Quantity<Speed>> maxSpeedSpinner;

    @FXML
    private ComboBox<FlightplanSpeedModes> maxSpeedAutomatic;

    @FXML
    private ComboBox<AltitudeAdjustModes> terrainModeCombobox;

    @FXML
    private ToggleSwitch recalculateSwitch;

    @FXML
    private AutoCommitSpinner<Quantity<Dimension.Percentage>> gsdTolerance;

    @FXML
    public Label gsdToleranceLbl;

    @FXML
    private ComboBox refPoint;

    @FXML
    private ToggleButton chooseRefPointPositionButton;

    @FXML
    private Spinner<VariantQuantity> refPointLatitudeSpinner;

    @FXML
    private Spinner<VariantQuantity> refPointLongitudeSpinner;

    @FXML
    private AutoCommitSpinner refPointElevationSpinner;

    @FXML
    private VBox maxSpeedPane;

    @FXML
    private Button recalculateNowBtn;

    @FXML
    private ToggleSwitch stoppingSwitch;

    @FXML
    public HBox notUsingSurfaceElevationInSettings;

    @FXML
    private ToggleSwitch enableJumpOverWaypoints;

    @Inject
    private ISettingsManager settingsManager;

    private IQuantityStyleProvider quantityStyleProvider;

    private static final double TERRAIN_MODE_ICON_SIZE = 16;
    private ChangeListener<Boolean> useSurfaceDataForPlanningListener;

    @Override
    public void initializeView() {
        super.initializeView();

        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);

        recalculateSwitch.selectedProperty().bindBidirectional(viewModel.recalculateOnEveryChangeProperty());
        stoppingSwitch.selectedProperty().bindBidirectional(viewModel.stopAtWaypointsProperty());
        enableJumpOverWaypoints.selectedProperty().bindBidirectional(viewModel.enableJumpOverWaypointsProperty());

        initSpeedModeCombobox();
        initTerrainModeCombobox();
        initReferencePointControls();

        QuantitySpinnerValueFactory<Speed> factory =
            new QuantitySpinnerValueFactory<>(generalSettings, viewModel.maxGroundSpeedProperty().getUnitInfo(), 1);
        factory.setWrapAround(false);
        factory.setMin(Quantity.of(MIN_GROUND_SPEED, Unit.METER_PER_SECOND));
        factory.maxProperty().bind(viewModel.maxGroundSpeedUpperLimitProperty());
        factory.valueProperty().bindBidirectional(viewModel.maxGroundSpeedProperty());
        maxSpeedSpinner.setValueFactory(factory);

        recalculateNowBtn.disableProperty().bind(viewModel.recalculateOnEveryChangeProperty());

        maxSpeedPane.managedProperty().bind(viewModel.maxSpeedUIVisibleProperty());
        maxSpeedPane.visibleProperty().bind(viewModel.maxSpeedUIVisibleProperty());

        maxSpeedSpinner.editableProperty().bind(viewModel.maxSpeedSpinnerEnabledProperty());
        maxSpeedSpinner.disableProperty().bind(viewModel.maxSpeedSpinnerEnabledProperty().not());

        stoppingSwitch.disableProperty().bind(viewModel.maxSpeedSpinnerEnabledProperty().not());

        chooseRefPointPositionButton.getStyleClass().remove("toggle-button");
    }

    @Override
    public Control getRootNode() {
        return rootNode;
    }

    @Override
    public GeneralSettingsSectionViewModel getViewModel() {
        return viewModel;
    }

    private void initReferencePointControls() {
        int maxAngleFractionDigits = 6;
        int significantAngleDigits = 8;

        var latAngleSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Angle.class,
                Quantity.of(-90, Unit.DEGREE),
                Quantity.of(90, Unit.DEGREE),
                0.00000899823,
                true,
                significantAngleDigits,
                maxAngleFractionDigits);

        var lonAngleSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Angle.class,
                Quantity.of(-180, Unit.DEGREE),
                Quantity.of(180, Unit.DEGREE),
                0.00000899823,
                true,
                significantAngleDigits,
                maxAngleFractionDigits);

        var lengthSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Length.class,
                Quantity.of(-Double.MAX_VALUE, Unit.METER),
                Quantity.of(Double.MAX_VALUE, Unit.METER),
                1,
                true,
                8,
                5);

        ViewHelper.initAutoCommitSpinner(
            gsdTolerance,
            viewModel.gsdToleranceQuantityProperty(),
            Unit.PERCENTAGE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            PERCENT_MIN,
            PERCENT_MAX,
            PERCENT_STEP,
            false);

        gsdTolerance
            .managedProperty()
            .bind(viewModel.selectedTerrainModeProperty().isEqualTo(AltitudeAdjustModes.CONSTANT_OVER_R));
        gsdTolerance
            .visibleProperty()
            .bind(viewModel.selectedTerrainModeProperty().isEqualTo(AltitudeAdjustModes.CONSTANT_OVER_R));
        gsdToleranceLbl
            .managedProperty()
            .bind(viewModel.selectedTerrainModeProperty().isEqualTo(AltitudeAdjustModes.CONSTANT_OVER_R));
        gsdToleranceLbl
            .visibleProperty()
            .bind(viewModel.selectedTerrainModeProperty().isEqualTo(AltitudeAdjustModes.CONSTANT_OVER_R));

        var valueFactory =
            new VariantQuantitySpinnerValueFactory(
                quantityStyleProvider,
                QuantityArithmetic.LATITUDE,
                viewModel.refPointLatitudeProperty(),
                latAngleSettings,
                lengthSettings);

        refPointLatitudeSpinner.setValueFactory(valueFactory);
        refPointLatitudeSpinner.setEditable(true);

        var lonValueFactory =
            new VariantQuantitySpinnerValueFactory(
                quantityStyleProvider,
                QuantityArithmetic.LONGITUDE,
                viewModel.refPointLongitudeProperty(),
                lonAngleSettings,
                lengthSettings);

        refPointLongitudeSpinner.setValueFactory(lonValueFactory);
        refPointLongitudeSpinner.setEditable(true);

        ViewHelper.initAutoCommitSpinner(
            refPointElevationSpinner,
            viewModel.refPointElevationProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            5,
            0.0,
            1000.0,
            1.0,
            false);

        chooseRefPointPositionButton
            .selectedProperty()
            .bindBidirectional(viewModel.chooseRefPointButtonPressedProperty());

        chooseRefPointPositionButton.disableProperty().bind(viewModel.manualRefPointProperty());
        refPointLatitudeSpinner.disableProperty().bind(viewModel.manualRefPointProperty());
        refPointLongitudeSpinner.disableProperty().bind(viewModel.manualRefPointProperty());
        refPointElevationSpinner.disableProperty().bind(viewModel.manualRefPointProperty());
        refPoint.itemsProperty().bind(viewModel.refPointOptionsListProperty());
        refPoint.setConverter(
            new StringConverter<>() {
                @Override
                public String toString(Object object) {
                    if (object != null) {
                        if (object instanceof AreaOfInterestCorner) {
                            AreaOfInterestCorner corner = (AreaOfInterestCorner)object;
                            return corner.getAreaName()
                                + " "
                                + languageHelper.getString(PREFIX + ReferencePointType.VERTEX)
                                + " "
                                + corner.getIndex();
                        } else if (object instanceof ObjectProperty) {
                            return languageHelper.getString(PREFIX + ReferencePointType.TAKEOFF);
                        } else {
                            return languageHelper.getString(PREFIX + ReferencePointType.MANUAL);
                        }
                    }

                    return "";
                }

                @Override
                public Object fromString(String string) {
                    return null;
                }
            });
        refPoint.valueProperty().bindBidirectional(viewModel.selectedRefPointOptionProperty());

        refPointLatitudeSpinner.disableProperty().bind(viewModel.refPointCoordinatesEditableProperty().not());
        refPointLongitudeSpinner.disableProperty().bind(viewModel.refPointCoordinatesEditableProperty().not());
        refPointElevationSpinner.disableProperty().bind(viewModel.refPointCoordinatesEditableProperty().not());
    }

    private void initSpeedModeCombobox() {
        EnumConverter<FlightplanSpeedModes> speedModesEnumConverter =
            new EnumConverter<>(languageHelper, FlightplanSpeedModes.class);
        maxSpeedAutomatic.getItems().addAll(FlightplanSpeedModes.values());
        maxSpeedAutomatic.setConverter(speedModesEnumConverter);
        maxSpeedAutomatic.valueProperty().bindBidirectional(viewModel.selectedMaxGroundSpeedAutomaticProperty());
    }

    private void initTerrainModeCombobox() {
        EnumConverter<AltitudeAdjustModes> terrainModeConverter =
            new EnumConverter<>(languageHelper, AltitudeAdjustModes.class);
        terrainModeCombobox.getItems().addAll(viewModel.getAvailableAltitudeAdjustModes());
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
        terrainModeCombobox.getSelectionModel().selectFirst();
        terrainModeCombobox.valueProperty().bindBidirectional(viewModel.selectedTerrainModeProperty());
        terrainModeCombobox
            .disableProperty()
            .bind(settingsManager.getSection(ElevationModelSettings.class).useSurfaceDataForPlanningProperty().not());

        terrainModeCombobox.valueProperty().bindBidirectional(viewModel.selectedTerrainModeProperty());

        AsyncBooleanProperty useSurfaceModel =
            settingsManager.getSection(ElevationModelSettings.class).useSurfaceDataForPlanningProperty();

        notUsingSurfaceElevationInSettings.managedProperty().bind(useSurfaceModel.not());
        notUsingSurfaceElevationInSettings.visibleProperty().bind(useSurfaceModel.not());

        if (!useSurfaceModel.get()) {
            viewModel.selectedTerrainModeProperty().set(AltitudeAdjustModes.CONSTANT_OVER_R);
        }

        useSurfaceDataForPlanningListener =
            (observable, oldValue, newValue) -> {
                if (newValue == false) {
                    viewModel.selectedTerrainModeProperty().set(AltitudeAdjustModes.CONSTANT_OVER_R);
                } else {
                    viewModel.selectedTerrainModeProperty().set(AltitudeAdjustModes.FOLLOW_TERRAIN);
                }
            };
        useSurfaceModel.addListener(new WeakChangeListener<>(useSurfaceDataForPlanningListener));
    }

    public void recalculateNow(ActionEvent actionEvent) {
        viewModel.recalculateNow();
    }

    @FXML
    public void onToggleChooseRefPositionClicked() {
        viewModel.getToggleChooseRefPointCommand().execute();
    }

    public void navigateToElevationSettings(ActionEvent actionEvent) {
        viewModel.navigateToElevationSettings();
    }

}
