/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.api.IFlightPlanTemplateService;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.mission.bindings.BeanAdapter;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.utils.GeoUtils;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhotoSettings;
import eu.mavinci.core.flightplan.FlightplanSpeedModes;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.flightplan.visitors.CollectsTypeVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.helper.IRecomputeListener;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.Waypoint;
import eu.mavinci.flightplan.computation.FPsim;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

public class FlightPlan implements ISaveable {

    private static final Date DATE_MIN = new Date(0);
    private final IFlightplanChangeListener flightplanChangeListeners =
        new IFlightplanChangeListener() {

            @Override
            public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                updateProperties();
            }

            @Override
            public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
                updateProperties();
                updateWaypointList();
            }

            @Override
            public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
                updateProperties();
                updateWaypointList();
            }

            @Override
            public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {
                updateProperties();
                updateWaypointList();
            }
        };

    ListChangeListener<AreaOfInterest> areaOfInterestListChangeListener =
        new ListChangeListener<AreaOfInterest>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends AreaOfInterest> c) {
                allAoisValid.set(
                    areasOfInterestProperty().stream().allMatch(aoi -> aoi.hasEnoughCornersBinding().get()));

                while (c.next()) {
                    // TODO do we need to add a new PicArea to the map here ???
                    if (c.wasRemoved()) {
                        c.getRemoved().forEach(FlightPlan.this::onRemoveAreaOfInterest);
                    }
                }
            }
        };

    private IFlightPlanTemplateService templateService =
        DependencyInjector.getInstance().getInstanceOf(IFlightPlanTemplateService.class);
    private GeneralSettings generalSettings =
        DependencyInjector.getInstance().getInstanceOf(ISettingsManager.class).getSection(GeneralSettings.class);
    private final AirspacesProvidersSettings airspacesProvidersSettings =
        DependencyInjector.getInstance().getInstanceOf(AirspacesProvidersSettings.class);

    private final ListProperty<AreaOfInterest> areasOfInterest =
        new SimpleListProperty<>(
            FXCollections.observableArrayList(aoi -> new Observable[] {aoi.hasEnoughCornersBinding()}));
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<File> path = new SimpleObjectProperty<File>();
    private final BooleanProperty isNameSet = new SimpleBooleanProperty(false);
    private final ObjectProperty<FlightPlanTemplate> basedOnTemplate = new SimpleObjectProperty<>();
    private final BooleanProperty isTemplate = new SimpleBooleanProperty(false);
    private final BooleanProperty hasUnsavedChanges = new SimpleBooleanProperty(false);
    private final ObjectProperty<AltitudeAdjustModes> currentAltMode = new SimpleObjectProperty<>();
    private final ListProperty<WayPoint> waypoints = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<Position> refPointPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<Position> takeoffPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<Position> landingPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<LandingModes> landingMode = new SimpleObjectProperty<>();
    private final ObjectProperty<FlightplanSpeedModes> maxGroundSpeedAutomatic =
        new SimpleObjectProperty<>(FlightplanSpeedModes.MANUAL_CONSTANT);
    private final SimpleQuantityProperty<Dimension.Speed> maxGroundSpeed =
        new SimpleQuantityProperty<>(
            generalSettings,
            UnitInfo.INVARIANT_SPEED_MPS,
            Quantity.of(CPhotoSettings.MIN_SPEED, Unit.METER_PER_SECOND));
    private final BooleanProperty stopAtWaypoints = new SimpleBooleanProperty();
    private final BooleanProperty recalculateOnEveryChange = new SimpleBooleanProperty();
    private final ObjectProperty<MinMaxPair> gsdMismatchRange = new SimpleObjectProperty<>();

    private final BooleanProperty autoComputeSafetyHeight = new SimpleBooleanProperty();
    private final DoubleProperty safetyHeight = new SimpleDoubleProperty();
    private final DoubleProperty gsdTolerance = new SimpleDoubleProperty();

    private final BooleanProperty takeoffAuto = new SimpleBooleanProperty(true);
    private final BooleanProperty refPointAuto = new SimpleBooleanProperty(false);
    private ObjectProperty<ReferencePointType> refPointType = new SimpleObjectProperty<>();
    private IntegerProperty refPointOptionIndex = new SimpleIntegerProperty();
    private final SimpleQuantityProperty<Dimension.Length> refPointElevation =
        new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
    private final BooleanProperty allAoisValid = new SimpleBooleanProperty(true); // not updated any more
    private final BooleanProperty saveable = new SimpleBooleanProperty(true); // not updated any more
    private final SimpleQuantityProperty<Dimension.Length> takeoffElevation =
        new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
    private BooleanProperty enableJumpOverWaypoints = new SimpleBooleanProperty();
    private BooleanProperty allAoisSizeValid = new SimpleBooleanProperty(true);
    private final BooleanProperty simulatedTimeValid = new SimpleBooleanProperty(true);

    private final BeanAdapter<Flightplan> beanAdapter;

    private final eu.mavinci.flightplan.Flightplan legacyFlightplan;

    private final IRecomputeListener fpSimListener =
        (recomputer, anotherRecomputeIsWaiting, runNo) ->
            Dispatcher.postToUI(
                () -> {
                    for (WayPoint wp : waypoints) {
                        wp.airspaceWarningProperty().set(false);
                        wp.heightWarningProperty().set(false);
                        wp.groundDistanceProperty().set(null);
                    }

                    updateWaypointWarnings(waypoints.get());
                });

    private final IRecomputeListener listenerCoverage =
        new IRecomputeListener() {
            @Override
            public void recomputeReady(Recomputer recomputer, boolean anotherRecomputeIsWaiting, long runNo) {
                Dispatcher.postToUI(
                    () -> gsdMismatchRange.set(legacyFlightplan.getFPcoverage().getGsdMissmatchRange()));
            }
        };

    public FlightPlan(eu.mavinci.flightplan.Flightplan legacyFlightplan, boolean isTemplate) {
        Expect.notNull(legacyFlightplan, "flightplan");
        this.legacyFlightplan = legacyFlightplan;
        beanAdapter = new BeanAdapter<>(legacyFlightplan);
        this.isTemplate.set(isTemplate);
        saveable.bind(allAoisValid.and(this.isTemplate.or(areasOfInterest.sizeProperty().greaterThan(0))));

        legacyFlightplan.addFPChangeListener(flightplanChangeListeners);
        if (!isTemplate) {
            legacyFlightplan.getFPcoverage().addRecomputeListener(listenerCoverage);
            legacyFlightplan.getFPsim().addRecomputeListener(fpSimListener);
        }

        onUpdateProperties();
        updateWaypointList();

        this.areasOfInterest.addListener(areaOfInterestListChangeListener);
        airspacesProvidersSettings
            .useAirspaceDataForPlanningProperty()
            .addListener((observable, oldValue, newValue) -> doFlightplanCalculationIfAutoRecalcIsAvtive());
        bindFlightplan();
    }

    public void deleteWaypoints(Iterable<WayPoint> waypoints) {
        try {
            legacyFlightplan.setMute(true);
            for (var wp : waypoints) {
                var wpL = wp.getLegacyWaypoint();
                wpL.getParent().removeFromFlightplanContainer(wpL);
            }
        } finally {
            legacyFlightplan.setMute(false);
        }
    }

    private void bindFlightplan() {
        beanAdapter.bind(name).to(Flightplan::getName, Flightplan::setName);
        beanAdapter.bind(path).to(Flightplan::getFile, Flightplan::setFile);
        beanAdapter.bind(refPointPosition).to(Flightplan::getRefPointPosition, Flightplan::setRefPointPosition);
        beanAdapter.bind(takeoffPosition).to(Flightplan::getTakeoffPosition, Flightplan::setTakeoffPosition);
        beanAdapter.bind(landingPosition).to(Flightplan::getLandingPosition, Flightplan::setLandingPosition);
        beanAdapter
            .bind(landingMode)
            .to((f) -> f.getLandingpoint().getMode(), (f, mode) -> f.getLandingpoint().setMode(mode));
        beanAdapter
            .bind(maxGroundSpeedAutomatic)
            .to(
                (fp) -> fp.getPhotoSettings().getMaxGroundSpeedAutomatic(),
                (fp, mode) -> fp.getPhotoSettings().setMaxGroundSpeedAutomatic(mode));
        beanAdapter
            .bind(maxGroundSpeed)
            .to(
                (fp) -> Quantity.of(fp.getPhotoSettings().getMaxGroundSpeedMPSec(), Unit.METER_PER_SECOND),
                (fp, speed) ->
                    fp.getPhotoSettings()
                        .setMaxGroundSpeedMPSec(speed.convertTo(Unit.METER_PER_SECOND).getValue().doubleValue()));

        beanAdapter
            .bind(stopAtWaypoints)
            .to(
                (fp) -> fp.getPhotoSettings().isStoppingAtWaypoints(),
                (fp, stop) -> fp.getPhotoSettings().setStoppingAtWaypoints(stop));

        beanAdapter
            .bind(currentAltMode)
            .to(
                (fp) -> fp.getPhotoSettings().getAltitudeAdjustMode(),
                (fp, mode) -> fp.getPhotoSettings().setAltitudeAdjustMode(mode));
        beanAdapter.bind(hasUnsavedChanges).to(Flightplan::isChanged);
        beanAdapter
            .bind(takeoffAuto)
            .to((fp) -> fp.getTakeoff().isAuto(), (fp, isAuto) -> fp.getTakeoff().setIsAuto(isAuto));
        beanAdapter
            .bind(refPointAuto)
            .to((fp) -> fp.getRefPoint().isAuto(), (fp, isAuto) -> fp.getRefPoint().setIsAuto(isAuto));
        beanAdapter
            .bind(recalculateOnEveryChange)
            .to(Flightplan::getRecalculateOnEveryChange, Flightplan::setRecalculateOnEveryChange);
        beanAdapter
            .bind(safetyHeight)
            .to((fp) -> fp.getEventList().getAltWithinM(), (fp, alt) -> fp.getEventList().setAltWithinM(alt));
        beanAdapter
            .bind(autoComputeSafetyHeight)
            .to(
                (fp) -> fp.getEventList().isAutoComputingSafetyHeight(),
                (fp, auto) -> fp.getEventList().setAutoComputeSafetyHeight(auto));
        beanAdapter
            .bind(gsdTolerance)
            .to(
                (fp) -> fp.getPhotoSettings().getGsdTolerance(),
                (fp, gsdTolerance) -> fp.getPhotoSettings().setGsdTolerance(gsdTolerance));
        beanAdapter.bind(refPointType).to(Flightplan::getRefPointType, Flightplan::setRefPointType);
        beanAdapter
            .bind(refPointOptionIndex)
            .to(Flightplan::getRefPointOptionIndex, Flightplan::setRefPointOptionIndex);
        beanAdapter
            .bind(refPointElevation)
            .to(
                (fp) -> Quantity.of(fp.getRefPoint().getElevation(), Unit.METER),
                (fp, quantity) ->
                    fp.getRefPoint().setElevation(quantity.convertTo(Unit.METER).getValue().doubleValue()));

        beanAdapter
            .bind(takeoffElevation)
            .to(
                (fp) -> Quantity.of(fp.getTakeoff().getElevation(), Unit.METER),
                (fp, quantity) ->
                    fp.getTakeoff().setElevation(quantity.convertTo(Unit.METER).getValue().doubleValue()));
        beanAdapter
            .bind(enableJumpOverWaypoints)
            .to(Flightplan::enableJumpOverWaypoints, Flightplan::setEnableJumpOverWaypoints);
        beanAdapter.bind(allAoisSizeValid).to(Flightplan::allAoisSizeValid);
    }

    public ReadOnlyObjectProperty<MinMaxPair> gsdMismatchRangeProperty() {
        return gsdMismatchRange;
    }

    public ObjectProperty<LandingModes> landingModeProperty() {
        return landingMode;
    }

    public ReadOnlyListProperty<AreaOfInterest> areasOfInterestProperty() {
        return this.areasOfInterest;
    }

    public ObjectProperty<AltitudeAdjustModes> currentAltModeProperty() {
        return currentAltMode;
    }

    public ListProperty<AreaOfInterest> getAreasOfInterest() {
        return areasOfInterest;
    }

    public ReadOnlyBooleanProperty isTemplateProperty() {
        return isTemplate;
    }

    public BooleanProperty hasUnsavedChangesProperty() {
        return hasUnsavedChanges;
    }

    public BooleanProperty allAoisSizeValidProperty() {
        return allAoisSizeValid;
    }

    public BooleanProperty isSimulatedTimeValidProperty() {
        return simulatedTimeValid;
    }

    public boolean isTemplate() {
        return isTemplate.get();
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges.get();
    }

    public Flightplan getLegacyFlightplan() {
        return legacyFlightplan;
    }

    public String getName() {
        return name.get();
    }

    public DoubleProperty gsdToleranceProperty() {
        return gsdTolerance;
    }

    @Override
    public boolean canBeSaved() {
        return saveable.get();
    }

    public ReadOnlyBooleanProperty saveableProperty() {
        return saveable;
    }

    public BooleanProperty allAoisValidProperty() {
        return allAoisValid;
    }

    @Override
    public File getResourceFile() {
        return legacyFlightplan.getFile();
    }

    @Override
    public void save() {
        legacyFlightplan.save(null);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public BooleanProperty isNameSetProperty() {
        return isNameSet;
    }

    public ReadOnlyObjectProperty<FlightPlanTemplate> basedOnTemplateProperty() {
        return basedOnTemplate;
    }

    public LatLon getCenter() {
        return (GeoUtils.computeCenter(
            areasOfInterest
                .stream()
                .map(AreaOfInterest::getCenter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
    }

    public Sector getSector() {
        return legacyFlightplan.getSector();
    }

    public OptionalDouble getMaxElev() {
        return legacyFlightplan.getMaxElev();
    }

    public OptionalDouble getMinElev() {
        return legacyFlightplan.getMinElev();
    }

    private void updateProperties() {
        if (Platform.isFxApplicationThread()) {
            onUpdateProperties();
        } else {
            Platform.runLater(
                () -> {
                    onUpdateProperties();
                });
        }
    }

    private boolean isLoaded;

    private void onUpdateProperties() {
        beanAdapter.updateValuesFromSource();

        if (this.isLoaded) {
            return;
        }

        this.isLoaded = true;

        updateBaseTemplate();

        Iterator<IFlightplanStatement> it = legacyFlightplan.iterator();
        while (it.hasNext()) {
            IFlightplanStatement state = it.next();
            if (state instanceof PicArea) {
                PicArea picArea = (PicArea)state;
                AreaOfInterest areaOfInterest =
                    new AreaOfInterest(picArea, legacyFlightplan.getPicAreaTemplate(picArea.getPlanType()));
                this.areasOfInterest.add(areaOfInterest);
            }
        }
    }

    private void updateWaypointList() {
        CollectsTypeVisitor<Waypoint> vis = new CollectsTypeVisitor<>(Waypoint.class);
        vis.startVisit(legacyFlightplan);
        List<WayPoint> newList = new ArrayList<>(vis.matches.size());
        int numberInFlight = 0;

        for (Waypoint wp : vis.matches) {
            WayPoint wpN = new WayPoint(wp, generalSettings, ++numberInFlight);
            newList.add(wpN);
        }

        updateWaypointWarnings(newList);

        waypoints.setAll();
        waypoints.addAll(newList);
    }

    private void updateWaypointWarnings(List<WayPoint> newList) {
        if (isTemplate.get()){
            return;
        }
        FPsim.SimResultData simRes = legacyFlightplan.getFPsim().getSimResult();
        if (simRes != null) {
            simulatedTimeValid.set(simRes.simulatedTimeValid);

            double minGroundDistanceLimit =
                legacyFlightplan
                    .getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMinGroundDistance()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue();
            Double minGroundDistance = null;
            IFlightplanRelatedObject lastHeadingWp = null;
            WayPoint wpN = null;
            var iterator = newList.iterator();
            for (FPsim.SimDistance simDistance : simRes.simDistances) {
                if (lastHeadingWp != simDistance.fpRelObjectHeading) {
                    // search next waypoint where data has to be written into
                    lastHeadingWp = simDistance.fpRelObjectHeading;
                    if (wpN != null && minGroundDistance != null) {
                        wpN.groundDistanceProperty().set(Quantity.of(minGroundDistance, Unit.METER));
                    }

                    minGroundDistance = null;
                    boolean found = false;

                    while (iterator.hasNext()) {
                        wpN = iterator.next();
                        if (wpN.getLegacyWaypoint().equals(lastHeadingWp)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        // if we cant find the related WayPoint something is broken, because we cant write back
                        // simulation results -> give up
                        break;
                    }
                }

                if ((!simDistance.aoiCollisions.isEmpty())
                        && airspacesProvidersSettings.isUseAirspaceDataForPlanning()) {
                    wpN.airspaceWarningProperty().set(true);
                } else if ((simDistance.airspaceDistanceMeter < 0)
                        && airspacesProvidersSettings.isUseAirspaceDataForPlanning()) {
                    wpN.airspaceWarningProperty().set(true);
                } else if (simDistance.fpRelObjectHeading == simRes.firstFPobj) {
                    wpN.airspaceWarningProperty().set(false);
                }

                // do not check min ground distance before the first waypoint, it is checked separately
                if (minGroundDistance == null
                        || minGroundDistance > simDistance.groundDistanceMeter
                        || simDistance.fpRelObjectHeading == simRes.firstFPobj) {
                    minGroundDistance = simDistance.groundDistanceMeter;
                    if (minGroundDistance < minGroundDistanceLimit) {
                        wpN.heightWarningProperty().set(true);
                    } else {
                        wpN.heightWarningProperty().set(false);
                    }
                }
            }

            if (wpN != null && minGroundDistance != null) {
                wpN.groundDistanceProperty().set(Quantity.of(minGroundDistance, Unit.METER));
            }
        }
    }

    public void updateBaseTemplate() {
        basedOnTemplate.set(templateService.findByName(legacyFlightplan.getBasedOnTemplate()));
    }

    public void rename(String newName) {
        name.set(newName);
        isNameSet.setValue(true);
    }

    public void setBasedOnTemplate(FlightPlanTemplate flightPlanTemplate) {
        legacyFlightplan.setBasedOnTemplate(flightPlanTemplate.getName());
        basedOnTemplate.set(flightPlanTemplate);
    }

    private void onRemoveAreaOfInterest(AreaOfInterest areaOfInterest) {
        if (areaOfInterest == null) {
            return;
        }

        PicArea picArea = areaOfInterest.getPicArea();

        if (picArea == null) {
            return;
        }

        legacyFlightplan.removeFromFlightplanContainer(picArea);
    }

    public Position getFirstWaypointPosition(boolean includeAltitude) {
        return legacyFlightplan.getFirstWaypointPosition(includeAltitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightPlan)) return false;
        FlightPlan that = (FlightPlan)o;
        return Objects.equals(name, that.name)
            && Objects.equals(basedOnTemplate, that.basedOnTemplate)
            && Objects.equals(legacyFlightplan, that.legacyFlightplan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, basedOnTemplate, legacyFlightplan);
    }

    public ListProperty<WayPoint> waypointsProperty() {
        return waypoints;
    }

    public ObjectProperty<Position> takeoffPositionProperty() {
        return takeoffPosition;
    }

    public ObjectProperty<Position> refPointPositionProperty() {
        return refPointPosition;
    }

    public BooleanProperty takeoffAutoProperty() {
        return takeoffAuto;
    }

    public ObjectProperty<FlightplanSpeedModes> maxGroundSpeedAutomaticProperty() {
        return maxGroundSpeedAutomatic;
    }

    public SimpleQuantityProperty<Dimension.Speed> maxGroundSpeedProperty() {
        return maxGroundSpeed;
    }

    public BooleanProperty stopAtWaypointsProperty() {
        return stopAtWaypoints;
    }

    public BooleanProperty recalculateOnEveryChangeProperty() {
        return recalculateOnEveryChange;
    }

    public void doFlightplanCalculation() {
        legacyFlightplan.doFlightplanCalculation();
    }

    public boolean doFlightplanCalculationIfAutoRecalcIsAvtive() {
        return legacyFlightplan.doFlightplanCalculationIfAutoRecalcIsAvtive();
    }

    public BooleanProperty autoComputeSafetyHeightProperty() {
        return autoComputeSafetyHeight;
    }

    public DoubleProperty safetyHeightProperty() {
        return safetyHeight;
    }

    public BooleanProperty refPointAutoProperty() {
        return refPointAuto;
    }

    public ObjectProperty<ReferencePointType> refPointTypeProperty() {
        return refPointType;
    }

    public IntegerProperty getRefPointOptionIndexProperty() {
        return refPointOptionIndex;
    }

    public SimpleQuantityProperty<Dimension.Length> refPointElevationProperty() {
        return refPointElevation;
    }

    public AreaOfInterest getAreaOfInterest(PicArea picArea) {
        for (AreaOfInterest aoi : areasOfInterestProperty()) {
            if (aoi.getPicArea() == picArea) {
                return aoi;
            }
        }

        return null;
    }

    public SimpleQuantityProperty<Dimension.Length> takeoffElevationProperty() {
        return takeoffElevation;
    }

    public BooleanProperty enableJumpOverWaypointsProperty() {
        return enableJumpOverWaypoints;
    }
}
