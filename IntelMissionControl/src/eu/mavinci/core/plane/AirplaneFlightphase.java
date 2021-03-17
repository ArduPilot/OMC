/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.util.Vector;

public enum AirplaneFlightphase {
    ground(0, "eu.mavinci.core.plane.AirplaneFlightphase.ground"), // =0
    takeoff(1, "eu.mavinci.core.plane.AirplaneFlightphase.takeoff"), // =1
    airborne(2, "eu.mavinci.core.plane.AirplaneFlightphase.airborne"), // =2
    descending(3, "eu.mavinci.core.plane.AirplaneFlightphase.descending"), // =3
    landing(4, "eu.mavinci.core.plane.AirplaneFlightphase.landing"), // =4
    FixedOrientation(5, "eu.mavinci.core.plane.AirplaneFlightphase.FixedOrientation"), // =5
    groundtest(6, "eu.mavinci.core.plane.AirplaneFlightphase.groundtest"), // =6
    returnhome(7, "eu.mavinci.core.plane.AirplaneFlightphase.returnhome"), // =7
    gpsloss(8, "eu.mavinci.core.plane.AirplaneFlightphase.gpsloss"), // =8
    waitingforgps(9, "eu.mavinci.core.plane.AirplaneFlightphase.waitingforgps"), // =9
    jumpToLanding(
        10, "eu.mavinci.core.plane.AirplaneFlightphase.jumpToLanding"), // 10 -> only avaliable for AP Version >= 5.0
    areaRestricted(
        11,
        "eu.mavinci.core.plane.AirplaneFlightphase.areaRestricted"), // 11 -> only avaliable for AP Version >= 5.0 and
    // will only be send in case FP is in geofencing
    // inside embargo country and has GPS. sinding this to AP is blocked by AP
    startFlight(12, "eu.mavinci.core.plane.AirplaneFlightphase.startFlight"),
    holdPosition(13, "eu.mavinci.core.plane.AirplaneFlightphase.holdPosition");

    private int value;
    private String displayNameKey;

    AirplaneFlightphase(int value, String displayNameKey) {
        this.value = value;
        this.displayNameKey = displayNameKey;
    }

    public String getDisplayNameKey() {
        return displayNameKey;
    }

    /**
     * Determinate the state of a flightphase if it is in the air or on the ground some phases like takeoff or landing
     * will result shortly after their setting in such a state, and this expected state is used!
     *
     * @param phase
     * @return 0 => Air, 1 => ground, 2 => unknown
     */
    public int isFlightphaseOnGround() {
        switch (this) {
        case airborne:
        case descending:
        case returnhome:
        case jumpToLanding:
        case holdPosition:
            return 0;

        case ground:
        case groundtest:
        case waitingforgps:
        case landing:
        case areaRestricted:
            return 1;

        case gpsloss:
        case FixedOrientation:
        case takeoff:
        default:
            return 2;
        }
    }

    public boolean isTakeoffLike() {
        switch (this) {
        case descending:
        case returnhome:
        case ground:
        case groundtest:
        case waitingforgps:
        case landing:
        case areaRestricted:
            return false;

        case airborne:
        case gpsloss:
        case FixedOrientation:
        case takeoff:
        case jumpToLanding:
        default:
            return true;
        }
    }

    /** @return true if this Flightphase is on ground or it is in a kind of landing mode */
    public boolean isGroundTarget() {
        return isFlightphaseOnGround() == 1; // landing is from now on included by default! || isLanding());
    }

    public boolean isLanding() {
        return (this == AirplaneFlightphase.descending || this == AirplaneFlightphase.landing);
    }

    public boolean isVisible() {
        if (DependencyInjector.getInstance()
                    .getInstanceOf(ISettingsManager.class)
                    .getSection(GeneralSettings.class)
                    .getOperationLevel()
                == OperationLevel.DEBUG) {
            return true;
        }

        return this != FixedOrientation && this != groundtest;
    }

    public static Vector<AirplaneFlightphase> valuesVisible() {
        Vector<AirplaneFlightphase> tmp = new Vector<AirplaneFlightphase>();
        for (AirplaneFlightphase phase : values()) {
            if (phase.isVisible()) {
                tmp.add(phase);
            }
        }

        return tmp;
    }

    public static String getEnumByValue(Integer code) {
        for (AirplaneFlightphase e : AirplaneFlightphase.values()) {
            if (code == e.value) {
                return e.name();
            }
        }

        return null;
    }
}
