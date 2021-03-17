/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableList;

class DeferredListListener<E> {
    final InvalidationListener invalidationListener;
    final SubInvalidationListener subInvalidationListener;
    final ChangeListener<? super AsyncObservableList<E>> changeListener;
    final SubChangeListener subChangeListener;
    final ListChangeListener<? super E> listChangeListener;
    final boolean added;

    DeferredListListener(InvalidationListener invalidationListener, boolean added) {
        this.invalidationListener = invalidationListener;
        this.subInvalidationListener = null;
        this.changeListener = null;
        this.subChangeListener = null;
        this.listChangeListener = null;
        this.added = added;
    }

    DeferredListListener(SubInvalidationListener subInvalidationListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = subInvalidationListener;
        this.changeListener = null;
        this.subChangeListener = null;
        this.listChangeListener = null;
        this.added = added;
    }

    DeferredListListener(ChangeListener<? super AsyncObservableList<E>> changeListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = null;
        this.changeListener = changeListener;
        this.subChangeListener = null;
        this.listChangeListener = null;
        this.added = added;
    }

    DeferredListListener(SubChangeListener subChangeListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = null;
        this.changeListener = null;
        this.subChangeListener = subChangeListener;
        this.listChangeListener = null;
        this.added = added;
    }

    DeferredListListener(ListChangeListener<? super E> listChangeListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = null;
        this.changeListener = null;
        this.subChangeListener = null;
        this.listChangeListener = listChangeListener;
        this.added = added;
    }
}
