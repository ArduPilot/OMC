/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.warnings;

import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

class WarningItemViewModel implements ViewModel {

    private final StringProperty message;
    private final StringProperty firstResolveActionText = new SimpleStringProperty();
    private final StringProperty secondResolveActionText = new SimpleStringProperty();
    private final ObjectProperty<ValidationMessageCategory> category = new SimpleObjectProperty<>();

    private ICommand firstResolveActionCommand;
    private ICommand secondResolveActionCommand;

    WarningItemViewModel(ResolvableValidationMessage message) {
        this.message = new ReadOnlyStringWrapper(message.getMessage());
        this.category.set(message.getCategory());

        int size = message.getResolveActions().size();
        if (size > 0) {
            firstResolveActionCommand = new DelegateCommand(() -> message.getResolveActions().get(0).resolve());
            firstResolveActionText.set(message.getResolveActions().get(0).getMessage());
        }

        if (size > 1) {
            secondResolveActionCommand = new DelegateCommand(() -> message.getResolveActions().get(1).resolve());
            secondResolveActionText.set(message.getResolveActions().get(1).getMessage());
        }
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

    public ReadOnlyObjectProperty<ValidationMessageCategory> categoryProperty() {
        return category;
    }

    public ICommand getFirstResolveActionCommand() {
        return firstResolveActionCommand;
    }

    public ICommand getSecondResolveActionCommand() {
        return secondResolveActionCommand;
    }

}
