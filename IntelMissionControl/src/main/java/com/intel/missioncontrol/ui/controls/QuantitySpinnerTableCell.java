/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.helper.NodeHelper;
import com.intel.missioncontrol.measure.FillMode;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import java.util.function.Function;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;

public class QuantitySpinnerTableCell<S, T, Q extends Quantity<Q>> extends TableCell<S, T> {

    protected final IQuantityStyleProvider quantityStyleProvider;
    protected final Function<T, QuantityProperty<Q>> propertyFunc;
    protected final int significantDigits;
    protected final int maxFractionDigits;
    protected final FillMode fillMode;
    protected final Quantity<Q> min;
    protected final Quantity<Q> max;
    protected final double amountToStepBy;
    protected final boolean wrapAround;
    protected Group editContent;
    protected Spinner<Quantity<Q>> spinner;
    protected Node oldClip;
    protected int oldDepthIndex;

    public QuantitySpinnerTableCell(
            IQuantityStyleProvider quantityStyleProvider,
            Function<T, QuantityProperty<Q>> propertyFunc,
            Quantity<Q> min,
            Quantity<Q> max,
            int significantDigits,
            int maxFractionDigits,
            FillMode fillMode,
            double amountToStepBy,
            boolean wrapAround) {
        this.quantityStyleProvider = quantityStyleProvider;
        this.propertyFunc = propertyFunc;
        this.min = min;
        this.max = max;
        this.significantDigits = significantDigits;
        this.maxFractionDigits = maxFractionDigits;
        this.fillMode = fillMode;
        this.amountToStepBy = amountToStepBy;
        this.wrapAround = wrapAround;

        editableProperty()
            .addListener(
                ((observable, oldValue, newValue) -> {
                    if (!newValue && isEditing()) {
                        cancelEdit();
                    }
                }));
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            textProperty().unbind();
            setGraphic(null);
            setText(null);
        } else {
            editContent = new Group();
            var property = propertyFunc.apply(item);
            spinner = new AutoCommitSpinner<>();
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    property.getUnitInfo(),
                    significantDigits,
                    maxFractionDigits,
                    min,
                    max,
                    amountToStepBy,
                    wrapAround);
            valueFactory.valueProperty().bindBidirectional(property);
            spinner.setValueFactory(valueFactory);
            spinner.editableProperty().bind(editableProperty());
            editContent.getChildren().add(spinner);

            var quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
            quantityFormat.setSignificantDigits(significantDigits);
            quantityFormat.setMaximumFractionDigits(maxFractionDigits);
            quantityFormat.setFillMode(fillMode);
            textProperty().bind(QuantityBindings.createStringBinding(property, quantityFormat));
            setAlignment(Pos.CENTER_RIGHT);
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

        textProperty().unbind();
        setGraphic(editContent);
        setText(null);
        oldDepthIndex = NodeHelper.getDepthIndex(this);
        oldClip = getClip();
        setClip(null);
        toFront();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        textProperty().bind(spinner.getEditor().textProperty());
        NodeHelper.setDepthIndex(this, oldDepthIndex);
        setClip(oldClip);
    }

}
