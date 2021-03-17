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
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.AsyncFX;
import org.asyncfx.beans.AccessController;
import org.asyncfx.beans.binding.AsyncExpressionHelper;
import org.asyncfx.beans.binding.AsyncListExpressionHelper;
import org.asyncfx.beans.binding.AsyncSetExpressionHelper;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

public final class PropertyHelper {

    private static final AtomicLong uniqueIdCounter = new AtomicLong();
    private static final Map<Class<?>, List<Field>> classFields = new HashMap<>();

    /**
     * Sets the value of a property and returns a future that represents the progress of the operation. If the value of
     * the property cannot be set on the current thread, the operation will be dispatched to the appropriate executor
     * specified by the property's metadata.
     */
    public static <T> Future<Void> setValueAsync(AsyncProperty<T> property, T value) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
        property.getExecutor()
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
        property.getExecutor().execute(() -> property.setValue(value));
    }

    /**
     * Resets the value of a property as an asynchronous operation. If the value of the property cannot be reset on the
     * current thread, the operation will be dispatched to the appropriate executor specified by the property's
     * metadata.
     */
    public static <T> Future<Void> resetValueAsync(AsyncProperty<T> property) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
        property.getExecutor()
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
        property.getExecutor().execute(property::reset);
    }

    static Dispatcher verifyAccess(ReadOnlyAsyncProperty property, PropertyMetadata metadata) {
        Dispatcher metadataDispatcher = metadata.getDispatcher();
        if (metadataDispatcher != null) {
            if (AsyncFX.isVerifyPropertyAccess() && !metadataDispatcher.hasAccess()) {
                throw new IllegalStateException(
                    "Illegal cross-thread access: expected = "
                        + metadataDispatcher
                        + "; currentThread = "
                        + Dispatcher.fromThread(Thread.currentThread())
                        + ".");
            }

            return metadataDispatcher;
        } else {
            Object bean = property.getBean();
            Dispatcher dispatcher = bean instanceof PropertyObject ? ((PropertyObject)bean).getDispatcher() : null;

            if (AsyncFX.isVerifyPropertyAccess() && dispatcher != null && !dispatcher.hasAccess()) {
                throw new IllegalStateException(
                    "Illegal cross-thread access: expected = "
                        + dispatcher
                        + "; currentThread = "
                        + Dispatcher.fromThread(Thread.currentThread())
                        + ".");
            }

            return dispatcher;
        }
    }

    static void verifyConsistency(PropertyMetadata metadata) {
        if (!AsyncFX.isVerifyPropertyAccess()) {
            return;
        }

        ConsistencyGroup consistencyGroup = metadata.getConsistencyGroup();
        if (consistencyGroup != null) {
            for (ReadOnlyAsyncProperty property : consistencyGroup.getProperties()) {
                if (!property.getAccessController().isLocked()) {
                    throw new IllegalStateException(
                        "Illegal access: property is part of a consistency group"
                            + " and can only be accessed within a critical section.");
                }
            }
        }
    }

    static <T> String getPropertyName(Object bean, AsyncProperty property, PropertyMetadata<T> metadata) {
        String metadataName = metadata.getName();
        if (metadataName != null) {
            return metadataName;
        }

        String reflectiveName = null;
        if (bean != null && !metadata.isCustomBean()) {
            reflectiveName = getReflectivePropertyName(bean, property);
        }

        return reflectiveName;
    }

    static long getNextUniqueId() {
        return uniqueIdCounter.incrementAndGet();
    }

    @SuppressWarnings("unchecked")
    static void addListener(
            ObservableValue property, InvalidationListener listener, AccessController accessController) {
        if (!(property instanceof ReadOnlyAsyncProperty)
                || ((ReadOnlyAsyncProperty)property).getAccessController() != accessController) {
            property.addListener(listener);
            return;
        }

        if (property instanceof AsyncObjectPropertyBaseImpl) {
            AsyncObjectPropertyBaseImpl propertyImpl = ((AsyncObjectPropertyBaseImpl)property);
            propertyImpl.helper =
                AsyncExpressionHelper.addListener(propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncBooleanPropertyBaseImpl) {
            AsyncBooleanPropertyBaseImpl propertyImpl = ((AsyncBooleanPropertyBaseImpl)property);
            propertyImpl.helper =
                AsyncExpressionHelper.addListener(propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncIntegerPropertyBaseImpl) {
            AsyncIntegerPropertyBaseImpl propertyImpl = ((AsyncIntegerPropertyBaseImpl)property);
            propertyImpl.helper =
                AsyncExpressionHelper.addListener(propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncLongPropertyBaseImpl) {
            AsyncLongPropertyBaseImpl propertyImpl = ((AsyncLongPropertyBaseImpl)property);
            propertyImpl.helper =
                AsyncExpressionHelper.addListener(propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncFloatPropertyBaseImpl) {
            AsyncFloatPropertyBaseImpl propertyImpl = ((AsyncFloatPropertyBaseImpl)property);
            propertyImpl.helper =
                AsyncExpressionHelper.addListener(propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncDoublePropertyBaseImpl) {
            AsyncDoublePropertyBaseImpl propertyImpl = ((AsyncDoublePropertyBaseImpl)property);
            propertyImpl.helper =
                AsyncExpressionHelper.addListener(propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncStringPropertyBaseImpl) {
            AsyncStringPropertyBaseImpl propertyImpl = ((AsyncStringPropertyBaseImpl)property);
            propertyImpl.helper =
                AsyncExpressionHelper.addListener(propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncListPropertyBase) {
            AsyncListPropertyBase propertyImpl = ((AsyncListPropertyBase)property);
            propertyImpl.helper =
                AsyncListExpressionHelper.addListener(
                    propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncSetPropertyBase) {
            AsyncSetPropertyBase propertyImpl = ((AsyncSetPropertyBase)property);
            propertyImpl.helper =
                AsyncSetExpressionHelper.addListener(
                    propertyImpl.helper, propertyImpl, propertyImpl.getCore(), listener);
        } else {
            throw new IllegalArgumentException("property");
        }
    }

    @SuppressWarnings("unchecked")
    static void removeListener(
            ObservableValue property, InvalidationListener listener, AccessController accessController) {
        if (!(property instanceof ReadOnlyAsyncProperty)
                || ((ReadOnlyAsyncProperty)property).getAccessController() != accessController) {
            property.removeListener(listener);
            return;
        }

        if (property instanceof AsyncObjectPropertyBaseImpl) {
            AsyncObjectPropertyBaseImpl propertyImpl = ((AsyncObjectPropertyBaseImpl)property);
            propertyImpl.helper = AsyncExpressionHelper.removeListener(propertyImpl.helper, listener);
        } else if (property instanceof AsyncBooleanPropertyBaseImpl) {
            AsyncBooleanPropertyBaseImpl propertyImpl = ((AsyncBooleanPropertyBaseImpl)property);
            propertyImpl.helper = AsyncExpressionHelper.removeListener(propertyImpl.helper, listener);
        } else if (property instanceof AsyncIntegerPropertyBaseImpl) {
            AsyncIntegerPropertyBaseImpl propertyImpl = ((AsyncIntegerPropertyBaseImpl)property);
            propertyImpl.helper = AsyncExpressionHelper.removeListener(propertyImpl.helper, listener);
        } else if (property instanceof AsyncLongPropertyBaseImpl) {
            AsyncLongPropertyBaseImpl propertyImpl = ((AsyncLongPropertyBaseImpl)property);
            propertyImpl.helper = AsyncExpressionHelper.removeListener(propertyImpl.helper, listener);
        } else if (property instanceof AsyncFloatPropertyBaseImpl) {
            AsyncFloatPropertyBaseImpl propertyImpl = ((AsyncFloatPropertyBaseImpl)property);
            propertyImpl.helper = AsyncExpressionHelper.removeListener(propertyImpl.helper, listener);
        } else if (property instanceof AsyncDoublePropertyBaseImpl) {
            AsyncDoublePropertyBaseImpl propertyImpl = ((AsyncDoublePropertyBaseImpl)property);
            propertyImpl.helper = AsyncExpressionHelper.removeListener(propertyImpl.helper, listener);
        } else if (property instanceof AsyncStringPropertyBaseImpl) {
            AsyncStringPropertyBaseImpl propertyImpl = ((AsyncStringPropertyBaseImpl)property);
            propertyImpl.helper = AsyncExpressionHelper.removeListener(propertyImpl.helper, listener);
        } else if (property instanceof AsyncListPropertyBase) {
            AsyncListPropertyBase propertyImpl = ((AsyncListPropertyBase)property);
            propertyImpl.helper =
                AsyncListExpressionHelper.removeListener(propertyImpl.helper, propertyImpl.getCore(), listener);
        } else if (property instanceof AsyncSetPropertyBase) {
            AsyncSetPropertyBase propertyImpl = ((AsyncSetPropertyBase)property);
            propertyImpl.helper =
                AsyncSetExpressionHelper.removeListener(propertyImpl.helper, propertyImpl.getCore(), listener);
        } else {
            throw new IllegalArgumentException("property");
        }
    }

    static Object getValueUncritical(ReadOnlyAsyncProperty property, AccessController accessController) {
        if (property.getAccessController() != accessController) {
            return property.getValueUncritical();
        }

        if (property instanceof AsyncObjectPropertyBaseImpl) {
            return ((AsyncObjectPropertyBaseImpl)property).getCore();
        } else if (property instanceof AsyncBooleanPropertyBaseImpl) {
            return ((AsyncBooleanPropertyBaseImpl)property).getCore();
        } else if (property instanceof AsyncIntegerPropertyBaseImpl) {
            return ((AsyncIntegerPropertyBaseImpl)property).getCore();
        } else if (property instanceof AsyncLongPropertyBaseImpl) {
            return ((AsyncLongPropertyBaseImpl)property).getCore();
        } else if (property instanceof AsyncFloatPropertyBaseImpl) {
            return ((AsyncFloatPropertyBaseImpl)property).getCore();
        } else if (property instanceof AsyncDoublePropertyBaseImpl) {
            return ((AsyncDoublePropertyBaseImpl)property).getCore();
        } else if (property instanceof AsyncStringPropertyBaseImpl) {
            return ((AsyncStringPropertyBaseImpl)property).getCore();
        } else if (property instanceof AsyncListPropertyBase) {
            return ((AsyncListPropertyBase)property).getCore();
        } else if (property instanceof AsyncSetPropertyBase) {
            return ((AsyncSetPropertyBase)property).getCore();
        }

        throw new IllegalArgumentException("property");
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
