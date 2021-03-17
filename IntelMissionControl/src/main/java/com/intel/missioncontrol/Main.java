/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.splashscreen.SplashScreen;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        SplashScreen.show(Bootstrapper.MAILSLOT_NAME);
        javafx.application.Application.launch(Bootstrapper.class, args);
    }

}
