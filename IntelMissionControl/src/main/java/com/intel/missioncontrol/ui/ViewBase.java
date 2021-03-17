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

    public static class Accessor {
        public static ViewModel getViewModel(ViewBase view) {
            return view.getViewModel();
        }
    }

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

        // The JavaFX scene graph does not keep a strong reference to the controller of a FXML file by default, which
        // can result in the controller instance being garbage-collected.
        //
        // A strong reference to the controller is created implicitly by:
        //      1. Adding a listener to one of the controls via FXML, e.g. <Button onAction="#onClick"/>
        //      2. Adding a listener to one of the controls in the controller class, such that the listener holds a
        //         reference to the controller class (for example, by using a lambda listener).
        //
        // A listener that compiles down to a static method invocation (and thus does not reference the controller
        // instance) will not keep the controller alive.
        //
        // Since we don't want to be surprised by garbage-collection if we create a view controller that is not
        // strongly reachable from the scene graph, we store a reference to the view controller in the root node's
        // user data.
        rootNode.setUserData(this);

        viewModel.subscribe(IDialogService.GET_WINDOW_REQUEST, new WeakNotificationObserver(getWindowObserver));

        initializeView();

        if (!superInitializeCalled) {
            throw new IllegalStateException("Did you forget to call super.initializeView()?");
        }
    }

}
