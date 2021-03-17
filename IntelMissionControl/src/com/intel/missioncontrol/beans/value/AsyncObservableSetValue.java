/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import javafx.beans.value.ObservableObjectValue;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncObservableSetValue<E>
        extends ObservableObjectValue<AsyncObservableSet<E>>, AsyncObservableSet<E> {}
