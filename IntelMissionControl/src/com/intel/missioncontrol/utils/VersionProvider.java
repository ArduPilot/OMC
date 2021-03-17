/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.main.OsTypes;
import eu.mavinci.core.obfuscation.IKeepConstructors;
import eu.mavinci.core.update.UpdateURL;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionProvider implements IVersionProvider, IKeepConstructors {

    public static final Logger log = LoggerFactory.getLogger(VersionProvider.class);

    public static final String PROPERTIES_FILE_NAME = "application.properties";
    public static final String PROPERTIES_BUILD_COMMIT_TIME = "git.commit.time";
    public static final String PROPERTIES_BUILD_COMMIT_ID = "build.commit.id";
    public static final String PROPERTIES_APP_NAME = "application.name";
    public static final String PROPERTIES_APP_VERSION = "application.version";

    public static final String PROPERTIES_APP_BRANCH = "git.branch";

    public static final String BUILD_TIME_FORMAT = "dd.MM.yyyy '@' HH:mm:ss z";

    private Properties properties;

    @Inject
    public VersionProvider() {}

    @Override
    public String getApplicationName() {
        return getProperties().getProperty(PROPERTIES_APP_NAME);
    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();

            try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)) {
                if (is == null) {
                    log.error(
                        "Read application properties are NULL:"
                            + PROPERTIES_FILE_NAME
                            + ". please try a maven build before starting to get this file created");
                    return properties;
                }

                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8.name());
                properties.load(reader);
            } catch (IOException e) {
                log.error("Read application properties error", e);
            }
        }

        return properties;
    }

    @Override
    public String getAppBranch() {
        return getProperties().getProperty(PROPERTIES_APP_BRANCH);
    }

    @Override
    public String getBuildCommitTime() {
        return getProperties().getProperty(PROPERTIES_BUILD_COMMIT_TIME);
    }

    long commitTimeAsLong = -1;

    @Override
    public long getBuildCommitTimeAsLong() {
        if (commitTimeAsLong < 0) {
            ZonedDateTime dateTime;
            try {
                dateTime = ZonedDateTime.parse(getBuildCommitTime(), DateTimeFormatter.ofPattern(BUILD_TIME_FORMAT));
            } catch (Exception e) {
                log.info(
                    "Could not parse build time from properties. Probably maven git plugin did not process it or your running this not form a installed build");
                dateTime = LocalDateTime.of(2017, 7, 18, 19, 00).atZone(ZoneId.systemDefault());
            }

            commitTimeAsLong = dateTime.toInstant().getEpochSecond();
        }

        return commitTimeAsLong;
    }

    @Override
    public String getAppMajorVersion() {
        return getAppBranch();
    }

    @Override
    public boolean isCompatible(String externalReleaseVersion) {
        return externalReleaseVersion.equals(getAppMajorVersion());
    }

    private OsTypes osTypes;

    @Override
    public OsTypes getSystem() {
        if (osTypes == null) {
            if (isWindows()) {
                osTypes = OsTypes.Win;
            } else if (isMac()) {
                osTypes = OsTypes.Mac;
            } else {
                osTypes = OsTypes.Linux64;
            }
        }

        return osTypes;
    }

    @Override
    public String getHumanReadableVersion() {
        return UpdateURL.getHumanReadableVersion(getAppBranch(), getBuildCommitTimeAsLong());
    }

    @Override
    public String getCommitID() {
        return getProperties().getProperty(PROPERTIES_BUILD_COMMIT_ID);
    }

    private Boolean isDebuggingCompiled = null;

    @Override
    public boolean isDebuggingCompiled() {
        if (isDebuggingCompiled == null) {
            StackTraceElement[] tr = (new Exception()).getStackTrace();
            // StackTraceElement top = tr[tr.length-1];
            StackTraceElement bottom = tr[0];
            isDebuggingCompiled = bottom.getLineNumber() > 0;
        }

        return isDebuggingCompiled;
    }

    private Boolean isObfuscated = null;

    @Override
    public boolean isObfuscated() {
        if (isObfuscated == null) {
            StackTraceElement[] tr = (new Exception()).getStackTrace();
            StackTraceElement bottom = tr[0];
            isObfuscated = !bottom.getMethodName().equals("isObfuscated");
        }

        return isObfuscated;
    }

    private static final String keys[] = {
        "sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch",
    };

    @Override
    public boolean is64Bit() {
        for (String key : keys) {
            String property = System.getProperty(key);
            if (property != null) {
                return (property.indexOf("64") >= 0);
            }
        }

        return false;
    }

    @Override
    public boolean isSystem64Bit() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            return (System.getenv("ProgramFiles(x86)") != null);
        } else {
            return is64Bit();
        }
    }

    @Override
    public boolean is32Bit() {
        return !is64Bit();
    }

    @Override
    public boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);
    }

    @Override
    public boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);
    }

    @Override
    public boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        // linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }

    private File codeSourceFile = null;

    @Override
    public File getCodeSourceFile() {
        if (codeSourceFile == null) {
            try {
                codeSourceFile =
                    new File(
                        URLDecoder.decode(
                            VersionProvider.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
                            "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Version Detection Framework failed", e);
            }
        }

        return codeSourceFile;
    }

    private Boolean isEclipseLaunched = null;

    @Override
    public boolean isEclipseLaunched() {
        if (isEclipseLaunched == null) {
            isEclipseLaunched = getCodeSourceFile().isDirectory();
        }

        return isEclipseLaunched;
    }

    @Override
    public File getInstallDir() {
        getCodeSourceFile();
        if (codeSourceFile.isDirectory()) return codeSourceFile;
        return codeSourceFile.getParentFile();
    }

    @Override
    public File getExecFile() {
        if (!isEclipseLaunched()) {
            File dir = getInstallDir();
            if (isWindows()) {
                return new File(dir, "IntelMissionControl.exe");
            } else if (isUnix()) {
                return new File(dir, "intel-mission-control");
            }
        }
        // MAC is not supporting commandline launch
        throw new RuntimeException("commandline launch of this Open Mission Control version ist not supported");
    }

    @Override
    public boolean isOpenJDK() {
        String javaruntimeProperty = System.getProperty("java.runtime.name");
        Ensure.notNull(javaruntimeProperty, "javaruntimeProperty");
        return javaruntimeProperty.toLowerCase().contains("openjdk");
    }

}
