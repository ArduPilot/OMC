/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import eu.mavinci.desktop.main.debug.Debug;
import org.gdal.osr.SpatialReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;

public class MapProjection {
    // See http://docs.opengeospatial.org/is/12-063r5/12-063r5.html , Chapter E.2 Map projection Methods
    // Some methods have variants that are not distinguishable by automatic detection. Only one of each types is used
    // here.

    /*
     * Supported Projection Methods
     */
    public enum ProjectionType {
        // TODO: NATIVE NAMES: e.g. Transverse_Mercator
        /*
         * Automatic detection of projection method
         */
        Automatic("Automatic", -1),
        Unknown("Unknown", -1),

        /*
         * No Projection Method (Data is geodetic or geocentric)
         */
        None("None", 0),

        /*
         * American Polyconic projection. Parameters: Latitude of origin, longitude of origin, 1, false easting, false northing
         */
        AmericanPolyconic("American Polyconic", 9818),
        Polyconic("Polyconic", 9818),

        /*
         * Cassini-Soldner projection. Parameters: Latitude of origin, longitude of origin, 1, false easting, false northing
         */
        CassiniSoldner("Cassini Soldner", 9806),

        /*
         * Hotine Oblique Mercator (Variant A, Rectified skew orthomorphic) projection. Parameters: Latitude of projection centre, Longitude
         * of projection centre, scale factor on initial line, false easting, false northing, azimuth angle of initial line, angle from
         * rectified to skew grid
         */
        HotineObliqueMercatorA("Hotine Oblique Mercator", 9812),
        HotineObliqueMercatorB("Hotine Oblique Mercator Azimuth Center", 9815),
        // 8811, 8812, 8813, 8814, 8815, 8806, 8807
        // 9815: return new int[]{8811, 8812, 8813, 8814, 8815, 8816, 8817};

        /*
         * Lambert Azimuthal Equal Area (LAEA) projection. Parameters: Latitude of origin, longitude of origin, 1, false easting, false
         * northing
         */
        LambertEqualArea("Lambert Azimuthal Equal Area", 9820),

        /*
         * Lambert Conic Conformal (1SP, LCC) projection. Parameters: Latitude of origin, longitude of origin, scale factor, false easting,
         * false northing
         */
        LambertConicConformal1SP("Lambert Conic Conformal (1SP)", 9801),

        /*
         * Lambert Conic Conformal (2SP, LCC) projection. Parameters: Latitude of origin, longitude of origin, 1, false easting, false
         * northing, latitude of 1st standard parallel, latitude of 2nd standard parallel
         */
        LambertConicConformal2SP("Lambert Conic Conformal (2SP)", 9802),

        /*
         * Mercator projection (variant B). Parameters: Latitude of origin, longitude of origin, 1, false easting, false northing
         */
        Mercator("Mercator", 9804),

        Mercator1SP("Mercator1SP", 9804), // TODO
        Mercator2SP("Mercator2SP", 9805), // TODO

        /*
         * Oblique Stereographic projection (Double stereographic). Parameters: Latitude of origin, longitude of origin, scale factor, false
         * easting, false northing
         */
        ObliqueStereographic("Oblique Stereographic", 9809),
        PolarStereographic("Polar Stereographic", 9999), // TODO CORRECT NUMBER
        /*
         * Transverse Mercator projection (Gauss-Boaga, Gauss-Krüger, TM). Parameters: Latitude of origin, longitude of origin, scale
         * factor, false easting, false northing
         */
        TransverseMercator("Transverse Mercator", 9807),

        /*
         * Transverse Mercator projection (South orientated, Gauss-Conform). Parameters: Latitude of origin, longitude of origin, scale
         * factor, false easting, false northing
         */
        TransverseMercatorSouthOrientated("Transverse Mercator (south orientated)", 9808),

        AlbersConicEqualArea("Albers Conic Equal Area", 9822), // TODO

        Krovak("Krovak", 9999), // TODO

        TunisiaMiningGrid("Tunisia Mining Grid", 9805);

        private String description;
        private int epsgMethodCode;

        public static final String CMB_SEPERATOR_ELEMENTS = " - ";
        public static final Pattern CMB_SEPERATOR_ELEMENTS_PAT = Pattern.compile(Pattern.quote(CMB_SEPERATOR_ELEMENTS));

        ProjectionType(String description, int epsgMethodCode) {
            this.description = description;
            this.epsgMethodCode = epsgMethodCode;
        }

        public static ProjectionType fromEpgsMethodCode(int epsgMethodCode) {
            return Arrays.stream(ProjectionType.values())
                .filter(e -> (e.epsgMethodCode == epsgMethodCode))
                .findFirst()
                .get();
        }

        public String toString() {
            return this.description;
        }

