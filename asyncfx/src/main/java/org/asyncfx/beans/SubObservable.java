/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans;

import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface SubObservable {

    void addListener(SubInvalidationListener listener);

    void removeListener(SubInvalidationListener listener);

}
