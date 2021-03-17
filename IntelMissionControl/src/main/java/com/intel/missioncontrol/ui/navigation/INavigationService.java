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

    WorkflowStep getWorkflowStep();

    NavBarDialog getNavbarDialog();

    SidePanePage getSidePanePage();

    SettingsPage getSettingsPage();

    void enable();

    void disable();

    void pushNavigationState();

    void popNavigationState();

    /** Navigate to the given WorkflowStep. May be called from any thread. */
    void navigateTo(WorkflowStep step);

    /** Navigate to the given NavBarDialog. May be called from any thread. */
    void navigateTo(NavBarDialog dialog);

    /** Navigate to the given SidePanePage. May be called from any thread. */
    void navigateTo(SidePanePage page);

    /** Navigate to the given SettingsPage. May be called from any thread. */
    void navigateTo(SettingsPage page);
}