        public String toStringLong() {
            return this.description + (this.epsgMethodCode <= 1 ? "" : "," + this.epsgMethodCode);
        }

        public String toStringForCmb() {
            if (epsgMethodCode() <= 0) {
                return toStringLong();
            }

            return "EPSG:" + epsgMethodCode() + " - " + toString();
        }

        public static Vector<String> getProjectionTypesForCmb() {
            Vector<String> projections = new Vector<String>();
            for (int i = 0; i < ProjectionType.values().length; i++) {
                projections.add(getProjectionTypeForCmb(i));
            }

            return projections;
        }

        public static Vector<String> getProjectionTypesForCmbAuto() {
            Vector<String> projections = new Vector<String>();
            for (int i = 0; i < ProjectionType.values().length; i++) {
                if (!getProjectionTypeForCmb(i).equals("Automatic")
                        && !getProjectionTypeForCmb(i).equals("None")
                        && !getProjectionTypeForCmb(i).equals("Unknown")) {
                    projections.add(getProjectionTypeForCmb(i));
                }
            }

            Collections.sort(projections);
            projections.add(0, "Automatic");

            return projections;
        }

        public static Vector<String> getProjectionTypesForCmbNoAuto() {
            Vector<String> projections = new Vector<String>();
            for (int i = 0; i < ProjectionType.values().length; i++) {
                if (!getProjectionTypeForCmb(i).equals("Automatic")
                        && !getProjectionTypeForCmb(i).equals("None")
                        && !getProjectionTypeForCmb(i).equals("Unknown")) {
                    projections.add(getProjectionTypeForCmb(i));
                }
            }

            Collections.sort(projections);
            projections.add(0, "Unknown");
            projections.add(0, "None");

            return projections;
        }

        private static String getProjectionTypeForCmb(int value) {
            return ProjectionType.values()[value].toStringForCmb();
        }

        public static String getProjectionTypeForCmb(String projectionTypeName) {
            if (projectionTypeName == null) {
                return ProjectionType.None.toStringForCmb();
            }

            ProjectionType ref = ProjectionType.getProjectionTypefromString(projectionTypeName);
            if (ref == null) {
                return ProjectionType.None.toStringForCmb();
            }

            return ref.toStringForCmb();
        }

        public static String getProjectionTypeForCmbFromCode(int uomCode) {
            ProjectionType ref;
            if (uomCode == 0) {
                return ProjectionType.None.toStringForCmb();
            }

            ref = ProjectionType.fromEpgsMethodCode(uomCode);
            return ref.toStringForCmb();
        }

        public String toStringNative() {
            switch (this) {
                // TODO set values from WKS where not already defined:

            case Automatic:
                // case Unknown:
            case None:
            case AmericanPolyconic:
            case Polyconic:
            case CassiniSoldner:
            case LambertEqualArea:
            case Mercator: // TODO 3. Parameter ??

            case ObliqueStereographic:
            case PolarStereographic:
            case Krovak:
            case TransverseMercator:
                return this.description.replace(" ", "_");
            case HotineObliqueMercatorA:
                return "Hotine_Oblique_Mercator";
            case HotineObliqueMercatorB:
                return "Hotine_Oblique_Mercator_Azimuth_Center";
                // setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);
            case Mercator1SP:
                return "Mercator_1SP";
            case Mercator2SP:
                return "Mercator_2SP";
            case LambertConicConformal1SP:
                return "Lambert_Conformal_Conic_1SP";
            case LambertConicConformal2SP:
                return "Lambert_Conformal_Conic_2SP";
                // setParams(latitude, longitude, 1.0, falseEasting, falseNorthing, this.params[5], this.params[6]);
            case TransverseMercatorSouthOrientated:
                return "Transverse_Mercator_South_Orientated";
            case AlbersConicEqualArea:
                return "Albers_Conic_Equal_Area";
            case TunisiaMiningGrid:
                return "Tunisia_Mining_Grid";
            default:
                return this.description.replace(" ", "_");
                // TODO ? case 9815: return new int[]{8811, 8812, 8813, 8814, 8815, 8816, 8817};
                // case 9805: return new int[]{8823, 8802, 8806, 8807};
                // case 9822: return new int[]{8821, 8822, 8823, 8824, 8826, 8827};
            }
        }

        public int epsgMethodCode() {
            return this.epsgMethodCode;
        }

        public static ProjectionType getProjectionType(String stringLong) {
            // stringLong: Name,code
            // if(stringLong==null || stringLong.startsWith("Unknown")) return ProjectionType.Unknown;
            if (stringLong.startsWith("Automatic")) {
                return ProjectionType.Automatic;
            }

            if (stringLong.startsWith("None")) {
                return ProjectionType.None;
            }

            int epsgMethodCode = Integer.parseInt(stringLong.substring(stringLong.length() - 4));
            return new MapProjection(epsgMethodCode).projectionType;
        }

