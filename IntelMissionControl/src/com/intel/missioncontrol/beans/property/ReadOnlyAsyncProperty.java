/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.value.AsyncObservableValue;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface ReadOnlyAsyncProperty<T> extends AsyncObservableValue<T> {

    Object getBean();

    String getName();

}
