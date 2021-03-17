/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling;

import com.logicstyle.samplr.FileResultsArchiver;
import com.logicstyle.samplr.Request;
import com.logicstyle.samplr.ResultFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MFileResultsArchiver extends FileResultsArchiver {

    public final int maxProfilingHistory = 10;

    public MFileResultsArchiver(File currentProfilingsFolder) {
        File currentProfilingFolder =
            new File(
                currentProfilingsFolder,
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(System.currentTimeMillis()));

        File[] old = currentProfilingsFolder.listFiles();
        if (old != null && old.length >= maxProfilingHistory) {
            List<File> oldF = Arrays.asList(old);
            Collections.sort(
                oldF,
                new Comparator<File>() {

                    @Override
                    public int compare(File o1, File o2) {
                        int t1 = -1;
                        int t2 = -1;
                        try {
                            t1 = Integer.parseInt(o1.getName());
                        } catch (Exception e) {
                        }

                        try {
                            t2 = Integer.parseInt(o2.getName());
                        } catch (Exception e) {
                        }

                        return Integer.compare(t1, t2);
                    }
                });
            for (int n = 0; n < oldF.size() - maxProfilingHistory; n++) {
                oldF.get(n).delete();
            }
        }

        setOutputDirectory(currentProfilingFolder);
    }

    @Override
    public void processResult(Request request, List<ResultFile> resultFiles) {
        if (resultFiles.size() < 2) {
            return;
        }

        File resultsDir =
            new File(getOutputDirectory(), request.getClass().getSimpleName() + "_" + String.valueOf(request.getId()));

        resultsDir.mkdirs();
        if (!resultsDir.exists()) {
            throw new RuntimeException("Cannot create diretory to output results: " + resultsDir.getAbsolutePath());
        }

        try {
            for (ResultFile resultFile : resultFiles) {
                writeFile(resultsDir, resultFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(File directory, ResultFile resultFile) throws FileNotFoundException, IOException {
        File outputFile = new File(directory, resultFile.getName());
        FileOutputStream out = new FileOutputStream(outputFile);

        byte[] tmp = new byte[8192];
        int c;
        try {
            InputStream in = resultFile.getContent();
            while ((c = in.read(tmp)) != -1) {
                out.write(tmp, 0, c);
            }

            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
