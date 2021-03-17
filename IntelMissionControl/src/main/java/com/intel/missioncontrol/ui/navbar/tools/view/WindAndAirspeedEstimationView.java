/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.view;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.tools.model.UavDataParameter;
import com.intel.missioncontrol.ui.navbar.tools.model.WindAndAirspeedViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.plane.IAirplane;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindAndAirspeedEstimationView extends ViewBase<WindAndAirspeedViewModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindAndAirspeedEstimationView.class);

    @InjectViewModel
    private WindAndAirspeedViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private TableView<UavDataParameter<PositionData>> table;

    @FXML
    private TableColumn<UavDataParameter, String> parameterNameColumn;

    @FXML
    private TableColumn<UavDataParameter, String> parameterValueColumn;

    @FXML
    private Button btnReset;

    @FXML
    protected void onBtnResetAction() {
        IAirplane airplane = viewModel.getUav().getLegacyPlane();
        if (airplane != null) {
            airplane.getWindEstimate().reset();
        }
    }

    @Override
    public void initializeView() {
        super.initializeView();

        UavParameterTableViewHelper.prepareColumns(parameterNameColumn, parameterValueColumn);
        parameterNameColumn.setPrefWidth(ScaleHelper.emsToPixels(13.));
        parameterValueColumn.setPrefWidth(ScaleHelper.emsToPixels(22.));
        table.setItems(viewModel.getData());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public WindAndAirspeedViewModel getViewModel() {
        return viewModel;
    }

}
