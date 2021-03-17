/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.coordinatetransform.MapProjection.ProjectionType;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.SrsManager;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.gdal.osr.SpatialReference;

public class SpatialReferenceOptimizer {
    static int optimizerActualNo;
    private final ISrsManager srsManager = StaticInjector.getInstance(ISrsManager.class);

    /*
     * Try all epsg systems first before using full automatic optimizer.
     */
    public SpatialReferenceOptimizerResult getEpsgOrAutomaticTargetSrs(
            MapProjection mapProjection,
            Ellipsoid ellipsoid,
            double[] bwp,
            Vector<Position> orgPositionsWgs84,
            Vector<Vec4> targetList) {
        SpatialReferenceOptimizerResult res = null;

        double errMin = Double.MAX_VALUE;
        String keyOpt = "";

        // Try all known epsg systems:
        SpatialReference srsTargetRef;
        optimizerActualNo = 0;
        for (String key : srsManager.getReferences().keySet()) { // new String[] {"EPSG:5650"}) {
            optimizerActualNo++;

            if (key.isEmpty()) {
                continue;
            }

            if (srsManager.getReferences().get(key).getNo() >= SrsManager.ID_PRIVATE_MIN) {
                continue;
            }

            if (optimizerActualNo > srsManager.getReferences().size()) {
                return null;
            }

            srsTargetRef = srsManager.getReferences().get(key).getSR();

            // TODO only distinct projections

            if ((srsTargetRef.IsProjected() == 0) || (srsTargetRef.IsCompound() != 0)) {
                continue;
            }

            // quick transformation test without optimizer:
            double err = Helpers.getDeviationWithSrs(orgPositionsWgs84, targetList, srsTargetRef);

            if (Double.isNaN(err) || (err > 100000)) { // 100km threshold
                continue;
            }

            // With Optimizer:

            // Extract projection with parameters and ellipsoid:
            MapProjection mapProj;
            try {
                mapProj = MapProjection.fromSrs(srsTargetRef);
                System.out.println(key + "." + err);
            } catch (IllegalStateException ex) {
                // TODO why this exception?
                System.out.println(key + ": skip, not implemented" + err);
                continue;
            }

            // to stop while debugging
            // if(key.equals("EPSG:32633"))
            // System.out.println(key);

            // Overwrite projection etc if given:
            if ((mapProjection != null)
                    && (mapProjection.projectionType != MapProjection.ProjectionType.Automatic)
                    && (mapProjection.projectionType != MapProjection.ProjectionType.Unknown)) {
                if (mapProjection.projectionType != mapProj.projectionType) {
                    continue;
                }

                // replace epsg projection params if given:
                if ((mapProjection.params() != null) && (mapProjection.params().length > 0)) {
                    mapProj = mapProjection;
                }
            }

            Ellipsoid el = null;
            if (ellipsoid != null) {
                el = ellipsoid;
            } else {
                String elStr = srsTargetRef.GetAttrValue("Spheroid");

                el = new Ellipsoid(elStr, srsTargetRef.GetSemiMajor(), srsTargetRef.GetInvFlattening());
            }

            // Call optimizer:

            // TODO mapProj: generate a List with tested mapProj - parameters (and all other parameters if modified), if
            // already tested:
            // ignore this
            // will make it faster, even if bigger threshold or without one

            SpatialReferenceOptimizerResult currentRes =
                getAutomaticTargetSrs(mapProj, el, bwp, orgPositionsWgs84, targetList);

            Ensure.notNull(currentRes, "currentRes");

            double err1 = currentRes.err;
            if (!Double.isNaN(err1)) {
                System.out.println(key + ": " + err1 + " (errMin already found:" + errMin + ")");

                if (err1 < errMin) {
                    // new global optimum
                    errMin = err1;
                    keyOpt = key;
                    res = currentRes;

                    res.optimizedSrs.SetGeogCS(
                        "UserOptimized (based on " + keyOpt + ")",
                        "GCS_UserOptimized",
                        el.getName(),
                        el.getSemiMajorAxis(),
                        el.getInvFlattening());

                    if (res.optimizedSrs.IsProjected() == 1) {
                        res.optimizedSrs.SetAttrValue("PROJCS", "UserOptimized (based on " + keyOpt + ")");
                    }

                    if (res.bwp != null) {
                        res.optimizedSrs.SetTOWGS84(
                            res.bwp[0], res.bwp[1], res.bwp[2], res.bwp[3], res.bwp[4], res.bwp[5], res.bwp[6]);
                    }
                }
            }
        }

        // Check if fully automatic (no epsg) is significantly better:

        SpatialReferenceOptimizerResult autoRes =
            getAutomaticTargetSrs(mapProjection, ellipsoid, bwp, orgPositionsWgs84, targetList);
        Ensure.notNull(autoRes, "autoRes");
        System.out.println("Automatic non-epsg: " + autoRes.err);

        if ((keyOpt == "") || ((autoRes.err < errMin * 0.99) && (errMin > 0.005))) {
            res = autoRes;

            System.out.println("Using non-epsg optimum: " + res.err);
        } else {
            System.out.println("Using optimum epsg " + keyOpt + ": " + res.err);
        }

        return res;
    }

