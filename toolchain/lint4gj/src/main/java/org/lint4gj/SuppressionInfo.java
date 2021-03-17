/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

class SuppressionInfo {

    private final boolean local;
    private final boolean localOrInherited;

    SuppressionInfo(boolean local, boolean localOrInherited) {
        this.local = local;
        this.localOrInherited = localOrInherited;
    }

    boolean isLocal() {
        return local;
    }

    boolean isLocalOrInherited() {
        return localOrInherited;
    }

}
