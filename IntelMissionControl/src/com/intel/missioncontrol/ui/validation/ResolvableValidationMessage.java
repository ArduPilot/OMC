/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import de.saxsys.mvvmfx.utils.validation.Severity;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import de.saxsys.mvvmfx.utils.validation.Validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResolvableValidationMessage extends ValidationMessage {

    private final Validator validator;
    private final List<IResolveAction> resolveActions;
    private final ValidationMessageCategory category;

    public ResolvableValidationMessage(
            Validator validator, Severity severity, ValidationMessageCategory category, String message, IResolveAction... actions) {
        super(severity, message);
        this.validator = validator;
        this.category = category;
        this.resolveActions = actions == null ? Collections.emptyList() : Arrays.asList(actions);
    }

    public ResolvableValidationMessage(
            Validator validator, Severity severity, String message, IResolveAction... actions) {
        this(validator, severity, ValidationMessageCategory.NORMAL, message, actions);
    }

    public List<IResolveAction> getResolveActions() {
        return resolveActions;
    }

    /**
     * this can be used to tie different message to the same root cause class
     *
     * @return parent validator
     */
    public Validator getValidator() {
        return validator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        ResolvableValidationMessage that = (ResolvableValidationMessage)o;

        if (!validator.equals(that.validator)) {
            return false;
        }

        return resolveActions.equals(that.resolveActions);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + validator.hashCode();
        result = 31 * result + resolveActions.hashCode();
        return result;
    }

    public ValidationMessageCategory getCategory() {
        return category;
    }
}
