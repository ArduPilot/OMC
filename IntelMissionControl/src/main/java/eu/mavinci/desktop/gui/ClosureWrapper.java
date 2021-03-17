/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClosureWrapper {

    Object target = null;

    @SuppressWarnings("rawtypes")
    Consumer consumer = null;

    @SuppressWarnings("rawtypes")
    BiConsumer biConsumer = null;

    @SuppressWarnings("rawtypes")
    public ClosureWrapper(Object target, Consumer consumer) {
        this.target = target;
        this.consumer = consumer;
    }

    @SuppressWarnings("rawtypes")
    public ClosureWrapper(Object target, BiConsumer consumer) {
        this.target = target;
        this.biConsumer = consumer;
    }

    @SuppressWarnings("unchecked")
    public void execute(Object source) {
        if (consumer != null && target != null) {
            consumer.accept(target);
        } else if (biConsumer != null && target != null && source != null) {
            biConsumer.accept(target, source);
        }
    }
}
