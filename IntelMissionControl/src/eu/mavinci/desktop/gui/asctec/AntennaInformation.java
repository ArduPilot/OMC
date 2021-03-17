/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.asctec;

import com.sun.jna.Structure;
import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.core.obfuscation.IKeepAll;

import java.util.Arrays;
import java.util.List;

public class AntennaInformation extends Structure implements IKeepAll {

    public double lat;
    public double lon;
    public double height;
    public double timestamp;
    public double ID;

    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] {"lat", "lon", "height", "timestamp", "ID"});
    }

}
