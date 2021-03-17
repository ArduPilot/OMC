/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.update;

/** Created by aleonov on 9/22/2017. */
public class AvailableUpdate {
    private Integer revision;
    private String version;

    public String getVersion() {
        return version;
    }

    public Integer getRevision() {
        return revision;
    }

    private AvailableUpdate(Integer revision, String version) {
        this.revision = revision;
        this.version = version;
    }

    public static AvailableUpdate of(Integer revision, String version) {
        return new AvailableUpdate(revision, version);
    }
}
