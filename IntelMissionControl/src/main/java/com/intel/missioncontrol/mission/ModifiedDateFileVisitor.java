/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

/** @author Vladimir Iordanov */
public class ModifiedDateFileVisitor extends SimpleFileVisitor<Path> {
    private long latestModifiedFileDate;
    private long latestModifiedFolderDate;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.toString().contains(MissionConstants.FOLDER_NAME_SCREENSHOT)) {
            return FileVisitResult.CONTINUE;
        }

        long folderDate = attrs.lastModifiedTime().toMillis();
        if (latestModifiedFolderDate < folderDate) {
            latestModifiedFolderDate = folderDate;
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        long fileDate = attrs.lastModifiedTime().toMillis();
        if (latestModifiedFileDate < fileDate) {
            latestModifiedFileDate = fileDate;
        }

        return FileVisitResult.CONTINUE;
    }

    public Date getDate() {
        long result;
        if (latestModifiedFileDate > 0) {
            result = latestModifiedFileDate;
        } else if (latestModifiedFolderDate > 0) {
            result = latestModifiedFolderDate;
        } else {
            result = System.currentTimeMillis();
        }

        return new Date(result);
    }
}
