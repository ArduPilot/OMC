/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.value.WritableBooleanValue;

public interface AsyncWritableBooleanValue extends AsyncWritableValue<Boolean>, WritableBooleanValue {

    ListenableFuture<Void> setAsync(boolean value);

    @Override
    ListenableFuture<Void> setValueAsync(Boolean value);

}
