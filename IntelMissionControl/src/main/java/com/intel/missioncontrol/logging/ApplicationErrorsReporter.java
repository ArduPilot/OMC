/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.logging;

import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.desktop.main.debug.Debug;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "ApplicationErrorsReporter", category = "Core", elementType = "appender", printObject = true)
public final class ApplicationErrorsReporter extends AbstractAppender implements IKeepAll {
    private ApplicationErrorsReporter(String name) {
        super(name, null, null, false);
    }

    @PluginFactory
    public static ApplicationErrorsReporter createAppender(@PluginAttribute("name") String name) {
        if (name == null) {
            LOGGER.error("No name provided for ApplicationErrorsReporter");
            return null;
        }

        return new ApplicationErrorsReporter(name);
    }

    @Override
    public void append(LogEvent logEvent) {
        if (isEligibleForCatching(logEvent)) {
            Debug.reportIssueAppearance();
        }
    }

    private boolean isEligibleForCatching(LogEvent logEvent) {
        if (logEvent.getLoggerName().equals("gov.nasa.worldwind")) {
            return false;
        }
        return logEvent.getLevel().intLevel() <= Level.WARN.intLevel();
    }
}
