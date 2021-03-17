/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

public interface ILiveVideoWidgetService {
    ReadOnlyBooleanProperty canMakeVisibleProperty();

    BooleanProperty isVisibleProperty();

    BooleanProperty isDetachedProperty();

    // FIXME: this ViewModel needs to be changed to one, the takes care of spawning the Dialog
    void setDialogControllerViewModel(LiveVideoPaneModel viewModel);

    void setPaneControllerViewModel(LiveVideoPaneModel viewModel);
}
