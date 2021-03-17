/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

public interface IRecomputeRunnable extends Runnable {

    public void runLaterOnUIThread();
}
