/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.view;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.tools.model.OtherDetailsViewModel;
import com.intel.missioncontrol.ui.navbar.tools.model.UavDataParameter;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtherDetailsView extends ViewBase<OtherDetailsViewModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtherDetailsView.class);

    @InjectViewModel
    private OtherDetailsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private TableView<UavDataParameter<Object>> table;

    @FXML
    private TableColumn<UavDataParameter, String> parameterNameColumn;

    @FXML
    private TableColumn<UavDataParameter, String> parameterValueColumn;

    @Override
    public void initializeView() {
        super.initializeView();
        UavParameterTableViewHelper.prepareColumns(parameterNameColumn, parameterValueColumn);
        parameterNameColumn.setPrefWidth(ScaleHelper.emsToPixels(15.));
        parameterValueColumn.setPrefWidth(ScaleHelper.emsToPixels(25.));

        @SuppressWarnings("unchecked")
        ObservableList<UavDataParameter<Object>> data = viewModel.getData();
        table.setItems(data);
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public OtherDetailsViewModel getViewModel() {
        return viewModel;
    }

}
