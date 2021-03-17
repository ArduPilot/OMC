/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.LinkInfo;
import eu.mavinci.core.plane.sendableobjects.LinkInfo;

public interface IAirplaneListenerLinkInfo extends IAirplaneListener {

    /** Receive Information about the quality of the RF link to the airplane */
    public void recv_linkInfo(LinkInfo li);

}
