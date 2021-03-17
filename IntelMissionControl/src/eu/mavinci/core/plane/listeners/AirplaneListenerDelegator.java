/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.CommandResultData;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.LinkInfo;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.LinkInfo;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;

import java.util.logging.Level;

public class AirplaneListenerDelegator implements IAirplaneListenerDelegator {

    protected WeakListenerList<IAirplaneListenerAllExternal> listenerIAirplaneListenerAllExternal =
        new WeakListenerList<IAirplaneListenerAllExternal>("listenerIAirplaneListenerAllExternal");
    protected WeakListenerList<IAirplaneListenerLoggingStateChanged> listenerIAirplaneListenerLoggingStateChanged =
        new WeakListenerList<IAirplaneListenerLoggingStateChanged>("listenerIAirplaneListenerLoggingStateChanged");
    protected WeakListenerList<IAirplaneListenerGuiClose> listenerIAirplaneListenerGuiClose =
        new WeakListenerList<IAirplaneListenerGuiClose>("listenerIAirplaneListenerGuiClose");
    protected WeakListenerList<IAirplaneListenerConnectionState> listenerIAirplaneListenerConnectionState =
        new WeakListenerList<IAirplaneListenerConnectionState>("listenerIAirplaneListenerConnectionState");
    protected WeakListenerList<IAirplaneListenerLogReplay> listenerIAirplaneListenerLogReplay =
        new WeakListenerList<IAirplaneListenerLogReplay>("listenerIAirplaneListenerLogReplay");
    protected WeakListenerList<IAirplaneListenerBackendConnectionLost> listenerIAirplaneListenerBackendConnectionLost =
        new WeakListenerList<IAirplaneListenerBackendConnectionLost>("listenerIAirplaneListenerBackendConnectionLost");
    protected WeakListenerList<IAirplaneListenerConfig> listenerIAirplaneListenerConfig =
        new WeakListenerList<IAirplaneListenerConfig>("listenerIAirplaneListenerConfig");
    protected WeakListenerList<IAirplaneListenerFlightphase> listenerIAirplaneListenerFlightphase =
        new WeakListenerList<IAirplaneListenerFlightphase>("listenerIAirplaneListenerFlightphase");
    protected WeakListenerList<IAirplaneListenerFlightPlanASM> listenerIAirplaneListenerFlightPlanASM =
        new WeakListenerList<IAirplaneListenerFlightPlanASM>("listenerIAirplaneListenerFlightPlanASM");
    protected WeakListenerList<IAirplaneListenerFlightPlanXML> listenerIAirplaneListenerFlightPlanXML =
        new WeakListenerList<IAirplaneListenerFlightPlanXML>("listenerIAirplaneListenerFlightPlanXML");
    protected WeakListenerList<IAirplaneListenerHealth> listenerIAirplaneListenerHealth =
        new WeakListenerList<IAirplaneListenerHealth>("listenerIAirplaneListenerHealth");
    protected WeakListenerList<IAirplaneListenerIsSimulation> listenerIAirplaneListenerIsSimulation =
        new WeakListenerList<IAirplaneListenerIsSimulation>("listenerIAirplaneListenerIsSimulation");
    protected WeakListenerList<IAirplaneListenerMsg> listenerIAirplaneListenerMsg =
        new WeakListenerList<IAirplaneListenerMsg>("listenerIAirplaneListenerMsg");
    protected WeakListenerList<IAirplaneListenerName> listenerIAirplaneListenerName =
        new WeakListenerList<IAirplaneListenerName>("listenerIAirplaneListenerName");
    protected WeakListenerList<IAirplaneListenerPhotoLogName> listenerIAirplaneListenerPhotoLogName =
        new WeakListenerList<IAirplaneListenerPhotoLogName>("listenerIAirplaneListenerPhotoLogName");
    protected WeakListenerList<IAirplaneListenerOrientation> listenerIAirplaneListenerOrientation =
        new WeakListenerList<IAirplaneListenerOrientation>("listenerIAirplaneListenerOrientation");
    protected WeakListenerList<IAirplaneListenerBackend> listenerIAirplaneListenerPorts =
        new WeakListenerList<IAirplaneListenerBackend>("listenerIAirplaneListenerPorts");
    protected WeakListenerList<IAirplaneListenerPosition> listenerIAirplaneListenerPosition =
        new WeakListenerList<IAirplaneListenerPosition>("listenerIAirplaneListenerPosition");
    protected WeakListenerList<IAirplaneListenerSimulationSettings> listenerIAirplaneListenerSimulationSettings =
        new WeakListenerList<IAirplaneListenerSimulationSettings>("listenerIAirplaneListenerSimulationSettings");
    protected WeakListenerList<IAirplaneListenerStartPos> listenerIAirplaneListenerStartPos =
        new WeakListenerList<IAirplaneListenerStartPos>("listenerIAirplaneListenerStartPos");
    protected WeakListenerList<IAirplaneListenerFixedOrientation> listenerIAirplaneListenerFixedOrientation =
        new WeakListenerList<IAirplaneListenerFixedOrientation>("listenerIAirplaneListenerFixedOrientation");
    protected WeakListenerList<IAirplaneListenerRawBackendStrings> listenerIAirplaneListenerRawBackendStrings =
        new WeakListenerList<IAirplaneListenerRawBackendStrings>("listenerIAirplaneListenerRawBackendStrings");
    protected WeakListenerList<IAirplaneListenerPhoto> listenerIAirplaneListenerPhoto =
        new WeakListenerList<IAirplaneListenerPhoto>("listenerIAirplaneListenerPhoto");
    protected WeakListenerList<IAirplaneListenerConnectionEstablished> listenerIAirplaneListenerConnectionEstablished =
        new WeakListenerList<IAirplaneListenerConnectionEstablished>("listenerIAirplaneListenerConnectionEstablished");
    protected WeakListenerList<IAirplaneListenerPowerOn> listenerIAirplaneListenerPowerOn =
        new WeakListenerList<IAirplaneListenerPowerOn>("listenerIAirplaneListenerPowerOn");
    protected WeakListenerList<IAirplaneListenerPlaneInfo> listenerIAirplaneListenerPlaneInfo =
        new WeakListenerList<IAirplaneListenerPlaneInfo>("listenerIAirplaneListenerPlaneInfo");
    protected WeakListenerList<IAirplaneListenerAndroidState> listenerIAirplaneListenerAndroidState =
        new WeakListenerList<IAirplaneListenerAndroidState>("listenerIAirplaneListenerAndroidState");
    protected WeakListenerList<IAirplaneListenerLinkInfo> listenerIAirplaneListenerLinkInfo =
        new WeakListenerList<IAirplaneListenerLinkInfo>("listenerIAirplaneListenerLinkInfo");
    protected WeakListenerList<IAirplaneListenerPing> listenerIAirplaneListenerPing =
        new WeakListenerList<IAirplaneListenerPing>("listenerIAirplaneListenerPing");
    protected WeakListenerList<IAirplaneListenerFileTransfer> listenerIAirplaneListenerFileTransfer =
        new WeakListenerList<IAirplaneListenerFileTransfer>("listenerIAirplaneListenerFileTransfer");
    protected WeakListenerList<IAirplaneListenerDebug> listenerIAirplaneListenerDebug =
        new WeakListenerList<IAirplaneListenerDebug>("listenerIAirplaneListenerDebug");
    protected WeakListenerList<IAirplaneListenerPositionOrientation> listenerIAirplaneListenerPositionOrientation =
        new WeakListenerList<IAirplaneListenerPositionOrientation>("listenerIAirplaneListenerPositionOrientation");
    protected WeakListenerList<IAirplaneListenerExpertSimulatedFails> listenerIAirplaneListenerExpertSimulatedFails =
        new WeakListenerList<IAirplaneListenerExpertSimulatedFails>("listenerIAirplaneListenerExpertSimulatedFails");
    protected WeakListenerList<IAirplaneFlightPlanSendingListener> listenerIAirplaneFlightPlanSendingListener =
        new WeakListenerList<IAirplaneFlightPlanSendingListener>("listenerIAirplaneFlightPlanSendingListener");
    protected WeakListenerList<IAirplaneParamsUpdateListener> listenerIAirplaneParamsUpdateListener =
        new WeakListenerList<IAirplaneParamsUpdateListener>("listenerIAirplaneParamsUpdateListener");
    protected WeakListenerList<ICommandListenerResult> listenerICommandResultListener =
            new WeakListenerList<ICommandListenerResult>("listenerICommandResultListener");


