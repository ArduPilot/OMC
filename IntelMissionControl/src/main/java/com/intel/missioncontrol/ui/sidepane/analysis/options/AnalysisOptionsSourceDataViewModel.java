/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import static com.intel.missioncontrol.measure.Unit.DEGREE;
import static com.intel.missioncontrol.measure.Unit.METER;
import static com.intel.missioncontrol.measure.UnitInfo.ANGLE_DEGREES;
import static com.intel.missioncontrol.measure.UnitInfo.LOCALIZED_LENGTH;
import static com.intel.missioncontrol.ui.common.BindingUtils.rebindBidirectional;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.mission.AreaFilter;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.AreaFilterViewModel;
import com.intel.missioncontrol.ui.sidepane.analysis.DataImportNewViewModel;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import com.intel.missioncontrol.ui.validation.matching.ExifValidator;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import java.util.stream.Collectors;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.concurrent.Dispatcher;

@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
public class AnalysisOptionsSourceDataViewModel extends ViewModelBase {

    private final IDialogService dialogService;

    @InjectScope
    private MainScope mainScope;

    private BooleanProperty altitudeEnabled = new SimpleBooleanProperty(false);
    private QuantityProperty<Length> altitudeFrom;
    private QuantityProperty<Length> altitudeTo;
    private QuantityProperty<Length> altitudeMin;
    private QuantityProperty<Length> altitudeMax;
    private DoubleProperty altitudeFromAdapter = new SimpleDoubleProperty();
    private DoubleProperty altitudeToAdapter = new SimpleDoubleProperty();
    private DoubleProperty altitudeMinAdapter = new SimpleDoubleProperty();
    private DoubleProperty altitudeMaxAdapter = new SimpleDoubleProperty();

    private final BooleanProperty rollEnabled = new SimpleBooleanProperty(false);
    private QuantityProperty<Angle> rollFrom; // [0, 180], def 15
    private QuantityProperty<Angle> rollTo;
    private final DoubleProperty rollFromAdapter = new SimpleDoubleProperty();
    private final DoubleProperty rollToAdapter = new SimpleDoubleProperty();
    private DoubleProperty rollMinAdapter = new SimpleDoubleProperty();
    private DoubleProperty rollMaxAdapter = new SimpleDoubleProperty();
    private QuantityProperty<Angle> rollMin;
    private QuantityProperty<Angle> rollMax;

    private BooleanProperty pitchEnabled = new SimpleBooleanProperty(false);
    private QuantityProperty<Angle> pitchFrom; // [0, 90], def 30
    private QuantityProperty<Angle> pitchTo;
    private DoubleProperty pitchFromAdapter = new SimpleDoubleProperty();
    private DoubleProperty pitchToAdapter = new SimpleDoubleProperty();
    private DoubleProperty pitchMinAdapter = new SimpleDoubleProperty();
    private DoubleProperty pitchMaxAdapter = new SimpleDoubleProperty();
    private QuantityProperty<Angle> pitchMin;
    private QuantityProperty<Angle> pitchMax;

    private BooleanProperty yawEnabled = new SimpleBooleanProperty(false);
    private QuantityProperty<Angle> yawFrom;
    private QuantityProperty<Angle> yawTo;
    private DoubleProperty yawFromAdapter = new SimpleDoubleProperty();
    private DoubleProperty yawToAdapter = new SimpleDoubleProperty();
    private DoubleProperty yawMinAdapter = new SimpleDoubleProperty();
    private DoubleProperty yawMaxAdapter = new SimpleDoubleProperty();
    private QuantityProperty<Angle> yawMin;
    private QuantityProperty<Angle> yawMax;

    // TODO IMC-3043 add new fields
    private BooleanProperty isoEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty exposureTimeEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty exposureEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty imageTypeEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty annotationEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty areaEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty flightplanEnabled = new SimpleBooleanProperty(false);

    private final ObjectProperty<IPlatformDescription> selectedPlatform = new SimpleObjectProperty<>();
    private final ObjectProperty<IGenericCameraDescription> selectedCamera = new SimpleObjectProperty<>();
    private final ObjectProperty<ILensDescription> selectedLens = new SimpleObjectProperty<>();

