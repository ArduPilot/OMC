/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

public class LiveVideoPaneView extends ViewBase<LiveVideoPaneViewModel> {

    private final IDialogContextProvider dialogContextProvider;
    private final ILanguageHelper languageHelper;

    @InjectViewModel
    private LiveVideoPaneViewModel viewModel;

    @FXML
    private Pane root;

    @FXML
    private ComboBox<IUILiveVideoStream> comboBoxStreams;

    @FXML
    private LiveVideoScreenView liveVideoScreenController;

    @FXML
    protected void selectPrevCam() {
        comboBoxStreams.getSelectionModel().selectPrevious();
    }

    @FXML
    protected void selectNextCam() {
        comboBoxStreams.getSelectionModel().selectNext();
    }

    @InjectContext
    private Context context;

    @Inject
    public LiveVideoPaneView(IDialogContextProvider dialogContextProvider, ILanguageHelper languageHelper) {
        this.dialogContextProvider = dialogContextProvider;
        this.languageHelper = languageHelper;
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    public ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);
        root.visibleProperty().bind(viewModel.videoPaneVisibleProperty());
        root.managedProperty().bind(viewModel.videoPaneVisibleProperty());

        StreamComboBoxHelper.setupComboBox(comboBoxStreams, languageHelper);
        StreamComboBoxHelper.bindBidirectional(comboBoxStreams.getSelectionModel(), viewModel.selectedStreamProperty());

        ((LiveVideoScreenViewModel)liveVideoScreenController.getViewModel())
            .streamProperty()
            .bind(viewModel.selectedStreamProperty());
    }
}
