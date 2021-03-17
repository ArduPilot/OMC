/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation;

import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ResolvableValidationStatus extends ValidationStatus {

    private final BooleanProperty isRunning = new SimpleBooleanProperty();
    private final BooleanProperty hasWarnings = new SimpleBooleanProperty();
    private final BooleanProperty hasErrors = new SimpleBooleanProperty();
    private final StringProperty okMessage = new SimpleStringProperty("");

    private final ListProperty<ResolvableValidationMessage> messagesResolvable =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private List<InvalidationListener> listeners;

    public ResolvableValidationStatus() {
        reset();
        getMessagesInternal()
            .addListener(
                (InvalidationListener)
                    observable -> {
                        hasWarnings.set(!getWarningMessages().isEmpty());
                        hasErrors.set(!getErrorMessages().isEmpty());
                        invalidated(observable);
                    });
        isRunningProperty().addListener(this::invalidated);
    }

    public void reset() {
        getMessagesInternal().clear();
        messagesResolvable.clear();
        setIsRunning(true);
    }

    public void setIsRunning(boolean running) {
        this.isRunning.set(running);
    }

    public void addMessage(ResolvableValidationMessage message) {
        getMessagesInternal().add(message);
        messagesResolvable.add(message);
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

    public ReadOnlyStringProperty getOkMessage() {
        return okMessage;
    }

    public void setOkMessage(String okMessage) {
        this.okMessage.set(okMessage);
    }

    public ObservableList<ResolvableValidationMessage> getResolvableMessages() {
        return messagesResolvable;
    }

    @Override
    public String toString() {
        return "ResolvableValidationStatus{"
            + "isRunning="
            + isRunning.get()
            + ", hasWarnings="
            + hasWarnings.get()
            + ", hasErrors="
            + hasErrors.get()
            + ", okMessage="
            + okMessage.get()
            + ", messagesResolvable="
            + messagesResolvable.get()
            + '}';
    }

    // invalidation event
    public void invalidated(Observable observable) {
        synchronized (this) {
            if (listeners != null) {
                for (InvalidationListener listener : listeners) {
                    listener.invalidated(observable);
                }
            }
        }
    }

    public void addListener(InvalidationListener listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList<>(1);
            }

            listeners.add(listener);
        }
    }

    public void removeListener(InvalidationListener listener) {
        synchronized (this) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }
}
