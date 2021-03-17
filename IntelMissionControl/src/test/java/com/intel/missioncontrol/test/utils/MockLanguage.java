/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.test.utils;

import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.obfuscation.IKeepAll;

public class MockLanguage implements ILanguageHelper {
    @Override
    public String getString(Class<?> definingClass, String key, Object... params) {
        return "mock";
    }

    @Override
    public String getString(String key, Object... params) {
        return "mock";
    }

    @Override
    public <E extends Enum<E> & IKeepAll> String toFriendlyName(E value) {
        return "mock";
    }

    @Override
    public <E extends Enum<E>> String toFriendlyName(String customPrefix, E value) {
        return "mock";
    }

    @Override
    public <E extends Enum<E> & IKeepAll> E fromFriendlyName(Class<? extends Enum<E>> class1, String name) {
        return null;
    }

    @Override
    public <E extends Enum<E>> E fromFriendlyName(Class<? extends Enum<E>> class1, String customPrefix, String name) {
        return null;
    }
}
