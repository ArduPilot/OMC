/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.asyncfx.beans.binding.ProxyListExpressionHelper;
import org.jetbrains.annotations.NotNull;

class ListPropertyProxy<E> extends ListProperty<E> {

    private static class NullObservableList<E> implements ObservableList<E> {
        private static class ListItr<E> implements ListIterator<E> {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public E next() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public E previous() {
                return null;
            }

            @Override
            public int nextIndex() {
                return -1;
            }

            @Override
            public int previousIndex() {
                return -1;
            }

            @Override
            public void remove() {}

            @Override
            public void set(E e) {}

            @Override
            public void add(E e) {}
        }

        @Override
        @SafeVarargs
        public final boolean addAll(E... es) {
            return false;
        }

        @Override
        @SafeVarargs
        public final boolean setAll(E... es) {
            return false;
        }

        @Override
        public boolean setAll(Collection<? extends E> collection) {
            return false;
        }

        @Override
        @SafeVarargs
        public final boolean removeAll(E... es) {
            return false;
        }

        @Override
        @SafeVarargs
        public final boolean retainAll(E... es) {
            return false;
        }

        @Override
        public void remove(int i, int i1) {}

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public E next() {
                    return null;
                }
            };
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            return a;
        }

        @Override
        public boolean add(E e) {
            return true;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {}

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public E get(int index) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public E set(int index, E element) {
            return null;
        }

        @Override
        public void add(int index, E element) {}

        @Override
        public E remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return -1;
        }

        @NotNull
        @Override
        public ListIterator<E> listIterator() {
            return new ListItr<>();
        }

        @NotNull
        @Override
        public ListIterator<E> listIterator(int index) {
            return new ListItr<>();
        }

        @NotNull
        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return Collections.emptyList();
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {}

        @Override
        public void removeListener(InvalidationListener invalidationListener) {}

        @Override
        public void addListener(ListChangeListener<? super E> listChangeListener) {}

        @Override
        public void removeListener(ListChangeListener<? super E> listChangeListener) {}
    }

    enum BindingType {
        UNIDIRECTIONAL,
        CONTENT_UNIDIRECTIONAL,
        CONTENT_BIDIRECTIONAL
    }

    private final PropertyPath.PropertyPathSegment endpoint;
    private ObservableList<E> fallbackValue;
    private Observable observable;
    private BindingType bindingType;
    private ProxyListExpressionHelper<E> helper;
    private ListProperty<E> peer;
    private ReadOnlyIntegerWrapper size;
    private ReadOnlyBooleanWrapper empty;

    ListPropertyProxy(PropertyPath.PropertyPathSegment endpoint, ObservableList<E> fallbackValue) {
        this.endpoint = endpoint;
        this.fallbackValue = fallbackValue;
    }

    @SuppressWarnings("unchecked")
    void setPeer(ListProperty<E> peer) {
        if (observable != null) {
            switch (bindingType) {
            case UNIDIRECTIONAL:
                if (this.peer != null) {
                    this.peer.unbind();
                }

                peer.bind((ObservableValue<? extends ObservableList<E>>)observable);
                break;

            case CONTENT_UNIDIRECTIONAL:
                if (this.peer != null) {
                    this.peer.unbindContent(observable);
                }

                peer.bindContent((ObservableList<E>)observable);
                break;

            case CONTENT_BIDIRECTIONAL:
                if (this.peer != null) {
                    this.peer.unbindContentBidirectional(observable);
                }

                peer.bindContentBidirectional((ObservableList<E>)observable);
                break;
            }
        }

        if (size != null) {
            size.unbind();
            size.set(0);
        }

        if (empty != null) {
            empty.unbind();
            empty.set(true);
        }

        this.peer = peer;

        if (peer != null) {
            if (size != null) {
                size.bind(peer.sizeProperty());
            }

            if (empty != null) {
                empty.bind(peer.emptyProperty());
            }
        }

        ProxyListExpressionHelper.setPeer(helper, peer);
    }

    @Override
    public ReadOnlyIntegerProperty sizeProperty() {
        if (size == null) {
            size = new ReadOnlyIntegerWrapper(this, "size");

            if (peer != null) {
                size.bind(peer.sizeProperty());
            }
        }

        return size.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyBooleanProperty emptyProperty() {
        if (empty == null) {
            empty = new ReadOnlyBooleanWrapper(this, "empty", true);

            if (peer != null) {
                empty.bind(peer.emptyProperty());
            }
        }

        return empty;
    }

    @Override
    public void bindContent(ObservableList<E> observable) {
        if (peer != null) {
            peer.bindContent(observable);
        }

        this.observable = observable;
        this.bindingType = BindingType.CONTENT_UNIDIRECTIONAL;
    }

    @Override
    public void unbindContent(Object o) {
        if (peer != null) {
            peer.unbindContent(o);
        }

        observable = null;
        bindingType = null;
    }

    @Override
    public void bindContentBidirectional(ObservableList<E> observable) {
        if (peer != null) {
            peer.bindContentBidirectional(observable);
        }

        this.observable = observable;
        this.bindingType = BindingType.CONTENT_BIDIRECTIONAL;
    }

    @Override
    public void unbindContentBidirectional(Object o) {
        if (peer != null) {
            peer.unbindContentBidirectional(o);
        }

        observable = null;
        bindingType = null;
    }

    @Override
    public void bind(ObservableValue<? extends ObservableList<E>> observable) {
        if (peer != null) {
            peer.bind(observable);
        }

        this.observable = observable;
        this.bindingType = BindingType.UNIDIRECTIONAL;
    }

    @Override
    public void unbind() {
        if (peer != null) {
            peer.unbind();
        }

        observable = null;
        bindingType = null;
    }

    @Override
    public boolean isBound() {
        return observable != null;
    }

    @Override
    public Object getBean() {
        return peer != null ? peer.getBean() : null;
    }

    @Override
    public String getName() {
        return peer != null ? peer.getName() : null;
    }

    @Override
    public ObservableList<E> get() {
        if (peer == null) {
            if (fallbackValue == null) {
                fallbackValue = new NullObservableList<>();
            }

            return fallbackValue;
        }

        return peer.getValue();
    }

    @Override
    public void addListener(ListChangeListener<? super E> listChangeListener) {
        helper = ProxyListExpressionHelper.addListener(helper, this, peer, listChangeListener);
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listChangeListener) {
        helper = ProxyListExpressionHelper.removeListener(helper, listChangeListener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableList<E>> changeListener) {
        helper = ProxyListExpressionHelper.addListener(helper, this, peer, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableList<E>> changeListener) {
        helper = ProxyListExpressionHelper.removeListener(helper, changeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        helper = ProxyListExpressionHelper.addListener(helper, this, peer, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        helper = ProxyListExpressionHelper.removeListener(helper, invalidationListener);
    }

    @Override
    public void set(ObservableList<E> value) {
        if (peer != null) {
            peer.setValue(value);
        }
    }

    @Override
    public synchronized String toString() {
        Object bean = this.getBean();
        String name = this.getName();
        StringBuilder stringBuilder = new StringBuilder(getClass().getSimpleName());
        stringBuilder.append(" [");
        if (bean != null) {
            stringBuilder.append("bean: ").append(bean).append(", ");
        }

        if (name != null && !name.equals("")) {
            stringBuilder.append("name: ").append(name).append(", ");
        }

        if (peer == null) {
            stringBuilder.append("unresolved, ");
        }

        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }

}