        public static ProjectionType getProjectionTypefromString(String string) {
            ProjectionType[] pt = ProjectionType.values();
            for (int i = 0; i < pt.length; i++) {
                // System.out.println(pt[i].toString());
                if (pt[i].toString().equals(string)) {
                    return pt[i];
                }
            }

            return null;
        }

        public static ProjectionType getProjectionTypefromNative(String stringNative) {
            // if(stringNative==null || stringNative.startsWith("Unknown")) return ProjectionType.Unknown;
            if (stringNative.startsWith("Automatic")) {
                return ProjectionType.Automatic;
            }

            if (stringNative.startsWith("None")) {
                return ProjectionType.None;
            }

            ProjectionType[] pt = ProjectionType.values();
            for (int i = 0; i < pt.length; i++) {
                if (pt[i].toStringNative().equals(stringNative)) {
                    return pt[i];
                }

                if (pt[i].toStringNative().replace("_", "").equals(stringNative)) {
                    return pt[i];
                }

                if (pt[i].toStringNative().replace("_", " ").equals(stringNative)) {
                    return pt[i];
                }
            }

            return null;
        }

        public static ProjectionType getProjectionTypeFromCmb(String cmbBoxSelectedItem) {
            String[] parts = CMB_SEPERATOR_ELEMENTS_PAT.split(cmbBoxSelectedItem);
            ProjectionType ref;
            if (parts.length == 1) {
                ref = ProjectionType.getProjectionTypefromString(parts[0]);
            } else {
                ref = ProjectionType.getProjectionTypefromString(parts[1]);
            }

            return ref;
        }

        public static ProjectionType getProjectionTypeFromCmb(Object cmbBoxSelectedItem) {
            String[] parts = CMB_SEPERATOR_ELEMENTS_PAT.split(cmbBoxSelectedItem.toString());
            ProjectionType ref;
            if (parts.length == 1) {
                ref = ProjectionType.getProjectionTypefromString(parts[0]);
            } else {
                ref = ProjectionType.getProjectionTypefromString(parts[1]);
            }

            return ref;
        }

        public int[] getParams() {
            /**
             * Albers Equal Area Albers 9822 8821, 8822, 8823. 8824, 8826, 8827 American Polyconic Polyconic 9818 8801,
             * 8802, 8806, 8807 Cassini-Soldner Cassini 9806 8801, 8802, 8806, 8807 Hotine Oblique Mercator (variant A)
             * Rectified skew orthomorphic 9812 8811, 8812, 8813, 8814, 8815, 8806, 8807 Hotine Oblique Mercator
             * (variant B) Rectified skew orthomorphic 9815 8811, 8812, 8813, 8814, 8815, 8816, 8817 Lambert Azimuthal
             * Equal Area Lambert Equal Area LAEA 9820 8801, 8802, 8806, 8807 Lambert Conic Conformal (1SP) Lambert
             * Conic Conformal LCC 9801 8801, 8802, 8805, 8806, 8807 Lambert Conic Conformal (2SP) Lambert Conic
             * Conformal LCC 9802 8821, 8822, 8823. 8824, 8826, 8827 Mercator (variant A) Mercator 9804 8801, 8802,
             * 8805, 8806, 8807 Mercator (variant B) Mercator 9805 8823, 8802, 8806, 8807 Oblique stereographic Double
             * stereographic 9809 8801, 8802, 8805, 8806, 8807 Transverse Mercator Gauss-Boaga Gauss-Krüger TM 9807
             * 8801, 8802, 8805, 8806, 8807 Transverse Mercator (South Orientated) Gauss-Conform 9808 8801, 8802, 8805,
             * 8806, 8807
             */
            switch (this) {
            case Automatic:
            case Unknown:
            case None:
                return new int[] {};
                // case Unknown:
                // return new int[]{};//from values
                // return new int[]{8801, 8802, 8805, 8806, 8807};
            case AmericanPolyconic:
            case Polyconic:
            case CassiniSoldner:
            case LambertEqualArea:
                return new int[] {8801, 8802, 1, 8806, 8807};
                // setParams(new double[] { latitude, longitude, 1.0, falseEasting, falseNorthing });
            case Mercator: // TODO 3. Parameter ??
                return new int[] {8801, 8802, 8805, 8806, 8807};
                // setParams(new double[] { latitude, longitude, 1.0, falseEasting, falseNorthing });

            case Mercator1SP:
                return new int[] {8802, 8805, 8806, 8807};
            case Mercator2SP:
                return new int[] {8823, 8802, 8806, 8807};
            case LambertConicConformal1SP:
            case ObliqueStereographic:
            case PolarStereographic:
            case TransverseMercator:
            case TransverseMercatorSouthOrientated:
                return new int[] {8801, 8802, 8805, 8806, 8807};
                // setParams(new double[] { latitude, longitude, scale, falseEasting, falseNorthing });
            case HotineObliqueMercatorA:
                // return new int[] { 8811, 8812, 8813, 8814, 8815, 8806, 8807 }; // TODO CHECK
                return new int[] {8811, 8812, 8815, 8806, 8807, 8813, 8814};
                // 9812: 8811, 8812, 8813, 8814, 8815, 8806, 8807
                // 9815: return new int[]{8811, 8812, 8813, 8814, 8815, 8816, 8817};
                // setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);

                // int org.gdal.osr.SpatialReference.SetHOM(double clat, double clong, double azimuth, double
                // recttoskew, double scale, double fe, double fn)

            case HotineObliqueMercatorB:
                // return new int[] { 8811, 8812, 8813, 8814, 8815, 8816, 8817 };
                return new int[] {8811, 8812, 8815, 8816, 8817, 8813, 8814};
                // setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);
            case LambertConicConformal2SP:
                return new int[] {8821, 8822, 1, 8823, 8824, 8826, 8827};
                // setParams(latitude, longitude, 1.0, falseEasting, falseNorthing, this.params[5], this.params[6]);
            case AlbersConicEqualArea:
                return new int[] {8823, 8824, 8821, 8822, 8826, 8827};
                // return new int[]{8821, 8822, 8823, 8824, 8826, 8827};
                // return new
                // String[]{
                // "standard_parallel_1","standard_parallel_2","latitude_of_center","longitude_of_center","false_easting","false_northing"};
                // return new int[]{8821, 8822, 8801, 8823, 8806, 8807};
            case TunisiaMiningGrid:
                return new int[] {8801, 8802, 1, 8806, 8807};
            case Krovak:
                return new int[] {8811, 8812, 8813, 8823, 8815, 8816, 8817};
            default:
                System.out.println("Projection Type not implemented: " + this.name());
                throw new IllegalArgumentException("Projection type not implemented");
                // TODO ? case 9815: return new int[]{8811, 8812, 8813, 8814, 8815, 8816, 8817};
                // case 9805: return new int[]{8823, 8802, 8806, 8807};
                // case 9822: return new int[]{8821, 8822, 8823, 8824, 8826, 8827};
            }
        }

