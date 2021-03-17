/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.uploader;

import eu.mavinci.desktop.helper.FileHelper;

import java.io.File;

public class UploadFile {

    public UploadFile(File file, boolean foreUpload) {
        this(file, file.getParentFile(), foreUpload);
    }

    public UploadFile(File file, File baseFile, boolean foreUpload) {
        localFile = file;
        localFileBase = baseFile;
        this.foreUpload = foreUpload;
    }

    public final File localFile;
    public final File localFileBase;
    public final boolean foreUpload;

    public String getRemotePath() {
        return FileHelper.getRelativePath(localFile, localFileBase, "/");
    }

    public String getRemoteFolder() {
        return FileHelper.getRelativePath(localFile.getParentFile(), localFileBase, "/");
    }

    public String getRemotePath(String fileNamePrefix) {
        return FileHelper.getRelativePath(
            new File(localFile.getParentFile(), fileNamePrefix + localFile.getName()), localFileBase, "/");
    }
}
