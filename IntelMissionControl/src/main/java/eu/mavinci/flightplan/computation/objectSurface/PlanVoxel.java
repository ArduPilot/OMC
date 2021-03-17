/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.flightplan.computation.FlightplanVertex;
import gov.nasa.worldwind.geom.Vec4;

public class PlanVoxel {
    public boolean coreModel; // massive volxelized model of the object
    public boolean coreSurface; // surface of the voxelized model of the object

    public boolean dilatedModel; // massive dilated model by capturing distance
    public boolean dilatedSurface; // massive dilated model by capturing distance

    public boolean dilatedModelCollisionCheck;

    //		public boolean ereasedModel;
    //		public boolean ereasedSurface;

    public int coverage;
    public FlightplanVertex lastCoveringImg;

    public boolean fromSide;
    public boolean hasNormal;
    public Vec4 normal;
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
