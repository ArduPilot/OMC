/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ManifestInfo {

    private static int major;
    private static int minor;
    private static int patch;
    private static OffsetDateTime buildTime;
    private static String branchName;
    private static String commitHash;

    static {
        int major = 0, minor = 0, patch = 0;
        String branchName = "", commitHash = "";
        OffsetDateTime buildTime = OffsetDateTime.MIN;

        try {
            Enumeration enumeration =
                Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);

            if (enumeration.hasMoreElements()) {
                try (InputStream stream = ((URL)enumeration.nextElement()).openStream()) {
                    Manifest manifest = new Manifest(stream);
                    Attributes attributes = manifest.getMainAttributes();
                    String version = attributes.getValue("Implementation-Version");
                    String[] tokens = version.split("\\.");
                    if (tokens.length > 0) {
                        major = Integer.parseInt(tokens[0]);
                    }

                    if (tokens.length > 1) {
                        minor = Integer.parseInt(tokens[1]);
                    }

                    if (tokens.length > 2) {
                        patch = Integer.parseInt(tokens[2]);
                    }

                    branchName = attributes.getValue("Branch");
                    commitHash = attributes.getValue("Build");

                    String timestamp = attributes.getValue("Timestamp");
                    buildTime = OffsetDateTime.parse(timestamp);
                }
            }
        } catch (Exception ignored) {
        }

        ManifestInfo.major = major;
        ManifestInfo.minor = minor;
        ManifestInfo.patch = patch;
        ManifestInfo.buildTime = buildTime;
        ManifestInfo.branchName = branchName;
        ManifestInfo.commitHash = commitHash;
    }

    public static String getVersion() {
        return major + "." + minor + "." + patch;
    }

    public static int getMajor() {
        return major;
    }

    public static int getMinor() {
        return minor;
    }

    public static int getPatch() {
        return patch;
    }

    public static OffsetDateTime getBuildTime() {
        return buildTime;
    }

    public static String getBranchName() {
        return branchName;
    }

    public static String getCommitHash() {
        return commitHash;
    }

}
