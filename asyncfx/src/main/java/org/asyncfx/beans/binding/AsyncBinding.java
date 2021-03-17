/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import org.asyncfx.PublishSource;
import org.asyncfx.beans.value.AsyncObservableValue;
import org.asyncfx.collections.AsyncObservableList;

@PublishSource(
        module = "openjfx",
        licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public interface AsyncBinding<T> extends AsyncObservableValue<T> {

    boolean isValid();

    void invalidate();

    AsyncObservableList<?> getDependencies();

    void dispose();

}
