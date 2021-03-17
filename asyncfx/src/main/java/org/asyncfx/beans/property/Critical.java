/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import static org.asyncfx.beans.AccessControllerImpl.LockName.VALUE;
import static org.asyncfx.beans.AccessControllerImpl.LockType.GROUP;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Supplier;
import org.asyncfx.Optional;
import org.asyncfx.beans.AccessControllerImpl;

/**
 * Controls access to a set of properties to enable consistent reading and writing.
 *
 * <p>If a writer locks a set of properties, modifications of these properties will not be visible to a reader until the
 * writer exits the critical section; in this way, the reader will never observe a state where only some of the
 * properties appear to have modifications (consistency guarantee).
 *
 * <p>Writers and readers that opt for this consistency guarantee must access the properties only within critical
 * sections. Reading properties outside of a critical section is allowed, but breaks the consistency guarantee for the
 * reader.
 *
 * <p>It is possible to disallow uncritical access to a set of properties by specifying a {@link ConsistencyGroup} as
 * part of their metadata. Trying to read or write a property that is part of a consistency group without locking the
 * properties will throw an {@link IllegalStateException}. If a set of properties share a consistency group, locking any
 * one of the properties is equivalent to locking all of the properties.
 *
 * <p>If a set of properties is protected by a {@link ConsistencyGroup}, the value of any property can be read outside
 * of a critical section by calling {@link ReadOnlyAsyncProperty#getValueUncritical()}. Note that this breaks the
 * consistency guarantee for the reader.
 *
 * <p>If a property is part of a consistency group, it cannot be the target of a binding (as this would break the
 * consistency guarantee). It can, however, be the source of a binding.
 */
@SuppressWarnings({"unused"})
public final class Critical {

    @SuppressWarnings("ComparatorCombinators")
    private static final Comparator<ReadOnlyAsyncProperty> propertyComparator =
        (left, right) -> Long.compare(left.getUniqueId(), right.getUniqueId());

    private static final ThreadLocal<AccessControllerImpl.GroupLock> currentLockGroup = new ThreadLocal<>();

    private Critical() {}

    public static void lock(ReadOnlyAsyncProperty property0, Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        lock(properties, runnable);
    }

    public static void lock(ReadOnlyAsyncProperty property0, ReadOnlyAsyncProperty property1, Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        lock(properties, runnable);
    }

