/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox;

import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import javafx.beans.property.ReadOnlyListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;

public interface ILinkBox {
    byte[] getIMCKey();

    byte[] getDroneKey();

    ReadOnlyAsyncBooleanProperty linkBoxOnlineProperty();

    ReadOnlyAsyncStringProperty linkBoxNameProperty();

    ReadOnlyAsyncObjectProperty<BatteryAlertLevel> getBatteryInfo();

    ReadOnlyAsyncObjectProperty<WifiConnectionQuality> getLinkBoxConnectionQuality();

    ReadOnlyAsyncObjectProperty<DroneConnectionQuality> getDroneConnectionQuality();

    ReadOnlyAsyncObjectProperty<DataConnectionStatus> getDataConnectionStatus();

    ReadOnlyAsyncObjectProperty<LinkBoxAlertLevel> getAlertLevel();

    ReadOnlyAsyncStringProperty messageProperty();

    ReadOnlyAsyncBooleanProperty linkBoxAuthenticatedProperty();

    void requestLinkBoxAuthentication();

    ReadOnlyListProperty<ResolvableValidationMessage> resolvableLinkBoxMessagesProperty();
}
