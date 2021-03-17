/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import de.saxsys.mvvmfx.utils.validation.Validator;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ResolvableValidationStatus extends ValidationStatus {

    private BooleanProperty isRunning = new SimpleBooleanProperty();
    private BooleanProperty hasWarnings = new SimpleBooleanProperty();
    private BooleanProperty hasErrors = new SimpleBooleanProperty();
    private StringProperty okMessage = new SimpleStringProperty("");

    ListProperty<ResolvableValidationMessage> messagesResolveable =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    public final Validator validator;

    protected ResolvableValidationStatus(Validator validator) {
        this.validator = validator;
        getMessagesInternal()
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        hasWarnings.set(!getWarningMessages().isEmpty());
                        hasErrors.set(!getErrorMessages().isEmpty());
                    }
                });
    }

    void reset() {
        getMessagesInternal().clear();
        messagesResolveable.clear();
        isRunning.set(true);
    }

    void setIsRunning(boolean running) {
        this.isRunning.set(running);
    }

    void addMessage(ResolvableValidationMessage message) {
        messagesResolveable.add(message);
        getMessagesInternal().add(message);
    }

    public ReadOnlyBooleanProperty isRunningProperty() {
        return isRunning;
    }

    public ReadOnlyBooleanProperty hasWarningsProperty() {
        return hasWarnings;
    }

    public ReadOnlyBooleanProperty hasErrorsProperty() {
        return hasErrors;
    }

    public StringProperty getOkMessage() {
        return okMessage;
    }

    protected void setOkMessage(String okMessage) {
        this.okMessage.set(okMessage);
    }

    public ObservableList<ResolvableValidationMessage> getResolveableMessages() {
        return messagesResolveable;
    }
}
