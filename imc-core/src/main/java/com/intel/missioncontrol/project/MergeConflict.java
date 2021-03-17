/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import java.util.List;

public class MergeConflict {

    public enum Type {
        VALUE,
        ITEM_REMOVED
    }

    private final List<Object> path;
    private final Object ours;
    private final Object theirs;
    private final Type type;

    public MergeConflict(List<Object> path) {
        this.path = path;
        this.ours = null;
        this.theirs = null;
        this.type = Type.ITEM_REMOVED;
    }

    public MergeConflict(List<Object> path, Object ours, Object theirs) {
        this.path = path;
        this.ours = ours;
        this.theirs = theirs;
        this.type = Type.VALUE;
    }

    public List<Object> getPath() {
        return path;
    }

    public Object getOurs() {
        return ours;
    }

    public Object getTheirs() {
        return theirs;
    }

    public Type getType() {
        return type;
    }

}
