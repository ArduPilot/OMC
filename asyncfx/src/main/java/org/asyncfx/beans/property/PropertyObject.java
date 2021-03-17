/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AccessControllerImpl;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.SubObservable;
import org.asyncfx.beans.value.SubChangeListener;
import org.asyncfx.concurrent.Dispatcher;
import org.jetbrains.annotations.Nullable;

/**
 * PropertyObject is the base class for objects that contain async properties and supports the following features:
 *
 * <ul>
 *   <li>Sub-property invalidation and change notifications<br>
 *       Users can register {@link SubInvalidationListener} or {@link SubChangeListener} for properties of type {@link
 *       AsyncObjectProperty}, {@link AsyncListProperty} or {@link AsyncSetProperty} to receive notifications from
 *       sub-properties.
 *   <li>Attached values<br>
 *       Properties of type {@link AttachedProperty} can be set on an instance of PropertyObject. An attached property
 *       is stored on the object instance as if it was a regular property, but it is usually defined on another class.
 *   <li>Shared access controller<br>
 *       All properties defined on a PropertyObject share the same access controller to reduce their memory footprint.
 *   <li>Dispatcher attachment<br>
 *       An instance of PropertyObject can be attached to a dispatcher, which will limit write access to threads
 *       associated with the dispatcher. For example, if a PropertyObject instance is attached to the platform
 *       dispatcher, properties defined on this object can only be modified on the JavaFX application thread. A property
 *       might choose to override this behavior by specifying a custom dispatcher as part of its property metadata.
 * </ul>
 */
@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class PropertyObject {

    public static class Accessor {
        public static void registerProperty(PropertyObject propertyObject, AsyncProperty property) {
            propertyObject.registerProperty(property);
        }
    }

    private final transient Listener invalidationListener = new Listener();
    private transient AsyncProperty[] properties = new AsyncProperty[getPropertyCount(getClass())];
    private transient List<ValueEntry> attachedValues;
    private transient List<InvalidationListener> listeners;
    private transient AccessControllerImpl accessController;
    private transient Dispatcher dispatcher;

    /** Gets the dispatcher that is associated with this instance. */
    public @Nullable Dispatcher getDispatcher() {
        return dispatcher;
    }

    /** Attaches the object to the specified dispatcher. An object can only be attached to a single dispatcher. */
    public void attachToDispatcher(Dispatcher dispatcher) {
        if (this.dispatcher != null) {
            throw new IllegalStateException("The object is already attached to a dispatcher.");
        }

        this.dispatcher = dispatcher;
    }

    /**
     * Detaches the object from its dispatcher. After calling this method, the object is free-threaded and can be
     * modified on any thread.
     */
    public void detachFromDispatcher() {
        this.dispatcher = null;
    }

    /**
     * Gets the value of the specified attached property.
     *
     * @throws IllegalArgumentException if the property was not defined on the current object.
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T getValue(AttachedProperty<T> key) {
        if (attachedValues != null) {
            for (ValueEntry entry : attachedValues) {
                if (entry.key == key) {
                    return (T)entry.value;
                }
            }
        }

        throw new IllegalArgumentException(String.format("Attached property value '%s' was not found.", key.getName()));
    }

    /** Gets the value of the specified attached property, returning a fallback value if the property was not found. */
    @SuppressWarnings("unchecked")
    public synchronized <T> T getValue(AttachedProperty<T> key, T fallbackValue) {
        if (attachedValues != null) {
            for (ValueEntry entry : attachedValues) {
                if (entry.key == key) {
                    return (T)entry.value;
                }
            }
        }

        return fallbackValue;
    }

    /** Sets the value of the specified attached property on the current object. */
    public synchronized <T> void setValue(AttachedProperty<T> key, T value) {
        if (attachedValues == null) {
            attachedValues = new ArrayList<>(1);
        }

        for (ValueEntry entry : attachedValues) {
            if (entry.key == key) {
                entry.value = value;
                return;
            }
        }

        ValueEntry entry = new ValueEntry();
        entry.key = key;
        entry.value = value;
        attachedValues.add(entry);
    }

    AccessControllerImpl getSharedAccessController() {
        if (accessController == null) {
            accessController = new AccessControllerImpl();
        }

        return accessController;
    }

    void addListener(InvalidationListener listener) {
        assert listener != null;

        synchronized (invalidationListener) {
            if (listeners == null) {
                listeners = new ArrayList<>(1);
            }

            listeners.add(listener);
        }
    }

    void removeListener(InvalidationListener listener) {
        synchronized (invalidationListener) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    @SuppressWarnings("ManualArrayCopy")
    void registerProperty(AsyncProperty property) {
        if (property instanceof SubObservable) {
            ((SubObservable)property).addListener(invalidationListener);
        } else {
            property.addListener(invalidationListener);
        }

        if (properties[properties.length - 1] != null) {
            properties = Arrays.copyOf(properties, properties.length + 1);
        }

        long uniqueId = property.getUniqueId();
        int index = findInsertIndex(uniqueId);
        for (int i = properties.length - 1; i > index; --i) {
            properties[i] = properties[i - 1];
        }

        properties[index] = property;
    }

    void validate() {
        for (AsyncProperty property : properties) {
            property.getValue();
        }
    }

    AsyncProperty[] getProperties() {
        return properties;
    }

    private int findInsertIndex(long serial) {
        int low = 0;
        int high = properties.length;

        while (high - low > 3) {
            int pivot = (high + low) / 2;

            AsyncProperty property = properties[pivot];
            if ((property == null || property.getUniqueId() > serial)
                    && (properties[pivot - 1] != null && properties[pivot - 1].getUniqueId() < serial)) {
                return pivot;
            }

            if (property == null && properties[pivot - 1] == null) {
                high = pivot;
            } else {
                low = pivot + 1;
            }
        }

        AsyncProperty property = properties[low];
        if (property == null || property.getUniqueId() > serial) {
            return low;
        }

        property = properties[low + 1];
        if (property == null || property.getUniqueId() > serial) {
            return low + 1;
        }

        return low + 2;
    }

    private static final Map<Class<?>, Integer> propertyCount = new HashMap<>();

    private static int getPropertyCount(Class<?> clazz) {
        synchronized (propertyCount) {
            Integer count = propertyCount.get(clazz);
            if (count != null) {
                return count;
            }

            int c = 0;
            Class<?> classCopy = clazz;
            while (classCopy != Object.class) {
                for (Field field : classCopy.getDeclaredFields()) {
                    if (!field.isSynthetic() && AsyncProperty.class.isAssignableFrom(field.getType())) {
                        ++c;
                    }
                }

                classCopy = classCopy.getSuperclass();
            }

            propertyCount.put(clazz, c);
            return c;
        }
    }

    private final class Listener implements InvalidationListener, SubInvalidationListener {
        @Override
        public void invalidated(Observable observable, boolean subInvalidation) {
            invalidated(observable);
        }

        @Override
        public void invalidated(Observable observable) {
            synchronized (this) {
                if (listeners != null) {
                    for (InvalidationListener listener : listeners) {
                        listener.invalidated(observable);
                    }
                }
            }
        }
    }

    private static final class ValueEntry {
        AttachedProperty<?> key;
        Object value;
    }

}
