/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import java.util.function.Function;
import javafx.beans.property.Property;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

public class AutoCommitTextFieldTableCell<S, T> extends TableCell<S, T> {
    private final Function<T, Property<String>> propertyFunc;
    private TextField textField;

    public AutoCommitTextFieldTableCell(Function<T, Property<String>> propertyFunc) {
        this.propertyFunc = propertyFunc;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            textProperty().unbind();
            setText(null);
            setGraphic(null);
        } else {
            var property = propertyFunc.apply(item);
            textField = new TextField();
            textField.textProperty().bindBidirectional(property);
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