        public String[] getParamsNames() {
            return getParamName(getParams());
        }

        private String[] getParamName(int[] parameterCode) {
            String[] paramName = new String[7];
            for (int i = 0; i < parameterCode.length; i++) {
                paramName[i] = getParamName(parameterCode[i]);
            }

            return paramName;
        }

        private String getParamName(int parameterCode) {
            switch (parameterCode) {
            case 8801:
                return "latitude of natural origin"; // angle
            case 8802:
                return "longitude of natural origin"; // angle
            case 8805:
                return "scale factor at natural origin"; // scale
            case 8806:
                return "false easting"; // length
            case 8807:
                return "false northing"; // length
            case 8811:
                return "latitude of projection centre"; // angle
            case 8812:
                return "longitude of projection centre"; // angle
            case 8813:
                return "azimuth of initial line direction"; // angle
            case 8814:
                return "angle from rectified to skew grid"; // angle
            case 8815:
                return "scale factor on initial line"; // scale
            case 8816:
                return "easting at projection centre"; // length
            case 8817:
                return "northing at projection centre"; // length
            case 8821:
                return "latitude of false origin"; // angle
            case 8822:
                return "longitude of false origin"; // angle
            case 8823:
                return "latitude of 1st standard parallel"; // angle
            case 8824:
                return "latitude of 2nd standard parallel"; // angle
            case 8826:
                return "easting at false origin"; // length
            case 8827:
                return "northing at false origin"; // length
            case 1:
                return "fix: 1.0"; // length
            default:
                break;
            }

            ;
            return null;
        }

