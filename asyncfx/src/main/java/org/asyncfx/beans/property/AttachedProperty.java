/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.ArrayList;
import java.util.List;

public final class AttachedProperty<T> {

    private static final List<AttachedProperty> registeredProperties = new ArrayList<>();

    private final String name;
    private final Class<T> type;

    private AttachedProperty(String name, Class<T> type) {
        synchronized (registeredProperties) {
            for (AttachedProperty key : registeredProperties) {
                if (key.getName().equals(name)) {
                    throw new IllegalArgumentException("Duplicate attached property: " + name);
                }
            }

            registeredProperties.add(this);
        }

        this.name = name;
        this.type = type;
    }

    public static <T> AttachedProperty<T> register(String name, Class<T> type) {
        return new AttachedProperty<>(name, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> AttachedProperty<T> find(String name) {
        synchronized (registeredProperties) {
            for (AttachedProperty key : registeredProperties) {
                if (key.getName().equals(name)) {
                    return key;
                }
            }

            throw new IllegalArgumentException("Attached property not found: " + name);
        }
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

}
