/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTKConfigurationView extends DialogView<RTKConfigurationViewModel> {

    private final static Logger LOGGER = LoggerFactory.getLogger(RTKConfigurationView.class);

    private final StringProperty title = new SimpleStringProperty();

    @FXML
    public Pane rootNode;

    @FXML
    private WebView webView;

    @InjectViewModel
    private RTKConfigurationViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    public void initializeView() {
        super.initializeView();
        installCertificateVerificationBypassTools();
        title.bind(Bindings.createStringBinding(() -> languageHelper.getString(RTKConfigurationView.class, "title")));
        initializeWebView();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    private void initializeWebView() {
        AnchorPane.setBottomAnchor(webView, 0.0);
        AnchorPane.setLeftAnchor(webView, 0.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        AnchorPane.setTopAnchor(webView, 0.0);

        // Disable the context menu
        webView.setContextMenuEnabled(false);
        // Increase the text font size by 20%
        webView.setFontScale(1.20);
        // Set the Zoom 20%
        webView.setZoom(1.20);
        // Set font smoothing type to GRAY
        webView.setFontSmoothingType(FontSmoothingType.GRAY);
        webView.setContextMenuEnabled(false);

        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.load("https://launchbox.internal/imc/rtk-setup");
    }

    private void installCertificateVerificationBypassTools() {
        this.installCustomTrustManager();
        this.installCustomHostNameVerifier();
    }

    private void installCustomTrustManager() {
        try {
            TrustManager[] nonDiscriminantTrustManager =
                new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // ignore client trust check
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // ignore server trust check
                        }
                    }
                };
            final SSLContext ret = SSLContext.getInstance("SSL");
            ret.init(null, nonDiscriminantTrustManager, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(ret.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            LOGGER.warn("Cannot install trust manager.", ex);
        }
    }

    private void installCustomHostNameVerifier() {
        HostnameVerifier hv = (string, ssls) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

}
