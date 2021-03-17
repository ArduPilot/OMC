/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.atteo.classindex.IndexAnnotated;

/**
 * Identifies a section in the settings file. Sections are identified by a name, and will be serialized and deserialized
 * automatically by SettingsManager. All fields of a section class (except those marked as transient) will be serialized
 * to disk. When a section class contains JavaFX properties, their underlying value will be serialized (instead of their
 * internal structure).
 */
@IndexAnnotated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SettingsMetadata {

    enum Scope {
        APPLICATION
    }

    String section();

    Scope scope() default Scope.APPLICATION;

}
