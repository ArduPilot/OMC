/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.LatLon;
import java.util.LinkedList;
import java.util.List;

public class PolygonAndArcAirspace extends Airspace {
	public static final int DEFAULT_ARC_SAMPLING = 20;

	public PolygonAndArcAirspace(String name, AirspaceTypes type) {
		super(name, type);
		edges = new LinkedList<IAirspacePointConnector>();
	}

	@Override
	public List<LatLon> getPolygon() {
		if (vertices.size() == 0){
			vertices = new LinkedList<LatLon>();
			interpolate(DEFAULT_ARC_SAMPLING);
//			System.out.println("...");
//			System.out.println(this);
//			System.out.println(edges);
//			System.out.println("vertices"+vertices);
		}
		return vertices;
	}

	@Override
	public void addVertex(LatLon v) {
		edges.add(new StraightLine(v));
	}

	protected void addArc(LatLon center_point, LatLon endpoint,
			Arc.TurnDir turn_dir) {
		edges.add(new Arc(center_point, vertices.get(vertices.size() - 1),
				endpoint, turn_dir));
	}

	public void addArc(Arc arc) {
		edges.add(arc);
	}

	private void interpolate(int number_of_sampling_points) {
		for (IAirspacePointConnector edge : edges){
			List<LatLon> l = edge.getConnectingPoints(number_of_sampling_points);
			vertices.addAll(l);
		}
	}

	private List<IAirspacePointConnector> edges;
}
