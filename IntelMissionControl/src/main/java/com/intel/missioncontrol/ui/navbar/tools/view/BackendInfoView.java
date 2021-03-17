/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.view;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.tools.model.BackendInfoViewModel;
import com.intel.missioncontrol.ui.navbar.tools.model.SmartUavDataParameter;
import com.intel.missioncontrol.ui.navbar.tools.model.UavDataParameter;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.plane.sendableobjects.BackendInfo;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

public class BackendInfoView extends ViewBase<BackendInfoViewModel> {

    @InjectViewModel
    private BackendInfoViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private TableView<UavDataParameter<BackendInfo>> table;

    @FXML
    private TableColumn<SmartUavDataParameter, String> parameterNameColumn;

    @FXML
    private TableColumn<SmartUavDataParameter, String> parameterValueColumn;

    @Override
    public void initializeView() {
        super.initializeView();
        UavParameterTableViewHelper.prepareColumns(parameterNameColumn, parameterValueColumn);
        parameterNameColumn.setPrefWidth(ScaleHelper.emsToPixels(15.));
        parameterValueColumn.setPrefWidth(ScaleHelper.emsToPixels(20.));
        table.setItems(viewModel.getData());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public BackendInfoViewModel getViewModel() {
        return viewModel;
    }

}
