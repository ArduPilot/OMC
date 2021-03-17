/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

public enum Dimension {
    LENGTH,
    AREA,
    TIME,
    ANGLE,
    PERCENTAGE,
    SPEED,
    ANGULAR_SPEED,
    STORAGE;

    public interface Length extends Quantity<Length> {}

    public interface Area extends Quantity<Area> {}

    public interface Time extends Quantity<Time> {}

    public interface Angle extends Quantity<Angle> {}

    public interface Percentage extends Quantity<Percentage> {}

    public interface Speed extends Quantity<Speed> {}

    public interface AngularSpeed extends Quantity<AngularSpeed> {}

    public interface Storage extends Quantity<Storage> {}

}