    public static void lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        lock(properties, runnable);
    }

    public static void lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        lock(properties, runnable);
    }

    public static void lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            ReadOnlyAsyncProperty property4,
            Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        addProperties(properties, property4);
        lock(properties, runnable);
    }

    public static void lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            ReadOnlyAsyncProperty property4,
            ReadOnlyAsyncProperty property5,
            Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        addProperties(properties, property4);
        addProperties(properties, property5);
        lock(properties, runnable);
    }

    public static void lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            ReadOnlyAsyncProperty property4,
            ReadOnlyAsyncProperty property5,
            ReadOnlyAsyncProperty property6,
            Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        addProperties(properties, property4);
        addProperties(properties, property5);
        addProperties(properties, property6);
        lock(properties, runnable);
    }

    public static void lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            ReadOnlyAsyncProperty property4,
            ReadOnlyAsyncProperty property5,
            ReadOnlyAsyncProperty property6,
            ReadOnlyAsyncProperty property7,
            Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        addProperties(properties, property4);
        addProperties(properties, property5);
        addProperties(properties, property6);
        addProperties(properties, property7);
        lock(properties, runnable);
    }

    public static void lock(ReadOnlyAsyncProperty[] properties, Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> props = new TreeSet<>(propertyComparator);
        for (ReadOnlyAsyncProperty property : properties) {
            addProperties(props, property);
        }

        lock(props, runnable);
    }

    public static void lock(Iterable<ReadOnlyAsyncProperty> properties, Runnable runnable) {
        TreeSet<ReadOnlyAsyncProperty> props = new TreeSet<>(propertyComparator);
        for (ReadOnlyAsyncProperty property : properties) {
            addProperties(props, property);
        }

        lock(props, runnable);
    }

    public static <T> T lock(ReadOnlyAsyncProperty property0, Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        return lock(properties, supplier);
    }

    public static <T> T lock(ReadOnlyAsyncProperty property0, ReadOnlyAsyncProperty property1, Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        return lock(properties, supplier);
    }

    public static <T> T lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        return lock(properties, supplier);
    }

    public static <T> T lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        return lock(properties, supplier);
    }

    public static <T> T lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            ReadOnlyAsyncProperty property4,
            Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        addProperties(properties, property4);
        return lock(properties, supplier);
    }

    public static <T> T lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            ReadOnlyAsyncProperty property4,
            ReadOnlyAsyncProperty property5,
            Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        addProperties(properties, property4);
        addProperties(properties, property5);
        return lock(properties, supplier);
    }

    public static <T> T lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            ReadOnlyAsyncProperty property4,
            ReadOnlyAsyncProperty property5,
            ReadOnlyAsyncProperty property6,
            Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        addProperties(properties, property4);
        addProperties(properties, property5);
        addProperties(properties, property6);
        return lock(properties, supplier);
    }

    public static <T> T lock(
            ReadOnlyAsyncProperty property0,
            ReadOnlyAsyncProperty property1,
            ReadOnlyAsyncProperty property2,
            ReadOnlyAsyncProperty property3,
            ReadOnlyAsyncProperty property4,
            ReadOnlyAsyncProperty property5,
            ReadOnlyAsyncProperty property6,
            ReadOnlyAsyncProperty property7,
            Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> properties = new TreeSet<>(propertyComparator);
        addProperties(properties, property0);
        addProperties(properties, property1);
        addProperties(properties, property2);
        addProperties(properties, property3);
        addProperties(properties, property4);
        addProperties(properties, property5);
        addProperties(properties, property6);
        addProperties(properties, property7);
        return lock(properties, supplier);
    }

    public static <T> T lock(ReadOnlyAsyncProperty[] properties, Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> props = new TreeSet<>(propertyComparator);
        for (ReadOnlyAsyncProperty property : properties) {
            addProperties(props, property);
        }

        return lock(props, supplier);
    }

    public static <T> T lock(Iterable<ReadOnlyAsyncProperty> properties, Supplier<T> supplier) {
        TreeSet<ReadOnlyAsyncProperty> props = new TreeSet<>(propertyComparator);
        for (ReadOnlyAsyncProperty property : properties) {
            addProperties(props, property);
        }

        return lock(props, supplier);
    }

    private static void lock(TreeSet<ReadOnlyAsyncProperty> properties, Runnable runnable) {
        long[] stamps = writeLock(properties);
        AccessControllerImpl.GroupLock groupLock = new AccessControllerImpl.GroupLock(properties);

        boolean lockGroupSet = false;
        AccessControllerImpl.GroupLock currentGroupLock = Critical.currentLockGroup.get();
        if (currentGroupLock != null) {
            if (!currentGroupLock.getProperties().containsAll(properties)) {
                throw new IllegalStateException(
                    "Illegal attempt to extend a critical section: "
                        + "nesting is only allowed if no additional properties are locked.");
            }
        } else {
            setGroupLock(properties, groupLock);
            Critical.currentLockGroup.set(groupLock);
            lockGroupSet = true;
        }

        unlockWrite(properties, stamps);

        try {
            runnable.run();
        } finally {
            if (lockGroupSet) {
                stamps = writeLock(properties);
                setGroupLock(properties, null);
                unlockWrite(properties, stamps);
                Critical.currentLockGroup.set(null);
                groupLock.set();
            }
        }
    }

    private static <T> T lock(TreeSet<ReadOnlyAsyncProperty> properties, Supplier<T> supplier) {
        long[] stamps = writeLock(properties);
        AccessControllerImpl.GroupLock groupLock = new AccessControllerImpl.GroupLock(properties);

        boolean lockGroupSet = false;
        AccessControllerImpl.GroupLock currentGroupLock = Critical.currentLockGroup.get();
        if (currentGroupLock != null) {
            if (!currentGroupLock.getProperties().containsAll(properties)) {
                throw new IllegalStateException(
                    "Illegal attempt to extend a critical section: "
                        + "nesting is only allowed if no additional properties are locked.");
            }
        } else {
            setGroupLock(properties, groupLock);
            Critical.currentLockGroup.set(groupLock);
            lockGroupSet = true;
        }

        unlockWrite(properties, stamps);

        try {
            return supplier.get();
        } finally {
            if (lockGroupSet) {
                stamps = writeLock(properties);
                setGroupLock(properties, null);
                unlockWrite(properties, stamps);
                Critical.currentLockGroup.set(null);
                groupLock.set();
            }
        }
    }

    private static void addProperties(Collection<ReadOnlyAsyncProperty> c, ReadOnlyAsyncProperty property) {
        c.add(property);
        Optional<ConsistencyGroup> group = PropertyMetadata.Accessor.getConsistencyGroup(property.getMetadata());
        if (group.isPresent()) {
            Collections.addAll(c, group.get().getProperties());
        }
    }

    private static long[] writeLock(TreeSet<ReadOnlyAsyncProperty> properties) {
        long[] stamps = new long[properties.size()];
        int i = 0;

        for (ReadOnlyAsyncProperty property : properties) {
            AccessControllerImpl accessController = (AccessControllerImpl)property.getAccessController();
            stamps[i++] = accessController.writeLock(VALUE, GROUP);
        }

        return stamps;
    }

    private static void unlockWrite(TreeSet<ReadOnlyAsyncProperty> properties, long[] stamps) {
        Iterator<ReadOnlyAsyncProperty> it = properties.descendingIterator();
        int i = stamps.length;
        while (it.hasNext()) {
            ((AccessControllerImpl)it.next().getAccessController()).unlockWrite(VALUE, stamps[--i]);
            stamps[i] = 0;
        }
    }

    private static void setGroupLock(
            TreeSet<ReadOnlyAsyncProperty> properties, AccessControllerImpl.GroupLock groupLock) {
        for (ReadOnlyAsyncProperty property : properties) {
            ((AccessControllerImpl)property.getAccessController()).setGroupLock(groupLock);
        }
    }

}
