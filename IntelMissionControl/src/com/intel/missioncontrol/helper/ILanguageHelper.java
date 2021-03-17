/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import eu.mavinci.core.obfuscation.IKeepAll;

public interface ILanguageHelper {

    String getString(String key, Object... params);

    <E extends Enum<E> & IKeepAll> String toFriendlyName(E value);

    <E extends Enum<E>> String toFriendlyName(String customPrefix, E value);

    <E extends Enum<E> & IKeepAll> E fromFriendlyName(Class<? extends Enum<E>> class1, String name);

    <E extends Enum<E>> E fromFriendlyName(Class<? extends Enum<E>> class1, String customPrefix, String name);

}
