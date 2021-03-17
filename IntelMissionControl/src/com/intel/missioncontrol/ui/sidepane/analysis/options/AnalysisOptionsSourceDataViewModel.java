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
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.AreaFilter;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.AreaFilterViewModel;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import com.intel.missioncontrol.ui.validation.matching.ExifValidator;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.util.Duration;

public class AnalysisOptionsSourceDataViewModel extends ViewModelBase {

    @InjectScope
    private MainScope mainScope;

    private final BooleanProperty filtersEnabled = new SimpleBooleanProperty(true);

    private BooleanProperty altitudeEnabled = new SimpleBooleanProperty(true);
    private QuantityProperty<Length> altitude;
    private QuantityProperty<Length> altitudeSpread;
    private DoubleProperty altitudeAdapter = new SimpleDoubleProperty();
    private DoubleProperty altitudeSpreadAdapter = new SimpleDoubleProperty();

    private final BooleanProperty rollEnabled = new SimpleBooleanProperty(true);
    private QuantityProperty<Angle> roll; // [0, 180], def 15
    private QuantityProperty<Angle> rollSpread;
    private final DoubleProperty rollAdapter = new SimpleDoubleProperty();
    private final DoubleProperty rollSpreadAdapter = new SimpleDoubleProperty();

    private BooleanProperty pitchEnabled = new SimpleBooleanProperty(true);
    private QuantityProperty<Angle> pitch; // [0, 90], def 30
    private QuantityProperty<Angle> pitchSpread;
    private DoubleProperty pitchAdapter = new SimpleDoubleProperty();
    private DoubleProperty pitchSpreadAdapter = new SimpleDoubleProperty();

    private BooleanProperty yawEnabled = new SimpleBooleanProperty(true);
    private QuantityProperty<Angle> yaw;
    private QuantityProperty<Angle> yawSpread;
    private DoubleProperty yawAdapter = new SimpleDoubleProperty();
    private DoubleProperty yawSpreadAdapter = new SimpleDoubleProperty();

    private final BooleanProperty onlyImagesOverlapAois = new SimpleBooleanProperty(false);

    private final ObjectProperty<IPlatformDescription> selectedPlatform = new SimpleObjectProperty<>();
    private final ObjectProperty<IGenericCameraDescription> selectedCamera = new SimpleObjectProperty<>();
    private final ObjectProperty<ILensDescription> selectedLens = new SimpleObjectProperty<>();

    private final StringProperty exifDataMsg = new SimpleStringProperty("");

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final ObjectProperty<IHardwareConfiguration> hardwareConfiguration = new SimpleObjectProperty<>();
    private final IValidationService validationService;

    private final ListProperty<AreaFilterViewModel> areaFiltersViewModels =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<AreaFilter> areaFilters = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<FlightPlan> flightPlans = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<ValidationStatus> exifValidationStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<Matching> currentMatching = new SimpleObjectProperty<>();

