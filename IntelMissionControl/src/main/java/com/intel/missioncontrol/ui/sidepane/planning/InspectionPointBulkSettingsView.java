/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityArithmetic;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.controls.QuantitySpinnerValueFactory;
import com.intel.missioncontrol.ui.controls.VariantQuantitySpinnerValueFactory;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import gov.nasa.worldwind.globes.Earth;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class InspectionPointBulkSettingsView extends DialogView<InspectionPointBulkSettingsViewModel> {

    public static final double LAT_LON_STEP = 0.00001;
    public static final double ALT_STEP = 0.00001;
    private final ILanguageHelper languageHelper;
    private final ISettingsManager settingsManager;

    @FXML
    public AnchorPane layoutRoot;

    @FXML
    public CheckBox latCheckbox;

    @FXML
    public AutoCommitSpinner<VariantQuantity> latSpinner;

    @FXML
    public CheckBox lonCheckbox;

    @FXML
    public AutoCommitSpinner<VariantQuantity> lonSpinner;

    @FXML
    public CheckBox altCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Dimension.Length>> altSpinner;

    @FXML
    public CheckBox pitchCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Dimension.Angle>> pitchSpinner;

    @FXML
    public CheckBox yawCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Dimension.Angle>> yawSpinner;

    @FXML
    public ComboBox triggerComboBox;

    @FXML
    public ComboBox targetComboBox;

    @FXML
    public CheckBox resolutionCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Dimension.Length>> resolutionSpinner;

    @FXML
    public CheckBox distanceCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Dimension.Length>> distanceSpinner;

    @FXML
    public CheckBox frameDiagCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Dimension.Length>> frameDiagSpinner;

    @FXML
    public CheckBox rotateCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Dimension.Angle>> rotateSpinner;

    @FXML
    public TextField noteTextField;

    @InjectViewModel
    private InspectionPointBulkSettingsViewModel viewModel;

    @FXML
    private Button applyButton;

    private IDialogService dialogService;

    @Inject
    public InspectionPointBulkSettingsView(
            ILanguageHelper languageHelper, ISettingsManager settingsManager, IDialogService dialogService) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
        this.dialogService = dialogService;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(EditPowerpolePointsView.class.getName() + ".title"));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        final int maxAngleFractionDigits = 6;
        final int significantAngleDigits = 8;
        final var minYaw = Quantity.of(0, Unit.DEGREE);
        final var maxYaw = Quantity.of(360, Unit.DEGREE);

        IQuantityStyleProvider quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        QuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setMaximumFractionDigits(maxAngleFractionDigits);
        quantityFormat.setSignificantDigits(significantAngleDigits);

        lonCheckbox.selectedProperty().bindBidirectional(viewModel.latLonAddCheckedProperty());
        latCheckbox.selectedProperty().bindBidirectional(viewModel.latLonAddCheckedProperty());
        altCheckbox.selectedProperty().bindBidirectional(viewModel.altAddCheckedProperty());
        pitchCheckbox.selectedProperty().bindBidirectional(viewModel.pitchAddCheckedProperty());
        yawCheckbox.selectedProperty().bindBidirectional(viewModel.yawAddCheckedProperty());
        triggerComboBox.valueProperty().bindBidirectional(viewModel.triggerChangeStatusProperty());
        targetComboBox.valueProperty().bindBidirectional(viewModel.targetChangeStatusProperty());
        noteTextField.textProperty().bindBidirectional(viewModel.noteChangeProperty());
        triggerComboBox.itemsProperty().bind(viewModel.availableTriggerChangeTypesProperty());

        targetComboBox.itemsProperty().bind(viewModel.availableTargetChangeTypeProperty());

        resolutionCheckbox.selectedProperty().bindBidirectional(viewModel.resolutionAddCheckedProperty());

        distanceCheckbox.selectedProperty().bindBidirectional(viewModel.distanceAddCheckedProperty());
        frameDiagCheckbox.selectedProperty().bindBidirectional(viewModel.frameDiagAddCheckedProperty());
        rotateCheckbox.selectedProperty().bindBidirectional(viewModel.rotateAddCheckedProperty());

        latCheckbox
            .selectedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // When the checkbox is selected, we want to enter a difference in angle/meters
                    if (newValue) {
                        var latChange = viewModel.latChangeProperty().get();
                        switch (latChange.getDimension()) {
                        case LENGTH:
                            {
                            }

                            break;
                        case ANGLE:
                            {
                                double dLat = latChange.convertTo(Unit.DEGREE).getValue().doubleValue();
                                double dMetre =
                                    Math.log(Math.tan(Math.PI / 4 + Math.toRadians(dLat) / 2))
                                        * Earth.WGS84_EQUATORIAL_RADIUS;
                                viewModel.latChangeProperty().set(VariantQuantity.of(dMetre, Unit.METER));
                            }

                            break;
                        default:
                            // do nothing
                            break;
                        }
                    } else {
                        viewModel.latChangeProperty().set(VariantQuantity.of(0, Unit.DEGREE));
                    }
                });

        lonCheckbox
            .selectedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // When the checkbox is selected, we want to enter a difference in angle/meters
                    if (newValue) {
                        var lonChange = viewModel.lonChangeProperty().get();
                        switch (lonChange.getDimension()) {
                        case LENGTH:
                            {
                            }

                            break;
                        case ANGLE:
                            {
                                double dLon = lonChange.convertTo(Unit.DEGREE).getValue().doubleValue();
                                double dMetre = Math.toRadians(dLon) * Earth.WGS84_EQUATORIAL_RADIUS;
                                viewModel.lonChangeProperty().set(VariantQuantity.of(dMetre, Unit.METER));
                            }

                            break;
                        default:
                            // do nothing
                            break;
                        }
                    } else {
                        viewModel.lonChangeProperty().set(VariantQuantity.of(0, Unit.DEGREE));
                    }
                });

        var angleSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Angle.class,
                Quantity.of(-360, Unit.DEGREE),
                Quantity.of(360, Unit.DEGREE),
                1,
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

        {
            var valueFactory =
                new VariantQuantitySpinnerValueFactory(
                    quantityStyleProvider,
                    QuantityArithmetic.LATITUDE,
                    viewModel.latChangeProperty(),
                    angleSettings,
                    lengthSettings);

            latSpinner.setValueFactory(valueFactory);
            latSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new VariantQuantitySpinnerValueFactory(
                    quantityStyleProvider,
                    QuantityArithmetic.LONGITUDE,
                    viewModel.lonChangeProperty(),
                    angleSettings,
                    lengthSettings);
            lonSpinner.setValueFactory(valueFactory);
            lonSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.altChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    Quantity.of(-Double.MAX_VALUE, Unit.METER),
                    Quantity.of(Double.MAX_VALUE, Unit.METER),
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.altChangeProperty());
            altSpinner.setValueFactory(valueFactory);
            altSpinner.setEditable(true);
        }

        {
            var rageBearing = Quantity.of(360, Unit.DEGREE);
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.rollChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    rageBearing.multiply(-1),
                    rageBearing,
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.rotateAddChangeProperty());
            rotateSpinner.setValueFactory(valueFactory);
            rotateSpinner.setEditable(true);
        }

        {
            var ragePitch = Quantity.of(360, Unit.DEGREE);
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.pitchChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    ragePitch.multiply(-1),
                    ragePitch,
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.pitchChangeProperty());
            pitchSpinner.setValueFactory(valueFactory);
            pitchSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.yawChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    maxYaw.multiply(-1),
                    maxYaw,
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.yawChangeProperty());
            yawSpinner.setValueFactory(valueFactory);
            yawSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.altChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    Quantity.of(-Double.MAX_VALUE, Unit.METER),
                    Quantity.of(Double.MAX_VALUE, Unit.METER),
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.resolutionAddChangeProperty());
            resolutionSpinner.setValueFactory(valueFactory);
            resolutionSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.altChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    Quantity.of(-Double.MAX_VALUE, Unit.METER),
                    Quantity.of(Double.MAX_VALUE, Unit.METER),
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.distanceAddChangeProperty());
            distanceSpinner.setValueFactory(valueFactory);
            distanceSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.altChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    Quantity.of(-Double.MAX_VALUE, Unit.METER),
                    Quantity.of(Double.MAX_VALUE, Unit.METER),
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.frameDiagAddChangeProperty());
            frameDiagSpinner.setValueFactory(valueFactory);
            frameDiagSpinner.setEditable(true);
        }

        applyButton.disableProperty().bind(viewModel.getApplyChangesCommand().notExecutableProperty());
    }

    public void deleteAll(ActionEvent actionEvent) {
        viewModel.getDeleteSelectedAreaOfInterestCornersCommand().execute();
    }

    public void applyToAll(ActionEvent actionEvent) {
        viewModel.getApplyChangesCommand().execute();
    }

    public void cancel(ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }
}