    protected WeakListenerList<?>[] listenersAll = {
        listenerIAirplaneListenerAllExternal, listenerIAirplaneListenerLoggingStateChanged,
            listenerIAirplaneListenerGuiClose,
        listenerIAirplaneListenerConnectionState, listenerIAirplaneListenerLogReplay,
            listenerIAirplaneListenerBackendConnectionLost,
        listenerIAirplaneListenerConfig, listenerIAirplaneListenerFlightphase, listenerIAirplaneListenerFlightPlanASM,
        listenerIAirplaneListenerFlightPlanXML, listenerIAirplaneListenerHealth, listenerIAirplaneListenerIsSimulation,
        listenerIAirplaneListenerMsg, listenerIAirplaneListenerName, listenerIAirplaneListenerPhotoLogName,
        listenerIAirplaneListenerOrientation, listenerIAirplaneListenerPorts, listenerIAirplaneListenerPosition,
        listenerIAirplaneListenerSimulationSettings, listenerIAirplaneListenerStartPos,
            listenerIAirplaneListenerFixedOrientation,
        listenerIAirplaneListenerRawBackendStrings, listenerIAirplaneListenerPhoto,
            listenerIAirplaneListenerConnectionEstablished,
        listenerIAirplaneListenerPowerOn, listenerIAirplaneListenerPlaneInfo, listenerIAirplaneListenerAndroidState,
        listenerIAirplaneListenerLinkInfo, listenerIAirplaneListenerPing, listenerIAirplaneListenerFileTransfer,
        listenerIAirplaneListenerPositionOrientation, listenerIAirplaneListenerDebug,
            listenerIAirplaneListenerExpertSimulatedFails,
        listenerIAirplaneFlightPlanSendingListener, listenerIAirplaneParamsUpdateListener, listenerICommandResultListener
    };

