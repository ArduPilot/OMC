/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import static com.intel.missioncontrol.measure.Unit.DEGREE;
import static com.intel.missioncontrol.measure.Unit.METER;
import static com.intel.missioncontrol.measure.Unit.METER_PER_SECOND;
import static com.intel.missioncontrol.measure.UnitInfo.ANGLE_DEGREES;
import static com.intel.missioncontrol.measure.UnitInfo.INVARIANT_SPEED_MPS;
import static com.intel.missioncontrol.measure.UnitInfo.LOCALIZED_LENGTH;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.geometry.AreaOfInterestCorner;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleVariantQuantityProperty;
import com.intel.missioncontrol.measure.property.VariantQuantityProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.flightplan.PicAreaCorners;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.PointWithAltitudes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.asyncfx.beans.property.PropertyPathStore;
import org.jetbrains.annotations.NotNull;

public class InspectionPointBulkSettingsViewModel extends DialogViewModel<Void, AreaOfInterest> {

    public enum TriggerChangeType {
        NO_CHANGE("No change"),
        TRIGGER_ACTIVE("Capture image"),
        TRIGGER_INACTIVE("Don't capture");

        private String label;

        TriggerChangeType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum TargetChangeType {
        NO_CHANGE("No change"),
        TRIGGER_ACTIVE("Target point"),
        TRIGGER_INACTIVE("Way point");

        private String label;

        TargetChangeType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final Command applyChangesCommand;
    private final IApplicationContext applicationContext;
    private final StringProperty noteChange = new SimpleStringProperty();
    private final ListProperty<TriggerChangeType> availableTriggerChangeTypes =
        new SimpleListProperty<>(FXCollections.observableArrayList(TriggerChangeType.values()));
    private final ListProperty<TargetChangeType> availableTargetChangeType =
        new SimpleListProperty<>(FXCollections.observableArrayList(TargetChangeType.values()));

    public TriggerChangeType getTriggerChangeStatus() {
        return triggerChangeStatus.get();
    }

    public ObjectProperty<TriggerChangeType> triggerChangeStatusProperty() {
        return triggerChangeStatus;
    }

    public TargetChangeType getTargetChangeStatus() {
        return targetChangeStatus.get();
    }

    public ObjectProperty<TargetChangeType> targetChangeStatusProperty() {
        return targetChangeStatus;
    }

    private final ObjectProperty<TriggerChangeType> triggerChangeStatus =
        new SimpleObjectProperty<>(TriggerChangeType.NO_CHANGE);
    private final ObjectProperty<TargetChangeType> targetChangeStatus =
        new SimpleObjectProperty<>(TargetChangeType.NO_CHANGE);
    private final BooleanProperty bulkEditable = new SimpleBooleanProperty(true);
    private final BooleanProperty editable = new SimpleBooleanProperty(true);
    // {
    //                @Override
    //                protected void invalidated() {
    //                    super.invalidated();
    //
    //                    boolean anySelected = false;
    //                    for (AreaOfInterestCorner wp : areaOfInterestCorners) {
    //                        if (wp.isSelected()) {
    //                            anySelected = true;
    //                            break;
    //                        }
    //                    }
    //
    //                    bulkEditable.set(get() && anySelected);
    //                }
    //            };
    private final BooleanProperty latLonAddChecked;
    private final VariantQuantityProperty latChange;
    private final VariantQuantityProperty lonChange;
    private final BooleanProperty altAddChecked;
    private final QuantityProperty<Dimension.Length> altChange;
    private final BooleanProperty rollAddChecked;
    private final QuantityProperty<Dimension.Angle> rollChange;
    private final BooleanProperty pitchAddChecked;
    private final QuantityProperty<Dimension.Angle> pitchChange;
    private final BooleanProperty speedAddChecked;
    private final QuantityProperty<Dimension.Speed> speedChange;
    private final BooleanProperty yawAddChecked;
    private final QuantityProperty<Dimension.Angle> yawChange;
    private final ILanguageHelper languageHelper;
    private final ISelectionManager selectionManager;
    private final Quantity<Dimension.Speed> maxSpeedMps;
    private final Quantity<Dimension.Angle> minPitch;
    private final Quantity<Dimension.Angle> maxPitch;
    private final Quantity<Dimension.Angle> minRoll;
    private final Quantity<Dimension.Angle> maxRoll;
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final BooleanProperty resolutionAddChecked;
    private final BooleanProperty distanceAddChecked;
    private final BooleanProperty frameDiagAddChecked;
    private final BooleanProperty rotateAddChecked;
    private final QuantityProperty<Dimension.Length> resolutionAddChange;
    private final QuantityProperty<Dimension.Length> distanceAddChange;
    private final QuantityProperty<Dimension.Length> frameDiagAddChange;
    private final SimpleQuantityProperty<Dimension.Angle> rotateAddChange;
    private final GeneralSettings generalSettings;
    private final ISettingsManager settingsManager;
    private DelegateCommand deleteSelectedAreaOfInterestCornersCommand;
    private FilteredList<AreaOfInterestCorner> highlightedCorners;
    private ListProperty<AreaOfInterestCorner> areaOfInterestCorners;

    private AreaOfInterest areaOfInterest;

    @Inject
    public InspectionPointBulkSettingsViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            ISelectionManager selectionManager) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.selectionManager = selectionManager;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.settingsManager = settingsManager;
        applyChangesCommand = new DelegateCommand(this::applyAllChanges, bulkEditable);