    /**
     * Attempts to find the spatial reference system (SRS) of a set of target coordinates, given a matching set of
     * coordinates in the WGS84 system. Any of the map projection, map projection parameters, reference ellipsoid and 7
     * parameter Bursa-Wolfe parameters for the target SRS may be given in advance, if known.
     *
     * @param mapProjection The target SRS map projection, use null if not known. If mapProjection.params is null, the
     *     projection parameters will be auto-determined.
     * @param ellipsoid The target SRS ellipsoid, use null if not known.
     * @param bwp The target SRS Bursa-Wolfe-parameters, use null if not known. Must have length=7 otherwise.
     * @param orgPositionsWGS84 Vector of WGS84 coordinates, given as latitude/longitude/elevation values.
     * @param targetList Vector of map coordinates in target SRS, given as x (east), y (north) and z (height) values for
     *     most projection types. Coordinates must correspond to coordinates in orgPositionsWGS84
     * @return Returns the best-fit estimate for the target SRS (optimizedSrs), including map projection, ellipsoid and
     *     Bursa-Wolfe parameters.
     */
    public static SpatialReferenceOptimizerResult getAutomaticTargetSrs(
            MapProjection mapProjection,
            Ellipsoid ellipsoid,
            double[] bwp,
            Vector<Position> orgPositionsWgs84,
            Vector<Vec4> targetList) {
        double errGoal = 0.005; // Skip further optimization if deviation in meter is lower than this value

        Ellipsoid el = ellipsoid;
        if (ellipsoid == null) {
            // Initial guess
            el = Ellipsoid.wgs84Ellipsoid;
        }

        UnitOfMeasure unit = null;
        if (mapProjection != null) {
            unit = mapProjection.getUnit();
        }

        // If unit is unknown, run whole optimizer for some usual units and take best result.
        if ((unit == null) || (unit == UnitOfMeasure.unKnown)) {
            UnitOfMeasure[] unitsToTry = new UnitOfMeasure[] {UnitOfMeasure.meter, UnitOfMeasure.foot};
            SpatialReferenceOptimizerResult optRes = null;
            double minErr = Double.MAX_VALUE;

            if (mapProjection != null) {
                for (UnitOfMeasure testunit : unitsToTry) {
                    MapProjection mapProj = mapProjection;
                    mapProj.setUnit(testunit);
                    // recursive call, with unit set
                    SpatialReferenceOptimizerResult testRes =
                        getAutomaticTargetSrs(mapProj, ellipsoid, bwp, orgPositionsWgs84, targetList);
                    if (testRes.err < minErr * 0.99) // Only use next if significantly better
                    {
                        minErr = testRes.err;
                        optRes = testRes;
                    }

                    if (testRes.err <= errGoal) {
                        break;
                    }
                }
            }

            return optRes;
        }

        // prepare result
        SpatialReference srsTargetInitial = new SpatialReference();
        srsTargetInitial.SetGeogCS("User", "GCS_User", el.getName(), el.getSemiMajorAxis(), el.getInvFlattening());
        ProjectionOptimizer projectionOptimizer =
            new ProjectionOptimizer(orgPositionsWgs84, targetList, srsTargetInitial, unit);

        MapProjection mapProj = mapProjection;
        if ((mapProjection == null) || (mapProjection.projectionType == ProjectionType.Automatic)) {
            // No projection method given, auto-determine method + parameters
            mapProj = findOptimumProjectionMethod(projectionOptimizer, el);
        } else {
            // Projection method given
            if (mapProjection.params().length == 0) {
                // Projection parameters not given, auto-detect them:
                SpatialReferenceOptimizerResult por1 =
                    projectionOptimizer.optimizeProjection(mapProjection.projectionType, false);
                SpatialReferenceOptimizerResult por2 =
                    projectionOptimizer.optimizeProjection(mapProjection.projectionType, true);
                if (por1.err < por2.err) {
                    mapProj = por1.mapProjection;
                } else {
                    mapProj = por2.mapProjection;
                }
            }
        }

        SpatialReferenceOptimizerResult srsTargetRes =
            projectionOptimizer.getOptimizedSpatialReferenceWithProjection(mapProj, el, bwp);

        // Ellipsoid was not given, optimize now that projection is known
        if (ellipsoid == null) {
            // Test known Ellipsoids:
            double minErr = Double.MAX_VALUE;
            for (int i = 0; i < Ellipsoid.knownEllipsoids.length; i++) {
                // recursive call, with ellipsoid set
                SpatialReferenceOptimizerResult targetRes =
                    getAutomaticTargetSrs(mapProj, Ellipsoid.knownEllipsoids[i], bwp, orgPositionsWgs84, targetList);

                // Only use next if significantly better
                if (targetRes.err < minErr * 0.99) {
                    minErr = targetRes.err;
                    el = Ellipsoid.knownEllipsoids[i];
                }

                if (minErr <= errGoal) {
                    break;
                }
            }

            if (minErr > errGoal) {
                // Also test ellipsoid fit:
                Ellipsoid elOpt = getOptimizedEllipsoid(orgPositionsWgs84, targetList, srsTargetRes.optimizedSrs);
                SpatialReferenceOptimizerResult elOptSrsTargetRes =
                    getAutomaticTargetSrs(mapProj, elOpt, bwp, orgPositionsWgs84, targetList);

                // Only use next if significantly better
                if (elOptSrsTargetRes.err < minErr * 0.99) {
                    el = elOpt;
                }
            }

            // if mapProjection was not given, redo projection optimization with improved Ellipsoid
            if ((mapProjection == null) || (mapProjection.projectionType == ProjectionType.Automatic)) {
                mapProj = new MapProjection(ProjectionType.Automatic);
                mapProj.unit = (mapProjection != null) ? mapProjection.getUnit() : null;
            }

            srsTargetRes = getAutomaticTargetSrs(mapProj, el, bwp, orgPositionsWgs84, targetList);
        }

        return srsTargetRes;
    }

