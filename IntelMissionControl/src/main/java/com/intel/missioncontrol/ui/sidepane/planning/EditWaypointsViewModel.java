/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import static com.intel.missioncontrol.measure.Dimension.Length;
import static com.intel.missioncontrol.measure.Dimension.Speed;
import static com.intel.missioncontrol.measure.Unit.DEGREE;
import static com.intel.missioncontrol.measure.Unit.METER;
import static com.intel.missioncontrol.measure.Unit.METER_PER_SECOND;
import static com.intel.missioncontrol.measure.UnitInfo.ANGLE_DEGREES;
import static com.intel.missioncontrol.measure.UnitInfo.INVARIANT_SPEED_MPS;
import static com.intel.missioncontrol.measure.UnitInfo.LOCALIZED_LENGTH;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleVariantQuantityProperty;
import com.intel.missioncontrol.measure.property.VariantQuantityProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.WayPoint;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.sun.javafx.collections.NonIterableChange;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.globes.Earth;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.asyncfx.beans.binding.BidirectionalValueConverter;
import org.asyncfx.beans.binding.ConversionBindings;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

public class EditWaypointsViewModel extends DialogViewModel<Boolean, Void> {

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

    public enum SelectionFilter {
        ANY,
        NO_IMAGE,
        HAS_WARNINGS
    }

    private static class MuteableListProperty<E> extends SimpleListProperty<E> {
        protected boolean mute;

        MuteableListProperty(ObservableList<E> initialValue) {
            super(initialValue);
        }

        @Override
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
            if (!mute) {
                super.fireValueChangedEvent(change);
            }
        }

