/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.collections.AsyncObservableList;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

class DeferredListListener<E> {
    final InvalidationListener invalidationListener;
    final ChangeListener<? super AsyncObservableList<E>> changeListener;
    final ListChangeListener<? super E> listChangeListener;
    final boolean added;

    DeferredListListener(InvalidationListener invalidationListener, boolean added) {
        this.invalidationListener = invalidationListener;
        this.changeListener = null;
        this.listChangeListener = null;
        this.added = added;
    }

    DeferredListListener(ChangeListener<? super AsyncObservableList<E>> changeListener, boolean added) {
        this.invalidationListener = null;
        this.changeListener = changeListener;
        this.listChangeListener = null;
        this.added = added;
    }

    DeferredListListener(ListChangeListener<? super E> listChangeListener, boolean added) {
        this.invalidationListener = null;
        this.changeListener = null;
        this.listChangeListener = listChangeListener;
        this.added = added;
    }
}
