/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.desktop.helper.uploader.Uploader;
import eu.mavinci.desktop.helper.uploader.UploaderMAVinciSCP;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorDownloading {

    public static final String KEY = "eu.mavinci.desktop.helper.ErrorDownloading";

    public static final String[] ignoreWarnings = {};
    /*    "SEVERE: Retrieval failed for",
        "SEVERE: Exception while retrieving resources for",
        "WARNING: http://mavinci.eu/wmsproxy",
        "exif-Tool:Warning:",
        "WARNING: <?xml version",
        "SEVERE: Exception attempting to read texture",
        "SEVERE: Error attempting to retrieve",
        "SEVERE: Error reading from connection to",
        "SEVERE: Retrieval returned no content for http",
        "WARNING: Surface tile draw context is null",
        "WARNING: Exception encountered while repainting",
        "SEVERE: Exception attempting to create texture",
        "SEVERE: Exception attempting to read texture file",
        "WARNING: Unrecognized element",
        "WARNING: http://mavinci.de/wmsproxy/",
    };*/

    public static final String[] realWarnings = {
        "] ERROR",
    };

    static final String replacementWarning = Matcher.quoteReplacement("REMOVED-wrng/Svr no:");
    static final String replacementMinWarning = Matcher.quoteReplacement("REMOVED-minor_wrng");

    public static final String[] ignoreWarningsQuoted = new String[ignoreWarnings.length];

    static {
        for (int i = 0; i != ignoreWarnings.length; i++) {
            ignoreWarningsQuoted[i] = Pattern.quote(ignoreWarnings[i]);
        }
    }

    public static File downloadErrorUploadImc(String token, File baseFolder, IMProgressMonitor mon) {
        File targetFolder = new File(baseFolder, token);

        final String remoteBaseDir = "/data/" + token + "/";

        final Uploader up = new UploaderMAVinciSCP(remoteBaseDir);
        final TreeMap<String, Long> files = up.scanDir("", Uploader.DEFAULT_RETRYS);
        if (files == null) {
            return null;
        }

        boolean ok = up.downloadIMC(files, remoteBaseDir, targetFolder, Uploader.DEFAULT_RETRYS, mon);
        //        System.out.println("ok=" + ok);
        //        System.out.println("files="+up.lastWrittenFiles);

        mon.setMaximum(up.lastWrittenFiles.size());

        int k = 0;
        for (File f : up.lastWrittenFiles) {
            mon.setProgressNote(FileHelper.getRelativePath(f, targetFolder), k);
            if (mon.isCanceled()) {
                break;
            }

            k++;
            if (f.isFile() && f.exists()) {
                File fUnzipped = f;
                String name = f.getName();
                // don'T extract too large files, this would take way tooo long
                if (name.endsWith(".zip")
                        && !(name.endsWith(".vlg.zip") || name.endsWith(".flg.zip") || name.startsWith("core_backend_"))
                        && f.length() < 10 * 1024 * 1024) {
                    // System.out.println("Unzip file = " + f.getName());
                    try {
                        fUnzipped = FileHelper.unzipSingleFile(f, false);
                        if (f != null) {
                            f.delete();
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Debug.WARNING, "problem unzipping File", e);
                    }
                }

                Ensure.notNull(fUnzipped, "fUnzipped");
                name = fUnzipped.getName();

                if (DependencyInjector.getInstance()
                                .getInstanceOf(ISettingsManager.class)
                                .getSection(GeneralSettings.class)
                                .getOperationLevel()
                                .compareTo(OperationLevel.TECHNICIAN)
                            >= 0
                        && name.endsWith(".log")
                        && name.startsWith("application")) {
                    // System.out.println("deep scan it!");
                    boolean shouldOpen = false;
                    try {
                        String content = FileHelper.readFileAsString(fUnzipped);
                        int i = 0;
                        for (String s : ignoreWarningsQuoted) {
                            i++;
                            content = content.replaceAll(s, replacementWarning + i);
                        }

                        content = content.replaceAll(Pattern.quote("WARNING"), replacementMinWarning);
                        for (String s : realWarnings) {
                            if (content.contains(s)) {
                                shouldOpen = true;
                                break;
                            }
                        }

                        /*if (fUnzipped.length() < AppLogger.MAX_FILE_SIZE
                                && !name.endsWith("_0.log")
                                && !content.contains("EOM")) {
                            shouldOpen = true;
                            content +=
                                "\nSEVERE: END OF FILE (EOF) MISSING. The application crashed or was killed by the user.";
                        }*/

                        File fout = new File(fUnzipped.getParentFile(), "_" + name);
                        try (PrintWriter out = new PrintWriter(fout, "UTF-8")) {
                            out.print(content);
                        } catch (IOException ex) {
                            Debug.getLog().log(Level.WARNING, "Problems writing file: " + name, ex);
                        }

                        if (shouldOpen) {
                            FileHelper.openFile(fout);
                        }
                    } catch (Exception e) {
                        Debug.getLog()
                            .log(
                                Level.WARNING,
                                "Problems removing not important warning from logfile : " + fUnzipped,
                                e);
                    }
                }
            }
        }

        mon.close();
        return targetFolder;
    }
}
