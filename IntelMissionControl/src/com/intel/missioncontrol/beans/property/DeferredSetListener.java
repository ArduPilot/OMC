/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.collections.AsyncObservableSet;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;

class DeferredSetListener<E> {
    final InvalidationListener invalidationListener;
    final ChangeListener<? super AsyncObservableSet<E>> changeListener;
    final SetChangeListener<? super E> setChangeListener;
    final boolean added;

    DeferredSetListener(InvalidationListener invalidationListener, boolean added) {
        this.invalidationListener = invalidationListener;
        this.changeListener = null;
        this.setChangeListener = null;
        this.added = added;
    }

    DeferredSetListener(ChangeListener<? super AsyncObservableSet<E>> changeListener, boolean added) {
        this.invalidationListener = null;
        this.changeListener = changeListener;
        this.setChangeListener = null;
        this.added = added;
    }

    DeferredSetListener(SetChangeListener<? super E> setChangeListener, boolean added) {
        this.invalidationListener = null;
        this.changeListener = null;
        this.setChangeListener = setChangeListener;
        this.added = added;
    }
}
