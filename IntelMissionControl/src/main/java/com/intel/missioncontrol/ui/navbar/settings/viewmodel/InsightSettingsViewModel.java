/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.concurrent.CallerThread;
import com.intel.missioncontrol.concurrent.MethodAccess;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.sidepane.analysis.AnalysisSettings;
import de.saxsys.mvvmfx.utils.commands.AsyncCommand;
import de.saxsys.mvvmfx.utils.commands.DelegateAsyncCommand;
import javafx.beans.property.Property;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;

public class InsightSettingsViewModel extends ViewModelBase {

    private final UIAsyncBooleanProperty signedIn = new UIAsyncBooleanProperty(this);
    private final UIAsyncStringProperty userName = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty password = new UIAsyncStringProperty(this);

    private final AsyncCommand signInCommand;
    private final AsyncCommand signOutCommand;

    private final AnalysisSettings analysisSettings;

    @Inject
    public InsightSettingsViewModel(AnalysisSettings analysisSettings) {
        userName.setValue(analysisSettings.getInsightUsername());
        password.setValue(analysisSettings.getInsightPassword());
        PropertyHelper.setValueSafe(signedIn, analysisSettings.getInsightLoggedIn());
        signInCommand = new DelegateAsyncCommand(this::onSignIn, signedIn.not());
        signOutCommand = new DelegateAsyncCommand(this::onSignOut, signedIn);
        this.analysisSettings = analysisSettings;
    }

    public AsyncCommand getSignInCommand() {
        return signInCommand;
    }

    public AsyncCommand getSignOutCommand() {
        return signOutCommand;
    }

    public UIAsyncBooleanProperty signedInProperty() {
        return signedIn;
    }

    public Property<String> userNameProperty() {
        return userName;
    }

    public UIAsyncStringProperty passwordProperty() {
        return password;
    }

    @MethodAccess(CallerThread.BACKGROUND)
    private void onSignIn() {
        // Insight sign-in happens here...
        analysisSettings.insightUsernameProperty().set(userName.get());
        analysisSettings.insightPasswordProperty().set(password.get());
        PropertyHelper.setValueSafe(signedIn, true);
    }

    @MethodAccess(CallerThread.BACKGROUND)
    private void onSignOut() {
        analysisSettings.insightUsernameProperty().set("");
        analysisSettings.insightPasswordProperty().set("");
        PropertyHelper.setValueSafe(signedIn, false);
    }

}
