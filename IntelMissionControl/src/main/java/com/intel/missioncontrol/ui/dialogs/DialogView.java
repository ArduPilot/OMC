/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.ui.RootView;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.image.Image;

/** This is the base class for views that are represented by dialog windows. */
public abstract class DialogView<ViewModelType extends DialogViewModel> extends RootView<ViewModelType> {

    private static final Image icon =
        new Image(
            DialogView.class
                .getResource("/com/intel/missioncontrol/app-icon/mission-control-icon.png")
                .toExternalForm());

    public abstract ReadOnlyStringProperty titleProperty();

    public Image getIcon() {
        return icon;
    }

}
