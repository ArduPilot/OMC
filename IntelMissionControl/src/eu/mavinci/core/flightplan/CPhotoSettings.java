/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.desktop.helper.MathHelper;

public abstract class CPhotoSettings extends CAFlightplanstatement implements IFlightplanRelatedObject, IMuteable {

    public static final double MIN_SPEED = 0.1;
    protected double maxRoll = DEFAULT_MAX_ROLL;
    protected double maxNick = DEFAULT_MAX_NICK;

    public static final double DEFAULT_MAX_ROLL = 8;
    public static final double DEFAULT_MAX_NICK = 8;

    public static final FlightplanSpeedModes DEFAULT_SPEEDMODE = FlightplanSpeedModes.AUTOMATIC_DYNAMIC;

    protected FlightplanSpeedModes maxGroundSpeedAutomatic = DEFAULT_SPEEDMODE;

    protected boolean stoppingAtWaypoints;

    protected double maxGroundSpeedMPSec = -1; // in m/sec, -1 means not computed jet

    protected double gsdTolerance = 0.3;

    /**
     * in km/h to get images sharp... -1 means no limit
     *
     * @return
     */
    public double getMaxGroundSpeedMPSec() {
        return maxGroundSpeedMPSec;
    }

    public boolean setMaxGroundSpeedMPSec(double maxGroundSpeedMPSec) {
        CFlightplan fp = getFlightplan();
        if (fp != null) {
            double max =
                fp.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMaxPlaneSpeed()
                    .convertTo(Unit.METER_PER_SECOND)
                    .getValue()
                    .doubleValue();

            // this check here is left only to not change throw exception behaviour
            if (max < maxGroundSpeedMPSec) {
                throw new IllegalArgumentException("Specified ground speed exceeds the platform limits.");
            }

            // this line here guarantees that the speed is not less than the MIN_SPEED
            maxGroundSpeedMPSec = MathHelper.intoRange(maxGroundSpeedMPSec, MIN_SPEED, max);
        }

        if (this.maxGroundSpeedMPSec == maxGroundSpeedMPSec) {
            return false;
        }

        this.maxGroundSpeedMPSec = maxGroundSpeedMPSec;
        informChangeListener();
        return true;
    }

    public FlightplanSpeedModes getMaxGroundSpeedAutomatic() {
        return maxGroundSpeedAutomatic;
    }

    public boolean isStoppingAtWaypoints() {
        return stoppingAtWaypoints;
    }

    public boolean setStoppingAtWaypoints(boolean stoppingAtWaypoints) {
        if (this.stoppingAtWaypoints == stoppingAtWaypoints) {
            return false;
        }

        this.stoppingAtWaypoints = stoppingAtWaypoints;
        informChangeListener();
        return true;
    }

    public boolean setMaxGroundSpeedAutomatic(FlightplanSpeedModes maxGroundSpeedAutomatic) {
        if (this.maxGroundSpeedAutomatic == maxGroundSpeedAutomatic) {
            return false;
        }

        this.maxGroundSpeedAutomatic = maxGroundSpeedAutomatic;
        CFlightplan fp = getFlightplan();
        if (fp != null && maxGroundSpeedAutomatic.isAutomaticallyAdjusting()) {
            setStoppingAtWaypoints(false); // in automatic mode this should be false to not confuse users

            if (setMaxGroundSpeedMPSec(
                    fp.getHardwareConfiguration()
                        .getPlatformDescription()
                        .getMaxPlaneSpeed()
                        .convertTo(Unit.METER_PER_SECOND)
                        .getValue()
                        .doubleValue())) {
                // dont fire change listener a second time -> speedup
                return true;
            }
        }

        informChangeListener();
        return true;
    }

    public void silentResetMaxGroundSpeed() {
        if (!maxGroundSpeedAutomatic.isAutomaticallyAdjusting()) {
            return;
        }

        CFlightplan fp = getFlightplan();
        if (fp != null) {
            maxGroundSpeedMPSec =
                fp.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMaxPlaneSpeed()
                    .convertTo(Unit.METER_PER_SECOND)
                    .getValue()
                    .doubleValue();
        }
    }

    @Deprecated // please use configuration instead... this is only here for sirius downwards compatibility
    public double getMaxRoll() {
        return maxRoll;
    }

    @Deprecated // please use configuration instead... this is only here for sirius downwards compatibility
    public double getMaxNick() {
        return maxNick;
    }

