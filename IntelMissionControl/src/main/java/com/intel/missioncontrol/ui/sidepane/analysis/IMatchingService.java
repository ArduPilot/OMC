/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import java.io.File;
import java.util.List;
import java.util.TreeMap;

public interface IMatchingService {
    List<MatchingService.FolderHolder> getSampleMatchingFolders(IMProgressMonitor monitor);

    boolean downloadMatchings(
            String remoteDir, TreeMap<String, Long> files, File matchingLocalFolder, IMProgressMonitor monitor);

    List<MatchingService.FolderHolder> getMatchingDirs();

}
