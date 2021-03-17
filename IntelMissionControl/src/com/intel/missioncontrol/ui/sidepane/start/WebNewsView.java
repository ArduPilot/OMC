/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.networking.proxy.ProxyManager;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

public class WebNewsView extends ViewBase<WebNewsViewModel> {

    private static final int DEFAULT_VIEWPORT_HEIGHT = 200;

    @InjectViewModel
    private WebNewsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private Button hideButton;

    private WebView webView;

    @Inject
    private IPathProvider pathProvider;

    @Inject
    private ProxyManager proxyManager;

    @Inject
    private INetworkInformation networkInformation;

    private ChangeListener<Boolean> networkChangeListener =
        (object, oldVal, newVal) -> {
            if (newVal) {
                navigate(viewModel.uriProperty().getValue());
            }
        };

    @Override
    protected void initializeView() {
        super.initializeView();

        rootNode.managedProperty().bind(viewModel.showingProperty());
        rootNode.visibleProperty().bind(rootNode.managedProperty());

        webView = new WebView();
        AnchorPane.setBottomAnchor(webView, 0.0);
        AnchorPane.setLeftAnchor(webView, 0.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        AnchorPane.setTopAnchor(webView, 0.0);
        rootNode.getChildren().setAll(webView, rootNode.getChildren().get(0));
        webView.setContextMenuEnabled(false);

        WebEngine webEngine = webView.getEngine();
        webEngine.setUserDataDirectory(pathProvider.getWebviewCacheFolder().toFile());
        webEngine.setCreatePopupHandler(callback -> null);
        webEngine.setUserStyleSheetLocation(
            WebNewsView.class
                .getResource("/com/intel/missioncontrol/ui/sidepane/start/WebNewsView.css")
                .toExternalForm());

        webEngine
            .getLoadWorker()
            .stateProperty()
            .addListener(
                ((observable, oldValue, newValue) -> {
                    Document document = webEngine.getDocument();
                    if (document == null) {
                        return;
                    }

                    viewModel.documentProperty().setValue(document);
                    setViewportHeight(document);
                    setAnchorClickBehavior(document);
                }));

        viewModel.uriProperty().addListener(((observable, oldValue, newValue) -> navigate(newValue)));
        viewModel
            .shouldShowProperty()
            .addListener(((observable, oldValue, newValue) -> navigate(viewModel.uriProperty().getValue())));
        hideButton.setOnAction(event -> viewModel.getCloseCommand().execute());
        hideButton.disableProperty().bind(viewModel.getCloseCommand().notExecutableProperty());

        navigate(viewModel.uriProperty().getValue());

        // refreshing the news if network status changes to online
        networkInformation.networkAvailableProperty().addListener(new WeakChangeListener<>(networkChangeListener));

        // refreshing the news if proxy init finished
        proxyManager.proxyInitializedProperty().addListener(new WeakChangeListener<>(networkChangeListener));
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private void navigate(String uri) {
        if (uri != null && viewModel.shouldShowProperty().get()) {
            webView.getEngine().load(uri);
        }
    }

    private void setViewportHeight(Document document) {
        Element root = document.getDocumentElement();
        if (root == null) {
            return;
        }

        NodeList list = root.getElementsByTagName("head");
        if (list.getLength() <= 0) {
            return;
        }

        Node node = list.item(0);
        if (!(node instanceof Element)) {
            return;
        }

        Element headElement = (Element)node;
        String height = headElement.getAttribute("data-viewport-height");
        if (height != null && !height.isEmpty()) {
            try {
                int h = Integer.parseInt(height);
                webView.setPrefHeight(h);
                webView.setMinHeight(h);
                webView.setMaxHeight(h);
                return;
            } catch (RuntimeException ignored) {
            }
        }

        webView.setPrefHeight(DEFAULT_VIEWPORT_HEIGHT);
        webView.setMinHeight(DEFAULT_VIEWPORT_HEIGHT);
        webView.setMaxHeight(DEFAULT_VIEWPORT_HEIGHT);
    }

    private void setAnchorClickBehavior(Document document) {
        NodeList nodes = document.getElementsByTagName("a");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element)nodes.item(i);
            if (element.hasAttribute("data-internal-link")) {
                continue;
            }

            ((EventTarget)element)
                .addEventListener(
                    "click",
                    event -> {
                        EventTarget target = event.getCurrentTarget();
                        HTMLAnchorElement anchorElement = (HTMLAnchorElement)target;
                        String href = anchorElement.getHref();
                        event.preventDefault();
                        viewModel.getFollowLinkCommand().execute(href);
                    },
                    false);
        }
    }

}