    public static final double MAX_MAXROLL = 180;
    public static final double MAX_MAXNICK = 180;

    protected double mintimeinterval = DEFAULT_MIN_TIME_INTERVALL;
    public static final double DEFAULT_MIN_TIME_INTERVALL = 2.0;

    @Deprecated // please use Lens configuration instead... this is only here for sirius downwards compatibility
    public double getMinTimeInterval() {
        return mintimeinterval;
    }

    public static final double MAX_INTERVALL = 60;

    public boolean setMinTimeInterval(double interval) {
        if (interval > MAX_INTERVALL) {
            interval = MAX_INTERVALL;
        }
        // normalize -0.0==+0.0 for primitives but NOT for Double or Float objects...
        if (interval <= 0) {
            interval = 0;
        }

        if (this.mintimeinterval != interval) {
            this.mintimeinterval = interval;
            informChangeListener();
            return true;
        }

        return false;
    }

    public boolean setMaxRoll(double maxRoll) {
        if (maxRoll > MAX_MAXROLL) {
            maxRoll = MAX_MAXROLL;
        }
        // normalize -0.0==+0.0 for primitives but NOT for Double or Float objects...
        if (maxRoll <= 0) {
            maxRoll = 0;
        }

        if (this.maxRoll != maxRoll) {
            this.maxRoll = maxRoll;
            informChangeListener();
            return true;
        }

        return false;
    }

    public double getGsdTolerance() {
        return gsdTolerance;
    }

    public boolean setGsdTolerance(double gsdTolerance) {
        gsdTolerance = MathHelper.intoRange(gsdTolerance, 0, 1);
        if (this.gsdTolerance == gsdTolerance) return false;
        this.gsdTolerance = gsdTolerance;
        informChangeListener();
        return true;
    }

    public boolean setMaxNick(double maxNick) {
        if (maxNick > MAX_MAXNICK) {
            maxNick = MAX_MAXNICK;
        }
        // normalize -0.0==+0.0 for primitives but NOT for Double or Float objects...
        if (maxNick <= 0) {
            maxNick = 0;
        }

        if (this.maxNick != maxNick) {
            this.maxNick = maxNick;
            informChangeListener();
            return true;
        }

        return false;
    }

    public static final double MAX_SPEED = 100; // in km/h

    public static final AltitudeAdjustModes DEFAULT_ALTMODE = AltitudeAdjustModes.CONSTANT_OVER_R;

    protected AltitudeAdjustModes altAdjustMode = DEFAULT_ALTMODE;

    public AltitudeAdjustModes getAltitudeAdjustMode() {
        return altAdjustMode;
    }

    public boolean setAltitudeAdjustMode(AltitudeAdjustModes altAdjustMode) {
        if (this.altAdjustMode == altAdjustMode) {
            return false;
        }

        this.altAdjustMode = altAdjustMode;
        informChangeListener();
        return true;
    }

    boolean isMultiFP = false;

    public boolean isMultiFP() {
        return isMultiFP;
    }

    public void setMultiFP(boolean isMultiFP) {
        if (this.isMultiFP != isMultiFP) {
            this.isMultiFP = isMultiFP;
            informChangeListener();
        }
    }

    protected CPhotoSettings(double maxRoll, double maxNick, double interval, IFlightplanContainer parent) {
        super(parent);
        mute = true;
        setMaxRoll(maxRoll);
        setMaxNick(maxNick);
        setMinTimeInterval(interval);

        mute = false;
    }

    private INotificationObject.ChangeListener hwConfigurationListener =
        new INotificationObject.ChangeListener() {
            @Override
            public void propertyChange(INotificationObject.ChangeEvent configurationChangeEvent) {
                if (!maxGroundSpeedAutomatic.isAutomaticallyAdjusting()) {
                    return;
                }

                CFlightplan fp = getFlightplan();
                if (fp != null) {
                    if (fp.getPhotoSettings() != null) {
                        setMaxGroundSpeedMPSec(fp.getPhotoSettings().getMaxGroundSpeedMPSec());
                    } else {
                        setMaxGroundSpeedMPSec(
                            fp.getHardwareConfiguration()
                                .getPlatformDescription()
                                .getMaxPlaneSpeed()
                                .convertTo(Unit.METER_PER_SECOND)
                                .getValue()
                                .doubleValue());
                    }
                }
            }
        };

