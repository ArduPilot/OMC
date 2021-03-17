/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.intel.missioncontrol.ui.RootView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

public class LiveviewView extends RootView<LiveviewViewModel> {

    @FXML
    private Pane root;

    @FXML
    private Button btnDetach;

    @FXML
    private CheckBox chkVisible;

    // NOTE: we need this reference here, otherwise LiveVideoPane would get gc'd
    @FXML
    @SuppressWarnings("unused")
    private LiveVideoPane liveVideoPaneController;

    @FXML
    protected void handleDetach(ActionEvent event) {
        viewModel.handleDetach();
    }

    @FXML
    protected void tryGC(ActionEvent event) {
        System.gc();
    }

    @InjectViewModel
    private LiveviewViewModel viewModel;

    @Override
    protected void initializeView() {
        super.initializeView();
        btnDetach.visibleProperty().bind(btnDetach.getParent().hoverProperty());
        chkVisible.disableProperty().bind(viewModel.canMakeVisibleProperty().not());
        chkVisible.selectedProperty().bindBidirectional(viewModel.liveVideoVisibleProperty());
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }
}
