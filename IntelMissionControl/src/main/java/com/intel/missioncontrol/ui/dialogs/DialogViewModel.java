/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DialogViewModel<TDialogResult, TPayload> extends ViewModelBase<TPayload> {

    public static class Accessor {
        public static void setCloseHandler(DialogViewModel viewModel, Runnable handler) {
            viewModel.setCloseHandler(handler);
        }
    }

    private final ObjectProperty<TDialogResult> dialogResult = new SimpleObjectProperty<>();
    private final BooleanProperty canClose = new SimpleBooleanProperty(true);
    private final Command closeCommand = new DelegateCommand(this::close, canClose);

    private Runnable closeHandler;

    public ReadOnlyObjectProperty<TDialogResult> dialogResultProperty() {
        return dialogResult;
    }

    public BooleanProperty canCloseProperty() {
        return canClose;
    }

    public @Nullable TDialogResult getDialogResult() {
        return dialogResult.get();
    }

    protected void setDialogResult(TDialogResult dialogResult) {
        this.dialogResult.set(dialogResult);
    }

    public boolean canClose() {
        return canClose.get();
    }

    public void setCanClose(boolean canClose) {
        this.canClose.set(canClose);
    }

    public Command getCloseCommand() {
        return closeCommand;
    }

    /**
     * This method is called when the dialog is closed. Depending on the implementation, this can occur once or multiple
     * times. For example, when this view model is represented by a dialog window, onClosing() will only be called once.
     */
    protected void onClosing() {}

    /**
     * Allows implementations to customize the closing behavior. If a close handler is set, the onClosing() method will
     * not be called when the close command is invoked. The implementation is responsible to call the onClosing() method
     * manually at an appropriate time.
     */
    private void setCloseHandler(Runnable handler) {
        closeHandler = handler;
    }

    private void close() {
        if (closeHandler != null) {
            closeHandler.run();
        } else {
            onClosing();
        }
    }

}
