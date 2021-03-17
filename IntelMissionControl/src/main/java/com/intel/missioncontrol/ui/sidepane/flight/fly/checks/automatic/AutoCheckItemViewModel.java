/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic;

import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.drone.validation.IFlightValidator;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;

public class AutoCheckItemViewModel implements ViewModel {

    private final UIAsyncStringProperty messageString = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty firstResolveActionText = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty secondResolveActionText = new UIAsyncStringProperty(this);
    private final UIAsyncObjectProperty<AlertType> alertImageType = new UIAsyncObjectProperty<>(this);

    private final ObjectProperty<IResolveAction> firstResolveAction = new SimpleObjectProperty<>();
    private final ObjectProperty<IResolveAction> secondResolveAction = new SimpleObjectProperty<>();

    private final Command firstResolveActionCommand;
    private final Command secondResolveActionCommand;

    @SuppressLinter(
        value = "IllegalViewModelMethod",
        reviewer = "mstrauss",
        justification = "Used to manually create view models."
    )
    public static AutoCheckItemViewModel fromValidator(IFlightValidator flightValidator) {
        return new AutoCheckItemViewModel(flightValidator);
    }

    private AutoCheckItemViewModel(IFlightValidator flightValidator) {
        this.messageString.bind(
            Bindings.createStringBinding(
                () -> flightValidator.getValidationStatus().getMessageString(),
                flightValidator.validationStatusProperty()));
        this.alertImageType.bind(
            Bindings.createObjectBinding(
                () -> flightValidator.getValidationStatus().getAlertType(),
                flightValidator.validationStatusProperty()));

        // TODO

        if (flightValidator.getFirstResolveAction() != null) {
            this.firstResolveAction.bind(flightValidator.getFirstResolveAction());
        }

        if (flightValidator.getSecondResolveAction() != null) {
            this.secondResolveAction.bind(flightValidator.getSecondResolveAction());
        }

        firstResolveActionCommand =
            new DelegateCommand(
                () -> {
                    var action = firstResolveAction.get();
                    if (action != null) {
                        action.resolve();
                    }
                });

        firstResolveActionText.bind(
            Bindings.createStringBinding(
                () -> firstResolveAction.get() != null ? firstResolveAction.get().getMessage() : null,
                firstResolveAction));

        secondResolveActionCommand =
            new DelegateCommand(
                () -> {
                    var action = secondResolveAction.get();
                    if (action != null) {
                        action.resolve();
                    }
                });

        secondResolveActionText.bind(
            Bindings.createStringBinding(
                () -> secondResolveAction.get() != null ? secondResolveAction.get().getMessage() : null,
                secondResolveAction));
    }

    public ReadOnlyProperty<String> messageStringProperty() {
        return messageString;
    }

    ReadOnlyProperty<String> firstResolveActionTextProperty() {
        return firstResolveActionText;
    }

    ReadOnlyProperty<String> secondResolveActionTextProperty() {
        return secondResolveActionText;
    }

    Command getFirstResolveActionCommand() {
        return firstResolveActionCommand;
    }

    Command getSecondResolveActionCommand() {
        return secondResolveActionCommand;
    }

    public ReadOnlyProperty<AlertType> alertTypeProperty() {
        return alertImageType;
    }

}