    public void addListener(IListener l) {
        if (l instanceof IAirplaneListenerLoggingStateChanged) {
            listenerIAirplaneListenerLoggingStateChanged.add((IAirplaneListenerLoggingStateChanged)l);
        }

        if (l instanceof IAirplaneListenerGuiClose) {
            listenerIAirplaneListenerGuiClose.add((IAirplaneListenerGuiClose)l);
        }

        if (l instanceof IAirplaneListenerConnectionState) {
            listenerIAirplaneListenerConnectionState.add((IAirplaneListenerConnectionState)l);
        }

        if (l instanceof IAirplaneListenerLogReplay) {
            listenerIAirplaneListenerLogReplay.add((IAirplaneListenerLogReplay)l);
        }

        if (l instanceof IAirplaneListenerBackendConnectionLost) {
            listenerIAirplaneListenerBackendConnectionLost.add((IAirplaneListenerBackendConnectionLost)l);
        }

        if (l instanceof IAirplaneListenerConfig) {
            listenerIAirplaneListenerConfig.add((IAirplaneListenerConfig)l);
        }

        if (l instanceof IAirplaneListenerFlightphase) {
            listenerIAirplaneListenerFlightphase.add((IAirplaneListenerFlightphase)l);
        }

        if (l instanceof IAirplaneListenerFlightPlanASM) {
            listenerIAirplaneListenerFlightPlanASM.add((IAirplaneListenerFlightPlanASM)l);
        }

        if (l instanceof IAirplaneListenerFlightPlanXML) {
            listenerIAirplaneListenerFlightPlanXML.add((IAirplaneListenerFlightPlanXML)l);
        }

        if (l instanceof IAirplaneListenerHealth) {
            listenerIAirplaneListenerHealth.add((IAirplaneListenerHealth)l);
        }

        if (l instanceof IAirplaneListenerIsSimulation) {
            listenerIAirplaneListenerIsSimulation.add((IAirplaneListenerIsSimulation)l);
        }

        if (l instanceof IAirplaneListenerMsg) {
            listenerIAirplaneListenerMsg.add((IAirplaneListenerMsg)l);
        }

        if (l instanceof IAirplaneListenerName) {
            listenerIAirplaneListenerName.add((IAirplaneListenerName)l);
        }

        if (l instanceof IAirplaneListenerPhotoLogName) {
            listenerIAirplaneListenerPhotoLogName.add((IAirplaneListenerPhotoLogName)l);
        }

        if (l instanceof IAirplaneListenerFlightphase) {
            listenerIAirplaneListenerFlightphase.add((IAirplaneListenerFlightphase)l);
        }

        if (l instanceof IAirplaneListenerOrientation) {
            listenerIAirplaneListenerOrientation.add((IAirplaneListenerOrientation)l);
        }

        if (l instanceof IAirplaneListenerBackend) {
            listenerIAirplaneListenerPorts.add((IAirplaneListenerBackend)l);
        }

        if (l instanceof IAirplaneListenerPositionOrientation) {
            listenerIAirplaneListenerPositionOrientation.add((IAirplaneListenerPositionOrientation)l);
        }

        if (l instanceof IAirplaneListenerDebug) {
            listenerIAirplaneListenerDebug.add((IAirplaneListenerDebug)l);
        }

        if (l instanceof IAirplaneListenerPosition) {
            listenerIAirplaneListenerPosition.add((IAirplaneListenerPosition)l);
        }

        if (l instanceof IAirplaneListenerSimulationSettings) {
            listenerIAirplaneListenerSimulationSettings.add((IAirplaneListenerSimulationSettings)l);
        }

        if (l instanceof IAirplaneListenerStartPos) {
            listenerIAirplaneListenerStartPos.add((IAirplaneListenerStartPos)l);
        }

        if (l instanceof IAirplaneListenerFixedOrientation) {
            listenerIAirplaneListenerFixedOrientation.add((IAirplaneListenerFixedOrientation)l);
        }

        if (l instanceof IAirplaneListenerRawBackendStrings) {
            listenerIAirplaneListenerRawBackendStrings.add((IAirplaneListenerRawBackendStrings)l);
        }

        if (l instanceof IAirplaneListenerPhoto) {
            listenerIAirplaneListenerPhoto.add((IAirplaneListenerPhoto)l);
        }

        if (l instanceof IAirplaneListenerConnectionEstablished) {
            listenerIAirplaneListenerConnectionEstablished.add((IAirplaneListenerConnectionEstablished)l);
        }

        if (l instanceof IAirplaneListenerPowerOn) {
            listenerIAirplaneListenerPowerOn.add((IAirplaneListenerPowerOn)l);
        }

        if (l instanceof IAirplaneListenerPlaneInfo) {
            listenerIAirplaneListenerPlaneInfo.add((IAirplaneListenerPlaneInfo)l);
        }

        if (l instanceof IAirplaneListenerAndroidState) {
            listenerIAirplaneListenerAndroidState.add((IAirplaneListenerAndroidState)l);
        }

        if (l instanceof IAirplaneListenerLinkInfo) {
            listenerIAirplaneListenerLinkInfo.add((IAirplaneListenerLinkInfo)l);
        }

        if (l instanceof IAirplaneFlightPlanSendingListener) {
            listenerIAirplaneFlightPlanSendingListener.add((IAirplaneFlightPlanSendingListener)l);
        }

        if (l instanceof IAirplaneListenerPing) {
            listenerIAirplaneListenerPing.add((IAirplaneListenerPing)l);
        }

        if (l instanceof IAirplaneListenerFileTransfer) {
            listenerIAirplaneListenerFileTransfer.add((IAirplaneListenerFileTransfer)l);
        }

        if (l instanceof IAirplaneListenerExpertSimulatedFails) {
            listenerIAirplaneListenerExpertSimulatedFails.add((IAirplaneListenerExpertSimulatedFails)l);
        }

        if (l instanceof IAirplaneParamsUpdateListener) {
            listenerIAirplaneParamsUpdateListener.add((IAirplaneParamsUpdateListener)l);
        }

        if (l instanceof ICommandListenerResult) {
            listenerICommandResultListener.add((ICommandListenerResult)l);
        }
    }

