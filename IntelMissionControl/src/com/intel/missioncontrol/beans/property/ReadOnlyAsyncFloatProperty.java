/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import javafx.beans.binding.FloatExpression;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public abstract class ReadOnlyAsyncFloatProperty extends FloatExpression implements ReadOnlyAsyncProperty<Number> {}
