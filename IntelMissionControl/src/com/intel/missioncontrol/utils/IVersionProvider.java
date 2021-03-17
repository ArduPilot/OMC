/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import eu.mavinci.core.main.OsTypes;
import java.io.File;

public interface IVersionProvider {
    String getAppBranch();

    String getBuildCommitTime();

    String getCommitID();

    long getBuildCommitTimeAsLong();

    String getAppMajorVersion();

    String getHumanReadableVersion();

    String getApplicationName();

    boolean isCompatible(String externalReleaseVersion);

    OsTypes getSystem();

    boolean isDebuggingCompiled();

    boolean isObfuscated();

    boolean is64Bit();

    boolean isSystem64Bit();

    boolean is32Bit();

    boolean isWindows();

    boolean isMac();

    boolean isUnix();

    File getCodeSourceFile();

    boolean isEclipseLaunched();

    File getInstallDir();

    File getExecFile();

    boolean isOpenJDK();
}
