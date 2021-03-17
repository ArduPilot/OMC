/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.support;

import java.io.File;

public class CouldNotSendReportException extends RuntimeException {

    private final File reportFolder;

    public CouldNotSendReportException(File reportFolder) {
        super("Could not send error report to support");
        this.reportFolder = reportFolder;
    }

    public File getReportFolder() {
        return reportFolder;
    }

}
