/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import eu.mavinci.desktop.main.debug.Debug;
import org.gdal.gdal.gdal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class PrimeMeridian {
    public static final String SEPERATOR_ELEMENTS = ",";
    public static final String CMB_SEPERATOR_ELEMENTS = " - ";
    public static final Pattern SEPERATOR_ELEMENTS_PAT = Pattern.compile(Pattern.quote(SEPERATOR_ELEMENTS));
    public static final Pattern CMB_SEPERATOR_ELEMENTS_PAT = Pattern.compile(Pattern.quote(CMB_SEPERATOR_ELEMENTS));

    private int primeMeridianCode;
    private String primeMeridianName;
    private double greenwichLongitude;
    private int uomCode;
    // more fields in csv
    // "PRIME_MERIDIAN_CODE","PRIME_MERIDIAN_NAME","GREENWICH_LONGITUDE","UOM_CODE","REMARKS","INFORMATION_SOURCE",
    // "DATA_SOURCE","REVISION_DATE","CHANGE_ID","DEPRECATED"

    public static PrimeMeridian Greenwich = new PrimeMeridian(8901, "Greenwich", 0, 9110);
    public static PrimeMeridian unKnown = new PrimeMeridian("unknown");

    public static TreeMap<String, PrimeMeridian> primeMeridians = new TreeMap<String, PrimeMeridian>();
    public static TreeMap<String, PrimeMeridian> primeMeridiansSortedId = new TreeMap<String, PrimeMeridian>();

    public PrimeMeridian(int primeMeridianCode, String primeMeridianName, double greenwichLongitude, int uomCode) {
        this.setPrimeMeridianCode(primeMeridianCode);
        this.setPrimeMeridianName(primeMeridianName);
        this.setGreenwichLongitude(greenwichLongitude);
        this.setUomCode(uomCode);
    }

    public PrimeMeridian(
            String primeMeridianCode, String primeMeridianName, String greenwichLongitude, String uomCode) {
        this.setPrimeMeridianCode(Integer.parseInt(primeMeridianCode));
        this.setPrimeMeridianName(primeMeridianName);
        this.setGreenwichLongitude(Double.parseDouble(greenwichLongitude));
        this.setUomCode(Integer.parseInt(uomCode));
    }

    public PrimeMeridian(String primeMeridianName) {
        this.setPrimeMeridianName(primeMeridianName);
    }

    public static Vector<String> getPrimeMeridianNames() {
        Vector<String> primeMeridians2 = new Vector<String>();
        for (PrimeMeridian primeMeridian : primeMeridians.values()) {
            primeMeridians2.add(primeMeridian.primeMeridianName);
        }

        return primeMeridians2;
    }

    public static Vector<String> getPrimeMeridiansForCmb() {
        Vector<String> primeMeridians2 = new Vector<String>();
        for (PrimeMeridian primeMeridian : primeMeridiansSortedId.values()) {
            primeMeridians2.add(primeMeridian.getPrimeMeridianForCmb());
        }

        return primeMeridians2;
    }

    private String getPrimeMeridianForCmb() {
        if (primeMeridianCode == 0) {
            return primeMeridianName;
        }

        return "EPSG:" + primeMeridianCode + " - " + primeMeridianName;
    }

    public static String getPrimeMeridianForCmb(String primeMeridianName) {
        return getPrimeMeridianFromCmb(primeMeridianName).getPrimeMeridianForCmb();
    }

    public static String getPrimeMeridianForCmbFromCode(int primeMeridianCode) {
        PrimeMeridian ref;
        if (primeMeridianCode == 0) {
            return unKnown.getPrimeMeridianForCmb();
        }

        ref = get(primeMeridianCode);
        return ref.getPrimeMeridianForCmb();
    }

    private static PrimeMeridian get(int primeMeridianCode) {
        for (PrimeMeridian primeMeridian : primeMeridians.values()) {
            if (primeMeridian.primeMeridianCode == primeMeridianCode) {
                return primeMeridian;
            }
        }

        return unKnown;
    }

    public static PrimeMeridian getPrimeMeridianFromCmb(String cmbBoxSelectedItem) {
        if (cmbBoxSelectedItem == null) {
            return PrimeMeridian.unKnown;
        }

        String[] parts = CMB_SEPERATOR_ELEMENTS_PAT.split(cmbBoxSelectedItem);
        PrimeMeridian ref;
        if (parts.length == 1) {
            if (parts[0].replace("EPSG:", "").matches("[0-9]+")) {
                ref = PrimeMeridian.get(Integer.parseInt(parts[0].replace("EPSG:", "")));
            } else {
                ref = primeMeridians.get(parts[0]);
            }
        } else {
            if (parts[0].replace("EPSG:", "").matches("[0-9]+")) {
                ref = PrimeMeridian.get(Integer.parseInt(parts[0].replace("EPSG:", "")));
            } else {
                ref = primeMeridians.get(parts[1]);
            }
        }

        if (ref == null) {
            ref = PrimeMeridian.unKnown;
        }

        return ref;
    }

    public static void assureInit() {
        Debug.getLog().config("Initialize Prime Meridians");
        initFromPrecompiledFile();
        primeMeridians.put(unKnown.primeMeridianName, unKnown);
        primeMeridiansSortedId.put(Integer.toString(unKnown.primeMeridianCode), unKnown);
        // check all
        for (int i = 0; i < primeMeridians.size(); i++) {}

        if (primeMeridians.size() == 0) {
            Debug.getLog().config("Number loaded Prime Meridians: " + primeMeridians.size());
        } else {
            Debug.getLog()
                .config(
                    "Number loaded Prime Meridians: "
                        + primeMeridians.size()
                        + "  in ["
                        + primeMeridians.firstKey()
                        + " ,  "
                        + primeMeridians.lastKey()
                        + "]");
        }

        Greenwich = primeMeridians.get("Greenwich");
    }

    private static void initFromPrecompiledFile() {
        InputStream is = null;
        try {
            String fileName = gdal.GetConfigOption("GDAL_DATA") + "/prime_meridian.csv";
            // System.out.println("load Prime Meridians from "+fileName);
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (is != null) {
            initFromStream(is);
        }
    }

    private static void initFromStream(InputStream reader) {
        try (InputStreamReader a = new InputStreamReader(reader)) {
            initFromReader(a);
        } catch (IOException e) {
            Debug.getLog().log(Level.SEVERE, "problems loading all Prime Meridians", e);
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
            String line = br.readLine();
            while (line != null && !line.trim().isEmpty()) {
                try {
                    if (lineNo == 0 || line.startsWith("#") || line.startsWith("\"")) {
                        line = br.readLine();
                        lineNo++;
                        continue;
                    }

                    PrimeMeridian ref = PrimeMeridian.createUsingPredefinedString(line);
                    primeMeridians.put(ref.primeMeridianName, ref);
                    primeMeridiansSortedId.put(Integer.toString(ref.primeMeridianCode), ref);
                } catch (Exception e) {
                    Debug.getLog().log(Level.WARNING, "problems loading single Prime Meridian on line:" + lineNo, e);
                }

                line = br.readLine();
                lineNo++;
            }

        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "problems loading all Prime Meridians", e);
        }
    }

    private static PrimeMeridian createUsingPredefinedString(String stuff) {
        String[] parts = SEPERATOR_ELEMENTS_PAT.split(stuff);
        PrimeMeridian ref = new PrimeMeridian(parts[0], parts[1], parts[2], parts[3]);
        return ref;
    }

    public int getUomCode() {
        return uomCode;
    }

    public void setUomCode(int uomCode) {
        this.uomCode = uomCode;
    }

    public int getPrimeMeridianCode() {
        return primeMeridianCode;
    }

    public void setPrimeMeridianCode(int primeMeridianCode) {
        this.primeMeridianCode = primeMeridianCode;
    }

    public double getGreenwichLongitude() {
        return greenwichLongitude;
    }

    public void setGreenwichLongitude(double greenwichLongitude) {
        this.greenwichLongitude = greenwichLongitude;
    }

    public String getPrimeMeridianName() {
        return primeMeridianName;
    }

    public void setPrimeMeridianName(String primeMeridianNameX) {
        this.primeMeridianName = primeMeridianNameX;
    }
}
