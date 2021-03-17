/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.Localizable;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.common.components.RenameDialog;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.menu.MenuModel;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ListChangeListener;

/**
 * Contains the menu model for the flight plan drop-down menu located in the sidepane header on the planning tab. This
 * class also contains business logic for the menu commands.
 */
public class FlightPlanMenuModel extends MenuModel {

    @Localizable
    public enum MenuIds implements IKeepAll {
        FLIGHTPLANS,
        NEW_FLIGHTPLAN,
        RENAME_FLIGHTPLAN,
        OPEN
    }

    private final IApplicationContext applicationContext;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final IFlightPlanService flightPlanService;
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    public FlightPlanMenuModel(
            IApplicationContext applicationContext,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IFlightPlanService flightPlanService) {
        super(null, false, null, null, null, null);

        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.flightPlanService = flightPlanService;

        setMnemonicParsing(false);

        getChildren()
            .addAll(
                MenuModel.checkGroup(MenuIds.FLIGHTPLANS),
                MenuModel.group(
                    MenuModel.item(MenuIds.NEW_FLIGHTPLAN, languageHelper.toFriendlyName(MenuIds.NEW_FLIGHTPLAN)),
                    MenuModel.item(MenuIds.OPEN, languageHelper.toFriendlyName(MenuIds.OPEN))),
                MenuModel.item(MenuIds.RENAME_FLIGHTPLAN, languageHelper.toFriendlyName(MenuIds.RENAME_FLIGHTPLAN)));

        find(MenuIds.FLIGHTPLANS)
            .<FlightPlan>checkedItemProperty()
            .bindBidirectional(
                propertyPathStore
                    .from(applicationContext.currentMissionProperty())
                    .selectObject(Mission::currentFlightPlanProperty));

        find(MenuIds.NEW_FLIGHTPLAN)
            .setCommandHandler(this::createNewFlightPlan, applicationContext.currentMissionIsNoDemo());

        find(MenuIds.RENAME_FLIGHTPLAN)
            .setCommandHandler(
                this::renameCurrentFlightPlan,
                PropertyPath.from(applicationContext.currentMissionProperty())
                    .selectReadOnlyObject(Mission::currentFlightPlanProperty)
                    .isNotNull()
                    .and(applicationContext.currentMissionIsNoDemo()));

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyList(Mission::flightPlansProperty)
            .addListener((ListChangeListener<FlightPlan>)change -> flightPlansChanged(change.getList()));

        textProperty()
            .bind(
                valueOrDefault(
                    PropertyPath.from(applicationContext.currentMissionProperty())
                        .select(Mission::currentFlightPlanProperty)
                        .selectReadOnlyString(FlightPlan::nameProperty)));
    }

    private void flightPlansChanged(List<? extends FlightPlan> flightPlans) {
        List<MenuModel> newMenuItems = new ArrayList<>();
        for (FlightPlan flightPlan : flightPlans) {
            MenuModel item = MenuModel.checkItem();
            item.setMnemonicParsing(false);
            item.textProperty().bind(valueOrDefault(flightPlan.nameProperty()));
            item.setUserData(flightPlan);
            newMenuItems.add(item);
        }

        MenuModel model = find(MenuIds.FLIGHTPLANS);
        model.getChildren().setAll(newMenuItems);
    }

    private StringBinding valueOrDefault(ReadOnlyStringProperty property) {
        return Bindings.createStringBinding(
            () -> {
                if (property.get() == null) {
                    return languageHelper.toFriendlyName(MenuIds.NEW_FLIGHTPLAN);
                }

                return property.get();
            },
            property);
    }

    // TODO: Why does setting the selected flight plan to null create a new flight plan?
    private void createNewFlightPlan() {
        applicationContext.getCurrentMission().setCurrentFlightPlan(null);
    }

    private void renameCurrentFlightPlan() {
        Mission mission = applicationContext.getCurrentMission();
        FlightPlan flightPlan = mission.getCurrentFlightPlan();
        String oldName = mission.getCurrentFlightPlan().getName();
        String newName =
            RenameDialog.requestNewMissionName(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.SidePaneView.selector.flightplan.rename.title"),
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.SidePaneView.selector.flightplan.rename.name"),
                    oldName,
                    languageHelper,
                    this::isFlightplanNameValid)
                .orElse(oldName);

        flightPlanService.renameFlightPlan(mission, flightPlan, newName);
    }

    private boolean isFlightplanNameValid(String flightPlanName) {
        Mission currentMission = applicationContext.getCurrentMission();
        int duplicates =
            currentMission
                .flightPlansProperty()
                .filtered(flightPlan -> flightPlan.getName().equals(flightPlanName))
                .size();
        return duplicates == 0 && flightPlanName.trim().length() > 1;
    }

}