        public String[] getParamsIds() {
            switch (this) {
                // oben mit rein!!
            case Automatic:
            case None:
                return new String[] {};
            case AmericanPolyconic:
            case Polyconic:
                // "American Polyconic"/"Polyconic"
                return new String[] {
                    "latitude_of_origin", "central_meridian", "fix: 1.0", "false_easting", "false_northing", null, null
                };
            case CassiniSoldner:
                // "Cassini_Soldner"
                return new String[] {
                    "latitude_of_origin", "central_meridian", "fix: 1.0", "false_easting", "false_northing", null, null
                };
            case LambertEqualArea:
                // "Lambert Azimuthal Equal Area",Lambert_Azimuthal_Equal_Area",{8801, 8802, 1, 8806, 8807}
                return new String[] {
                    "latitude_of_center",
                    "longitude_of_center",
                    "fix: 1.0",
                    "false_easting",
                    "false_northing",
                    null,
                    null
                };
                // setParams(new double[] { latitude, longitude, 1.0, falseEasting, falseNorthing });
            case Mercator: // TODO 3. Parameter ??
                // "Mercator"/"Mercator_1SP"
                return new String[] {
                    "latitude_of_origin",
                    "central_meridian",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    null,
                    null
                };
                // setParams(new double[] { latitude, longitude, 1.0, falseEasting, falseNorthing });
            case Mercator1SP: // TODO 3. Parameter ??
                // "Mercator"/"Mercator_1SP"
                return new String[] {
                    "central_meridian", "scale_factor", "false_easting", "false_northing", null, null, null
                };
                // setParams(new double[] { latitude, longitude, 1.0, falseEasting, falseNorthing });
            case Mercator2SP: // TODO 3. Parameter ??
                return new String[] {
                    "standard_parallel_1", "central_meridian", "false_easting", "false_northing", null, null, null
                };
            case LambertConicConformal1SP:
                // "Lambert Conic Conformal (1SP)"/"Lambert_Conformal_Conic_1SP"
                return new String[] {
                    "latitude_of_origin",
                    "central_meridian",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    null,
                    null
                };
            case ObliqueStereographic:
                // "Oblique Stereographic"/"Oblique_Stereographic"
                return new String[] {
                    "latitude_of_origin",
                    "central_meridian",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    null,
                    null
                };
            case PolarStereographic:
                return new String[] {
                    "latitude_of_origin",
                    "central_meridian",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    null,
                    null
                };
            case TransverseMercator:
                // "Transverse Mercator"/"Transverse_Mercator", {8801, 8802, 8805, 8806, 8807};
                return new String[] {
                    "latitude_of_origin",
                    "central_meridian",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    null,
                    null
                };
            case TransverseMercatorSouthOrientated:
                // "Transverse Mercator (south orientated)"/"Transverse_Mercator_South_Orientated"
                return new String[] {
                    "latitude_of_origin",
                    "central_meridian",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    null,
                    null
                };
                // setParams(new double[] { latitude, longitude, scale, falseEasting, falseNorthing });
            case HotineObliqueMercatorA:
                // "Hotine Oblique Mercator"/"Hotine_Oblique_Mercator"
                // return new
                // String[]{"latitude_of_center","longitude_of_center","azimuth","rectified_grid_angle","scale_factor","false_easting","false_northing"};
                return new String[] {
                    "latitude_of_center",
                    "longitude_of_center",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    "azimuth",
                    "rectified_grid_angle"
                };
                // setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);
            case HotineObliqueMercatorB:
                // return new String[] {
                //     "latitude_of_center", "longitude_of_center", "azimuth", "rectified_grid_angle", "scale_factor",
                // "false_easting", "false_northing"
                // };
                return new String[] {
                    "latitude_of_center",
                    "longitude_of_center",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    "azimuth",
                    "rectified_grid_angle"
                };
                // setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);

            case LambertConicConformal2SP:
                // "Lambert Conic Conformal (2SP)","Lambert_Conformal_Conic_2SP"
                // return new String[]{"standard_parallel_1","standard_parallel_2","fix:
                // 1.0","latitude_of_origin","central_meridian","false_easting","false_northing"};
                return new String[] {
                    "standard_parallel_1",
                    "standard_parallel_2",
                    "scale_factor",
                    "latitude_of_origin",
                    "central_meridian",
                    "false_easting",
                    "false_northing"
                };
                // setParams(latitude, longitude, 1.0, falseEasting, falseNorthing, this.params[5], this.params[6]);
            case AlbersConicEqualArea:
                // "Lambert Conic Conformal (2SP)","Lambert_Conformal_Conic_2SP"
                return new String[] {
                    "standard_parallel_1",
                    "standard_parallel_2",
                    "latitude_of_center",
                    "longitude_of_center",
                    "false_easting",
                    "false_northing"
                };
                // return new
                // String[]{
                // "standard_parallel_1","standard_parallel_2","scale_factor","latitude_of_origin","central_meridian",
                // "false_easting","false_northing"};
                // return new
                // String[]{
                // "standard_parallel_1","standard_parallel_2","latitude_of_center","longitude_of_center","false_easting","false_northing"};
            case TunisiaMiningGrid:
                return new String[] {
                    "latitude_of_origin",
                    "central_meridian",
                    "fix: 1.0",
                    "false_easting",
                    "false_northing",
                    null,
                    null,
                    null
                };
            case Krovak:
                return new String[] {
                    "latitude_of_center",
                    "longitude_of_center",
                    "azimuth",
                    "pseudo_standard_parallel_1",
                    "scale_factor",
                    "false_easting",
                    "false_northing",
                    null
                };
            default:
                throw new IllegalArgumentException("Projection type not implemented: " + this.name());
                // TODO ? case 9815: return new int[]{8811, 8812, 8813, 8814, 8815, 8816, 8817};
                // case 9805: return new int[]{8823, 8802, 8806, 8807};
                // case 9822: return new int[]{8821, 8822, 8823, 8824, 8826, 8827};
            }
        }

