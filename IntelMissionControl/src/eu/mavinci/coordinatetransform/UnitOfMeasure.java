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

public class UnitOfMeasure {
    public static final String SEPERATOR_ELEMENTS = ",";
    public static final Pattern SEPERATOR_ELEMENTS_PAT = Pattern.compile(Pattern.quote(SEPERATOR_ELEMENTS));
    public static final String CMB_SEPERATOR_ELEMENTS = " - ";
    public static final Pattern CMB_SEPERATOR_ELEMENTS_PAT = Pattern.compile(Pattern.quote(CMB_SEPERATOR_ELEMENTS));

    private String unitOfMeasureName;
    private String unitOfMeasureType;
    private int targetUomCode;
    // private double factor;
    private double factorB;
    private double factorC;
    private int uomCode;
    // more fields in csv
    // uom_code,unit_of_meas_name,unit_of_meas_type,target_uom_code,factor_b,factor_c,remarks,information_source,
    // data_source,revision_date,change_id,deprecated

    // evtl standards; in case file not loaded; mainly used entries
    public static UnitOfMeasure unKnown = new UnitOfMeasure("unknown");
    public static UnitOfMeasure notDef =
        new UnitOfMeasure("Meter", "length", 0, 1, 1, -1); // TODO Default value really meter?
    // public static UnitOfMeasure userOptimized = new UnitOfMeasure("userOptimized");

    public static UnitOfMeasure meter = new UnitOfMeasure("metre", "length", 9001, 1, 1, 9001);
    public static UnitOfMeasure foot = new UnitOfMeasure("Foot", "length", 9001, 0.3048, 1, 9001);

    public static TreeMap<String, UnitOfMeasure> unitOfMeasures = new TreeMap<String, UnitOfMeasure>();
    public static TreeMap<String, UnitOfMeasure> unitOfMeasuresSortedId = new TreeMap<String, UnitOfMeasure>();

    public UnitOfMeasure(String unitOfMeasName) {
        this.setUnitOfMeasName(unitOfMeasName);
    }

    public UnitOfMeasure(
            String unitOfMeasName,
            String unitOfMeasType,
            int targetUomCode,
            double factorB,
            double factorC,
            int uomCode) {
        this.setUnitOfMeasName(unitOfMeasName);
        this.setUnitOfMeasType(unitOfMeasType);
        this.setTargetUomCode(targetUomCode);
        this.setFactorB(factorB);
        this.setFactorC(factorC);
        this.setUomCode(uomCode);
    }

    public UnitOfMeasure(
            String uomCode,
            String unitOfMeasName,
            String unitOfMeasType,
            String targetUomCode,
            String factorB,
            String factorC) {
        try {
            this.setUomCode(Integer.parseInt(uomCode));
            this.setUnitOfMeasName(unitOfMeasName);
            this.setUnitOfMeasType(unitOfMeasType);
            this.setTargetUomCode(Integer.parseInt(targetUomCode));
            if (!factorB.equals("")) {
                this.setFactorB(Double.parseDouble(factorB));
            }

            if (!factorC.equals("")) {
                this.setFactorC(Double.parseDouble(factorC));
            }

            this.setUomCode(Integer.parseInt(uomCode));
        } catch (NumberFormatException e) {
            // e.printStackTrace();
        }
    }

    public static Vector<String> getUnitOfMeasureNames() {
        Vector<String> unitOfMeasures2 = new Vector<String>();
        for (UnitOfMeasure unitOfMeasure : unitOfMeasures.values()) {
            unitOfMeasures2.add(unitOfMeasure.unitOfMeasureName);
        }

        return unitOfMeasures2;
    }

    public static Vector<String> getUnitOfMeasuresForCmb() {
        Vector<String> unitOfMeasures2 = new Vector<String>();
        for (UnitOfMeasure unitOfMeasure : unitOfMeasuresSortedId.values()) {
            unitOfMeasures2.add(unitOfMeasure.getUnitOfMeasureForCmb());
        }

        return unitOfMeasures2;
    }

