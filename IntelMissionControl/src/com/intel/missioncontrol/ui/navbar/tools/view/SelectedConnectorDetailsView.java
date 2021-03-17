/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.tools.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.tools.model.SelectedConnectorDetailsViewModel;
import com.intel.missioncontrol.ui.navbar.tools.model.UavDataParameter;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.desktop.gui.widgets.BrowserFrame;
import eu.mavinci.plane.IAirplane;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectedConnectorDetailsView extends ViewBase<SelectedConnectorDetailsViewModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectedConnectorDetailsView.class);

    @InjectViewModel
    private SelectedConnectorDetailsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private TableView<UavDataParameter<BackendState>> table;

    @FXML
    private TableColumn<UavDataParameter, String> parameterNameColumn;

    @FXML
    private TableColumn<UavDataParameter, String> parameterValueColumn;

    @FXML
    private Button btnChannel1;

    @FXML
    private Button btnChannel2;

    @FXML
    private Button btnRtkConfig;

    private final GeneralSettings generalSettings;

    @Inject
    public SelectedConnectorDetailsView(ISettingsManager settingsManager) {
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);
    }

    @FXML
    protected void onRtkConfigAction() {
        IAirplane plane = viewModel.getUav().getLegacyPlane();
        if (plane != null) {
            try {
                openWebDialog(plane.getAirplaneCache().getBackendState());
            } catch (AirplaneCacheEmptyException e) {
                LOGGER.debug("Unable to fetch the backend state object. Reason: the cache is empty");
            }
        }
    }

    public boolean openWebDialog(BackendState backendState) {
        if (backendState == null) {
            return false;
        }

        String url = backendState.getHTTPurl();
        BrowserFrame frame = new BrowserFrame(url);
        frame.widget.topBar.setVisible(generalSettings.getOperationLevel() == OperationLevel.DEBUG);
        frame.widget.statusBar.setVisible(generalSettings.getOperationLevel() == OperationLevel.DEBUG);
        return true;
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
    public SelectedConnectorDetailsViewModel getViewModel() {
        return viewModel;
    }

}