        public String[] getParamsIDs() {
            return getParamId(getParams());
        }

        private String[] getParamId(int[] parameterCode) {
            String[] paramId = new String[7];
            for (int i = 0; i < parameterCode.length; i++) {
                paramId[i] = getParamId(parameterCode[i]);
            }

            return paramId;
        }

        private String getParamId(int parameterCode) {
            switch (parameterCode) {
                // TODO CLST weitere Werte setzen
            case 8801:
                return "latitude_of_origin"; // angle
            case 8802:
                return "central_meridian"; // longitude_of_origin"; //angle
            case 8805:
                return "scale_factor"; // scale
            case 8806:
                return "false_easting"; // length
            case 8807:
                return "false_northing"; // length
            case 8811:
                return "latitude_of_projection_centre"; // angle
            case 8812:
                return "longitude_of_projection_centre"; // angle
            case 8813:
                return "azimuth_of_initial_line_direction"; // angle
            case 8814:
                return "angle from rectified to skew grid"; // angle
            case 8815:
                return "scale factor on initial line"; // scale
            case 8816:
                return "easting at projection centre"; // length
            case 8817:
                return "northing at projection centre"; // length
            case 8821:
                return "latitude of false origin"; // angle
            case 8822:
                return "longitude of false origin"; // angle
            case 8823:
                return "latitude of 1st standard parallel"; // angle
            case 8824:
                return "latitude of 2nd standard parallel"; // angle
            case 8826:
                return "easting at false origin"; // length
            case 8827:
                return "northing at false origin"; // length
            case 1:
                return "fix: 1.0"; // length
            default:
                break;
            }

            ;
            return null;
        }

    }

    public ProjectionType projectionType = ProjectionType.None;
    private double[] params;
    UnitOfMeasure unit;

    public MapProjection(ProjectionType projectionType) {
        this.projectionType = projectionType;
        this.params = new double[] {};
        this.unit = null;
    }

    public MapProjection(ProjectionType projectionType, double[] params) {
        this(projectionType);
        this.setParams(params);
        this.unit = null;
    }

    public MapProjection(int epsgMethodCode) {
        this(ProjectionType.fromEpgsMethodCode(epsgMethodCode));
        this.unit = null; // TODO
    }

    public MapProjection clone() {
        MapProjection res = new MapProjection(this.projectionType).withParams(params.clone());
        res.setUnit(this.unit);
        return res;
    }

    public double[] params() {
        return params;
    }

    public void setParams(double[] params) {
        int n = params.length;
        if (n != projectionType.getParams().length) {
            throw new IllegalArgumentException(
                "Wrong number of projection parameters: " + n + ", expected: " + projectionType.getParams().length);
        }

        this.params = params;
    }