    // Optimize rotated System with given lat, lon and yaw angle
    public static SpatialReferenceOptimizerResult getOptimizedRotatedTargetSrs(
            double[] bwp, Vector<Position> orgPositionsWgs84, Vector<Vec4> targetList, double yaw) {
        // Special case: rotated SRS
        Ellipsoid el = Ellipsoid.grs1980;
        UnitOfMeasure unit = null;
        SpatialReference srsTargetInitial = new SpatialReference();
        srsTargetInitial.SetGeogCS("User", "GCS_User", el.getName(), el.getSemiMajorAxis(), el.getInvFlattening());
        ProjectionOptimizer projectionOptimizer =
            new ProjectionOptimizer(orgPositionsWgs84, targetList, srsTargetInitial, unit);

        SpatialReferenceOptimizerResult por1 = projectionOptimizer.optimizeRotatedProjection(yaw);
        MapProjection mapProj = por1.mapProjection;
        Ensure.notNull(mapProj, "mapProj");
        SpatialReferenceOptimizerResult srsTargetRes =
            projectionOptimizer.getOptimizedSpatialReferenceWithProjection(mapProj, el, bwp);

        return srsTargetRes;
    }

    public static int getOptimizerActualNo() {
        return optimizerActualNo;
    }

    public void cancel() {
        optimizerActualNo = srsManager.getReferences().size() + 1;
    }

