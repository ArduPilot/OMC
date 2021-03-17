/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.management;

import eu.mavinci.core.plane.management.CAirport;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.core.IAppListener;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import java.util.LinkedList;
import java.util.logging.Level;

public class Airport extends CAirport {

    public static Airport getInstance() {
        return (Airport)me;
    }

    public static void setInstance(Airport newMe) {
        me = newMe;
    }

    private final AirportAppListener appListener = new AirportAppListener();

    public Airport() {
        Application.addApplicationListener(appListener);
    }

    private class AirportAppListener implements IAppListener {

        @SuppressWarnings("unchecked")
        @Override
        public void appIsClosing() {
            LinkedList<IAirplane> planesCpy = (LinkedList<IAirplane>)planes.clone();
            for (IAirplane plane : planesCpy) {
                plane.close();
                plane.fireGuiClose();
            }

            Debug.getLog().log(Level.FINER, "Close all sessions");
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean appRequestClosing() {
            Debug.getLog().log(Level.FINER, "Request Close to all planes");
            LinkedList<IAirplane> planesCpy = (LinkedList<IAirplane>)planes.clone();
            for (IAirplane plane : planesCpy) {
                if (!plane.fireGuiCloseRequest()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void guiReadyLoaded() {}

    }
}
