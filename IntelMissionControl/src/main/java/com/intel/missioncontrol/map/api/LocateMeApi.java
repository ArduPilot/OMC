/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionInfo;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import java.util.LinkedList;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.asyncfx.collections.LockedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LocateMeApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocateMeApi.class);

    private final IApplicationContext applicationContext;
    private final IMissionManager missionManager;

    public static class SectorCeiling {
        public final Sector sector;
        public final OptionalDouble maxHeightWgs84;

        public SectorCeiling() {
            sector = Sector.EMPTY_SECTOR;
            maxHeightWgs84 = OptionalDouble.empty();
        }

        public SectorCeiling(Sector sector, OptionalDouble maxHeightWgs84) {
            this.sector = sector;
            this.maxHeightWgs84 = maxHeightWgs84;
        }

        public SectorCeiling(Sector sector, double maxHeightWgs84) {
            this.sector = sector == null ? Sector.EMPTY_SECTOR : sector;
            this.maxHeightWgs84 =
                maxHeightWgs84 == Double.NEGATIVE_INFINITY ? OptionalDouble.empty() : OptionalDouble.of(maxHeightWgs84);
        }

        public SectorCeiling(Sector sector) {
            this.sector = sector;
            this.maxHeightWgs84 = OptionalDouble.empty();
        }

        public SectorCeiling(Iterable<? extends MissionInfo> sectors) {
            if (sectors == null) {
                sector = Sector.EMPTY_SECTOR;
                maxHeightWgs84 = OptionalDouble.empty();
                return;
            }

            double maxHeightWgs84 = Double.NEGATIVE_INFINITY;

            LinkedList<Sector> sectorsList = new LinkedList<>();

            for (MissionInfo entry : sectors) {
                Sector s = entry.getSector();
                if (s == null) {
                    continue;
                }

                sectorsList.add(s);
                double m = entry.getMaxElev().orElse(Double.NEGATIVE_INFINITY);
                if (maxHeightWgs84 < m) {
                    maxHeightWgs84 = m;
                }
            }

            this.sector = sectorsList.isEmpty() ? Sector.EMPTY_SECTOR : Sector.union(sectorsList);
            this.maxHeightWgs84 =
                maxHeightWgs84 == Double.NEGATIVE_INFINITY ? OptionalDouble.empty() : OptionalDouble.of(maxHeightWgs84);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SectorCeiling that = (SectorCeiling)o;
            return Objects.equals(sector, that.sector) && Objects.equals(maxHeightWgs84, that.maxHeightWgs84);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sector, maxHeightWgs84);
        }
    }

    @Inject
    public LocateMeApi(IApplicationContext applicationContext, IMissionManager missionManager) {
        this.applicationContext = applicationContext;
        this.missionManager = missionManager;
    }

    public @Nullable SectorCeiling tryLocateCurrentPosition() {
        return tryDeterminePosition(this::byPlane, this::byBackend, this::byMissionSector, this::byMissionOverview);
    }

    private LocateMeApi.SectorCeiling tryDeterminePosition(Supplier<SectorCeiling>... positionCalculators) {
        int calculatorIndex = 0;
        for (Supplier<SectorCeiling> positionCalculator : positionCalculators) {
            SectorCeiling val = positionCalculator.get();
            if (val != null && val.sector != null && !val.sector.equals(Sector.EMPTY_SECTOR)) {
                LOGGER.info(
                    "Successfully determined position/height with calculator[{}]: {}",
                    calculatorIndex,
                    val.sector,
                    val.maxHeightWgs84);
                return val;
            }

            calculatorIndex++;
        }

        return null;
    }

    private LocateMeApi.SectorCeiling byPlane() {
        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission != null) {
            IAirplane airplane = currentMission.getLegacyPlane();
            if (airplane != null) {
                try {
                    if (airplane.getAirplaneCache().getPositionOrientation().fromSession) {
                        return null;
                    }
                } catch (AirplaneCacheEmptyException e) {
                }

                try {
                    return sectorOfPoint(airplane.getAirplaneCache().getCurPos());
                } catch (AirplaneCacheEmptyException e) {
                    return null;
                }
            }
        }

        return null;
    }

    private LocateMeApi.SectorCeiling byBackend() {
        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission != null) {
            IAirplane airplane = currentMission.getLegacyPlane();
            if (airplane != null) {
                try {
                    Backend backend = airplane.getAirplaneCache().getBackend();
                    if (backend.fromSession) {
                        return null;
                    }

                    return sectorOfPoint(
                        new Position(
                            Angle.fromDegreesLatitude(backend.lat),
                            Angle.fromDegreesLongitude(backend.lon),
                            backend.alt / 100));
                } catch (AirplaneCacheEmptyException e) {
                    return null;
                }
            }
        }

        return null;
    }

    private LocateMeApi.SectorCeiling byPilot() {
        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission != null) {
            IAirplane airplane = currentMission.getLegacyPlane();
            if (airplane != null) {
                try {
                    AndroidState androidState = airplane.getAirplaneCache().getAndroidState();
                    if (androidState.fromSession) {
                        return null;
                    }

                    return sectorOfPoint(
                        new Position(
                            Angle.fromDegreesLatitude(androidState.lat),
                            Angle.fromDegreesLongitude(androidState.lon),
                            androidState.alt / 100));
                } catch (AirplaneCacheEmptyException e) {
                    return null;
                }
            }
        }

        return null;
    }

    private SectorCeiling byMissionSector() {
        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission != null) {
            return new SectorCeiling(currentMission.getSector(), currentMission.getMaxElev());
        }

        return null;
    }

    private LocateMeApi.SectorCeiling byMissionOverview() {
        if (applicationContext.getCurrentMission() == null) {
            try (LockedList<MissionInfo> missionInfos = missionManager.recentMissionInfosProperty().lock()) {
                return new SectorCeiling(missionInfos);
            }
        }

        return null;
    }

    private SectorCeiling byEarthGlobe() {
        return new SectorCeiling(Sector.FULL_SPHERE);
    }

    private SectorCeiling sectorOfPoint(Position curPos) {
        return new SectorCeiling(
            new Sector(curPos.getLatitude(), curPos.getLatitude(), curPos.getLongitude(), curPos.getLongitude()),
            curPos.elevation);
    }

}
