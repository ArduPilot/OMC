/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CFlightplanManager;
import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.plane.listeners.AirplaneListenerDelegator;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.listeners.IBackendBroadcastListener;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.core.plane.tcp.AAirplaneConnector;
import eu.mavinci.desktop.main.debug.Debug;
import java.util.Vector;
import java.util.logging.Level;

public class CAirplane extends AAirplaneConnector implements IAirplaneListenerConnectionState, ICAirplane {

    private static IHardwareConfigurationManager hardwareConfigurationManager =
        DependencyInjector.getInstance().getInstanceOf(IHardwareConfigurationManager.class);

    public IAirplaneListenerDelegator getRootHandler() {
        return rootHandler;
    }

    protected void init() {
        cache = new CAirplaneCache(this);
        // rootHandler = AirplaneListenerDelegatorFactory.createNewAirplaneListenerDelegator(); // uses reflection
        rootHandler = new AirplaneListenerDelegator(); // without reflection
        addListenerAtBegin(cache); // listener nr. 0
        fpManager = new CFlightplanManager(this); // listener nr. 1
        addListener(this); // listener nr. 2
    }

    protected IAirplaneConnector connector;
    protected CAirplaneCache cache;
    protected CFlightplanManager fpManager;
    protected ICAirplane parentPlane;
    protected IHardwareConfiguration nativeHardwareConfiguration = hardwareConfigurationManager.getImmutableDefault();

    public static final String KEY = "eu.mavinci.core.plane.CAirplane";

    /** Add listener to the listener list additionally try to invoke the listener with values out of the cache */
    public void addListener(IListener l) {
        rootHandler.addListener(l);
        cache.invokeWithCacheValues(l);
    }

    public void addListenerAtBegin(IListener l) {
        rootHandler.addListenerAtBegin(l);
        cache.invokeWithCacheValues(l);
    }

    public void removeListener(IListener l) {
        rootHandler.removeListener(l);
    }

    public ICAirplane getParentPlane() {
        return parentPlane;
    }

    public void setParentPlane(ICAirplane parentPlane) {
        this.parentPlane = parentPlane;
    }

    public CAirplaneCache getAirplaneCache() {
        return cache;
    }

    public CFlightplanManager getFPmanager() {
        return fpManager;
    }

    public void requestAll() {
        if (!isWriteable()) {
            return;
        }
        // (new Exception("requestAll()")).printStackTrace();
        requestBackend();
        requestPlaneInfo();
        requestConfig();
        requestFlightPhase();
        requestIsSimulation();
        requestStartpos();
        requestAirplaneName();
        requestFlightPlanXML();
        requestFlightPlanASM();
        requestSimulationSpeed();
        requestFixedOrientation();
        requestSimulationSettings();
    }

    public void fireGuiClose() {
        close();
        rootHandler.guiClose();
    }

    public void fireStoreToSessionNow() {
        rootHandler.storeToSessionNow();
    }

    public boolean fireGuiCloseRequest() {
        if (!rootHandler.guiCloseRequest()) {
            return false;
        }

        return true;
    }

    public void setAirplaneConnector(IAirplaneConnector con) {
        // (new Exception("new Connection")).printStackTrace();
        if (this.connector != null) {
            unsetAirplaneConnector();
        }

        this.connector = con;
        if (con == null) {
            return;
            // con.setRootHandler(rootHandler);
            // if (con.isReadable())
            // rootHandler.connectionOpenedRead();
            // if (con.isWriteable())
            // rootHandler.connectionOpenedReadWrite();
        }

        fireConnectionState(con.getConnectionState());
        // connector.requestAll(); //this is done by the connectionOpenedReadWrite
        // BackendState bs = CAirport.getInstance().findBackendState(serialNumber)
    }

