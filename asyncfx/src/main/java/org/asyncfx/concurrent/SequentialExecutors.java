/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;

public class SequentialExecutors {

    private static final Executor DIRECT = MoreExecutors.newSequentialExecutor(MoreExecutors.directExecutor());

    private static final Executor BACKGROUND = MoreExecutors.newSequentialExecutor(Dispatcher.background()::runLater);

    public static Executor sequentialDirectExecutor() {
        return DIRECT;
    }

    public static Executor sequentialBackgroundExecutor() {
        return BACKGROUND;
    }

}
