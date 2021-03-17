/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

/** Created by ekorotkova on 27.11.2017. */
public interface IAirplaneParamsUpdateListener extends IAirplaneListener {
    /** flight Phase */
    public class ParamsUpdateStatus {
        public short varUpdProgress;
        public short cmdUpdProgress;
        public short paramUpdProgress;

        public float getTotalPercent() {
            float progress = (((varUpdProgress + cmdUpdProgress + paramUpdProgress) / 300.0f));
            return progress > 0.95f ? 1f : progress;
        }
    }

    public void recv_paramsUpdateStatusChange(ParamsUpdateStatus fpStatus);
}