    @Inject
    public AnalysisOptionsSourceDataViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IValidationService validationService) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.validationService = validationService;

        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        altitude = new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(100, METER));
        QuantityBindings.bindBidirectional(altitude, altitudeAdapter, Unit.METER);
        altitudeSpread = new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH);
        QuantityBindings.bindBidirectional(altitudeSpread, altitudeSpreadAdapter, Unit.METER);

        roll = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(15, DEGREE));
        QuantityBindings.bindBidirectional(roll, rollAdapter, Unit.DEGREE);
        rollSpread = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(5, DEGREE));
        QuantityBindings.bindBidirectional(rollSpread, rollSpreadAdapter, Unit.DEGREE);

        pitch = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(pitch, pitchAdapter, Unit.DEGREE);
        pitchSpread = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(5, DEGREE));
        QuantityBindings.bindBidirectional(pitchSpread, pitchSpreadAdapter, Unit.DEGREE);

        yaw = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(30, DEGREE));
        QuantityBindings.bindBidirectional(yaw, yawAdapter, Unit.DEGREE);
        yawSpread = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(5, DEGREE));
        QuantityBindings.bindBidirectional(yawSpread, yawSpreadAdapter, Unit.DEGREE);

        currentMatching.addListener(
            (observable, oldValue, newValue) -> {
                rebindBidirectional(areaFilters, Matching::areaFiltersProperty, oldValue, newValue);
                rebindBidirectional(filtersEnabled, Matching::filtersEnabledProperty, oldValue, newValue);
                rebindBidirectional(altitudeEnabled, Matching::altitudeEnabledProperty, oldValue, newValue);
                rebindBidirectional(altitudeAdapter, Matching::altitudeValueProperty, oldValue, newValue);
                rebindBidirectional(altitudeSpreadAdapter, Matching::altitudeSpreadProperty, oldValue, newValue);
                rebindBidirectional(rollEnabled, Matching::rollEnabledProperty, oldValue, newValue);
                rebindBidirectional(rollAdapter, Matching::rollValueProperty, oldValue, newValue);
                rebindBidirectional(rollSpreadAdapter, Matching::rollSpreadProperty, oldValue, newValue);
                rebindBidirectional(pitchEnabled, Matching::pitchEnabledProperty, oldValue, newValue);
                rebindBidirectional(pitchAdapter, Matching::pitchValueProperty, oldValue, newValue);
                rebindBidirectional(pitchSpreadAdapter, Matching::pitchSpreadProperty, oldValue, newValue);
                rebindBidirectional(yawEnabled, Matching::yawEnabledProperty, oldValue, newValue);
                rebindBidirectional(yawAdapter, Matching::yawValueProperty, oldValue, newValue);
                rebindBidirectional(yawSpreadAdapter, Matching::yawSpreadProperty, oldValue, newValue);
                rebindBidirectional(onlyImagesOverlapAois, Matching::onlyAoiEnabledProperty, oldValue, newValue);
                rebindBidirectional(exifDataMsg, Matching::exifDataMsgProperty, oldValue, newValue);
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

        applicationContext
            .currentMissionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    flightPlans.unbind();
                    if (newValue != null) {
                        flightPlans.bind(newValue.flightPlansProperty());
                    }
                });

        areaFilters.addListener(
            (observable, oldValue, newValue) -> {
                areaFiltersViewModels.setAll(
                    areaFilters.stream().map(a -> new AreaFilterViewModel(a)).collect(Collectors.toList()));
            });
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

    public ListProperty<FlightPlan> flightPlansProperty() {
        return flightPlans;
    }

    public ReadOnlyListProperty<AreaFilterViewModel> areaFiltersProperty() {
        return areaFiltersViewModels;
    }

    public BooleanProperty filtersEnabledProperty() {
        return this.filtersEnabled;
    }

    public BooleanProperty altitudeEnabledProperty() {
        return this.altitudeEnabled;
    }

    public QuantityProperty<Length> altitudeProperty() {
        return this.altitude;
    }

    public QuantityProperty<Length> altitudeSpreadProperty() {
        return this.altitudeSpread;
    }

    public BooleanProperty rollEnabledProperty() {
        return this.rollEnabled;
    }

    public QuantityProperty<Angle> rollProperty() {
        return this.roll;
    }

    public QuantityProperty<Angle> rollSpreadProperty() {
        return this.rollSpread;
    }

    public BooleanProperty pitchEnabledProperty() {
        return this.pitchEnabled;
    }

    public QuantityProperty<Angle> pitchProperty() {
        return this.pitch;
    }

    public QuantityProperty<Angle> pitchSpreadProperty() {
        return this.pitchSpread;
    }

    public BooleanProperty yawEnabledProperty() {
        return this.yawEnabled;
    }

    public QuantityProperty<Angle> yawProperty() {
        return this.yaw;
    }

    public QuantityProperty<Angle> yawSpreadProperty() {
        return this.yawSpread;
    }

    public BooleanProperty onlyImagesOverlapAoisProperty() {
        return onlyImagesOverlapAois;
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
        Mission mission = applicationContext.getCurrentMission();
        if (mission == null) {
            return;
        }

        Matching matching = mission.getCurrentMatching();
        if (matching == null) {
            return;
        }

        boolean succeded = matching.tryAddPicAreasFromFlightplan(fp);
        if (!succeded) {
            applicationContext.addToast(
                Toast.of(ToastType.INFO)
                    .setShowIcon(true)
                    .setText(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.sidepane.analysis.AnalysisOptionsSourceDataViewModel.CouldNotExtractFiltersFromFlightplan"))
                    .setCloseable(true)
                    .setTimeout(Duration.seconds(5))
                    .create());
        }
    }
}
