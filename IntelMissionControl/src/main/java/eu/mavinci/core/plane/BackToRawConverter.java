/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import eu.mavinci.core.plane.listeners.IAirplaneListenerAllExternal;
import eu.mavinci.core.plane.listeners.IAirplaneListenerRawBackendStrings;
import eu.mavinci.core.plane.protocol.ObjectPacking;
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
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.core.plane.listeners.IAirplaneListenerAllExternal;
import eu.mavinci.core.plane.listeners.IAirplaneListenerRawBackendStrings;
import eu.mavinci.core.plane.protocol.ObjectPacking;
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

import java.util.Vector;
import java.util.logging.Level;

public class BackToRawConverter implements IAirplaneListenerAllExternal {

    IAirplaneListenerRawBackendStrings listener;

    public BackToRawConverter(IAirplaneListenerRawBackendStrings listener) {
        this.listener = listener;
    }

    public void err_backendConnectionLost(ConnectionLostReasons reason) {}

    public void recv_config(Config_variables c) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_config", c, "c"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_flightPhase(Integer fp) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_flightPhase", fp, "fp"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_setFlightPlanASM(String plan, Boolean reentry, Boolean succeed) {
        Vector<Object> args = new Vector<Object>(3);
        Vector<String> names = new Vector<String>(3);

        args.add(plan);
        names.add("plan");

        args.add(reentry);
        names.add("reentry");

        args.add(succeed);
        names.add("succeed");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_setFlightPlanASM", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_setFlightPlanXML(String plan, Boolean reentry, Boolean succeed) {
        Vector<Object> args = new Vector<Object>(3);
        Vector<String> names = new Vector<String>(3);

        args.add(plan);
        names.add("plan");

        args.add(reentry);
        names.add("reentry");

        args.add(succeed);
        names.add("succeed");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_setFlightPlanXML", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_health(HealthData d) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_health", d, "d"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_isSimulation(Boolean simulation) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_isSimulation", simulation, "simulation"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_msg(Integer lvl, String data) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(lvl);
        names.add("lvl");

        args.add(data);
        names.add("data");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_msg", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_nameChange(String name) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_nameChange", name, "name"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_newPhotoLog(String name, Integer bytes) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(name);
        names.add("name");

        args.add(bytes);
        names.add("bytes");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_newPhotoLog", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_orientation(OrientationData o) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_orientation", o, "o"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_backend(Backend host, MVector<Port> ports) {
        if (ports.isEmpty()) {
            return;
        }

        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(host);
        names.add("host");

        args.add(ports);
        names.add("ports");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_backend", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_position(PositionData p) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_position", p, "p"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_simulationSettings(SimulationSettings settings) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_simulationSettings", settings, "settings"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_simulationSpeed(Float speed) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_simulationSpeed", speed, "speed"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void replayFinished() {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("simulationFinished"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void replayPaused(boolean paused) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("simulationPaused", paused, "paused"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void replayStopped(boolean stopped) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("simulationStopped", stopped, "stopped"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(lon);
        names.add("lon");

        args.add(lat);
        names.add("lat");

        args.add(pressureZero);
        names.add("pressureZero");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_startPos", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_fixedOrientation(Float roll, Float pitch, Float yaw) {
        Vector<Object> args = new Vector<Object>(3);
        Vector<String> names = new Vector<String>(3);

        args.add(roll);
        names.add("roll");

        args.add(pitch);
        names.add("pitch");

        args.add(yaw);
        names.add("yaw");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_fixedOrientation", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void rawDataFromBackend(String line) {}

    public void rawDataToBackend(String line) {}

    public void recv_photo(PhotoData photo) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_photo", photo, "photo"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_connectionEstablished(String port) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_connectionEstablished", port, "port"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_powerOn() {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_powerOn"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_planeInfo(PlaneInfo info) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_planeInfo", info, "info"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_androidState(AndroidState state) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_androidState", state, "state"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_linkInfo(LinkInfo li) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_linkInfo", li, "li"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void ping(String myID) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("ping", myID, "myID"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {
        if (files == null) {
            return;
        }

        Vector<Object> args = new Vector<Object>(3);
        Vector<String> names = new Vector<String>(3);

        args.add(parentPath);
        names.add("parentPath");

        args.add(files);
        names.add("files");

        args.add(sizes);
        names.add("sizes");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_dirListing", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_fileReceivingCancelled(String path) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_fileReceivingCancelled", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_fileReceivingProgress(String path, Integer progress) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(path);
        names.add("path");

        args.add(progress);
        names.add("progress");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_fileReceivingProgress", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_fileReceivingSucceeded(String path) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_fileReceivingSucceeded", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_fileSendingCancelled(String path) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_fileSendingCancelled", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_fileSendingProgress(String path, Integer progress) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(path);
        names.add("path");

        args.add(progress);
        names.add("progress");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_fileSendingProgress", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_fileSendingSucceeded(String path) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_fileSendingSucceeded", path, "path"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_debug(DebugData d) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_debug", d, "d"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_positionOrientation(PositionOrientationData po) {
        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_positionOrientation", po, "po"));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    public void recv_simulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
        Vector<Object> args = new Vector<Object>(2);
        Vector<String> names = new Vector<String>(2);

        args.add(failBitMask);
        names.add("failBitMask");

        args.add(debug1);
        names.add("debug1");
        args.add(debug2);
        names.add("debug2");
        args.add(debug3);
        names.add("debug3");

        try {
            listener.rawDataFromBackend(ObjectPacking.encodeFkt("recv_simulatedFails", args, names));
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not encode fct Call", e);
        }
    }

    @Override
    public void recv_fpSendingStatusChange(FlightPlanStaus fpStatus) {
        // TODO Auto-generated method stub

    }

    @Override
    public void recv_paramsUpdateStatusChange(ParamsUpdateStatus fpStatus) {
        // TODO Auto-generated method stub

    }

}