    public static Vector<String> getUnitOfMeasuresAngleForCmb() {
        Vector<String> unitOfMeasures2 = new Vector<String>();
        for (UnitOfMeasure unitOfMeasure : unitOfMeasuresSortedId.values()) {
            if (unitOfMeasure.unitOfMeasureType == null) {
                unitOfMeasures2.add(unitOfMeasure.getUnitOfMeasureForCmb());
            } else if (unitOfMeasure.unitOfMeasureType.equals("angle")) {
                unitOfMeasures2.add(unitOfMeasure.getUnitOfMeasureForCmb());
            }
        }

        return unitOfMeasures2;
    }

    public static Vector<String> getUnitOfMeasuresLengthForCmb() {
        Vector<String> unitOfMeasures2 = new Vector<String>();
        for (UnitOfMeasure unitOfMeasure : unitOfMeasuresSortedId.values()) {
            if (unitOfMeasure.unitOfMeasureType == null) {
                unitOfMeasures2.add(unitOfMeasure.getUnitOfMeasureForCmb());
            } else if (unitOfMeasure.unitOfMeasureType.equals("length")) {
                unitOfMeasures2.add(unitOfMeasure.getUnitOfMeasureForCmb());
            }
        }

        return unitOfMeasures2;
    }

    public static Vector<String> getUnitOfMeasuresScaleForCmb() {
        Vector<String> unitOfMeasures2 = new Vector<String>();
        for (UnitOfMeasure unitOfMeasure : unitOfMeasuresSortedId.values()) {
            if (unitOfMeasure.unitOfMeasureType == null) {
                unitOfMeasures2.add(unitOfMeasure.getUnitOfMeasureForCmb());
            } else if (unitOfMeasure.unitOfMeasureType.equals("scale")) {
                unitOfMeasures2.add(unitOfMeasure.getUnitOfMeasureForCmb());
            }
        }

        return unitOfMeasures2;
    }

    public String getUnitOfMeasureForCmb() {
        if (uomCode == 0) {
            return unitOfMeasureName;
        }

        if (uomCode == -1) {
            return "default (Meter)";
        }

        return "EPSG:" + uomCode + " - " + unitOfMeasureName;
    }

    public static String getUnitOfMeasureForCmb(String unitOfMeasureName) {
        if (unitOfMeasureName == null) {
            return unKnown.getUnitOfMeasureForCmb();
        }

        if (unitOfMeasureName.equals("")) {
            return notDef.getUnitOfMeasureForCmb();
        }

        if (unitOfMeasureName.equals("Meter")) {
            return notDef.getUnitOfMeasureForCmb();
        }

        UnitOfMeasure ref = getUnitOfMeasureFromCmb(unitOfMeasureName);
        if (ref == null) {
            return unKnown.getUnitOfMeasureForCmb();
        }

        return ref.getUnitOfMeasureForCmb();
    }

    public static UnitOfMeasure getUnitOfMeasureFromCmb(String cmbBoxSelectedItem) {
        if (cmbBoxSelectedItem == null
                || cmbBoxSelectedItem.equals("default (Meter)")
                || cmbBoxSelectedItem.equals("")) {
            return UnitOfMeasure.notDef;
        }

        if (cmbBoxSelectedItem.equals("unknown")) {
            return UnitOfMeasure.unKnown;
        }

        String[] parts = CMB_SEPERATOR_ELEMENTS_PAT.split(cmbBoxSelectedItem);
        UnitOfMeasure ref;
        if (parts.length == 1) {
            if (parts[0].replace("EPSG:", "").matches("[0-9]+")) {
                ref = UnitOfMeasure.get(Integer.parseInt(parts[0].replace("EPSG:", "")));
            } else {
                ref = unitOfMeasures.get(parts[0]);
            }
        } else {
            if (parts[0].replace("EPSG:", "").matches("[0-9]+")) {
                ref = UnitOfMeasure.get(Integer.parseInt(parts[0].replace("EPSG:", "")));
            } else {
                ref = unitOfMeasures.get(parts[1]);
            }
        }

        return ref;
    }

    public static UnitOfMeasure getUnitOfMeasureFromCmb(Object cmbBoxSelectedItem) {
        return getUnitOfMeasureFromCmb(cmbBoxSelectedItem.toString());
    }

