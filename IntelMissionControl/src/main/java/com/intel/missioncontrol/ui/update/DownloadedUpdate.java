/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.update;

/** Immutable object to store downloaded updates info. */
public class DownloadedUpdate {

    private final String serialId;
    private final String fileName;
    private final long fileSize;
    private final String filePath;
    private final String checkSum;

    DownloadedUpdate(String serialId, String fileName, long fileSize, String filePath, String checkSum) {
        this.serialId = serialId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.checkSum = checkSum;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getSerialId() {
        return serialId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCheckSum() {
        return checkSum;
    }
}
