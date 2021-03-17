/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.google.common.util.concurrent.ListenableFuture;
import javafx.beans.value.WritableStringValue;

public interface AsyncWritableStringValue extends AsyncWritableValue<String>, WritableStringValue {

    ListenableFuture<Void> setAsync(String value);

    @Override
    ListenableFuture<Void> setValueAsync(String value);

}