    private final StringProperty exifDataMsg = new SimpleStringProperty("");

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final ObjectProperty<IHardwareConfiguration> hardwareConfiguration = new SimpleObjectProperty<>();
    private final IValidationService validationService;
    private final INavigationService navigationService;

    private final ListProperty<AreaFilter> areaFilters = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FlightPlan> selectedFlightPlans =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final StringProperty selectedFlightPlansString = new SimpleStringProperty("");

    private ReadOnlyListProperty<FlightPlan> flightPlans =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<AreaFilterViewModel> areaFiltersViewModels =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<String> flightPlansFiltersViewModels =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<ValidationStatus> exifValidationStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<Matching> currentMatching = new SimpleObjectProperty<>();
    private final ObjectProperty<IBackgroundTaskManager.BackgroundTask> shownTask = new SimpleObjectProperty<>();

    private final BooleanProperty showTask = new SimpleBooleanProperty(false);

    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty progressDescription = new SimpleStringProperty("");
    private static final String KEY_PROGRESS_DESCRIPTION =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.lblProgressDescription";
    private final ChangeListener<? super Number> progressListener =
        (observable, oldValue, newValue) -> updateProgress();
    private final IntegerProperty filteredItemsCount = new SimpleIntegerProperty(0);

    private final IntegerProperty areaNotPassedFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty rangeNotPassedFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty pitchNotPassedFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty yawNotPassedFilter = new SimpleIntegerProperty(0);
    private final IntegerProperty rollNotPassedFilter = new SimpleIntegerProperty(0);
    private final StringProperty areaNotPassedFilterDescription = new SimpleStringProperty("");
    private final StringProperty rangeNotPassedFilterDescription = new SimpleStringProperty("");
    private final StringProperty pitchNotPassedFilterDescription = new SimpleStringProperty("");
    private final StringProperty yawNotPassedFilterDescription = new SimpleStringProperty("");
    private final StringProperty rollNotPassedFilterDescription = new SimpleStringProperty("");

    private final IntegerProperty picturesCount = new SimpleIntegerProperty(0);
    private final LongProperty filteredPicturesSizeBytes = new SimpleLongProperty(0L);
    private final StringProperty filteredPictureType = new SimpleStringProperty("");
    private static final String KB = "KB";
    private static final String MB = "MB";
    private static final String GB = "GB";
    private static final String TB = "TB";

    private static final String SIZE_FORMAT = "%.2f %s";

    private static final long KILOBYTE = 1024L;
    private static final long MEGABYTE = KILOBYTE * KILOBYTE;
    private static final long GIGABYTE = MEGABYTE * KILOBYTE;
    private static final long TERABYTE = GIGABYTE * KILOBYTE;

    private Property<String> selectedExportFilterShownProperty = new SimpleObjectProperty<>();
    private Property<String> selectedExportFilterProperty = new SimpleObjectProperty<>();
    private final ListProperty<String> exportFilterProperty =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private Property<String> selectedExportFilterFlagProperty = new SimpleObjectProperty<>();

    final String KEY_ALL_IMAGES =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.exportFilter.allImages";
    final String KEY_ALL_EXCEPT_FILTERED =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.exportFilter.allExceptFiltered";
    final String KEY_ONLY_FILTERED =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.exportFilter.onlyFiltered";

    final String KEY_ALL_IMAGES_NO =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.exportFilter.allImagesNo";
    final String KEY_ALL_EXCEPT_FILTERED_NO =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.exportFilter.allExceptFilteredNo";
    final String KEY_ONLY_FILTERED_NO =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsSourceDataView.exportFilter.onlyFilteredNo";

