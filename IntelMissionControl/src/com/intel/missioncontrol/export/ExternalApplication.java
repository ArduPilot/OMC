/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.export;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public enum ExternalApplication {
    AGI_SOFT_PHOTO_SCAN,
    PIX_4_DESKTOP,
    CONTEXT_CAPTURE;

    public Path[] getDefaultExecutablePaths() {
        switch (this) {
        case AGI_SOFT_PHOTO_SCAN:
            return getLocations(
                "Agisoft/Photogrammetric Kit Pro/apk.exe",
                "Agisoft/PhotoScan Pro/photoscan.exe",
                "Agisoft/Metashape Pro/metashape.exe");
        case PIX_4_DESKTOP:
            return getLocations("Pix4Dmapper/Pix4Dmapper.exe", "Pix4UAV/pix4uav.exe");
        case CONTEXT_CAPTURE:
            return getLocations("Bentley/Topcon ContextCapture Advanced/bin/CCMaster.exe");
        default:
            throw new IllegalArgumentException();
        }
    }

    private Path[] getLocations(String... paths) {
        var list = new ArrayList<Path>();
        for (var path : paths) {
            list.add(Paths.get(System.getenv("ProgramFiles")).resolve(Paths.get(path)));
            list.add(Paths.get(System.getenv("ProgramFiles(X86)")).resolve(Paths.get(path)));
        }

        return list.toArray(new Path[0]);
    }

}
