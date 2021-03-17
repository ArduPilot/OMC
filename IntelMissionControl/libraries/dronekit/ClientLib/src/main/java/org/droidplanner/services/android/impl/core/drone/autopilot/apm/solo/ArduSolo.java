package org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo;

import android.content.Context;
import android.os.Handler;
import com.MAVLink.Messages.MAVLinkMessage;
import com.intel.dronekit.NotImplementedDesktop;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;
import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.communication.service.MAVLinkClient;
import org.droidplanner.services.android.impl.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.impl.core.drone.profiles.ParameterManager;
import org.droidplanner.services.android.impl.core.drone.variables.*;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.utils.AndroidApWarningParser;

public class ArduSolo implements MavLinkDrone {
    public ArduSolo(String droneId, Context context, MAVLinkClient mavClient, Handler handler, AndroidApWarningParser androidApWarningParser, MavLinkDroneManager mavLinkDroneManager) {
        throw new NotImplementedDesktop();
    }

    @Override
    public boolean isConnectionAlive() {
        return false;
    }

    @Override
    public int getMavlinkVersion() {
        return 0;
    }

    @Override
    public void onMavLinkMessageReceived(MAVLinkMessage message) {

    }

    @Override
    public short getSysid() {
        return 0;
    }

    @Override
    public short getCompid() {
        return 0;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public ParameterManager getParameterManager() {
        return null;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public FirmwareType getFirmwareType() {
        return null;
    }

    @Override
    public DataLink.DataLinkProvider<MAVLinkMessage> getMavClient() {
        return null;
    }

    @Override
    public WaypointManager getWaypointManager() {
        return null;
    }

    @Override
    public MissionImpl getMission() {
        return null;
    }

    @Override
    public StreamRates getStreamRates() {
        return null;
    }

    @Override
    public MissionStats getMissionStats() {
        return null;
    }

    @Override
    public GuidedPoint getGuidedPoint() {
        return null;
    }

    @Override
    public AccelCalibration getCalibrationSetup() {
        return null;
    }

    @Override
    public MagnetometerCalibrationImpl getMagnetometerCalibration() {
        return null;
    }

    @Override
    public String getFirmwareVersion() {
        return null;
    }

    @Override
    public Camera getCamera() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public DroneAttribute getAttribute(String attributeType) {
        return null;
    }

    @Override
    public boolean executeAsyncAction(Action action, ICommandListener listener) {
        return false;
    }

    @Override
    public void setAttributeListener(DroneInterfaces.AttributeEventListener listener) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void addDroneListener(DroneInterfaces.OnDroneListener listener) {

    }

    @Override
    public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {

    }

    @Override
    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event) {

    }
}
