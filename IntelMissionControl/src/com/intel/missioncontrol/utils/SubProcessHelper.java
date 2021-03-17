/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.main.OsTypes;
import java.io.IOException;

/** Created by aleonov on 10/25/2017. */
public class SubProcessHelper {

    public static Process executeFile(String path) throws IOException {
        String[] cmdParts;
        OsTypes system = DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getSystem();
        if (system.isWindows()) {
            cmdParts = new String[] {"rundll32", "url.dll,FileProtocolHandler", path};
        } else if (system.isLinux()) {
            cmdParts = new String[] {"xdg-open", path};
        } else if (system.isMac()) {
            cmdParts = new String[] {"open", path};
        } else {
            throw new UnsupportedOperationException("Unsupported operating system detected: " + system);
        }

        return new ProcessBuilder().command(cmdParts).inheritIO().start();
    }
}
