/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.listeners;

public interface IListenerManager {
    /**
     * Add listener to the listener Delegator
     *
     * <p>The listener will be stored with a weak reference, so it has to be referenced somewhere else, otherwise it
     * will be removed implecitely directly. This behaviour prevents memory leaks, because otherwise closes gui
     * components wont be deleted, if they are regitered here as listener
     *
     * @param l
     */
    public void addListener(IListener l);

    /**
     * Add listerener at the second plance in the listener list. the fist place should be the airplane cache..
     *
     * <p>The listener will be stored with a weak reference, so it has to be referenced somewhere else, otherwise it
     * will be removed implecitely directly. This behaviour prevents memory leaks, because otherwise closes gui
     * components wont be deleted, if they are regitered here as listener
     *
     * @param l
     */
    public void addListenerAtBegin(IListener l);

    /**
     * Remove listener to the listener Delegator. They are also removed implicitely if no reference outside of this
     * delegator is existing to the listener.
     */
    public void removeListener(IListener l);

}
