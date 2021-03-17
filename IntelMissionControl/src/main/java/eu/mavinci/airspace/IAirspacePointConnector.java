/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.LatLon;
import java.util.List;

public interface IAirspacePointConnector {
	public List<LatLon> getConnectingPoints(int num_of_sampling_points);
}
