/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.coordinatetransform.MapProjection.ProjectionType;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.PointValuePair;
import org.gdal.osr.SpatialReference;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.Vector;

public class ProjectionOptimizer {
    public Vector<Position> orgPositionsWgs84;
    public Vector<Vec4> targetPositionsUnknownProjection;

    private SpatialReference srsWgs84;
    private SpatialReference srsTargetCurrent;
    private Vector<EcefCoordinate> orgXyz;

    private UnitOfMeasure unit;

    private static enum OptimizeFor {
        Latitude,
        Longitude,
        LongLat
    }

    /*
     * Constructor
     */
    public ProjectionOptimizer(
            Vector<Position> orgPositionsWgs84,
            Vector<Vec4> targetPositionsUnknownProjection,
            SpatialReference srsTargetInitial,
            UnitOfMeasure unit) {
        this.orgPositionsWgs84 = orgPositionsWgs84;
        this.targetPositionsUnknownProjection = targetPositionsUnknownProjection;
        this.orgXyz = EcefCoordinate.fromPositionVector(orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid);

        this.srsWgs84 = new SpatialReference();
        this.srsWgs84.SetGeogCS(
            "WGS84Test",
            "GCS_WGS84Test",
            "WGS84 Test",
            Ellipsoid.wgs84Ellipsoid.getSemiMajorAxis(),
            Ellipsoid.wgs84Ellipsoid.getInvFlattening());

        this.srsTargetCurrent = srsTargetInitial.Clone();

        this.unit = unit;
    }

