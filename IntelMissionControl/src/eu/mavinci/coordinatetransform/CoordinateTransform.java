/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.coordinatetransform.MapProjection.ProjectionType;
import eu.mavinci.desktop.helper.gdal.SrsManager;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import org.gdal.osr.SpatialReference;

import java.util.Vector;

public class CoordinateTransform {

    @SuppressWarnings({
        "checkstyle:abbreviationaswordinname",
        "checkstyle:variabledeclarationusagedistance",
        "checkstyle:linelength"
    })
    public static void main(String[] args) {

        // goal is to find a WKT representation of a transformation which fits as best as possible
        // between two given representations of a given list of points.

        Vector<Position> orgPositionsWgs84 = new Vector<Position>();
        orgPositionsWgs84.add(
            Position.fromDegrees(40. + 0. / 60 + 41.43965 / 3600., 111. + 22. / 60. + 0.3491 / 3600., 1127.791));
        orgPositionsWgs84.add(
            Position.fromDegrees(40. + 0. / 60 + 43.72818 / 3600., 111. + 21. / 60. + 11.11078 / 3600., 1162.022));
        orgPositionsWgs84.add(
            Position.fromDegrees(40. + 0. / 60 + 26.0391 / 3600., 111. + 21. / 60. + 13.81876 / 3600., 1161.094));
        orgPositionsWgs84.add(
            Position.fromDegrees(40. + 0. / 60 + 16.94112 / 3600., 111. + 21. / 60. + 29.64453 / 3600., 1126.61));
        orgPositionsWgs84.add(
            Position.fromDegrees(39. + 59. / 60 + 53.90776 / 3600., 111. + 21. / 60. + 58.1318 / 3600., 1046.512));
        orgPositionsWgs84.add(
            Position.fromDegrees(40. + 0. / 60 + 27.69651 / 3600., 111. + 20. / 60. + 23.08642 / 3600., 1186.236));

        Vector<Vec4> targetList = new Vector<Vec4>();
        targetList.add(new Vec4(531198.764, 4430864.131, 1152.419));
        targetList.add(new Vec4(530030.514, 4430930.008, 1186.736));
        targetList.add(new Vec4(530096.917, 4430384.580, 1185.797));
        targetList.add(new Vec4(530473.467, 4430105.422, 1151.277));
        targetList.add(new Vec4(531152.191, 4429397.627, 1071.109));
        targetList.add(new Vec4(528893.236, 4430431.022, 1211.025));

        /**
         * My source data in two systems: Geodetic : WGS84 Unit name : Meters Name Latitude Longitude Height,m 1
         * 40°00'41.43965 111°22'00.3491 1127.791 2 40°00'43.72818 111°21'11.11078 1162.022 5 40°00'26.0391
         * 111°21'13.81876 1161.094 6 40°00'16.94112 111°21'29.64453 1126.61 8 39°59'53.90776 111°21'58.1318 1046.512 9
         * 40°00'27.69651 111°20'23.08642 1186.236 ----------- Right pane : CHINA80 Grid : 111 Unit name : Meters Name
         * Northing,m Easting,m Height,m 1 4430864.131 531198.764 1152.419 2 4430930.008 530030.514 1186.736 5
         * 4430384.580 530096.917 1185.797 6 4430105.422 530473.467 1151.277 8 4429397.627 531152.191 1071.109 9
         * 4430431.022 528893.236 1211.025 -----------
         * Frame：PROJCS["Transverse_Mercator",GEOGCS["GCS_CHINA80",DATUM["CHINA80",SPHEROID["Xian
         * 1980",6378140,298.2569978029111],TOWGS84[-736.4243454,698.0915923,654.0024722,0.6414960749,6.790489356,-8.247774503,-180.8079349]],PRIMEM["Greenwich",0],UNIT["degree",0.0174532925199433]],PROJECTION["Transverse_Mercator"],PARAMETER["scale_factor",1],PARAMETER["central_meridian",111],PARAMETER["latitude_of_origin",0],PARAMETER["false_easting",500000],PARAMETER["false_northing",0],UNIT["Meter",1]]
         * After transformation in Intel Mission Control ΔN ΔE 1 4430864.150 531198.689 -0.019 0.075 2 4430930.027
         * 530030.439 -0.019 0.075 5 4430384.599 530096.842 -0.019 0.075
         */

        //SrsManager.assureInit();

        // Print data
        System.out.println("orgList:");
        for (int i = 0; i < orgPositionsWgs84.size(); i++) {
            System.out.println(
                orgPositionsWgs84.get(i).longitude.degrees
                    + ", "
                    + orgPositionsWgs84.get(i).latitude.degrees
                    + ", "
                    + orgPositionsWgs84.get(i).elevation
                    + ";");
        }

        System.out.println("targetList:");
        for (int i = 0; i < targetList.size(); i++) {
            System.out.println(targetList.get(i).x + ", " + targetList.get(i).y + ", " + targetList.get(i).z + ";");
        }

        SpatialReference srsOrg =
            new SpatialReference(
                "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");
        System.out.println("Orig SRS: \n" + srsOrg);

        SpatialReference srsWGS84 = new SpatialReference();
        srsWGS84.SetWellKnownGeogCS("WGS84");
        double semiMajorAxis = srsWGS84.GetSemiMajor();
        System.out.println("WGS84 SemiMajorAxis: " + semiMajorAxis);

        Vector<EcefCoordinate> orgXYZ = EcefCoordinate.fromPositionVector(orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid);

        // implemented in newer gdal version only:
        SpatialReference srsGeocentricECEF = new SpatialReference();
        // srsGeocentricECEF.ImportFromEPSG(4978); //still does not work...
        srsGeocentricECEF.ImportFromWkt(
            "GEOCCS[\"WGS 84\",DATUM[\"World Geodetic System 1984\",SPHEROID[\"WGS 84\",6378137.0,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0.0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"m\",1.0],AXIS[\"Geocentric X\",OTHER],AXIS[\"Geocentric Y\",EAST],AXIS[\"Geocentric Z\",NORTH],AUTHORITY[\"EPSG\",\"4978\"]]");

        // Vector<Vec4> orgListECEF = transformArray(convertPositionsToArray(orgList), srsOrg, srsGeocentricECEF);
        System.out.println(srsGeocentricECEF.ExportToPrettyWkt());

        double err;

        // Test accuracy. Use GDAL transform to transform all data to WGS84 using various SpatialReferences for
        // transform, then calculate
        // Euclidian distance deviation.
        Vector<Position> targetPositions;

        // China80 + Given BWPs from WKT:
        SpatialReference srsChina80TMGivenBwpFromWkt =
            new SpatialReference(
                "PROJCS[\"Transverse_Mercator\",GEOGCS[\"GCS_CHINA80\",DATUM[\"CHINA80\",SPHEROID[\"Xian 1980\",6378140,298.2569978029111],TOWGS84[-736.4243454,698.0915923,654.0024722,0.6414960749,6.790489356,-8.247774503,-180.8079349]],PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"scale_factor\",1],PARAMETER[\"central_meridian\",111],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"Meter\",1]]");
        System.out.println("Target SRS: \n" + srsChina80TMGivenBwpFromWkt);
        targetPositions = Helpers.transformToWgs84Positions(targetList, srsChina80TMGivenBwpFromWkt);
        err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions, Ellipsoid.wgs84Ellipsoid);
        System.out.println("Test SRS (China80 + given BWP from WKT) error: ");
        System.out.println(err);

