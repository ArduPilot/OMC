/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

import com.intel.missioncontrol.Localizable;
import com.intel.missioncontrol.helper.ILanguageHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LayerNameTest {

    private static class LangHelper implements ILanguageHelper {
        @Override
        public String getString(Class<?> definingClass, String key, Object... params) {
            if ("key".equals(key)) {
                return "value=%s";
            }

            throw new IllegalArgumentException();
        }

        @Override
        public String getString(String key, Object... params) {
            if ("key".equals(key)) {
                return "value=%s";
            }

            throw new IllegalArgumentException();
        }

        @Override
        public <E extends Enum<E> & Localizable> String toFriendlyName(E value) {
            return null;
        }

        @Override
        public <E extends Enum<E> & Localizable> String toFriendlyName(String customPrefix, E value) {
            return null;
        }

        @Override
        public <E extends Enum<E> & Localizable> E fromFriendlyName(Class<? extends Enum<E>> class1, String name) {
            return null;
        }

        @Override
        public <E extends Enum<E> & Localizable> E fromFriendlyName(
                Class<? extends Enum<E>> class1, String customPrefix, String name) {
            return null;
        }
    }

    @Test
    void LayerName_DetectKey() {
        LangHelper langHelper = new LangHelper();
        LayerName name;

        name = new LayerName("", "param");
        Assertions.assertEquals("", name.toString(langHelper));

        name = new LayerName("%key", "param");
        Assertions.assertEquals("value=param", name.toString(langHelper));

        name = new LayerName("%%key", "param");
        Assertions.assertEquals("%key", name.toString(langHelper));

        name = new LayerName("key%%notAKey", "param");
        Assertions.assertEquals("key%notAKey", name.toString(langHelper));
    }

}
