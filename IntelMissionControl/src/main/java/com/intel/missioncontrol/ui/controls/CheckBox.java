/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.SuppressFBWarnings;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAttribute;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class CheckBox extends javafx.scene.control.CheckBox {

    private static final PseudoClass PSEUDO_CLASS_EDITABLE = PseudoClass.getPseudoClass("editable");

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

    public CheckBox() {
        addEventHandler(ActionEvent.ACTION, this::onAction);
        pseudoClassStateChanged(PSEUDO_CLASS_EDITABLE, true);
    }

    public CheckBox(String text) {
        super(text);
        addEventHandler(ActionEvent.ACTION, this::onAction);
        pseudoClassStateChanged(PSEUDO_CLASS_EDITABLE, true);
    }

    private BooleanProperty editable;

    public final void setEditable(boolean value) {
        editableProperty().set(value);
    }

    public final boolean isEditable() {
        return editable == null || editable.get();
    }

    public final BooleanProperty editableProperty() {
        if (editable == null) {
            editable =
                new BooleanPropertyBase(true) {
                    @Override
                    protected void invalidated() {
                        final boolean v = get();
                        pseudoClassStateChanged(PSEUDO_CLASS_EDITABLE, v);
                        notifyAccessibleAttributeChanged(AccessibleAttribute.EDITABLE);
                    }

                    @Override
                    public Object getBean() {
                        return CheckBox.this;
                    }

                    @Override
                    public String getName() {
                        return "editable";
                    }
                };
        }

        return editable;
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

    @Override
    public void fire() {
        if (isEditable()) {
            super.fire();
        } else if (!isDisabled()) {
            fireEvent(new ActionEvent());
        }
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        if (attribute == AccessibleAttribute.EDITABLE) {
            return isEditable();
        }

        return super.queryAccessibleAttribute(attribute, parameters);
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
