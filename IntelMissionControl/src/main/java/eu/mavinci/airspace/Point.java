/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

public class Point {

	public Point(double xMeters, double yMeters) {
		this.x = xMeters;
		this.y = yMeters;
	}

	public double x;
	public double y;
	
	@Override
	public String toString() {
		return "x="+x+" y="+y;
	}
}
