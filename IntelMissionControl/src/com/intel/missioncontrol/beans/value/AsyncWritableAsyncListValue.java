/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.collections.AsyncObservableList;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncWritableAsyncListValue<E>
        extends AsyncWritableObjectValue<AsyncObservableList<E>>, AsyncObservableList<E> {}
