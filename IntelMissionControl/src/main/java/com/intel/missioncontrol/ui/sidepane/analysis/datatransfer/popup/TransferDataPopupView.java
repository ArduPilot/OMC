/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.datatransfer.popup;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;

/** The Transfer Data popup window */
public class TransferDataPopupView extends FancyTabView<TransferDataPopupViewModel> {

    @InjectViewModel
    private TransferDataPopupViewModel viewModel;

    // @FXML Hiding this part of layout per customer request
    private CheckBox popupDisabledCheckBox;

    @FXML
    private StackPane detailedInstructions;

    @FXML
    private Node defaultInstructions;

    @FXML
    private Button downloadSampleDataset;

    @Inject
    private IDialogService dialogService;

    @Inject
    private ILanguageHelper languageHelper;

    public void initializeView() {

        super.initializeView();
        // popupDisabledCheckBox.selectedProperty().bindBidirectional(
        //        viewModel.dataTransferPopupEnabledProperty());

        viewModel
            .currentUavProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    planeChanged(viewModel.getUavTypeName());
                });
        // following call should bind mission and current UAV property.
        // That will define the right information content
        viewModel.getUavTypeName();
        viewModel.setExecutingProperty(false);
        downloadSampleDataset.disableProperty().bind(viewModel.isExecutingProperty());
    }

    private void planeChanged(String uavTypeName) {
        Node instructionsNode =
            detailedInstructions
                .getChildren()
                .stream()
                .peek(node -> node.setVisible(false))
                .filter(node -> uavTypeName.equals(node.getUserData()))
                .findFirst()
                .orElse(defaultInstructions);
        instructionsNode.setVisible(true);
    }

    @FXML
    public void closePopup() {
        viewModel.closeMe();
    }

    public void downloadSampleDatasets() {
        viewModel.getSampleMatchings();
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }
}
