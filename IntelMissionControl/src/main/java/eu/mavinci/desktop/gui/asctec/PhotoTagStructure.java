/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.asctec;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import eu.mavinci.core.obfuscation.IKeepAll;

import java.util.Arrays;
import java.util.List;

public class PhotoTagStructure extends Structure implements IKeepAll {

    public PhotoTagStructure() {
        // TODO Auto-generated constructor stub
    }

    // position
    // gps WGS84 (lat, long, height), calculated from E_x
    public double lat;
    public double lon;
    public double height;

    // position relative
    public double relX;
    public double relY;
    public double relH;

    // camera orientation roll-pitch-yaw
    public double roll_cam;
    public double pitch_cam;
    public double yaw_cam;

    // rpy of the plane
    @SuppressWarnings("checkstyle:membername")
    public double roll_plane;
    public double pitch_plane;
    public double yaw_plane;


    // timestamp in GPS milliseconds
    public double timestamp;

    // number of waypoint
    public double num;

    // lat0 lon0 height0 - coordinates of the takeoff (origin)
    public double lat0;

    public double lon0;

    public double height0;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
            new String[] {
                "lat",
                "lon",
                "height",
                "relX",
                "relY",
                "relH",
                "roll_cam",
                "pitch_cam",
                "yaw_cam",
                "roll_plane",
                "pitch_plane",
                "yaw_plane",
                "timestamp",
                "num",
                "lat0",
                "lon0",
                "height0"
            });
    }

    public static class ByReference extends PhotoTagStructure implements Structure.ByReference {}

    public PhotoTagStructure(Pointer p) {
        super(p);
    }
}
