/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.AsyncObservable;
import java.util.concurrent.Executor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncObservableValue<T> extends ObservableValue<T>, AsyncObservable {

    void addListener(ChangeListener<? super T> listener, Executor executor);

}
