/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.core.update.UpdateURL;
import eu.mavinci.core.update.UpdateURL;

public class BackendInfo extends MObject {

    /** */
    private static final long serialVersionUID = -8423368871029434527L;

    /** RevisionNumber of the Autopilot Software */
    public volatile int revisionSoftware = 0;

    /** RevisionNumber of the Autopilot Hardware */
    public volatile int revisionHardware = 0;

    /** Major release of the Backend */
    public volatile String releaseVersion = "";

    /** Kind of the Airplane */
    public volatile String hardwareType = "";

    /** Serialnumber of the autopilot */
    public volatile String serialNumber = "";

    /** Version of the protocoll to the Backend */
    public volatile int protocolVersion = 0;

    public String getHumanReadableSWversion() {
        return UpdateURL.getHumanReadableVersion(releaseVersion, revisionSoftware);
    }

    public boolean isCompatible() {
        return protocolVersion == 2 || protocolVersion == 3;
        // return GlobalSettings.isCompatible(releaseVersion);
    }

    public boolean isUpdateable() {
        return !(serialNumber.toUpperCase().contains(PlaneConstants.MAC_SIM_SERVER));
    }
}
