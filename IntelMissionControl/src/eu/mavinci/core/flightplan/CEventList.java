/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.plane.AirplaneEventActions;
import eu.mavinci.desktop.main.debug.Debug;
import java.util.logging.Level;

public abstract class CEventList extends AFlightplanContainer implements IMuteable, IRecalculateable {

    protected CFlightplan fp;

    protected CEventList(CFlightplan fp) {
        this.fp = fp;
        mute = true;
        resetToDefaults();
        mute = false;
    }

    @Override
    public CFlightplan getParent() {
        return fp;
    }

    @Override
    public void setParent(IFlightplanContainer container) {
        fp = (CFlightplan)container;
    }

    @Override
    public String toString() {
        return "EventList"; // TODO better name
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        if (o instanceof CEventList) {
            CEventList otherEvents = (CEventList)o;
            return safetyAltitude_CM == otherEvents.safetyAltitude_CM;
        }

        return false;
    }

    @Override
    public boolean isAddableToFlightplanContainer(Class<? extends IFlightplanStatement> cls) {
        if (sizeOfFlightplanContainer() >= getMaxSize()) {
            return false;
        }

        return CEvent.class.isAssignableFrom(cls);
    }

    @Override
    public int getMaxSize() {
        return 10;
    }

    @Override
    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (fp != null) {
            fp.flightplanStatementAdded(statement);
        }
    }

    @Override
    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (fp != null) {
            fp.flightplanStatementChanged(statement);
        }
    }

    @Override
    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (fp != null) {
            fp.flightplanStatementRemove(i, statement);
        }
    }

    @Override
    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (fp != null) {
            fp.flightplanStatementStructureChanged(statement);
        }
    }

    @Override
    public CFlightplan getFlightplan() {
        return fp;
    }

    public CEvent getEventByName(String name) {
        for (IFlightplanStatement stmt : elements) {
            if (stmt instanceof CEvent) {
                CEvent event = (CEvent)stmt;
                if (event.getName().equals(name)) {
                    return event;
                }
            }
        }

        return null;
    }

    @Override
    public void reassignIDs() {}

    @Override
    public CEvent getFromFlightplanContainer(int i) {
        return (CEvent)super.getFromFlightplanContainer(i);
    }

    protected boolean mute;

    @Override
    public void setMute(boolean mute) {
        if (mute == this.mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            flightplanStatementStructureChanged(this);
            flightplanStatementChanged(this);
        }
    }

    @Override
    public void setSilentUnmute() {
        this.mute = false;
    }

    @Override
    public boolean isMute() {
        return mute;
    }

    public static final int SAFETY_ALT_CM_DEFAULT = CWaypoint.DEFAULT_ALT_WITHIN_CM;

    protected boolean autoComputeSafetyHeight = true;

    protected int safetyAltitude_CM = SAFETY_ALT_CM_DEFAULT;

    public int getAltWithinCM() {
        return safetyAltitude_CM;
    }

    public double getAltWithinM() {
        return (safetyAltitude_CM) / 100.d;
    }

    public void setAltWithinM(double alt) {
        setAltWithinCM((int)Math.round(alt * 100));
    }

    public void setAltWithinM(float alt) {
        setAltWithinCM(Math.round(alt * 100));
    }

    public boolean isAutoComputingSafetyHeight() {
        return autoComputeSafetyHeight;
    }

    public boolean setAutoComputeSafetyHeight(boolean autoComputeSafetyHeight) {
        if (this.autoComputeSafetyHeight == autoComputeSafetyHeight) {
            return false;
        }

        this.autoComputeSafetyHeight = autoComputeSafetyHeight;
        flightplanStatementChanged(this);
        return true;
    }

    public static final int minSafetyAlt_CM = 5000;
    public static final double minSafetyAlt_M = minSafetyAlt_CM / 100.;

    public void setAltWithinCM(int alt) {
        if (alt > CWaypoint.ALTITUDE_MAX_WITHIN_CM) {
            alt = CWaypoint.ALTITUDE_MAX_WITHIN_CM;
        }

        if (alt < minSafetyAlt_CM) {
            alt = minSafetyAlt_CM;
        }

        if (this.safetyAltitude_CM != alt) {
            this.safetyAltitude_CM = alt;
            flightplanStatementChanged(this);
        }
    }

    public static final String NAME_GPSLOSS = "gpsloss";
    public static final String NAME_RCDATALOSS = "rcdataloss";
    public static final String NAME_RCLOSS = "rcloss";
    public static final String NAME_DATALOSS = "dataloss";
    public static final String NAME_BATLOW = "batlow";

    public static final String[] eventsKeys = {NAME_GPSLOSS, NAME_RCDATALOSS, NAME_RCLOSS, NAME_DATALOSS, NAME_BATLOW};

    public void resetToDefaults() {
        setAltWithinCM(SAFETY_ALT_CM_DEFAULT);
        while (sizeOfFlightplanContainer() > 0) {
            removeFromFlightplanContainer(0);
        }

        try {
            for (String key : eventsKeys) {
                CEvent event = FlightplanFactory.getFactory().newCEvent(this, key);
                if (key == NAME_BATLOW) {
                    event.hasLevel = true;
                }

                addToFlightplanContainer(event);
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not setup error events", e);
        }
    }

    public void overwriteFromOther(CEventList other) {
        setMute(true);
        safetyAltitude_CM = other.safetyAltitude_CM;
        for (int i = 0; i != eventsKeys.length; i++) {
            CEvent event = (CEvent)elements.get(i);
            CEvent eventOther = (CEvent)other.elements.get(i);
            event.overwriteFromOther(eventOther);
        }

        setMute(false);
    }

    public void fixEvents() {
        for (IFlightplanStatement tmp : elements) {
            CEvent event = (CEvent)tmp;
            event.fixIt();
        }
    }

    @Override
    public boolean doSubRecalculationStage1() {
        if (getFlightplan().getLandingpoint().getMode().isAutoLanding()) {
            return true;
        }

        for (IFlightplanStatement tmp : elements) {
            CEvent event = (CEvent)tmp;
            if (event.action == AirplaneEventActions.jumpLanging) {
                event.action = AirplaneEventActions.returnToStart;
                event.informChangeListener();
            }
        }

        return true;
    }

    @Override
    public boolean doSubRecalculationStage2() {
        return true;
    }
}
