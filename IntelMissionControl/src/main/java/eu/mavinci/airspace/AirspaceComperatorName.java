/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import java.util.Comparator;

public class AirspaceComperatorName implements Comparator<IAirspace> {

	public int compare(IAirspace arg0, IAirspace arg1) {
		return arg0.toString().compareTo(arg1.toString());
	}
}
