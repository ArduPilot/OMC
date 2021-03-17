/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.three;

import com.intel.flightplanning.core.Waypoint;
import com.jme3.math.Vector3f;

public class PlanVoxel {
    public boolean coreModel; // massive volxelized model of the object
    public boolean coreSurface; // surface of the voxelized model of the object

    public boolean dilatedModel; // massive dilated model by capturing distance
    public boolean dilatedSurface; // massive dilated model by capturing distance

    public boolean dilatedModelCollisionCheck;

    //		public boolean ereasedModel;
    //		public boolean ereasedSurface;

    public int coverage;
    public Waypoint lastCoveringImg;

    public boolean fromSide;
    public boolean hasNormal;
    public Vector3f normal;
    double curving; // 1 means not curved, mess them one inner corner, larger 1 outer corner
    public boolean normalComputed;

    //		public Vec4 normalEreased;
    //		public boolean normalEreasedComputed;
    //		public boolean hasNormalEreased;

    //		public boolean inScope; //by min/max hight voxels got truncated
    public boolean
        isOutside; // is not inside the object. Is part of the outer non filled connceted cluster. used to distinguish
                   // non filled area inside objects from those outside

}
