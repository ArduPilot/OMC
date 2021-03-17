/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerGuiClose extends IAirplaneListener {

    /**
     * Session will close notification
     *
     * <p>is fired AFTER Backend Connection lost, when the user manually closes the plane related GUI components The
     * user can do it directly, or mutch later, so we have this seperated event
     */
    public void guiClose();

    /** Pre Session close notification. this is voteable.. */
    public boolean guiCloseRequest();

    /**
     * The session will write its properties to a file soon, so the listener should transfair all its informations to
     * the session
     */
    public void storeToSessionNow();
}
