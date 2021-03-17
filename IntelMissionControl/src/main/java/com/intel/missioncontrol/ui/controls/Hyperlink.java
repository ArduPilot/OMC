/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Hyperlink extends javafx.scene.control.Hyperlink {

    private final StringProperty shortcut = new SimpleStringProperty(this, "shortcut");

    private final ObjectProperty<Command> command =
        new SimpleObjectProperty<>(this, "command") {
            @Override
            protected void invalidated() {
                Command command = get();
                if (command != null) {
                    disableProperty().bind(command.notExecutableProperty());
                } else {
                    disableProperty().unbind();
                }
            }
        };

    private final ObjectProperty<Object> commandParameter = new SimpleObjectProperty<>();

    public Hyperlink() {
        addEventHandler(ActionEvent.ACTION, this::onAction);
    }

    public Hyperlink(String text) {
        super(text);
        addEventHandler(ActionEvent.ACTION, this::onAction);
    }

    public StringProperty shortcutProperty() {
        return shortcut;
    }

    public @Nullable String getShortcut() {
        return shortcut.get();
    }

    public void setShortcut(@Nullable String shortcut) {
        this.shortcut.set(shortcut);
    }

    public ObjectProperty<Command> commandProperty() {
        return command;
    }

    public @Nullable Command getCommand() {
        return command.get();
    }

    public void setCommand(Command command) {
        this.command.set(command);
    }

    public ObjectProperty<Object> commandParameterProperty() {
        return commandParameter;
    }

    public @Nullable Object getCommandParameter() {
        return commandParameter.get();
    }

    public void setCommandParameter(Object parameter) {
        this.commandParameter.set(parameter);
    }

    @SuppressWarnings("unchecked")
    private void onAction(ActionEvent actionEvent) {
        Command command = getCommand();
        Object parameter = getCommandParameter();
        if (command instanceof ParameterizedCommand && parameter != null) {
            ((ParameterizedCommand)command).execute(parameter);
        } else if (command != null) {
            command.execute();
        }
    }

}
