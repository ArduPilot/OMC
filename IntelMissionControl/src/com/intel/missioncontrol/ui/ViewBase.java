/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationObserver;
import de.saxsys.mvvmfx.utils.notifications.WeakNotificationObserver;
import eu.mavinci.core.obfuscation.IKeepClassname;
import javafx.scene.Parent;

/** ViewBase is the base class for all views that can be the owner of a dialog window. */
public abstract class ViewBase<T extends ViewModel> implements FxmlView<T>, IKeepClassname {

    protected abstract Parent getRootNode();

    protected abstract ViewModel getViewModel();

    protected void initializeView() {
        superInitializeCalled = true;
    }

    private boolean superInitializeCalled;

    private final NotificationObserver getWindowObserver =
        (key, payload) -> {
            if (payload.length > 0 && payload[0] instanceof IDialogService.GetWindowRequest) {
                IDialogService.GetWindowRequest getWindowRequest = (IDialogService.GetWindowRequest)payload[0];
                getWindowRequest.setWindow(getRootNode().getScene().getWindow());
            }
        };

    public final void initialize() {
        ViewModel viewModel = getViewModel();
        final Parent rootNode = getRootNode();

        Expect.notNull(
            rootNode, "rootNode",
            viewModel, "viewModel");

        viewModel.subscribe(IDialogService.GET_WINDOW_REQUEST, new WeakNotificationObserver(getWindowObserver));

        initializeView();

        if (!superInitializeCalled) {
            throw new IllegalStateException("Did you forget to call super.initializeView()?");
        }
    }

}