    public void addListenerAtBegin(IListener l) {
        if (l instanceof IAirplaneListenerLoggingStateChanged) {
            listenerIAirplaneListenerLoggingStateChanged.addAtBegin((IAirplaneListenerLoggingStateChanged)l);
        }

        if (l instanceof IAirplaneListenerGuiClose) {
            listenerIAirplaneListenerGuiClose.addAtBegin((IAirplaneListenerGuiClose)l);
        }

        if (l instanceof IAirplaneListenerConnectionState) {
            listenerIAirplaneListenerConnectionState.addAtBegin((IAirplaneListenerConnectionState)l);
        }

        if (l instanceof IAirplaneListenerLogReplay) {
            listenerIAirplaneListenerLogReplay.addAtBegin((IAirplaneListenerLogReplay)l);
        }

        if (l instanceof IAirplaneListenerBackendConnectionLost) {
            listenerIAirplaneListenerBackendConnectionLost.addAtBegin((IAirplaneListenerBackendConnectionLost)l);
        }

        if (l instanceof IAirplaneListenerConfig) {
            listenerIAirplaneListenerConfig.addAtBegin((IAirplaneListenerConfig)l);
        }

        if (l instanceof IAirplaneListenerFlightphase) {
            listenerIAirplaneListenerFlightphase.addAtBegin((IAirplaneListenerFlightphase)l);
        }

        if (l instanceof IAirplaneListenerFlightPlanASM) {
            listenerIAirplaneListenerFlightPlanASM.addAtBegin((IAirplaneListenerFlightPlanASM)l);
        }

        if (l instanceof IAirplaneListenerFlightPlanXML) {
            listenerIAirplaneListenerFlightPlanXML.addAtBegin((IAirplaneListenerFlightPlanXML)l);
        }

        if (l instanceof IAirplaneListenerHealth) {
            listenerIAirplaneListenerHealth.addAtBegin((IAirplaneListenerHealth)l);
        }

        if (l instanceof IAirplaneListenerIsSimulation) {
            listenerIAirplaneListenerIsSimulation.addAtBegin((IAirplaneListenerIsSimulation)l);
        }

        if (l instanceof IAirplaneListenerMsg) {
            listenerIAirplaneListenerMsg.addAtBegin((IAirplaneListenerMsg)l);
        }

        if (l instanceof IAirplaneListenerName) {
            listenerIAirplaneListenerName.addAtBegin((IAirplaneListenerName)l);
        }

        if (l instanceof IAirplaneListenerPhotoLogName) {
            listenerIAirplaneListenerPhotoLogName.addAtBegin((IAirplaneListenerPhotoLogName)l);
        }

        if (l instanceof IAirplaneListenerFlightphase) {
            listenerIAirplaneListenerFlightphase.addAtBegin((IAirplaneListenerFlightphase)l);
        }

        if (l instanceof IAirplaneListenerOrientation) {
            listenerIAirplaneListenerOrientation.addAtBegin((IAirplaneListenerOrientation)l);
        }

        if (l instanceof IAirplaneListenerBackend) {
            listenerIAirplaneListenerPorts.addAtBegin((IAirplaneListenerBackend)l);
        }

        if (l instanceof IAirplaneListenerPositionOrientation) {
            listenerIAirplaneListenerPositionOrientation.addAtBegin((IAirplaneListenerPositionOrientation)l);
        }

        if (l instanceof IAirplaneListenerDebug) {
            listenerIAirplaneListenerDebug.addAtBegin((IAirplaneListenerDebug)l);
        }

        if (l instanceof IAirplaneListenerPosition) {
            listenerIAirplaneListenerPosition.addAtBegin((IAirplaneListenerPosition)l);
        }

        if (l instanceof IAirplaneListenerSimulationSettings) {
            listenerIAirplaneListenerSimulationSettings.addAtBegin((IAirplaneListenerSimulationSettings)l);
        }

        if (l instanceof IAirplaneListenerStartPos) {
            listenerIAirplaneListenerStartPos.addAtBegin((IAirplaneListenerStartPos)l);
        }

        if (l instanceof IAirplaneListenerFixedOrientation) {
            listenerIAirplaneListenerFixedOrientation.addAtBegin((IAirplaneListenerFixedOrientation)l);
        }

        if (l instanceof IAirplaneListenerRawBackendStrings) {
            listenerIAirplaneListenerRawBackendStrings.addAtBegin((IAirplaneListenerRawBackendStrings)l);
        }

        if (l instanceof IAirplaneListenerPhoto) {
            listenerIAirplaneListenerPhoto.addAtBegin((IAirplaneListenerPhoto)l);
        }

        if (l instanceof IAirplaneListenerConnectionEstablished) {
            listenerIAirplaneListenerConnectionEstablished.addAtBegin((IAirplaneListenerConnectionEstablished)l);
        }

        if (l instanceof IAirplaneListenerPowerOn) {
            listenerIAirplaneListenerPowerOn.addAtBegin((IAirplaneListenerPowerOn)l);
        }

        if (l instanceof IAirplaneListenerPlaneInfo) {
            listenerIAirplaneListenerPlaneInfo.addAtBegin((IAirplaneListenerPlaneInfo)l);
        }

        if (l instanceof IAirplaneListenerAndroidState) {
            listenerIAirplaneListenerAndroidState.addAtBegin((IAirplaneListenerAndroidState)l);
        }

        if (l instanceof IAirplaneListenerLinkInfo) {
            listenerIAirplaneListenerLinkInfo.addAtBegin((IAirplaneListenerLinkInfo)l);
        }

        if (l instanceof IAirplaneFlightPlanSendingListener) {
            listenerIAirplaneFlightPlanSendingListener.addAtBegin((IAirplaneFlightPlanSendingListener)l);
        }

        if (l instanceof IAirplaneListenerPing) {
            listenerIAirplaneListenerPing.addAtBegin((IAirplaneListenerPing)l);
        }

        if (l instanceof IAirplaneListenerFileTransfer) {
            listenerIAirplaneListenerFileTransfer.addAtBegin((IAirplaneListenerFileTransfer)l);
        }

        if (l instanceof IAirplaneListenerExpertSimulatedFails) {
            listenerIAirplaneListenerExpertSimulatedFails.addAtBegin((IAirplaneListenerExpertSimulatedFails)l);
        }

        if (l instanceof IAirplaneParamsUpdateListener) {
            listenerIAirplaneParamsUpdateListener.addAtBegin((IAirplaneParamsUpdateListener)l);
        }

        if (l instanceof ICommandListenerResult) {
            listenerICommandResultListener.addAtBegin((ICommandListenerResult)l);
        }
    }

