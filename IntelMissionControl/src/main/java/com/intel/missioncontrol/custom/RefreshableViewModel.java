/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.custom;

import de.saxsys.mvvmfx.ViewModel;

/** Interface the model should implement in order to use {@link RefreshableViewModelCellFactory}. */
public interface RefreshableViewModel extends ViewModel {

    /**
     * When new instance of model comes this method is called to renew the state with given instance.
     *
     * @param model instance of model. You can cast it to this class
     */
    void refreshModel(RefreshableViewModel model);
}
