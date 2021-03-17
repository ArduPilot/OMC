/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.view;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.navbar.tools.model.UavDataParameter;
import javafx.scene.control.TableColumn;

/** Created by eivanchenko on 9/7/2017. */
public class UavParameterTableViewHelper {

    public static final double COLUMN_PREFFERED_WIDTH = ScaleHelper.emsToPixels(12.5);

    public static void prepareColumns(
            TableColumn<? extends UavDataParameter, String> nameColumn,
            TableColumn<? extends UavDataParameter, String> valueColumn) {
        nameColumn.setPrefWidth(COLUMN_PREFFERED_WIDTH);
        nameColumn.setCellValueFactory(p -> p.getValue().displayedNameProperty());

        valueColumn.setPrefWidth(COLUMN_PREFFERED_WIDTH);
        valueColumn.setCellValueFactory(p -> p.getValue().valueProperty());
    }

}
