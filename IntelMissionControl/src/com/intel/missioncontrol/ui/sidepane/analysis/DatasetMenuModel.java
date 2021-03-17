/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.Localizable;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.common.components.RenameDialog;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ListChangeListener;

/**
 * Contains the menu model for the datasets drop-down menu located in the sidepane header on the data preview tab. This
 * class also contains business logic for the menu commands.
 */
public class DatasetMenuModel extends MenuModel {

    private static final Logger LOG = Logger.getLogger(DatasetMenuModel.class.getSimpleName());

    @Localizable
    public enum MenuIds implements IKeepAll {
        CAPTION,
        DATASETS,
        NEW_DATASET,
        RENAME_DATASET,
        OPEN
    }

    private final IApplicationContext applicationContext;
    private final IHardwareConfigurationManager hardwareConfigurationManager;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final ObjectProperty<Matching> currentMatching;
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    public DatasetMenuModel(
            IApplicationContext applicationContext,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IDialogService dialogService,
            ILanguageHelper languageHelper) {
        super(MenuIds.CAPTION, false, null, null, null, null);

        this.applicationContext = applicationContext;
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;

        currentMatching =
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectObject(Mission::currentMatchingProperty);

        setMnemonicParsing(false);

        getChildren()
            .addAll(
                MenuModel.checkGroup(MenuIds.DATASETS),
                MenuModel.group(
                    MenuModel.item(MenuIds.NEW_DATASET, languageHelper.toFriendlyName(MenuIds.NEW_DATASET)),
                    MenuModel.item(MenuIds.OPEN, languageHelper.toFriendlyName(MenuIds.OPEN))),
                MenuModel.item(MenuIds.RENAME_DATASET, languageHelper.toFriendlyName(MenuIds.RENAME_DATASET)));

        find(MenuIds.DATASETS).<Matching>checkedItemProperty().bindBidirectional(currentMatching);

        find(MenuIds.NEW_DATASET)
            .setCommandHandler(
                this::createNewDataset,
                PropertyPath.from(currentMatching)
                    .selectReadOnlyObject(Matching::statusProperty)
                    .isEqualTo(MatchingStatus.IMPORTED));

        find(MenuIds.RENAME_DATASET)
            .setCommandHandler(
                this::renameCurrentDataset,
                PropertyPath.from(currentMatching)
                    .selectReadOnlyObject(Matching::statusProperty)
                    .isEqualTo(MatchingStatus.IMPORTED));

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyList(Mission::matchingsProperty)
            .addListener((ListChangeListener<Matching>)change -> datasetsChanged(change.getList()));

        textProperty()
            .bind(valueOrDefault(propertyPathStore.from(currentMatching).selectReadOnlyString(Matching::nameProperty)));
    }

    private StringBinding valueOrDefault(ReadOnlyStringProperty property) {
        return Bindings.createStringBinding(
            () -> {
                if (property.get() == null) {
                    return languageHelper.toFriendlyName(MenuIds.NEW_DATASET);
                }

                return property.get();
            },
            property);
    }

    private void datasetsChanged(List<? extends Matching> datasets) {
        List<MenuModel> newMenuItems = new ArrayList<>();
        for (Matching dataset : datasets) {
            MenuModel item = MenuModel.checkItem();
            item.setMnemonicParsing(false);
            item.textProperty().bind(valueOrDefault(dataset.nameProperty()));
            item.setUserData(dataset);
            newMenuItems.add(item);
        }

        MenuModel model = find(MenuIds.DATASETS);
        model.getChildren().setAll(newMenuItems);
    }

    private void createNewDataset() {
        Matching matching =
            new Matching(languageHelper.toFriendlyName(MenuIds.NEW_DATASET), hardwareConfigurationManager);
        applicationContext.getCurrentMission().matchingsProperty().add(matching);
        currentMatching.set(matching);
    }

    private void renameCurrentDataset() {
        String oldName = currentMatching.get().getFolderName();
        if (oldName == null) {
            dialogService.showWarningMessage(
                languageHelper.getString("com.intel.missioncontrol.ui.analysis.AnalysisView.renameDatasetError.title"),
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.analysis.AnalysisView.renameDatasetError.message"));
            return;
        }

        String newName =
            RenameDialog.requestNewMissionName(
                    languageHelper.getString("com.intel.missioncontrol.ui.analysis.AnalysisViewModel.renamePopupTitle"),
                    languageHelper.getString("com.intel.missioncontrol.ui.analysis.AnalysisViewModel.renameName"),
                    oldName,
                    languageHelper,
                    (s) -> s != null && !s.isEmpty())
                .orElse(oldName);

        try {
            currentMatching.get().rename(newName);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "can't rename dataset", e);
            applicationContext.addToast(
                Toast.of(ToastType.ALERT)
                    .setText(
                        languageHelper.getString("com.intel.missioncontrol.ui.analysis.AnalysisViewModel.renameFailed"))
                    .create());
        }
    }

}
