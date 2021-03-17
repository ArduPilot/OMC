/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.collections.AsyncCollection;
import org.jetbrains.annotations.Nullable;

/**
 * When merging a source object into a target object by calling {@link Mergeable#merge(Object, MergeStrategy)}, the
 * merge strategy specifies how conflicts will be handled. There are four strategies to choose from:
 *
 * <ul>
 *   <li>DryRun<br>
 *       This strategy simulates what would happen if the merge operation was executed. The target object is left
 *       unchanged, and all conflicts are recorded in {@link DryRun#getConflicts()}.
 *   <li>Default<br>
 *       The default strategy applies non-conflicting changes, but does not change the target if a conflict occurs.
 *       Conflicts are recorded in {@link Default#getConflicts()}.
 *   <li>KeepOurs<br>
 *       All non-conflicting changes are applied (same as with Default), while all conflicts are resolved by choosing
 *       'our' version over 'their' version.
 *   <li>KeepTheirs<br>
 *       ALl non-conflicting changes are applied (same as with Default), while all conflicts are resolved by choosing
 *       'their' version over 'ours'.
 * </ul>
 */
public abstract class MergeStrategy {

    public interface ConflictEntry {
        AsyncProperty getProperty();
    }

    public static class ValueConflictEntry implements ConflictEntry {
        private final AsyncProperty property;
        private final Object ourValue;
        private final Object theirValue;

        ValueConflictEntry(AsyncProperty property, Object ourValue, Object theirValue) {
            this.property = property;
            this.ourValue = ourValue;
            this.theirValue = theirValue;
        }

        @Override
        public AsyncProperty getProperty() {
            return property;
        }

        public Object getOurValue() {
            return ourValue;
        }

        public Object getTheirValue() {
            return theirValue;
        }

        @Override
        public String toString() {
            Object bean = property.getBean();
            String name =
                bean != null ? bean.getClass().getSimpleName() + "." + property.getName() : property.getName();
            return name + " changed on both sides (ours: " + ourValue + ", theirs: " + theirValue + ")";
        }
    }

    public static class ItemRemovedConflictEntry implements ConflictEntry {
        private final AsyncProperty property;
        private final boolean removedByUs;

        ItemRemovedConflictEntry(AsyncProperty property, boolean removedByUs) {
            this.property = property;
            this.removedByUs = removedByUs;
        }

        @Override
        public AsyncProperty getProperty() {
            return property;
        }

        public boolean isRemovedByUs() {
            return removedByUs;
        }

        @Override
        public String toString() {
            Object bean = property.getBean();

            if (removedByUs) {
                return (bean != null ? bean.toString() + "." + property.getName() : property.getName())
                    + " removed by us, modified by them.";
            } else {
                return (bean != null ? bean.toString() + "." + property.getName() : property.getName())
                    + " removed by them, modified by us.";
            }
        }
    }

    /** Merge strategy that records all conflicts, but does not modify the object. */
    public static class DryRun extends MergeStrategy {
        private final List<ConflictEntry> conflicts = new ArrayList<>();

        @Override
        public <T> void updateValue(TrackingAsyncProperty<T> property, T newValue) {}

        @Override
        public <T extends Identifiable, S extends Identifiable> void addItem(
                TrackingAsyncProperty<? extends AsyncCollection<T>> property,
                Collection<T> ourCollection,
                S item,
                Function<S, T> createItem) {}

        @Override
        public <T extends Identifiable> void removeItem(Collection<T> ourCollection, T ourItem) {}

        @Override
        public <T> void resolveValueConflict(TrackingAsyncProperty<T> property, T ourValue, T theirValue) {
            conflicts.add(new ValueConflictEntry(property, ourValue, theirValue));
        }

        @Override
        public <T extends Identifiable, S extends Identifiable> void resolveItemRemovedConflict(
                TrackingAsyncProperty<? extends AsyncCollection<T>> property,
                Collection<T> ourCollection,
                S item,
                @Nullable Function<S, T> createItem) {
            conflicts.add(new ItemRemovedConflictEntry(property, createItem != null));
        }

        public List<ConflictEntry> getConflicts() {
            return conflicts;
        }

        @Override
        public String toString() {
            List<ConflictEntry> conflicts = getConflicts();
            return conflicts.isEmpty() ? "No conflicts." : conflicts.toString();
        }
    }

    /** Merge strategy that applies all non-conflicting changes, but does not resolve conflicts. */
    public static class Default extends MergeStrategy {
        private final List<ConflictEntry> conflicts = new ArrayList<>();

        @Override
        public <T> void updateValue(TrackingAsyncProperty<T> property, T newValue) {
            property.init(newValue);
        }

        @Override
        public <T extends Identifiable, S extends Identifiable> void addItem(
                TrackingAsyncProperty<? extends AsyncCollection<T>> property,
                Collection<T> ourCollection,
                S item,
                Function<S, T> createItem) {
            ourCollection.add(createItem.apply(item));
        }

