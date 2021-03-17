/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubInvalidationListener;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncObservableListBase<E> extends AbstractList<E> implements AsyncSubObservableList<E> {

    private AsyncListListenerHelper<E> listenerHelper;
    private final AsyncListChangeBuilder<E> changeBuilder = new AsyncListChangeBuilder<E>(this);

    protected final void nextUpdate(int pos) {
        changeBuilder.nextUpdate(pos);
    }

    protected final void nextSet(int idx, E old) {
        changeBuilder.nextSet(idx, old);
    }

    protected final void nextReplace(int from, int to, List<? extends E> removed) {
        changeBuilder.nextReplace(from, to, removed);
    }

    protected final void nextRemove(int idx, List<? extends E> removed) {
        changeBuilder.nextRemove(idx, removed);
    }

    protected final void nextRemove(int idx, E removed) {
        changeBuilder.nextRemove(idx, removed);
    }

    protected final void nextPermutation(int from, int to, int[] perm) {
        changeBuilder.nextPermutation(from, to, perm);
    }

    protected final void nextAdd(int from, int to) {
        changeBuilder.nextAdd(from, to);
    }

    protected final void beginChange() {
        changeBuilder.beginChange();
    }

    protected final void endChange() {
        changeBuilder.endChange();
    }

    @Override
    public final void addListener(InvalidationListener listener) {
        listenerHelper = AsyncListListenerHelper.addListener(listenerHelper, this, listener);
    }

    @Override
    public final void removeListener(InvalidationListener listener) {
        listenerHelper = AsyncListListenerHelper.removeListener(listenerHelper, listener);
    }

    @Override
    public final void addListener(SubInvalidationListener listener) {
        listenerHelper = AsyncListListenerHelper.addListener(listenerHelper, this, listener);
    }

    @Override
    public final void removeListener(SubInvalidationListener listener) {
        listenerHelper = AsyncListListenerHelper.removeListener(listenerHelper, listener);
    }

    @Override
    public final void addListener(ListChangeListener<? super E> listener) {
        listenerHelper = AsyncListListenerHelper.addListener(listenerHelper, this, listener);
    }

    @Override
    public final void removeListener(ListChangeListener<? super E> listener) {
        listenerHelper = AsyncListListenerHelper.removeListener(listenerHelper, listener);
    }

    protected void fireChange(ListChangeListener.Change<? extends E> change, boolean finalChange) {
        AsyncListListenerHelper.fireValueChangedEvent(listenerHelper, change, finalChange);
    }

    protected final boolean hasListeners() {
        return AsyncListListenerHelper.hasListeners(listenerHelper);
    }

    @Override
    public boolean addAll(E... elements) {
        return addAll(Arrays.asList(elements));
    }

    @Override
    public boolean setAll(E... elements) {
        return setAll(Arrays.asList(elements));
    }

    @Override
    public boolean setAll(Collection<? extends E> col) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(E... elements) {
        return removeAll(Arrays.asList(elements));
    }

    @Override
    public boolean retainAll(E... elements) {
        return retainAll(Arrays.asList(elements));
    }

    @Override
    public void remove(int from, int to) {
        removeRange(from, to);
    }

}