    public SpatialReferenceOptimizerResult optimizeProjection(ProjectionType projectionType, boolean rounding) {
        MapProjection mp = new MapProjection(projectionType);
        mp.setUnit(unit);
        double unitscale = (unit != null) ? unit.getFactor() : 1;

        if (projectionType == ProjectionType.None) {
            double err = applyProjectionAndGetError(mp, OptimizeFor.LongLat);
            return new SpatialReferenceOptimizerResult(srsTargetCurrent, mp, null, null, err);
        }

        // guess initial values for projection parameters (use average coordinates):
        double longInit = orgPositionsWgs84.stream().mapToDouble(p -> p.longitude.degrees).average().getAsDouble();
        if (longInit > 180) {
            longInit = longInit - 360;
        }

        double latInit = orgPositionsWgs84.stream().mapToDouble(p -> p.latitude.degrees).average().getAsDouble();
        double falseEastingInit =
            targetPositionsUnknownProjection.stream().mapToDouble(p -> p.x).average().getAsDouble();
        double falseNorthingInit =
            targetPositionsUnknownProjection.stream().mapToDouble(p -> p.y).average().getAsDouble();

        // Get rough estimates for extents of point clouds in org and target Systems and determine scale factor initial
        // value:
        final double long0 = longInit;
        final double lat0 = latInit;
        final double x0 = falseEastingInit;
        final double y0 = falseNorthingInit;
        // TODO: use great circle formula instead
        double orgExtent =
            2.0
                * Math.PI
                * srsWgs84.GetSemiMajor()
                / 360.0
                * orgPositionsWgs84
                    .stream()
                    .mapToDouble(
                        p ->
                            Math.sqrt(
                                (p.longitude.degrees - long0) * (p.longitude.degrees - long0)
                                    + (p.latitude.degrees - lat0) * (p.latitude.degrees - lat0)))
                    .average()
                    .getAsDouble();
        double targetExtent =
            unitscale
                * targetPositionsUnknownProjection
                    .stream()
                    .mapToDouble(p -> Math.sqrt((p.x - x0) * (p.x - x0) + (p.y - y0) * (p.y - y0)))
                    .average()
                    .getAsDouble();
        double[] scaleInitChoices;

        switch (projectionType) {
        case LambertConicConformal1SP:
        case ObliqueStereographic:
        case TransverseMercator:
        case TransverseMercatorSouthOrientated:
        case HotineObliqueMercatorA:
            scaleInitChoices = new double[] {1.00, 3.28, targetExtent / orgExtent};
            falseEastingInit = srsWgs84.GetSemiMajor();

            break;
        default:
            scaleInitChoices = new double[] {1.0};
        }

        Vector<SpatialReferenceOptimizerResult> optVecAll = new Vector<SpatialReferenceOptimizerResult>();
        for (double scaleInit : scaleInitChoices) {
            if (projectionType == ProjectionType.HotineObliqueMercatorA) {
                Vector<SpatialReferenceOptimizerResult> optVec = new Vector<SpatialReferenceOptimizerResult>();
                for (double ang = -89.9; ang < 89.9; ang += 22.4) {
                    optVec.addElement(
                        minimizeProjectionError(
                            mp.withParams(latInit, longInit, scaleInit, 0, 0, ang, 90),
                            new int[] {3, 5},
                            OptimizeFor.LongLat));
                    optVec.addElement(
                        minimizeProjectionError(
                            mp.withParams(latInit, longInit, scaleInit, -falseEastingInit, 0, ang, 90),
                            new int[] {3, 5},
                            OptimizeFor.LongLat));
                    optVec.addElement(
                        minimizeProjectionError(
                            mp.withParams(latInit, longInit, scaleInit, 0, 0, ang, -90),
                            new int[] {3, 5},
                            OptimizeFor.LongLat));
                    optVec.addElement(
                        minimizeProjectionError(
                            mp.withParams(latInit, longInit, scaleInit, falseEastingInit, 0, ang, -90),
                            new int[] {3, 5},
                            OptimizeFor.LongLat));
                }

                MapProjection optMapProject =
                    optVec.stream().min(Comparator.comparing(por -> por.err)).get().mapProjection;
                Ensure.notNull(optMapProject, "optMapProject");
                double[] optParams = optMapProject.params();

                SpatialReferenceOptimizerResult optRes1 =
                    minimizeProjectionError(mp.withParams(optParams), new int[] {0, 2, 3}, OptimizeFor.LongLat);
                Ensure.notNull(optRes1.mapProjection, "optRes1.mapProjection");
                optParams = optRes1.mapProjection.params();
                SpatialReferenceOptimizerResult optRes2 =
                    minimizeProjectionError(mp.withParams(optParams), new int[] {1, 2, 4}, OptimizeFor.LongLat);
                Ensure.notNull(optRes2.mapProjection, "optRes2.mapProjection");
                optParams = optRes2.mapProjection.params();
                mp.setParams(optParams);
            } else if (projectionType == ProjectionType.LambertConicConformal2SP) {
                double sp1Init = latInit + 2;
                double sp2Init = latInit + 1;
                double[] optParams = {
                    latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, sp1Init, sp2Init
                };

                for (int i = 0; i < 3; i++) {
                    optParams = optimizeLatLongProjectionParameters(mp.withParams(optParams), 1);
                    SpatialReferenceOptimizerResult optRes0 =
                        minimizeProjectionError(mp.withParams(optParams), new int[] {0, 4, 5, 6}, OptimizeFor.LongLat);

                    Ensure.notNull(optRes0.mapProjection, "optRes0.mapProjection");
                    optParams = optRes0.mapProjection.params();
                }

                if (rounding) {
                    roundProjectionParameters(mp.withParams(optParams));
                }
            } else {
                // All other projections
                double[] optParams = {latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit};
                optParams = optimizeLatLongProjectionParameters(mp.withParams(optParams), 3);

                if (rounding && optParams != null) {
                    roundProjectionParameters(mp.withParams(optParams));
                }
            }

            double err = applyProjectionAndGetError(mp, OptimizeFor.LongLat);

            optVecAll.addElement(new SpatialReferenceOptimizerResult(srsTargetCurrent, mp, null, null, err));
        }

        // Return best result found:
        return optVecAll.stream().min(Comparator.comparing(por2 -> por2.err)).get();
    }

