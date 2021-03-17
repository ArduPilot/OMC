/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.checklist;

import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChecklistWindowViewModel extends ViewModelBase {

    @InjectScope
    private ChecklistScope checklistScope;

    private ListProperty<ChecklistViewModel> checklists = new SimpleListProperty<>(FXCollections.observableArrayList());
    private IntegerProperty checkedCount = new SimpleIntegerProperty();
    private IntegerProperty totalCount = new SimpleIntegerProperty();

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        checklistScope.totalCountProperty().bind(totalCount);
        checklistScope.checkedCountProperty().bind(checkedCount);
        checklistScope
            .currentChecklistProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    checkedCount.unbind();

                    if (newValue != null) {
                        checklists.setValue(newValue.getValue());

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
                                checklists
                                    .stream()
                                    .map(ChecklistViewModel::checkedItemCountProperty)
                                    .toArray(Observable[]::new)));
                    }
                });
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
}
