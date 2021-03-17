/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.property.VariantQuantityProperty;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.SrsPosition;
import com.intel.missioncontrol.settings.RTKBasePositionContainer;
import com.intel.missioncontrol.settings.RtkBasePosition;
import com.intel.missioncontrol.settings.RtkBasePositionsSettings;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.InjectScope;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.LocationType;
import eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserViewModel;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import gov.nasa.worldwind.geom.Position;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.UIAsyncListProperty;

@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
public class AnalysisOptionsLocationViewModel extends ViewModelBase {

    private static final double RTK_FILTER_RADIUS = 20.0;

    private static final String ASSUMED_LOCATION_AVERAGE =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsLocationView.lblAssumedLocationAverage";

    @InjectScope
    private MainScope mainScope;

    private final UIAsyncListProperty<RtkBasePosition> rtkBasePositions = new UIAsyncListProperty<>(this);
    private final Property<LocationType> rtkBaseLocationProperty =
        new SimpleObjectProperty<LocationType>(LocationType.ASSUMED);

    private final ObjectProperty<Matching> matchingProperty = new SimpleObjectProperty<>();
    private final Property<RTKBasePositionContainer> selectedRtkStationProperty = new SimpleObjectProperty<>();

    private final Property<String> srsNameProperty = new SimpleStringProperty("");
    private final Property<String> assumedLocationAverageProperty = new SimpleStringProperty("");

    private final BooleanProperty rtkLocationConfirmedProperty = new SimpleBooleanProperty(false);

    private final QuantityProperty<Length> antennaHeightProperty;
    private final DoubleProperty antennaHeightAdapter = new SimpleDoubleProperty(0.0);
    private final DoubleProperty rtkAvgTime = new SimpleDoubleProperty(0.0);

    private final QuantityProperty<Length> geoidOffsetProperty;
    private final DoubleProperty geoidOffsetAdapter = new SimpleDoubleProperty(0.0);

    private final ListProperty<RTKBasePositionContainer> rtkStations =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final SimpleBooleanProperty rtkAvailable = new SimpleBooleanProperty(false);

    private final SrsPosition assumedRtkPostion;
    private final SrsPosition manualRtkPostion;
    private final ILanguageHelper languageHelper;
    private final IApplicationContext applicationContext;
    private final RtkBasePositionsSettings rtkBasePositionsSettings;
    private final IDialogService dialogService;
    private final ISrsManager srsManager;
    private boolean isSelectedRtkStationUpdating;
    private RtkBasePosition lastAddedRtkPosition;

    private final ObjectProperty<MSpatialReference> currentSrs = new SimpleObjectProperty<>();
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Inject
    public AnalysisOptionsLocationViewModel(
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            RtkBasePositionsSettings rtkBasePositionsSettings,
            IDialogService dialogService,
            ISrsManager srsManager) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.rtkBasePositionsSettings = rtkBasePositionsSettings;
        this.dialogService = dialogService;
        this.srsManager = srsManager;
        currentSrs.bindBidirectional(
            propertyPathStore.from(applicationContext.currentMissionProperty()).selectObject(Mission::srsProperty));

        assumedRtkPostion = new SrsPosition(quantityStyleProvider);
        manualRtkPostion = new SrsPosition(quantityStyleProvider);
        if (currentSrs.get() != null) {
            srsNameProperty.setValue(currentSrs.get().id);
        }

