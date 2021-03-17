/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

public interface ILiveVideoService {
    ReadOnlyBooleanProperty availableProperty();

    ReadOnlyAsyncListProperty<ILiveVideoStream> streamListProperty();

    void setSelectedStream(ILiveVideoStream stream);
    ILiveVideoStream getSelectedStream();

    void shutdown ();
}
