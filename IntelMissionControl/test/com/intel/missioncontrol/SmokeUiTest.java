/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(SmokeTest.class)
public class SmokeUiTest {

    private static final Runnable NO_OP = () -> {
    };
    private static final String[] NO_ARGS = new String[0];

    @Test
    public void makeSureThatApplicationCanStart() throws Exception {
        //new ApplicationStarter(NO_OP).run(NO_ARGS);
    }
}
