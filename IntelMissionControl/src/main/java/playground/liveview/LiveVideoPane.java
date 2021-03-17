/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class LiveVideoPane extends ViewBase<LiveVideoPaneModel> {

    private final IDialogContextProvider dialogContextProvider;

    @InjectViewModel
    private LiveVideoPaneModel viewModel;

    @FXML
    private Pane root;

    @InjectContext
    private Context context;

    @Inject
    public LiveVideoPane(IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected Parent getRootNode() {
        return root;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);
        viewModel
            .liveVideoViewPropertyProperty()
            .addListener(
                (obs, oldValue, newValue) -> {
                    if (newValue != null) {
                        root.getChildren().add(newValue);
                        ((Button)newValue.lookup("#btnDetach")).setOnAction(actionEvent -> viewModel.requestDetach());
                    } else {
                        root.getChildren().clear();
                    }
                });

//        root.setMaxWidth(250);
//        root.setMaxHeight(Region.USE_PREF_SIZE);
    }
}
