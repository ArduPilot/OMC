/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox;

import com.intel.missioncontrol.rtk.IRTKStation;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import javafx.beans.property.ReadOnlyListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface ILinkBoxConnectionService {
    ReadOnlyAsyncObjectProperty<LinkBoxStatus> linkBoxStatusProperty();

    ReadOnlyAsyncObjectProperty<ILinkBox> getLinkBox();

    ReadOnlyAsyncObjectProperty<IRTKStation> getRTKStation();

    void requestLinkBoxAuthentication();

    ReadOnlyListProperty<ResolvableValidationMessage> linkBoxResolvableMessagesProperty();

    enum LinkBoxStatus {
        OFFLINE,
        AUTHENTICATED,
        UNAUTHENTICATED
    }
}
