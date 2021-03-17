/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.helper.Expect;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.ViewModel;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.WeakHashMap;

public class DialogContextProvider implements IDialogContextProvider {

    private final WeakHashMap<ViewModel, Context> map = new WeakHashMap<>();

    @Override
    public synchronized void setContext(ViewModel viewModel, Context context) {
        Expect.notNull(
            viewModel, "viewModel",
            context, "context");

        if (getContext(viewModel) != null) {
            throw new IllegalArgumentException("The specified view model already has an associated context.");
        }

        map.put(viewModel, context);
    }

    @Override
    public synchronized @Nullable Context getContext(ViewModel viewModel) {
        Expect.notNull(viewModel, "viewModel");
        return map.get(viewModel);
    }

}
