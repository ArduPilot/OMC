/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navigation;

import java.util.function.Supplier;
import javafx.beans.property.ReadOnlyObjectProperty;

public interface INavigationService {

    void addRule(WorkflowStep navigationTarget, Supplier<SidePanePage> rule);

    void addRule(SidePaneTab navigationTarget, Supplier<SidePanePage> rule);

    ReadOnlyObjectProperty<WorkflowStep> workflowStepProperty();

    ReadOnlyObjectProperty<NavBarDialog> navBarDialogProperty();

    ReadOnlyObjectProperty<SidePaneTab> sidePaneTabProperty();

    ReadOnlyObjectProperty<SidePanePage> sidePanePageProperty();

    ReadOnlyObjectProperty<SettingsPage> settingsPageProperty();

    ReadOnlyObjectProperty<ConnectionPage> connectionPageProperty();

    WorkflowStep getWorkflowStep();

    NavBarDialog getNavbarDialog();

    SidePanePage getSidePanePage();

    SettingsPage getSettingsPage();

    ConnectionPage getConnectionPage();

    void enable();

    void disable();

    void pushNavigationState();

    void popNavigationState();

    boolean navigateTo(WorkflowStep step);

    boolean navigateTo(NavBarDialog dialog);

    boolean navigateTo(SidePanePage page);

    boolean navigateTo(SettingsPage page);

    boolean navigateTo(ConnectionPage page);

}
