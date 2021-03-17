/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ActivityButton;
import com.intel.missioncontrol.ui.controls.RichTextLabel;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.InsightSettingsViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class InsightSettingsView extends ViewBase<InsightSettingsViewModel> {

    private static final String SR_NOT_SIGNED_IN = InsightSettingsView.class.getName() + ".notSignedIn";
    private static final String SR_SIGNED_IN_AS = InsightSettingsView.class.getName() + ".signedInAs";

    @InjectViewModel
    private InsightSettingsViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private RichTextLabel signedInStatusLabel;

    @FXML
    private VBox usernameBox;

    @FXML
    private VBox passwordBox;

    @FXML
    private TextField userNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ActivityButton signInButton;

    @FXML
    private ActivityButton signOutButton;

    private final ILanguageHelper languageHelper;

    @Inject
    public InsightSettingsView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        signedInStatusLabel
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        if (viewModel.signedInProperty().get()) {
                            return languageHelper.getString(
                                SR_SIGNED_IN_AS, RichTextLabel.bold(viewModel.userNameProperty().getValue()));
                        }

                        return languageHelper.getString(SR_NOT_SIGNED_IN);
                    },
                    viewModel.signedInProperty(),
                    viewModel.userNameProperty()));

        ObservableBooleanValue signedIn = viewModel.signedInProperty();
        ObservableBooleanValue notSignedIn = viewModel.signedInProperty().not();
        ObservableBooleanValue currentlyExecuting =
            viewModel.getSignInCommand().runningProperty().or(viewModel.getSignOutCommand().runningProperty());

        usernameBox.visibleProperty().bind(notSignedIn);
        usernameBox.managedProperty().bind(notSignedIn);
        passwordBox.visibleProperty().bind(notSignedIn);
        passwordBox.managedProperty().bind(notSignedIn);

        userNameField.textProperty().bindBidirectional(viewModel.userNameProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        userNameField.disableProperty().bind(currentlyExecuting);
        passwordField.disableProperty().bind(currentlyExecuting);

        signInButton.visibleProperty().bind(notSignedIn);
        signInButton.managedProperty().bind(notSignedIn);
        signInButton.disableProperty().bind(currentlyExecuting);
        signInButton.isBusyProperty().bind(currentlyExecuting);
        signInButton.setOnAction(event -> viewModel.getSignInCommand().executeAsync());

        signOutButton.visibleProperty().bind(signedIn);
        signOutButton.managedProperty().bind(signedIn);
        signOutButton.disableProperty().bind(currentlyExecuting);
        signOutButton.isBusyProperty().bind(currentlyExecuting);
        signOutButton.setOnAction(event -> viewModel.getSignOutCommand().executeAsync());
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

}
