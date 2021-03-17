/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.model;

import eu.mavinci.desktop.main.core.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by eivanchenko on 8/10/2017. */
public abstract class UavDataParameter<T> {

    public static final String DEFAULT_NAME = "UNKNOWN";
    public static final String NOT_A_VALUE = "NaN";

    private static final Logger LOGGER = LoggerFactory.getLogger(UavDataParameter.class);

    private StringProperty displayedName = new SimpleStringProperty(DEFAULT_NAME);
    private StringProperty value = new SimpleStringProperty(NOT_A_VALUE);
    private UavDataParameterType type = UavDataParameterType.GENERAL;

    public void reset() {
        value.setValue(NOT_A_VALUE);
    }

    public boolean isInArray() {
        return false;
    }

    public boolean isInList() {
        return false;
    }

    public int getChannel() {
        return -1;
    }

    public final UavDataParameterType getType() {
        return type;
    }

    protected final void setType(UavDataParameterType type) {
        if (type == null) {
            this.type = UavDataParameterType.GENERAL;
        } else {
            this.type = type;
        }
    }

    protected abstract Object extractRawValue(T valueContainer) throws Exception;

    public void updateValue(T valueContainer) {
        String newVal = NOT_A_VALUE;
        if (valueContainer != null) {
            try {
                Object rawValue = extractRawValue(valueContainer);
                switch (getType()) {
                case DOUBLE:
                case FLOAT:
                    newVal = rawValue.toString();
                    break;
                default:
                    newVal = rawValue.toString();
                }
            } catch (Exception e) {
                LOGGER.debug("Unable to update a '{}' parameter", getDisplayedName());
            }
        }

        value.setValue(newVal);
    }

    public String getDisplayedName() {
        return displayedName.get();
    }

    public final StringProperty displayedNameProperty() {
        return displayedName;
    }

    public final String getDisplayName() {
        return displayedName.get();
    }

    protected final void setDisplayName(String name) {
        if (name == null) {
            displayedName.set(DEFAULT_NAME);
        } else {
            displayedName.set(name);
        }
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }
}
