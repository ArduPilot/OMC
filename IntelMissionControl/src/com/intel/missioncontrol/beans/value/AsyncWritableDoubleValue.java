/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.value.WritableDoubleValue;

public interface AsyncWritableDoubleValue extends AsyncWritableNumberValue, WritableDoubleValue {

    ListenableFuture<Void> setAsync(double value);

    @Override
    ListenableFuture<Void> setValueAsync(Number value);

}
