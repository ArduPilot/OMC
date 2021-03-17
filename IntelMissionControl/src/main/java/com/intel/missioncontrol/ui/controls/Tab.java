/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class Tab extends javafx.scene.control.Tab {

    private final ListChangeListener<Node> subtreeChangedMethod = this::subtreeChanged;
    private final ChangeListener<Number> contentHeightChangedMethod = this::contentHeightChanged;

    public Tab() {
        contentProperty().addListener(this::contentChanged);
    }

    private void contentChanged(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
        if (oldValue instanceof Region) {
            removeListenerRecursively((Region)oldValue);
        }

        if (newValue instanceof Region) {
            addListenerRecursively((Region)newValue);
        }

        javafx.scene.control.TabPane tabPane = getTabPane();
        if (tabPane != null) {
            tabPane.requestLayout();
        }
    }

    private void addListenerRecursively(Region region) {
        region.heightProperty().addListener(contentHeightChangedMethod);

        ObservableList<Node> children = region.getChildrenUnmodifiable();
        children.addListener(subtreeChangedMethod);

        for (Node child : children) {
            if (child instanceof Region) {
                addListenerRecursively((Region)child);
            }
        }
    }

    private void removeListenerRecursively(Region region) {
        region.heightProperty().removeListener(contentHeightChangedMethod);

        ObservableList<Node> children = region.getChildrenUnmodifiable();
        children.removeListener(subtreeChangedMethod);

        for (Node child : children) {
            if (child instanceof Region) {
                removeListenerRecursively((Region)child);
            }
        }
    }

    private void subtreeChanged(ListChangeListener.Change<? extends Node> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (Node child : change.getAddedSubList()) {
                    if (child instanceof Region) {
                        addListenerRecursively((Region)child);
                    }
                }
            }

            if (change.wasRemoved()) {
                for (Node child : change.getRemoved()) {
                    if (child instanceof Region) {
                        removeListenerRecursively((Region)child);
                    }
                }
            }
        }
    }

    private void contentHeightChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        getTabPane().requestLayout();
    }

}
