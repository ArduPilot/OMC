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
import com.intel.missioncontrol.ui.menu.MainMenuModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import eu.mavinci.core.licence.ILicenceManager;
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

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Object> strongReferences = new ArrayList<>();

    private final GeneralSettings settings;
    private final ILicenceManager licenceManager;
    private final ParameterizedCommand<Enum> navigateToCommand = new ParameterizedDelegateCommand<>(this::navigateTo);
    private Command sendSupportRequestCommand;

    @Inject
    public NavBarViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            NotificationCenter notificationCenter,
            ISettingsManager settingsManager,
            ILicenceManager licenceManager) {
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.settings = settingsManager.getSection(GeneralSettings.class);
        this.licenceManager = licenceManager;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        Expect.notNull(mainScope, "mainScope");

        this.sendSupportRequestCommand =
            mainScope.mainMenuModelProperty().get().find(MainMenuModel.Help.SUPPORT_REQUEST).getCommand();

        bindAvailableAndEnabledState(WorkflowStep.PLANNING, applicationContext.currentMissionProperty().isNotNull());
        bindAvailableAndEnabledState(
            WorkflowStep.FLIGHT,
            applicationContext
                .currentMissionProperty()
                .isNotNull()
                .and(
                    settings.operationLevelProperty()
                        .isEqualTo(OperationLevel.DEBUG)
                        .or(licenceManager.isGrayHawkEditionProperty())));
        bindAvailableAndEnabledState(
            WorkflowStep.DATA_PREVIEW, applicationContext.currentMissionProperty().isNotNull());

        bindAvailableAndEnabledState(
            WorkflowStep.FLIGHT,
            applicationContext
                .currentMissionProperty()
                .isNotNull()
                .and(
                    settings.operationLevelProperty()
                        .isEqualTo(OperationLevel.DEBUG)
                        .or(licenceManager.isGrayHawkEditionProperty())));

        bindAvailableAndEnabledState(
            NavBarDialog.TOOLS,
            applicationContext
                .currentMissionProperty()
                .isNotNull()
                .and(
                    settings.operationLevelProperty()
                        .isEqualTo(OperationLevel.DEBUG)
                        .and(licenceManager.isGrayHawkEditionProperty())));
    }

    public ReadOnlyObjectProperty<WorkflowStep> currentWorkflowStepProperty() {
        return navigationService.workflowStepProperty();
    }

    public ReadOnlyObjectProperty<NavBarDialog> currentNavBarDialogProperty() {
        return navigationService.navBarDialogProperty();
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

    public ParameterizedCommand<Enum> getNavigateToCommand() {
        return navigateToCommand;
    }

    public Command getSendSupportRequestCommand() {
        return sendSupportRequestCommand;
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
            (observable, oldValue, newValue) -> workflowStepEnabledState.replace(navBarDialog, binding.getValue()));

        workflowStepEnabledState.putIfAbsent(navBarDialog, binding.get());
        strongReferences.add(binding);
    }

    private void navigateTo(Enum page) {
        if (page instanceof WorkflowStep) {
            navigationService.navigateTo((WorkflowStep)page);
        } else if (page instanceof NavBarDialog) {
            navigationService.navigateTo((NavBarDialog)page);
        }
    }

}