    public void addListenerAtSecond(IListener l) {
        if (l instanceof IAirplaneListenerLoggingStateChanged) {
            listenerIAirplaneListenerLoggingStateChanged.addAtSecond((IAirplaneListenerLoggingStateChanged)l);
        }

        if (l instanceof IAirplaneListenerGuiClose) {
            listenerIAirplaneListenerGuiClose.addAtSecond((IAirplaneListenerGuiClose)l);
        }

        if (l instanceof IAirplaneListenerConnectionState) {
            listenerIAirplaneListenerConnectionState.addAtSecond((IAirplaneListenerConnectionState)l);
        }

        if (l instanceof IAirplaneListenerLogReplay) {
            listenerIAirplaneListenerLogReplay.addAtSecond((IAirplaneListenerLogReplay)l);
        }

        if (l instanceof IAirplaneListenerBackendConnectionLost) {
            listenerIAirplaneListenerBackendConnectionLost.addAtSecond((IAirplaneListenerBackendConnectionLost)l);
        }

        if (l instanceof IAirplaneListenerConfig) {
            listenerIAirplaneListenerConfig.addAtSecond((IAirplaneListenerConfig)l);
        }

        if (l instanceof IAirplaneListenerFlightphase) {
            listenerIAirplaneListenerFlightphase.addAtSecond((IAirplaneListenerFlightphase)l);
        }

        if (l instanceof IAirplaneListenerFlightPlanASM) {
            listenerIAirplaneListenerFlightPlanASM.addAtSecond((IAirplaneListenerFlightPlanASM)l);
        }

        if (l instanceof IAirplaneListenerFlightPlanXML) {
            listenerIAirplaneListenerFlightPlanXML.addAtSecond((IAirplaneListenerFlightPlanXML)l);
        }

        if (l instanceof IAirplaneListenerHealth) {
            listenerIAirplaneListenerHealth.addAtSecond((IAirplaneListenerHealth)l);
        }

        if (l instanceof IAirplaneListenerIsSimulation) {
            listenerIAirplaneListenerIsSimulation.addAtSecond((IAirplaneListenerIsSimulation)l);
        }

        if (l instanceof IAirplaneListenerMsg) {
            listenerIAirplaneListenerMsg.addAtSecond((IAirplaneListenerMsg)l);
        }

        if (l instanceof IAirplaneListenerName) {
            listenerIAirplaneListenerName.addAtSecond((IAirplaneListenerName)l);
        }

        if (l instanceof IAirplaneListenerPhotoLogName) {
            listenerIAirplaneListenerPhotoLogName.addAtSecond((IAirplaneListenerPhotoLogName)l);
        }

        if (l instanceof IAirplaneListenerFlightphase) {
            listenerIAirplaneListenerFlightphase.addAtSecond((IAirplaneListenerFlightphase)l);
        }

        if (l instanceof IAirplaneListenerOrientation) {
            listenerIAirplaneListenerOrientation.addAtSecond((IAirplaneListenerOrientation)l);
        }

        if (l instanceof IAirplaneListenerBackend) {
            listenerIAirplaneListenerPorts.addAtSecond((IAirplaneListenerBackend)l);
        }

        if (l instanceof IAirplaneListenerPositionOrientation) {
            listenerIAirplaneListenerPositionOrientation.addAtSecond((IAirplaneListenerPositionOrientation)l);
        }

        if (l instanceof IAirplaneListenerDebug) {
            listenerIAirplaneListenerDebug.addAtSecond((IAirplaneListenerDebug)l);
        }

        if (l instanceof IAirplaneListenerPosition) {
            listenerIAirplaneListenerPosition.addAtSecond((IAirplaneListenerPosition)l);
        }

        if (l instanceof IAirplaneListenerSimulationSettings) {
            listenerIAirplaneListenerSimulationSettings.addAtSecond((IAirplaneListenerSimulationSettings)l);
        }

        if (l instanceof IAirplaneListenerStartPos) {
            listenerIAirplaneListenerStartPos.addAtSecond((IAirplaneListenerStartPos)l);
        }

        if (l instanceof IAirplaneListenerFixedOrientation) {
            listenerIAirplaneListenerFixedOrientation.addAtSecond((IAirplaneListenerFixedOrientation)l);
        }

        if (l instanceof IAirplaneListenerRawBackendStrings) {
            listenerIAirplaneListenerRawBackendStrings.addAtSecond((IAirplaneListenerRawBackendStrings)l);
        }

        if (l instanceof IAirplaneListenerPhoto) {
            listenerIAirplaneListenerPhoto.addAtSecond((IAirplaneListenerPhoto)l);
        }

        if (l instanceof IAirplaneListenerConnectionEstablished) {
            listenerIAirplaneListenerConnectionEstablished.addAtSecond((IAirplaneListenerConnectionEstablished)l);
        }

        if (l instanceof IAirplaneListenerPowerOn) {
            listenerIAirplaneListenerPowerOn.addAtSecond((IAirplaneListenerPowerOn)l);
        }

        if (l instanceof IAirplaneListenerPlaneInfo) {
            listenerIAirplaneListenerPlaneInfo.addAtSecond((IAirplaneListenerPlaneInfo)l);
        }

        if (l instanceof IAirplaneListenerAndroidState) {
            listenerIAirplaneListenerAndroidState.addAtSecond((IAirplaneListenerAndroidState)l);
        }

        if (l instanceof IAirplaneListenerLinkInfo) {
            listenerIAirplaneListenerLinkInfo.addAtSecond((IAirplaneListenerLinkInfo)l);
        }

        if (l instanceof IAirplaneFlightPlanSendingListener) {
            listenerIAirplaneFlightPlanSendingListener.addAtSecond((IAirplaneFlightPlanSendingListener)l);
        }

        if (l instanceof IAirplaneListenerPing) {
            listenerIAirplaneListenerPing.addAtSecond((IAirplaneListenerPing)l);
        }

        if (l instanceof IAirplaneListenerFileTransfer) {
            listenerIAirplaneListenerFileTransfer.addAtSecond((IAirplaneListenerFileTransfer)l);
        }

        if (l instanceof IAirplaneListenerExpertSimulatedFails) {
            listenerIAirplaneListenerExpertSimulatedFails.addAtSecond((IAirplaneListenerExpertSimulatedFails)l);
        }

        if (l instanceof IAirplaneParamsUpdateListener) {
            listenerIAirplaneParamsUpdateListener.addAtSecond((IAirplaneParamsUpdateListener)l);
        }

        if (l instanceof ICommandListenerResult) {
            listenerICommandResultListener.addAtSecond((ICommandListenerResult)l);
        }
    }

