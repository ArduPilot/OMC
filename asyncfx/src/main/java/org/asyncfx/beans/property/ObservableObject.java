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
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.AccessControllerImpl;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.SubObservable;

/** Base class for objects that support sub-property change notifications. */
@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ObservableObject {

    public static class Accessor {
        public static void registerProperty(ObservableObject observableObject, AsyncProperty property) {
            observableObject.registerProperty(property);
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

    private final transient Listener invalidationListener = new Listener();
    private transient AsyncProperty[] properties = new AsyncProperty[getPropertyCount(getClass())];
    private transient List<InvalidationListener> listeners;
    private transient AccessControllerImpl accessController;

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

}
