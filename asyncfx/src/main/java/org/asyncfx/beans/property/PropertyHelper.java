/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;
import org.asyncfx.concurrent.Futures;

public final class PropertyHelper {

    private static final AtomicLong uniqueIdCounter = new AtomicLong();
    private static final Map<Class<?>, List<Field>> classFields = new HashMap<>();

    /**
     * Sets the value of a property and returns a future that represents the progress of the operation. If the value of
     * the property cannot be set on the current thread, the operation will be dispatched to the appropriate executor
     * specified by the property's metadata.
     */
    public static <T> Future<Void> setValueAsync(AsyncProperty<T> property, T value) {
        PropertyMetadata.HasAccessDelegate hasAccessDelegate =
            PropertyMetadata.Accessor.getHasAccess(property.getMetadata());
        if (hasAccessDelegate == null || hasAccessDelegate.hasAccess()) {
            property.setValue(value);
            return Futures.successful();
        }

        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
        property.getMetadata()
            .getExecutor()
            .execute(
                () -> {
                    property.setValue(value);
                    futureCompletionSource.setResult(null);
                });

        return futureCompletionSource.getFuture();
    }

    /**
     * Sets the value of a property. If the value of the property cannot be set on the current thread, the operation
     * will be dispatched to the appropriate executor specified by the property's metadata. Otherwise, the operation
     * will run synchronously on the current thread.
     */
    public static <T> void setValueSafe(AsyncProperty<T> property, T value) {
        PropertyMetadata.HasAccessDelegate hasAccessDelegate =
            PropertyMetadata.Accessor.getHasAccess(property.getMetadata());
        if (hasAccessDelegate == null || hasAccessDelegate.hasAccess()) {
            property.setValue(value);
        } else {
            property.getMetadata().getExecutor().execute(() -> property.setValue(value));
        }
    }

    /**
     * Resets the value of a property as an asynchronous operation. If the value of the property cannot be reset on the
     * current thread, the operation will be dispatched to the appropriate executor specified by the property's
     * metadata.
     */
    public static <T> Future<Void> resetValueAsync(AsyncProperty<T> property) {
        PropertyMetadata.HasAccessDelegate hasAccessDelegate =
            PropertyMetadata.Accessor.getHasAccess(property.getMetadata());
        if (hasAccessDelegate == null || hasAccessDelegate.hasAccess()) {
            property.reset();
            return Futures.successful();
        }

        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
        property.getMetadata()
            .getExecutor()
            .execute(
                () -> {
                    property.reset();
                    futureCompletionSource.setResult(null);
                });

        return futureCompletionSource.getFuture();
    }

    /**
     * Resets the value of a property. If the value of the property cannot be reset on the current thread, the operation
     * will be dispatched to the appropriate executor specified by the property's metadata. Otherwise, the operation
     * will run synchronously on the current thread.
     */
    public static <T> void resetValueSafe(AsyncProperty<T> property) {
        PropertyMetadata.HasAccessDelegate hasAccessDelegate =
            PropertyMetadata.Accessor.getHasAccess(property.getMetadata());
        if (hasAccessDelegate == null || hasAccessDelegate.hasAccess()) {
            property.reset();
        } else {
            property.getMetadata().getExecutor().execute(property::reset);
        }
    }

    static <T> String getPropertyName(Object bean, AsyncProperty property, PropertyMetadata<T> metadata) {
        String metadataName = metadata.getName();
        if (metadataName != null) {
            return metadataName;
        }

        String reflectiveName = null;
        if (!metadata.isCustomBean()) {
            reflectiveName = getReflectivePropertyName(bean, property);
        }

        return reflectiveName;
    }

    static long getNextUniqueId() {
        return uniqueIdCounter.incrementAndGet();
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private static String getReflectivePropertyName(Object bean, AsyncProperty property) {
        try {
            Class<?> cls = bean.getClass();
            synchronized (bean) {
                List<Field> fields = classFields.get(cls);
                if (fields == null) {
                    fields = getAllFields(cls);
                    classFields.put(cls, fields);
                }

                for (Field field : fields) {
                    if (field.get(bean) == property) {
                        return field.getName();
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>(3);

        while (type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                fields.add(field);
            }

            type = type.getSuperclass();
        }

        return fields;
    }

}
