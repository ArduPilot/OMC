/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;

public interface IAirplaneListenerPhoto extends IAirplaneListener {
    /**
     * new Photo captured
     *
     * @param photo
     */
    public void recv_photo(PhotoData photo);
}