    public static String getUnitOfMeasureForCmbFromCode(int uomCode) {
        UnitOfMeasure ref;
        if (uomCode == 0) {
            return unKnown.getUnitOfMeasureForCmb();
        }

        if (uomCode == -1) {
            return notDef.getUnitOfMeasureForCmb();
        }

        ref = get(uomCode);
        return ref.getUnitOfMeasureForCmb();
    }

    private static UnitOfMeasure get(int uomCode) {
        for (UnitOfMeasure unitOfMeasure : unitOfMeasures.values()) {
            if (unitOfMeasure.uomCode == uomCode) {
                return unitOfMeasure;
            }
        }

        return unKnown;
    }

    public static void assureInit() {
        Debug.getLog().config("Initialize Units of Measure");
        initFromPrecompiledFile();
        // other known entries:
        unitOfMeasures.put(unKnown.unitOfMeasureName, unKnown);
        unitOfMeasures.put(unKnown.unitOfMeasureName, notDef);
        unitOfMeasuresSortedId.put(Integer.toString(unKnown.uomCode), unKnown);
        unitOfMeasuresSortedId.put(Integer.toString(notDef.uomCode), notDef);
        // check all
        for (int i = 0; i < unitOfMeasures.size(); i++) {}

        if (unitOfMeasures.size() == 0) {
            Debug.getLog().config("Number loaded Units of Measure: " + unitOfMeasures.size());
        } else {
            Debug.getLog()
                .config(
                    "Number loaded Units of Measure: "
                        + unitOfMeasures.size()
                        + "  in ["
                        + unitOfMeasures.firstKey()
                        + " ,  "
                        + unitOfMeasures.lastKey()
                        + "]");
        }
    }

    private static void initFromPrecompiledFile() {
        InputStream is = null;
        try {
            String fileName = gdal.GetConfigOption("GDAL_DATA") + "/unit_of_measure.csv";
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
            Debug.getLog().log(Level.SEVERE, "problems loading all Units of Measure", e);
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
            String line2 = null;

            while (line != null && !line.trim().isEmpty()) {
                try {
                    if (line.startsWith("#") || lineNo == 0) {
                        line = br.readLine();
                        lineNo++;
                        continue;
                    }

                    if ((line.length() - line.replaceAll("\"", "").length()) % 2 == 1) {
                        line = line + " " + br.readLine();
                        lineNo++;
                        continue;
                    }

                    UnitOfMeasure ref = UnitOfMeasure.createUsingPredefinedString(line);
                    unitOfMeasures.put(ref.unitOfMeasureName, ref);
                    unitOfMeasuresSortedId.put(Integer.toString(ref.uomCode), ref);
                } catch (Exception e) {
                    Debug.getLog().log(Level.WARNING, "problems loading single unitOfMeasure on line:" + lineNo, e);
                }

                line = br.readLine();
                lineNo++;
            }

        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "problems loading all unitOfMeasure", e);
        }
    }

    private static UnitOfMeasure createUsingPredefinedString(String stuff) {
        String[] parts = SEPERATOR_ELEMENTS_PAT.split(stuff);

        UnitOfMeasure ref = new UnitOfMeasure(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        return ref;
    }

    public int getUomCode() {
        return uomCode;
    }

    public void setUomCode(int uomCode) {
        this.uomCode = uomCode;
    }

    public String getUnitOfMeasName() {
        if (unitOfMeasureName.equals("degree (supplier to define representation)")) {
            return "degree";
        }

        return unitOfMeasureName;
    }

    public void setUnitOfMeasName(String unitOfMeasName) {
        this.unitOfMeasureName = unitOfMeasName;
    }

    public String getUnitOfMeasType() {
        return unitOfMeasureType;
    }

    public void setUnitOfMeasType(String unitOfMeasType) {
        this.unitOfMeasureType = unitOfMeasType;
    }

    public double getFactorB() {
        return factorB;
    }

    public void setFactorB(double factorB) {
        this.factorB = factorB;
    }

    public int getTargetUomCode() {
        return targetUomCode;
    }

    public void setTargetUomCode(int targetUomCode) {
        this.targetUomCode = targetUomCode;
    }

    public double getFactorC() {
        return factorC;
    }

    public void setFactorC(double factorC) {
        this.factorC = factorC;
    }

    public double getFactor() {
        return getFactorB() / getFactorC();
    }

}
