/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.Localizable;
import com.intel.missioncontrol.helper.ILanguageHelper;
import javafx.util.StringConverter;

/** Created by akorotenko on 7/4/17. */
public class EnumConverter<T extends Enum<T> & Localizable> extends StringConverter<T> {

    private final ILanguageHelper languageHelper;
    private final Class<T> clazz;

    public EnumConverter(ILanguageHelper languageHelper, Class<T> clazz) {
        this.languageHelper = languageHelper;
        this.clazz = clazz;
    }

    @Override
    public String toString(T object) {
        return languageHelper.toFriendlyName(object);
    }

    @Override
    public T fromString(String string) {
        return languageHelper.fromFriendlyName(clazz, string);
    }

}
