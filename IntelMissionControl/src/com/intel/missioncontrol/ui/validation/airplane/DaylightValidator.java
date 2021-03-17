/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.airplane;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.desktop.gui.doublepanel.sunangles.RelevantSunElevation;
import eu.mavinci.desktop.gui.doublepanel.sunangles.SunComputer;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.computation.FPsim;
import eu.mavinci.plane.IAirplane;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * check B-08: Current time is outside of daylight hours for this location. check B-09: Landing time is outside of
 * daylight hours.
 */
public class DaylightValidator extends AirplaneValidatorBase {

    public interface Factory {
        DaylightValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = DaylightValidator.class.getName();
    private final AirspacesProvidersSettings settings;

    public static final long minCheckingIntervalSec = 30;
    public static final long minCheckingIntervalMs = minCheckingIntervalSec * 1000;

    @Inject
    public DaylightValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addPositionOrientationListener();
        addRecomputeListener();
        setOkMessage(languageHelper.getString(className + ".okMessage"));
        settings = DependencyInjector.getInstance().getInstanceOf(AirspacesProvidersSettings.class);
        settings.minimumTimeLandingProperty().addListener((a, b, c) -> invalidate());
    }

    private long lastCheckSystemMs;
    private double lastCheckDroneSec;

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        long currentCheckSystemMs = System.currentTimeMillis();
        if (currentCheckSystemMs < lastCheckSystemMs + minCheckingIntervalMs && lastCheckDroneSec != 0) {
            return true;
        }

        lastCheckSystemMs = currentCheckSystemMs;

        final Flightplan fp = flightplanAirplanePair.getFlightplan();
        if (fp == null) {
            return false;
        }

        final IAirplane plane = flightplanAirplanePair.getPlane();
        long timestampTakeoff;

        double currentDroneTimeSec;
        try {
            currentDroneTimeSec = plane.getAirplaneCache().getCurTime();
            if (currentDroneTimeSec < lastCheckDroneSec + minCheckingIntervalSec) {
                return true;
            }

            timestampTakeoff = (long)(1000 * plane.getAirplaneCache().getPosition().getTimestamp());
        } catch (AirplaneCacheEmptyException e) {
            currentDroneTimeSec = System.currentTimeMillis() / 1000.;
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
            try {
                timestampTakeoff = dateFormatLocal.parse(dateFormatGmt.format(new Date())).getTime();
            } catch (ParseException e1) {
                Debug.getLog().log(Level.WARNING, "could not extract UTC time", e);
                return false;
            }
        }

        FPsim.SimResultData simResultData = fp.getFPsim().getSimResult();
        if (simResultData == null) {
            return false;
        }

        long timestampLanding = timestampTakeoff + (long)(simResultData.flightTime * 1000);
        double timestampMargin = timestampLanding + settings.minimumTimeLandingProperty().doubleValue() * 60 * 1000;

        // compute start of today in UTC
        ZoneId zoneId = ZoneId.of("UTC");
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestampTakeoff), zoneId);
        ZonedDateTime zdtStart = zdt.toLocalDate().atStartOfDay(zoneId);

        SunComputer sun =
            new SunComputer(
                fp.getLandingpoint().getLatLon(), Date.from(zdtStart.toInstant()), TimeZone.getTimeZone(zoneId));
        SunComputer.SunComputerResults sunRiseSet = sun.getSunRiseSet(RelevantSunElevation.sunrise.thresholdDeg);
        if (sunRiseSet.timeRise < sunRiseSet.timeSet) {
            if (timestampTakeoff < sunRiseSet.timeRise) {
                // start before sunrise
                addWarning(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.validation.airplane.DaylightValidator.stillDark",
                        StringHelper.secToShortDHMS((sunRiseSet.timeRise - timestampTakeoff) / 1000.)),
                    ValidationMessageCategory.NORMAL);
            }

            if (timestampTakeoff > sunRiseSet.timeSet) {
                // start after sunset
                SunComputer sunNext =
                    new SunComputer(
                        fp.getLandingpoint().getLatLon(),
                        Date.from(zdtStart.toInstant().plusSeconds(60 * 60 * 24)),
                        TimeZone.getTimeZone(zoneId));
                SunComputer.SunComputerResults sunRiseSetNext =
                    sunNext.getSunRiseSet(RelevantSunElevation.sunrise.thresholdDeg);

                addWarning(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.validation.airplane.DaylightValidator.stillDark",
                        StringHelper.secToShortDHMS((sunRiseSetNext.timeRise - timestampTakeoff) / 1000.)),
                    ValidationMessageCategory.NORMAL);
            }

            if (timestampMargin >= sunRiseSet.timeSet) {
                // landing too late
                double timeDiff = (timestampLanding - sunRiseSet.timeSet) / 1000.;
                if (timeDiff <= 0) {
                    addWarning(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.validation.airplane.DaylightValidator.soonDark1",
                            StringHelper.secToShortDHMS(-timeDiff),
                            StringHelper.secToShortDHMS(settings.minimumTimeLandingProperty().doubleValue() * 60)),
                        ValidationMessageCategory.NORMAL);
                } else {
                    addWarning(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.validation.airplane.DaylightValidator.soonDark2",
                            StringHelper.secToShortDHMS(timeDiff),
                            StringHelper.secToShortDHMS(settings.minimumTimeLandingProperty().doubleValue() * 60)),
                        ValidationMessageCategory.NORMAL);
                }
            }
        } else {
            // day starts with sunset
            if (timestampMargin < sunRiseSet.timeSet) {
                // flight before sunset
            } else if (timestampTakeoff < sunRiseSet.timeRise) {
                // start before sunrise
                addWarning(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.validation.airplane.DaylightValidator.stillDark",
                        StringHelper.secToShortDHMS((sunRiseSet.timeRise - timestampTakeoff) / 1000.)),
                    ValidationMessageCategory.NORMAL);
            } else {
                // landing before next sunset?
                SunComputer sunNext =
                    new SunComputer(
                        fp.getLandingpoint().getLatLon(),
                        Date.from(zdtStart.toInstant().plusSeconds(60 * 60 * 24)),
                        TimeZone.getTimeZone(zoneId));
                SunComputer.SunComputerResults sunRiseSetNext =
                    sunNext.getSunRiseSet(RelevantSunElevation.sunrise.thresholdDeg);

                if (sunRiseSetNext.timeSet <= timestampMargin) {
                    // landing too late
                    addWarning(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.validation.airplane.DaylightValidator.soonDark1",
                            StringHelper.secToShortDHMS((sunRiseSetNext.timeSet - timestampLanding) / 1000.),
                            StringHelper.secToShortDHMS(settings.minimumTimeLandingProperty().doubleValue() * 60)),
                        ValidationMessageCategory.NORMAL);
                }
            }
        }

        lastCheckDroneSec = currentDroneTimeSec;

        return true;
    }

}
