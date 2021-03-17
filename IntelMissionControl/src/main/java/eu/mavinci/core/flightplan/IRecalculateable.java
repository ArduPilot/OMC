/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IRecalculateable {

    /**
     * executing the updating of any derived values of this flight plan element
     * @return false in case of a failiure
     */
    boolean doSubRecalculationStage1();

    boolean doSubRecalculationStage2();

}
