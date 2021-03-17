/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.desktop.helper.uploader.Uploader;
import eu.mavinci.desktop.helper.uploader.UploaderMAVinciSCP;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;

public class MatchingService implements IMatchingService {

    final String remoteBaseDir = "/sampleData/IMC/";
    final Uploader up = new UploaderMAVinciSCP(remoteBaseDir);

    private final List<FolderHolder> matchingDirs = new ArrayList<>();

    @Override
    public List<FolderHolder> getSampleMatchingFolders(IMProgressMonitor monitor) {
        matchingDirs.clear();

        final TreeMap<String, Long> files = up.scanDirImc("", Uploader.DEFAULT_RETRYS, monitor);
        if (files == null) {
            return matchingDirs;
        }

        // 1. run, search matchings.
        for (Map.Entry<String, Long> entry : files.entrySet()) {
            if (entry.getKey().endsWith(MapLayerMatching.DEFAULT_FILENAME)) {
                String name =
                    entry.getKey().substring(0, entry.getKey().length() - MapLayerMatching.DEFAULT_FILENAME.length());
                matchingDirs.add(new FolderHolder(name, 0, new TreeMap<>()));
            }
        }

        // 2. run, count sizes of matchings
        for (Map.Entry<String, Long> entry : files.entrySet()) {
            for (FolderHolder folder : matchingDirs) {
                if (entry.getKey().startsWith(folder.getName())) {
                    String fileName = entry.getKey().substring(folder.getName().length());
                    folder.getFiles().put(fileName, entry.getValue());
                    folder.setSize(folder.getSize() + entry.getValue());
                }
            }
        }

        //        downloadMatchings(matchingDirs.get(3).getName(), matchingDirs.get(3).getFiles(), new
        // File("/Users/akorotenko/Intel Mission Control/test"), mon);

        return matchingDirs;
    }

    @Override
    public boolean downloadMatchings(
            String remoteDir, TreeMap<String, Long> files, File matchingLocalFolder, IMProgressMonitor monitor) {
        boolean ok = up.downloadIMC(files, remoteDir, matchingLocalFolder, Uploader.DEFAULT_RETRYS, monitor);

        return ok;
    }

    public List<FolderHolder> getMatchingDirs() {
        return matchingDirs;
    }

    public static class FolderHolder {
        private String name;
        private long size;
        private TreeMap<String, Long> files;

        public FolderHolder(String name, long size, TreeMap<String, Long> files) {
            this.name = name;
            this.size = size;
            this.files = files;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public TreeMap<String, Long> getFiles() {
            return files;
        }

        public void setFiles(TreeMap<String, Long> files) {
            this.files = files;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
         * name + size
         *
         * @return
         */
        public String getTitle() {
            return name + " " + FileUtils.byteCountToDisplaySize(size);
        }
    }
}