        currentSrs.addListener(
            ((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    return;
                }

                srsNameProperty.setValue(newValue.id);
                assumedRtkPostion.setSrs(newValue);
                manualRtkPostion.setSrs(newValue);
            }));

        antennaHeightProperty =
            new SimpleQuantityProperty<>(
                quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        geoidOffsetProperty =
            new SimpleQuantityProperty<>(
                quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        QuantityBindings.bindBidirectional(antennaHeightProperty, antennaHeightAdapter, Unit.METER);
        QuantityBindings.bindBidirectional(geoidOffsetProperty, geoidOffsetAdapter, Unit.METER);
    }

    public void init() {
        matchingProperty.addListener(
            (observable1, oldValue1, newValue1) -> {
                BindingUtils.rebindBidirectional(
                    antennaHeightAdapter, Matching::antennaHeightProperty, oldValue1, newValue1);

                BindingUtils.rebindBidirectional(
                    geoidOffsetAdapter, Matching::geoidOffsetProperty, oldValue1, newValue1);

                BindingUtils.rebindBidirectional(
                    rtkLocationConfirmedProperty, Matching::rtkLocationConfirmedProperty, oldValue1, newValue1);
                BindingUtils.rebindBidirectional(
                    rtkBaseLocationProperty, Matching::rtkBaseLocationTypeProperty, oldValue1, newValue1);
                BindingUtils.rebindBidirectional(
                    manualRtkPostion.positionProperty(), Matching::positionRealProperty, oldValue1, newValue1);
                BindingUtils.rebindBidirectional(
                    assumedRtkPostion.positionProperty(), Matching::positionAssumedProperty, oldValue1, newValue1);
                BindingUtils.rebind(rtkAvgTime, Matching::rtkAvgTimeProperty, newValue1);
                BindingUtils.rebind(rtkAvailable, Matching::rtkAvailableProperty, newValue1);
            });
        matchingProperty.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyObject(Mission::currentMatchingProperty)); // should trigger the listener above

        BooleanBinding rule =
            Bindings.createBooleanBinding(
                () ->
                    rtkAvailable.not().get()
                        || (rtkLocationConfirmedProperty.getValue()
                            || (matchingProperty.getValue() == null
                                || matchingProperty.getValue().getStatus() == MatchingStatus.NEW)
                            || rtkBaseLocationProperty.getValue() == LocationType.MANUAL),
                rtkLocationConfirmedProperty,
                matchingProperty,
                rtkAvailable,
                rtkBaseLocationProperty);

        assumedRtkPostion
            .positionProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    initRtkStations(newValue, true);
                });

        rtkAvgTime.addListener(
            (observable, oldValue, newValue) -> {
                final String avgTimeString = StringHelper.secToShortDHMS(newValue.doubleValue());
                final String averageString = languageHelper.getString(ASSUMED_LOCATION_AVERAGE, avgTimeString);
                assumedLocationAverageProperty.setValue(averageString);
            });

        rtkBasePositionsSettings
            .rtkBasePositionsProperty()
            .addListener((observable, oldValue, newValue) -> initRtkStations(assumedRtkPostion.getPosition(), true));

        selectedRtkStationProperty.addListener(
            (observable, oldValue, newValue) -> onSelectedRtkStationUpdated(newValue));
    }

    private void onSelectedRtkStationUpdated(RTKBasePositionContainer newValue) {
        if ((newValue == null) || (newValue.isEmpty())) {
            return;
        }

        isSelectedRtkStationUpdating = true;

        try {
            RtkBasePosition rtkStation = newValue.getPosition();
            manualRtkPostion.setPosition(rtkStation.getPosition(srsManager));
            currentSrs.set(srsManager.getSrsByIdOrDefault(rtkStation.srsThisProperty().get()));
            antennaHeightAdapter.setValue(rtkStation.antennaHeightProperty().get());
            geoidOffsetAdapter.setValue(rtkStation.geoIdOffsetProperty().get());
        } finally {
            isSelectedRtkStationUpdating = false;
        }
    }

    private void resetSelectedRtkStation() {
        if (isSelectedRtkStationUpdating) {
            return;
        }

        selectedRtkStationProperty.setValue(RTKBasePositionContainer.EMPTY);
    }

    private void initRtkStations(Position rtkAssumedPosition, boolean updatePositions) {
        List<RTKBasePositionContainer> containers =
            rtkBasePositionsSettings
                .rtkBasePositionsProperty()
                .stream()
                .map(position -> createPositionContainer(rtkAssumedPosition, position, updatePositions))
                .filter(container -> container.getDistance() <= RTK_FILTER_RADIUS)
                .sorted()
                .collect(Collectors.toList());

        rtkStations.setAll(containers);

        selectLastAddedRtkPosition();
    }

    private void selectLastAddedRtkPosition() {
        if (lastAddedRtkPosition == null) {
            return;
        }

        Optional<RTKBasePositionContainer> lastAddedRtkPostionContainer =
            rtkStations.stream().filter(container -> lastAddedRtkPosition.equals(container.getPosition())).findFirst();

        lastAddedRtkPosition = null;
        lastAddedRtkPostionContainer.ifPresent(container -> selectedRtkStationProperty.setValue(container));
    }

    private RTKBasePositionContainer createPositionContainer(
            Position rtkAssumedPosition, RtkBasePosition position, boolean updatePosition) {
        return RTKBasePositionContainer.forBasePositionAndAssumed(
            position, rtkAssumedPosition, updatePosition, srsManager);
    }

    private ReadOnlyListProperty<RtkBasePosition> rtkBasePositions() {
        return rtkBasePositions.getReadOnlyProperty();
    }

    public Property<LocationType> rtkBaseLocationProperty() {
        return rtkBaseLocationProperty;
    }

    public Property<String> srsProperty() {
        return srsNameProperty;
    }

    public Property<String> assumedLocationAverageProperty() {
        return assumedLocationAverageProperty;
    }

    public ReadOnlyProperty<String> latitudeProperty() {
        return assumedRtkPostion.latitudeDescriptionProperty();
    }

    public ReadOnlyProperty<String> longitudeProperty() {
        return assumedRtkPostion.longitudeDescriptionProperty();
    }

    public ReadOnlyProperty<String> altitudeProperty() {
        return assumedRtkPostion.altitudeDescriptionProperty();
    }

    public BooleanProperty rtkLocationConfirmedProperty() {
        return rtkLocationConfirmedProperty;
    }

    public DoubleProperty latitudeValueProperty() {
        return manualRtkPostion.latitudeValueProperty();
    }

    public DoubleProperty longitudeValueProperty() {
        return manualRtkPostion.longitudeValueProperty();
    }

    public DoubleProperty altitudeValueProperty() {
        return manualRtkPostion.altitudeValueProperty();
    }

    public QuantityProperty<Length> antennaHeightProperty() {
        return antennaHeightProperty;
    }

    public QuantityProperty<Length> geoidOffsetProperty() {
        return geoidOffsetProperty;
    }

    public ListProperty<RTKBasePositionContainer> rtkStationsProperty() {
        return rtkStations;
    }

    public Property<RTKBasePositionContainer> selectedRtkStationProperty() {
        return selectedRtkStationProperty;
    }

    public QuantityProperty<Length> altitudeQuantityProperty() {
        return manualRtkPostion.altitudeQuantity();
    }

    public VariantQuantityProperty latitudeQuantityProperty() {
        return manualRtkPostion.latitudeQuantity();
    }

    public VariantQuantityProperty longitudeQuantityProperty() {
        return manualRtkPostion.longitudeQuantity();
    }

    public void useAssumedRtkPosition() {
        manualRtkPostion.positionProperty().setValue(assumedRtkPostion.positionProperty().getValue());
        resetSelectedRtkStation();
    }

    public void saveLocation(String name) {
        lastAddedRtkPosition = createRtkPosition(name);
        rtkBasePositionsSettings.rtkBasePositionsProperty().add(lastAddedRtkPosition);
    }

    private RtkBasePosition createRtkPosition(String name) {
        RtkBasePosition position = new RtkBasePosition(name);

        position.lonOrXProperty().set(longitudeQuantityProperty().get().getValue().doubleValue());
        position.latOrYProperty().set(latitudeQuantityProperty().get().getValue().doubleValue());
        position.altOrZProperty().set(altitudeValueProperty().get());

        position.antennaHeightProperty().set(antennaHeightAdapter.get());
        position.geoIdOffsetProperty().set(geoidOffsetAdapter.get());

        MSpatialReference srs = currentSrs.get();

        if (srs != null) {
            position.srsThisProperty().set(srs.id);
            position.srsTargetProperty().set(srs.id);
        }

        return position;
    }

    public void changeSrs() {
        dialogService
            .requestDialogAndWait(this, SpatialReferenceChooserViewModel.class, () -> currentSrs)
            .getDialogResult();
    }

    public void resetAssumedRtkPosition() {
        antennaHeightAdapter.set(0.);
        geoidOffsetAdapter.set(0.);
        useAssumedRtkPosition();
    }
}
