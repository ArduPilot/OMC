/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.menu.MainMenuModel;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionScope;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;

public class NavBarViewModel extends ViewModelBase {

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    UavConnectionScope uavConnectionScope;

    private final MapProperty<WorkflowStep, Boolean> workflowStepAvailableState =
        new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final MapProperty<WorkflowStep, Boolean> workflowStepEnabledState =
        new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final MapProperty<NavBarDialog, Boolean> navBarDialogAvailableState =
        new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final MapProperty<NavBarDialog, Boolean> navBarDialogEnabledState =
        new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final List<Object> strongReferences = new ArrayList<>();
    private final GeneralSettings settings;

    @Inject
    public NavBarViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            NotificationCenter notificationCenter,
            ISettingsManager settingsManager) {
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.settings = settingsManager.getSection(GeneralSettings.class);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        Expect.notNull(mainScope, "mainScope");

        bindEnabledState(WorkflowStep.PLANNING, applicationContext.currentMissionProperty().isNotNull());
        bindEnabledState(
            WorkflowStep.FLIGHT,
            applicationContext
                .currentMissionProperty()
                .isNotNull()
                .and(settings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG)));
        bindEnabledState(WorkflowStep.DATA_PREVIEW, applicationContext.currentMissionProperty().isNotNull());

        bindAvailableAndEnabledState(
            WorkflowStep.FLIGHT,
            applicationContext
                .currentMissionProperty()
                .isNotNull()
                .and(settings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG)));

        bindAvailableAndEnabledState(
            NavBarDialog.TOOLS,
            applicationContext
                .currentMissionProperty()
                .isNotNull()
                .and(settings.operationLevelProperty().isNotEqualTo(OperationLevel.USER)));

        bindAvailableAndEnabledState(
            NavBarDialog.CONNECTION,
            applicationContext
                .currentMissionProperty()
                .isNotNull()
                .and(settings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG)));
    }

    public ReadOnlyObjectProperty<WorkflowStep> currentWorkflowStepProperty() {
        return navigationService.workflowStepProperty();
    }

    public ReadOnlyObjectProperty<NavBarDialog> currentNavBarDialogProperty() {
        return navigationService.navBarDialogProperty();
    }

    public ReadOnlyObjectProperty<ConnectionState> connectionStateProperty() {
        return uavConnectionScope.connectionStateProperty();
    }

    public MapProperty<WorkflowStep, Boolean> workflowStepAvailableStateProperty() {
        return workflowStepAvailableState;
    }

    public MapProperty<WorkflowStep, Boolean> workflowStepEnabledStateProperty() {
        return workflowStepEnabledState;
    }

    public MapProperty<NavBarDialog, Boolean> navBarDialogAvailableStateProperty() {
        return navBarDialogAvailableState;
    }

    public MapProperty<NavBarDialog, Boolean> navBarDialogEnabledStateProperty() {
        return navBarDialogEnabledState;
    }

    public void navigateTo(WorkflowStep page) {
        navigationService.navigateTo(page);
    }

    public void navigateTo(NavBarDialog page) {
        navigationService.navigateTo(page);
    }

    private void bindAvailableAndEnabledState(NavBarDialog navBarDialog, BooleanBinding binding) {
        bindAvailableState(navBarDialog, binding);
        bindEnabledState(navBarDialog, binding);
    }

    private void bindAvailableAndEnabledState(WorkflowStep workflowStep, BooleanBinding binding) {
        bindAvailableState(workflowStep, binding);
        bindEnabledState(workflowStep, binding);
    }

    private void bindAvailableState(NavBarDialog navBarDialog, BooleanBinding binding) {
        binding.addListener(
            (observable, oldValue, newValue) -> navBarDialogAvailableState.replace(navBarDialog, binding.getValue()));

        navBarDialogAvailableState.putIfAbsent(navBarDialog, binding.get());
        strongReferences.add(binding);
    }

    private void bindEnabledState(NavBarDialog navBarDialog, BooleanBinding binding) {
        binding.addListener(
            (observable, oldValue, newValue) -> navBarDialogEnabledState.replace(navBarDialog, binding.getValue()));

        navBarDialogEnabledState.putIfAbsent(navBarDialog, binding.get());
        strongReferences.add(binding);
    }

    private void bindAvailableState(WorkflowStep navBarDialog, BooleanBinding binding) {
        binding.addListener(
            (observable, oldValue, newValue) -> workflowStepAvailableState.replace(navBarDialog, binding.getValue()));

        workflowStepAvailableState.putIfAbsent(navBarDialog, binding.get());
        strongReferences.add(binding);
    }

    private void bindEnabledState(WorkflowStep navBarDialog, BooleanBinding binding) {
        binding.addListener(
            (observable, oldValue, newValue) -> workflowStepAvailableState.replace(navBarDialog, binding.getValue()));

        workflowStepAvailableState.putIfAbsent(navBarDialog, binding.get());
        strongReferences.add(binding);
    }

}
