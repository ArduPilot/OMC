/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

public interface IVectorAccess {

    /**
     * Return the user object wrapped by element i
     *
     * @param i
     * @return
     */
    Object getUserObject(int i);

    /**
     * Insert allready wrapped object in container
     *
     * @param o
     * @param i
     */
    void insertWrapped(Object o, int i);

    /**
     * Remove wrapped object from container
     *
     * @param i
     */
    void removeWrapped(int i);
}