    private static Vector<SpatialReferenceOptimizerResult> getProjectionMethodAccuracies(
            ProjectionOptimizer po, Ellipsoid ellipsoid) {
        // Optimize parameters for all known projection methods:
        Vector<SpatialReferenceOptimizerResult> porVec = new Vector<SpatialReferenceOptimizerResult>();

        // TODO:
        porVec.add(po.optimizeProjection(ProjectionType.None, false));

        for (int i = 0; i < 2; i++) {
            boolean rounding = (i == 0);

            porVec.add(po.optimizeProjection(ProjectionType.TransverseMercator, rounding));

            if (porVec.get(porVec.size() - 1).err > 1) {
                porVec.add(po.optimizeProjection(ProjectionType.AmericanPolyconic, rounding));
                porVec.add(po.optimizeProjection(ProjectionType.CassiniSoldner, rounding));
                porVec.add(po.optimizeProjection(ProjectionType.LambertEqualArea, rounding));
                porVec.add(po.optimizeProjection(ProjectionType.Mercator, rounding));
                porVec.add(po.optimizeProjection(ProjectionType.ObliqueStereographic, rounding));
                porVec.add(po.optimizeProjection(ProjectionType.HotineObliqueMercatorA, rounding));
                porVec.add(po.optimizeProjection(ProjectionType.LambertConicConformal2SP, rounding));
            }

            // Optimize Bursa-Wolfe-Parameters for each projection method:
            for (SpatialReferenceOptimizerResult por : porVec) {
                SpatialReferenceOptimizerResult srsRes =
                    po.getOptimizedSpatialReferenceWithProjection(por.mapProjection, ellipsoid, null);

                por.err =
                    Helpers.getDeviationWithSrs(
                        po.orgPositionsWgs84, po.targetPositionsUnknownProjection, srsRes.optimizedSrs);

                // Vector<Position> targetPositionsOptimizedWgs84 =
                // Helpers.transformToWgs84Positions(po.targetPositionsUnknownProjection,
                // srsRes.optimizedSrs);
                // por.err = Helpers.getDeviation(po.orgPositionsWGS84, Ellipsoid.wgs84Ellipsoid,
                // targetPositionsOptimizedWgs84,
                // Ellipsoid.wgs84Ellipsoid);
            }
        }

        return porVec;
    }

    private static MapProjection findOptimumProjectionMethod(ProjectionOptimizer po, Ellipsoid ellipsoid) {
        Vector<SpatialReferenceOptimizerResult> porVec = getProjectionMethodAccuracies(po, ellipsoid);

        // Get result with minimum error
        SpatialReferenceOptimizerResult optPor = porVec.stream().min(Comparator.comparing(por -> por.err)).get();

        return optPor.mapProjection;
    }

    private static Ellipsoid getOptimizedEllipsoid(
            Vector<Position> orgPositionsWgs84, Vector<Vec4> targetList, SpatialReference projectedSrs) {
        double[] initialValues = new double[] {6378137, 298.257223563};
        int maxEval = 100;

        SpatialReference srsWgs84 = new SpatialReference();
        srsWgs84.SetGeogCS(
            "UserOptimized",
            "GCS_UserOptimized",
            "User Optimized",
            Ellipsoid.wgs84Ellipsoid.getSemiMajorAxis(),
            Ellipsoid.wgs84Ellipsoid.getInvFlattening());

        MultivariateFunction deviationFnc =
            new MultivariateFunction() {
                @Override
                public double value(double[] point) {
                    // Limit values to prevent gdal errors
                    if (point[1] < 250) {
                        point[1] = 250;
                    }

                    if (point[1] > 350) {
                        point[1] = 350;
                    }

                    Ellipsoid currentEllipsoid = new Ellipsoid("User Optimized", point[0], point[1]);
                    SpatialReference srsTargetCurrent = projectedSrs.Clone();
                    srsTargetCurrent.SetGeogCS(
                        "UserOptimized", "GCS_UserOptimized", currentEllipsoid.getName(), point[0], point[1]);

                    // Vector<Position> targetPositionsCalculated = Helpers.transformToWgs84Positions(targetList,
                    // srsTargetCurrent);
                    // double err = Helpers.getDeviation(orgPositionsWGS84, Ellipsoid.wgs84Ellipsoid,
                    // targetPositionsCalculated,
                    // Ellipsoid.wgs84Ellipsoid);

                    double err = Helpers.getDeviationWithSrs(orgPositionsWgs84, targetList, srsTargetCurrent);

                    return err;
                }
            };

        // Find best fit ellipsoid:
        PointValuePair optimum = NonlinearOptimizer.minimize(deviationFnc, initialValues, maxEval);

        return new Ellipsoid("User Optimized", optimum.getPoint()[0], optimum.getPoint()[1]);
    }