    public void setParams(double latitude, double longitude, double scale, double falseEasting, double falseNorthing) {
        switch (projectionType) {
        case Automatic:
            throw new IllegalStateException("ProjectionType is undetermined");
        case None:
            setParams(new double[] {});
            break;
        case AmericanPolyconic:
        case Polyconic:
        case CassiniSoldner:
        case LambertEqualArea:
        case Mercator:
            setParams(new double[] {latitude, longitude, 1.0, falseEasting, falseNorthing});
            break;
        case Mercator1SP:
            setParams(new double[] {longitude, 1.0, falseEasting, falseNorthing});
            break;
        case Mercator2SP:
            setParams(new double[] {latitude, longitude, falseEasting, falseNorthing});
            break;
        case LambertConicConformal1SP:
        case ObliqueStereographic:
        case PolarStereographic:
        case TransverseMercator:
        case TransverseMercatorSouthOrientated:
            setParams(new double[] {latitude, longitude, scale, falseEasting, falseNorthing});
            break;
        case HotineObliqueMercatorA:
            setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);
            break;
        case HotineObliqueMercatorB:
            setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);
            break;
        case LambertConicConformal2SP:
            // setParams(latitude, longitude, 1.0, falseEasting, falseNorthing, this.params[5], this.params[6]);
            setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);
            break;
        case AlbersConicEqualArea:
            setParams(latitude, longitude, 1.0, falseEasting, falseNorthing, this.params[5], this.params[6]);
            break;
        case TunisiaMiningGrid:
            setParams(new double[] {latitude, longitude, 1.0, falseEasting, falseNorthing});
            break;
            // case Krovak:
            // setParams(latitude, longitude, scale, falseEasting, falseNorthing, this.params[5], this.params[6]);
            // break;
        default:
            throw new IllegalArgumentException("Projection type not implemented");
        }
    }

    @SuppressWarnings("checkstyle:parametername")
    public void setParams(
            double latitude,
            double longitude,
            double scale,
            double falseEasting,
            double falseNorthing,
            double azimuth_or_1st_parallel,
            double skew_or_2nd_parallel) {
        double s = (unit != null) ? unit.getFactor() : 1.0;

        falseEasting *= s;
        falseNorthing *= s;

        switch (projectionType) {
        case LambertConicConformal2SP:
            setParams(
                new double[] {
                    latitude,
                    longitude,
                    scale,
                    falseEasting,
                    falseNorthing,
                    azimuth_or_1st_parallel,
                    skew_or_2nd_parallel
                });
            break;
        case HotineObliqueMercatorA:
            setParams(
                new double[] {
                    latitude,
                    longitude,
                    scale,
                    falseEasting,
                    falseNorthing,
                    azimuth_or_1st_parallel,
                    skew_or_2nd_parallel
                });
            break;
        case HotineObliqueMercatorB:
            setParams(
                new double[] {
                    latitude,
                    longitude,
                    scale,
                    falseEasting,
                    falseNorthing,
                    azimuth_or_1st_parallel,
                    skew_or_2nd_parallel
                });
            break;
        case AlbersConicEqualArea:
            setParams(
                new double[] {
                    latitude,
                    longitude,
                    scale,
                    falseEasting,
                    falseNorthing,
                    azimuth_or_1st_parallel,
                    skew_or_2nd_parallel
                });
            break;
        case Krovak:
            setParams(
                new double[] {
                    latitude,
                    longitude,
                    scale,
                    falseEasting,
                    falseNorthing,
                    azimuth_or_1st_parallel,
                    skew_or_2nd_parallel
                });
            break;
        default:
            throw new IllegalArgumentException("Projection type not implemented");
        }
    }

    public MapProjection withParams(
            double latitude, double longitude, double scale, double falseEasting, double falseNorthing) {
        setParams(latitude, longitude, scale, falseEasting, falseNorthing);

        return this;
    }

    @SuppressWarnings("checkstyle:parametername")
    public MapProjection withParams(
            double latitude,
            double longitude,
            double scale,
            double falseEasting,
            double falseNorthing,
            double azimuth_or_1st_parallel,
            double skew_or_2nd_parallel) {
        setParams(
            latitude, longitude, scale, falseEasting, falseNorthing, azimuth_or_1st_parallel, skew_or_2nd_parallel);
        return this;
    }

    public MapProjection withParams(double[] params) {
        setParams(params);

        return this;
    }

    public SpatialReference setSrsProjection(SpatialReference srs) {
        srs.SetProjCS(projectionType.toString());

        if (unit != null) {
            srs.SetLinearUnits(unit.getUnitOfMeasName(), unit.getFactor());
        }

        if (params.length >= 5) {
            // scale factor must be positive
            if (params[2] < 0) {
                params[2] = Math.abs(params[2]);
            }
        }

        switch (projectionType) {
        case Automatic:
            throw new IllegalStateException("ProjectionType is undetermined");
        case None:
            if (srs.IsProjected() != 0) {
                // Remove projection
                srs = srs.CloneGeogCS();
            }

            break;
        case AmericanPolyconic:
        case Polyconic:
            srs.SetPolyconic(params[0], params[1], params[3], params[4]);
            break;
        case CassiniSoldner:
            srs.SetCS(params[0], params[1], params[3], params[4]);
            break;
        case LambertEqualArea:
            srs.SetLAEA(params[0], params[1], params[3], params[4]);
            break;
        case LambertConicConformal1SP:
            srs.SetLCC1SP(params[0], params[1], params[2], params[3], params[4]);
            break;
        case Mercator:
            // latitude
            if (params[0] < -89.9) {
                params[0] = -89.9;
            }

            // latitude
            if (params[0] > 89.9) {
                params[0] = 89.9;
            }

            // scale
            if (params[2] != 1.0) {
                params[2] = 1.0;
            }

            srs.SetMercator(params[0], params[1], params[2], params[3], params[4]);
            break;

        case ObliqueStereographic:
            srs.SetOS(params[0], params[1], params[2], params[3], params[4]);
            break;
        case PolarStereographic:
            srs.SetOS(params[0], params[1], params[2], params[3], params[4]);
            break;
        case TransverseMercator:
            srs.SetTM(params[0], params[1], params[2], params[3], params[4]);
            break;
        case TransverseMercatorSouthOrientated:
            srs.SetTMSO(params[0], params[1], params[2], params[3], params[4]);
            break;
        case LambertConicConformal2SP:
            srs.SetLCC(params[5], params[6], params[0], params[1], params[3], params[4]);
            break;
        case HotineObliqueMercatorA:
        case HotineObliqueMercatorB:
            // lat
            if (params[0] == -90) {
                params[0] = -89.999;
            } else if (params[0] == 90) {
                params[0] = 89.999;
            }

            // azimuth
            if (params[5] == -180) {
                params[5] = -179.999;
            }

            if (Math.abs(params[5]) < 1e-3) {
                params[5] = 1e-3;
            }

            srs.SetHOM(params[0], params[1], params[5], params[6], params[2], params[3], params[4]);
            break;
        case AlbersConicEqualArea:
            srs.SetACEA(params[5], params[6], params[0], params[1], params[3], params[4]);
            break;
        case TunisiaMiningGrid:
            srs.SetTMG(params[0], params[1], params[3], params[4]);
            break;
        case Krovak:
            srs.SetKrovak(params[0], params[1], params[4], params[5], params[6], params[2], params[3]);
            break;
        case Mercator1SP:
            Debug.getLog().log(Debug.WARNING, "Mercator1SP not defined! Please check!");
            srs.SetMercator(0, params[0], params[1], params[2], params[3]);
            break;
        case Mercator2SP:
            Debug.getLog().log(Debug.WARNING, "Mercator2SP not defined! Please check!");
            srs.SetMercator(0, params[0], params[1], params[2], params[3]);
            break;
        default:
            throw new IllegalStateException("ProjectionType is not implemented");
        }

        return srs;
    }

    public static MapProjection fromSrs(SpatialReference srs) {
        MapProjection mapProj = null;

        if (srs.IsProjected() == 0) {
            mapProj = new MapProjection(MapProjection.ProjectionType.None);
        } else {
            switch (srs.GetAttrValue("Projection")) {
                /*
                 * case "AmericanPolyconic": throw new IllegalStateException("Not implemented"); case "CassiniSoldner": throw new
                 * IllegalStateException("Not implemented"); case "LambertEqualArea": throw new IllegalStateException("Not implemented"); case
                 * "LambertConicConformal1SP": throw new IllegalStateException("Not implemented"); case "Mercator": throw new
                 * IllegalStateException("Not implemented");
                 */
            case "Oblique_Stereographic":
                {
                    mapProj = new MapProjection(MapProjection.ProjectionType.ObliqueStereographic);
                    double[] params = {
                        srs.GetProjParm("latitude_of_origin"),
                        srs.GetProjParm("central_meridian"),
                        srs.GetProjParm("scale_factor"),
                        srs.GetProjParm("false_easting"),
                        srs.GetProjParm("false_northing")
                    };
                    mapProj.setParams(params);
                }

                break;
                /*
                 * case "PolarStereographic": throw new IllegalStateException("Not implemented");
                 */
            case "Transverse_Mercator":
                {
                    mapProj = new MapProjection(MapProjection.ProjectionType.TransverseMercator);
                    double[] params = {
                        srs.GetProjParm("latitude_of_origin"),
                        srs.GetProjParm("central_meridian"),
                        srs.GetProjParm("scale_factor"),
                        srs.GetProjParm("false_easting"),
                        srs.GetProjParm("false_northing")
                    };
                    mapProj.setParams(params);
                }

                break;

            case "Transverse_Mercator_South_Orientated":
                {
                    mapProj = new MapProjection(MapProjection.ProjectionType.TransverseMercatorSouthOrientated);
                    double[] params = {
                        srs.GetProjParm("latitude_of_origin"),
                        srs.GetProjParm("central_meridian"),
                        srs.GetProjParm("scale_factor"),
                        srs.GetProjParm("false_easting"),
                        srs.GetProjParm("false_northing")
                    };
                    mapProj.setParams(params);
                }

                break;
                /*
                 * case "LambertConicConformal2SP": throw new IllegalStateException("Not implemented"); case "HotineObliqueMercatorA": throw new
                 * IllegalStateException("Not implemented"); case "AlbersConicEqualArea": throw new IllegalStateException("Not implemented");
                 */
            default:
                mapProj = new MapProjection(MapProjection.ProjectionType.Unknown);
                throw new IllegalStateException("Not implemented");
            }
        }

        return mapProj;
    }

    public void setUnit(UnitOfMeasure unit) {
        this.unit = unit;
    }

    public UnitOfMeasure getUnit() {
        return this.unit;
    }

}