        @Override
        public <T extends Identifiable> void removeItem(Collection<T> ourCollection, T ourItem) {
            Iterator<T> it = ourCollection.iterator();
            while (it.hasNext()) {
                if (it.next().getId().equals(ourItem.getId())) {
                    it.remove();
                    break;
                }
            }
        }

        @Override
        public <T> void resolveValueConflict(TrackingAsyncProperty<T> property, T ourValue, T theirValue) {
            conflicts.add(new ValueConflictEntry(property, ourValue, theirValue));
        }

        @Override
        public <T extends Identifiable, S extends Identifiable> void resolveItemRemovedConflict(
                TrackingAsyncProperty<? extends AsyncCollection<T>> property,
                Collection<T> ourCollection,
                S item,
                @Nullable Function<S, T> createItem) {
            conflicts.add(new ItemRemovedConflictEntry(property, createItem != null));
        }

        public List<ConflictEntry> getConflicts() {
            return conflicts;
        }

        @Override
        public String toString() {
            List<ConflictEntry> conflicts = getConflicts();
            return conflicts.isEmpty() ? "No conflicts." : conflicts.toString();
        }
    }

    /** Merge strategy that resolves all merge conflicts by choosing 'our' version. */
    public static class KeepOurs extends MergeStrategy {
        @Override
        public <T> void updateValue(TrackingAsyncProperty<T> property, T newValue) {
            property.init(newValue);
        }

        @Override
        public <T extends Identifiable, S extends Identifiable> void addItem(
                TrackingAsyncProperty<? extends AsyncCollection<T>> property,
                Collection<T> ourCollection,
                S item,
                Function<S, T> createItem) {
            ourCollection.add(createItem.apply(item));
        }

        @Override
        public <T extends Identifiable> void removeItem(Collection<T> ourCollection, T ourItem) {
            Iterator<T> it = ourCollection.iterator();
            while (it.hasNext()) {
                if (it.next().getId().equals(ourItem.getId())) {
                    it.remove();
                    break;
                }
            }
        }

        @Override
        public <T> void resolveValueConflict(TrackingAsyncProperty<T> property, T ourValue, T theirValue) {
            property.init(ourValue);
        }

        @Override
        public <T extends Identifiable, S extends Identifiable> void resolveItemRemovedConflict(
                TrackingAsyncProperty<? extends AsyncCollection<T>> property,
                Collection<T> ourCollection,
                S item,
                @Nullable Function<S, T> createItem) {}
    }

    /** Merge strategy that resolves all merge conflicts by choosing 'their' version. */
    public static class KeepTheirs extends MergeStrategy {
        @Override
        public <T> void updateValue(TrackingAsyncProperty<T> property, T newValue) {
            property.init(newValue);
        }

        @Override
        public <T extends Identifiable, S extends Identifiable> void addItem(
                TrackingAsyncProperty<? extends AsyncCollection<T>> property,
                Collection<T> ourCollection,
                S item,
                Function<S, T> createItem) {
            ourCollection.add(createItem.apply(item));
        }

        @Override
        public <T extends Identifiable> void removeItem(Collection<T> ourCollection, T ourItem) {
            Iterator<T> it = ourCollection.iterator();
            while (it.hasNext()) {
                if (it.next().getId().equals(ourItem.getId())) {
                    it.remove();
                    break;
                }
            }
        }

        @Override
        public <T> void resolveValueConflict(TrackingAsyncProperty<T> property, T ourValue, T theirValue) {
            property.init(theirValue);
        }

        @Override
        public <T extends Identifiable, S extends Identifiable> void resolveItemRemovedConflict(
                TrackingAsyncProperty<? extends AsyncCollection<T>> property,
                Collection<T> ourCollection,
                S item,
                @Nullable Function<S, T> createItem) {
            if (createItem != null) {
                ourCollection.add(createItem.apply(item));
            } else {
                Iterator<T> it = ourCollection.iterator();
                while (it.hasNext()) {
                    if (it.next().getId().equals(item.getId())) {
                        it.remove();
                        break;
                    }
                }
            }
        }
    }

    public abstract <T> void updateValue(TrackingAsyncProperty<T> property, T newValue);

    public abstract <T extends Identifiable, S extends Identifiable> void addItem(
            TrackingAsyncProperty<? extends AsyncCollection<T>> property,
            Collection<T> ourCollection,
            S item,
            Function<S, T> createItem);

    public abstract <T extends Identifiable> void removeItem(Collection<T> ourCollection, T ourItem);

    public abstract <T> void resolveValueConflict(TrackingAsyncProperty<T> property, T ourValue, T theirValue);

    public abstract <T extends Identifiable, S extends Identifiable> void resolveItemRemovedConflict(
            TrackingAsyncProperty<? extends AsyncCollection<T>> property,
            Collection<T> ourCollection,
            S item,
            @Nullable Function<S, T> createItem);

}
