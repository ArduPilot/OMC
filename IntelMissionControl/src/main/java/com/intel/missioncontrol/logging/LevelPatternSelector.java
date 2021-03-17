/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.logging;

import eu.mavinci.core.obfuscation.IKeepAll;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternMatch;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;

@Plugin(
    name = "LevelPatternSelector",
    category = Core.CATEGORY_NAME,
    elementType = PatternSelector.ELEMENT_TYPE,
    printObject = true
)
@SuppressWarnings("unused")
public class LevelPatternSelector implements PatternSelector, IKeepAll {

    private final Map<String, PatternFormatter[]> formatterMap;
    private final PatternFormatter[] defaultFormatters;

    private LevelPatternSelector(
            PatternMatch[] properties,
            String defaultPattern,
            boolean alwaysWriteExceptions,
            boolean disableAnsi,
            boolean noConsoleNoAnsi,
            Configuration config) {
        this.formatterMap = new HashMap<>();
        PatternParser parser = PatternLayout.createPatternParser(config);

        for (PatternMatch property : properties) {
            try {
                List<PatternFormatter> list =
                    parser.parse(property.getPattern(), alwaysWriteExceptions, disableAnsi, noConsoleNoAnsi);
                this.formatterMap.put(property.getKey(), list.toArray(new PatternFormatter[0]));
            } catch (RuntimeException var14) {
                throw new IllegalArgumentException("Cannot parse pattern '" + property.getPattern() + "'", var14);
            }
        }

        try {
            List<PatternFormatter> list =
                parser.parse(defaultPattern, alwaysWriteExceptions, disableAnsi, noConsoleNoAnsi);
            this.defaultFormatters = list.toArray(new PatternFormatter[0]);
        } catch (RuntimeException var13) {
            throw new IllegalArgumentException("Cannot parse pattern '" + defaultPattern + "'", var13);
        }
    }

    @Override
    public PatternFormatter[] getFormatters(LogEvent logEvent) {
        if (logEvent.getLoggerName().equals(ProvisioningLogger.class.getName())) {
            for (Map.Entry<String, PatternFormatter[]> entry : formatterMap.entrySet()) {
                if (entry.getKey().equals("GUICE")) {
                    return entry.getValue();
                }
            }
        }

        String level = logEvent.getLevel().name();
        for (Map.Entry<String, PatternFormatter[]> entry : formatterMap.entrySet()) {
            if (entry.getKey().equals(level)) {
                return entry.getValue();
            }
        }

        return defaultFormatters;
    }

    @PluginBuilderFactory
    public static LevelPatternSelector.Builder newBuilder() {
        return new LevelPatternSelector.Builder();
    }

    public static class Builder implements org.apache.logging.log4j.core.util.Builder<LevelPatternSelector> {
        @PluginElement("PatternMatch")
        private PatternMatch[] properties;

        @PluginBuilderAttribute("defaultPattern")
        private String defaultPattern;

        @PluginBuilderAttribute("alwaysWriteExceptions")
        private boolean alwaysWriteExceptions = true;

        @PluginBuilderAttribute("disableAnsi")
        private boolean disableAnsi;

        @PluginBuilderAttribute("noConsoleNoAnsi")
        private boolean noConsoleNoAnsi;

        @PluginConfiguration
        private Configuration configuration;

        public Builder() {}

        public LevelPatternSelector build() {
            if (this.defaultPattern == null) {
                this.defaultPattern = "%m%n";
            }

            if (this.properties != null && this.properties.length != 0) {
                return new LevelPatternSelector(
                    this.properties,
                    this.defaultPattern,
                    this.alwaysWriteExceptions,
                    this.disableAnsi,
                    this.noConsoleNoAnsi,
                    this.configuration);
            } else {
                return null;
            }
        }

        public Builder setProperties(PatternMatch[] properties) {
            this.properties = properties;
            return this;
        }

        public Builder setDefaultPattern(String defaultPattern) {
            this.defaultPattern = defaultPattern;
            return this;
        }

        public Builder setAlwaysWriteExceptions(boolean alwaysWriteExceptions) {
            this.alwaysWriteExceptions = alwaysWriteExceptions;
            return this;
        }

        public Builder setDisableAnsi(boolean disableAnsi) {
            this.disableAnsi = disableAnsi;
            return this;
        }

        public Builder setNoConsoleNoAnsi(boolean noConsoleNoAnsi) {
            this.noConsoleNoAnsi = noConsoleNoAnsi;
            return this;
        }

        public Builder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }
    }

}
