/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigationService implements INavigationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationService.class);

    private static class NavState {
        final WorkflowStep workflowStep;
        final NavBarDialog navbarDialog;
        final SidePanePage sidePanePage;
        final SettingsPage settingsPage;
        final Map<WorkflowStep, SidePanePage> storedSidePanePages = new HashMap<>();

        NavState(
                WorkflowStep workflowStep,
                NavBarDialog navbarDialog,
                SidePanePage sidePanePage,
                SettingsPage settingsPage,
                Map<WorkflowStep, SidePanePage> storedSidePanePages) {
            this.workflowStep = workflowStep;
            this.navbarDialog = navbarDialog;
            this.sidePanePage = sidePanePage;
            this.settingsPage = settingsPage;
            this.storedSidePanePages.putAll(storedSidePanePages);
        }
    }

    private final ObjectProperty<WorkflowStep> workflowStep = new SimpleObjectProperty<>();
    private final ObjectProperty<NavBarDialog> navbarDialog = new SimpleObjectProperty<>();
    private final ObjectProperty<SidePaneTab> sidePaneTab = new SimpleObjectProperty<>();
    private final ObjectProperty<SidePanePage> sidePanePage = new SimpleObjectProperty<>();
    private final ObjectProperty<SettingsPage> settingsPage = new SimpleObjectProperty<>();
    private final Map<WorkflowStep, SidePanePage> storedSidePanePages = new HashMap<>();
    private final Map<WorkflowStep, Supplier<SidePanePage>> workflowStepRules = new HashMap<>();
    private final Map<SidePaneTab, Supplier<SidePanePage>> sidePaneTabRules = new HashMap<>();
    private final Stack<NavState> history = new Stack<>();
    private NavState targetNavState;
    private boolean disabled;

    public NavigationService() {
        applyNavState(
            new NavState(
                WorkflowStep.NONE,
                NavBarDialog.NONE,
                SidePanePage.RECENT_MISSIONS,
                SettingsPage.GENERAL,
                storedSidePanePages));
    }

    @Override
    public ReadOnlyObjectProperty<WorkflowStep> workflowStepProperty() {
        return workflowStep;
    }

    @Override
    public ReadOnlyObjectProperty<NavBarDialog> navBarDialogProperty() {
        return navbarDialog;
    }

    @Override
    public ReadOnlyObjectProperty<SidePaneTab> sidePaneTabProperty() {
        return sidePaneTab;
    }

    @Override
    public ReadOnlyObjectProperty<SidePanePage> sidePanePageProperty() {
        return sidePanePage;
    }

    @Override
    public ReadOnlyObjectProperty<SettingsPage> settingsPageProperty() {
        return settingsPage;
    }

    @Override
    public WorkflowStep getWorkflowStep() {
        return workflowStep.get();
    }

    @Override
    public NavBarDialog getNavbarDialog() {
        return navbarDialog.get();
    }

    @Override
    public SidePanePage getSidePanePage() {
        return sidePanePage.get();
    }

    @Override
    public SettingsPage getSettingsPage() {
        return settingsPage.get();
    }

    @Override
    public void enable() {
        disabled = false;
    }

    @Override
    public void disable() {
        disabled = true;
    }

    @Override
    public void addRule(WorkflowStep navigationTarget, Supplier<SidePanePage> rule) {
        workflowStepRules.put(navigationTarget, rule);
    }

    @Override
    public void addRule(SidePaneTab navigationTarget, Supplier<SidePanePage> rule) {
        sidePaneTabRules.put(navigationTarget, rule);
    }

    @Override
    public void pushNavigationState() {
        history.push(
            new NavState(
                workflowStep.get(), navbarDialog.get(), sidePanePage.get(), settingsPage.get(), storedSidePanePages));
    }

    @Override
    public void popNavigationState() {
        if (!history.isEmpty()) {
            NavState navState = history.pop();
            this.workflowStep.set(navState.workflowStep);
            this.navbarDialog.set(navState.navbarDialog);
            this.sidePanePage.set(navState.sidePanePage);
            this.settingsPage.set(navState.settingsPage);
            this.storedSidePanePages.clear();
            this.storedSidePanePages.putAll(navState.storedSidePanePages);
        }
    }

    @Override
    public void navigateTo(WorkflowStep step) {
        if (!Platform.isFxApplicationThread()) {
            Dispatcher.platform().runLater(() -> navigateTo(step));
        } else {
            if (disabled) {
                LOGGER.info(getDiscardDisabledMessage(step));
                return;
            }

            if (targetNavState != null) {
                if (targetNavState.workflowStep == step) {
                    return;
                }

                LOGGER.debug(getDiscardNestedMessage(step, targetNavState.workflowStep));
                return;
            }

            NavState currentNavState = getCurrentNavState();
            SidePanePage newPage = null;
            if (workflowStep.get() != step) {
                Supplier<SidePanePage> supplier = workflowStepRules.get(step);
                if (supplier != null) {
                    newPage = supplier.get();
                }

                if (newPage == null) {
                    newPage = storedSidePanePages.get(step);
                }

                if (newPage == null) {
                    newPage = step.getFirstSidePanePage();
                }
            }

            if (newPage == null) {
                newPage = currentNavState.sidePanePage;
            }

            applyNavState(
                new NavState(
                    step,
                    NavBarDialog.NONE,
                    newPage,
                    currentNavState.settingsPage,
                    currentNavState.storedSidePanePages));
        }
    }

    @Override
    public void navigateTo(NavBarDialog dialog) {
        if (!Platform.isFxApplicationThread()) {
            Dispatcher.platform().runLater(() -> navigateTo(dialog));
        } else {
            if (disabled) {
                LOGGER.debug(getDiscardDisabledMessage(dialog));
                return;
            }

            if (targetNavState != null) {
                if (targetNavState.navbarDialog == dialog) {
                    return;
                }

                LOGGER.debug(getDiscardNestedMessage(dialog, targetNavState.navbarDialog));
                return;
            }

            NavState currentNavState = getCurrentNavState();
            applyNavState(
                new NavState(
                    currentNavState.workflowStep,
                    dialog,
                    currentNavState.sidePanePage,
                    currentNavState.settingsPage,
                    currentNavState.storedSidePanePages));
        }
    }

    @Override
    public void navigateTo(SidePanePage page) {
        if (!Platform.isFxApplicationThread()) {
            Dispatcher.platform().runLater(() -> navigateTo(page));
        } else {
            if (disabled) {
                LOGGER.debug(getDiscardDisabledMessage(page));
                return;
            }

            if (targetNavState != null) {
                if (targetNavState.sidePanePage == page) {
                    return;
                }

                LOGGER.debug(getDiscardNestedMessage(page, targetNavState.sidePanePage));
                return;
            }

            NavState currentNavState = getCurrentNavState();
            Map<WorkflowStep, SidePanePage> newStoredSidePanePages = new HashMap<>(currentNavState.storedSidePanePages);
            newStoredSidePanePages.put(page.getWorkflowStep(), page);
            applyNavState(
                new NavState(
                    page.getWorkflowStep(),
                    NavBarDialog.NONE,
                    page,
                    currentNavState.settingsPage,
                    newStoredSidePanePages));
        }
    }

    @Override
    public void navigateTo(SettingsPage page) {
        if (!Platform.isFxApplicationThread()) {
            Dispatcher.platform().runLater(() -> navigateTo(page));
        } else {
            if (disabled) {
                LOGGER.debug(getDiscardDisabledMessage(page));
                return;
            }

            if (targetNavState != null) {
                if (targetNavState.settingsPage == page) {
                    return;
                }

                LOGGER.debug(getDiscardNestedMessage(page, targetNavState.settingsPage));
                return;
            }

            NavState currentNavState = getCurrentNavState();
            applyNavState(
                new NavState(
                    currentNavState.workflowStep,
                    NavBarDialog.SETTINGS,
                    currentNavState.sidePanePage,
                    page,
                    currentNavState.storedSidePanePages));
        }
    }

    private NavState getCurrentNavState() {
        return new NavState(
            workflowStep.get(), navbarDialog.get(), sidePanePage.get(), settingsPage.get(), storedSidePanePages);
    }

    private void applyNavState(NavState navState) {
        targetNavState = navState;
        workflowStep.set(navState.workflowStep);
        navbarDialog.set(navState.navbarDialog);
        sidePaneTab.set(navState.sidePanePage.getTab());
        sidePanePage.set(navState.sidePanePage);
        settingsPage.set(navState.settingsPage);
        storedSidePanePages.clear();
        storedSidePanePages.putAll(navState.storedSidePanePages);
        targetNavState = null;
    }

    private String getDiscardNestedMessage(Object requested, Object current) {
        return "Nested navigation request to " + requested + " was discarded, currently navigating to " + current;
    }

    private String getDiscardDisabledMessage(Object requested) {
        return "Navigation request to " + requested + " was discarded because navigation is currently disabled.";
    }

}
