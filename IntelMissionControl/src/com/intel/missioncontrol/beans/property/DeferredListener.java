/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;

class DeferredListener<T> {
    final InvalidationListener invalidationListener;
    final ChangeListener<T> changeListener;
    final boolean added;

    DeferredListener(InvalidationListener invalidationListener, boolean added) {
        this.invalidationListener = invalidationListener;
        this.changeListener = null;
        this.added = added;
    }

    DeferredListener(ChangeListener<T> changeListener, boolean added) {
        this.invalidationListener = null;
        this.changeListener = changeListener;
        this.added = added;
    }
}
