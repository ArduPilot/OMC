/*
/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public abstract class ValidationAggregator<T> {

    private final List<ValidatorBase<T>> validators = new ArrayList<>();
    private List<ResolvableValidationStatus> validationStatuses = new ArrayList<>();
    private IntegerProperty runningValidators = new SimpleIntegerProperty(0);

    protected ValidationAggregator(ValidatorBase<T>... validators) {
        for (ValidatorBase<T> validator : validators) {
            this.validators.add(validator);

            validator
                .getValidationStatus()
                .isRunningProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue) {
                            runningValidators.set(runningValidators.get() + 1);
                        } else {
                            runningValidators.set(runningValidators.get() - 1);
                        }
                    });
            validator.invalidate(); // initialize it
        }

        validationStatuses.addAll(
            this.validators.stream().map(ValidatorBase::getValidationStatus).collect(Collectors.toList()));
    }

    public ReadOnlyIntegerProperty runningValidatorsProperty() {
        return runningValidators;
    }

    public List<ResolvableValidationStatus> getValidationStatuses() {
        return validationStatuses;
    }

    protected List<ValidatorBase<T>> getValidators() {
        return validators;
    }

    protected void setValidationValue(T value) {
        for (ValidatorBase<T> validator : validators) {
            validator.setValidationValue(value);
        }
    }

    public ValidatorBase<T> getValidator(Class<? extends ValidatorBase<T>> type) {
        for (ValidatorBase<T> validator : validators) {
            if (validator.getClass() == type) {
                return validator;
            }
        }

        throw new IllegalArgumentException("Validator not found: " + type);
    }

}
