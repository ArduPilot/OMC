/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import com.intel.missioncontrol.Localizable;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import java.util.LinkedList;
import java.util.logging.Level;

public class ElevationModelShiftWrapper extends AElevationModelWrapper {

    private double shift;
    private Sector sector;
    private final IEgmModel egm;

    private boolean isInit = false;

    public ElevationModelShiftWrapper(ElevationModel slave, Sector sector, IEgmModel egm) {
        super(slave);
        this.shift = 0;
        this.sector = sector;
        this.egm = egm;
    }

    public boolean isInit() {
        return isInit;
    }

    public void setNotInit() {
        isInit = false;
    }

    public static final double SHIFT_MIN = -1000;
    public static final double SHIFT_MAX = 10000;

    public void setShift(double shift) {
        if (!MathHelper.isValid(shift)) {
            shift = 0;
        }

        shift = MathHelper.intoRange(shift, SHIFT_MIN, SHIFT_MAX);
        isInit = true;
        if (this.shift == shift) {
            return;
        }

        this.shift = shift;
    }

    public double getShift() {
        return shift;
    }

    public ElevationModel getSlave() {
        return slave;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    @Override
    public double shift(double slaveAlt, Angle latitude, Angle longitude) {
        if (slaveAlt == getMissingDataSignal()) {
            return slaveAlt;
        }

        return slaveAlt + shift;
    }

    @Override
    double getExtremeMinShift() {
        return shift;
    }

    @Override
    double getExtremeMaxShift() {
        return shift;
    }

    ShiftType shiftType = ShiftType.MANUAL;

    public void setShiftType(ShiftType shiftType) {
        if (shiftType == null) {
            return;
        }

        if (shiftType == this.shiftType) {
            return;
        }

        this.shiftType = shiftType;
        if (shiftType == ShiftType.ADD_EGM || shiftType == ShiftType.SUB_EGM) {
            LatLon center = sector.getCentroid();
            double offset = egm.getEGM96Offset(center);
            if (shiftType == ShiftType.SUB_EGM) {
                offset = -offset;
            }

            setShift(offset);
        }
    }

    public void autoAdjustShift() {
        if (shiftType != ShiftType.MANUAL) {
            return;
        }

        ElevationModel master =
            DependencyInjector.getInstance()
                .getInstanceOf(IElevationModelsManager.class)
                .baseLayerProperty()
                .getElevationModel();
        Debug.getLog().log(Level.CONFIG, "reshiftElevationModel: " + getSlave().getName());
        final int sample_count = 20;
        if (getSector() != null) {
            for (int attempt = 0; attempt < 2; attempt++) {
                synchronized (this) {
                    Sector sector1 = getSector();
                    double minRad = Math.min(sector1.getDeltaLatRadians(), sector1.getDeltaLonRadians()) / sample_count;
                    double oldShift = getShift();
                    double sumShift = 0;
                    int sumShiftCount = 0;
                    setShift(0);

                    LinkedList<LatLon> locations = new LinkedList<>();
                    for (int ix = 0; ix <= sample_count; ix++) {
                        for (int iy = 0; iy <= sample_count; iy++) {
                            LatLon latLon =
                                LatLon.fromDegrees(
                                    sector1.getMinLatitude().degrees + ix * sector1.getDeltaLatDegrees() / sample_count,
                                    sector1.getMinLongitude().degrees
                                        + iy * sector1.getDeltaLonDegrees() / sample_count);
                            if (master.contains(latLon.latitude, latLon.longitude)
                                    && contains(latLon.latitude, latLon.longitude)) {
                                locations.add(latLon);
                                sumShiftCount++;
                            }
                        }
                    }

                    double[] bufferMaster = new double[sumShiftCount];
                    double masterTargetRes = Math.max(master.getBestResolution(sector1), minRad);
                    double masterActualRes = master.getElevations(sector1, locations, masterTargetRes, bufferMaster);
                    double masterResMismatch = masterActualRes / masterTargetRes;

                    double[] bufferSlave = new double[sumShiftCount];
                    double slaveTargetRes = Math.max(getBestResolution(sector1), minRad);
                    double slaveActualRes = getElevations(sector1, locations, slaveTargetRes, bufferSlave);
                    double slaveResMismatch = slaveActualRes / slaveTargetRes;

                    if (masterResMismatch >= 2) {
                        Debug.getLog().log(Level.INFO, "master resolution too bad by " + masterResMismatch + "x");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }

                        continue;
                    }

                    if (slaveResMismatch >= 2) {
                        Debug.getLog().log(Level.INFO, "slave resolution too bad by " + slaveResMismatch + "x");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }

                        continue;
                    }

                    int num = 0;
                    for (int i = 0; i != sumShiftCount; i++) {
                        double tmp1 = bufferMaster[i];
                        double tmp2 = bufferSlave[i];
                        if (tmp1 == master.getMissingDataSignal() || tmp1 == master.getMissingDataReplacement()) {
                            continue;
                        }

                        if (tmp2 == getMissingDataSignal() || tmp1 == getMissingDataReplacement()) {
                            continue;
                        }

                        num++;
                        sumShift += tmp1;
                        sumShift -= tmp2;
                    }

                    if (num == 0) {
                        sumShift = oldShift;
                        Debug.getLog().log(Level.INFO, "could not compute shift, 0 samples found");
                    } else {
                        sumShift /= num;
                    }

                    Debug.getLog().info("model shift [m] is " + sumShift + " for " + getSlave().getName());

                    if (Math.abs(sumShift - oldShift) < SHIFT_CHANGE_THRESHOLD) {
                        sumShift = oldShift;
                    }

                    Debug.getLog().log(Level.INFO, "new Shift=" + sumShift + "  delta=" + (sumShift - oldShift));
                    setShift(sumShift);
                }
            }
        }
    }

    public static final double SHIFT_CHANGE_THRESHOLD = 0.3;

    public ShiftType getShiftType() {
        return shiftType;
    }

    public enum ShiftType implements Localizable {
        MANUAL,
        ADD_EGM,
        SUB_EGM
    }

    @Override
    public String toString() {
        return "wrapping-elevationModel: shift:" + shift + "m around:" + slave;
    }

}
