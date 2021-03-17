/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

/** Created by eivanchenko on 8/10/2017. */
public abstract class SimpleUavDataParameter<T> extends UavDataParameter<T> {

    private String internalName;

    public SimpleUavDataParameter(UavDataParameterType type) {
        this(DEFAULT_NAME, type);
    }

    public SimpleUavDataParameter(String internalName, UavDataParameterType type) {
        this.internalName = internalName;
        setDisplayName(internalName);
        setType(type);
    }

    public String getInternalName() {
        return internalName;
    }

    @Override
    protected abstract Object extractRawValue(T valueContainer);
}
