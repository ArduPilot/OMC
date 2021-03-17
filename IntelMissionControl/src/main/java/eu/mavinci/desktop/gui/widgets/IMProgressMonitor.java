/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.widgets;

public interface IMProgressMonitor {
    void setProgressNote(String note, int progress);

    void setNote(String note);

    void setProgress(int nv);

    void close();

    boolean isCanceled();

    void setMaximum(final int m);
}
