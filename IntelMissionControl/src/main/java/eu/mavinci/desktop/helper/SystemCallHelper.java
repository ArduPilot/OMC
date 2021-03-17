/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import eu.mavinci.desktop.main.debug.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Level;

public class SystemCallHelper {

    /**
     * @param cmdarray command and parameter of System call
     * @param dir the directory execute system call
     * @return the return code of system call , default is -1
     */
    public static int systemCallWait(String[] cmdarray, File dir) {
        int result = -1;
        Debug.getLog().config("systemCall:\n" + Arrays.asList(cmdarray) + "\nDir=" + dir);
        Process p;
        try {
            p = ProcessHelper.exec(cmdarray, null, dir);
            try (InputStream stdin = p.getErrorStream()) {
                InputStreamReader isr = new java.io.InputStreamReader(stdin);
                BufferedReader br = new java.io.BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    Debug.getLog().log(Level.FINE, "systemCall: processErr:" + line);
                }

                try {
                    result = p.exitValue();
                } catch (Exception ie) {
                    Debug.getLog().log(Level.FINE, "systemCall: could not get exit value of process", ie);
                    try {
                        result = p.waitFor();
                    } catch (InterruptedException e) {
                        Debug.getLog().log(Level.FINE, "systemCall: waiting for process got interrupted", e);
                    }
                }
            }
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "systemCall: process failed", e);
        }

        return result;
    }
}
