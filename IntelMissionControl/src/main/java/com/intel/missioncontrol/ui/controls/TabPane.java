/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.sun.javafx.scene.NodeHelper;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;

public class TabPane extends javafx.scene.control.TabPane {

    private final ChangeListener<Tab> selectedItemChangedMethod = this::selectedItemChanged;
    private final BooleanExpression treeVisible;
    private final BooleanExpression treeShowing;

    public TabPane() {
        selectionModelProperty().addListener(this::selectionModelChanged);
        selectionModelChanged(null, null, getSelectionModel());
        treeVisible = NodeHelper.treeVisibleProperty(this);
        treeShowing = NodeHelper.treeShowingProperty(this);
        treeVisible.addListener(observable -> requestLayout());
        treeShowing.addListener(observable -> requestLayout());
    }

    private void selectionModelChanged(
            ObservableValue<? extends SingleSelectionModel<Tab>> observable,
            SingleSelectionModel<Tab> oldValue,
            SingleSelectionModel<Tab> newValue) {
        if (oldValue != null) {
            oldValue.selectedItemProperty().removeListener(selectedItemChangedMethod);
        }

        if (newValue != null) {
            newValue.selectedItemProperty().addListener(selectedItemChangedMethod);
        }

        requestLayout();
    }

    private void selectedItemChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        for (Tab tab : getTabs()) {
            Node content = tab.getContent();
            if (content != null) {
                content.setManaged(tab == newValue);
            }
        }

        requestLayout();
    }

}
