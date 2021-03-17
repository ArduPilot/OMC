/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane;

import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

public abstract class PageAwareViewModel extends ViewModelBase {

    @InjectScope
    protected MainScope mainScope;

    private final INavigationService navigationService;

    protected PageAwareViewModel(INavigationService navigationService) {
        this.navigationService = navigationService;
    }

    protected INavigationService getNavigationService() {
        return navigationService;
    }

    public BooleanExpression pageIs(SidePanePage... pages) {
        return Bindings.createBooleanBinding(
            () -> {
                boolean result = false;
                for (SidePanePage page : pages) {
                    result = result || (navigationService.getSidePanePage() == page);
                }

                return result;
            },
            navigationService.sidePanePageProperty());
    }

    public BooleanExpression navigationPageIs(WorkflowStep... pages) {
        return Bindings.createBooleanBinding(
            () -> {
                boolean result = false;
                for (WorkflowStep page : pages) {
                    result = result || (navigationService.workflowStepProperty().isEqualTo(page).get());
                }

                return result;
            },
            navigationService.workflowStepProperty());
    }

    public void bindNodeVisibility(BooleanExpression condition, Node... nodes) {
        for (Node node : nodes) {
            BindingUtils.bindVisibility(node, condition);
        }
    }

    public void bindMenuItemVisibility(BooleanExpression condition, MenuItem... menuItems) {
        for (MenuItem menuItem : menuItems) {
            menuItem.visibleProperty().bind(condition);
        }
    }

}
