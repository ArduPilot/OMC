/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.google.common.base.Objects;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.ui.controls.skins.SpinnerSkin;
import java.lang.reflect.Field;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.Skin;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

public final class AutoCommitSpinner<T> extends Spinner<T> {

    private static final Field helperField;

    static {
        // In the constructor of Spinner a listener is added to the focusProperty. The listener in question commits its
        // changes directly via a final method. This behavior is a bug-fixed introduced with JDK 9. However, the
        // "correct", behavior depends on that we add our own listeners to the focus Property. Here, we reflect on the
        // helper field of ReadOnlyBooleanPropertyBase so that we can remove listeners from focusProperty in the
        // constructor
        Field tmpHelperField;
        try {
            tmpHelperField = ReadOnlyBooleanPropertyBase.class.getDeclaredField("helper");
            tmpHelperField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        helperField = tmpHelperField;
    }

    private final BooleanProperty allowEmptyValue = new SimpleBooleanProperty();
    private final ChangeListener<Object> quantityStyleChanged = (observable, oldValue, newValue) -> updateEditorText();
    private boolean isDirty = false;

    public AutoCommitSpinner() {
        try {
            helperField.set(focusedProperty(), null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        final TextField editor = getEditor();

        focusedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        isDirty = false;
                    } else {
                        if (isDirty) {
                            commitEditorText();
                        }
                    }
                });

        valueFactoryProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue instanceof IQuantityStyleProvider) {
                        var quantityStyleProvider = (IQuantityStyleProvider)oldValue;
                        quantityStyleProvider.angleStyleProperty().removeListener(quantityStyleChanged);
                        quantityStyleProvider.timeStyleProperty().removeListener(quantityStyleChanged);
                    }

                    if (newValue instanceof IQuantityStyleProvider) {
                        var quantityStyleProvider = (IQuantityStyleProvider)newValue;
                        quantityStyleProvider
                            .angleStyleProperty()
                            .addListener(new WeakChangeListener<>(quantityStyleChanged));
                        quantityStyleProvider
                            .timeStyleProperty()
                            .addListener(new WeakChangeListener<>(quantityStyleChanged));
                    }
                });

        editor.addEventHandler(
            KeyEvent.KEY_RELEASED,
            event -> isDirty = AutoCommitSpinner.this.isEditable() && event.getCode() != KeyCode.ENTER);

        editor.setOnKeyPressed(
            keyEvent -> {
                if (AutoCommitSpinner.this.isEditable()) {
                    switch (keyEvent.getCode()) {
                    case UP:
                        increment(1);
                        break;
                    case DOWN:
                        decrement(1);
                        break;
                    }
                }
            });

        editor.setOnAction(action -> commitEditorText());

        getStyleClass().add(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);

        setOnScroll(
            scrollEvent -> {
                if (!isEditable() || !isFocused() || isDisabled()) {
                    return;
                }

                if (scrollEvent.getDeltaY() > 0) {
                    editor.commitValue();
                    getValueFactory().increment(1);
                } else if (scrollEvent.getDeltaY() < 0) {
                    editor.commitValue();
                    getValueFactory().decrement(1);
                }

                scrollEvent.consume();
            });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SpinnerSkin<>(this);
    }

    public BooleanProperty allowEmptyValueProperty() {
        return allowEmptyValue;
    }

    public boolean isAllowEmptyValue() {
        return allowEmptyValue.get();
    }

    void setAllowEmptyValue(boolean value) {
        this.allowEmptyValue.set(value);
    }

    @Override
    public void increment(int steps) {
        SpinnerValueFactory<T> valueFactory = getValueFactory();
        if (valueFactory == null) {
            throw new IllegalStateException("Can't increment Spinner with a null SpinnerValueFactory");
        }

        int caretPosition = getEditor().getCaretPosition();
        commitEditorText();
        getEditor().positionCaret(caretPosition);
        valueFactory.increment(steps);
    }

    @Override
    public void decrement(int steps) {
        SpinnerValueFactory<T> valueFactory = getValueFactory();
        if (valueFactory == null) {
            throw new IllegalStateException("Can't decrement Spinner with a null SpinnerValueFactory");
        }

        int caretPosition = getEditor().getCaretPosition();
        commitEditorText();
        getEditor().positionCaret(caretPosition);
        valueFactory.decrement(steps);
    }

    private void commitEditorText() {
        String text = getEditor().getText();
        SpinnerValueFactory<T> valueFactory = getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T oldValue = valueFactory.getValue();
                T newValue = converter.fromString(text);

                if (newValue != null) {
                    if (!Objects.equal(converter.toString(oldValue), text)) {
                        valueFactory.setValue(newValue);
                    }

                    if (newValue.equals(oldValue)) {
                        getEditor().setText(converter.toString(newValue));
                    }
                } else if (allowEmptyValue.get()) {
                    valueFactory.setValue(null);
                } else if (oldValue != null) {
                    getEditor().setText(converter.toString(oldValue));
                } else {
                    getEditor().setText(null);
                }
            }
        }
    }

    private void updateEditorText() {
        SpinnerValueFactory<T> valueFactory = getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                getEditor().setText(converter.toString(valueFactory.getValue()));
            }
        }
    }

}
