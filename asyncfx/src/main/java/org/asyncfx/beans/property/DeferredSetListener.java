/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableSet;

class DeferredSetListener<E> {
    final InvalidationListener invalidationListener;
    final SubInvalidationListener subInvalidationListener;
    final ChangeListener<? super AsyncObservableSet<E>> changeListener;
    final SubChangeListener subChangeListener;
    final SetChangeListener<? super E> setChangeListener;
    final boolean added;

    DeferredSetListener(InvalidationListener invalidationListener, boolean added) {
        this.invalidationListener = invalidationListener;
        this.subInvalidationListener = null;
        this.changeListener = null;
        this.subChangeListener = null;
        this.setChangeListener = null;
        this.added = added;
    }

    DeferredSetListener(SubInvalidationListener subInvalidationListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = subInvalidationListener;
        this.changeListener = null;
        this.subChangeListener = null;
        this.setChangeListener = null;
        this.added = added;
    }

    DeferredSetListener(ChangeListener<? super AsyncObservableSet<E>> changeListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = null;
        this.changeListener = changeListener;
        this.subChangeListener = null;
        this.setChangeListener = null;
        this.added = added;
    }

    DeferredSetListener(SubChangeListener subChangeListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = null;
        this.changeListener = null;
        this.subChangeListener = subChangeListener;
        this.setChangeListener = null;
        this.added = added;
    }

    DeferredSetListener(SetChangeListener<? super E> setChangeListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = null;
        this.changeListener = null;
        this.subChangeListener = null;
        this.setChangeListener = setChangeListener;
        this.added = added;
    }
}