    // Returns a target SRS with optimized Bursa-Wolfe parameters for the given ellipsoid and target data in
    // latitude/longitude/elevation
    // units (no map projection).
    public static SpatialReference getTargetSrsWithOptimizedBwp(
            Vector<EcefCoordinate> orgXyz, Ellipsoid targetEllipsoid, Vector<Position> targetPositionsUnknownSrs) {
        SpatialReference srsTarget = new SpatialReference();
        srsTarget.SetGeogCS(
            "UserOptimized",
            "GCS_UserOptimized",
            targetEllipsoid.getName(),
            targetEllipsoid.getSemiMajorAxis(),
            targetEllipsoid.getInvFlattening());

        Vector<EcefCoordinate> targetXyz =
            EcefCoordinate.fromPositionVector(targetPositionsUnknownSrs, targetEllipsoid);
        double[] bwp = BursaWolfeParameters.calculateFromSamples(orgXyz, targetXyz);
        srsTarget.SetTOWGS84(bwp[0], bwp[1], bwp[2], bwp[3], bwp[4], bwp[5], bwp[6]);

        return srsTarget;
    }

    /*
     * Creates a number of WGS84 test coordinates that are valid in the given test Srs.
     */
    public static Vector<Position> createWgs84CoordinatesPointsValidInSrs(SpatialReference srsTest) {
        SpatialReference srsWgs84 = new SpatialReference();
        srsWgs84.SetWellKnownGeogCS("WGS84");

        // Data points around the globe:
        Vector<Position> globePositionsWgs84 = new Vector<Position>();

        // Check if we know the map origin and use region around it:
        double lat0 = srsTest.GetProjParm("latitude_of_origin");
        double latmin = lat0 - 2;
        if ((lat0 == 0) || (latmin < -89)) {
            latmin = -89;
        }

        double latmax = lat0 + 2;
        if ((lat0 == 0) || (latmax > 89)) {
            latmax = 89;
        }

        double long0 = srsTest.GetProjParm("central_meridian");
        double longmin = long0 - 2;
        if (longmin < -359) {
            longmin = -359;
        }

        double longmax = long0 + 2;
        if (longmax > 359) {
            longmax = 359;
        }

        System.out.println("lat0: " + lat0 + ", long0: " + long0);

        Random rand = new Random();

        for (double latitude = latmin; latitude < latmax; latitude += 0.2) {
            for (double longitude = longmin; longitude < longmax; longitude += 0.2) {
                globePositionsWgs84.add(
                    Position.fromDegrees(
                        latitude + (rand.nextDouble() - 0.5) * 0.1, longitude + (rand.nextDouble() - 0.5) * 0.1, 0.0));
            }
        }

        Vector<Vec4> globeDataWgs84 = Helpers.convertPositionsToVec4(globePositionsWgs84);
        Vector<EcefCoordinate> globeDataEcef =
            EcefCoordinate.fromPositionVector(globePositionsWgs84, Ellipsoid.wgs84Ellipsoid);

        // Data needs to be in correct region, otherwise most SRS will not be valid.
        // As we don't know the valid regions for all SRS, we test transform a lot of points around the globe and only
        // take those with high
        // accuracy.

        // Convert to test SRS:
        Vector<Vec4> testGlobeData = Helpers.transform(globeDataWgs84, srsWgs84, srsTest);
        Vector<Position> globeDataWgs84BackTransformed = Helpers.transformToWgs84Positions(testGlobeData, srsTest);
        Vector<EcefCoordinate> globeDataEcefBackTransformed =
            EcefCoordinate.fromPositionVector(globeDataWgs84BackTransformed, Ellipsoid.wgs84Ellipsoid);

        double maxErr = 1e-6; // 1 um

        // Take only points with good accuracy:
        Vector<Position> positionDataWgs84 =
            IntStream.range(0, globeDataEcef.size())
                .filter(i -> (globeDataEcef.get(i).distanceTo3(globeDataEcefBackTransformed.get(i)) < maxErr))
                .mapToObj(i -> globePositionsWgs84.get(i))
                .collect(Collectors.toCollection(Vector::new));

        return positionDataWgs84;
    }

}
