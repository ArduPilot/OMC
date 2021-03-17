/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.intel.missioncontrol.settings.GeneralSettings;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LanguageHelper implements ILanguageHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageHelper.class);
    private static final Joiner DOT_JOINER = Joiner.on('.');

    private final ResourceBundle resourceBundle;

    public LanguageHelper() {
        this(Locale.getDefault());
    }

    @Inject
    public LanguageHelper(GeneralSettings settings) {
        this(settings.getLocale());
    }

    private LanguageHelper(Locale locale) {
        this.resourceBundle =
            ResourceBundle.getBundle(
                "com/intel/missioncontrol/IntelMissionControl", locale, new UTF8PropertiesControl());
    }

    @Override
    public String getString(String key, Object... params) {
        try {
            if (params != null && params.length > 0) {
                return String.format(resourceBundle.getString(key), params);
            }

            return resourceBundle.getString(key);
        } catch (NullPointerException | MissingResourceException e) {
            LOGGER.error("Can't find resource string for key: {}", key, e);
            return key;
        }
    }

    @Override
    public String getString(Class<?> definingClass, String key, Object... params) {
        Expect.notNull(definingClass, "definingClass", key, "key");
        String fullKey = definingClass.getName() + "." + key;

        try {
            if (params != null && params.length > 0) {
                return String.format(resourceBundle.getString(fullKey), params);
            }

            return resourceBundle.getString(fullKey);
        } catch (NullPointerException | MissingResourceException e) {
            LOGGER.error("Can't find resource string for key: {}", fullKey, e);
            return key;
        }
    }

    @Override
    public <E extends Enum<E> & IKeepAll> String toFriendlyName(E value) {
        if (value == null) {
            return "<null>";
        }

        return toFriendlyName(value.getClass().getName(), value);
    }

    @Override
    public <E extends Enum<E>> String toFriendlyName(String customPrefix, E value) {
        if (value == null) {
            return "<null>";
        }

        String key = DOT_JOINER.join(customPrefix, value.name());

        if (resourceBundle.containsKey(key)) {
            return resourceBundle.getString(key);
        }

        return value.toString();
    }

    @Override
    public <E extends Enum<E> & IKeepAll> E fromFriendlyName(Class<? extends Enum<E>> enumType, String name) {
        return fromFriendlyName(enumType, enumType.getName(), name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E fromFriendlyName(Class<? extends Enum<E>> enumType, String customPrefix, String name) {
        for (Enum<E> enumValue : enumType.getEnumConstants()) {
            String enumConstantName = DOT_JOINER.join(customPrefix, enumValue.name());
            if (resourceBundle.containsKey(enumConstantName)
                    && resourceBundle.getString(enumConstantName).equals(name)) {
                return (E)enumValue;
            }
        }

        for (Enum<E> constant : enumType.getEnumConstants()) {
            if (constant.name().equals(name)) {
                return (E)constant;
            }
        }

        return null;
    }

}
