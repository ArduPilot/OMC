/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.SuppressFBWarnings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAttribute;

@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class CheckBox extends javafx.scene.control.CheckBox {

    private static final PseudoClass PSEUDO_CLASS_EDITABLE = PseudoClass.getPseudoClass("editable");

    public CheckBox() {
        pseudoClassStateChanged(PSEUDO_CLASS_EDITABLE, true);
    }

    public CheckBox(String text) {
        super(text);
        pseudoClassStateChanged(PSEUDO_CLASS_EDITABLE, true);
    }

    private BooleanProperty editable;

    public final void setEditable(boolean value) {
        editableProperty().set(value);
    }

    public final boolean isEditable() {
        return editable == null || editable.get();
    }

    public final BooleanProperty editableProperty() {
        if (editable == null) {
            editable =
                new BooleanPropertyBase(true) {
                    @Override
                    protected void invalidated() {
                        final Boolean v = get();
                        pseudoClassStateChanged(PSEUDO_CLASS_EDITABLE, v);
                        notifyAccessibleAttributeChanged(AccessibleAttribute.EDITABLE);
                    }

                    @Override
                    public Object getBean() {
                        return CheckBox.this;
                    }

                    @Override
                    public String getName() {
                        return "editable";
                    }
                };
        }

        return editable;
    }

    @Override
    public void fire() {
        if (isEditable()) {
            super.fire();
        } else if (!isDisabled()) {
            fireEvent(new ActionEvent());
        }
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        if (attribute == AccessibleAttribute.EDITABLE) {
            return isEditable();
        }

        return super.queryAccessibleAttribute(attribute, parameters);
    }
}
