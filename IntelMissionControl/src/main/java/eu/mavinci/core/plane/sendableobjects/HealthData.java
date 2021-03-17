/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

public class HealthData extends MObject {
    /** */
    private static final long serialVersionUID = 7103219809095600322L;

    /** Absolute values of health channels */
    public MVector<Float> absolute = new MVector<Float>(Float.class);

    /** Relative values of health channels. Values below -10000 are marking that no percent value is avaliable */
    public MVector<Float> percent = new MVector<Float>(Float.class);

}
