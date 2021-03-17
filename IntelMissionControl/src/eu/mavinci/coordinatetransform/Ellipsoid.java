/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import eu.mavinci.desktop.main.debug.Debug;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "checkstyle:membername"})
public class Ellipsoid {
    public static final String SEPERATOR_ELEMENTS = "</|/|<";
    public static final Pattern SEPERATOR_ELEMENTS_PAT = Pattern.compile(Pattern.quote(SEPERATOR_ELEMENTS));
    public static final String CMB_SEPERATOR_ELEMENTS = " - ";
    public static final Pattern CMB_SEPERATOR_ELEMENTS_PAT = Pattern.compile(Pattern.quote(CMB_SEPERATOR_ELEMENTS));

    private double factorFoot = 0.3048;

    private int ellipsoid_code;
    private String ellipsoid_name;
    private double semi_major_axis;
    private int uom_code;
    private double flattening;
    private double inv_flattening;
    private double semi_minor_axis;
    private int ellipsoid_shape;
    private String remarks;
    private String information_source;
    private String data_source;
    private String revision_date;
    private String change_id;
    private int deprecated;

    // 7030|WGS 84|6378137|9001|298.257223563||1|Inverse flattening derived from four defining parameters (semi-major
    // axis; C20 =
    // -484.16685*10e-6; earth's angular velocity w = 7292115e11 rad/sec; gravitational constant GM = 3986005e8
    // m*m*m/s/s).|DMA Technical
    // Manual 8350.2-B |OGP|1998-11-11|1998.320|0|

    public static Ellipsoid wgs84Ellipsoid = new Ellipsoid("WGS 84", 6378137, 298.257223563);
    public static Ellipsoid grs1980 = new Ellipsoid("GRS 1980", 6378137, 298.257222101);
    public static Ellipsoid clarke1880Ellipsoid = new Ellipsoid("Clarke 1880 (RGS)", 6378249.145, 293.465);
    public static Ellipsoid clarke1866Ellipsoid = new Ellipsoid("Clarke 1866", 6378206.4, 294.978698214);
    public static Ellipsoid unKnown = new Ellipsoid("unknown", 0, 0);
    public static Ellipsoid Xian1980 = new Ellipsoid("Xian 1980", 6378140, 298.2569978029111);

    // Known ellipsoids to use for automatic determination. Start with most preferred going to least preferred.
    public static final Ellipsoid[] knownEllipsoids = {
        wgs84Ellipsoid, grs1980, clarke1866Ellipsoid, clarke1880Ellipsoid
    };

    // public static Vector<Ellipsoid> references;
    public static TreeMap<String, Ellipsoid> references = new TreeMap<String, Ellipsoid>();
    public static TreeMap<String, Ellipsoid> validEllipsoids = new TreeMap<String, Ellipsoid>();
    public static TreeMap<String, Ellipsoid> validEllipsoidsSortedId = new TreeMap<String, Ellipsoid>();

    public static TreeMap<String, Ellipsoid> ellipsoidsSortedId = new TreeMap<String, Ellipsoid>();

    public Ellipsoid(String name, double semiMajorAxis, double invFlattening) {
        ellipsoid_name = name;
        semi_major_axis = semiMajorAxis;
        flattening = 1.0 / invFlattening;
        inv_flattening = invFlattening;
        // System.out.println("Ellipsoid: "+ellipsoid_name+"|"+semi_major_axis+"|"+semi_minor_axis+ "|"+
        // inv_flattening);
    }

    public Ellipsoid() {}

    public double getSemiMajorAxis() {
        return semi_major_axis;
    }

    public double getSemiMajorAxisToFoot() {
        return semi_major_axis * factorFoot;
    }

    public double getSemiMajorAxisToMeter() {
        return semi_major_axis / factorFoot;
    }

    public double getInvFlattening() {
        // System.out.println("Ellipsoid: "+ellipsoid_name+"|"+semi_major_axis+"|"+semi_minor_axis+ "|"+
        // inv_flattening);
        if (inv_flattening == 0) {
            // System.out.println("Ellipsoid: "+ellipsoid_name+"|"+semi_major_axis+"|"+semi_minor_axis+ "|"+ 1.0/(1.0 -
            // (semi_minor_axis /
            // semi_major_axis ) ));
            return 1.0 / (1.0 - (semi_minor_axis / semi_major_axis));
        } else {
            return 1.0 / flattening;
        }
    }

    public String getName() {
        return ellipsoid_name;
    }

