/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.binding;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncIntegerProperty;
import com.intel.missioncontrol.beans.value.AsyncObservableSetValue;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.sun.javafx.binding.StringFormatter;
import java.util.Collection;
import java.util.Iterator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncSetExpression<E> implements AsyncObservableSetValue<E> {

    private static final AsyncObservableSet EMPTY_SET = FXAsyncCollections.emptyObservableSet();

    @Override
    public AsyncObservableSet<E> getValue() {
        return get();
    }

    public int getSize() {
        return size();
    }

    public abstract ReadOnlyAsyncIntegerProperty sizeProperty();

    public abstract ReadOnlyAsyncBooleanProperty emptyProperty();

    public BooleanBinding isEqualTo(final AsyncObservableSet<?> other) {
        return Bindings.equal(this, other);
    }

    public BooleanBinding isNotEqualTo(final AsyncObservableSet<?> other) {
        return Bindings.notEqual(this, other);
    }

    public BooleanBinding isNull() {
        return Bindings.isNull(this);
    }

    public BooleanBinding isNotNull() {
        return Bindings.isNotNull(this);
    }

    public StringBinding asString() {
        return (StringBinding)StringFormatter.convert(this);
    }

    @Override
    public int size() {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.size() : set.size();
    }

    @Override
    public boolean isEmpty() {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.isEmpty() : set.isEmpty();
    }

    @Override
    public boolean contains(Object obj) {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.contains(obj) : set.contains(obj);
    }

    @Override
    public Iterator<E> iterator() {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.iterator() : set.iterator();
    }

    @Override
    public Object[] toArray() {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.toArray() : set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? (T[])EMPTY_SET.toArray(array) : set.toArray(array);
    }

    @Override
    public boolean add(E element) {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.add(element) : set.add(element);
    }

    @Override
    public boolean remove(Object obj) {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.remove(obj) : set.remove(obj);
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.contains(objects) : set.containsAll(objects);
    }

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.addAll(elements) : set.addAll(elements);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.removeAll(objects) : set.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        final AsyncObservableSet<E> set = get();
        return (set == null) ? EMPTY_SET.retainAll(objects) : set.retainAll(objects);
    }

    @Override
    public void clear() {
        final AsyncObservableSet<E> set = get();
        if (set == null) {
            EMPTY_SET.clear();
        } else {
            set.clear();
        }
    }

}
