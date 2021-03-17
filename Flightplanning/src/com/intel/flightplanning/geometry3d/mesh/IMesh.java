/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.geometry3d.mesh;

import com.jme3.math.Ray;

public interface IMesh {
    public void getVertices();
    public void getNormals();
    public void getBoundingBox();
    public void getTriangles();
    public void intersect(Ray ray);
    public void voxelize();
}