    public double getSemiMinorAxis() {
        if (semi_minor_axis == 0) {
            return semi_major_axis * (1.0 - (flattening));
        } else {
            return semi_minor_axis;
        }
    }

    public static double getSemiMinorAxis(double semiMajorAxis, double invFlattening) {
        return semiMajorAxis * (1.0 - (1.0 / invFlattening));
    }

    public static double getSemiMinorAxis(String semiMajorAxis, String invFlattening) {
        if (invFlattening.equals("inf")) {
            return Double.parseDouble(semiMajorAxis) * (1.0 - (0));
        } else {
            return Double.parseDouble(semiMajorAxis) * (1.0 - (1.0 / Double.parseDouble(invFlattening)));
        }
    }

    public double getSemiMinorAxisToFoot() {
        return getSemiMinorAxis() * factorFoot;
    }

    public double getSemiMinorAxisToMeter() {
        return getSemiMinorAxis() / factorFoot;
    }

    public double getEccentricity() {
        // https://en.wikipedia.org/wiki/Eccentricity_(mathematics)
        return Math.sqrt(1.0 - (getSemiMinorAxis() * getSemiMinorAxis() / (semi_major_axis * semi_major_axis)));
    }

    public static Vector<String> getNames() {
        Vector<String> ellipsoids2 = new Vector<String>();
        for (Ellipsoid ellipsoid : validEllipsoidsSortedId.values()) {
            ellipsoids2.add(ellipsoid.ellipsoid_name);
        }

        return ellipsoids2;
    }

    public static void assureInit() {
        Debug.getLog().config("Initialize Ellipsoids");
        initFromPrecompiledFile();

        // check all
        for (int i = 0; i < references.size(); i++) {}

        if (references.size() == 0) {
            Debug.getLog().config("Number loaded ellipsoids: " + references.size());
        } else {
            Debug.getLog()
                .config(
                    "Number loaded ellipsoids: "
                        + references.size()
                        + "  in ["
                        + references.firstKey()
                        + " ,  "
                        + references.lastKey()
                        + "]");
        }

        wgs84Ellipsoid = references.get("WGS 84");
        grs1980 = references.get("GRS 1980");
        clarke1880Ellipsoid = references.get("Clarke 1880 (RGS)");
        clarke1866Ellipsoid = references.get("Clarke 1866");
        Xian1980 = references.get("IAG 1975");
    }

    private static void initFromPrecompiledFile() {
        // System.out.println(ClassLoader.getSystemResourceAsStream("eu/mavinci/other/ellipsoid.dat")..toString());
        initFromStream(ClassLoader.getSystemResourceAsStream("eu/mavinci/other/ellipsoid.dat"));
    }

    private static void initFromStream(InputStream reader) {
        try (InputStreamReader a = new InputStreamReader(reader)) {
            initFromReader(a);
        } catch (IOException e) {
            // System.out.println(e.getMessage());
            Debug.getLog().log(Level.SEVERE, "problems loading all ellipsoid references", e);
        } finally {
            try {
                reader.close();
            } catch (Exception expected) {
            }
        }
    }

