/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
class ConvertingContentBinding {

    @SuppressWarnings("unchecked")
    static <T, U> void bindContent(List<T> target, ObservableList<? extends U> source, ValueConverter<U, T> converter) {
        checkParameters(target, source);
        final ConvertingListContentBinding<T, U> contentBinding = new ConvertingListContentBinding<>(target, converter);
        List<T> sourceList = new ArrayList<>(source.size());
        for (U item : source) {
            sourceList.add(converter.convert(item));
        }

        if (target instanceof ObservableList) {
            ((ObservableList)target).setAll(sourceList);
        } else {
            target.clear();
            target.addAll(sourceList);
        }

        source.removeListener(contentBinding);
        source.addListener(contentBinding);
    }

    static <T, U> void bindContent(Set<T> target, ObservableSet<? extends U> source, ValueConverter<U, T> converter) {
        checkParameters(target, source);
        final ConvertingSetContentBinding<T, U> contentBinding = new ConvertingSetContentBinding<>(target, converter);
        target.clear();
        for (U item : source) {
            target.add(converter.convert(item));
        }

        source.removeListener(contentBinding);
        source.addListener(contentBinding);
    }

    @SuppressWarnings("unchecked")
    static void unbindContent(Object obj1, Object obj2) {
        checkParameters(obj1, obj2);
        if ((obj1 instanceof List) && (obj2 instanceof ObservableList)) {
            ((ObservableList)obj2).removeListener(new ConvertingListContentBinding((List)obj1, null));
        } else if ((obj1 instanceof Set) && (obj2 instanceof ObservableSet)) {
            ((ObservableSet)obj2).removeListener(new ConvertingSetContentBinding((Set)obj1, null));
        }
    }

    static class ConvertingListContentBinding<T, U> implements ListChangeListener<U>, WeakListener {
        private final WeakReference<List<T>> listRef;
        private final ValueConverter<U, T> converter;

        ConvertingListContentBinding(List<T> list, ValueConverter<U, T> converter) {
            this.listRef = new WeakReference<>(list);
            this.converter = converter;
        }

        @Override
        public void onChanged(Change<? extends U> change) {
            final List<T> list = listRef.get();
            if (list == null) {
                change.getList().removeListener(this);
            } else {
                while (change.next()) {
                    if (change.wasPermutated() || change.wasUpdated()) {
                        list.subList(change.getFrom(), change.getTo()).clear();
                        List<T> newList = new ArrayList<>(change.getTo() - change.getFrom());
                        for (U item : change.getList().subList(change.getFrom(), change.getTo())) {
                            newList.add(converter.convert(item));
                        }

                        list.addAll(change.getFrom(), newList);
                    } else {
                        if (change.wasRemoved()) {
                            list.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                        }

                        if (change.wasAdded()) {
                            List<T> newList = new ArrayList<>(change.getTo() - change.getFrom());
                            for (U item : change.getAddedSubList()) {
                                newList.add(converter.convert(item));
                            }

                            list.addAll(change.getFrom(), newList);
                        }
                    }
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return listRef.get() == null;
        }

        @Override
        public int hashCode() {
            final List<T> list = listRef.get();
            return (list == null) ? 0 : list.hashCode();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final List<T> list1 = listRef.get();
            if (list1 == null) {
                return false;
            }

            if (obj instanceof ConvertingListContentBinding) {
                final ConvertingListContentBinding<T, ?> other = (ConvertingListContentBinding<T, ?>)obj;
                final List<?> list2 = other.listRef.get();
                return list1 == list2;
            }

            return false;
        }
    }

    static class ConvertingSetContentBinding<T, U> implements SetChangeListener<U>, WeakListener {
        private final WeakReference<Set<T>> setRef;
        private final ValueConverter<U, T> converter;

        ConvertingSetContentBinding(Set<T> set, ValueConverter<U, T> converter) {
            this.setRef = new WeakReference<>(set);
            this.converter = converter;
        }

        @Override
        public void onChanged(Change<? extends U> change) {
            final Set<T> set = setRef.get();
            if (set == null) {
                change.getSet().removeListener(this);
            } else {
                if (change.wasRemoved()) {
                    set.remove(converter.convert(change.getElementRemoved()));
                } else {
                    set.add(converter.convert(change.getElementAdded()));
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return setRef.get() == null;
        }

        @Override
        public int hashCode() {
            final Set<T> list = setRef.get();
            return (list == null) ? 0 : list.hashCode();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final Set<T> set1 = setRef.get();
            if (set1 == null) {
                return false;
            }

            if (obj instanceof ConvertingSetContentBinding) {
                final ConvertingSetContentBinding<T, ?> other = (ConvertingSetContentBinding<T, ?>)obj;
                final Set<?> set2 = other.setRef.get();
                return set1 == set2;
            }

            return false;
        }
    }

    private static void checkParameters(Object property1, Object property2) {
        if (property1 == null || property2 == null) {
            throw new NullPointerException("Both parameters must be specified.");
        }

        if (property1 == property2) {
            throw new IllegalArgumentException("Cannot bind object to itself");
        }
    }

}
