/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.CPhotoLogLine;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.desktop.gui.doublepanel.camerasettings.CameraHelper;

public class PhotoLogLine extends CPhotoLogLine {

    IHardwareConfiguration hardwareConfiguration;

    public PhotoLogLine(PhotoData pd, IHardwareConfiguration hardwareConfiguration) {
        super(pd);
        this.hardwareConfiguration = hardwareConfiguration;
    }

    public PhotoLogLine(IHardwareConfiguration hardwareConfiguration) {
        super();
        this.hardwareConfiguration = hardwareConfiguration;
    }

    public PhotoLogLine(double timestamp, IHardwareConfiguration hardwareConfiguration) {
        super(timestamp);
        this.hardwareConfiguration = hardwareConfiguration;
    }

    @Override
    public double getTimestamp() {
        return super.getTimestamp()
            + (hardwareConfiguration != null
                ? (CameraHelper.computePlgDataDelay(this, hardwareConfiguration) / 1000.)
                : 0);
    }

}
