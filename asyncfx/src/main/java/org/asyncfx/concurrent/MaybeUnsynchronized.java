/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Indicates that, when this method is called, the object instance might not be synchronized on 'this'. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@interface MaybeUnsynchronized {}
