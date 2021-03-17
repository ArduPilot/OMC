/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import static com.intel.missioncontrol.Bootstrapper.MAILSLOT_NAME;

import com.intel.missioncontrol.splashscreen.SplashScreen;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (System.getProperty("mailslot") != null) {
            javafx.application.Application.launch(SplashScreen.class);
        } else {
            SplashScreen.show(MAILSLOT_NAME);
            javafx.application.Application.launch(Bootstrapper.class);
        }
    }

}
