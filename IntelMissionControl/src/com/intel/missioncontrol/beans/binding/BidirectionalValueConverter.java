/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.binding;

import com.intel.missioncontrol.PublishSource;

@PublishSource(
    module = "openjfx",
    licenses = {"intel-gpl-classpath-exception"}
)
public interface BidirectionalValueConverter<T, U> extends ValueConverter<T, U> {

    T convertBack(U value);

}
