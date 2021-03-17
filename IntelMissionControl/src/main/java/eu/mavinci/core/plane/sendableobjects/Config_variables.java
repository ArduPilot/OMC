/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.plane.PlaneConstants;

public class Config_variables extends MObject {

    /** */
    private static final long serialVersionUID = -7670379363690426785L;

    public Config_variables() {
        this(PlaneConstants.DEFAULT_SERVO_COUNT);
    }

    public Config_variables(int servoCount) {
        servomin = new MVector<Integer>(servoCount, Integer.class);
        servotrim = new MVector<Integer>(servoCount, Integer.class);
        servomax = new MVector<Integer>(servoCount, Integer.class);
        for (int i = 0; i != servoCount; i++) {
            // init vectors!
            servomin.add(0);
            servotrim.add(0);
            servomax.add(0);
        }

        DBG = new MVector<Integer>(PlaneConstants.DEBUG_COUNT, Integer.class);
        for (int i = 0; i != PlaneConstants.DEBUG_COUNT; i++) {
            // init vectors!
            DBG.add(0);
        }
    }

    /**
     * minimal, maximal an center value for trimming the servos. Value Range is 0..255. The number of Servos has to be 5
     */
    public MVector<Integer> servomin;

    public MVector<Integer> servotrim;
    public MVector<Integer> servomax;

    public int CONT_ALT_PCONST = 0; // 10 meter = 100% winkel
    public int CONT_ALT_DESCMAX = 0; // maximale Sinkrate (grad)
    public int CONT_ALT_CLIMBMAX = 0; // maximale Steigrate (grad)
    public int CONT_ALT_ALTHOLD = 0; // nick angle for altitude hold
    public int CONT_ALT_DESCENDING = 0; // nick angle for descending phase
    public int CONT_ALT_LANDING = 0; // nick angle for landing phase
    public int CONT_ALT_VSPEED = 0; // max sink/rising speed of D part
    public int CONT_ALT_D = 0; // D part of altitude pitch angle controller
    public int CONT_ALT_LANDINGALTITUDE =
        PlaneConstants.DEF_CONT_ALT_LANDINGALTITUDE; // altitude where to switch to LANDING

    public int CONT_NAV_PCONST = 0; // XX grad = 100% servo
    public int CONT_NAV_DISTCONST = 0; // less 80 meters distance: give more rudder
    public int CONT_NAV_DESTINATIONREACHED = 0; // if closer than 15 meters, count as reached
    public int CONT_NAV_AILERONOFFSET = 0; // Aileron/Rudder mixing: Dont give any aileron if rudder<CONST (in %)
    public int CONT_NAV_AILERONCONST =
        0; // Aileron/Rudder mixing: Aileron=CONST*(Rudder-OFFSET) (but min/max travel from servo setup
    // applies!)
    public int CONT_NAV_DPS = 0; // Max. degrees per second yaw rate
    public int CONT_NAV_DCONST = 0; // D-Const for navigation
    public int CONT_NAV_ELEVATORCONST = 0; // Elevator mixing
    public int CONT_NAV_MAXCTERROR = 0; // Maximal Cross Track error (if larger, fly 90 deg to track!) [cm]
    public int CONT_NAV_CIRCR = PlaneConstants.DEF_CONT_NAV_CIRCR; // Circle radius [cm]
    public int CONT_NAV_CIRCD = 0; // Maximal Cross Track error from circle (if larger, fly 90 deg to track!) [cm]
    public int CONT_NAV_ICONST = 0;
    public int CONT_NAV_FORWARDEST = 10;
    public int CONT_NAV_MAXROLLASSISTED; // Max roll in assisted mode landing/desc/takeoff
    public int
        CONT_NAV_IIGNORE; // distance in cm after last waypoint/before next wp which is not used in i part of nav
                          // controller (about
    // 50 meters =

    public int CONT_STAB_AICONST = 0; // P-Const for aileron during stabilization
    public int CONT_STAB_ADPS = 0; // Max. degrees per second roll rate
    public int CONT_STAB_AIDCONST = 0; // D-Const aileron
    public int CONT_STAB_ELECONST = 0; // P-Const for aileron during stabilization
    public int CONT_STAB_EDPS = 0; // Max. degrees per second nick rate
    public int CONT_STAB_ELDCONST = 0; // D-Const elevator
    public int CONT_STAB_RUDDERAIMIX = 100; // RUDDER / AI MIX in percent
    public int CONT_STAB_RUDDERSLIP = 100;
    public int CONT_STAB_MAXFLAPS; // maximum flaps setting
    public int CONT_STAB_LANDINGFLAPS;
    public int CONT_STAB_TAKEOFFFLAPS;
    public int CONT_STAB_DESCFLAPS;
    public int CONT_STAB_SPOILSINKPITCH = 0; // pitch angle when sinking with spoilerons

