/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import com.intel.missioncontrol.airspaces.sources.AirspaceSource;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EAirspaceManager {
    private AirspaceSource airspaceSource;

    private WeakListenerList<IAirspaceListener> listeners = new WeakListenerList<>("airspaceListeners");

    public static String KEY = "AirspaceManager";
    private static EAirspaceManager instance;
    private WWGroundProvider groundProvider;

    private EAirspaceManager() {}

    public static void install(AirspaceSource airspaceSource) {
        if (instance == null) {
            instance = new EAirspaceManager();
            instance.airspaceSource = airspaceSource;
        }
    }

    public static EAirspaceManager instance() {
        Ensure.notNull(instance, "instance not installed jet");

        if (instance.groundProvider == null) {
            instance.initGroundProvider();
        }

        return instance;
    }

    private void initGroundProvider() {
        groundProvider = new WWGroundProvider();
        AirspaceManager.setGroundProvider(groundProvider);
    }

    public List<IAirspace> getAllAirspacesInCache() {
        return getAirspacesInCache(Sector.fromDegrees(-90, 90, -180, 179.99));
    }

    public List<IAirspace> getAirspacesInCache(Sector bbox) {
        Ensure.notNull(airspaceSource, "airspaceSource[perhaps search in cache functionality is not supported here]");
        List<IAirspace> list = airspaceSource.getCachedAirspacesWithin(bbox);
        list.add(new GolfUpperBoundAirspace());
        return list;
    }

    public List<IAirspace> getAirspaces(Sector bbox) {
        Ensure.notNull(airspaceSource, "airspaceSource[perhaps search in cache functionality is not supported here]");
        List<IAirspace> list = airspaceSource.getAirspacesWithin(bbox);
        list.add(new GolfUpperBoundAirspace());
        return list;
    }

    public static void fireAirspacesChangedStatic() {
        EAirspaceManager instance = EAirspaceManager.instance;
        if (instance != null) {
            instance.fireAirspacesChanged();
        }
    }

    private void fireAirspacesChanged() {
        for (IAirspaceListener listener : listeners) {
            listener.airspacesChanged();
        }
    }

    public void addAirspaceListListener(IAirspaceListener listener) {
        listeners.add(listener);
    }

    public static Sector getSector(List<IAirspace> list) {
        Sector s = null;
        for (IAirspace as : list) {
            if (as instanceof GolfUpperBoundAirspace) {
                continue;
            }

            Sector nextSector = as.getBoundingBox();
            if (s == null) {
                s = nextSector;
            } else {
                s = s.union(nextSector);
            }
        }

        return s;
    }

    public static LowestAirspace getMaxMAVAltitude(
            List<IAirspace> airspaces, Sector bb, double groundLevelElevationEGM) {
        LowestAirspace lowestAirspace = new LowestAirspace(groundLevelElevationEGM);
        LatLon leftBottom = new LatLon(bb.getMinLatitude(), bb.getMinLongitude());
        LatLon rightBottom = new LatLon(bb.getMinLatitude(), bb.getMaxLongitude());
        LatLon leftTop = new LatLon(bb.getMaxLatitude(), bb.getMinLongitude());
        LatLon rightTop = new LatLon(bb.getMaxLatitude(), bb.getMaxLongitude());
        for (IAirspace a : getAirspacesForAltitudeCalculation(airspaces)) {
            if (!a.getType().isMAVAllowed()) {
                lowestAirspace.computeOther(a, leftBottom);
                lowestAirspace.computeOther(a, rightBottom);
                lowestAirspace.computeOther(a, leftTop);
                lowestAirspace.computeOther(a, rightTop);
            }
        }

        return lowestAirspace;
    }

    private static Set<IAirspace> getAirspacesForAltitudeCalculation(List<IAirspace> airspaces) {
        Set<IAirspace> allAirspacesForAltCalculation = new HashSet<>(airspaces);
        // allAirspacesForAltCalculation.add(new GolfUpperBoundAirspace());
        return allAirspacesForAltCalculation;
    }

    public static String GOLF_CHANGED_EVENT = "GOLF_CHANGED_EVENT";

    public static void setGolfMeters(double metersRelative, double metersAbsolute) {
        GolfUpperBoundAirspace.GOLF_FLOOR_METERS_REL = metersRelative;
        GolfUpperBoundAirspace.GOLF_FLOOR_METERS_ABS = metersAbsolute;
        MvvmFX.getNotificationCenter().publish(GOLF_CHANGED_EVENT);
    }

    /**
     * Provides method to get ground elevation from some topography model relative to EGM960.
     *
     * @param latitude in degrees north [-90,90]
     * @param lonngitude in degrees east (-180,180]
     * @return the ground elevation in meters at this point
     */
    @SuppressWarnings("checkstyle:abbreviationaswordinname")
    private static class WWGroundProvider implements IGroundAltProvider {

        static final double bestRes_rad = 100. / Earth.WGS84_EQUATORIAL_RADIUS;

        IElevationModel elevationModel;
        IEgmModel egmModel;

        /**
         * Method to get ground elevation from some topography model relative to EGM960.
         *
         * @param latitude in degrees north [-90,90]
         * @param lonngitude in degrees east (-180,180]
         * @return the ground elevation in meters at this point
         */
        @Override
        public double groundElevationMetersEGM(double latDegrees, double lonDegrees) {
            if (latDegrees > 90) {
                Debug.getLog().log(Debug.WARNING, "Latitudes out of range [-90,90]," + latDegrees);
            }

            if (latDegrees < -90) {
                Debug.getLog().log(Debug.WARNING, "Latitudes out of range [-90,90]," + latDegrees);
            }

            if (lonDegrees > 180) {
                Debug.getLog().log(Debug.WARNING, "Longitudes out of range (-180,180]," + lonDegrees);
            }

            if (lonDegrees <= -180) {
                Debug.getLog().log(Debug.WARNING, "Longitudes out of range (-180,180]," + lonDegrees);
            }

            // System.out.println("bestRes " + bestRes + " sectorIs="+sec);
            gov.nasa.worldwind.geom.LatLon latLon = gov.nasa.worldwind.geom.LatLon.fromDegrees(latDegrees, lonDegrees);

            if (elevationModel == null) {
                elevationModel = DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
            }

            double tmp = elevationModel.getElevationAsGoodAsPossible(latLon);

            if (tmp < -1e3) {
                return 0;
            }

            if (tmp > 1e5) {
                return 0;
            }

            // elevationCache.put(latLon, tmp);
            if (egmModel == null) {
                egmModel = DependencyInjector.getInstance().getInstanceOf(IEgmModel.class);
            }

            double egmOffset = egmModel.getEGM96Offset(latLon);
            // System.out.println("ground: " + latLon + " -> " + (tmp+egmOffset));
            return tmp + egmOffset;
        }
    }
}
