/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerRawBackendStrings extends IAirplaneListener {

    /** Raw data from Backend */
    public void rawDataFromBackend(String line);

    /** Raw data send tobackend */
    public void rawDataToBackend(String line);
}
