/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

public class Optional<T> {

    private final T value;
    private final boolean isPresent;

    private Optional() {
        this.value = null;
        this.isPresent = false;
    }

    private Optional(T value) {
        this.value = value;
        this.isPresent = true;
    }

    public static <T> Optional<T> empty() {
        return new Optional<>();
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public boolean isPresent() {
        return isPresent;
    }

    public T get() {
        return value;
    }

    public T orElse(T value) {
        return isPresent ? this.value : value;
    }

}
