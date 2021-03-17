/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.ViewModel;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IDialogContextProviderTestDummy implements IDialogContextProvider {
    @Override
    public void setContext(ViewModel viewModel, Context context) {

    }

    @Override
    public @Nullable Context getContext(ViewModel viewModel) {
        return null;
    }
}