    protected CPhotoSettings(IFlightplanContainer parent) {
        super(parent);
        mute = true;
        resetToDefaults();
        mute = false;
        CFlightplan fp = getFlightplan();
        if (fp != null) {
            fp.getHardwareConfiguration()
                .addListener(new INotificationObject.WeakChangeListener(hwConfigurationListener));
        }

        hwConfigurationListener.propertyChange(null);
    }

    @Override
    public void setParent(IFlightplanContainer container) {
        CFlightplan fp = getFlightplan();
        if (fp != null) {
            fp.getHardwareConfiguration().removeListener(hwConfigurationListener);
        }

        super.setParent(container);
        fp = getFlightplan();
        if (fp != null) {
            fp.getHardwareConfiguration()
                .addListener(new INotificationObject.WeakChangeListener(hwConfigurationListener));
        }

        // dont trigger HW change jet, otherwise closing and loading of FPs causes issues
        // hwConfigurationListener.propertyChange(null);
    }

    public String toString() {
        return "PhotoSettings"; // TODO better name
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof CPhotoSettings) {
            CPhotoSettings ps = (CPhotoSettings)o;
            return maxNick == ps.maxNick
                && maxRoll == ps.maxRoll
                && mintimeinterval == ps.mintimeinterval
                && altAdjustMode == ps.altAdjustMode
                && maxGroundSpeedMPSec == ps.maxGroundSpeedMPSec
                && maxGroundSpeedAutomatic == ps.maxGroundSpeedAutomatic
                && stoppingAtWaypoints == ps.stoppingAtWaypoints
                && gsdTolerance == ps.gsdTolerance;
        }

        return false;
    }

    @Override
    public CPhotoSettings clone() {
        CPhotoSettings ps = FlightplanFactory.getFactory().newCPhotoSettings(maxRoll, maxNick, mintimeinterval, cont);
        ps.altAdjustMode = altAdjustMode;
        ps.maxGroundSpeedMPSec = maxGroundSpeedMPSec;
        ps.maxGroundSpeedAutomatic = maxGroundSpeedAutomatic;
        ps.stoppingAtWaypoints = stoppingAtWaypoints;
        ps.gsdTolerance = gsdTolerance;
        return ps;
    }

    public void setFromOther(CPhotoSettings ps) {
        boolean changed = false;
        setMute(true);
        changed |= setMaxNick(ps.getMaxNick());
        changed |= setMaxRoll(ps.getMaxRoll());
        changed |= setMinTimeInterval(ps.getMinTimeInterval());
        changed |= setAltitudeAdjustMode(ps.getAltitudeAdjustMode());
        changed |= setMaxGroundSpeedMPSec(ps.getMaxGroundSpeedMPSec());
        changed |= setMaxGroundSpeedAutomatic(ps.getMaxGroundSpeedAutomatic());
        changed |= setStoppingAtWaypoints(ps.isStoppingAtWaypoints());
        setSilentUnmute();
        if (changed) {
            informChangeListener();
        }
    }

    @Override
    public void informChangeListener() {
        if (mute) {
            return;
        }

        super.informChangeListener();
    }

    boolean mute = false;

    public void setMute(boolean mute) {
        if (this.mute == mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            informChangeListener();
        }
    }

    public void setSilentUnmute() {
        this.mute = false;
    }

    public boolean isMute() {
        return mute;
    }

    public void resetToDefaults() {
        boolean changed = false;
        setMute(true);
        changed |= setMaxNick(CPhotoSettings.DEFAULT_MAX_NICK);
        changed |= setMaxRoll(CPhotoSettings.DEFAULT_MAX_ROLL);
        changed |= setMinTimeInterval(CPhotoSettings.DEFAULT_MIN_TIME_INTERVALL);
        changed |= setAltitudeAdjustMode(DEFAULT_ALTMODE);
        changed |= setMaxGroundSpeedAutomatic(DEFAULT_SPEEDMODE);
        changed |= setStoppingAtWaypoints(false);
        CFlightplan fp = getFlightplan();
        if (fp != null) {
            changed |=
                setMaxGroundSpeedMPSec(
                    fp.getHardwareConfiguration()
                        .getPlatformDescription()
                        .getMaxPlaneSpeed()
                        .convertTo(Unit.METER_PER_SECOND)
                        .getValue()
                        .doubleValue());
        }

        setSilentUnmute();
        if (changed) {
            informChangeListener();
        }
    }

}
