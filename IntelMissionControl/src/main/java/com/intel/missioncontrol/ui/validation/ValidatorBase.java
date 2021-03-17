/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import de.saxsys.mvvmfx.utils.validation.Severity;
import de.saxsys.mvvmfx.utils.validation.Validator;
import eu.mavinci.desktop.main.debug.Debug;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import org.asyncfx.concurrent.Dispatcher;

public abstract class ValidatorBase<T> implements Validator {

    private final ResolvableValidationStatus validationStatus = new ResolvableValidationStatus();
    private final ObservableValue<T> observableValue;
    private T value;
    private final QuantityFormat quantityFormat;

    protected ValidatorBase(T value, IQuantityStyleProvider quantityStyleProvider) {
        this.quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setSignificantDigits(3);
        this.observableValue = null;
        this.value = value;
    }

    protected ValidatorBase(ObservableValue<T> observableValue, IQuantityStyleProvider quantityStyleProvider) {
        this.quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setSignificantDigits(3);
        this.value = null;
        this.observableValue = observableValue;
        this.observableValue.addListener((observable, oldValue, newValue) -> invalidate());
    }

    protected String formatLength(double dist) {
        return formatLength(Quantity.of(dist, Unit.METER));
    }

    protected String formatLength(Quantity dist) {
        return quantityFormat.format(dist, UnitInfo.LOCALIZED_LENGTH);
    }

    protected String formatBytes(long bytes) {
        return quantityFormat.format(Quantity.of(bytes * 1024 * 1024, Unit.BYTE), UnitInfo.STORAGE);
    }

    @Override
    public final ResolvableValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public final T getValidationValue() {
        return this.value;
    }

    final void setValidationValue(T value) {
        if (observableValue != null) {
            throw new IllegalStateException("Validator is already set to an ObservableValue<> instance.");
        }

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                T oldValue = this.value;
                this.value = value;
                onValidationValueChanged(oldValue, value);
                invalidate();
            });
    }

    protected void onValidationValueChanged(T oldValue, T newValue) {}

    protected final void invalidate() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not on JavaFX thread.");
        }

        validationStatus.reset();

        try {
            boolean stillRunning = !onInvalidated(value != null ? value : observableValue.getValue());
            validationStatus.setIsRunning(stillRunning);
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "cant execute test", e);
        }
    }

    /** Validates the provided value. Returns true if the validation was completed; false otherwise. */
    protected abstract boolean onInvalidated(T newValue);

    protected final void addWarning(
            String message, ValidationMessageCategory category, IResolveAction... resolveActions) {
        validationStatus.addMessage(
            new ResolvableValidationMessage(this, Severity.WARNING, category, message, resolveActions));
    }

    protected final void setOkMessage(String okMessage) {
        validationStatus.setOkMessage(okMessage);
    }

}
