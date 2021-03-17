/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.LatLon;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class StraightLine implements IAirspacePointConnector, Serializable {

	public StraightLine(LatLon endpoint) {
		vertex = new LinkedList<LatLon>();
		vertex.add(endpoint);
	}
	
	public List<LatLon> getConnectingPoints(int num_of_sampling_points) {
		return vertex;
	}
	
	public LatLon vertex() {
		return vertex.get(0);
	}
	
	private List<LatLon> vertex;

}
