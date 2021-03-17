/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.sync;

import java.util.Iterator;

public abstract class AWrappedListSyncSourceHandler implements Iterator<Object> {

    public abstract Object getWrapperObjectForLastObject();

    @Override
    public void remove() {}
}