    public int PHOTO_LASTIMAGEONLINEDISTANCE = 1000;
    public int PHOTO_FIRSTIMAGEONLINEALLOWEDROLL = 50; // wird noch durch 10 geteilt!

    public int MOTOR_TAKEOFF = 0;
    public int MOTOR_AIRBORNE = 0;
    public int MOTOR_DESCENDING = 0;
    public int MOTOR_LANDING = 0;
    public int MOTOR_DYNAMIC = 0;

    public int MOTOR_AIRBORNECLIMB = 0;
    public int MOTOR_AIRBORNESINK = 0;
    public int MOTOR_PCONST = 0;
    public int MOTOR_ICONST = 0;
    public int MOTOR_DCONST = 0;
    public int MOTOR_TAKEOFFRPM = 0;
    public int MOTOR_AIRBORNERPM = 0;
    public int MOTOR_AIRBORNECLIMBRPM = 0;
    public int MOTOR_AIRBORNESINKRPM = 0;
    public int MOTOR_DESCENDINGRPM = 0;
    public int MOTOR_LANDINGRPM = 0;
    public int MOTOR_MINPWMMOTORON = 0;
    public int MOTOR_RPMINCREMENT = 0;
    public int MOTOR_RPMDECREMENT = 0;

    public int L3D_PITCHHI;
    public int L3D_ALTDIFF1;
    public int L3D_ALTDIFF2;
    public int L3D_ALTDIFF3;
    public int L3D_ALTSWLANDING;
    public int L3D_ALTOFFSET;
    public int L3D_ALTMOTORSTART;
    public int L3D_SPOILLANDING;
    public int L3D_IPART;
    public int L3D_MAXCTE;
    public int L3D_WRLOG;
    public int L3D_DPARTLO;
    public int L3D_DESTREACHED;
    public int L3D_DPARTHI;
    public int L3D_INRANGE;

    public int MISC_LANDINGALPHA; // alpha angle for advanced landing mode (elwira)
    public int MISC_ROLLOFFSET = 0;
    public int MISC_NICKOFFSET = 0;
    public int MISC_IMUACCLTRUST = 250;
    public int MISC_ACCLLOWPASS;
    public int MISC_ENABLEWLAN = 0;
    public int MISC_ENABLEBLUETOOTH = 0;
    public int MISC_MANUALSWITCHTOLANDING; // 0: automatic switch (ultrasonic etc.), 1: manual switch
    public int MISC_COMPASSINTERVAL; // (1-N/100) ... (1+N/100) is the interval for compass trust (maybe 50 or so?)
    public int
        MISC_COMPASSSPEED; // 0.05*(cfg.DBG[12]/1000.)*ranglediff(bearing,imud.euler[2]) is compass convergence speed
                           // (set to 1000)
    public int MISC_GPSCORRECTIONENABLE = 0;
    public int MISC_GPSTRUST; // GPS trust scale (set to 1000)
    public int MISC_GPSCORRECTION; // GPS correction if compass fails: 0=use angle directly, 1=low pass filter
    public int MISC_GPSCUTOFF; // GPS correction roll angle cut off
    public int MISC_FAKEGPS; // 1 = fake GPS for demo
    public int
        MISC_GPSTYPE; // 0: default (old GPS config ublox), 1: 115.2kpbs 20hz nmea skytraq, 2: topcon DGPS, 3: new ublox
                      // (9600bps
    // init), 4: new ublox 9600bps init and $MVCAM feedback
    public int MISC_TOPCONMODE = PlaneConstants.DEF_MISC_TOPCONMODE; // 0: TOPCON DGPS in SA/WAAS mode, 1: RTK mode
    public int MISC_UPDATEBBOX;

    @Deprecated
    public int MISC_BBOXSIZE = -1; // bounding box radius in cm

    public int MISC_MODEM0; // XBEE-Type
    public int MISC_MODEM1; // XBEE-Type
    public int MISC_XBEEAPIMODE;
    public int MISC_MAXFAILSEC; // max time in sec. for simulated failures
    public int MISC_ROLLLAZYINTTRIGGER;

    public String name = ""; // Airplane name

    public MVector<Integer> DBG;

    public int USER_NEWASSISTEDLANDING = 0; // 0==off 1==on
    public int USER_BBOXSIZE = -1; // bounding box radius in cm

    public double getCircularRadiusInM() {
        return CONT_NAV_CIRCR / 100.0;
    }

    public double detEndCircleAltInM(Config_variables vars) {
        return CONT_ALT_LANDINGALTITUDE / 100.0;
    }
}
