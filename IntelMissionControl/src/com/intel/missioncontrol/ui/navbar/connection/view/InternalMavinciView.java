/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.InternalMavinciViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** @author Vladimir Iordanov */
public class InternalMavinciView extends ViewBase<InternalMavinciViewModel> {

    public static final String WEB_DIALOG_TITLE =
        "com.intel.missioncontrol.ui.connection.view.InternalMavinciView.browserSectiontitle";
    public static final double WEB_DIALOG_WIDTH = ScaleHelper.emsToPixels(100);
    public static final double WEB_DIALOG_HEIGHT = ScaleHelper.emsToPixels(60);

    @FXML
    private Pane rootNode;

    @FXML
    private Label loadingIndicator;

    @FXML
    private Label connectionErrorPlaceholder;

    @FXML
    private Hyperlink openPageLink;

    @FXML
    private VBox internalWebView;

    @InjectViewModel
    private InternalMavinciViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private IPathProvider pathProvider;

    private WebView browser;
    private Stage webDialog;

    @Override
    public void initializeView() {
        super.initializeView();
        browser = new WebView();
        viewModel
            .isFailedConnectionProperty()
            .bind(browser.getEngine().getLoadWorker().stateProperty().isEqualTo(Worker.State.FAILED));
        openPageLink
            .visibleProperty()
            .bind(
                browser.getEngine()
                    .getLoadWorker()
                    .runningProperty()
                    .not()
                    .and(viewModel.isFailedConnectionProperty().not()));
        connectionErrorPlaceholder.visibleProperty().bind(viewModel.isFailedConnectionProperty());
        loadingIndicator.visibleProperty().bind(browser.getEngine().getLoadWorker().runningProperty());

        viewModel
            .currentUrlProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    WebEngine webEngine = browser.getEngine();
                    webEngine.setUserDataDirectory(pathProvider.getWebviewCacheFolder().toFile());
                    webEngine.load(newValue);
                });

        internalWebView.visibleProperty().bind(viewModel.isConnectedBinding());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public InternalMavinciViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    private void openPage() {
        if (webDialog == null) {
            webDialog = new Stage();
            webDialog.setTitle(languageHelper.getString(WEB_DIALOG_TITLE));
            webDialog.initStyle(StageStyle.UTILITY);
            webDialog.initModality(Modality.APPLICATION_MODAL);
            webDialog.initOwner(WindowHelper.getPrimaryStage());
            webDialog.setScene(new Scene(browser, WEB_DIALOG_WIDTH, WEB_DIALOG_HEIGHT));
        }

        webDialog.show();
    }
}
