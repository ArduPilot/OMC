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
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.common.hardware.HardwareSelectionView;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.sidepane.analysis.AreaFilterView;
import com.intel.missioncontrol.ui.sidepane.analysis.AreaFilterViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.asyncfx.beans.property.PropertyPath;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.TaskProgressView;

public class AnalysisOptionsSourceDataView extends ViewBase<AnalysisOptionsSourceDataViewModel> {

    private static final String RANGE_LABEL_FORMAT =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.rangeFormat";
    private static final double ANGLE_STEP = 1.0;
    private static final double ALT_STEP = 1.0;
    private static final int FRACTION_DIGITS = 1;

    @FXML
    public ComboBox exportFilterComboBox;

    @FXML
    public MenuItem altitude;

    @FXML
    public MenuItem pitch;

    @FXML
    public MenuItem yaw;

    @FXML
    public MenuItem roll;

    @FXML
    public MenuItem iso;

    @FXML
    public MenuItem exposureTime;

    @FXML
    public MenuItem exposure;

    @FXML
    public MenuItem imageType;

    @FXML
    public MenuItem annotation;

    @FXML
    public MenuItem byArea;

    @FXML
    public MenuItem byFlightplan;

    @FXML
    private ToggleGroup filterAOIGroup; // TODO IMC-3131 implement

    @FXML
    private Button doneAddingAOIButton;

    @FXML
    private Button btnAddDefaultAOI;

    @FXML
    private Label lblPassedFilter;

    @FXML
    private Label lblCalculating;

    @FXML
    private ProgressBar passedFilterProgressBar;

    @FXML
    private Label areaNotPassedFilter;

    @FXML
    private Label rangeNotPassedFilter;

    @FXML
    private Label pitchNotPassedFilter;

    @FXML
    private Label yawNotPassedFilter;

    @FXML
    private Label rollNotPassedFilter;

    @InjectViewModel
    private AnalysisOptionsSourceDataViewModel viewModel;

    @FXML
    private VBox root;

    @FXML
    private MenuButton btnAddFilter;

    @FXML
    private Button btnImportData;

    @FXML
    private Button btnAddData;

    @FXML
    private TitledPane imageImportPane;

    @FXML
    private ItemsView<AreaFilterViewModel> areaFilterListView;

    @FXML
    private TitledPane imagePane;

    @FXML
    private StackPane altitudeFilterPane;

    @FXML
    private StackPane pitchFilterPane;

    @FXML
    private StackPane yawFilterPane;

    @FXML
    private StackPane rollFilterPane;

    @FXML
    private StackPane isoFilterPane;

    @FXML
    private StackPane exposureTimeFilterPane;

    @FXML
    private StackPane exposureFilterPane;

    @FXML
    private StackPane imageTypeFilterPane;

    @FXML
    private StackPane annotationFilterPane;

    @FXML
    private StackPane areaFilterPane;

    @FXML
    private StackPane flightplanFilterPane;

    @FXML
    private TitledPane summaryPane;

    @FXML
    private Spinner<Quantity<Length>> altitudeFromSpinner;

    @FXML
    private Spinner<Quantity<Length>> altitudeToSpinner;

    @FXML
    private Label altitudeRangeLabel;

    @FXML
    private Spinner<Quantity<Angle>> rollFromSpinner;

    @FXML
    private Spinner<Quantity<Angle>> rollToSpinner;

    @FXML
    private Label rollRangeLabel;

    @FXML
    private Spinner<Quantity<Angle>> pitchFromSpinner;

    @FXML
    private Spinner<Quantity<Angle>> pitchToSpinner;

    @FXML
    private Label pitchRangeLabel;

    @FXML
    private Spinner<Quantity<Angle>> yawFromSpinner;

    @FXML
    private Spinner<Quantity<Angle>> yawToSpinner;

    @FXML
    private Label yawRangeLabel;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private ILanguageHelper languageHelper;

    // Name ending in -Controller will be automatically imported by JavaFX
    @FXML
    private HardwareSelectionView hardwareSelectionViewController;

    @FXML
    private TaskProgressView<Task<Void>> progressView;

    @FXML
    private CheckComboBox<FlightPlan> flightPlanNamesComboBox;

