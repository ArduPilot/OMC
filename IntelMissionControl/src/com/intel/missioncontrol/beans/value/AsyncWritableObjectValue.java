/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.value.WritableObjectValue;

public interface AsyncWritableObjectValue<T> extends AsyncWritableValue<T>, WritableObjectValue<T> {

    ListenableFuture<Void> setAsync(T value);

    @Override
    ListenableFuture<Void> setValueAsync(T value);

}
