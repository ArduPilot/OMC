/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.binding;

import org.asyncfx.PublishSource;

@PublishSource(
    module = "openjfx",
    licenses = {"intel-gpl-classpath-exception"}
)
public interface BidirectionalValueConverter<T, U> extends ValueConverter<T, U> {

    T convertBack(U value);

}
