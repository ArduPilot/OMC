/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.value.WritableFloatValue;

public interface AsyncWritableFloatValue extends AsyncWritableNumberValue, WritableFloatValue {

    ListenableFuture<Void> setAsync(float value);

    @Override
    ListenableFuture<Void> setValueAsync(Number value);

}
