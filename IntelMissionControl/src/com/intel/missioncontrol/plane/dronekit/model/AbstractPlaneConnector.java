/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model;

import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.core.plane.tcp.AAirplaneConnector;
import java.util.Vector;

abstract class AbstractPlaneConnector extends AAirplaneConnector {
    // expose protected method
    public void fireConnectionStateEvent(AirplaneConnectorState newConnectorState) {
        fireConnectionState(newConnectorState);
    }

    // ------------------------------------------------------------------------------
    // AAirplaneConnector methods
    // ------------------------------------------------------------------------------
    @Override
    public boolean isSimulation() {
        return false;
    }

    @Override
    public void cancelLaunch() {

    }

    @Override
    public void cancelLanding() {

    }

    @Override
    public boolean isWriteable() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public Object getPlanePort() {
        return null;
    }

    @Override
    public void connect(String port) {

    }

    @Override
    public void mavinci(Integer version) {

    }

    @Override
    public void requestBackend() {

    }

    @Override
    public void requestIsSimulation() {

    }

    @Override
    public void requestPlaneInfo() {

    }

    @Override
    public void requestConfig() {

    }

    @Override
    public void setConfig(Config_variables c) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void saveConfig() {

    }

    public abstract void setFlightPhase(AirplaneFlightphase phase);

    @Override
    public void requestFlightPhase() {

    }

    @Override
    public void setFlightPlanASM(String plan, Integer entrypoint) {

    }

    @Override
    public abstract void setFlightPlanXML(String plan, Integer entrypoint);

    @Override
    public void requestFlightPlanXML() {

    }

    @Override
    public void requestFlightPlanASM() {

    }

    @Override
    public void requestStartpos() {

    }

    @Override
    public void setSimulationSpeed(Float speed) {

    }

    @Override
    public void setSimulationSettings(SimulationSettings settings) {

    }

    @Override
    public void requestSimulationSettings() {

    }

    @Override
    public void requestSimulationSpeed() {

    }

    @Override
    public void setStartpos(Double lon, Double lat) {

    }

    @Override
    public void expertUpdateFirmware(String path) {

    }

    @Override
    public void expertUpdateBackend(String path) {

    }

    @Override
    public void expertRecalibrate() {

    }

    @Override
    public void expertTrimOn() {

    }

    @Override
    public void expertTrimOff() {

    }

    @Override
    public void expertSendSimulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {

    }

    @Override
    public void expertRequestSimulatedFails() {

    }

    @Override
    public void expertRecalibrateCompassStart() {

    }

    @Override
    public void expertRecalibrateCompassStop() {

    }

    @Override
    public void dbgRTon() {

    }

    @Override
    public void dbgRToff() {

    }

    @Override
    public void dbgExitAutopilot() {

    }

    @Override
    public void dbgResetDebug() {

    }

    @Override
    public void requestAirplaneName() {

    }

    @Override
    public void setAirplaneName(String newName) {

    }

    @Override
    public void setFixedOrientation(Float roll, Float pitch, Float yaw) {

    }

    @Override
    public void requestFixedOrientation() {

    }

    @Override
    public void dbgCommand0() {

    }

    @Override
    public void dbgCommand1() {

    }

    @Override
    public void dbgCommand2() {

    }

    @Override
    public void dbgCommand3() {

    }

    @Override
    public void dbgCommand4() {

    }

    @Override
    public void dbgCommand5() {

    }

    @Override
    public void dbgCommand6() {

    }

    @Override
    public void dbgCommand7() {

    }

    @Override
    public void dbgCommand8() {

    }

    @Override
    public void dbgCommand9() {

    }

    @Override
    public void clearNVRAM() {

    }

    @Override
    public void updateAndroidState(AndroidState state) {

    }

    @Override
    public void requestDirListing(String path) {

    }

    @Override
    public void getFile(String path) {

    }

    @Override
    public void sendFile(String path) {

    }

    @Override
    public void deleteFile(String path) {

    }

    @Override
    public void makeDir(String path) {

    }

    @Override
    public void writeToFlash(String path) {

    }

    @Override
    public void cancelSending(String path) {

    }

    @Override
    public void cancelReceiving(String path) {

    }

    @Override
    public void expertUpdateBackendTopconOAF(String path) {

    }

    @Override
    public void expertUpdateFirmwareTopconOAF(String path) {

    }

    @Override
    public void setManualServos(Vector<Integer> manualServos) {

    }
}