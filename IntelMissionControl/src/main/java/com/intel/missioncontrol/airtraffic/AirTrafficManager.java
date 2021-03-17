/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.airtraffic.dto.AirtrafficObject;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirTrafficManager implements IAirTrafficManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirTrafficManager.class);

    public final double RADIUS = 500_000; // meters
    private final SimpleAsyncListProperty<AirtrafficObject> airtrafficObjects =
            new SimpleAsyncListProperty<>(
                    this,
                    new PropertyMetadata.Builder<AsyncObservableList<AirtrafficObject>>()
                            .initialValue(FXAsyncCollections.observableArrayList())
                            .create());
    private final IDroneConnectionService droneConnectionService;
    private final IAirtrafficService ats;
    private Toast lastToast;

    @Inject
    public AirTrafficManager(
            IDroneConnectionService droneConnectionService,
            AirspacesProvidersSettings airspacesProvidersSettings,
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            GeneralSettings generalSettings,
            IMapView mapView) {
        switch (airspacesProvidersSettings.getAirtrafficProvider()) {
            case GARRECHT:
                ats = new AirAvionics();
                break;
            case NONE:
            case MAVLINK:
            default:
                ats = new MockAirtraffic();
                break;
        }

        final AdaptiveQuantityFormat quantityFormat = new AdaptiveQuantityFormat(generalSettings);
        quantityFormat.setMaximumFractionDigits(0);

        this.droneConnectionService = droneConnectionService;
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.runLaterAsync(
                () -> {
                    try {
                        List<Position> positions = new ArrayList<Position>();
                        // get list of all drone positions, and ask around them for traffic
                        try (LockedList<IReadOnlyConnectionItem> connectionItemLockedList =
                                     droneConnectionService.connectedDroneConnectionItemsProperty().lock()) {
                            for (IReadOnlyConnectionItem connectionItem : connectionItemLockedList) {
                                IDrone drone = droneConnectionService.getConnectedDrone(connectionItem);
                                Position pos = drone.positionProperty().get();
                                if (pos != null) {
                                    positions.add(pos);
                                }
                            }
                        }

                        // positions.add(Position.fromDegrees(50.0379, 8.5622, 0)); // TODO REMOVE ME!

                        HashSet<LatLon> allPoints = new HashSet<>();
                        double min2d = Double.POSITIVE_INFINITY;
                        double minElevation = Double.POSITIVE_INFINITY;

                        try (var atos = airtrafficObjects.lock()) {
                            atos.clear();
                            for (Position pos : positions) {
                                try {
                                    atos.addAll(ats.getTraffic(pos.latitude.degrees, pos.longitude.degrees, RADIUS, 10));
                                } catch (Exception e) {
                                    LOGGER.error("cant get air traffic", e);
                                }
                            }

                            // check intersection of all drones and all airtraffic objects
                            for (Position pos : positions) {
                                for (var ato : atos) {
                                    double distAlt = Math.abs(pos.elevation - ato.getProperties().getWgs84Altitude());
                                    LatLon posTraffic =
                                            LatLon.fromDegrees(
                                                    ato.geometry.coordinates.get(1), ato.geometry.coordinates.get(0));
                                    double dist2d =
                                            LatLon.ellipsoidalDistance(
                                                    pos, posTraffic, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
                                    if (min2d > dist2d) {
                                        min2d = dist2d;
                                    }

                                    if (minElevation > distAlt) {
                                        minElevation = distAlt;
                                    }

                                    if (dist2d < airspacesProvidersSettings.getMinimumHorizontalDistance().doubleValue()
                                            && distAlt
                                            < airspacesProvidersSettings.getMinimumVerticalDistance().doubleValue()) {
                                        allPoints.add(pos);
                                        allPoints.add(posTraffic);
                                    }
                                }
                            }

                            if (!allPoints.isEmpty()) {
                                Sector bounding = Sector.boundingSector(allPoints);
                                if (lastToast != null) {
                                    lastToast.dismiss();
                                }

                                lastToast =
                                        Toast.of(ToastType.ALERT)
                                                .setText(
                                                        languageHelper.getString(
                                                                AirTrafficManager.class,
                                                                "alertText",
                                                                quantityFormat.format(Quantity.of(min2d, Unit.METER)),
                                                                quantityFormat.format(Quantity.of(minElevation, Unit.METER))))
                                                .setCloseable(false)
                                                .setShowIcon(true)
                                                .setAction(
                                                        languageHelper.getString(AirTrafficManager.class, "show"),
                                                        false,
                                                        false,
                                                        () -> mapView.goToSectorAsync(bounding, OptionalDouble.empty()))
                                                .create();
                                applicationContext.addToast(lastToast);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.error("cant receive traffic", e);
                    }
                },
                Duration.ZERO,
                Duration.ofSeconds(5));

        dispatcher.runLaterAsync(
                () -> {
                    if (generalSettings.getPublishAdsb()) {
                        List<Position> positions = new ArrayList<Position>();
                        // get list of all drone positions, and ask around them for traffic
                        try (LockedList<IReadOnlyConnectionItem> connectionItemLockedList =
                                     droneConnectionService.connectedDroneConnectionItemsProperty().lock()) {
                            for (IReadOnlyConnectionItem connectionItem : connectionItemLockedList) {
                                IDrone drone = droneConnectionService.getConnectedDrone(connectionItem);
                                Position pos = drone.positionProperty().get();
                                if (pos != null) {
                                    int heading = 0;
                                    if (!(drone.attitudeProperty().get() == null)) {
                                        heading = (int) Math.round(drone.attitudeProperty().get().getRotationX().degrees);
                                    }
                                    this.ats.publishUavPosition(drone.positionProperty().get().longitude.degrees,
                                            drone.positionProperty().get().latitude.degrees,
                                            (int) Math.round(drone.positionProperty().get().getAltitude()),
                                            heading,
                                            0,
                                            "Gray Hawk",
                                            false,
                                            0,
                                            "",
                                            13,
                                            0,
                                            (int) Math.round(drone.positionProperty().get().getAltitude()));

                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error while trying to publish UAV position", e);
                        }
                    }

                },
                Duration.ZERO,
                Duration.ofSeconds(5));


    }

    @Override
    public AsyncListProperty<AirtrafficObject> relevantTrafficProperty() {
        return airtrafficObjects;
    }
}