    public void removeListener(IListener l) {
        for (WeakListenerList<?> listeners : listenersAll) {
            listeners.remove(l);
        }
    }

    public void err_backendConnectionLost(ConnectionLostReasons reason) {
        for (IAirplaneListenerBackendConnectionLost listener : listenerIAirplaneListenerBackendConnectionLost) {
            if (listener == null) {
                continue;
            }

            try {
                listener.err_backendConnectionLost(reason);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_config(Config_variables c) {
        for (IAirplaneListenerConfig listener : listenerIAirplaneListenerConfig) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_config(c);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_flightPhase(Integer fp) {
        for (IAirplaneListenerFlightphase listener : listenerIAirplaneListenerFlightphase) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_flightPhase(fp);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_isSimulation(Boolean simulation) {
        for (IAirplaneListenerIsSimulation listener : listenerIAirplaneListenerIsSimulation) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_isSimulation(simulation);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_msg(Integer lvl, String data) {
        for (IAirplaneListenerMsg listener : listenerIAirplaneListenerMsg) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_msg(lvl, data);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_nameChange(String name) {
        for (IAirplaneListenerName listener : listenerIAirplaneListenerName) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_nameChange(name);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_newPhotoLog(String name, Integer bytes) {
        for (IAirplaneListenerPhotoLogName listener : listenerIAirplaneListenerPhotoLogName) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_newPhotoLog(name, bytes);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_orientation(OrientationData o) {
        for (IAirplaneListenerOrientation listener : listenerIAirplaneListenerOrientation) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_orientation(o);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_backend(Backend host, MVector<Port> ports) {
        for (IAirplaneListenerBackend listener : listenerIAirplaneListenerPorts) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_backend(host, ports);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_position(PositionData p) {
        for (IAirplaneListenerPosition listener : listenerIAirplaneListenerPosition) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_position(p);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_setFlightPlanASM(String plan, Boolean reentry, Boolean succeed) {
        for (IAirplaneListenerFlightPlanASM listener : listenerIAirplaneListenerFlightPlanASM) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_setFlightPlanASM(plan, reentry, succeed);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_setFlightPlanXML(String plan, Boolean reentry, Boolean succeed) {
        for (IAirplaneListenerFlightPlanXML listener : listenerIAirplaneListenerFlightPlanXML) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_setFlightPlanXML(plan, reentry, succeed);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_simulationSpeed(Float speed) {
        for (IAirplaneListenerSimulationSettings listener : listenerIAirplaneListenerSimulationSettings) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_simulationSpeed(speed);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        for (IAirplaneListenerStartPos listener : listenerIAirplaneListenerStartPos) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_startPos(lon, lat, pressureZero);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_health(HealthData d) {
        for (IAirplaneListenerHealth listener : listenerIAirplaneListenerHealth) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_health(d);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_fixedOrientation(Float roll, Float pitch, Float yaw) {
        for (IAirplaneListenerFixedOrientation listener : listenerIAirplaneListenerFixedOrientation) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_fixedOrientation(roll, pitch, yaw);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void rawDataFromBackend(String line) {
        for (IAirplaneListenerRawBackendStrings listener : listenerIAirplaneListenerRawBackendStrings) {
            if (listener == null) {
                continue;
            }

            try {
                listener.rawDataFromBackend(line);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void rawDataToBackend(String line) {
        for (IAirplaneListenerRawBackendStrings listener : listenerIAirplaneListenerRawBackendStrings) {
            if (listener == null) {
                continue;
            }

            try {
                listener.rawDataToBackend(line);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void loggingStateChangedTCP(boolean tcp_log) {
        for (IAirplaneListenerLoggingStateChanged listener : listenerIAirplaneListenerLoggingStateChanged) {
            if (listener == null) {
                continue;
            }

            try {
                listener.loggingStateChangedTCP(tcp_log);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void loggingStateChangedFLG(boolean flg_log) {
        for (IAirplaneListenerLoggingStateChanged listener : listenerIAirplaneListenerLoggingStateChanged) {
            if (listener == null) {
                continue;
            }

            try {
                listener.loggingStateChangedFLG(flg_log);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void guiClose() {
        for (IAirplaneListenerGuiClose listener : listenerIAirplaneListenerGuiClose.reversed()) {
            if (listener == null) {
                continue;
            }

            try {
                listener.guiClose();
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public boolean guiCloseRequest() {
        for (IAirplaneListenerGuiClose listener : listenerIAirplaneListenerGuiClose.reversed()) {
            try {
                if (listener == null || !listener.guiCloseRequest()) {
                    return false;
                }
            } catch (NullPointerException e) {
                // ignore, this is normal because the Proxys for echoing all stuff can not deal with returning something
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }

        return true;
    }

    public void storeToSessionNow() {
        for (IAirplaneListenerGuiClose listener : listenerIAirplaneListenerGuiClose) {
            if (listener == null) {
                continue;
            }

            try {
                listener.storeToSessionNow();
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void replaySkipPhase(boolean isSkipping) {
        for (IAirplaneListenerLogReplay listener : listenerIAirplaneListenerLogReplay) {
            if (listener == null) {
                continue;
            }

            try {
                listener.replaySkipPhase(isSkipping);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void replayPaused(boolean paused) {
        for (IAirplaneListenerLogReplay listener : listenerIAirplaneListenerLogReplay) {
            if (listener == null) {
                continue;
            }

            try {
                listener.replayPaused(paused);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void replayStopped(boolean stopped) {
        for (IAirplaneListenerLogReplay listener : listenerIAirplaneListenerLogReplay) {
            if (listener == null) {
                continue;
            }

            try {
                listener.replayStopped(stopped);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void replayFinished() {
        for (IAirplaneListenerLogReplay listener : listenerIAirplaneListenerLogReplay) {
            if (listener == null) {
                continue;
            }

            try {
                listener.replayFinished();
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_photo(PhotoData photo) {
        for (IAirplaneListenerPhoto listener : listenerIAirplaneListenerPhoto) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_photo(photo);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_powerOn() {
        for (IAirplaneListenerPowerOn listener : listenerIAirplaneListenerPowerOn) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_powerOn();
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_planeInfo(PlaneInfo info) {
        for (IAirplaneListenerPlaneInfo listener : listenerIAirplaneListenerPlaneInfo) {
            if (listener == null) {
                continue;
            }

            listener.recv_planeInfo(info);
        }
    }

    // public void connectionOpenedRead() {
    // for (IAirplaneListenerConnectionOpenend listener : listenerIAirplaneListenerConnectionOpenend) {
    // try {
    // listener.connectionOpenedRead();
    // } catch (Throwable e) {
    // if (e.getCause() != null)
    // e = e.getCause();
    // CDebug.getLog().log(Level.SEVERE,
    // "Problem invoking Airplane Listener", e);
    // }
    // }
    // }
    //
    // public void connectionOpenedReadWrite() {
    // for (IAirplaneListenerConnectionOpenend listener : listenerIAirplaneListenerConnectionOpenend) {
    // try {
    // listener.connectionOpenedReadWrite();
    // } catch (Throwable e) {
    // if (e.getCause() != null)
    // e = e.getCause();
    // CDebug.getLog().log(Level.SEVERE,
    // "Problem invoking Airplane Listener", e);
    // }
    // }
    // }

    public void recv_connectionEstablished(String port) {
        for (IAirplaneListenerConnectionEstablished listener : listenerIAirplaneListenerConnectionEstablished) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_connectionEstablished(port);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_androidState(AndroidState state) {
        for (IAirplaneListenerAndroidState listener : listenerIAirplaneListenerAndroidState) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_androidState(state);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_linkInfo(LinkInfo li) {
        for (IAirplaneListenerLinkInfo listener : listenerIAirplaneListenerLinkInfo) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_linkInfo(li);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void ping(String myID) {
        for (IAirplaneListenerPing listener : listenerIAirplaneListenerPing) {
            if (listener == null) {
                continue;
            }

            try {
                listener.ping(myID);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {
        for (IAirplaneListenerFileTransfer listener : listenerIAirplaneListenerFileTransfer) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_dirListing(parentPath, files, sizes);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void connectionStateChange(AirplaneConnectorState newState) {
        for (IAirplaneListenerConnectionState listener : listenerIAirplaneListenerConnectionState) {
            if (listener == null) {
                continue;
            }

            try {
                listener.connectionStateChange(newState);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void elapsedSimTime(double secs, double secsTotal) {
        for (IAirplaneListenerLogReplay listener : listenerIAirplaneListenerLogReplay) {
            if (listener == null) {
                continue;
            }

            try {
                listener.elapsedSimTime(secs, secsTotal);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_simulationSettings(SimulationSettings settings) {
        for (IAirplaneListenerSimulationSettings listener : listenerIAirplaneListenerSimulationSettings) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_simulationSettings(settings);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_fileReceivingCancelled(String path) {
        for (IAirplaneListenerFileTransfer listener : listenerIAirplaneListenerFileTransfer) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_fileReceivingCancelled(path);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_fileReceivingProgress(String path, Integer progress) {
        for (IAirplaneListenerFileTransfer listener : listenerIAirplaneListenerFileTransfer) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_fileReceivingProgress(path, progress);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_fileReceivingSucceeded(String path) {
        for (IAirplaneListenerFileTransfer listener : listenerIAirplaneListenerFileTransfer) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_fileReceivingSucceeded(path);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_fileSendingCancelled(String path) {
        for (IAirplaneListenerFileTransfer listener : listenerIAirplaneListenerFileTransfer) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_fileSendingCancelled(path);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_fileSendingProgress(String path, Integer progress) {
        for (IAirplaneListenerFileTransfer listener : listenerIAirplaneListenerFileTransfer) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_fileSendingProgress(path, progress);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_fileSendingSucceeded(String path) {
        for (IAirplaneListenerFileTransfer listener : listenerIAirplaneListenerFileTransfer) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_fileSendingSucceeded(path);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_startPos(Double lon, Double lat) {
        // for compatibility reasons
        recv_startPos(lon, lat, 1);
    }

    public void recv_debug(DebugData d) {
        for (IAirplaneListenerDebug listener : listenerIAirplaneListenerDebug) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_debug(d);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_positionOrientation(PositionOrientationData po) {
        for (IAirplaneListenerPositionOrientation listener : listenerIAirplaneListenerPositionOrientation) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_positionOrientation(po);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_simulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
        for (IAirplaneListenerExpertSimulatedFails listener : listenerIAirplaneListenerExpertSimulatedFails) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_simulatedFails(failBitMask, debug1, debug2, debug3);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    @Override
    public void recv_fpSendingStatusChange(FlightPlanStaus fpStatus) {
        for (IAirplaneFlightPlanSendingListener listener : listenerIAirplaneFlightPlanSendingListener) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_fpSendingStatusChange(fpStatus);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }

                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    @Override
    public void recv_paramsUpdateStatusChange(IAirplaneParamsUpdateListener.ParamsUpdateStatus fpStatus) {
        for (IAirplaneParamsUpdateListener listener : listenerIAirplaneParamsUpdateListener) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_paramsUpdateStatusChange(fpStatus);
            } catch (Throwable e) {
                if (e.getCause() != null) e = e.getCause();
                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

    public void recv_cmd_result(CommandResultData resultData) {
        for (ICommandListenerResult listener : listenerICommandResultListener) {
            if (listener == null) {
                continue;
            }

            try {
                listener.recv_cmd_result(resultData);
            } catch (Throwable e) {
                if (e.getCause() != null) {
                    e = e.getCause();
                }
                Debug.getLog().log(Level.SEVERE, "Problem invoking Airplane Listener", e);
            }
        }
    }

}
