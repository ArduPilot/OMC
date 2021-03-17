/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checks.preflight;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.common.CheckListUtils;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.Checklist;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistItem;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistScope;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checklist.ChecklistViewModel;
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
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class PreflightChecklistDialogViewModel extends DialogViewModel {

    @InjectScope
    private FlightScope flightScope;

    @InjectScope
    private ChecklistScope checklistScope;

    private final ListProperty<ChecklistViewModel> checklists =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final IntegerProperty checkedCount = new SimpleIntegerProperty();
    private final IntegerProperty totalCount = new SimpleIntegerProperty();
    private final HashMap<AirplaneType, ListProperty<ChecklistViewModel>> planeChecklist = new HashMap<>();
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final ReadOnlyAsyncObjectProperty<IHardwareConfiguration> hardwareConfiguration;
    private final ILanguageHelper languageHelper;

    @Inject
    public PreflightChecklistDialogViewModel(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
        hardwareConfiguration =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::hardwareConfigurationProperty);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        drone.bind(flightScope.currentDroneProperty());

        initPlaneChecklists();

        // TODO: fix this
        hardwareConfiguration.addListener(
            (observable, oldValue, newValue) -> {
                checklistScope.currentChecklistProperty().setValue(null);

                if (newValue != null) {
                    AirplaneType currentAirplaneType = newValue.getPlatformDescription().getAirplaneType();

                    if (planeChecklist.containsKey(currentAirplaneType)) {
                        checklistScope.currentChecklistProperty().setValue(planeChecklist.get(currentAirplaneType));
                    }
                }
            });

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
