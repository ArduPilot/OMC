/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import eu.mavinci.core.flightplan.CFlightplanManager;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import eu.mavinci.core.listeners.IListenerManager;
import eu.mavinci.core.obfuscation.IKeepMethods;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;

public interface ICAirplane extends IListenerManager, IAirplaneConnector, IAirplaneExternal, IKeepMethods {

    boolean isSameSession(ICAirplane plane);

    /** Get the Handler for receiving data from the airplane */
    IAirplaneListenerDelegator getRootHandler();

    /** Requesting all Airplane parameter... */
    void requestAll();

    /** try reconnect to something from airport */
    void tryReconnect();

    /**
     * Get's a local cache with all relevant data
     *
     * @return
     */
    CAirplaneCache getAirplaneCache();

    /**
     * Get's the fligtplan Manager for this Plane
     *
     * @return
     */
    CFlightplanManager getFPmanager();

    /**
     * provides a direct access to the plane connector! Attention: don't store this locally, because it may change over
     * time!
     *
     * @return
     */
    IAirplaneConnector getAirplaneConnector();

    /**
     * set a new connection
     *
     * @param con
     */
    void setAirplaneConnector(IAirplaneConnector con);

    void unsetAirplaneConnector();

    boolean isAirplaneConnectorAvaliable();

    Class<? extends IAirplaneConnector> getAirplaneConnectorClass();

    boolean isSimulation();

    /** Inform Everyone that the plane releated Gui will close imediately */
    void fireGuiClose();

    /** Ask Everyone if the gui could close */
    boolean fireGuiCloseRequest();

    void fireStoreToSessionNow();

    /** @return description of the platform based on the information received from the real connected plane */
    IHardwareConfiguration getNativeHardwareConfiguration();

    void setNativeHardwareConfiguration(IHardwareConfiguration hardwareConfiguration);

}
