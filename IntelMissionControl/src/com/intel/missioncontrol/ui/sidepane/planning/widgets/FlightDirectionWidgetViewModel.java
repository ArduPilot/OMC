/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.sidepane.flight.WindDirection;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.computation.FlightLine;
import java.util.Vector;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FlightDirectionWidgetViewModel implements ViewModel {

    private final QuantityProperty<Angle> flightDirectionQuantity;
    private final SimpleDoubleProperty flightDirection = new SimpleDoubleProperty(0.);

    private final SimpleObjectProperty<PlanType> aoiType = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<AreaOfInterest> aoi = new SimpleObjectProperty<>();

    private final IApplicationContext applicationContext;
    private final IMapView mapView;

    @Inject
    public FlightDirectionWidgetViewModel(
            IApplicationContext applicationContext, ISettingsManager settingsManager, IMapView mapView) {
        this.applicationContext = applicationContext;
        this.mapView = mapView;
        flightDirectionQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.ANGLE_DEGREES,
                Quantity.of(0.0, Unit.DEGREE));

        QuantityBindings.bindBidirectional(flightDirectionQuantity, flightDirection, Unit.DEGREE);
    }

    public void setFlightDirectionFromView() {
        double heading = mapView.getHeading().getDegrees();
        heading = WindDirection.normalizeAngle(heading);
        flightDirection.setValue(heading);
    }

    public void setFlightDirectionFromUav() {
        Mission currentMission = applicationContext.getCurrentMission();

        if (currentMission == null) {
            return;
        }

        Uav uav = currentMission.uavProperty().get();

        if (uav == null) {
            return;
        }

        OrientationData orientation = uav.getOrientationFromCache();

        if (orientation == null) {
            return;
        }

        flightDirection.setValue(WindDirection.normalizeAngle(orientation.yaw));
    }

    public void plus90() {
        flightDirection.setValue(WindDirection.normalizeAngle(flightDirection.get() + 90));
    }

    public void minus90() {
        flightDirection.setValue(WindDirection.normalizeAngle(flightDirection.get() - 90));
    }

    public void setDirectionShortestPath() {
        // TAKEN FROM TreeNodePhotoSettings
        double optimizedDirection = getOptimizedDirection(FlightLine::getLength, false);
        optimizedDirection = WindDirection.normalizeAngle(optimizedDirection);
        flightDirection.setValue(optimizedDirection);
    }

    public void setDirectionOptimizeForTerrain() {
        // TAKEN FROM TreeNodePhotoSettings
        double optimizedDirection = getOptimizedDirection(fl -> fl.getLength() * fl.getMinMaxElevation().size(), true);
        optimizedDirection = WindDirection.normalizeAngle(optimizedDirection);
        flightDirection.setValue(optimizedDirection);
    }

    /**
     * TAKEN FROM TreeNodePhotoSettings. Get optimized flight direction yaw
     *
     * @param flightLineLengthGetter function that gets length from FlightLine
     * @return optimal flight direction yaw
     */
    private double getOptimizedDirection(Function<FlightLine, Double> flightLineLengthGetter, boolean flip90) {
        AreaOfInterest aoiVal = aoi.get();
        if (aoiVal == null) return 0;
        PicArea picArea = aoiVal.getPicArea();
        double oldYaw = picArea.getYaw();
        Flightplan flightplan = picArea.getFlightplan();
        if (flightplan == null) {
            return oldYaw;
        }

        if (flip90 && flightplan.getHardwareConfiguration().getPlatformDescription().isInCopterMode()) {
            oldYaw += 90;
        }

        double optYaw = oldYaw;
        double optQuality = Double.POSITIVE_INFINITY;

        PlanType type = picArea.getPlanType();
        try {
            flightplan.setMute(true);
            picArea.setMute(true);
            if (type == PlanType.BUILDING) {
                picArea.setPlanType(PlanType.POLYGON); // WAY FASTER TO COMPUTE, AND HAS THE SAME OPTIMAL YAW
            }

            for (double yaw = oldYaw - 90; yaw < oldYaw + 90; yaw++) {
                double quality = 0;
                picArea.setYaw(yaw);
                picArea.setupTransformNotSpreading();
                picArea.computeFlightLinesNotSpreading(true);
                Vector<FlightLine> flightLines = picArea.getFlightLines();
                if (flightLines == null) {
                    quality = Double.POSITIVE_INFINITY;
                    continue;
                }

                for (FlightLine fl : flightLines) {
                    quality += flightLineLengthGetter.apply(fl);
                }

                if (quality < optQuality) {
                    optQuality = quality;
                    optYaw = yaw;
                }
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "cant optimize yaw", e);
            return oldYaw;
        } finally {
            picArea.setPlanType(type);
            picArea.setYaw(oldYaw);
            picArea.setupTransformNotSpreading();
            picArea.computeFlightLinesNotSpreading(true);
            picArea.setSilentUnmute();
            flightplan.setSilentUnmute();
        }

        if (optQuality != Double.POSITIVE_INFINITY) {

            // for (falcon 8+ ;-) copters its actually best to fly always up and down, since then slope can be
            // compenstated by gimble
            if (flip90 && flightplan.getHardwareConfiguration().getPlatformDescription().isInCopterMode()) {
                optYaw -= 90;
            }

            return optYaw;
        }

        return oldYaw;
    }

    public int getCustomFlightDirection() {
        return flightDirection.getValue().intValue();
    }

    public SimpleDoubleProperty flightDirectionProperty() {
        return flightDirection;
    }

    public QuantityProperty<Angle> flightDirectionQuantityProperty() {
        return flightDirectionQuantity;
    }

    public SimpleObjectProperty<PlanType> aoiTypeProperty() {
        return aoiType;
    }

    public SimpleObjectProperty<AreaOfInterest> aoiProperty() {
        return aoi;
    }
}
