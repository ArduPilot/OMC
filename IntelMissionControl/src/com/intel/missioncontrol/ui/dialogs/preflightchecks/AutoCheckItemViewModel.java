/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.annotation.Nullable;

public class AutoCheckItemViewModel implements ViewModel {

    private final StringProperty message;
    private final StringProperty firstResolveActionText = new SimpleStringProperty();
    private final StringProperty secondResolveActionText = new SimpleStringProperty();
    private final BooleanProperty isCritical = new SimpleBooleanProperty();
    private final IntegerProperty numberOfResolveActions = new SimpleIntegerProperty(0);
    private final BooleanProperty isOkMessage = new SimpleBooleanProperty(false);
    private final BooleanProperty inProgress = new SimpleBooleanProperty(false);

    private ICommand firstResolveActionCommand;
    private ICommand secondResolveActionCommand;

    public AutoCheckItemViewModel(ResolvableValidationMessage message, boolean inProgress) {
        this.message = new ReadOnlyStringWrapper(message.getMessage());
        this.isCritical.set(message.getCategory() == ValidationMessageCategory.BLOCKING);
        this.inProgress.set(inProgress);
        int size = message.getResolveActions().size();
        numberOfResolveActions.set(size);
        if (size > 0) {
            firstResolveActionCommand = new DelegateCommand(() -> message.getResolveActions().get(0).resolve());

            firstResolveActionText.set(message.getResolveActions().get(0).getMessage());
        }

        if (size > 1) {
            secondResolveActionCommand = new DelegateCommand(() -> message.getResolveActions().get(1).resolve());

            firstResolveActionText.set(message.getResolveActions().get(1).getMessage());
        }
    }

    public AutoCheckItemViewModel(String okMessage, boolean inProgress) {
        this.message = new ReadOnlyStringWrapper(okMessage);
        this.inProgress.set(inProgress);
        this.isOkMessage.set(true);
    }

    public ReadOnlyStringProperty messageProperty() {
        return message;
    }

    public ReadOnlyStringProperty firstResolveActionTextProperty() {
        return firstResolveActionText;
    }

    public ReadOnlyStringProperty secondResolveActionTextProperty() {
        return secondResolveActionText;
    }

    public ReadOnlyBooleanProperty isCriticalProperty() {
        return isCritical;
    }

    public ReadOnlyIntegerProperty numberOfResolveActionsProperty() {
        return numberOfResolveActions;
    }

    public ReadOnlyBooleanProperty isOkMessageProperty() {
        return isOkMessage;
    }

    public @Nullable ICommand getFirstResolveActionCommand() {
        return firstResolveActionCommand;
    }

    public @Nullable ICommand getSecondResolveActionCommand() {
        return secondResolveActionCommand;
    }

    public BooleanProperty isInProgressProperty() {
        return inProgress;
    }

    public boolean isInProgress() {
        return inProgress.get();
    }
}
