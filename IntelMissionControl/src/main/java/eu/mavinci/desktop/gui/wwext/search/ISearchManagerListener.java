/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

/**
 * Everyone who implement this could informed about changing of the search results
 *
 * @author colman
 */
public interface ISearchManagerListener {

    /** called after the results are retrieved this allows the tree to be reconstructed and updated in the display */
    public void searchResultChanged();
}
