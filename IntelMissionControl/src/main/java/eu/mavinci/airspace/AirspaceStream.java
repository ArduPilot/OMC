/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;

public class AirspaceStream extends AAirspaceList {
	
	String name;
	
	public AirspaceStream(InputStream is, String name) throws Exception{
		super(Collections.emptyList());
		this.name = name;
		ADebug.log.log(Level.FINE,"start loading Airspaces from:"+name);
		try {
			this.list.addAll((new OpenAirspaceParser(is)).getAirspaces());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		ADebug.log.log(Level.FINE,"stop loading Airspaces from:"+name);
	}

	@Override
	public String getName() {
		return name;
	}
	
}
