/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.helper.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class AoiGeneralTabViewModel extends ViewModelBase<AreaOfInterest> {

    @InjectScope
    private PlanningScope planningScope;

    private final IntegerProperty aoiOrder = new SimpleIntegerProperty();
    private final IntegerProperty aoiCount = new SimpleIntegerProperty();

    private BiConsumer<Integer, Integer> aoiOrderChangedConsumer;
    private AreaOfInterest areaOfInterest;

    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final Command browseModelCommand;

    @Inject
    public AoiGeneralTabViewModel(
            IDialogService dialogService,
            ILanguageHelper languageHelper) {
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        browseModelCommand = new DelegateCommand(this::browseModel);
    }

    @Override
    public void initializeViewModel(AreaOfInterest aoi) {
        super.initializeViewModel(aoi);
        this.areaOfInterest = aoi;
        aoiCount.bind(planningScope.aoiCountProperty());

        int indexOfAoi = planningScope.indexOfAoi(aoi);
        if (indexOfAoi < 0) {
            return;
        }

        aoiOrder.setValue(indexOfAoi + 1);
        aoiOrder.addListener(
            (observable, oldValue, newValue) -> {
                planningScope.changeAoiPosition(oldValue.intValue() - 1, newValue.intValue() - 1);
                if (aoiOrderChangedConsumer != null) {
                    aoiOrderChangedConsumer.accept(oldValue.intValue() - 1, newValue.intValue() - 1);
                }
            });
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

    public IntegerProperty aoiOrderProperty() {
        return aoiOrder;
    }

    public IntegerProperty aoiCountProperty() {
        return aoiCount;
    }

    public Command getBrowseModelCommand() {
        return browseModelCommand;
    }

    public void setAoiOrderChangedConsumer(BiConsumer<Integer, Integer> aoiOrderChangedConsumer) {
        this.aoiOrderChangedConsumer = aoiOrderChangedConsumer;
    }

    private void browseModel() {
        String pathStr = areaOfInterest.modelFilePathProperty().get();
        Path p = null;
        if (!StringHelper.isNullOrEmpty(pathStr)) {
            p = Paths.get(pathStr);
            if (!Files.exists(p)) {
                p = null;
            } else if (Files.isRegularFile(p)) {
                p = p.getParent();
            }
        }

        var res =
            dialogService.requestFileOpenDialog(
                this,
                languageHelper.getString("com.intel.missioncontrol.ui.planning.aoi.AoiGeneralTabView.selectFile"),
                p,
                areaOfInterest.typeProperty().get() == PlanType.COPTER3D ? FileFilter.OBJ : FileFilter.CSV);
        if (res != null) {
            areaOfInterest.modelFilePathProperty().setValue(res.toString());
        }
    }
}
