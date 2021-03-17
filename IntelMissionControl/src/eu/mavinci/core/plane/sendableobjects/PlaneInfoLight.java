/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.update.UpdateURL;
import eu.mavinci.core.update.UpdateURL;

public class PlaneInfoLight extends MObject {

    private static final long serialVersionUID = -4306911600549313256L;

    /** RevisionNumber of the Autopilot Software */
    public int revisionSoftware = 0;

    /** RevisionNumber of the Autopilot Hardware */
    public int revisionHardware = 0;

    /** Major release of the autopilot */
    public String releaseVersion = "";

    /** Kind of the Airplane */
    public String hardwareType = "";

    /** Serialnumber of the autopilot */
    public String serialNumber = "";

    /** implement vloslimit setting. <=0 means unrestricted, >0 means restriction in meters. */
    public int vloslimit = -1;

    public String getHumanReadableSWversion() {
        return UpdateURL.getHumanReadableVersion(releaseVersion, revisionSoftware);
    }
    //
    // protected boolean isCompatible(){
    // return true;
    //// return GlobalSettings.isCompatible(releaseVersion);
    // }
}
