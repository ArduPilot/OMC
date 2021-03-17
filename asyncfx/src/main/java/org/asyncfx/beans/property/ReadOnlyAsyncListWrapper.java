/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.LockedList;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ReadOnlyAsyncListWrapper<E> extends SimpleAsyncListProperty<E> {

    private final ReadOnlyAsyncPropertyImpl readOnlyProperty = new ReadOnlyAsyncPropertyImpl();

    public ReadOnlyAsyncListWrapper(Object bean) {
        super(bean);
    }

    public ReadOnlyAsyncListWrapper(Object bean, PropertyMetadata<AsyncObservableList<E>> metadata) {
        super(bean, metadata);
    }

    public ReadOnlyAsyncListProperty<E> getReadOnlyProperty() {
        return readOnlyProperty;
    }

    private class ReadOnlyAsyncPropertyImpl extends ReadOnlyAsyncListProperty<E> {
        @Override
        public long getUniqueId() {
            return ReadOnlyAsyncListWrapper.this.getUniqueId();
        }

        @Override
        public ReadOnlyAsyncIntegerProperty sizeProperty() {
            return ReadOnlyAsyncListWrapper.this.sizeProperty();
        }

        @Override
        public LockedList<E> lock() {
            return ReadOnlyAsyncListWrapper.this.lock();
        }

        @Override
        public ReadOnlyAsyncBooleanProperty emptyProperty() {
            return ReadOnlyAsyncListWrapper.this.emptyProperty();
        }

        @Override
        public AsyncObservableList<E> get() {
            return ReadOnlyAsyncListWrapper.this.get();
        }

        @Override
        public AsyncObservableList<E> getUncritical() {
            return ReadOnlyAsyncListWrapper.this.getUncritical();
        }

        @Override
        public int indexOf(Object obj) {
            return ReadOnlyAsyncListWrapper.this.indexOf(obj);
        }

        @Override
        public int lastIndexOf(Object obj) {
            return ReadOnlyAsyncListWrapper.this.lastIndexOf(obj);
        }

        @Override
        public E get(int i) {
            return ReadOnlyAsyncListWrapper.this.get(i);
        }

        @Override
        public boolean contains(Object obj) {
            return ReadOnlyAsyncListWrapper.this.contains(obj);
        }

        @Override
        public boolean containsAll(Collection<?> objects) {
            return ReadOnlyAsyncListWrapper.this.containsAll(objects);
        }

        @Override
        public Iterator<E> iterator() {
            return ReadOnlyAsyncListWrapper.this.iterator();
        }

        @Override
        public ListIterator<E> listIterator() {
            return ReadOnlyAsyncListWrapper.this.listIterator();
        }

        @Override
        public Spliterator<E> spliterator() {
            return ReadOnlyAsyncListWrapper.this.spliterator();
        }

        @Override
        public Object getBean() {
            return ReadOnlyAsyncListWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyAsyncListWrapper.this.getName();
        }

        @Override
        public PropertyMetadata<AsyncObservableList<E>> getMetadata() {
            return ReadOnlyAsyncListWrapper.this.getMetadata();
        }

        @Override
        public AccessController getAccessController() {
            return ReadOnlyAsyncListWrapper.this.getAccessController();
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            ReadOnlyAsyncListWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super AsyncObservableList<E>> listener, Executor executor) {
            ReadOnlyAsyncListWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(ChangeListener<? super AsyncObservableList<E>> listener) {
            ReadOnlyAsyncListWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener) {
            ReadOnlyAsyncListWrapper.this.addListener(listener);
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener, Executor executor) {
            ReadOnlyAsyncListWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            ReadOnlyAsyncListWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super AsyncObservableList<E>> listener) {
            ReadOnlyAsyncListWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            ReadOnlyAsyncListWrapper.this.removeListener(listener);
        }

        @Override
        public void removeListener(ListChangeListener<? super E> listener) {
            ReadOnlyAsyncListWrapper.this.removeListener(listener);
        }

        @Override
        public void addListener(SubChangeListener listener, Executor executor) {
            ReadOnlyAsyncListWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(SubInvalidationListener listener, Executor executor) {
            ReadOnlyAsyncListWrapper.this.addListener(listener, executor);
        }

        @Override
        public void addListener(SubChangeListener listener) {
            ReadOnlyAsyncListWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(SubChangeListener listener) {
            ReadOnlyAsyncListWrapper.this.removeListener(listener);
        }

        @Override
        public void addListener(SubInvalidationListener listener) {
            ReadOnlyAsyncListWrapper.this.addListener(listener);
        }

        @Override
        public void removeListener(SubInvalidationListener listener) {
            ReadOnlyAsyncListWrapper.this.removeListener(listener);
        }

        @Override
        public Object[] toArray() {
            return ReadOnlyAsyncListWrapper.this.toArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return ReadOnlyAsyncListWrapper.this.toArray(array);
        }

        @Override
        public boolean equals(Object obj) {
            return ReadOnlyAsyncListWrapper.this.equals(obj);
        }

        @Override
        public String toString() {
            return ReadOnlyAsyncListWrapper.this.toString();
        }
    }

}
