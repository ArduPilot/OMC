/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.time.Duration;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

/** Check if current drone position is close to takeoff position specified in flight plan */
public class TakeoffPositionValidator implements IFlightValidator {

    public interface Factory {
        TakeoffPositionValidator create(CancellationSource cancellationSource);
    }

    // TODO: json file setting
    private final double maxDistanceMeters = 10.0;
    private final Duration updateInterval = Duration.ofSeconds(1);

    private final Globe globe;

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    private final AsyncObjectProperty<Position> position = new SimpleAsyncObjectProperty<>(this);

    @Inject
    TakeoffPositionValidator(
            IFlightValidationService flightValidationService,
            IQuantityStyleProvider quantityStyleProvider,
            ILanguageHelper languageHelper,
            IWWGlobes wwGlobes,
            @Assisted CancellationSource cancellationSource) {
        // TODO: remove dependency on wwGlobes
        globe = wwGlobes.getDefaultGlobe();

        ReadOnlyObjectProperty<Position> fpTakeoffPosition =
            propertyPathStore
                .from(flightValidationService.flightPlanProperty())
                .selectReadOnlyObject(FlightPlan::takeoffPositionProperty);

        // update position periodically:
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.runLaterAsync(
            () -> {
                IDrone drone = flightValidationService.droneProperty().get();
                PropertyHelper.setValueSafe(position, drone != null ? drone.positionProperty().get() : null);
            },
            Duration.ZERO,
            updateInterval,
            cancellationSource);

        ReadOnlyAsyncBooleanProperty telemetryOld =
            propertyPathStore
                .from(flightValidationService.droneProperty())
                .selectReadOnlyAsyncBoolean(IDrone::positionTelemetryOldProperty);

        ReadOnlyAsyncObjectProperty<FlightSegment> flightSegment =
            propertyPathStore
                .from(flightValidationService.droneProperty())
                .selectReadOnlyAsyncObject(IDrone::flightSegmentProperty);

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    FlightSegment segment = flightSegment.get();
                    if (segment == null) {
                        segment = FlightSegment.UNKNOWN;
                    }

                    if (segment != FlightSegment.ON_GROUND && segment != FlightSegment.UNKNOWN) {
                        return new FlightValidationStatus(
                            AlertType.COMPLETED,
                            languageHelper.getString(TakeoffPositionValidator.class, "inFlightMessage"));
                    }

                    Position pos = position.get();
                    if (pos == null || telemetryOld.get() || segment == FlightSegment.UNKNOWN) {
                        return new FlightValidationStatus(
                            AlertType.LOADING,
                            languageHelper.getString(TakeoffPositionValidator.class, "loadingMessage"));
                    }

                    Position fpTakeoffPos = fpTakeoffPosition.get();
                    if (fpTakeoffPos == null) {
                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(TakeoffPositionValidator.class, "flightPlanIssue"));
                    }

                    double distanceMeters = getDistance(pos, fpTakeoffPos);
                    if (distanceMeters > maxDistanceMeters) {
                        AdaptiveQuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
                        quantityFormat.setSignificantDigits(3);

                        String distanceString = quantityFormat.format(Quantity.of(distanceMeters, Unit.METER));

                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(
                                TakeoffPositionValidator.class, "tooFarAwayMessage", distanceString));
                    }

                    return new FlightValidationStatus(
                        AlertType.COMPLETED, languageHelper.getString(TakeoffPositionValidator.class, "okMessage"));
                },
                fpTakeoffPosition,
                position,
                telemetryOld,
                flightSegment));
    }

    private double getDistance(Position pos, Position fpTakeoffPos) {
        Vec4 vPos = globe.computePointFromPosition(pos);
        Vec4 vToff = globe.computePointFromPosition(fpTakeoffPos);

        return vPos.distanceTo3(vToff);
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.TAKEOFF_POSITION;
    }

}