    // Optimize HotineObliqueMercatorA for rotated system with known origin + yaw angle.
    public SpatialReferenceOptimizerResult optimizeRotatedProjection(double yaw) {
        MapProjection mp = new MapProjection(ProjectionType.HotineObliqueMercatorA);
        mp.setUnit(unit);

        double[] scaleInitChoices = new double[] {1.00};
        // guess initial values for projection parameters (use average coordinates):
        double longInit = orgPositionsWgs84.stream().mapToDouble(p -> p.longitude.degrees).average().getAsDouble();
        if (longInit > 180) {
            longInit = longInit - 360;
        }

        double latInit = orgPositionsWgs84.stream().mapToDouble(p -> p.latitude.degrees).average().getAsDouble();
        double falseEastingInit = -srsWgs84.GetSemiMajor();
        double falseNorthingInit = 0;

        double azimuth = yaw;
        double skew = 90;
        if (yaw >= 180) {
            azimuth -= 180;
            skew = -90;
            falseEastingInit *= -1;
        }

        Vector<SpatialReferenceOptimizerResult> optVec = new Vector<SpatialReferenceOptimizerResult>();
        for (double scaleInit : scaleInitChoices) {
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, azimuth, skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, azimuth, -skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, azimuth, skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, -azimuth, -skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, -falseEastingInit, falseNorthingInit, azimuth, skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, -falseEastingInit, falseNorthingInit, azimuth, -skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, -falseEastingInit, falseNorthingInit, azimuth, skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, -falseEastingInit, falseNorthingInit, -azimuth, -skew),
                    new int[] {3},
                    OptimizeFor.LongLat));

            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(
                        latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, azimuth + 90, skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(
                        latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, azimuth + 90, -skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(
                        latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, azimuth + 90, skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(
                        latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit, -azimuth + 90, -skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(
                        latInit, longInit, scaleInit, -falseEastingInit, falseNorthingInit, azimuth + 90, skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(
                        latInit, longInit, scaleInit, -falseEastingInit, falseNorthingInit, azimuth + 90, -skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(
                        latInit, longInit, scaleInit, -falseEastingInit, falseNorthingInit, azimuth + 90, skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(
                        latInit, longInit, scaleInit, -falseEastingInit, falseNorthingInit, -azimuth + 90, -skew),
                    new int[] {3},
                    OptimizeFor.LongLat));
        }

        MapProjection optMapProject = optVec.stream().min(Comparator.comparing(por -> por.err)).get().mapProjection;
        Ensure.notNull(optMapProject, "optMapProject");
        double[] optParams = optMapProject.params();

        SpatialReferenceOptimizerResult optRes1 =
            minimizeProjectionError(mp.withParams(optParams), new int[] {0, 1, 3}, OptimizeFor.LongLat);
        return optRes1;

        // Return best result found:
        // return optVec.stream().min(Comparator.comparing(por2 -> por2.err)).get();

    }

    private double[] optimizeLatLongProjectionParameters(MapProjection mp, int iterations) {
        double[] currentParams = mp.params();
        double[] optParams = null;
        SpatialReferenceOptimizerResult optRes0;

        double latInit = currentParams[0];
        double longInit = currentParams[1];
        double scaleInit = currentParams[2];
        double falseEastingInit = currentParams[3];
        double falseNorthingInit = currentParams[4];

        for (int i = 0; i < iterations; i++) {
            // Try various combinations of starting values and choose the best result, first separately for x and y
            // directions, then both.
            Vector<SpatialReferenceOptimizerResult> optVec;

            // Optimize longOrigin and falseEasting:
            optVec = new Vector<SpatialReferenceOptimizerResult>();
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, 0.0, scaleInit, 0.0, falseNorthingInit),
                    new int[] {1, 3},
                    OptimizeFor.Longitude));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, 0.0, falseNorthingInit),
                    new int[] {1, 3},
                    OptimizeFor.Longitude));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, 0.0, scaleInit, falseEastingInit, falseNorthingInit),
                    new int[] {1, 3},
                    OptimizeFor.Longitude));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit),
                    new int[] {1, 3},
                    OptimizeFor.Longitude));

            MapProjection optMapProject1 =
                optVec.stream().min(Comparator.comparing(optRes1 -> optRes1.err)).get().mapProjection;
            Ensure.notNull(optMapProject1, "optMapProject1");
            optParams = optMapProject1.params();
            longInit = optParams[1];
            falseEastingInit = optParams[3];

            // Optimize latOrigin and falseNorthing:
            optVec = new Vector<SpatialReferenceOptimizerResult>();
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(0.001, longInit, scaleInit, falseEastingInit, 0.0),
                    new int[] {0, 4},
                    OptimizeFor.Latitude));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(0.001, longInit, scaleInit, falseEastingInit, falseNorthingInit),
                    new int[] {0, 4},
                    OptimizeFor.Latitude));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, falseEastingInit, 0.0),
                    new int[] {0, 4},
                    OptimizeFor.Latitude));
            optVec.addElement(
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit),
                    new int[] {0, 4},
                    OptimizeFor.Latitude));

            MapProjection optMapProject11 =
                optVec.stream().min(Comparator.comparing(optRes1 -> optRes1.err)).get().mapProjection;
            Ensure.notNull(optMapProject11, "optMapProject11");
            optParams = optMapProject11.params();
            latInit = optParams[0];
            falseNorthingInit = optParams[4];

            // Optimize both:
            optRes0 =
                minimizeProjectionError(
                    mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit),
                    new int[] {0, 1, 3, 4},
                    OptimizeFor.LongLat);
            // optParams = (optRes0.mapProjection==null)?null:optRes0.mapProjection.params();
            MapProjection mapProject0 = optRes0.mapProjection;
            Ensure.notNull(mapProject0, "mapProject0");
            optParams = mapProject0.params();

            // Otherwise not implemented in PROJ.4
            if ((mp.projectionType != ProjectionType.Mercator) || (latInit == 0.0)) {
                latInit = optParams[0];
                longInit = optParams[1];
                falseEastingInit = optParams[3];
                falseNorthingInit = optParams[4];

                // optimize scale factor:
                optRes0 =
                    minimizeProjectionError(
                        mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit),
                        new int[] {2},
                        OptimizeFor.LongLat);
                optParams = (optRes0.mapProjection == null) ? null : optRes0.mapProjection.params();
            }
        }

        if (optParams == null) {
            return new double[0];
        }

        return optParams;
    }

    private void roundProjectionParameters(MapProjection mp) {
        double[] currentParams = mp.params();

        // Round to 0.5:
        double longInit = Math.round(2.0 * currentParams[1]) / 2.0;

        if (longInit > 180.0) {
            longInit -= 360.0;
        }

        double scaleInit = new BigDecimal(Math.abs(currentParams[2])).round(new MathContext(4)).doubleValue();

        // Round false easting and northing to 4 significant digits and multiples of 0.333:
        double falseEastingInit =
            Math.round(new BigDecimal(currentParams[3]).round(new MathContext(4)).doubleValue() * 3.0) / 3.0;
        if (falseEastingInit < 0.1) {
            falseEastingInit = 0.0;
        }

        double falseNorthingInit =
            Math.round(new BigDecimal(currentParams[4]).round(new MathContext(4)).doubleValue() * 3.0) / 3.0;
        if (falseNorthingInit < 0.1) {
            falseNorthingInit = 0.0;
        }

        // Round to 0.5:
        double latInit = Math.round(2.0 * currentParams[0]) / 2.0;

        // Get error with optimized values:
        double err1 =
            applyProjectionAndGetError(
                mp.withParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit), OptimizeFor.LongLat);

        // Use rounded values if it does not increase the error significantly:
        double falseEastingInit2 = new BigDecimal(currentParams[3]).round(new MathContext(1)).doubleValue();
        if (falseEastingInit2 < 0.1) {
            falseEastingInit2 = 0.0;
        }

        double falseNorthingInit2 = new BigDecimal(currentParams[4]).round(new MathContext(1)).doubleValue();
        if (falseNorthingInit2 < 0.1) {
            falseNorthingInit2 = 0.0;
        }

        double err2 =
            applyProjectionAndGetError(
                mp.withParams(latInit, longInit, scaleInit, falseEastingInit2, falseNorthingInit2),
                OptimizeFor.LongLat);
        if (err2 * 0.99 > err1) {
            mp.setParams(latInit, longInit, scaleInit, falseEastingInit, falseNorthingInit);
        }
    }

    // Optimize Srs via BWPs with given projection and ellipsoid
    public SpatialReferenceOptimizerResult getOptimizedSpatialReferenceWithProjection(
            MapProjection mapProjection, Ellipsoid ellipsoid, double[] bwp) {
        SpatialReference srsUnprojected = new SpatialReference();
        srsUnprojected.SetGeogCS(
            "UserOptimized",
            "GCS_UserOptimized",
            ellipsoid.getName(),
            ellipsoid.getSemiMajorAxis(),
            ellipsoid.getInvFlattening());
        SpatialReference srsProjected = mapProjection.setSrsProjection(srsUnprojected.Clone());
        SpatialReference srsProjectedNoBwp = srsProjected.Clone();

        // Error function for optimizer:
        MultivariateFunction errFncWithBwp =
            new MultivariateFunction() {
                public double value(double[] bwp) {
                    srsProjected.SetTOWGS84(bwp[0], bwp[1], bwp[2], bwp[3], bwp[4], bwp[5], bwp[6]);
                    return Helpers.getDeviationWithSrs(
                        orgPositionsWgs84, targetPositionsUnknownProjection, srsProjected);
                }
            };

        double err;
        double[] bwpOpt = null;
        if (bwp == null) {
            // No BWP given, auto-optimize

            // No BWP:
            double errNoBwp =
                Helpers.getDeviationWithSrs(orgPositionsWgs84, targetPositionsUnknownProjection, srsProjectedNoBwp);

            if (ellipsoid == Ellipsoid.wgs84Ellipsoid) {
                return new SpatialReferenceOptimizerResult(srsProjectedNoBwp, mapProjection, ellipsoid, null, errNoBwp);
            } else {
                // Initial value for BWP:
                Vector<Position> targetPositions =
                    Helpers.convertVec4ToPositions(
                        Helpers.transform(targetPositionsUnknownProjection, srsProjected, srsUnprojected));
                Vector<EcefCoordinate> targetXyz = EcefCoordinate.fromPositionVector(targetPositions, ellipsoid);

                double[] bwpInit = BursaWolfeParameters.calculateFromSamples(orgXyz, targetXyz);
                // srsProjected.SetTOWGS84(bwpInit[0], bwpInit[1], bwpInit[2], bwpInit[3], bwpInit[4], bwpInit[5],
                // bwpInit[6]);
                // double errInit = errFncWithBwp.value(bwpInit);

                // Optimize 7-Parameter BWP:
                PointValuePair optimum = NonlinearOptimizer.minimize(errFncWithBwp, bwpInit, 500);
                bwpOpt = optimum.getPoint();
                err = optimum.getValue();
                srsProjected.SetTOWGS84(bwpOpt[0], bwpOpt[1], bwpOpt[2], bwpOpt[3], bwpOpt[4], bwpOpt[5], bwpOpt[6]);

                // Use no BWP if it does not change the error significantly
                if ((errNoBwp * 0.99) < err) {
                    return new SpatialReferenceOptimizerResult(
                        srsProjectedNoBwp, mapProjection, ellipsoid, null, errNoBwp);
                }
            }
        } else {
            // BWP given
            err = errFncWithBwp.value(bwp);
            bwpOpt = bwp;
        }

        return new SpatialReferenceOptimizerResult(srsProjected, mapProjection, ellipsoid, bwpOpt, err);
    }

    private SpatialReferenceOptimizerResult minimizeProjectionError(
            MapProjection initialProjection, int[] optimizeParamIndices, OptimizeFor latOrLong) {
        MapProjection currentProjection = initialProjection.clone();

        // Build initial values list:
        double[] initialValues = new double[optimizeParamIndices.length];
        for (int i = 0; i < optimizeParamIndices.length; i++) {
            initialValues[i] = currentProjection.params()[optimizeParamIndices[i]];
        }

        PointValuePair optimum =
            NonlinearOptimizer.minimize(
                new MultivariateFunction() {
                    public double value(double[] point) {
                        for (int i = 0; i < optimizeParamIndices.length; i++) {
                            currentProjection.params()[optimizeParamIndices[i]] = point[i];
                        }

                        return applyProjectionAndGetError(currentProjection, latOrLong);
                    }
                },
                initialValues,
                100);

        double err = optimum.getValue();
        return new SpatialReferenceOptimizerResult(srsTargetCurrent, currentProjection, null, null, err);
    }

    private double applyProjectionAndGetError(MapProjection mapProjection, OptimizeFor latOrLong) {
        srsTargetCurrent = mapProjection.setSrsProjection(srsTargetCurrent);

        Vector<Position> targetPositionsUnprojected =
            Helpers.transformToWgs84Positions(targetPositionsUnknownProjection, srsTargetCurrent);

        double errsum = 0;
        for (int i = 0; i != orgPositionsWgs84.size(); i++) {
            switch (latOrLong) {
            case Latitude:
                errsum +=
                    Math.pow(
                        orgPositionsWgs84
                            .get(i)
                            .latitude
                            .angularDistanceTo(targetPositionsUnprojected.get(i).latitude)
                            .degrees,
                        2);
                break;
            case Longitude:
                errsum +=
                    Math.pow(
                        orgPositionsWgs84
                            .get(i)
                            .longitude
                            .angularDistanceTo(targetPositionsUnprojected.get(i).longitude)
                            .degrees,
                        2);
                break;
            case LongLat:
                errsum +=
                    Math.pow(
                            orgPositionsWgs84
                                .get(i)
                                .latitude
                                .angularDistanceTo(targetPositionsUnprojected.get(i).latitude)
                                .degrees,
                            2)
                        + Math.pow(
                            orgPositionsWgs84
                                .get(i)
                                .longitude
                                .angularDistanceTo(targetPositionsUnprojected.get(i).longitude)
                                .degrees,
                            2);
                break;
            default:
                break;
            }
        }

        return Math.sqrt(errsum / orgPositionsWgs84.size());
    }
}
