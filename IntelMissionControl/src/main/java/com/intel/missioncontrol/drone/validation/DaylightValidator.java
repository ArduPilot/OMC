/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import eu.mavinci.desktop.gui.doublepanel.sunangles.RelevantSunElevation;
import eu.mavinci.desktop.gui.doublepanel.sunangles.SunComputer;
import eu.mavinci.flightplan.computation.FPsim;
import gov.nasa.worldwind.geom.Position;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import javafx.beans.binding.Bindings;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

public class DaylightValidator implements IFlightValidator {

    public interface Factory {
        DaylightValidator create(CancellationSource cancellationSource);
    }

    private final Duration updateInterval = Duration.ofSeconds(1);

    private final ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);
    private final AirspacesProvidersSettings airspacesProvidersSettings =
        StaticInjector.getInstance(AirspacesProvidersSettings.class);
    private final IQuantityStyleProvider quantityStyleProvider =
        StaticInjector.getInstance(IQuantityStyleProvider.class);
    private final AdaptiveQuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Duration> estimatedFlightDuration = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Position> landingPosition = new SimpleAsyncObjectProperty<>(this);

    private final AsyncObjectProperty<Instant> currentTimestamp = new SimpleAsyncObjectProperty<>(this);

    @Inject
    DaylightValidator(
            IFlightValidationService flightValidationService, @Assisted CancellationSource cancellationSource) {
        estimatedFlightDuration.bind(
            Bindings.createObjectBinding(
                () -> {
                    FlightPlan fp = flightValidationService.flightPlanProperty().get();
                    if (fp == null) {
                        return null;
                    }

                    FPsim.SimResultData simResult = fp.getLegacyFlightplan().getFPsim().getSimResult();
                    if (simResult == null) {
                        return null;
                    }

                    return Duration.ofSeconds((long)simResult.flightTime);
                },
                flightValidationService.flightPlanProperty()));

        landingPosition.bind(
            PropertyPath.from(flightValidationService.flightPlanProperty())
                .selectObject(FlightPlan::landingPositionProperty));

        // TODO handler for flight time estimation change within flightplan

        // update time periodically:
        currentTimestamp.set(Instant.now());
        Dispatcher.background()
            .runLaterAsync(
                () -> currentTimestamp.set(Instant.now()), updateInterval, updateInterval, cancellationSource);

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    Position pos = landingPosition.get();
                    if (pos == null
                            || (pos.getLatitude().getDegrees() == 0.0 && pos.getLongitude().getDegrees() == 0.0)) {
                        return new FlightValidationStatus(
                            AlertType.WARNING, languageHelper.getString(DaylightValidator.class, "invalid"));
                    }

                    Duration estimatedFlightDuration = this.estimatedFlightDuration.get();
                    if (estimatedFlightDuration == null) {
                        return new FlightValidationStatus(
                            AlertType.WARNING, languageHelper.getString(DaylightValidator.class, "invalid"));
                    }

                    Duration minTimeBetweenLandingAndSunset =
                        Duration.ofMinutes(Math.round(airspacesProvidersSettings.minimumTimeLandingProperty().get()));

                    Instant timestampTakeoff = currentTimestamp.get();
                    Instant timestampLanding = timestampTakeoff.plus(estimatedFlightDuration);
                    Instant timestampMargin = timestampLanding.plus(minTimeBetweenLandingAndSunset);

                    // compute start of today in UTC
                    ZoneId zoneId = ZoneId.of("UTC");
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(timestampTakeoff, zoneId);
                    ZonedDateTime zdtStart = zdt.toLocalDate().atStartOfDay(zoneId);

                    SunComputer sun =
                        new SunComputer(pos, Date.from(zdtStart.toInstant()), TimeZone.getTimeZone(zoneId));
                    SunComputer.SunComputerResults sunRiseSet =
                        sun.getSunRiseSet(RelevantSunElevation.sunrise.thresholdDeg);
                    if (sunRiseSet.getSunRise().isBefore(sunRiseSet.getSunSet())) {
                        if (timestampTakeoff.isBefore(sunRiseSet.getSunRise())) {
                            // start before sunrise
                            return new FlightValidationStatus(
                                AlertType.WARNING,
                                languageHelper.getString(
                                    DaylightValidator.class,
                                    "stillDark",
                                    quantityFormat.format(
                                        Quantity.of(
                                            Duration.between(timestampTakeoff, sunRiseSet.getSunRise()).toMinutes(),
                                            Unit.MINUTE))));
                        }

                        if (timestampTakeoff.isAfter(sunRiseSet.getSunSet())) {
                            // start after sunset
                            SunComputer sunNext =
                                new SunComputer(
                                    pos,
                                    Date.from(zdtStart.toInstant().plusSeconds(60 * 60 * 24)),
                                    TimeZone.getTimeZone(zoneId));
                            SunComputer.SunComputerResults sunRiseSetNext =
                                sunNext.getSunRiseSet(RelevantSunElevation.sunrise.thresholdDeg);

                            return new FlightValidationStatus(
                                AlertType.WARNING,
                                languageHelper.getString(
                                    DaylightValidator.class,
                                    "stillDark",
                                    quantityFormat.format(
                                        Quantity.of(
                                            Duration.between(timestampTakeoff, sunRiseSetNext.getSunRise()).toMinutes(),
                                            Unit.MINUTE))));
                        }

                        if (timestampMargin.isAfter(sunRiseSet.getSunSet())) {
                            // landing too late
                            if (timestampLanding.isBefore(sunRiseSet.getSunSet())) {
                                return new FlightValidationStatus(
                                    AlertType.WARNING,
                                    languageHelper.getString(
                                        DaylightValidator.class,
                                        "soonDark1",
                                        quantityFormat.format(
                                            Quantity.of(
                                                Duration.between(timestampLanding, sunRiseSet.getSunSet()).toMinutes(),
                                                Unit.MINUTE)),
                                        quantityFormat.format(
                                            Quantity.of(minTimeBetweenLandingAndSunset.toMinutes(), Unit.MINUTE))));
                            } else {
                                return new FlightValidationStatus(
                                    AlertType.WARNING,
                                    languageHelper.getString(
                                        DaylightValidator.class,
                                        "soonDark2",
                                        quantityFormat.format(
                                            Quantity.of(
                                                Duration.between(sunRiseSet.getSunSet(), timestampLanding).toMinutes(),
                                                Unit.MINUTE)),
                                        quantityFormat.format(
                                            Quantity.of(minTimeBetweenLandingAndSunset.toMinutes(), Unit.MINUTE))));
                            }
                        }
                    } else {
                        // day starts with sunset
                        if (timestampTakeoff.isBefore(sunRiseSet.getSunRise())) {
                            // start before sunrise
                            return new FlightValidationStatus(
                                AlertType.WARNING,
                                languageHelper.getString(
                                    DaylightValidator.class,
                                    "stillDark",
                                    quantityFormat.format(
                                        Quantity.of(
                                            Duration.between(timestampTakeoff, sunRiseSet.getSunRise()).toMinutes(),
                                            Unit.MINUTE))));
                        } else {
                            // landing before next sunset?
                            SunComputer sunNext =
                                new SunComputer(
                                    pos,
                                    Date.from(zdtStart.toInstant().plus(Duration.ofDays(1))),
                                    TimeZone.getTimeZone(zoneId));
                            SunComputer.SunComputerResults sunRiseSetNext =
                                sunNext.getSunRiseSet(RelevantSunElevation.sunrise.thresholdDeg);

                            if (sunRiseSetNext.getSunSet().isBefore(timestampMargin)) {
                                // landing too late
                                return new FlightValidationStatus(
                                    AlertType.WARNING,
                                    languageHelper.getString(
                                        DaylightValidator.class,
                                        "soonDark1",
                                        quantityFormat.format(
                                            Quantity.of(
                                                Duration.between(timestampLanding, sunRiseSetNext.getSunSet())
                                                    .toMinutes(),
                                                Unit.MINUTE)),
                                        quantityFormat.format(
                                            Quantity.of(minTimeBetweenLandingAndSunset.toMinutes(), Unit.MINUTE))));
                            }
                        }
                    }

                    return new FlightValidationStatus(
                        AlertType.COMPLETED, languageHelper.getString(DaylightValidator.class, "okMessage"));
                },
                currentTimestamp,
                airspacesProvidersSettings.minimumTimeLandingProperty(),
                landingPosition));
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.DAYLIGHT;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IResolveAction> getFirstResolveAction() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IResolveAction> getSecondResolveAction() {
        return null;
    }

}
