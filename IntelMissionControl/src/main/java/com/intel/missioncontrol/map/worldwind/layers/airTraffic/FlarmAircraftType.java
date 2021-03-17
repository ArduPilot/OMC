/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.airTraffic;

import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public enum FlarmAircraftType {
    unknown,
    glider_motorGlider,
    tow_tugPlane,
    helicopter_rotorcraft,
    parachute,
    dropPlaneForParachute,
    hangGliderHard,
    paraGliderSoft,
    poweredAircraft,
    jetAircraft,
    flyingSaucerUFO,
    balloon,
    airship,
    uav,
    undefined,
    staticObject,
    skydiverDropZone,
    aerodromeTrafficZone,
    militaryFiringArea,
    kiteFlyingZone,
    winchLaunchingArea,
    RCflyingRrea,
    UASflyingArea,
    aerobaticBox,
    genericDangerArea,
    genericProhibitedArea,
    otherZone,
    ;

    public static FlarmAircraftType fromCathegory(String cat) {
        switch (cat) {
            // Set A = 4
        case "20":
            return unknown;
        case "21":
            return poweredAircraft;
        case "22":
        case "23":
        case "24":
        case "25":
        case "26":
            return jetAircraft;
        case "27":
            return helicopter_rotorcraft;

            // Set B=3
        case "18":
            return unknown;
        case "19":
            return glider_motorGlider;
        case "1A":
            return airship;
        case "1B":
            return parachute;
        case "1C":
            return paraGliderSoft;
        case "1D":
            return undefined;
        case "1E":
            return uav;
        case "1F":
            return jetAircraft; // resp. ROCKET / Space shuttle, but thats not defined here;-)

            // Set C=2
        case "10":
            return unknown;
        case "11":
            return staticObject; // resp. ground vehicle
        case "12":
            return staticObject; // resp. ground vehicle
        case "13":
            return staticObject;
        case "14":
            return staticObject;
        case "15":
            return staticObject;
        case "16":
            return undefined;
        case "17":
            return undefined;

            // Set D=1
        case "08":
        case "09":
        case "0A":
        case "0B":
        case "0C":
        case "0D":
        case "0E":
        case "0F":
            return undefined;
        case "41":
            return skydiverDropZone;
        case "43":
            return aerodromeTrafficZone;
        case "44":
            return militaryFiringArea;
        case "45":
            return kiteFlyingZone;
        case "46":
            return winchLaunchingArea;
        case "47":
            return RCflyingRrea;
        case "48":
            return UASflyingArea;
        case "49":
            return aerobaticBox;
        case "7E":
            return genericDangerArea;
        case "7F":
            return genericProhibitedArea;
        default:
            Debug.getLog().info("unknown Flarm Aircraft Category: " + cat);
            return otherZone;
        }
    }

    public String getABSbCategory() {
        switch (this) {
        default:
        case unknown:
        case flyingSaucerUFO:
        case undefined:
            return "18";
        case glider_motorGlider:
            return "19";
        case tow_tugPlane:
        case dropPlaneForParachute:
        case poweredAircraft:
            return "21";
        case helicopter_rotorcraft:
            return "27";
        case parachute:
            return "1B";
        case hangGliderHard:
        case paraGliderSoft:
            return "1C";
        case jetAircraft:
            return "26";
        case balloon:
        case airship:
            return "1A";
        case uav:
            return "1E";
        case staticObject:
            return "13";
        }
    }

    public String getResourcePath() {
        return "com/intel/missioncontrol/gfx/icon_adsb-object.svg";
    }

    BufferedImage imgFull = null;

    public BufferedImage getBufferedImageFull() {
        if (imgFull == null) {
            imgFull = Application.getBufferedImageFromResource(getResourcePath(), -2);
            //			System.out.println("path="+getResourcePath() + " img:"+imgFull + " w:"+imgFull.getWidth());
        }

        return imgFull;
    }

    ImageIcon icon = null;

}
