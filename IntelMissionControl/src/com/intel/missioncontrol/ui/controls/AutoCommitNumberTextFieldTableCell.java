/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.concurrent.Dispatcher;
import java.util.function.Function;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

public class AutoCommitNumberTextFieldTableCell<S, T> extends TableCell<S, T> {
    private final Function<T, DoubleProperty> propertyFunc;
    private final StringConverter<Double> stringConverter = new DoubleStringConverter();
    private TextField textField;
    private DoubleProperty property;

    public AutoCommitNumberTextFieldTableCell(Function<T, DoubleProperty> propertyFunc) {
        this.propertyFunc = propertyFunc;
    }

    private boolean muteChange;

    private final ChangeListener<Number> propertyChange =
        new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (muteChange) {
                    return;
                }

                muteChange = true;
                try {
                    textField.textProperty().setValue(stringConverter.toString(newValue.doubleValue()));
                } finally {
                    muteChange = false;
                }
            }
        };

    private final ChangeListener<String> textChange =
        new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (muteChange) {
                    return;
                }

                muteChange = true;
                try {
                    property.setValue(stringConverter.fromString(newValue));
                } catch (NumberFormatException e) {
                    // ignore, undo change
                    Dispatcher.postToUI(
                        () -> textField.textProperty().setValue(stringConverter.toString(property.get())));
                } finally {
                    muteChange = false;
                }
            }
        };

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (property != null) {
            property.removeListener(propertyChange);
            property = null;
        }

        if (textField != null) {
            textField.textProperty().removeListener(textChange);
            textField = null;
        }

        if (item == null || empty) {
            textProperty().unbind();
            setText(null);
            setGraphic(null);
        } else {
            property = propertyFunc.apply(item);
            textField = new TextField();
            textField.textProperty().set(stringConverter.toString(property.get()));
            property.addListener(propertyChange);
            textField.textProperty().addListener(textChange);
            textField.editableProperty().bind(editableProperty());
            textProperty().bind(textField.textProperty());
        }
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return;
        }

        super.startEdit();

        if (!isEditing()) {
            return;
        }

        setGraphic(textField);
        textProperty().unbind();
        setText(null);
        textField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        textProperty().bind(textField.textProperty());
    }

}
