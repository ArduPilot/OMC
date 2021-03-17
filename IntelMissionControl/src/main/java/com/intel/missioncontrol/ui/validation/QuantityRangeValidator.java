/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import de.saxsys.mvvmfx.utils.validation.ObservableRuleBasedValidator;
import de.saxsys.mvvmfx.utils.validation.ObservableRules;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import eu.mavinci.core.obfuscation.IKeepClassname;
import java.util.Objects;
import javafx.beans.value.ObservableValue;

public class QuantityRangeValidator<Q extends Quantity<Q>> extends ObservableRuleBasedValidator
        implements IKeepClassname {

    private final QuantityFormat quantityFormat = new QuantityFormat();

    public QuantityRangeValidator(
            IQuantityStyleProvider quantityStyleProvider,
            ILanguageHelper languageHelper,
            ObservableValue<Quantity<Q>> source,
            Quantity<Q> min,
            Quantity<Q> max) {
        quantityFormat.setAngleStyle(quantityStyleProvider.getAngleStyle());
        quantityFormat.setMaximumFractionDigits(2);

        addRule(
            ObservableRules.fromPredicate(source, Objects::nonNull),
            ValidationMessage.error(languageHelper.getString(QuantityRangeValidator.class.getName() + ".emptyValue")));

        addRule(
            ObservableRules.fromPredicate(source, q -> q == null || q.compareTo(min) >= 0),
            ValidationMessage.error(
                languageHelper.getString(
                    QuantityRangeValidator.class.getName() + ".valueTooSmall", quantityFormat.format(min))));

        addRule(
            ObservableRules.fromPredicate(source, q -> q == null || q.compareTo(max) <= 0),
            ValidationMessage.error(
                languageHelper.getString(
                    QuantityRangeValidator.class.getName() + ".valueTooLarge", quantityFormat.format(max))));
    }

}
