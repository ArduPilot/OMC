/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.concurrent.Executor;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubInvalidationListener;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncSubObservableSet<E> extends AsyncObservableSet<E>, SubObservableSet<E> {

    void addListener(SubInvalidationListener listener, Executor executor);

}
