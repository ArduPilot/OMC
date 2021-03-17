/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.binding;

import com.intel.missioncontrol.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface ValueConverter<S, T> {

    T convert(S value);

}
