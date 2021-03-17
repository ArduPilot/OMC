/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import com.sun.javafx.binding.StringFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.value.AsyncObservableListValue;
import org.asyncfx.collections.AsyncObservableList;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class AsyncListExpression<E> implements AsyncObservableListValue<E> {

    private static final ObservableList EMPTY_LIST = FXCollections.emptyObservableList();

    @Override
    public AsyncObservableList<E> getValue() {
        return get();
    }

    public int getSize() {
        return size();
    }

    public abstract ReadOnlyAsyncIntegerProperty sizeProperty();

    public abstract ReadOnlyAsyncBooleanProperty emptyProperty();

    public ObjectBinding<E> valueAt(int index) {
        return Bindings.valueAt(this, index);
    }

    public ObjectBinding<E> valueAt(ObservableIntegerValue index) {
        return Bindings.valueAt(this, index);
    }

    public BooleanBinding isEqualTo(final ObservableList<?> other) {
        return Bindings.equal(this, other);
    }

    public BooleanBinding isNotEqualTo(final ObservableList<?> other) {
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
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.size() : list.size();
    }

    @Override
    public boolean isEmpty() {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.isEmpty() : list.isEmpty();
    }

    @Override
    public boolean contains(Object obj) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.contains(obj) : list.contains(obj);
    }

    @Override
    public Iterator<E> iterator() {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.iterator() : list.iterator();
    }

    @Override
    public Object[] toArray() {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.toArray() : list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        final ObservableList<E> list = get();
        return (list == null) ? (T[])EMPTY_LIST.toArray(array) : list.toArray(array);
    }

    @Override
    public boolean add(E element) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.add(element) : list.add(element);
    }

    @Override
    public boolean remove(Object obj) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.remove(obj) : list.remove(obj);
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.contains(objects) : list.containsAll(objects);
    }

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.addAll(elements) : list.addAll(elements);
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> elements) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.addAll(i, elements) : list.addAll(i, elements);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.removeAll(objects) : list.removeAll(objects);
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.retainAll(objects) : list.retainAll(objects);
    }

    @Override
    public void clear() {
        final ObservableList<E> list = get();
        if (list == null) {
            EMPTY_LIST.clear();
        } else {
            list.clear();
        }
    }

    @Override
    public E get(int i) {
        final ObservableList<E> list = get();
        return (list == null) ? (E)EMPTY_LIST.get(i) : list.get(i);
    }

    @Override
    public E set(int i, E element) {
        final ObservableList<E> list = get();
        return (list == null) ? (E)EMPTY_LIST.set(i, element) : list.set(i, element);
    }

    @Override
    public void add(int i, E element) {
        final ObservableList<E> list = get();
        if (list == null) {
            EMPTY_LIST.add(i, element);
        } else {
            list.add(i, element);
        }
    }

    @Override
    public E remove(int i) {
        final ObservableList<E> list = get();
        return (list == null) ? (E)EMPTY_LIST.remove(i) : list.remove(i);
    }

    @Override
    public int indexOf(Object obj) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.indexOf(obj) : list.indexOf(obj);
    }

    @Override
    public int lastIndexOf(Object obj) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.lastIndexOf(obj) : list.lastIndexOf(obj);
    }

    @Override
    public ListIterator<E> listIterator() {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.listIterator() : list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int i) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.listIterator(i) : list.listIterator(i);
    }

    @Override
    public List<E> subList(int from, int to) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.subList(from, to) : list.subList(from, to);
    }

    @Override
    public boolean addAll(E... elements) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.addAll(elements) : list.addAll(elements);
    }

    @Override
    public boolean setAll(E... elements) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.setAll(elements) : list.setAll(elements);
    }

    @Override
    public boolean setAll(Collection<? extends E> elements) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.setAll(elements) : list.setAll(elements);
    }

    @Override
    public boolean removeAll(E... elements) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.removeAll(elements) : list.removeAll(elements);
    }

    @Override
    public boolean retainAll(E... elements) {
        final ObservableList<E> list = get();
        return (list == null) ? EMPTY_LIST.retainAll(elements) : list.retainAll(elements);
    }

    @Override
    public void remove(int from, int to) {
        final ObservableList<E> list = get();
        if (list == null) {
            EMPTY_LIST.remove(from, to);
        } else {
            list.remove(from, to);
        }
    }

}
