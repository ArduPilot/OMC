/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.view;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.tools.model.OrientationDataViewModel;
import com.intel.missioncontrol.ui.navbar.tools.model.SmartUavDataParameter;
import com.intel.missioncontrol.ui.navbar.tools.model.UavDataParameter;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

public class OrientationDataView extends ViewBase<OrientationDataViewModel> {

    @InjectViewModel
    private OrientationDataViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private TableView<UavDataParameter<OrientationData>> table;

    @FXML
    private TableColumn<SmartUavDataParameter, String> parameterNameColumn;

    @FXML
    private TableColumn<SmartUavDataParameter, String> parameterValueColumn;

    @Override
    public void initializeView() {
        super.initializeView();
        UavParameterTableViewHelper.prepareColumns(parameterNameColumn, parameterValueColumn);
        parameterValueColumn.setPrefWidth(ScaleHelper.emsToPixels(13.5));
        table.setItems(viewModel.getData());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public OrientationDataViewModel getViewModel() {
        return viewModel;
    }

}
