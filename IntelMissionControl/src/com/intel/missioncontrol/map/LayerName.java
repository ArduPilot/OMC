/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.helper.ILanguageHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class LayerName {

    private final String key;
    private final String pattern;
    private final String[] params;

    public LayerName(String pattern) {
        this(pattern, (String[])null);
    }

    public LayerName(String pattern, @Nullable String... params) {
        if (pattern == null) {
            pattern = "";
        }

        pattern = pattern.trim();
        if (pattern.length() >= 2 && pattern.charAt(0) == '%' && pattern.charAt(1) != '%') {
            key = pattern.substring(1);
        } else {
            key = null;
        }

        this.pattern = pattern;
        this.params = params;
    }

    @Override
    public String toString() {
        return pattern;
    }

    public String toString(ILanguageHelper languageHelper) {
        String localizedPattern = key != null ? languageHelper.getString(key) : pattern;
        return params != null ? String.format(localizedPattern, (Object[])params) : localizedPattern;
    }

}
