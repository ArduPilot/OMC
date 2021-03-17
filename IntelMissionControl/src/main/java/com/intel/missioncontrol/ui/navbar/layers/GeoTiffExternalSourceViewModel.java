/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.geotiff.GeoTiffEntry;
import com.intel.missioncontrol.map.geotiff.IGeoTiffManager;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import eu.mavinci.desktop.helper.FileFilter;
import java.nio.file.Path;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GeoTiffExternalSourceViewModel extends DialogViewModel {

    private final ListProperty<GeoTiffEntry> geoTiffFiles =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<GeoTiffEntry> selectedGeoTiff = new SimpleObjectProperty<>();
    private final ParameterizedCommand<GeoTiffEntry> unloadCommand;
    private final ParameterizedCommand<GeoTiffEntry> autoDetectManualOffsetCommand;
    private final Command addGeotiffCommand = new DelegateCommand(this::addGeotiffFile);

    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final IPathProvider pathProvider;
    private final IGeoTiffManager geoTiffHelper;

    @Inject
    public GeoTiffExternalSourceViewModel(
            IGeoTiffManager geoTiffHelper,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IPathProvider pathProvider) {
        this.pathProvider = pathProvider;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.geoTiffHelper = geoTiffHelper;
        geoTiffFiles.set(geoTiffHelper.geoTiffEntriesProperty());

        unloadCommand = new ParameterizedDelegateCommand<>(this::unloadGeoTiff);
        autoDetectManualOffsetCommand = new ParameterizedDelegateCommand<>(this::autoDetectManualOffset);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        selectedGeoTiff.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.zoomToItem();
                }
            });
    }

    public ObservableValue<? extends ObservableList<GeoTiffEntry>> geoTiffFilesProperty() {
        return geoTiffFiles;
    }

    public ObjectProperty<GeoTiffEntry> selectedGeoTiffProperty() {
        return selectedGeoTiff;
    }

    public ParameterizedCommand<GeoTiffEntry> getUnloadCommand() {
        return unloadCommand;
    }

    public ParameterizedCommand<GeoTiffEntry> getAutoDetectManualOffsetCommand() {
        return autoDetectManualOffsetCommand;
    }

    public Command getAddGeotiffCommand() {
        return addGeotiffCommand;
    }

    private void addGeotiffFile() {
        Path[] files =
            dialogService.requestMultiFileOpenDialog(
                this,
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.layers.ManageExternalSourceView.selectExternalSource"),
                this,
                pathProvider.getUserHomeDirectory(),
                FileFilter.GEO_TIFF);

        for (Path file : files) {
            geoTiffHelper.addGeoTiff(file.toFile());
        }
    }

    private void unloadGeoTiff(GeoTiffEntry geoTiffEntry) {
        geoTiffHelper.dropGeotiffImport(geoTiffEntry);
    }

    private void autoDetectManualOffset(GeoTiffEntry item) {
        item.autoDetectManualOffset();
    }

}
