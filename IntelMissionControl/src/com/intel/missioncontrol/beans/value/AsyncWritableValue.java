/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.google.common.util.concurrent.ListenableFuture;
import com.intel.missioncontrol.PublishSource;
import javafx.beans.value.WritableValue;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncWritableValue<T> extends WritableValue<T> {

    ListenableFuture<Void> setValueAsync(T value);

}
