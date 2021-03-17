/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.value;

import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubObservable;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface SubObservableValue<T> extends SubObservable {

    void addListener(SubChangeListener listener);

    void removeListener(SubChangeListener listener);

}
