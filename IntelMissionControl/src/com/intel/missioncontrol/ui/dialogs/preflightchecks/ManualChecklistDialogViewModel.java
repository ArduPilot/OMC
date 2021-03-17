/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.ui.common.CheckListUtils;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.UnmannedAerialVehicle;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.Checklist;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.ChecklistItem;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.ChecklistScope;
import com.intel.missioncontrol.ui.sidepane.flight.checklist.ChecklistViewModel;
import de.saxsys.mvvmfx.InjectScope;
import eu.mavinci.core.plane.AirplaneType;
import java.util.HashMap;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ManualChecklistDialogViewModel extends DialogViewModel {

    @InjectScope
    private ChecklistScope checklistScope;

    @InjectScope
    private UavConnectionScope uavConnectionScope;

    private ListProperty<ChecklistViewModel> checklists = new SimpleListProperty<>(FXCollections.observableArrayList());
    private IntegerProperty checkedCount = new SimpleIntegerProperty();
    private IntegerProperty totalCount = new SimpleIntegerProperty();

    private LanguageHelper languageHelper;
    private HashMap<AirplaneType, ListProperty<ChecklistViewModel>> planeChecklist = new HashMap<>();
    private AirplaneType currentAirplaneType;

    @Inject
    public ManualChecklistDialogViewModel(LanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        initPlaneChecklists();
        UnmannedAerialVehicle selectedUav = uavConnectionScope.selectedUavProperty().getValue();
        if (selectedUav != null) {
            currentAirplaneType = selectedUav.model;
        }

        checklistScope.currentChecklistProperty().setValue(null);
        if (planeChecklist.containsKey(currentAirplaneType)) {
            checklistScope.currentChecklistProperty().setValue(planeChecklist.get(currentAirplaneType));
        }

        checklists.bind(checklistScope.currentChecklistProperty());
        int tCount = 0;
        for (ChecklistViewModel checklist : checklists) {
            tCount += checklist.getTotalItemCount();
        }

        totalCount.set(tCount);

        checkedCount.bind(
            Bindings.createIntegerBinding(
                () -> {
                    int count = 0;
                    for (ChecklistViewModel checklist : checklists) {
                        count += checklist.getCheckedItemCount();
                    }

                    return count;
                },
                checklists.stream().map(ChecklistViewModel::checkedItemCountProperty).toArray(Observable[]::new)));

        checklistScope.totalCountProperty().bind(totalCount);
        checklistScope.checkedCountProperty().bind(checkedCount);
    }

    public ReadOnlyListProperty<ChecklistViewModel> checklistsProperty() {
        return checklists;
    }

    public ObservableList<ChecklistViewModel> getChecklists() {
        return checklists.get();
    }

    public IntegerProperty checkedCountProperty() {
        return checkedCount;
    }

    public int getCheckedCount() {
        return checkedCount.get();
    }

    private void initPlaneChecklists() {
        Checklist[] checklistItems = CheckListUtils.readAllCheckLists();

        if (checklistItems == null) {
            return;
        }

        for (Checklist checklist : checklistItems) {
            ListProperty<ChecklistViewModel> checklists = new SimpleListProperty<>(FXCollections.observableArrayList());
            for (ChecklistItem item : checklist.getChecklistItem()) {
                fillTextByKeys(item);
                checklists.add(new ChecklistViewModel(item));
            }

            planeChecklist.put(checklist.getAirplaneType(), checklists);
        }
    }

    private void fillTextByKeys(ChecklistItem item) {
        item.setTitle(languageHelper.getString(item.getTitle()));
        for (int i = 0; i < item.getItems().length; i++) {
            item.getItems()[i] = languageHelper.getString(item.getItems()[i]);
        }
    }
}
