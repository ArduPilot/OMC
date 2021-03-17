/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.components;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.utils.AoiDefines;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;

public class ScalableTableView<T> extends TableView<T> {

    public static final double CELL_SIZE = ScaleHelper.emsToPixels(2.5);

    public ScalableTableView() {
        setFixedCellSize(CELL_SIZE);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        minHeightProperty().bind(prefHeightProperty());
        maxHeightProperty().bind(prefHeightProperty());
    }

    public DoubleBinding scalableHeightBinding() {
        return Bindings.size(getItems()).multiply(getFixedCellSize()).add(ScalableTableView.CELL_SIZE);
    }

    public void applyCoordMultipplier(double coordMultiplier, TableColumn... columns) {
        DoubleBinding multiplier = widthProperty().multiply(coordMultiplier);
        for (TableColumn column : columns) {
            column.prefWidthProperty().bind(multiplier);
            column.minWidthProperty().bind(multiplier);
            column.maxWidthProperty().bind(multiplier);
        }
    }

    @Override
    protected void layoutChildren() {
        for (Node n : lookupAll(".scroll-bar:vertical")) {
            if (n instanceof ScrollBar) {
                ScrollBar scrollBar = (ScrollBar)n;
                scrollBar.setPrefWidth(AoiDefines.ZERO_VALUE);
                scrollBar.setMaxWidth(AoiDefines.ZERO_VALUE);
                scrollBar.setVisible(false);
                scrollBar.setOpacity(1);
            }
        }
        for  (TableColumn<?,?> column : getColumns()){
            column.setReorderable(false);
        }

        super.layoutChildren();
    }

}