    @Inject
    public AnalysisOptionsSourceDataViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IValidationService validationService,
            INavigationService navigationService,
            IDialogService dialogService) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.validationService = validationService;
        this.navigationService = navigationService;
        this.dialogService = dialogService;
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        altitudeFrom =
            new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(100, METER)); // ? 100m
        QuantityBindings.bindBidirectional(altitudeFrom, altitudeFromAdapter, Unit.METER);
        altitudeTo = new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(100, METER));
        QuantityBindings.bindBidirectional(altitudeTo, altitudeToAdapter, Unit.METER);

        altitudeMin =
            new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(100, METER)); // ? 100m
        QuantityBindings.bindBidirectional(altitudeMin, altitudeMinAdapter, Unit.METER);
        altitudeMax = new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(100, METER));
        QuantityBindings.bindBidirectional(altitudeMax, altitudeMaxAdapter, Unit.METER);

        rollFrom = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(15, DEGREE));
        QuantityBindings.bindBidirectional(rollFrom, rollFromAdapter, Unit.DEGREE);
        rollTo = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(15, DEGREE));
        QuantityBindings.bindBidirectional(rollTo, rollToAdapter, Unit.DEGREE);
        rollMin = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(15, DEGREE));
        QuantityBindings.bindBidirectional(rollMin, rollMinAdapter, Unit.DEGREE);
        rollMax = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(15, DEGREE));
        QuantityBindings.bindBidirectional(rollMax, rollMaxAdapter, Unit.DEGREE);

        pitchFrom = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(pitchFrom, pitchFromAdapter, Unit.DEGREE);
        pitchTo = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(pitchTo, pitchToAdapter, Unit.DEGREE);
        pitchMin = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(pitchMin, pitchMinAdapter, Unit.DEGREE);
        pitchMax = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(pitchMax, pitchMaxAdapter, Unit.DEGREE);

        yawFrom = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(yawFrom, yawFromAdapter, Unit.DEGREE);
        yawTo = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(yawTo, yawToAdapter, Unit.DEGREE);

        yawMin = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(yawMin, yawMinAdapter, Unit.DEGREE);
        yawMax = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(yawMax, yawMaxAdapter, Unit.DEGREE);

        filteredItemsCount.addListener(progressListener);
        picturesCount.addListener(progressListener);
        filteredPicturesSizeBytes.addListener(progressListener);
        areaNotPassedFilter.addListener(progressListener);
        rollNotPassedFilter.addListener(progressListener);
        pitchNotPassedFilter.addListener(progressListener);
        yawNotPassedFilter.addListener(progressListener);
        rangeNotPassedFilter.addListener(progressListener);

        currentMatching.addListener(
            (observable, oldValue, newValue) -> {
                rebindBidirectional(areaFilters, Matching::areaFiltersProperty, oldValue, newValue);

                rebindBidirectional(altitudeEnabled, Matching::altitudeEnabledProperty, oldValue, newValue);
                rebindBidirectional(altitudeMinAdapter, Matching::altitudeMinProperty, oldValue, newValue);
                rebindBidirectional(altitudeMaxAdapter, Matching::altitudeMaxProperty, oldValue, newValue);
                rebindBidirectional(altitudeFromAdapter, Matching::altitudeFromProperty, oldValue, newValue);
                rebindBidirectional(altitudeToAdapter, Matching::altitudeToProperty, oldValue, newValue);

                rebindBidirectional(rollEnabled, Matching::rollEnabledProperty, oldValue, newValue);
                rebindBidirectional(rollMinAdapter, Matching::rollMinProperty, oldValue, newValue);
                rebindBidirectional(rollMaxAdapter, Matching::rollMaxProperty, oldValue, newValue);
                rebindBidirectional(rollFromAdapter, Matching::rollFromProperty, oldValue, newValue);
                rebindBidirectional(rollToAdapter, Matching::rollToProperty, oldValue, newValue);

                rebindBidirectional(pitchEnabled, Matching::pitchEnabledProperty, oldValue, newValue);
                rebindBidirectional(pitchMinAdapter, Matching::pitchMinProperty, oldValue, newValue);
                rebindBidirectional(pitchMaxAdapter, Matching::pitchMaxProperty, oldValue, newValue);
                rebindBidirectional(pitchFromAdapter, Matching::pitchFromProperty, oldValue, newValue);
                rebindBidirectional(pitchToAdapter, Matching::pitchToProperty, oldValue, newValue);

                rebindBidirectional(yawEnabled, Matching::yawEnabledProperty, oldValue, newValue);
                rebindBidirectional(yawMinAdapter, Matching::yawMinProperty, oldValue, newValue);
                rebindBidirectional(yawMaxAdapter, Matching::yawMaxProperty, oldValue, newValue);
                rebindBidirectional(yawFromAdapter, Matching::yawFromProperty, oldValue, newValue);
                rebindBidirectional(yawToAdapter, Matching::yawToProperty, oldValue, newValue);

                // TODO IMC-3043 show the number of images filtered out by each filter.
                // Should be updated on every value change.

                rebindBidirectional(isoEnabled, Matching::isoEnabledProperty, oldValue, newValue);
                rebindBidirectional(exposureTimeEnabled, Matching::exposureTimeEnabledProperty, oldValue, newValue);
                rebindBidirectional(exposureEnabled, Matching::exposureEnabledProperty, oldValue, newValue);
                rebindBidirectional(imageTypeEnabled, Matching::imageTypeEnabledProperty, oldValue, newValue);
                rebindBidirectional(annotationEnabled, Matching::annotationEnabledProperty, oldValue, newValue);

                rebindBidirectional(areaEnabled, Matching::areaEnabledProperty, oldValue, newValue);
                rebindBidirectional(flightplanEnabled, Matching::flightplanEnabledProperty, oldValue, newValue);
                rebindBidirectional(
                    selectedFlightPlansString, Matching::selectedFlightplanProperty, oldValue, newValue);
                // TODO IMC-3043 selectedFlightPlansString as extra values, at the moment comma seperated string

                rebindBidirectional(exifDataMsg, Matching::exifDataMsgProperty, oldValue, newValue);
                rebindBidirectional(
                    selectedExportFilterProperty, Matching::selectedExportFilterProperty, oldValue, newValue);
                rebindBidirectional(
                    selectedExportFilterFlagProperty, Matching::selectedExportFilterFlagProperty, oldValue, newValue);
            });
        currentMatching.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyObject(Mission::currentMatchingProperty));

        // could have null value in the beginning
        hardwareConfiguration.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::hardwareConfigurationProperty));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        validationService.addValidatorsChangedListener(observable -> initValidators());

        initValidators();

        areaFilters.addListener(
            (observable, oldValue, newValue) -> {
                areaFiltersViewModels.setAll(
                    newValue.stream()
                        .map(
                            a -> {
                                return new AreaFilterViewModel(a);
                            })
                        .collect(Collectors.toList()));
            });
        updateFlightPlansProperty();
        applicationContext
            .currentMissionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    updateFlightPlansProperty();
                });

        selectedFlightPlans.addListener(
            (observable, oldValue, newValue) -> {
                if (currentMatching.get() == null) return;

                currentMatching
                    .get()
                    .getLegacyMatching()
                    .getPicAreas()
                    .forEach(
                        a -> {
                            a.setDeleteDisabled(false);
                        });
                newValue.forEach(
                    flightPlan -> {
                        tryAddPicAreasFromFlightplan(flightPlan, true);
                    });

                areaFiltersViewModels.setAll(
                    areaFilters
                        .stream()
                        .map(
                            a -> {
                                return new AreaFilterViewModel(a);
                            })
                        .collect(Collectors.toList()));

                StringProperty selectedFlightPlansString = new SimpleStringProperty("");
                newValue.forEach(
                    flightPlan -> {
                        if (!selectedFlightPlansString
                                .get()
                                .contains(flightPlan.getResourceFile().getAbsolutePath() + ","))
                            selectedFlightPlansString.set(
                                selectedFlightPlansString.get() + flightPlan.getResourceFile().getAbsolutePath() + ",");
                    });
                if (!this.selectedFlightPlansString.get().equals(selectedFlightPlansString.get()))
                    this.selectedFlightPlansString.set(selectedFlightPlansString.get());
            });

        exportFilterProperty.clear();

        exportFilterProperty.addAll(
            languageHelper.getString(KEY_ALL_IMAGES),
            languageHelper.getString(KEY_ALL_EXCEPT_FILTERED),
            languageHelper.getString(KEY_ONLY_FILTERED));

        selectedExportFilterShownProperty.addListener(
            (observable, oldValue, newValue) -> {
                // remove  size,...
                if (newValue != null && !newValue.split(", ")[0].equals(selectedExportFilterProperty.getValue())) {
                    selectedExportFilterProperty.setValue(newValue.split(", ")[0]);

                    selectedExportFilterFlagProperty.setValue("" + exportFilterProperty.indexOf(newValue));
                }
            });

        shownTask.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::dataTransferBackgroundTaskProperty));

        filteredItemsCount.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::filteredItemsCountProperty));

        picturesCount.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::picturesCountProperty));
        filteredPicturesSizeBytes.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::filteredPicturesSizeBytesProperty));
        filteredPictureType.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::filteredPictureTypeProperty));
        rangeNotPassedFilter.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::rangeNotPassedFilterProperty));
        areaNotPassedFilter.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::areaNotPassedFilterProperty));
        rollNotPassedFilter.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::rollNotPassedFilterProperty));
        yawNotPassedFilter.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::yawNotPassedFilterProperty));
        pitchNotPassedFilter.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::pitchNotPassedFilterProperty));
    }

    private void initValidators() {
        ValidatorBase<?> tmp;
        Mission mission = applicationContext.getCurrentMission();
        if (mission == null) {
            return;
        }

        Matching matching = mission.getCurrentMatching();
        if (matching == null) {
            return;
        }

        tmp = validationService.getValidator(ExifValidator.class, matching);
        exifValidationStatus.set(tmp != null ? tmp.getValidationStatus() : null);

        BindingUtils.rebind(filteredItemsCount, Matching::filteredItemsCountProperty, matching);
        BindingUtils.rebind(picturesCount, Matching::picturesCountProperty, matching);
        BindingUtils.rebind(filteredPicturesSizeBytes, Matching::filteredPicturesSizeBytesProperty, matching);
        BindingUtils.rebind(filteredPictureType, Matching::filteredPictureTypeProperty, matching);

        BindingUtils.rebind(areaNotPassedFilter, Matching::areaNotPassedFilterProperty, matching);
        BindingUtils.rebind(rollNotPassedFilter, Matching::rollNotPassedFilterProperty, matching);
        BindingUtils.rebind(yawNotPassedFilter, Matching::yawNotPassedFilterProperty, matching);
        BindingUtils.rebind(pitchNotPassedFilter, Matching::pitchNotPassedFilterProperty, matching);
        BindingUtils.rebind(rangeNotPassedFilter, Matching::rangeNotPassedFilterProperty, matching);
        // TODO IMC-3043 show the number of images filtered out by each new filter.
        //  Should be updated on every value change.
    }

    private void initFilterByParameterReaction(
            BooleanProperty filterEnabledProp,
            DoubleProperty valueProperty,
            Supplier<Double> valueResetter,
            Supplier<Double> originalValueGetter) {
        filterEnabledProp.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue) {
                    valueProperty.setValue(originalValueGetter.get());
                } else {
                    valueProperty.setValue(valueResetter.get());
                }
            });
    }

    public ReadOnlyListProperty<String> flightPlansFiltersProperty() {
        return flightPlansFiltersViewModels;
    }

    public ReadOnlyListProperty<FlightPlan> flightPlansProperty() {
        return flightPlans;
    }

    public void updateFlightPlansProperty() {
        flightPlans =
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyList(Mission::flightPlansProperty);
        // TODO IMC-3131 add flightplans in dataset
    }

    public ReadOnlyListProperty<AreaFilterViewModel> areaFiltersProperty() {
        return areaFiltersViewModels;
    }

    public BooleanProperty altitudeEnabledProperty() {
        return this.altitudeEnabled;
    }

    public BooleanBinding isNewProperty() {
        return PropertyPath.from(currentMatchingProperty())
            .selectReadOnlyObject(Matching::statusProperty)
            .isEqualTo(MatchingStatus.NEW);
    }

    public QuantityProperty<Length> altitudeFromProperty() {
        return this.altitudeFrom;
    }

    public QuantityProperty<Length> altitudeToProperty() {
        return this.altitudeTo;
    }

    public QuantityProperty<Length> altitudeMinProperty() {
        return this.altitudeMin;
    }

    public QuantityProperty<Length> altitudeMaxProperty() {
        return this.altitudeMax;
    }

    public BooleanProperty rollEnabledProperty() {
        return this.rollEnabled;
    }

    public QuantityProperty<Angle> rollFromProperty() {
        return this.rollFrom;
    }

    public QuantityProperty<Angle> rollToProperty() {
        return this.rollTo;
    }

    public QuantityProperty<Angle> rollMinProperty() {
        return this.rollMin;
    }

    public QuantityProperty<Angle> rollMaxProperty() {
        return this.rollMax;
    }

    public BooleanProperty pitchEnabledProperty() {
        return this.pitchEnabled;
    }

    public QuantityProperty<Angle> pitchFromProperty() {
        return this.pitchFrom;
    }

    public QuantityProperty<Angle> pitchToProperty() {
        return this.pitchTo;
    }

    public QuantityProperty<Angle> pitchMinProperty() {
        return this.pitchMin;
    }

    public QuantityProperty<Angle> pitchMaxProperty() {
        return this.pitchMax;
    }

    public BooleanProperty yawEnabledProperty() {
        return this.yawEnabled;
    }

    public QuantityProperty<Angle> yawFromProperty() {
        return this.yawFrom;
    }

    public QuantityProperty<Angle> yawToProperty() {
        return this.yawTo;
    }

    public QuantityProperty<Angle> yawMinProperty() {
        return this.yawMin;
    }

    public QuantityProperty<Angle> yawMaxProperty() {
        return this.yawMax;
    }

    public BooleanProperty isoEnabledProperty() {
        return this.isoEnabled;
    }

    public BooleanProperty exposureTimeEnabledProperty() {
        return this.exposureTimeEnabled;
    }

    public BooleanProperty exposureEnabledProperty() {
        return this.exposureEnabled;
    }

    public BooleanProperty imageTypeEnabledProperty() {
        return this.imageTypeEnabled;
    }

    public BooleanProperty annotationEnabledProperty() {
        return this.annotationEnabled;
    }

    public BooleanProperty areaEnabledProperty() {
        return this.areaEnabled;
    }

    public BooleanProperty flightplanEnabledProperty() {
        return this.flightplanEnabled;
    }

    public ObjectProperty<ValidationStatus> exifValidationStatusProperty() {
        return exifValidationStatus;
    }

    public ObjectProperty<IHardwareConfiguration> hardwareConfigurationProperty() {
        return hardwareConfiguration;
    }

    public void addDefaultAreaFilter() {
        Mission mission = applicationContext.getCurrentMission();
        if (mission == null) {
            return;
        }

        Matching matching = mission.getCurrentMatching();
        if (matching == null) {
            return;
        }

        matching.addDefaultAreaFilter();
    }

    public void tryAddPicAreasFromFlightplan(FlightPlan fp) {
        tryAddPicAreasFromFlightplan(fp, false);
    }

    public void tryAddPicAreasFromFlightplan(FlightPlan fp, boolean silent) {
        Mission mission = applicationContext.getCurrentMission();
        if (mission == null) {
            return;
        }

        Matching matching = mission.getCurrentMatching();
        if (matching == null) {
            return;
        }

        boolean succeded = matching.tryAddPicAreasFromFlightplan(fp);
        if (!succeded && !silent) {
            applicationContext.addToast(
                Toast.of(ToastType.INFO)
                    .setText(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.analysis.AnalysisOptionsSourceDataViewModel.CouldNotExtractFiltersFromFlightplan"))
                    .create());
        }
    }

    public ObservableValue<? extends IBackgroundTaskManager.BackgroundTask> shownTask() {
        return PropertyPath.from(applicationContext.currentMissionProperty())
            .select(Mission::currentMatchingProperty)
            .selectReadOnlyObject(Matching::dataTransferBackgroundTaskProperty);
    }

    public BooleanProperty hasShownTask() {
        return showTask;
    }

    public void importDataCommand() {
        DataImportNewViewModel vm =
            dialogService.requestDialogAndWait(WindowHelper.getPrimaryViewModel(), DataImportNewViewModel.class);
    }

    public Property<IBackgroundTaskManager.BackgroundTask> shownTaskProperty() {
        return shownTask;
    }

    public ReadOnlyObjectProperty<Matching> currentMatchingProperty() {
        return currentMatching;
    }

    public ObservableValue<? extends String> progressDescriptionProperty() {
        return progressDescription;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void updateProgress() {
        int picturesTotalCount = picturesCount.get();
        int picturesFilteredCount = filteredItemsCount.get();
        long sizeBytes = filteredPicturesSizeBytes.get();
        String sizeDescription = buildSizeDescription(sizeBytes);

        double progressValue =
            (((picturesTotalCount > 0) && (picturesFilteredCount > 0))
                ? ((double)picturesFilteredCount / (double)picturesTotalCount)
                : (0.0));

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                progress.set(progressValue);
                progressDescription.set(
                    languageHelper.getString(
                        KEY_PROGRESS_DESCRIPTION, picturesFilteredCount, picturesTotalCount, sizeDescription));
                areaNotPassedFilterDescription.set("" + areaNotPassedFilter.getValue());
                rangeNotPassedFilterDescription.set("" + rangeNotPassedFilter.getValue());
                pitchNotPassedFilterDescription.set("" + pitchNotPassedFilter.getValue());
                yawNotPassedFilterDescription.set("" + yawNotPassedFilter.getValue());
                rollNotPassedFilterDescription.set("" + rollNotPassedFilter.getValue());
                // TODO IMC-3043 add counts for new filters

                if (selectedExportFilterShownProperty.getValue() == null) {
                    exportFilterProperty.add(languageHelper.getString(KEY_ALL_EXCEPT_FILTERED_NO));
                    selectedExportFilterShownProperty.setValue(languageHelper.getString(KEY_ALL_EXCEPT_FILTERED_NO));
                }

                if (selectedExportFilterShownProperty.getValue() != null) {
                    String selectedFilter = selectedExportFilterShownProperty.getValue().split(",")[0];
                    exportFilterProperty.clear();
                    exportFilterProperty.addAll(
                        languageHelper.getString(KEY_ALL_IMAGES_NO, picturesTotalCount),
                        languageHelper.getString(KEY_ALL_EXCEPT_FILTERED_NO, picturesFilteredCount),
                        languageHelper.getString(KEY_ONLY_FILTERED_NO, (picturesTotalCount - picturesFilteredCount)));

                    if (selectedFilter.equals(languageHelper.getString(KEY_ALL_IMAGES))) {
                        selectedExportFilterShownProperty().setValue(exportFilterProperty.get(0));
                    } else if (selectedFilter.equals(languageHelper.getString(KEY_ONLY_FILTERED))) {
                        selectedExportFilterShownProperty().setValue(exportFilterProperty.get(2));
                    } else {
                        selectedExportFilterShownProperty().setValue(exportFilterProperty.get(1));
                    }
                }
            });
    }

    private String buildSizeDescription(long sizeBytes) {
        if (sizeBytes <= 0L) {
            return "0 KB";
        }

        if (sizeBytes < MEGABYTE) {
            return String.format(SIZE_FORMAT, (double)sizeBytes / KILOBYTE, KB);
        }

        if (sizeBytes < GIGABYTE) {
            return String.format(SIZE_FORMAT, (double)sizeBytes / MEGABYTE, MB);
        }

        if (sizeBytes < TERABYTE) {
            return String.format(SIZE_FORMAT, (double)sizeBytes / GIGABYTE, GB);
        }

        return String.format(SIZE_FORMAT, (double)sizeBytes / TERABYTE, TB);
    }

    public ObservableValue exportFilterProperty() {
        return exportFilterProperty;
    }

    public Property selectedExportFilterShownProperty() {
        return selectedExportFilterShownProperty;
    }

    public ListProperty<FlightPlan> selectedFlightPlansProperty() {
        return selectedFlightPlans;
    }

    public ChangeListener<? super Number> getProgressListener() {
        return progressListener;
    }

    public StringProperty areaNotPassedFilterProperty() {
        return areaNotPassedFilterDescription;
    }

    public StringProperty rangeNotPassedFilterProperty() {
        return rangeNotPassedFilterDescription;
    }

    public StringProperty pitchNotPassedFilterProperty() {
        return pitchNotPassedFilterDescription;
    }

    public StringProperty yawNotPassedFilterProperty() {
        return yawNotPassedFilterDescription;
    }

    public StringProperty rollNotPassedFilterProperty() {
        return rollNotPassedFilterDescription;
    }

    public StringProperty selectedFlightPlansStringProperty() {
        return selectedFlightPlansString;
    }
}