        latLonAddChecked = new SimpleBooleanProperty(true);
        latChange =
            new SimpleVariantQuantityProperty(generalSettings, UnitInfo.ANGLE_DEGREES, UnitInfo.LOCALIZED_LENGTH);
        latChange.set(Quantity.of(0, METER).toVariant());

        lonChange =
            new SimpleVariantQuantityProperty(generalSettings, UnitInfo.ANGLE_DEGREES, UnitInfo.LOCALIZED_LENGTH);
        lonChange.set(Quantity.of(0, METER).toVariant());

        altAddChecked = new SimpleBooleanProperty(true);
        altChange = new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(0, METER));

        rollAddChecked = new SimpleBooleanProperty(true);
        rollChange = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(0, DEGREE));

        pitchAddChecked = new SimpleBooleanProperty(true);
        pitchChange = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(0, DEGREE));

        yawAddChecked = new SimpleBooleanProperty(true);
        yawChange = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(0, DEGREE));

        resolutionAddChecked = new SimpleBooleanProperty(true);

        distanceAddChecked = new SimpleBooleanProperty(true);
        frameDiagAddChecked = new SimpleBooleanProperty(true);
        rotateAddChecked = new SimpleBooleanProperty(true);

        resolutionAddChange = new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(0, METER));
        distanceAddChange = new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(0, METER));
        frameDiagAddChange = new SimpleQuantityProperty<>(generalSettings, LOCALIZED_LENGTH, Quantity.of(0, METER));

        rotateAddChange = new SimpleQuantityProperty<>(generalSettings, ANGLE_DEGREES, Quantity.of(0, DEGREE));

        speedAddChecked = new SimpleBooleanProperty(true);
        speedChange =
            new SimpleQuantityProperty<>(generalSettings, INVARIANT_SPEED_MPS, Quantity.of(0, METER_PER_SECOND));

        FlightPlan fp = applicationContext.getCurrentMission().getCurrentFlightPlan();
        maxSpeedMps = fp.getLegacyFlightplan().getHardwareConfiguration().getPlatformDescription().getMaxPlaneSpeed();
        minPitch =
            fp.getLegacyFlightplan().getHardwareConfiguration().getPrimaryPayload().getDescription().getMinPitch();
        maxPitch =
            fp.getLegacyFlightplan().getHardwareConfiguration().getPrimaryPayload().getDescription().getMaxPitch();
        minRoll = fp.getLegacyFlightplan().getHardwareConfiguration().getPrimaryPayload().getDescription().getMinRoll();
        maxRoll = fp.getLegacyFlightplan().getHardwareConfiguration().getPrimaryPayload().getDescription().getMaxRoll();

        //
        //        highlightedCorners.bindBidirectional(selectionManager.getHighlighted());
        //        selectedWayPoint.addListener(
        //            (observable, oldValue, newValue) -> {
        //                if (newValue != null) {
        //                    selectionManager.setSelection(newValue.getLegacyWaypoint());
        //                } else {
        //                    selectionManager.setSelection(fp.getLegacyFlightplan());
        //                }
        //            });

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        getCloseCommand().execute();
                    }
                });

        applicationContext
            .currentMissionProperty()
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        getCloseCommand().execute();
                    }
                });
    }

    @Override
    protected void initializeViewModel(AreaOfInterest areaOfInterest) {
        super.initializeViewModel(areaOfInterest);
        this.areaOfInterest = areaOfInterest;
        areaOfInterestCorners = areaOfInterest.cornerListProperty();

        this.deleteSelectedAreaOfInterestCornersCommand =
            new DelegateCommand(this::deleteSelectedAreaOfInterestCorners);

        highlightedCorners = new FilteredList<>(areaOfInterestCorners, p -> p.selectedProperty().get());
    }

    private void deleteSelectedAreaOfInterestCorners() {
        var selectedIndices = new ArrayList<Integer>();
        int i = 0;
        for (var corner : areaOfInterestCorners) {
            if (corner.isSelected()) {
                selectedIndices.add(i);
            }

            i++;
        }

        Collections.reverse(selectedIndices);
        areaOfInterest.getPicArea().setMute(true);
        for (var selectedIndex : selectedIndices) {
            areaOfInterestCorners.get(selectedIndex).deleteMe();
        }

        areaOfInterest.getPicArea().setMute(false);
    }

    public boolean isEditable() {
        return editable.get();
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public boolean isLatLonAddChecked() {
        return latLonAddChecked.get();
    }

    public BooleanProperty latLonAddCheckedProperty() {
        return latLonAddChecked;
    }

    public VariantQuantity getLatChange() {
        return latChange.get();
    }

    public VariantQuantityProperty latChangeProperty() {
        return latChange;
    }

    public VariantQuantity getLonChange() {
        return lonChange.get();
    }

    public VariantQuantityProperty lonChangeProperty() {
        return lonChange;
    }

    public boolean isAltAddChecked() {
        return altAddChecked.get();
    }

    public BooleanProperty altAddCheckedProperty() {
        return altAddChecked;
    }

    public Quantity<Dimension.Length> getAltChange() {
        return altChange.get();
    }

    public QuantityProperty<Dimension.Length> altChangeProperty() {
        return altChange;
    }

    public boolean isRollAddChecked() {
        return rollAddChecked.get();
    }

    public BooleanProperty rollAddCheckedProperty() {
        return rollAddChecked;
    }

    public Quantity<Dimension.Angle> getRollChange() {
        return rollChange.get();
    }

    public QuantityProperty<Dimension.Angle> rollChangeProperty() {
        return rollChange;
    }

    public boolean isPitchAddChecked() {
        return pitchAddChecked.get();
    }

    public BooleanProperty pitchAddCheckedProperty() {
        return pitchAddChecked;
    }

    public Quantity<Dimension.Angle> getPitchChange() {
        return pitchChange.get();
    }

    public QuantityProperty<Dimension.Angle> pitchChangeProperty() {
        return pitchChange;
    }

    public boolean isSpeedAddChecked() {
        return speedAddChecked.get();
    }

    public BooleanProperty speedAddCheckedProperty() {
        return speedAddChecked;
    }

    public Quantity<Dimension.Speed> getSpeedChange() {
        return speedChange.get();
    }

    public QuantityProperty<Dimension.Speed> speedChangeProperty() {
        return speedChange;
    }

    public boolean isYawAddChecked() {
        return yawAddChecked.get();
    }

    public BooleanProperty yawAddCheckedProperty() {
        return yawAddChecked;
    }

    public Quantity<Dimension.Angle> getYawChange() {
        return yawChange.get();
    }

    public QuantityProperty<Dimension.Angle> yawChangeProperty() {
        return yawChange;
    }

    private void shiftLat(AreaOfInterestCorner toShift, VariantQuantityProperty q) {
        switch (q.get().getDimension()) {
        case LENGTH:
            {
                double deltaLatInM = q.get().convertTo(METER).getValue().doubleValue();

                double dLat = deltaLatInM / Earth.WGS84_EQUATORIAL_RADIUS;

                var oldLat = toShift.latProperty().get().convertTo(DEGREE).getValue().doubleValue();

                double newLat = oldLat + dLat * 180 / Math.PI;
                toShift.latProperty().set(Quantity.of(newLat, DEGREE));
            }

            break;
        case ANGLE:
            {
                var shiftAngle = q.get().convertTo(DEGREE).getValue().doubleValue();
                var oldLat = toShift.latProperty().get().convertTo(DEGREE).getValue().doubleValue();
                var newLat = oldLat + shiftAngle;
                toShift.latProperty().set(Quantity.of(newLat, DEGREE));
            }

            break;
        default:
            // do nothing
            break;
        }
    }

    private void shiftLon(AreaOfInterestCorner toShift, VariantQuantityProperty q, double lat) {
        switch (q.get().getDimension()) {
        case LENGTH:
            {
                double deltaLonInM = q.get().convertTo(METER).getValue().doubleValue();

                double dLon = deltaLonInM / Earth.WGS84_EQUATORIAL_RADIUS * Math.cos(Math.PI * lat / 180.0);

                var oldLon = toShift.lonProperty().get().convertTo(DEGREE).getValue().doubleValue();

                double newLon = oldLon + dLon * 180 / Math.PI;
                toShift.lonProperty().set(Quantity.of(newLon, DEGREE));
            }

            break;
        case ANGLE:
            {
                var shiftAngle = q.get().convertTo(DEGREE).getValue().doubleValue();
                var oldLon = toShift.lonProperty().get().convertTo(DEGREE).getValue().doubleValue();
                var newLon = oldLon + shiftAngle;
                toShift.lonProperty().set(Quantity.of(newLon, DEGREE));
            }

            break;
        default:
            // do nothing
            break;
        }
    }

    // Todo: wrong signature, doesn't do anything :)
    private void rotate(AreaOfInterestCorner toShift, VariantQuantityProperty q, double lat) {
        switch (q.get().getDimension()) {
        case LENGTH:
            {
                double deltaLonInM = q.get().convertTo(METER).getValue().doubleValue();

                double dLon = deltaLonInM / Earth.WGS84_EQUATORIAL_RADIUS * Math.cos(Math.PI * lat / 180.0);

                var oldLon = toShift.lonProperty().get().convertTo(DEGREE).getValue().doubleValue();

                double newLon = oldLon + dLon * 180 / Math.PI;
                toShift.lonProperty().set(Quantity.of(newLon, DEGREE));
            }

            break;
        case ANGLE:
            {
                var shiftAngle = q.get().convertTo(DEGREE).getValue().doubleValue();
                var oldLon = toShift.lonProperty().get().convertTo(DEGREE).getValue().doubleValue();
                var newLon = oldLon + shiftAngle;
                toShift.lonProperty().set(Quantity.of(newLon, DEGREE));
            }

            break;
        default:
            // do nothing
            break;
        }
    }

    private void applyAllChanges() {
        boolean latSetErrorShown = false;
        boolean lonSetErrorShown = false;
        IHardwareConfiguration hardwareConfig =
            applicationContext
                .getCurrentMission()
                .getCurrentFlightPlan()
                .getLegacyFlightplan()
                .getHardwareConfiguration();
        for (AreaOfInterestCorner corner : this.areaOfInterestCorners) {
            if (!corner.selectedProperty().getValue()) {
                continue;
            }

            // lat
            if (this.latLonAddChecked.get()) {
                shiftLat(corner, this.latChangeProperty());
            } else if (latChangeProperty().get().getDimension() == Dimension.ANGLE) {
                corner.latProperty().set(latChangeProperty().get().convertTo(DEGREE));
            } else {
                if (!latSetErrorShown) {
                    applicationContext.addToast(
                        Toast.of(ToastType.ALERT)
                            .setShowIcon(true)
                            .setText("Absolute latitude values must be given as angle")
                            .create());
                    latSetErrorShown = true;
                }
            }

            // lon
            if (this.latLonAddChecked.get()) {
                double lat = corner.latProperty().get().convertTo(DEGREE).getValue().doubleValue();
                shiftLon(corner, this.lonChangeProperty(), lat);
            } else if (lonChangeProperty().get().getDimension() == Dimension.ANGLE) {
                corner.lonProperty().set(lonChangeProperty().get().convertTo(DEGREE));
            } else {
                if (!lonSetErrorShown) {
                    applicationContext.addToast(
                        Toast.of(ToastType.ALERT)
                            .setShowIcon(true)
                            .setText("Absolute longitude values must be given as angle")
                            .create());
                    lonSetErrorShown = true;
                }
            }

            // alt
            if (this.altAddChecked.get()) {
                var oldAlt = corner.altAboveRefPointProperty().get().convertTo(METER).getValue().doubleValue();
                var newAlt = oldAlt + altChangeProperty().get().convertTo(METER).getValue().doubleValue();
                corner.altAboveRefPointProperty().set(Quantity.of(newAlt, METER));
            } else {
                corner.altAboveRefPointProperty().set(altChangeProperty().get());
            }

            // pitch
            MinMaxPair minMaxPitch =
                new MinMaxPair(
                    hardwareConfig
                        .getPrimaryPayload()
                        .getDescription()
                        .getMinPitch()
                        .convertTo(DEGREE)
                        .getValue()
                        .doubleValue(),
                    hardwareConfig
                        .getPrimaryPayload()
                        .getDescription()
                        .getMaxPitch()
                        .convertTo(DEGREE)
                        .getValue()
                        .doubleValue());
            double newpitch = pitchChangeProperty().get().convertTo(DEGREE).getValue().doubleValue();
            if (this.pitchAddChecked.get()) {
                newpitch += corner.pitchProperty().get().convertTo(DEGREE).getValue().doubleValue();
            }

            newpitch = minMaxPitch.restricByInterval(newpitch);
            corner.pitchProperty().set(Quantity.of(newpitch, DEGREE));

            // yaw
            var newYaw = yawChangeProperty().get().convertTo(DEGREE).getValue().doubleValue();
            if (this.yawAddChecked.get()) {
                newYaw += corner.yawProperty().get().convertTo(DEGREE).getValue().doubleValue();
            }

            while (newYaw >= 360) {
                newYaw -= 360;
            }

            while (newYaw < 0) {
                newYaw += 360;
            }

            corner.yawProperty().set(Quantity.of(newYaw, DEGREE));

            switch (this.triggerChangeStatus.get()) {
            case NO_CHANGE:
                // tja
                break;
            case TRIGGER_ACTIVE:
                corner.triggerImageProperty().set(true);
                break;
            case TRIGGER_INACTIVE:
                corner.triggerImageProperty().set(false);
                break;
            default:
                // lolwat?
                break;
            }

            switch (this.targetChangeStatus.get()) {
            case NO_CHANGE:
                // tja
                break;
            case TRIGGER_ACTIVE:
                corner.targetProperty().set(true);
                break;
            case TRIGGER_INACTIVE:
                corner.targetProperty().set(false);
                break;
            default:
                // lolwat?
                break;
            }

            if (this.noteChangeProperty() != null && this.noteChangeProperty().getValue() != null) {
                if (!this.noteChangeProperty().getValue().isEmpty()) {
                    corner.noteProperty().set(noteChangeProperty().getValue());
                }
            }

            /*if (this.resolutionAddCheckedProperty().get()) {
                var newVal = this.resolutionAddChangeProperty().get().add(this.resolutionAddChangeProperty().get());
                corner.resolutionProperty().set(newVal);
            } else {
                var newVal = this.resolutionAddChangeProperty().get();
                corner.resolutionProperty().set(newVal);
            }*/

            // TODO the current UI isnt letting you choose by which property you wanna adjust the distance, so only one
            // can get implemented by now
            // FIXME!!
            corner.distanceSourceProperty().set(Point.DistanceSource.BY_DISTANCE);
            if (this.distanceAddCheckedProperty().get()) {
                var newVal = this.distanceAddChangeProperty().get().add(this.distanceAddChangeProperty().get());
                corner.distanceProperty().set(newVal);
            } else {
                var newVal = this.distanceAddChangeProperty().get();
                corner.distanceProperty().set(newVal);
            }

            /*
            if (this.frameDiagAddCheckedProperty().get()) {
                var newVal = this.frameDiagAddChangeProperty().get().add(this.frameDiagAddChangeProperty().get());
                corner.frameDiagProperty().set(newVal);
            } else {
                var newVal = this.frameDiagAddChangeProperty().get();
                corner.frameDiagProperty().set(newVal);
            }*/
        }

        // bearing
        List<AreaOfInterestCorner> corners;
        if (this.rotateAddChecked.get()) {
            corners = rotateCornersByRelativeAngle();
        } else {
            corners = rotateCornersByAbsoluteAngle();
        }

        areaOfInterestCorners.forEach((areaOfInterestCorner -> areaOfInterestCorner.removeFPListeners()));
        areaOfInterestCorners.setAll(corners);

        // reflectChangesOfPicArea();

    }

    private List<AreaOfInterestCorner> rotateCornersByAbsoluteAngle() {
        PicAreaCorners legacyCorners = areaOfInterest.getPicArea().getCorners();
        var n = areaOfInterest.getPicArea().getCorners().sizeOfFlightplanContainer();
        double[][] simplePoints = new double[n][2];

        int j = 0;
        for (IFlightplanStatement tmp : legacyCorners) {
            PointWithAltitudes point = new PointWithAltitudes((Point)tmp);
            simplePoints[j] = new double[] {point.getLat(), point.getLon()};
            j++;
        }

        RealMatrix realMatrix = MatrixUtils.createRealMatrix(simplePoints);

        Covariance covariance = new Covariance(realMatrix);
        RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();
        EigenDecomposition ed = new EigenDecomposition(covarianceMatrix);
        var firstEv = ed.getEigenvector(0);
        var angle = Math.acos(firstEv.cosine(new ArrayRealVector(new double[] {1.0, 0.0})));

        var center = areaOfInterest.getCenter();
        var rot =
            Matrix.fromRotationZ(
                Angle.fromRadians(angle)
                    .addDegrees(this.rotateAddChange.get().convertTo(DEGREE).getValue().doubleValue()));

        //            corners.applyVisitorFlatPost(new AFlightplanVisitor() {
        //                @Override
        //                public boolean visit(IFlightplanRelatedObject fpObj) {
        //                    return false;
        //                }
        //            });
        //
        List<AreaOfInterestCorner> corners = new ArrayList<>();
        int i = 1;
        areaOfInterest.getPicArea().setMute(true);
        for (IFlightplanStatement tmp : legacyCorners) {
            PointWithAltitudes point = new PointWithAltitudes((Point)tmp);

            Vec4 x = new Vec4(point.getLat(), point.getLon());
            var centerVec = new Vec4(center.getLatitude().degrees, center.getLongitude().degrees);
            var dx = x.subtract3(centerVec);
            var dy = dx.transformBy3(rot).add3(centerVec);
            point.setLatLon(dy.x, dy.y);

            corners.add(
                new AreaOfInterestCorner(
                    i, point, this.generalSettings, areaOfInterest.getPicArea(), this.settingsManager));

            i++;
        }

        areaOfInterest.getPicArea().setMute(false);
        return corners;
    }

    @NotNull
    private List<AreaOfInterestCorner> rotateCornersByRelativeAngle() {
        var center = areaOfInterest.getCenter();
        var rot =
            Matrix.fromRotationZ(
                Angle.fromDegrees(this.rotateAddChange.get().convertTo(DEGREE).getValue().doubleValue()));

        PicAreaCorners legacyCorners = areaOfInterest.getPicArea().getCorners();

        //            corners.applyVisitorFlatPost(new AFlightplanVisitor() {
        //                @Override
        //                public boolean visit(IFlightplanRelatedObject fpObj) {
        //                    return false;
        //                }
        //            });
        //
        List<AreaOfInterestCorner> corners = new ArrayList<>();
        int i = 1;
        areaOfInterest.getPicArea().setMute(true);
        for (IFlightplanStatement tmp : legacyCorners) {
            PointWithAltitudes point = new PointWithAltitudes((Point)tmp);

            Vec4 x = new Vec4(point.getLat(), point.getLon());
            var centerVec = new Vec4(center.getLatitude().degrees, center.getLongitude().degrees);
            var dx = x.subtract3(centerVec);
            var dy = dx.transformBy3(rot).add3(centerVec);
            point.setLatLon(dy.x, dy.y);

            corners.add(
                new AreaOfInterestCorner(
                    i, point, this.generalSettings, areaOfInterest.getPicArea(), this.settingsManager));

            i++;
        }

        areaOfInterest.getPicArea().setMute(false);
        return corners;
    }

    public String getNoteChange() {
        return noteChange.get();
    }

    public StringProperty noteChangeProperty() {
        return noteChange;
    }

    public ObservableList<TriggerChangeType> getAvailableTriggerChangeTypes() {
        return availableTriggerChangeTypes.get();
    }

    public ListProperty<TriggerChangeType> availableTriggerChangeTypesProperty() {
        return availableTriggerChangeTypes;
    }

    public ObservableList<AreaOfInterestCorner> getAreaOfInterestCorners() {
        return areaOfInterestCorners.get();
    }

    public ListProperty<AreaOfInterestCorner> areaOfInterestCornersProperty() {
        return areaOfInterestCorners;
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

    public boolean getResolutionAddChecked() {
        return resolutionAddChecked.get();
    }

    public BooleanProperty resolutionAddCheckedProperty() {
        return resolutionAddChecked;
    }

    public boolean getDistanceAddChecked() {
        return distanceAddChecked.get();
    }

    public BooleanProperty distanceAddCheckedProperty() {
        return distanceAddChecked;
    }

    public boolean getFrameDiagAddChecked() {
        return frameDiagAddChecked.get();
    }

    public BooleanProperty frameDiagAddCheckedProperty() {
        return frameDiagAddChecked;
    }

    public boolean isRotateAddChecked() {
        return rotateAddChecked.get();
    }

    public BooleanProperty rotateAddCheckedProperty() {
        return rotateAddChecked;
    }

    public Quantity<Dimension.Angle> getRotateAddChange() {
        return rotateAddChange.get();
    }

    public SimpleQuantityProperty<Dimension.Angle> rotateAddChangeProperty() {
        return rotateAddChange;
    }

    public Command getApplyChangesCommand() {
        return applyChangesCommand;
    }

    public Quantity<Dimension.Length> getResolutionAddChange() {
        return resolutionAddChange.get();
    }

    public QuantityProperty<Dimension.Length> resolutionAddChangeProperty() {
        return resolutionAddChange;
    }

    public Quantity<Dimension.Length> getDistanceAddChange() {
        return distanceAddChange.get();
    }

    public QuantityProperty<Dimension.Length> distanceAddChangeProperty() {
        return distanceAddChange;
    }

    public Quantity<Dimension.Length> getFrameDiagAddChange() {
        return frameDiagAddChange.get();
    }

    public QuantityProperty<Dimension.Length> frameDiagAddChangeProperty() {
        return frameDiagAddChange;
    }

    public DelegateCommand getDeleteSelectedAreaOfInterestCornersCommand() {
        return deleteSelectedAreaOfInterestCornersCommand;
    }

    public ObservableList<TargetChangeType> getAvailableTargetChangeType() {
        return availableTargetChangeType.get();
    }

    public ListProperty<TargetChangeType> availableTargetChangeTypeProperty() {
        return availableTargetChangeType;
    }

}
