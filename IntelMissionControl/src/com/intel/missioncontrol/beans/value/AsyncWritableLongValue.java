/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.value.WritableLongValue;

public interface AsyncWritableLongValue extends AsyncWritableNumberValue, WritableLongValue {

    ListenableFuture<Void> setAsync(long value);

    @Override
    ListenableFuture<Void> setValueAsync(Number value);

}
