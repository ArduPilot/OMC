/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.value;

import java.util.concurrent.Executor;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncSubObservable;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncSubObservableValue<T> extends SubObservableValue<T>, AsyncSubObservable {

    void addListener(SubChangeListener listener, Executor executor);

}
