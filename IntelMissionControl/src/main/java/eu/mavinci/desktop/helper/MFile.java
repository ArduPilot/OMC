/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MFile {

    public static String adaptToCurSystem(String fileStr) {
        if (DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getSystem().isWindows()) {
            return fileStr.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement(File.separator));
        } else {
            return fileStr.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement(File.separator));
        }
    }

    public static String adaptToUnix(String fileStr) {
        return fileStr.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement("\\"));
    }

}