    @Override
    public void initializeView() {
        super.initializeView();

        hardwareSelectionViewController.bindHardwareConfiguration(viewModel.hardwareConfigurationProperty());

        hardwareSelectionViewController
            .validationStatusPropertyProperty()
            .bind(viewModel.exifValidationStatusProperty());

        // TODO IMC-3137, IMC-3136 imagePane => split images to paths, show path(s), images thumbs,...
        // TODO IMC-3136 change header text
        // imagePane.visibleProperty().bind(viewModel.isNewProperty().not());

        altitudeFilterPane
            .visibleProperty()
            .bind(viewModel.altitudeEnabledProperty().and(viewModel.isNewProperty().not()));
        pitchFilterPane.visibleProperty().bind(viewModel.pitchEnabledProperty().and(viewModel.isNewProperty().not()));
        yawFilterPane.visibleProperty().bind(viewModel.yawEnabledProperty().and(viewModel.isNewProperty().not()));
        rollFilterPane.visibleProperty().bind(viewModel.rollEnabledProperty().and(viewModel.isNewProperty().not()));
        isoFilterPane.visibleProperty().bind(viewModel.isoEnabledProperty().and(viewModel.isNewProperty().not()));
        exposureTimeFilterPane
            .visibleProperty()
            .bind(viewModel.exposureTimeEnabledProperty().and(viewModel.isNewProperty().not()));
        exposureFilterPane
            .visibleProperty()
            .bind(viewModel.exposureEnabledProperty().and(viewModel.isNewProperty().not()));
        imageTypeFilterPane
            .visibleProperty()
            .bind(viewModel.imageTypeEnabledProperty().and(viewModel.isNewProperty().not()));
        annotationFilterPane
            .visibleProperty()
            .bind(viewModel.annotationEnabledProperty().and(viewModel.isNewProperty().not()));
        areaFilterPane.visibleProperty().bind(viewModel.areaEnabledProperty().and(viewModel.isNewProperty().not()));
        flightplanFilterPane
            .visibleProperty()
            .bind(viewModel.flightplanEnabledProperty().and(viewModel.isNewProperty().not()));

        altitudeFilterPane
            .managedProperty()
            .bind(viewModel.altitudeEnabledProperty().and(viewModel.isNewProperty().not()));
        pitchFilterPane.managedProperty().bind(viewModel.pitchEnabledProperty().and(viewModel.isNewProperty().not()));
        yawFilterPane.managedProperty().bind(viewModel.yawEnabledProperty().and(viewModel.isNewProperty().not()));
        rollFilterPane.managedProperty().bind(viewModel.rollEnabledProperty().and(viewModel.isNewProperty().not()));
        isoFilterPane.managedProperty().bind(viewModel.isoEnabledProperty().and(viewModel.isNewProperty().not()));
        exposureTimeFilterPane
            .managedProperty()
            .bind(viewModel.exposureTimeEnabledProperty().and(viewModel.isNewProperty().not()));
        exposureFilterPane
            .managedProperty()
            .bind(viewModel.exposureEnabledProperty().and(viewModel.isNewProperty().not()));
        imageTypeFilterPane
            .managedProperty()
            .bind(viewModel.imageTypeEnabledProperty().and(viewModel.isNewProperty().not()));
        annotationFilterPane
            .managedProperty()
            .bind(viewModel.annotationEnabledProperty().and(viewModel.isNewProperty().not()));
        areaFilterPane.managedProperty().bind(viewModel.areaEnabledProperty().and(viewModel.isNewProperty().not()));
        flightplanFilterPane
            .managedProperty()
            .bind(viewModel.flightplanEnabledProperty().and(viewModel.isNewProperty().not()));

        altitude.disableProperty().bind(viewModel.altitudeEnabledProperty());
        pitch.disableProperty().bind(viewModel.pitchEnabledProperty());
        yaw.disableProperty().bind(viewModel.yawEnabledProperty());
        roll.disableProperty().bind(viewModel.rollEnabledProperty());
        iso.disableProperty().bind(viewModel.isoEnabledProperty());
        exposureTime.disableProperty().bind(viewModel.exposureTimeEnabledProperty());
        exposure.disableProperty().bind(viewModel.exposureEnabledProperty());
        imageType.disableProperty().bind(viewModel.imageTypeEnabledProperty());
        annotation.disableProperty().bind(viewModel.annotationEnabledProperty());
        byArea.disableProperty().bind(viewModel.areaEnabledProperty());
        byFlightplan.disableProperty().bind(viewModel.flightplanEnabledProperty());

        summaryPane.visibleProperty().bind(viewModel.isNewProperty().not());
        summaryPane.managedProperty().bind(viewModel.isNewProperty().not());

        lblPassedFilter.textProperty().bind(viewModel.progressDescriptionProperty());
        lblPassedFilter.visibleProperty().bind(viewModel.hasShownTask().not());
        lblPassedFilter.managedProperty().bind(lblPassedFilter.visibleProperty());
        lblCalculating.visibleProperty().bind(viewModel.hasShownTask());
        lblCalculating.managedProperty().bind(lblCalculating.visibleProperty());

        areaNotPassedFilter.textProperty().bind(viewModel.areaNotPassedFilterProperty());
        rangeNotPassedFilter.textProperty().bind(viewModel.rangeNotPassedFilterProperty());
        pitchNotPassedFilter.textProperty().bind(viewModel.pitchNotPassedFilterProperty());
        yawNotPassedFilter.textProperty().bind(viewModel.yawNotPassedFilterProperty());
        rollNotPassedFilter.textProperty().bind(viewModel.rollNotPassedFilterProperty());

        passedFilterProgressBar.progressProperty().bind(viewModel.progressProperty());

        exportFilterComboBox.itemsProperty().bind(viewModel.exportFilterProperty());
        exportFilterComboBox.valueProperty().bindBidirectional(viewModel.selectedExportFilterShownProperty());
        exportFilterComboBox.editableProperty().bind(viewModel.isNewProperty().not());

        // TODO IMC-3137 check if not disabled, what happens if parallel imported. Tasks should start each after another
        // btnImportData.disableProperty().bind(viewModel.hasShownTask());
        // btnAddData.disableProperty().bind(viewModel.hasShownTask());
        btnImportData.visibleProperty().bind(viewModel.isNewProperty());
        btnAddData.visibleProperty().bind(viewModel.isNewProperty().not());
        btnImportData.managedProperty().bind(viewModel.isNewProperty());
        btnAddData.managedProperty().bind(viewModel.isNewProperty().not());

        btnAddFilter
            .disableProperty()
            .bind(
                PropertyPath.from(viewModel.currentMatchingProperty())
                    .selectReadOnlyObject(Matching::statusProperty)
                    .isEqualTo(MatchingStatus.NEW));
        initAltitudeSpinners(
            altitudeFromSpinner,
            viewModel.altitudeFromProperty(),
            altitudeToSpinner,
            viewModel.altitudeToProperty(),
            viewModel.altitudeMinProperty(),
            viewModel.altitudeMaxProperty(),
            altitudeRangeLabel);
        initAngleSpinners(
            rollFromSpinner,
            viewModel.rollFromProperty(),
            rollToSpinner,
            viewModel.rollToProperty(),
            viewModel.rollMinProperty(),
            viewModel.rollMaxProperty(),
            rollRangeLabel);
        initAngleSpinners(
            pitchFromSpinner,
            viewModel.pitchFromProperty(),
            pitchToSpinner,
            viewModel.pitchToProperty(),
            viewModel.pitchMinProperty(),
            viewModel.pitchMaxProperty(),
            pitchRangeLabel);
        initAngleSpinnersYaw(
            yawFromSpinner,
            viewModel.yawFromProperty(),
            yawToSpinner,
            viewModel.yawToProperty(),
            viewModel.yawMinProperty(),
            viewModel.yawMaxProperty(),
            yawRangeLabel);

        // TODO IMC-3043 add new fields

        areaFilterListView.addViewFactory(
            AreaFilterViewModel.class,
            vm -> FluentViewLoader.fxmlView(AreaFilterView.class).viewModel(vm).load().getView());
        areaFilterListView.itemsProperty().bind(viewModel.areaFiltersProperty());

        viewModel.updateFlightPlansProperty();

        flightPlanNamesComboBox
            .disableProperty()
            .bind(viewModel.flightPlansProperty().emptyProperty().and(viewModel.isNewProperty().not()));

        flightPlanNamesComboBox.setConverter(
            new StringConverter<>() {
                @Override
                public String toString(FlightPlan object) {
                    if (viewModel.currentMatchingProperty().get() == null) {
                        return null;
                    }

                    File fpFolder =
                        new File(
                            viewModel.currentMatchingProperty().get().getMatchingFolder(),
                            AMapLayerMatching.FOLDER_NAME_FLIGHTPLANS_SUBFOLDER);
                    if (object.getResourceFile().getParent().equals(fpFolder.getAbsolutePath())) {
                        return object.getName() + " (dataset)";
                    }

                    return object.getName();
                }

                @Override
                public FlightPlan fromString(String string) {
                    return null;
                }
            });

        Bindings.bindContent(flightPlanNamesComboBox.getItems(), viewModel.flightPlansProperty());

        Bindings.bindContent(
            viewModel.selectedFlightPlansProperty(), flightPlanNamesComboBox.getCheckModel().getCheckedItems());
        imageImportPane.visibleProperty().bind(viewModel.hasShownTask());
        imageImportPane.managedProperty().bind(imageImportPane.visibleProperty());

        viewModel
            .shownTaskProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (!newValue.isDone() && !newValue.hasFinished() && !newValue.isCancelled()) {
                            progressView.getTasks().add(newValue);
                        } else {
                            progressView.getTasks().remove(newValue);
                        }
                    }
                });

        progressView
            .getTasks()
            .addListener(
                (ListChangeListener)
                    (o) -> {
                        updateImageImportPane(o.getList().isEmpty());
                        viewModel.updateProgress();
                    });

        doneAddingAOIButton.visibleProperty().bind(btnAddDefaultAOI.visibleProperty().not());
        doneAddingAOIButton.managedProperty().bind(btnAddDefaultAOI.visibleProperty().not());
        btnAddDefaultAOI.managedProperty().bind(btnAddDefaultAOI.visibleProperty());
    }

    private void updateImageImportPane(boolean empty) {
        if (empty) {
            viewModel.hasShownTask().set(false);
        } else {
            viewModel.hasShownTask().set(true);
        }

        root.autosize();
    }

    public VBox getRootNode() {
        return root;
    }

    public AnalysisOptionsSourceDataViewModel getViewModel() {
        return viewModel;
    }

    private void initAltitudeSpinners(
            Spinner<Quantity<Length>> valueSpinnerFrom,
            QuantityProperty<Length> valuePropertyFrom,
            Spinner<Quantity<Length>> valueSpinnerTo,
            QuantityProperty<Length> valuePropertyTo,
            QuantityProperty<Length> valuePropertyMin,
            QuantityProperty<Length> valuePropertyMax,
            Label rangeLabel) {
        ViewHelper.initAutoCommitSpinner(
            valueSpinnerFrom,
            valuePropertyFrom,
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            valuePropertyMin,
            valuePropertyTo,
            ALT_STEP,
            false);
        ViewHelper.initAutoCommitSpinner(
            valueSpinnerTo,
            valuePropertyTo,
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            valuePropertyFrom,
            valuePropertyMax,
            ALT_STEP,
            false);
        bindSpinners(
            valueSpinnerFrom,
            valuePropertyFrom,
            valueSpinnerTo,
            valuePropertyTo,
            valuePropertyMin,
            valuePropertyMax,
            rangeLabel,
            identityTrafo);
    }

    private void initAngleSpinners(
            Spinner<Quantity<Angle>> valueSpinnerFrom,
            QuantityProperty<Angle> valuePropertyFrom,
            Spinner<Quantity<Angle>> valueSpinnerTo,
            QuantityProperty<Angle> valuePropertyTo,
            QuantityProperty<Angle> valuePropertyMin,
            QuantityProperty<Angle> valuePropertyMax,
            Label rangeLabel) {
        ViewHelper.initAutoCommitSpinner(
            valueSpinnerFrom,
            valuePropertyFrom,
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            valuePropertyMin,
            valuePropertyTo,
            ANGLE_STEP,
            false);
        ViewHelper.initAutoCommitSpinner(
            valueSpinnerTo,
            valuePropertyTo,
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            valuePropertyFrom,
            valuePropertyMax,
            ANGLE_STEP,
            false);
        bindSpinners(
            valueSpinnerFrom,
            valuePropertyFrom,
            valueSpinnerTo,
            valuePropertyTo,
            valuePropertyMin,
            valuePropertyMax,
            rangeLabel,
            normalizePitchTrafo);
    }

    private void initAngleSpinnersYaw(
            Spinner<Quantity<Angle>> valueSpinnerFrom,
            QuantityProperty<Angle> valuePropertyFrom,
            Spinner<Quantity<Angle>> valueSpinnerTo,
            QuantityProperty<Angle> valuePropertyTo,
            QuantityProperty<Angle> valuePropertyMin,
            QuantityProperty<Angle> valuePropertyMax,
            Label rangeLabel) {
        ViewHelper.initAutoCommitSpinner(
            valueSpinnerFrom,
            valuePropertyFrom,
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            valuePropertyMin,
            valuePropertyTo,
            ANGLE_STEP,
            false);
        ViewHelper.initAutoCommitSpinner(
            valueSpinnerTo,
            valuePropertyTo,
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            FRACTION_DIGITS,
            valuePropertyFrom,
            valuePropertyMax,
            ANGLE_STEP,
            false);
        bindSpinners(
            valueSpinnerFrom,
            valuePropertyFrom,
            valueSpinnerTo,
            valuePropertyTo,
            valuePropertyMin,
            valuePropertyMax,
            rangeLabel,
            normalizeYawTrafo);
    }

    private <T extends Quantity<T>> void bindSpinners(
            Spinner<Quantity<T>> valueSpinnerFrom,
            QuantityProperty<T> valuePropertyFrom,
            Spinner<Quantity<T>> valueSpinnerTo,
            QuantityProperty<T> valuePropertyTo,
            QuantityProperty<T> valuePropertyMin,
            QuantityProperty<T> valuePropertyMax,
            Label rangeLabel,
            QunatitiyTransformator trafo) {
        StringConverter<Quantity<T>> quantityConverter = valueSpinnerFrom.getValueFactory().getConverter();
        StringBinding rangeLabelBinding =
            Bindings.createStringBinding(
                () -> {
                    return languageHelper.getString(
                        RANGE_LABEL_FORMAT, valuePropertyMin.getValue(), valuePropertyMax.getValue());
                },
                valuePropertyFrom,
                valuePropertyTo,
                valuePropertyMin,
                valuePropertyMax);
        if (rangeLabel != null) rangeLabel.textProperty().bind(rangeLabelBinding);
    }

    public void btnImportDataClicked(ActionEvent actionEvent) {
        viewModel.importDataCommand();
    }

    public void filterClicked(ActionEvent actionEvent) {
        switch ((((MenuItem)actionEvent.getSource()).idProperty().getValue())) {
        case "altitude":
            viewModel.altitudeEnabledProperty().set(true);
            return;
        case "pitch":
            viewModel.pitchEnabledProperty().set(true);
            return;
        case "roll":
            viewModel.rollEnabledProperty().set(true);
            return;
        case "yaw":
            viewModel.yawEnabledProperty().set(true);
            return;
        case "iso":
            viewModel.isoEnabledProperty().set(true);
            return;
        case "exposureTime":
            viewModel.exposureTimeEnabledProperty().set(true);
            return;
        case "exposure":
            viewModel.exposureEnabledProperty().set(true);
            return;
        case "imageType":
            viewModel.imageTypeEnabledProperty().set(true);
            return;
        case "annotation":
            viewModel.annotationEnabledProperty().set(true);
            return;
        case "byArea":
            viewModel.areaEnabledProperty().set(true);
            return;
        case "byFlightplan":
            viewModel.flightplanEnabledProperty().set(true);
            return;
        }
    }

    public void removeFilter(ActionEvent actionEvent) {
        switch (((((Button)actionEvent.getSource()).getParent()).idProperty().getValue())) {
        case "altitudeFilterPane":
            viewModel.altitudeEnabledProperty().set(false);
            return;
        case "pitchFilterPane":
            viewModel.pitchEnabledProperty().set(false);
            return;
        case "rollFilterPane":
            viewModel.rollEnabledProperty().set(false);
            return;
        case "yawFilterPane":
            viewModel.yawEnabledProperty().set(false);
            return;
        case "isoFilterPane":
            viewModel.isoEnabledProperty().set(false);
            return;
        case "exposureTimeFilterPane":
            viewModel.exposureTimeEnabledProperty().set(false);
            return;
        case "exposureFilterPane":
            viewModel.exposureEnabledProperty().set(false);
            return;
        case "imageTypeFilterPane":
            viewModel.imageTypeEnabledProperty().set(false);
            return;
        case "annotationFilterPane":
            viewModel.annotationEnabledProperty().set(false);
            return;
        case "areaFilterPane":
            viewModel.areaEnabledProperty().set(false);
            return;
        case "flightplanFilterPane":
            viewModel.flightplanEnabledProperty().set(false);
            return;
        }
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
        btnAddDefaultAOI.setVisible(false);
        viewModel.addDefaultAreaFilter();
    }

    public void doneAddingDefaultAreaFilter(ActionEvent actionEvent) {
        btnAddDefaultAOI.setVisible(true);
    }

}
