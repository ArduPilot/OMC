/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.core;

@Deprecated(forRemoval = true)
public interface IAppListener {
    /**
     * voteable pre close notification
     *
     * @return true = this listeners allows the gui closing, otherwise gui will not close!
     */
    public boolean appRequestClosing();

    /** non voteable pre close notification */
    public void appIsClosing();

    /** thrown after visibility of the MainWindow */
    public void guiReadyLoaded();

}
