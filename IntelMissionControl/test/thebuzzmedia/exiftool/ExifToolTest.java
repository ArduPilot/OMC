/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */


package thebuzzmedia.exiftool;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExifToolTest {
    @Test
    public void testStartup() {
        var test = ExifTool.instance;
        ExifTool.checkFeatureSupport(ExifTool.Feature.STAY_OPEN);
        ExifTool.instance.isRunning();
        ExifTool.instance.close();
    }
} 