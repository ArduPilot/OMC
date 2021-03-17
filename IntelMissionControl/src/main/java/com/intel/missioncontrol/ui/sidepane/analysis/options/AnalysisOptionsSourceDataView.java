/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.common.hardware.HardwareSelectionView;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.sidepane.analysis.AreaFilterView;
import com.intel.missioncontrol.ui.sidepane.analysis.AreaFilterViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class AnalysisOptionsSourceDataView extends ViewBase<AnalysisOptionsSourceDataViewModel> {

    private static final String RANGE_LABEL_FORMAT =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.rangeFormat";
    private static final double ANGLE_STEP = 1.0;
    private static final double ALT_MIN = 0.0;
    private static final double ALT_MAX = 10000.0;
    private static final double ALT_STEP = 1.0;
    private static final int FRACTION_DIGITS = 1;

    @InjectViewModel
    private AnalysisOptionsSourceDataViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private ItemsView<AreaFilterViewModel> areaFilterListView;

    @FXML
    private ToggleSwitch filterSwitch;

    @FXML
    private TitledPane imageParametersFilterPane;

    @FXML
    private TitledPane areaFilterPane;

    @FXML
    private CheckBox altitudeCheckBox;

    @FXML
    private Spinner<Quantity<Length>> altitudeSpinner;

    @FXML
    private Spinner<Quantity<Length>> altitudeSpreadSpinner;

    @FXML
    private Label altitudeRangeLabel;

    @FXML
    private CheckBox rollCheckBox;

    @FXML
    private Spinner<Quantity<Angle>> rollSpinner;

    @FXML
    private Spinner<Quantity<Angle>> rollSpreadSpinner;

    @FXML
    private Label rollRangeLabel;

    @FXML
    private CheckBox pitchCheckBox;

    @FXML
    private Spinner<Quantity<Angle>> pitchSpinner;

    @FXML
    private Spinner<Quantity<Angle>> pitchSpreadSpinner;

    @FXML
    private Label pitchRangeLabel;

    @FXML
    private CheckBox yawCheckBox;

    @FXML
    private Spinner<Quantity<Angle>> yawSpinner;

    @FXML
    private Spinner<Quantity<Angle>> yawSpreadSpinner;

    @FXML
    private Label yawRangeLabel;

    @FXML
    private ToggleSwitch onlyByAoi;

    @FXML
    private Button btnAddDefaultAOI;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private MenuButton btnAddAoiFromPlans;

    // Name ending in -Controller will be automatically imported by JavaFX
    @FXML
    private HardwareSelectionView hardwareSelectionViewController;

    @Override
    public void initializeView() {
        super.initializeView();

        hardwareSelectionViewController.bindHardwareConfiguration(viewModel.hardwareConfigurationProperty());

        hardwareSelectionViewController
            .validationStatusPropertyProperty()
            .bind(viewModel.exifValidationStatusProperty());
        altitudeCheckBox.selectedProperty().bindBidirectional(viewModel.altitudeEnabledProperty());

        filterSwitch.selectedProperty().bindBidirectional(viewModel.filtersEnabledProperty());
        BindingUtils.bindVisibility(imageParametersFilterPane, viewModel.filtersEnabledProperty());
        BindingUtils.bindVisibility(areaFilterPane, viewModel.filtersEnabledProperty());
        BindingUtils.bindVisibility(
            btnAddDefaultAOI, viewModel.filtersEnabledProperty().and(viewModel.onlyImagesOverlapAoisProperty()));
        BindingUtils.bindVisibility(
            areaFilterListView, viewModel.filtersEnabledProperty().and(viewModel.onlyImagesOverlapAoisProperty()));
        BindingUtils.bindVisibility(
            btnAddAoiFromPlans, viewModel.filtersEnabledProperty().and(viewModel.onlyImagesOverlapAoisProperty()));

        initAltitudeSpinners(
            altitudeCheckBox,
            altitudeSpinner,
            viewModel.altitudeProperty(),
            altitudeSpreadSpinner,
            viewModel.altitudeSpreadProperty(),
            altitudeRangeLabel);
        initAngleSpinners(
            rollCheckBox,
            rollSpinner,
            viewModel.rollProperty(),
            rollSpreadSpinner,
            viewModel.rollSpreadProperty(),
            rollRangeLabel);
        initAngleSpinners(
            pitchCheckBox,
            pitchSpinner,
            viewModel.pitchProperty(),
            pitchSpreadSpinner,
            viewModel.pitchSpreadProperty(),
            pitchRangeLabel);
        initAngleSpinnersYaw(
            yawCheckBox,
            yawSpinner,
            viewModel.yawProperty(),
            yawSpreadSpinner,
            viewModel.yawSpreadProperty(),
            yawRangeLabel);

        onlyByAoi.selectedProperty().bindBidirectional(viewModel.onlyImagesOverlapAoisProperty());
        rollCheckBox.selectedProperty().bindBidirectional(viewModel.rollEnabledProperty());
        pitchCheckBox.selectedProperty().bindBidirectional(viewModel.pitchEnabledProperty());
        yawCheckBox.selectedProperty().bindBidirectional(viewModel.yawEnabledProperty());

        areaFilterListView.addViewFactory(
            AreaFilterViewModel.class,
            vm -> FluentViewLoader.fxmlView(AreaFilterView.class).viewModel(vm).load().getView());
        areaFilterListView.itemsProperty().bind(viewModel.areaFiltersProperty());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public AnalysisOptionsSourceDataViewModel getViewModel() {
        return viewModel;
    }

    private void initAltitudeSpinners(
            CheckBox checkBox,
            Spinner<Quantity<Length>> valueSpinner,
            QuantityProperty<Length> valueProperty,
            Spinner<Quantity<Length>> spreadSpinner,
            QuantityProperty<Length> spreadProperty,
            Label rangeLabel) {
        ViewHelper.initAutoCommitSpinner(
            valueSpinner,
            valueProperty,
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            ALT_MIN,
            ALT_MAX,
            ALT_STEP,
            false);
        ViewHelper.initAutoCommitSpinner(
            spreadSpinner,
            spreadProperty,
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            0,
            0.,
            ALT_MAX,
            ALT_STEP,
            false);
        bindSpinners(checkBox, valueSpinner, valueProperty, spreadSpinner, spreadProperty, rangeLabel, identityTrafo);
    }

    private void initAngleSpinners(
            CheckBox checkBox,
            Spinner<Quantity<Angle>> valueSpinner,
            QuantityProperty<Angle> valueProperty,
            Spinner<Quantity<Angle>> spreadSpinner,
            QuantityProperty<Angle> spreadProperty,
            Label rangeLabel) {
        ViewHelper.initAutoCommitSpinner(
            valueSpinner,
            valueProperty,
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            -180.,
            180.,
            ANGLE_STEP,
            true);
        ViewHelper.initAutoCommitSpinner(
            spreadSpinner,
            spreadProperty,
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            0.,
            180.,
            ANGLE_STEP,
            false);
        bindSpinners(
            checkBox, valueSpinner, valueProperty, spreadSpinner, spreadProperty, rangeLabel, normalizePitchTrafo);
    }

    @FXML
    private void btnAddAoiFromPlansClicked() {
        btnAddAoiFromPlans
            .getItems()
            .setAll(
                viewModel
                    .flightPlansProperty()
                    .get()
                    .stream()
                    .map(
                        fp -> {
                            MenuItem m = new MenuItem();
                            m.setMnemonicParsing(false);
                            m.setText(fp.getName());
                            m.setOnAction(
                                event2 -> {
                                    viewModel.tryAddPicAreasFromFlightplan(fp);
                                });
                            return m;
                        })
                    .collect(Collectors.toList()));
        btnAddAoiFromPlans.show();
    }

    private void initAngleSpinnersYaw(
            CheckBox checkBox,
            Spinner<Quantity<Angle>> valueSpinner,
            QuantityProperty<Angle> valueProperty,
            Spinner<Quantity<Angle>> spreadSpinner,
            QuantityProperty<Angle> spreadProperty,
            Label rangeLabel) {
        ViewHelper.initAutoCommitSpinner(
            valueSpinner,
            valueProperty,
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            0.,
            360.,
            ANGLE_STEP,
            true);
        ViewHelper.initAutoCommitSpinner(
            spreadSpinner,
            spreadProperty,
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            0.,
            360.,
            ANGLE_STEP,
            false);
        bindSpinners(
            checkBox, valueSpinner, valueProperty, spreadSpinner, spreadProperty, rangeLabel, normalizeYawTrafo);
    }

    private <T extends Quantity<T>> void bindSpinners(
            CheckBox checkBox,
            Spinner<Quantity<T>> valueSpinner,
            QuantityProperty<T> valueProperty,
            Spinner<Quantity<T>> spreadSpinner,
            QuantityProperty<T> spreadProperty,
            Label rangeLabel,
            QunatitiyTransformator trafo) {
        checkBox.setSelected(true);
        valueSpinner.disableProperty().bind(checkBox.selectedProperty().not());
        spreadSpinner.disableProperty().bind(checkBox.selectedProperty().not());
        rangeLabel.disableProperty().bind(checkBox.selectedProperty().not());
        StringConverter<Quantity<T>> quantityConverter = valueSpinner.getValueFactory().getConverter();
        StringBinding rangeLabelBinding =
            Bindings.createStringBinding(
                () ->
                    languageHelper.getString(
                        RANGE_LABEL_FORMAT,
                        quantityConverter.toString(trafo.transform(valueProperty.get().subtract(spreadProperty.get()))),
                        quantityConverter.toString(trafo.transform(valueProperty.get().add(spreadProperty.get())))),
                valueProperty,
                spreadProperty);
        rangeLabel.textProperty().bind(rangeLabelBinding);
    }

    public static interface QunatitiyTransformator {
        <T extends Quantity<T>> Quantity<T> transform(Quantity<T> value);
    }

    static QunatitiyTransformator identityTrafo =
        new QunatitiyTransformator() {

            public <T extends Quantity<T>> Quantity<T> transform(Quantity<T> value) {
                return value;
            }
        };

    static QunatitiyTransformator normalizeYawTrafo =
        new QunatitiyTransformator() {
            public <T extends Quantity<T>> Quantity<T> transform(Quantity<T> value) {
                double val = value.getValue().doubleValue();

                while (val < 0) {
                    val += 360;
                }

                while (val >= 360) {
                    val -= 360;
                }

                return Quantity.of(val, value.getUnit());
            }
        };

    static QunatitiyTransformator normalizePitchTrafo =
        new QunatitiyTransformator() {

            public <T extends Quantity<T>> Quantity<T> transform(Quantity<T> value) {
                double val = value.getValue().doubleValue();

                while (val < -180) {
                    val += 360;
                }

                while (val >= 180) {
                    val -= 360;
                }

                return Quantity.of(val, value.getUnit());
            }
        };

    @FXML
    private void addDefaultAreaFilter() {
        viewModel.addDefaultAreaFilter();
    }

}