    private static void initFromReader(Reader a) {
        int lineNo = 0;
        try (BufferedReader br = new BufferedReader(a); ) {
            validEllipsoids.put(Ellipsoid.unKnown.ellipsoid_name, Ellipsoid.unKnown);
            references.put(Ellipsoid.unKnown.ellipsoid_name, Ellipsoid.unKnown);
            ellipsoidsSortedId.put(Integer.toString(Ellipsoid.unKnown.ellipsoid_code), Ellipsoid.unKnown);
            validEllipsoidsSortedId.put(Integer.toString(Ellipsoid.unKnown.ellipsoid_code), Ellipsoid.unKnown);

            String line = br.readLine();
            while (line != null && !line.trim().isEmpty()) {
                try {
                    if (line.startsWith("#")) {
                        line = br.readLine();
                        lineNo++;
                        continue;
                    }

                    Ellipsoid ref = Ellipsoid.createUsingPredefinedString(line);
                    if (ref.deprecated != 1) {
                        validEllipsoids.put(ref.ellipsoid_name, ref);
                        validEllipsoidsSortedId.put(ref.ellipsoid_name, ref);
                    }

                    references.put(ref.ellipsoid_name, ref);
                    ellipsoidsSortedId.put(Integer.toString(ref.ellipsoid_code), ref);
                } catch (Exception e) {
                    if (lineNo != 0) {
                        Debug.getLog().log(Level.WARNING, "problems loading single ellipsoid on line:" + lineNo, e);
                    }
                }

                line = br.readLine();
                lineNo++;
            }

        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "problems loading all ellipsoids", e);
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    private static Ellipsoid createUsingPredefinedString(String stuff) {
        String[] parts = SEPERATOR_ELEMENTS_PAT.split(stuff);

        // ellipsoid_code|ellipsoid_name|semi_major_axis|uom_code|inv_flattening|semi_minor_axis|ellipsoid_shape|remarks|information_source|data_source|revision_date|change_id|deprecated|

        Ellipsoid ref = new Ellipsoid();
        ref.ellipsoid_code = Integer.parseInt(parts[0]);
        ref.ellipsoid_name = parts[1];
        if (!parts[2].equals("")) {
            ref.semi_major_axis = Double.parseDouble(parts[2]);
        }

        ref.uom_code = Integer.parseInt(parts[3]);
        if (!parts[4].equals("")) {
            ref.inv_flattening = Double.parseDouble(parts[4]);
        }

        if (!parts[4].equals("")) {
            ref.flattening = 1.0 / Double.parseDouble(parts[4]);
        }

        if (!parts[5].equals("")) {
            ref.semi_minor_axis = Double.parseDouble(parts[5]);
        }

        ref.ellipsoid_shape = Integer.parseInt(parts[6]);
        ref.remarks = parts[7];
        ref.information_source = parts[8];
        ref.data_source = parts[9];
        ref.revision_date = parts[10];
        ref.change_id = parts[11];
        ref.deprecated = Integer.parseInt(parts[12]);
        // System.out.println("Ellipsoid: "+ref.ellipsoid_name+"|"+ref.semi_major_axis+"|"+ref.semi_minor_axis+ "|"+
        // ref.inv_flattening);
        // ref.check();
        return ref;
    }

    public static Vector<String> getEllipsoidsForCmb() {
        Vector<String> ellipsoids2 = new Vector<String>();
        for (Ellipsoid ellipsoid : ellipsoidsSortedId.values()) {
            if (ellipsoid.ellipsoid_code != 0) {
                ellipsoids2.add(ellipsoid.getEllipsoidForCmb());
            }
        }

        Collections.sort(ellipsoids2);
        for (Ellipsoid ellipsoid : ellipsoidsSortedId.values()) {
            if (ellipsoid.ellipsoid_code == 0) {
                ellipsoids2.add(0, ellipsoid.getEllipsoidForCmb());
            }
        }

        return ellipsoids2;
    }

    public String getEllipsoidForCmb() {
        // return ellipsoidName+SEPERATOR_ELEMENTS+greenwichLongitude+SEPERATOR_ELEMENTS+ellipsoidCode;
        if (ellipsoid_code == 0) {
            return ellipsoid_name;
        }

        return "EPSG:" + ellipsoid_code + " - " + ellipsoid_name;
    }

    public static String getEllipsoidForCmb(String ellipsoidName) {
        Ellipsoid ref;
        if (ellipsoidName == null) {
            return unKnown.getEllipsoidForCmb();
        }

        ref = references.get(ellipsoidName);
        if (ref == null) {
            throw new IllegalStateException("ref cannot be null");
        }

        return ref.getEllipsoidForCmb();
    }

    public static String getEllipsoidForCmbFromCode(int ellipsoidCode) {
        Ellipsoid ref;
        if (ellipsoidCode == 0) {
            return unKnown.getEllipsoidForCmb();
        }

        ref = get(ellipsoidCode);
        return ref.getEllipsoidForCmb();
    }

    private static Ellipsoid get(int ellipsoidCode) {
        for (Ellipsoid ellipsoid : references.values()) {
            if (ellipsoid.ellipsoid_code == ellipsoidCode) {
                return ellipsoid;
            }
        }

        return unKnown;
    }

    public static Ellipsoid getEllipsoidFromCmb(String cmbBoxSelectedItem) {
        String[] parts = CMB_SEPERATOR_ELEMENTS_PAT.split(cmbBoxSelectedItem);
        Ellipsoid ref;
        if (parts.length == 1) {
            ref = references.get(parts[0]);
        } else {
            ref = references.get(parts[1]);
        }

        return ref;
    }

    public static Ellipsoid getEllipsoidFromCmb(Object cmbBoxSelectedItem) {
        String[] parts = CMB_SEPERATOR_ELEMENTS_PAT.split(cmbBoxSelectedItem.toString());
        Ellipsoid ref;
        if (parts.length == 1) {
            ref = references.get(parts[0]);
        } else {
            ref = references.get(parts[1]);
        }

        return ref;
    }

}
