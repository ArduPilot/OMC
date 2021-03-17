/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.NtripConnections;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.connection.RtkConnectionSetupState;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.plane.protocol.Base64;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.AuthType;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;
import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripSourceStr;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.scene.input.MouseEvent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SetupNtripConnectionViewModel extends DialogViewModel {

    @InjectScope
    RtkConnectionScope rtkConnectionScope;

    private final StringProperty host = new SimpleStringProperty("");
    private final ObjectProperty<Integer> port = new SimpleObjectProperty<>(2101);
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final ObjectProperty<NtripSourceStr> stream = new SimpleObjectProperty<>();
    private final BooleanProperty https = new SimpleBooleanProperty();
    private final BooleanProperty okButtonDisable = new SimpleBooleanProperty(true);
    private final BooleanProperty refreshStreamEnable = new SimpleBooleanProperty(true);
    private final BooleanProperty streamRefreshInProgress = new SimpleBooleanProperty(false);
    private final ListProperty<NtripSourceStr> ntripStreams;
    private final NtripConnections connections;
    private final NtripRequester ntripRequester;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private Command refreshStreamsCommand;

    @Inject
    public SetupNtripConnectionViewModel(
            ISettingsManager settingsManager,
            NtripRequester ntripRequester,
            IDialogService dialogService,
            ILanguageHelper languageHelper) {
        this.connections = settingsManager.getSection(NtripConnections.class);
        this.ntripRequester = ntripRequester;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        ntripStreams = new SimpleListProperty<>(FXCollections.observableList(new CopyOnWriteArrayList<>()));

        okButtonDisableProperty().bind(streamProperty().isNull().or(streamRefreshInProgressProperty()));

        BooleanBinding hostFilled = hostProperty().isNotEmpty();
        BooleanBinding userFilled = usernameProperty().isNotEmpty();
        BooleanBinding passwordFilled = passwordProperty().isNotEmpty();
        refreshStreamEnableProperty()
            .bind(
                hostFilled
                    .and((userFilled.and(passwordFilled)).or(userFilled.not().and(passwordFilled.not())))
                    .and(streamRefreshInProgressProperty().not()));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        if (rtkConnectionScope.rtkNtripConnectionSetupStateProperty().get() == RtkConnectionSetupState.EDIT) {
            NtripConnectionSettings settings = rtkConnectionScope.selectedNtripConnectionProperty().get();
            hostProperty().set(settings.getHost());
            portProperty().set(settings.getPort());
            usernameProperty().set(settings.getUser());
            passwordProperty().set(Base64.decodeString(settings.getPassword()));
            tryStreamRequest();
        }
    }

    public Command getRefreshStreamsCommand() {
        if (refreshStreamsCommand == null) {
            refreshStreamsCommand = getRefreshStreamsCommand(true);
        }

        return refreshStreamsCommand;
    }

    DelegateCommand getRefreshStreamsCommand(boolean asynchronous) {
        DelegateCommand refreshStreamsCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            tryStreamRequest();
                        }
                    },
                asynchronous);
        ReadOnlyObjectProperty<Worker.State> refreshState = refreshStreamsCommand.stateProperty();
        streamRefreshInProgressProperty()
            .bind(refreshState.isEqualTo(Worker.State.SCHEDULED).or(refreshState.isEqualTo(Worker.State.RUNNING)));
        refreshStreamsCommand.setOnScheduled(e -> streamProperty().set(null));
        return refreshStreamsCommand;
    }

    private void tryStreamRequest() {
        URL url = buildUrl();
        List<NtripSourceStr> collect =
            ntripRequester
                .requestNtripStreams(url)
                .filter(NtripSourceStr.class::isInstance)
                .map(NtripSourceStr.class::cast)
                .collect(Collectors.toList());
        ntripStreams.clear();
        ntripStreams.addAll(collect);
    }

    private URL buildUrl() {
        try {
            return new URL(getUrlFormat());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUrlFormat() {
        String userInfo = getUserInfo();
        String host = hostProperty().get();
        int port = portProperty().get();
        boolean https = httpsProperty().get();
        String protocol = https ? "https" : "http";
        return String.format("%s://%s%s:%d/", protocol, userInfo, host, port);
    }

    private String getUserInfo() {
        return credentialsNotProvided()
            ? ""
            : String.format("%s:%s", usernameProperty().get(), passwordProperty().get()) + "@";
    }

    public void handleCreate(MouseEvent event) {
        if (isAuthenticationRequired() && credentialsNotProvided()) {
            dialogService.showWarningMessage(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.connection.view.NtripConnectionView.credentialsTitle"),
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.connection.view.NtripConnectionView.credentialsMessage"));
            return;
        }

        NtripConnectionSettings settings = buildConnectionSettings();
        if (connections.contains(settings) && isConnectionSettingRewriteConfirmed()) {
            applyNewConnectionSettings(settings);
        } else if (!connections.contains(settings)) {
            connections.add(settings);
            applyNewConnectionSettings(settings);
        }
    }

    private NtripConnectionSettings buildConnectionSettings() {
        return NtripConnectionSettings.builder()
            .withHost(host.get())
            .withPort(port.get())
            .withUser(username.get())
            .withPassword(password.get())
            .withStream(stream.get())
            .build();
    }

    private boolean credentialsNotProvided() {
        return usernameProperty().get().isEmpty() || passwordProperty().get().isEmpty();
    }

    private boolean isAuthenticationRequired() {
        return streamProperty().get().getAuthType() != AuthType.NONE;
    }

    private void applyNewConnectionSettings(NtripConnectionSettings settings) {
        rtkConnectionScope.selectedNtripConnectionProperty().set(settings);
        getCloseCommand().execute();
    }

    private boolean isConnectionSettingRewriteConfirmed() {
        return dialogService.requestConfirmation(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.connection.view.NtripConnectionView.ntripConnectionExistTitle"),
            languageHelper.getString(
                "com.intel.missioncontrol.ui.connection.view.NtripConnectionView.ntripConnectionExistMessage"));
    }

    public StringProperty hostProperty() {
        return host;
    }

    public ObjectProperty<Integer> portProperty() {
        return port;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public ObjectProperty<NtripSourceStr> streamProperty() {
        return stream;
    }

    public BooleanProperty httpsProperty() {
        return https;
    }

    public ListProperty<NtripSourceStr> ntripStreamsProperty() {
        return ntripStreams;
    }

    public ObjectProperty<RtkConnectionSetupState> rtkNtripConnectionSetupStateProperty() {
        return rtkConnectionScope.rtkNtripConnectionSetupStateProperty();
    }

    public ObjectProperty<NtripConnectionSettings> selectedConnectionProperty() {
        return rtkConnectionScope.selectedNtripConnectionProperty();
    }

    public void handleDelete(MouseEvent event) {
        if (getConnectionDeleteConfirmation()) {
            connections.remove(selectedConnectionProperty().get());
            applyNewConnectionSettings(null);
        }
    }

    public void handleEdit(MouseEvent event) {
        connections.remove(selectedConnectionProperty().get());
        NtripConnectionSettings settings = buildConnectionSettings();
        connections.add(settings);
        applyNewConnectionSettings(settings);
    }

    public BooleanProperty okButtonDisableProperty() {
        return okButtonDisable;
    }

    public BooleanProperty refreshStreamEnableProperty() {
        return refreshStreamEnable;
    }

    public BooleanProperty streamRefreshInProgressProperty() {
        return streamRefreshInProgress;
    }

    private boolean getConnectionDeleteConfirmation() {
        return dialogService.requestConfirmation(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.connection.view.NtripConnectionView.connection.delete.confirmation.title"),
            languageHelper.getString(
                "com.intel.missioncontrol.ui.connection.view.NtripConnectionView.connection.delete.confirmation.message"));
    }
}
