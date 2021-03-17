/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class that supports serialization using {@link SettingsManager}. This should be used for classes that contain
 * JavaFX properties. If a class is not annotated with this annotation, it must be in the {@link SettingsManager}'s list
 * of simple types in order to be processed. If a class implements the {@link ISettings} interface, it does not need to
 * be annotated with {@link Serializable}.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Serializable {}
