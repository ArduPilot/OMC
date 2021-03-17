/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.intel.missioncontrol.PublishSource;
import java.util.Collection;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public abstract class LockedCollection<E> implements Collection<E>, AutoCloseable {
    abstract void closeInternal();

    public abstract void changeOwner(Thread thread);

    @Override
    public void close() {
        try {
            closeInternal();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
