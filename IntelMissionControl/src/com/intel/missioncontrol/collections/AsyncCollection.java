/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import java.util.Collection;

public interface AsyncCollection<E> extends Collection<E> {

    /**
     * Locks the collection for exclusive modification. The current thread will becomes the owner of the locked
     * collection. A locked collection can only be unlocked on the thread that owns the collection.
     */
    LockedCollection<E> lock();

}
