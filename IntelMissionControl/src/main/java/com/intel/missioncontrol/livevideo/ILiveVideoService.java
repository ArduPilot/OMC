/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.livevideo;

import com.intel.missioncontrol.ui.livevideo.IUILiveVideoStream;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;

public interface ILiveVideoService {
    ReadOnlyAsyncListProperty<IUILiveVideoStream> streamListProperty();

    void shutdown();
}
