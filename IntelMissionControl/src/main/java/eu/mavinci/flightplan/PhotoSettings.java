/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.OperationLevel;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.CPhotoSettings;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IRecalculateable;
import eu.mavinci.core.helper.CMathHelper;

public class PhotoSettings extends CPhotoSettings implements IRecalculateable {

    public static final String KEY = "eu.mavinci.flightplan.PhotoSettings";
    public static final String KEY_MAXRoll = KEY + ".MaxRoll";
    public static final String KEY_MAXNick = KEY + ".MaxNick";
    public static final String KEY_MAXgroundSpeed = KEY + ".MaxGroundSpeed";
    public static final String KEY_Intervall = KEY + ".Intervall";
    public static final String KEY_YAW = KEY + ".yaw";
    public static final String KEY_OnlySingleDirection = KEY + ".onlySingleDirection";
    public static final String KEY_TO_STRING = KEY + ".toString";
    public static final String KEY_ON = KEY + ".on";
    public static final String KEY_OFF = KEY + ".off";

    public static final String KEY_AltAdjustModes = KEY + ".AltAdjustModes";
    public static final String KEY_IsMultiFP = KEY + ".isMultiFP";

    public PhotoSettings(double maxRoll, double maxNick, double mintimeinterval, IFlightplanContainer parent) {
        super(maxRoll, maxNick, mintimeinterval, parent);
    }

    public PhotoSettings(IFlightplanContainer parent) {
        super(parent);
    }

    public PhotoSettings(PhotoSettings source) {
        super(source.maxRoll, source.maxNick, source.mintimeinterval, source.cont);
        this.altAdjustMode = source.altAdjustMode;
        this.maxGroundSpeedMPSec = source.maxGroundSpeedMPSec;
        this.maxGroundSpeedAutomatic = source.maxGroundSpeedAutomatic;
        this.stoppingAtWaypoints = source.stoppingAtWaypoints;
        this.gsdTolerance = source.gsdTolerance;
    }

    public String toString() {
        return StaticInjector.getInstance(ILanguageHelper.class)
            .getString(
                KEY_TO_STRING + "." + OperationLevel.DEBUG,
                CMathHelper.round(maxRoll, 1),
                CMathHelper.round(maxNick, 1),
                CMathHelper.round(mintimeinterval, 2));
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new PhotoSettings(this);
    }

    public void setDefaultsFromCamera(IGenericCameraConfiguration camera) {
        setMaxNick(camera.getDescription().getMaxPitch().convertTo(Unit.DEGREE).getValue().doubleValue());
        setMaxRoll(camera.getDescription().getMaxRoll().convertTo(Unit.DEGREE).getValue().doubleValue());
        setMinTimeInterval(
            camera.getLens().getDescription().getMinRepTime().convertTo(Unit.SECOND).getValue().doubleValue());
    }

    @Override
    public Flightplan getFlightplan() {
        return (Flightplan)super.getFlightplan();
    }

    @Override
    public boolean doSubRecalculationStage1() {
        silentResetMaxGroundSpeed();
        Flightplan fp = getFlightplan();
        Ensure.notNull(fp, "fp");
        IGenericCameraConfiguration cameraConfig =
            fp.getHardwareConfiguration().getPrimaryPayload(IGenericCameraConfiguration.class);

        setDefaultsFromCamera(cameraConfig);
        AltitudeAdjustModes shiftAltitudes = getAltitudeAdjustMode();
        return true;
    }

    @Override
    public boolean doSubRecalculationStage2() {
        return true;
    }
}