        // China80 + Given BWPs:
        SpatialReference srsChina80TMGivenBwp = new SpatialReference();
        srsChina80TMGivenBwp.SetGeogCS("CHINA80", "GCS_CHINA80", "Xian 1980", 6378140, 298.2569978029111);
        srsChina80TMGivenBwp.SetTM(0, 111.0, 1.0, 500000, 0);
        srsChina80TMGivenBwp.SetTOWGS84(
            -736.4243454, 698.0915923, 654.0024722, 0.6414960749, 6.790489356, -8.247774503, -180.8079349);

        // System.out.println("Target SRS: \n" + srsChina80TMGivenBwp);
        targetPositions = Helpers.transformToWgs84Positions(targetList, srsChina80TMGivenBwp);
        err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions, Ellipsoid.wgs84Ellipsoid);
        System.out.println("Test SRS (China80 + given BWP) error: ");
        System.out.println(err);

        // China80 + no BWPs:
        SpatialReference srsChina80TMnoBwp = new SpatialReference();
        srsChina80TMnoBwp.SetGeogCS("CHINA80", "GCS_CHINA80", "Xian 1980", 6378140, 298.2569978029111);
        // transverse mercator projection, central longitude 111°, false easting 500000m, scale 1.0.
        srsChina80TMnoBwp.SetTM(0, 111.0, 1.0, 500000, 0);

        SpatialReference srsChina80UnprojectedNoBwp = new SpatialReference();
        srsChina80UnprojectedNoBwp.SetGeogCS("CHINA80", "GCS_CHINA80", "Xian 1980", 6378140, 298.2569978029111);

        // Remove transverse mercator (using China80 ellipsoid) from target data:
        Vector<Vec4> targetChina80Unprojected =
            Helpers.transform(targetList, srsChina80TMnoBwp, srsChina80UnprojectedNoBwp);
        Vector<Position> targetPositionsChina80Unprojected = Helpers.convertVec4ToPositions(targetChina80Unprojected);

        targetPositions = Helpers.transformToWgs84Positions(targetChina80Unprojected, srsChina80UnprojectedNoBwp);
        err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions, Ellipsoid.wgs84Ellipsoid);
        System.out.println("Test SRS (China80 + no BWP) error: ");
        System.out.println(err);

        // China80 + optimized BWPs
        Ellipsoid china80Ellipsoid = new Ellipsoid("Xian 1980", 6378140, 298.2569978029111);
        SpatialReference srsChina80TMoptimizedBWP =
            SpatialReferenceOptimizer.getTargetSrsWithOptimizedBwp(
                orgXYZ, china80Ellipsoid, targetPositionsChina80Unprojected);
        targetPositions = Helpers.transformToWgs84Positions(targetChina80Unprojected, srsChina80TMoptimizedBWP);
        err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions, Ellipsoid.wgs84Ellipsoid);
        System.out.println(srsChina80TMoptimizedBWP);
        System.out.println("Test SRS (China80 + optimized BWP) error: ");
        System.out.println(err);

        // WGS84 + Given BWPs:
        SpatialReference srsWgs84TMGivenBwp = new SpatialReference();
        srsWgs84TMGivenBwp.SetGeogCS(
            "WGS84Test",
            "GCS_WGS84Test",
            "WGS84 Test",
            Ellipsoid.wgs84Ellipsoid.getSemiMajorAxis(),
            Ellipsoid.wgs84Ellipsoid.getInvFlattening());
        srsWgs84TMGivenBwp.SetTM(0, 111.0, 1.0, 500000, 0);
        srsWgs84TMGivenBwp.SetTOWGS84(
            -736.4243454, 698.0915923, 654.0024722, 0.6414960749, 6.790489356, -8.247774503, -180.8079349);
        targetPositions = Helpers.transformToWgs84Positions(targetList, srsWgs84TMGivenBwp);
        err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions, Ellipsoid.wgs84Ellipsoid);
        System.out.println("Test SRS (WGS84 + given BWP) error: ");
        System.out.println(err);

        // WGS84 + no BWP:
        SpatialReference srsWgs84TMnoBwp = new SpatialReference();
        srsWgs84TMnoBwp.SetGeogCS(
            "WGS84Test",
            "GCS_WGS84Test",
            "WGS84 Test",
            Ellipsoid.wgs84Ellipsoid.getSemiMajorAxis(),
            Ellipsoid.wgs84Ellipsoid.getInvFlattening());
        srsWgs84TMnoBwp.SetTM(0, 111.0, 1.0, 500000, 0);
        targetPositions = Helpers.transformToWgs84Positions(targetList, srsWgs84TMnoBwp);
        err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions, Ellipsoid.wgs84Ellipsoid);
        System.out.println("Test SRS (WGS84 + no BWP) error: ");
        System.out.println(err);

        SpatialReference srsWgs84UnprojectedNoBwp = new SpatialReference();
        srsWgs84UnprojectedNoBwp.SetGeogCS(
            "WGS84Test",
            "GCS_WGS84Test",
            "WGS84 Test",
            Ellipsoid.wgs84Ellipsoid.getSemiMajorAxis(),
            Ellipsoid.wgs84Ellipsoid.getInvFlattening());

        // WGS84 + optimized BWP
        // Remove transverse mercator (using WGS84 ellipsoid) from target data:
        Vector<Vec4> targetWgs84Unprojected = Helpers.transform(targetList, srsWgs84TMnoBwp, srsWgs84UnprojectedNoBwp);
        Vector<Position> targetPositionsWgs84Unprojected = Helpers.convertVec4ToPositions(targetWgs84Unprojected);

        SpatialReference srsWgs84TMoptimizedBWP =
            SpatialReferenceOptimizer.getTargetSrsWithOptimizedBwp(
                orgXYZ, Ellipsoid.wgs84Ellipsoid, targetPositionsWgs84Unprojected);
        targetPositions = Helpers.transformToWgs84Positions(targetWgs84Unprojected, srsWgs84TMoptimizedBWP);
        err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions, Ellipsoid.wgs84Ellipsoid);
        System.out.println("Test SRS (WGS84 + optimized BWP) error: ");
        System.out.println(err);

        System.out.println("\n\n");

        // UI backend Test:

        MapProjection mapProjection = new MapProjection(ProjectionType.Automatic);
        // MapProjection mapProjection = new MapProjection(ProjectionType.TransverseMercator);
        // MapProjection mapProjection = new MapProjection(ProjectionType.TransverseMercator, new double[] {0.0, 111.0,
        // 1.0, 500000.0,
        // 0.0});

        // Ellipsoid ellipsoid = new Ellipsoid("Xian 1980", 6378140, 298.2569978029111);
        // Ellipsoid ellipsoid = Ellipsoid.wgs84Ellipsoid; //Default
        Ellipsoid ellipsoid = null; // Auto

        // Bursa-Wolfe-parameters (7 numbers or null):
        double[] bwp = null; // auto
        // double[] bwp = { -736.4243454,698.0915923,654.0024722,0.6414960749,6.790489356,-8.247774503,-180.8079349 };

        SpatialReferenceOptimizerResult spatialReferenceOptimizerResult =
            SpatialReferenceOptimizer.getAutomaticTargetSrs(
                mapProjection, ellipsoid, bwp, orgPositionsWgs84, targetList);
        Ensure.notNull(spatialReferenceOptimizerResult, "spatialReferenceOptimizerResult");
        SpatialReference srsTargetAutomatic = spatialReferenceOptimizerResult.optimizedSrs;

        System.out.println("Auto determined SRS: " + srsTargetAutomatic);

        targetPositions = Helpers.transformToWgs84Positions(targetList, srsTargetAutomatic);
        err =
            Helpers.getDeviation(
                orgPositionsWgs84, Ellipsoid.wgs84Ellipsoid, targetPositions, Ellipsoid.wgs84Ellipsoid);
        System.out.println("Auto determined SRS error (average deviation in meters): ");
        System.out.println(err);
    }

}
