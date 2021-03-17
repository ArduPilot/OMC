/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.RtkBasePosition;
import com.intel.missioncontrol.settings.RtkBasePositionsSettings;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserViewModel;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class RTKBasePositionsSettingsViewModel extends ViewModelBase {

    private final RtkBasePositionsSettings rtkSettings;
    private final ILanguageHelper languageHelper;
    private final IDialogService dialogService;
    private final ISrsManager srsManager;

    private final ObjectProperty<MSpatialReference> ref = new SimpleObjectProperty<>();

    private final ListProperty<RtkBasePosition> rtkBasePositions =
        new SimpleListProperty(FXCollections.observableArrayList());

    @Inject
    public RTKBasePositionsSettingsViewModel(
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            ISrsManager srsManager) {
        this.rtkSettings = settingsManager.getSection(RtkBasePositionsSettings.class);
        this.srsManager = srsManager;
        this.languageHelper = languageHelper;
        this.dialogService = dialogService;

        // if we would use normal binding here, things would break, since the entire UI is updated all the time if some
        // inner value is changing within one entry of the list due to value extractors on the other end of this binding
        rtkBasePositions.bindContent(rtkSettings.rtkBasePositionsProperty());
    }

    public ReadOnlyListProperty<RtkBasePosition> settingsListProperty() {
        return rtkBasePositions;
    }

    public void addSetting() {
        final int numberOfSettings = rtkSettings.rtkBasePositionsProperty().get().size();
        final String settingRowName =
            String.format(languageHelper.getString("RTKBasePositionsSettingsView.rowName"), numberOfSettings);
        rtkSettings.rtkBasePositionsProperty().add(new RtkBasePosition(settingRowName));
    }

    public void deleteSetting(int index) {
        rtkSettings.rtkBasePositionsProperty().remove(index);
    }

    public void onSrsLinkClicked(StringProperty property) {
        MSpatialReference mRef = srsManager.getSrsByIdOrDefault(property.get());
        ref.set(mRef);

        dialogService.requestDialogAndWait(this, SpatialReferenceChooserViewModel.class, () -> ref).getDialogResult();
        property.setValue(ref.get().id);
    }

}
