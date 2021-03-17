/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import java.io.File;
import java.util.Date;

public class LogFileListItem {

    private final String qualifiedFileName;
    private final Long size;
    private final Date date;

    public LogFileListItem(File file) {
        this.qualifiedFileName = file.getAbsolutePath();
        this.size = file.length();
        this.date = new Date(file.lastModified());
    }

    public String getQualifiedFileName() {
        return qualifiedFileName;
    }

    public Long getSize() {
        return size;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }
}