        void setMute(boolean mute) {
            this.mute = mute;
        }
    }

    AsyncListProperty<WayPoint> highlightedWaypoints =
        new SimpleAsyncListProperty<WayPoint>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WayPoint>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final MuteableListProperty<WayPoint> waypoints =
        new MuteableListProperty<WayPoint>(
            FXCollections.observableArrayList(item -> new Observable[] {item.selectedProperty()})) {
            @Override
            protected void invalidated() {
                super.invalidated();
                if (mute) {
                    return;
                }

                selectionChanged();
            }
        };

    private void selectionChanged() {
        boolean anySelected = false;
        highlightedWaypoints.clear();
        for (WayPoint wp : waypoints) {
            if (wp.isSelected()) {
                anySelected = true;
                highlightedWaypoints.add(wp);
            }
        }

        bulkEditable.set(editable.get() && anySelected);
    }

    private final StringProperty noteChange = new SimpleStringProperty();

    private final ListProperty<TriggerChangeType> availableTriggerChangeTypes =
        new SimpleListProperty<>(FXCollections.observableArrayList(TriggerChangeType.values()));

    private final ObjectProperty<TriggerChangeType> triggerChangeStatus =
        new SimpleObjectProperty<>(TriggerChangeType.NO_CHANGE);

    private final ObjectProperty<WayPoint> selectedWayPoint = new SimpleObjectProperty<>();

    private final BooleanProperty editable =
        new SimpleBooleanProperty() {
            @Override
            protected void invalidated() {
                super.invalidated();

                boolean anySelected = false;
                for (WayPoint wp : waypoints) {
                    if (wp.isSelected()) {
                        anySelected = true;
                        break;
                    }
                }

                bulkEditable.set(get() && anySelected);
            }
        };

    private final BooleanProperty bulkEditable = new SimpleBooleanProperty();

    private final BooleanProperty latLonAddChecked;
    private final VariantQuantityProperty latChange;
    private final VariantQuantityProperty lonChange;

    private final BooleanProperty altAddChecked;
    private final QuantityProperty<Length> altChange;

    private final BooleanProperty rollAddChecked;
    private final QuantityProperty<Angle> rollChange;

    private final BooleanProperty pitchAddChecked;
    private final QuantityProperty<Angle> pitchChange;

    private final BooleanProperty speedAddChecked;
    private final QuantityProperty<Speed> speedChange;

    private final BooleanProperty yawAddChecked;
    private final QuantityProperty<Angle> yawChange;

    private final IApplicationContext applicationContext;
    private final ParameterizedCommand<SelectionFilter> selectCommand;
    private final ParameterizedCommand<SelectionFilter> deselectCommand;
    private final Command invertSelectionCommand;
    private final Command deleteSelectedWaypointsCommand;
    private final Command applyChangesCommand;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final ISelectionManager selectionManager;
    private final Quantity<Speed> maxSpeedMps;
    private final Quantity<Angle> minPitch;
    private final Quantity<Angle> maxPitch;
    private final Quantity<Angle> minRoll;
    private final Quantity<Angle> maxRoll;
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Inject
    public EditWaypointsViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            ISelectionManager selectionManager) {
        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.selectionManager = selectionManager;
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

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

        ConversionBindings.bindBidirectional(
            editable,
            fp.recalculateOnEveryChangeProperty(),
            new BidirectionalValueConverter<Boolean, Boolean>() {
                @Override
                public Boolean convert(Boolean value) {
                    return !value;
                }

                @Override
                public Boolean convertBack(Boolean value) {
                    return !value;
                }
            });

        fp.waypointsProperty()
            .addListener(
                ((observable, oldValue, newValue) -> {
                    // if the set of waypoints changes this is typicall due to
                    // recompuation of flight plan which will anyway crate new unselected objects,
                    // or due to deleting of the selection, where is also doen't hurt if we deselect first
                    deselectWaypoints(SelectionFilter.ANY);
                    waypoints.setAll(fp.waypointsProperty());
                }));

        waypoints.setAll(fp.waypointsProperty());

        selectCommand = new ParameterizedDelegateCommand<>(this::selectWaypoints);
        deselectCommand = new ParameterizedDelegateCommand<>(this::deselectWaypoints);
        invertSelectionCommand = new DelegateCommand(this::invertSelection);
        deleteSelectedWaypointsCommand = new DelegateCommand(this::deleteSelectedWaypoints, bulkEditable);
        applyChangesCommand = new DelegateCommand(this::applyAllChanges, bulkEditable);

        highlightedWaypoints.bindBidirectional(selectionManager.getHighlighted());
        selectedWayPoint.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectionManager.setSelection(newValue.getLegacyWaypoint());
                } else {
                    selectionManager.setSelection(fp.getLegacyFlightplan());
                }
            });

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

    public Quantity<Speed> getMaxSpeedMps() {
        return maxSpeedMps;
    }

    public Quantity<Angle> getMinPitch() {
        return minPitch;
    }

    public Quantity<Angle> getMaxPitch() {
        return maxPitch;
    }

    public Quantity<Angle> getMinRoll() {
        return minRoll;
    }

    public Quantity<Angle> getMaxRoll() {
        return maxRoll;
    }

    public ParameterizedCommand<SelectionFilter> getSelectCommand() {
        return selectCommand;
    }

    public ParameterizedCommand<SelectionFilter> getDeselectCommand() {
        return deselectCommand;
    }

    public Command getInvertSelectionCommand() {
        return invertSelectionCommand;
    }

    public Command getDeleteSelectedWaypointsCommand() {
        return deleteSelectedWaypointsCommand;
    }

    public Command getApplyChangesCommand() {
        return applyChangesCommand;
    }

    public ReadOnlyListProperty<WayPoint> waypointsProperty() {
        return waypoints;
    }

    public ReadOnlyListProperty<TriggerChangeType> availableTriggerChangeTypesProperty() {
        return availableTriggerChangeTypes;
    }

    public ObjectProperty<TriggerChangeType> triggerChangeStatusProperty() {
        return triggerChangeStatus;
    }

    public BooleanProperty speedAddCheckedProperty() {
        return speedAddChecked;
    }

    public QuantityProperty<Speed> speedChangeProperty() {
        return speedChange;
    }

    public VariantQuantityProperty latChangeProperty() {
        return latChange;
    }

    public BooleanProperty latLonAddCheckedProperty() {
        return latLonAddChecked;
    }

    public VariantQuantityProperty lonChangeProperty() {
        return lonChange;
    }

    public BooleanProperty altAddCheckedProperty() {
        return altAddChecked;
    }

    public QuantityProperty<Length> altChangeProperty() {
        return altChange;
    }

    public BooleanProperty rollAddCheckedProperty() {
        return rollAddChecked;
    }

    public QuantityProperty<Angle> rollChangeProperty() {
        return rollChange;
    }

    public BooleanProperty pitchAddCheckedProperty() {
        return pitchAddChecked;
    }

    public QuantityProperty<Angle> pitchChangeProperty() {
        return pitchChange;
    }

    public BooleanProperty yawAddCheckedProperty() {
        return yawAddChecked;
    }

    public QuantityProperty<Angle> yawChangeProperty() {
        return yawChange;
    }

    public ObjectProperty<WayPoint> selectedWayPointProperty() {
        return selectedWayPoint;
    }

    public ReadOnlyBooleanProperty bulkEditableProperty() {
        return bulkEditable;
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public Property<String> noteChangeProperty() {
        return noteChange;
    }

    @Override
    protected void onClosing() {
        selectionManager.setSelection(null);
        selectionManager.getHighlighted().clear();
        waypoints.setMute(true);
        for (WayPoint wp : waypoints) {
            wp.selectedProperty().setValue(false);
        }

        super.onClosing();
    }

    private void selectWaypoints(SelectionFilter filter) {
        waypoints.setMute(true);
        switch (filter) {
        case ANY:
            for (WayPoint wp : waypoints) {
                wp.setSelected(true);
            }

            break;
        case NO_IMAGE:
            for (WayPoint wp : waypoints) {
                if (!wp.triggerImageHereCopterModeProperty().get()) {
                    wp.setSelected(true);
                }
            }

            break;
        case HAS_WARNINGS:
            for (WayPoint wp : waypoints) {
                if (wp.warningProperty().get()) {
                    wp.setSelected(true);
                }
            }

            break;
        }

        waypoints.setMute(false);
        waypoints.fireValueChangedEvent(new NonIterableChange.SimpleUpdateChange<>(0, waypoints));
        selectionChanged();
    }

    private void deselectWaypoints(SelectionFilter filter) {
        waypoints.setMute(true);
        switch (filter) {
        case ANY:
            for (WayPoint wp : waypoints) {
                wp.setSelected(false);
            }

            break;
        case NO_IMAGE:
            for (WayPoint wp : waypoints) {
                if (!wp.triggerImageHereCopterModeProperty().get()) {
                    wp.setSelected(false);
                }
            }

            break;
        case HAS_WARNINGS:
            for (WayPoint wp : waypoints) {
                if (wp.warningProperty().get()) {
                    wp.setSelected(false);
                }
            }

            break;
        }

        waypoints.setMute(false);
        waypoints.fireValueChangedEvent(new NonIterableChange.SimpleUpdateChange<>(0, waypoints));
        selectionChanged();
    }

    private void invertSelection() {
        waypoints.setMute(true);
        for (WayPoint wp : waypoints) {
            wp.setSelected(!wp.isSelected());
        }

        waypoints.setMute(false);
        waypoints.fireValueChangedEvent(new NonIterableChange.SimpleUpdateChange<>(0, waypoints));
        selectionChanged();
    }

    private void deleteSelectedWaypoints() {
        if (!dialogService.requestConfirmation(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.planning.EditWaypointsViewModel.deleteConfirmTitle"),
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.planning.EditWaypointsViewModel.deleteConfirmMsg"))) {
            return;
        }

        List<WayPoint> wayPoints = new ArrayList<>();
        for (WayPoint wp : this.waypoints) {
            if (!wp.selectedProperty().get()) {
                continue;
            }

            wayPoints.add(wp);
        }

        if (wayPoints.isEmpty()) {
            return;
        }

        FlightPlan flightPlan = applicationContext.getCurrentMission().getCurrentFlightPlan();
        flightPlan.deleteWaypoints(wayPoints);
    }

    private void shiftLat(WayPoint toShift, VariantQuantityProperty q) {
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

    private void shiftLon(WayPoint toShift, VariantQuantityProperty q, double lat) {
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
        for (WayPoint wp : this.waypoints) {
            if (!wp.selectedProperty().getValue()) {
                continue;
            }

            // lat
            if (this.latLonAddChecked.get()) {
                shiftLat(wp, this.latChangeProperty());
            } else if (latChangeProperty().get().getDimension() == Dimension.ANGLE) {
                wp.latProperty().set(latChangeProperty().get().convertTo(DEGREE));
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
                double lat = wp.latProperty().get().convertTo(DEGREE).getValue().doubleValue();
                shiftLon(wp, this.lonChangeProperty(), lat);
            } else if (lonChangeProperty().get().getDimension() == Dimension.ANGLE) {
                wp.lonProperty().set(lonChangeProperty().get().convertTo(DEGREE));
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
                var oldAlt = wp.altitudeAboveRProperty().get().convertTo(METER).getValue().doubleValue();
                var newAlt = oldAlt + altChangeProperty().get().convertTo(METER).getValue().doubleValue();
                wp.altitudeAboveRProperty().set(Quantity.of(newAlt, METER));
            } else {
                wp.altitudeAboveRProperty().set(altChangeProperty().get());
            }

            // roll
            MinMaxPair minMaxRoll =
                new MinMaxPair(
                    hardwareConfig
                        .getPrimaryPayload()
                        .getDescription()
                        .getMinRoll()
                        .convertTo(DEGREE)
                        .getValue()
                        .doubleValue(),
                    hardwareConfig
                        .getPrimaryPayload()
                        .getDescription()
                        .getMaxRoll()
                        .convertTo(DEGREE)
                        .getValue()
                        .doubleValue());
            var newRoll = rollChangeProperty().get().convertTo(DEGREE).getValue().doubleValue();
            if (this.rollAddChecked.get()) {
                newRoll += wp.rollProperty().get().convertTo(DEGREE).getValue().doubleValue();
            }

            newRoll = minMaxRoll.restricByInterval(newRoll);
            wp.rollProperty().set(Quantity.of(newRoll, DEGREE));

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
                newpitch += wp.pitchProperty().get().convertTo(DEGREE).getValue().doubleValue();
            }

            newpitch = minMaxPitch.restricByInterval(newpitch);
            wp.pitchProperty().set(Quantity.of(newpitch, DEGREE));

            // yaw
            var newYaw = yawChangeProperty().get().convertTo(DEGREE).getValue().doubleValue();
            if (this.yawAddChecked.get()) {
                newYaw += wp.yawProperty().get().convertTo(DEGREE).getValue().doubleValue();
            }

            while (newYaw >= 360) {
                newYaw -= 360;
            }

            while (newYaw < 0) {
                newYaw += 360;
            }

            wp.yawProperty().set(Quantity.of(newYaw, DEGREE));

            switch (this.triggerChangeStatusProperty().get()) {
            case NO_CHANGE:
                // tja
                break;
            case TRIGGER_ACTIVE:
                wp.triggerImageHereCopterModeProperty().set(true);
                break;
            case TRIGGER_INACTIVE:
                wp.triggerImageHereCopterModeProperty().set(false);
                break;
            default:
                // lolwat?
                break;
            }

            // speed
            MinMaxPair minMaxSpeed =
                new MinMaxPair(
                    0,
                    hardwareConfig
                        .getPlatformDescription()
                        .getMaxPlaneSpeed()
                        .convertTo(METER_PER_SECOND)
                        .getValue()
                        .doubleValue());
            var newSpeed = speedChangeProperty().get().convertTo(METER_PER_SECOND).getValue().doubleValue();
            if (this.speedAddChecked.get()) {
                newSpeed += wp.speedProperty().get().convertTo(METER_PER_SECOND).getValue().doubleValue();
            }

            newSpeed = minMaxSpeed.restricByInterval(newSpeed);
            wp.speedProperty().set(Quantity.of(newSpeed, METER_PER_SECOND));

            if (this.noteChangeProperty() != null && this.noteChangeProperty().getValue() != null) {
                if (!this.noteChangeProperty().getValue().isEmpty()) {
                    wp.bodyProperty().set(noteChangeProperty().getValue());
                }
            }
        }
    }
}
