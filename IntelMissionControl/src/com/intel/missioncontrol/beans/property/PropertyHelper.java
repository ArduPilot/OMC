/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.EnvironmentOptions;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.diagnostics.Debugger;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHelper.class);
    private static final IntegerProperty totalTimeouts = new SimpleIntegerProperty();

    public static ReadOnlyIntegerProperty totalTimeoutsProperty() {
        return totalTimeouts;
    }

    public static int getTotalTimeouts() {
        return totalTimeouts.get();
    }

    public static void addTimeout() {
        Platform.runLater(() -> totalTimeouts.set(totalTimeouts.get() + 1));
    }

    public static boolean isVerifyPropertyAccessEnabled() {
        return EnvironmentOptions.VERIFY_PROPERTY_ACCESS || Debugger.isRunningTests();
    }

    public static int getEventHandlerTimeout() {
        return EnvironmentOptions.EVENT_HANDLER_TIMEOUT;
    }

    static void checkProperty(Object bean, AsyncProperty property, PropertyMetadata metadata) {
        if (bean instanceof PropertyMetadata) {
            throw new IllegalArgumentException("Invalid bean.");
        }

        if (!metadata.isCustomBean()) {
            if (bean == null
                    || bean.getClass().getName().startsWith("java.lang.")
                    || bean instanceof List
                    || bean instanceof Set) {
                throw new IllegalArgumentException("Invalid bean.");
            }
        }

        // Since the property name is evaluated lazily, the value of the "name" field is not visible in the debugger if
        // the getName() method has not been called before. In order to make debugging easier, this option eagerly calls
        // getName() on all new properties to evaluate the field.
        //
        if (EnvironmentOptions.EAGER_PROPERTY_NAME_EVALUATION) {
            Dispatcher.post((Runnable)property::getName);
        }
    }

    static <T> String getPropertyName(Object bean, AsyncProperty property, PropertyMetadata<T> metadata) {
        String metadataName = metadata.getName();
        String reflectiveName = null;
        if (!metadata.isCustomBean()) {
            reflectiveName = getReflectivePropertyName(bean, property);
        }

        return metadataName != null ? metadataName : reflectiveName;
    }

    private static String getReflectivePropertyName(Object bean, AsyncProperty property) {
        try {
            Class<?> cls = bean.getClass();
            while (cls != Object.class) {
                for (Field field : cls.getDeclaredFields()) {
                    if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    if (!field.canAccess(bean)) {
                        field.setAccessible(true);
                    }

                    if (field.get(bean) == property) {
                        return field.getName();
                    }
                }

                cls = cls.getSuperclass();
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

}
