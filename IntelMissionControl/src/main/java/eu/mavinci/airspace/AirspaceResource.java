/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;


public class AirspaceResource extends AirspaceStream {
	
	String resource;
	
	public AirspaceResource(String resource) throws Exception {
		this(resource,resource);
		String [] tmp = resource.split("/");
		name = "build in: " + tmp[tmp.length-1];
	}
		
	
	public AirspaceResource(String resource, String name) throws Exception {
		super(ClassLoader.getSystemResource(resource).openStream(),name);
		this.resource = resource;
	}

	public String getResource(){
		return resource;
	}
	
}
