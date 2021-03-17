/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import com.intel.missioncontrol.utils.IVersionProvider;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessHelper {
    public static Process exec(String arg) throws IOException {
        return exec(new String[] {arg}, null, null);
    }

    public static Process exec(String args[]) throws IOException {
        if (args == null) {
            args = new String[] {""};
        }

        return exec(args, null, null);
    }

    public static Process exec(String arg, String[] envp, File folder) throws IOException {
        return exec(new String[] {arg}, envp, folder);
    }

    public static Process exec(String args[], String[] envp, File folder) throws IOException {
        if (DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getSystem().isWindows()) {
            for (int i = 0; i != args.length; i++) {
                args[i] = args[i].replaceAll(Pattern.quote("\""), Matcher.quoteReplacement("\\\""));
            }
        }

        return Runtime.getRuntime().exec(args, envp, folder);
    }

}
