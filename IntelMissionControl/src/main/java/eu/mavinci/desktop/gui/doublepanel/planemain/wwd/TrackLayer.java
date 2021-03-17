/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.wwd;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenter;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenterListener;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.IAirplaneConnector;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.logfile.LogReaderVLG;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.awt.Color;

public class TrackLayer extends AColoredTrajectoryLayer implements IAirplaneListenerPosition {

    public static final Color COLOR_DYNAMIC_MANUALCONTROL = new Color(0x0066cc); // manual flight
    public static final Color COLOR_DYNAMIC_ASSISTED = new Color(0x0066cc); // assisted
    public static final Color COLOR_GPS_LOSS = new Color(0xFF5F5F); // GPS-LOSS
    public static final Color COLOR_DYNAMIC_ELSE = new Color(0x0066cc); // AUTO

    IAirplane plane;

    IMapClearingCenterListener listener =
        new IMapClearingCenterListener() {
            @Override
            public void clearUavImageCache() {}

            @Override
            public void clearTrackLog() {
                clear();
            }

            @Override
            public void clearOldTrackCache() {
                clearOldStuffAsync();
            }
        };

    private final GeneralSettings generalSettings = StaticInjector.getInstance(GeneralSettings.class);

    public TrackLayer(IAirplane plane, IWWMapView mapView) {
        super(plane, "TrackLayerName", false, false, mapView);
        this.plane = plane;

        plane.addListener(this);
        StaticInjector.getInstance(IMapClearingCenter.class).addWeakListener(listener);
    }

    @Override
    public void recv_position(PositionData p) {
        AirplaneFlightmode flightmode = AirplaneFlightmode.ManualControl;
        try {
            flightmode = plane.getAirplaneCache().getFlightMode();
        } catch (AirplaneCacheEmptyException e) {
        }

        AirplaneFlightphase flightphase = AirplaneFlightphase.airborne;
        try {
            flightphase = plane.getAirplaneCache().getFlightPhase();
        } catch (AirplaneCacheEmptyException e) {
        }

        Color c;
        if (flightphase == AirplaneFlightphase.gpsloss) {
            c = COLOR_GPS_LOSS;
        } else if (flightmode == AirplaneFlightmode.ManualControl) {
            c = COLOR_DYNAMIC_MANUALCONTROL;
            // manual flight
        } else if (flightmode == AirplaneFlightmode.AssistedFlying) {
            c = COLOR_DYNAMIC_ASSISTED;
            // assisted flight
        } else {
            c = COLOR_DYNAMIC_ELSE; // ... and at
            // autopilote green!
        }
        // System.out.println("NEW cOLO" + c);

        if (p.lostPositionUpdates) {
            c = new Color(COLOR_GPS_LOSS.getRed(), COLOR_GPS_LOSS.getGreen(), COLOR_GPS_LOSS.getBlue(), 0x44);
        }

        double elevationStartPoint;
        try {
            elevationStartPoint = plane.getAirplaneCache().getStartElevOverWGS84();
        } catch (AirplaneCacheEmptyException e) {
            return;
        }
        // System.out.println("elevation starting" + elevationStartPoint);
        // elevationStartPoint += 300;

        double curAlt = p.altitude / 100. + elevationStartPoint;
        IAirplaneConnector connector = plane.getAirplaneConnector();
        if (connector instanceof LogReaderVLG && ((LogReaderVLG)connector).isNMEA()) {
            curAlt = p.gpsAltitude / 100.0;
        }

        Position pos = new Position(Angle.fromDegreesLatitude(p.lat), Angle.fromDegreesLongitude(p.lon), curAlt);
        super.addPosition(pos, c);
    }

    @Override
    protected long getCutIntervalInMs() {
        return generalSettings.getAutoClearingIntervallInMS();
    }

}
