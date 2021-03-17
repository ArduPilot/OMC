/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

/**
 * Every message what REALY was send by the backend to this class
 *
 * @author marco
 */
public interface IAirplaneListenerAllExternal
        extends IAirplaneListenerBackendConnectionLost,
            IAirplaneListenerConfig,
            IAirplaneListenerFlightphase,
            IAirplaneListenerFlightPlanASM,
            IAirplaneListenerFlightPlanXML,
            IAirplaneListenerHealth,
            IAirplaneListenerIsSimulation,
            IAirplaneListenerMsg,
            IAirplaneListenerName,
            IAirplaneListenerPhotoLogName,
            IAirplaneListenerOrientation,
            IAirplaneListenerBackend,
            IAirplaneListenerPosition,
            IAirplaneListenerSimulationSettings,
            IAirplaneListenerStartPos,
            IAirplaneListenerFixedOrientation,
            IAirplaneListenerRawBackendStrings,
            IAirplaneListenerPhoto,
            IAirplaneListenerConnectionEstablished,
            IAirplaneListenerPowerOn,
            IAirplaneListenerPlaneInfo,
            IAirplaneListenerAndroidState,
            IAirplaneListenerLinkInfo,
            IAirplaneListenerPing,
            IAirplaneListenerFileTransfer,
            IAirplaneListenerDebug,
            IAirplaneListenerPositionOrientation,
            IAirplaneListenerExpertSimulatedFails,
            IAirplaneFlightPlanSendingListener,
            IAirplaneParamsUpdateListener {}
