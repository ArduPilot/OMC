/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.rtk.RtkSettings;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.connection.scope.RtkConnectionScope;
import com.intel.missioncontrol.ui.sidepane.flight.widget.ProgressButton;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.desktop.helper.FileFilter;
import eu.mavinci.desktop.helper.MFileFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** @author Vladimir Iordanov */
public class RtkConfigurationViewModel extends ViewModelBase {

    @Inject
    private IDialogService dialogService;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private ISettingsManager settingsManager;

    @Inject
    private IPathProvider pathProvider;

    @InjectScope
    private RtkConnectionScope rtkConnectionScope;

    private final BooleanProperty isSendingCanceled = new SimpleBooleanProperty();
    private final ListProperty<Pair<String, File>> rtkConfigs =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<Pair<String, File>> selectedConfig = new SimpleObjectProperty<>();
    private final ObjectProperty<ProgressButton.State> sendState =
        new SimpleObjectProperty<>(ProgressButton.State.PRE_PROGRESS);

    private Command browseCommand;
    private Command sendCommand;
    private Command sendCancelCommand;
    private Command advancedCommand;

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        initConfigList();

        browseCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            browse();
                        }
                    });

        sendCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            sendCurrentConfig();
                        }
                    });

        sendCancelCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            sendCancel();
                        }
                    });

        advancedCommand =
            new DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() throws Exception {
                            advanced();
                        }
                    });
    }

    private void browse() {
        String title =
            languageHelper.getString(
                "com.intel.missioncontrol.ui.connection.viewmodel.RtkConfigurationViewModel.fileSelector.title");
        Path selected = dialogService.requestFileOpenDialog(this, title, getRtkConfigFolder(), FileFilter.TXT);
        if (selected != null && Files.isRegularFile(selected)) {
            Pair<String, File> rtkConfig = addRtkConfig(selected.toFile());
            selectedConfig.set(rtkConfig);
        }
    }

    private void sendCurrentConfig() {
        if (selectedConfig.get() == null || rtkConnectionScope.getCurrentClient() == null) {
            return;
        }

        saveLastUsedConfig(getSelectedConfig());

        sendState.setValue(ProgressButton.State.IN_PROGRESS);

        Dispatcher.post(
            () -> {
                try {
                    rtkConnectionScope
                        .getCurrentClient()
                        .sendConfigFile(selectedConfig.get().second, isSendingCanceled::get);
                } finally {
                    Dispatcher.postToUI(
                        () -> {
                            sendState.setValue(ProgressButton.State.PRE_PROGRESS);
                            isSendingCanceled.set(false);
                        });
                }
            });
    }

    private void sendCancel() {
        isSendingCanceled.set(true);
    }

    private void advanced() {}

    private void initConfigList() {
        File rtkConfigFolder = getRtkConfigFolder().toFile();
        File[] rtkConfigFiles = rtkConfigFolder.listFiles(MFileFilter.txtFilter);
        Pair<String, File> lastUsedConfig = getLastUsedConfig();
        boolean lastConfigFound = false;
        if (rtkConfigFiles != null && rtkConfigFiles.length > 0) {
            for (File rtkConfig : rtkConfigFiles) {
                if (rtkConfig.isFile()) {
                    Pair<String, File> addedConfig = addRtkConfig(rtkConfig);
                    if (lastUsedConfig != null && lastUsedConfig.second.equals(rtkConfig)) {
                        selectedConfig.setValue(addedConfig);
                        lastConfigFound = true;
                    }
                }
            }

            if (selectedConfig.getValue() == null) {
                selectedConfig.setValue(rtkConfigs.get(0));
            }
        }
        // Case when the last used config was outside of configs folder
        if (lastUsedConfig != null && !lastConfigFound) {
            rtkConfigs.add(lastUsedConfig);
            selectedConfig.setValue(lastUsedConfig);
        }
    }

    private Pair<String, File> getLastUsedConfig() {
        RtkSettings rtkSettings = settingsManager.getSection(RtkSettings.class);
        if (rtkSettings.getLastUsedConfig() != null) {
            String lastUsedConfigStr = rtkSettings.getLastUsedConfig();
            File lastUsedConfigFile = new File(lastUsedConfigStr);
            if (lastUsedConfigFile.exists() && lastUsedConfigFile.isFile() && lastUsedConfigFile.length() > 0) {
                return createConfigItem(lastUsedConfigFile);
            }
        }

        return null;
    }

    private void saveLastUsedConfig(Pair<String, File> rtkConfig) {
        RtkSettings rtkSettings = settingsManager.getSection(RtkSettings.class);
        rtkSettings.lastUsedConfigProperty().set(rtkConfig.second.getPath());
    }

    private Pair<String, File> addRtkConfig(File rtkConfig) {
        Pair<String, File> duplicatedConfig =
            rtkConfigs
                .stream()
                .filter(stringFilePair -> stringFilePair.second.equals(rtkConfig))
                .findFirst()
                .orElse(null);
        if (duplicatedConfig != null) {
            return duplicatedConfig;
        }

        Pair<String, File> item = createConfigItem(rtkConfig);
        rtkConfigs.add(item);
        return item;
    }

    private Pair<String, File> createConfigItem(File rtkConfig) {
        String rtkConfigName = rtkConfig.getName();
        rtkConfigName = rtkConfigName.substring(0, rtkConfigName.indexOf("."));
        return new Pair<>(rtkConfigName, rtkConfig);
    }

    private Path getRtkConfigFolder() {
        return pathProvider.getExternalRtkConfigDirectory();
    }

    public ObservableList<Pair<String, File>> getRtkConfigs() {
        return rtkConfigs.get();
    }

    public ListProperty<Pair<String, File>> rtkConfigsProperty() {
        return rtkConfigs;
    }

    public Pair<String, File> getSelectedConfig() {
        return selectedConfig.get();
    }

    public Property<Pair<String, File>> selectedConfigProperty() {
        return selectedConfig;
    }

    public boolean getIsSendingCanceled() {
        return isSendingCanceled.get();
    }

    public BooleanProperty isSendingCanceledProperty() {
        return isSendingCanceled;
    }

    public Command getBrowseCommand() {
        return browseCommand;
    }

    public Command getSendCommand() {
        return sendCommand;
    }

    public Command getAdvancedCommand() {
        return advancedCommand;
    }

    public Command getSendCancelCommand() {
        return sendCancelCommand;
    }

    public ProgressButton.State getSendState() {
        return sendState.get();
    }

    public ObjectProperty<ProgressButton.State> sendStateProperty() {
        return sendState;
    }
}
