/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IHasStart {

    double getStartInM();

    void setStartInM(double startInM);

    public int getIndex();
}
