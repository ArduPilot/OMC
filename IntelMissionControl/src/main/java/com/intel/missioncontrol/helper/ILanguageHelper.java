/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import com.intel.missioncontrol.Localizable;

public interface ILanguageHelper {

    /** Use {@link ILanguageHelper#getString(Class, String, Object...)} instead. */
    @Deprecated
    String getString(String key, Object... params);

    String getString(Class<?> definingClass, String key, Object... params);

    <E extends Enum<E> & Localizable> String toFriendlyName(E value);

    <E extends Enum<E> & Localizable> String toFriendlyName(String customPrefix, E value);

    <E extends Enum<E> & Localizable> E fromFriendlyName(Class<? extends Enum<E>> class1, String name);

    <E extends Enum<E> & Localizable> E fromFriendlyName(
            Class<? extends Enum<E>> class1, String customPrefix, String name);

}
