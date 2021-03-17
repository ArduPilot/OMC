/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;

public class AirspaceFile extends AirspaceStream {
	
	File file;
	
	public AirspaceFile(File file) throws Exception {
		super(new FileInputStream(file),"file: "+file.getName());
		ADebug.log.log(Level.FINE,"ready loading Airspaces from FILE:"+file);
		this.file = file;
	}

	public File getFile(){
		return file;
	}
}
