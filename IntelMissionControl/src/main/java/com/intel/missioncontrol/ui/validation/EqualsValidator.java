/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.utils.validation.ObservableRuleBasedValidator;
import de.saxsys.mvvmfx.utils.validation.ObservableRules;
import de.saxsys.mvvmfx.utils.validation.Severity;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import eu.mavinci.core.obfuscation.IKeepClassname;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableNumberValue;

public class EqualsValidator<T extends ObservableNumberValue> extends ObservableRuleBasedValidator
        implements IKeepClassname {

    public EqualsValidator(ILanguageHelper languageHelper, T source, T target) {
        this(languageHelper, null, source, target);
    }

    public EqualsValidator(ILanguageHelper languageHelper, String message, T source, T target) {
        addRule(
            ObservableRules.fromPredicate(source, Objects::nonNull),
            new ResolvableValidationMessage(
                this, Severity.WARNING, languageHelper.getString(EqualsValidator.class.getName() + ".emptyValue")));

        addRule(
            ObservableRules.fromPredicate(target, Objects::nonNull),
            new ResolvableValidationMessage(
                this, Severity.WARNING, languageHelper.getString(EqualsValidator.class.getName() + ".emptyValue")));

        BooleanBinding equalBinding = Bindings.equal(source, target);

        if (message == null) {
            message = languageHelper.getString(EqualsValidator.class.getName() + ".valuesNotEqual");
        }

        addRule(equalBinding, ValidationMessage.warning(message));
    }

}
