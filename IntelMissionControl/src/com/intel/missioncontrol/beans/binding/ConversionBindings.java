/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.binding;

import com.intel.missioncontrol.PublishSource;
import java.util.List;
import java.util.Set;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ConversionBindings {

    public static <T, U> void bindBidirectional(
            Property<T> property1, Property<U> property2, BidirectionalValueConverter<U, T> converter) {
        ConvertingBidirectionalBinding.bind(property1, property2, converter);
    }

    public static <T, U> void unbindBidirectional(Property<T> property1, Property<U> property2) {
        ConvertingBidirectionalBinding.unbind(property1, property2);
    }

    public static <T, U> void bindContent(List<T> target, ObservableList<U> source, ValueConverter<U, T> converter) {
        ConvertingContentBinding.bindContent(target, source, converter);
    }

    public static <T, U> void bindContent(Set<T> target, ObservableSet<U> source, ValueConverter<U, T> converter) {
        ConvertingContentBinding.bindContent(target, source, converter);
    }

    public static <T, U> void unbindContent(List<T> target, ObservableList<U> source) {
        ConvertingContentBinding.unbindContent(target, source);
    }

    public static <T, U> void unbindContent(Set<T> target, ObservableSet<U> source) {
        ConvertingContentBinding.unbindContent(target, source);
    }

}
