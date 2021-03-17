/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import org.asyncfx.beans.value.SubChangeListener;

public class DeferredListener<T> {

    private final InvalidationListener invalidationListener;
    private final SubInvalidationListener subInvalidationListener;
    private final ChangeListener<T> changeListener;
    private final SubChangeListener subChangeListener;
    private final boolean added;

    public DeferredListener(InvalidationListener invalidationListener, boolean added) {
        this.invalidationListener = invalidationListener;
        this.subInvalidationListener = null;
        this.changeListener = null;
        this.subChangeListener = null;
        this.added = added;
    }

    public DeferredListener(SubInvalidationListener invalidationListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = invalidationListener;
        this.changeListener = null;
        this.subChangeListener = null;
        this.added = added;
    }

    public DeferredListener(SubChangeListener subChangeListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = null;
        this.changeListener = null;
        this.subChangeListener = subChangeListener;
        this.added = added;
    }

    public DeferredListener(ChangeListener<T> changeListener, boolean added) {
        this.invalidationListener = null;
        this.subInvalidationListener = null;
        this.changeListener = changeListener;
        this.subChangeListener = null;
        this.added = added;
    }

    public InvalidationListener getInvalidationListener() {
        return invalidationListener;
    }

    public SubInvalidationListener getSubInvalidationListener() {
        return subInvalidationListener;
    }

    public ChangeListener<T> getChangeListener() {
        return changeListener;
    }

    public SubChangeListener getSubChangeListener() {
        return subChangeListener;
    }

    public boolean wasAdded() {
        return added;
    }

}