    public void unsetAirplaneConnector() {
        if (connector == null) {
            return;
        }

        removeListener(connector);
        fireConnectionState(AirplaneConnectorState.unconnected);
        fpManager.getOnAirFlightplan().clear();
        // boolean throwMsg = connector.isReadable();
        connector = null;
        // if (throwMsg)
        rootHandler.err_backendConnectionLost(
            IAirplaneListenerBackendConnectionLost.ConnectionLostReasons.CONNECTION_REMOVED);
    }

    Level errLevel = Debug.WARNING;

    public boolean isAirplaneConnectorAvaliable() {
        return connector != null;
    }

    public IAirplaneConnector getAirplaneConnector() {
        return connector;
    }

    public Class<? extends IAirplaneConnector> getAirplaneConnectorClass() {
        if (connector == null) {
            return null;
        } else {
            return connector.getClass();
        }
    }

    public void connect(String port) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: connect");
            return;
        }

        connector.connect(port);
    }

    public boolean isWriteable() {
        if (connector == null) {
            return false;
        }

        return connector.isWriteable();
    }

    public void dbgCommand0() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand0");
            return;
        }

        connector.dbgCommand0();
    }

    public void dbgCommand1() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand1");
            return;
        }

        connector.dbgCommand1();
    }

    public void dbgCommand2() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand2");
            return;
        }

        connector.dbgCommand2();
    }

    public void dbgCommand3() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand3");
            return;
        }

        connector.dbgCommand3();
    }

    public void dbgCommand4() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand4");
            return;
        }

        connector.dbgCommand4();
    }

    public void dbgCommand5() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand5");
            return;
        }

        connector.dbgCommand5();
    }

    public void dbgCommand6() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand6");
            return;
        }

        connector.dbgCommand6();
    }

    public void dbgCommand7() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand7");
            return;
        }

        connector.dbgCommand7();
    }

    public void dbgCommand8() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand8");
            return;
        }

        connector.dbgCommand8();
    }

    public void dbgCommand9() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgCommand9");
            return;
        }

        connector.dbgCommand9();
    }

    public void dbgExitAutopilot() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgExitAutopilot");
            return;
        }

        connector.dbgExitAutopilot();
    }

    public void dbgRToff() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgRToff");
            return;
        }

        connector.dbgRToff();
    }

    public void dbgRTon() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgRTon");
            return;
        }

        connector.dbgRTon();
    }

    public void dbgResetDebug() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: dbgResetDebug");
            return;
        }

        connector.dbgResetDebug();
    }

    public void expertRecalibrate() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: expertRecalibrate");
            return;
        }

        connector.expertRecalibrate();
    }

    public void expertTrimOff() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request");
            return;
        }

        connector.expertTrimOff();
    }

    public void expertTrimOn() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request");
            return;
        }

        connector.expertTrimOn();
    }

    public void expertUpdateFirmware(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request");
            return;
        }

        connector.expertUpdateFirmware(path);
    }

    public void expertUpdateBackend(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: expertTrimOff");
            return;
        }

        connector.expertUpdateBackend(path);
    }

    public void mavinci(Integer version) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: mavinci");
            return;
        }

        connector.mavinci(version);
    }

    public void requestAirplaneName() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestAirplaneName");
            return;
        }

        connector.requestAirplaneName();
    }

    public void requestConfig() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestConfig");
            return;
        }

        connector.requestConfig();
    }

    public void requestFixedOrientation() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestFixedOrientation");
            return;
        }

        connector.requestFixedOrientation();
    }

    public void requestFlightPhase() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestFlightPhase");
            return;
        }

        connector.requestFlightPhase();
    }

    public void requestFlightPlanASM() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestFlightPlanASM");
            return;
        }

        connector.requestFlightPlanASM();
    }

    public void requestFlightPlanXML() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestFlightPlanXML");
            return;
        }

        connector.requestFlightPlanXML();
    }

    public void requestIsSimulation() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestIsSimulation");
            return;
        }

        connector.requestIsSimulation();
    }

    public void requestBackend() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestBackend");
            return;
        }

        connector.requestBackend();
    }

    public void requestSimulationSpeed() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestSimulationSpeed");
            return;
        }

        connector.requestSimulationSpeed();
    }

    public void requestStartpos() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestStartpos");
            return;
        }

        connector.requestStartpos();
    }

    public void setAirplaneName(String newName) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setAirplaneName");
            return;
        }

        connector.setAirplaneName(newName);
    }

    public void setConfig(Config_variables c) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setConfig");
            return;
        }

        connector.setConfig(c);
    }

    public void setFixedOrientation(Float roll, Float pitch, Float yaw) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setFixedOrientation");
            return;
        }

        connector.setFixedOrientation(roll, pitch, yaw);
    }

    public void setFlightPhase(AirplaneFlightphase p) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setFlightPhase");
            return;
        }
        /*
         * boolean wasDescending=false; try { wasDescending=cache.getFlightPhase() == AirplaneFlightphase.descending; } catch
         * (AirplaneCacheEmptyException e) { } if (!wasDescending && AirplaneFlightphase.descending == p){ //test if the current flightplan
         * has a preApproach ladning,if yes, make sure to jump to this instead of sending the descending flight phase!! CFlightplan fp =
         * getFPmanager().getOnAirFlightplan(); if (fp!= null){ CPreApproach pre = fp.getLandingpoint().getPreApproach(); if (pre!= null){
         * getFPmanager().sendFP(fp, pre.getId()); return; } } }
         */
        connector.setFlightPhase(p);
    }

    public void setFlightPlanASM(String plan, Integer entrypoint) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setFlightPlanASM");
            return;
        }

        connector.setFlightPlanASM(plan, entrypoint);
    }

    public void setFlightPlanXML(String plan, Integer entrypoint) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setFlightPlanASM");
            return;
        }

        connector.setFlightPlanXML(plan, entrypoint);
    }

    public void setSimulationSpeed(Float speed) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setSimulationSpeed");
            return;
        }

        connector.setSimulationSpeed(speed);
    }

    public void setStartpos(Double lon, Double lat) {
        if (!isWriteable() && getRootHandler() != null) {
            getRootHandler().recv_startPos(lon, lat, 0);
            getRootHandler().recv_startPos(lon, lat, 1);

            return;
        }

        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setStartpos");
            return;
        }

        connector.setStartpos(lon, lat);
    }

    @Deprecated
    public void shutdown() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: shutdown");
            return;
        }

        connector.shutdown();
    }

    public void saveConfig() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: writeToFlash");
            return;
        }

        connector.saveConfig();
    }

    public Object getPlanePort() {
        // hier wird absichtlich nichts in den logger geschrieben, das der rückgabewert so sinn macht
        if (connector == null) {
            return null;
        }

        return connector.getPlanePort();
    }

    public void close() {
        if (connector != null) {
            connector.close();
        }
    }

    public boolean isReadable() {
        if (connector == null) {
            return false;
        }

        return connector.isReadable();
    }

    public void requestPlaneInfo() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestPlaneInfo");
            return;
        }

        connector.requestPlaneInfo();
    }

    public void updateAndroidState(AndroidState state) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: updateAndroidState");
            return;
        }

        connector.updateAndroidState(state);
    }

    public void cancelReceiving(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: cancelReceiving");
            return;
        }

        connector.cancelReceiving(path);
    }

    public void cancelSending(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: cancelSending");
            return;
        }

        connector.cancelSending(path);
    }

    public void deleteFile(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: deleteFile");
            return;
        }

        connector.deleteFile(path);
    }

    public void getFile(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: getFile");
            return;
        }

        connector.getFile(path);
    }

    public void makeDir(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: makeDir");
            return;
        }

        connector.makeDir(path);
    }

    public void requestDirListing(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: requestDirListing");
            return;
        }

        Debug.getLog().config("requestDirListing=" + path);
        connector.requestDirListing(path);
    }

    public void sendFile(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: sendFile");
            return;
        }

        connector.sendFile(path);
    }

    public void connectionStateChange(AirplaneConnectorState newState) {
        curConnectorState = newState;
        if (newState == AirplaneConnectorState.fullyConnected) {
            requestAll();
        } else if (newState == AirplaneConnectorState.unconnected) {
            tryReconnect();
        }
    }

    // @Override
    // public AirplaneConnectorState getConnectionState() {
    // if (connector != null)
    // return connector.getConnectionState();
    // else
    // return super.getConnectionState();
    // }

    public void tryReconnect() {
    }

    public void requestSimulationSettings() {
        if (connector == null) {
            Debug.getLog()
                .log(errLevel, "No Airplane Connection avaliable, skipping Request: requestSimulationSettings");
            return;
        }

        connector.requestSimulationSettings();
    }

    public void setSimulationSettings(SimulationSettings settings) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setSimulationSettings");
            return;
        }

        connector.setSimulationSettings(settings);
    }

    public void writeToFlash(String path) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: writeToFlash");
            return;
        }

        connector.writeToFlash(path);
    }

    public boolean isSimulation() {
        IAirplaneConnector con = getAirplaneConnector();
        if (con == null) {
            return false;
        }

        return con.isSimulation();
    }

    @Override
    public void cancelLaunch() {
        getAirplaneConnector().cancelLaunch();
    }

    @Override
    public void cancelLanding() {
        getAirplaneConnector().cancelLanding();
    }

    public void expertRecalibrateCompassStart() {
        if (connector == null) {
            Debug.getLog()
                .log(errLevel, "No Airplane Connection avaliable, skipping Request: expertRecalibrateCompassStart");
            return;
        }

        connector.expertRecalibrateCompassStart();
    }

    public void expertRecalibrateCompassStop() {
        if (connector == null) {
            Debug.getLog()
                .log(errLevel, "No Airplane Connection avaliable, skipping Request: expertRecalibrateCompassStop");
            return;
        }

        connector.expertRecalibrateCompassStop();
    }

    public boolean isSameSession(ICAirplane plane) {
        return false;
    }

    public void expertUpdateBackendTopconOAF(String path) {
        if (connector == null) {
            Debug.getLog()
                .log(errLevel, "No Airplane Connection avaliable, skipping Request: expertUpdateBackendTopconOAF");
            return;
        }

        connector.expertUpdateBackendTopconOAF(path);
    }

    public void expertUpdateFirmwareTopconOAF(String path) {
        if (connector == null) {
            Debug.getLog()
                .log(errLevel, "No Airplane Connection avaliable, skipping Request: expertUpdateFirmwareTopconOAF");
            return;
        }

        connector.expertUpdateFirmwareTopconOAF(path);
    }

    public void expertSendSimulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
        if (connector == null) {
            Debug.getLog()
                .log(errLevel, "No Airplane Connection avaliable, skipping Request: expertSendSimulatedFails");
            return;
        }

        connector.expertSendSimulatedFails(failBitMask, debug1, debug2, debug3);
    }

    public void expertRequestSimulatedFails() {
        if (connector == null) {
            Debug.getLog()
                .log(errLevel, "No Airplane Connection avaliable, skipping Request: expertRequestSimulatedFails");
            return;
        }

        connector.expertRequestSimulatedFails();
    }

    public void clearNVRAM() {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: clearNVRAM");
            return;
        }

        connector.clearNVRAM();
    }

    public void setManualServos(Vector<Integer> manualServos) {
        if (connector == null) {
            Debug.getLog().log(errLevel, "No Airplane Connection avaliable, skipping Request: setManualServos");
            return;
        }

        connector.setManualServos(manualServos);
    }

    @Override
    public IHardwareConfiguration getNativeHardwareConfiguration() {
        return nativeHardwareConfiguration;
    }

    @Override
    public void setNativeHardwareConfiguration(IHardwareConfiguration hardwareConfiguration) {
        this.nativeHardwareConfiguration.initializeFrom(hardwareConfiguration);
    }
}
