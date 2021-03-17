/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

public abstract class AElevationModelWrapper implements ElevationModel {

    ElevationModel slave;
    private final Object slaveMutex = new Object();

    public AElevationModelWrapper(ElevationModel slave) {
        this.slave = slave;
    }

    @Override
    public double getDetailHint(Sector sector) {
        synchronized (slaveMutex) {
            return slave.getDetailHint(sector);
        }
    }

    public ElevationModel getSlave() {
        return slave;
    }

    @Override
    public double getMissingDataReplacement() {
        synchronized (slaveMutex) {
            return slave.getMissingDataReplacement();
        }
    }

    @Override
    public void setMissingDataReplacement(double missingDataValue) {
        synchronized (slaveMutex) {
            slave.setMissingDataReplacement(missingDataValue);
        }
    }

    @Override
    public double getMissingDataSignal() {
        synchronized (slaveMutex) {
            return slave.getMissingDataSignal();
        }
    }

    @Override
    public void setMissingDataSignal(double missingDataFlag) {
        synchronized (slaveMutex) {
            slave.setMissingDataSignal(missingDataFlag);
        }
    }

    @Override
    public int intersects(Sector sector) {
        synchronized (slaveMutex) {
            return slave.intersects(sector);
        }
    }

    public abstract double shift(double slaveAlt, Angle latitude, Angle longitude);

    @Override
    public double getElevations(
            Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer) {
        double ret;
        synchronized (slaveMutex) {
            ret = slave.getElevations(sector, latlons, targetResolution, buffer);
        }

        for (int i = 0; i != buffer.length; i++) {
            LatLon latLon = latlons.get(i);
            if (contains(latLon.latitude, latLon.longitude)) {
                buffer[i] = shift(buffer[i], latLon.latitude, latLon.longitude);
            }
        }

        return ret;
    }

    @Override
    public double[] getElevations(
            Sector sector, List<? extends LatLon> latlons, double[] targetResolution, double[] buffer) {
        double[] ret;
        synchronized (slaveMutex) {
            ret = slave.getElevations(sector, latlons, targetResolution, buffer);
        }

        for (int i = 0; i != buffer.length; i++) {
            LatLon latLon = latlons.get(i);
            if (contains(latLon.latitude, latLon.longitude)) {
                buffer[i] = shift(buffer[i], latLon.latitude, latLon.longitude);
            }
        }

        return ret;
    }

    @Override
    public double getUnmappedElevation(Angle latitude, Angle longitude) {
        synchronized (slaveMutex) {
            return shift(getLastUnmappedElevation(latitude, longitude), latitude, longitude);
        }
    }

    @Override
    public double getUnmappedElevations(
            Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer) {
        double ret;
        synchronized (slaveMutex) {
            ret = slave.getUnmappedElevations(sector, latlons, targetResolution, buffer);
        }

        int sign = 1;
        for (int i = 0; i != buffer.length; i++) {
            LatLon latLon = latlons.get(i);
            if (contains(latLon.latitude, latLon.longitude)) {
                // FIXME .. or better let the WWJ team do the lib fix ;-)
                buffer[i] =
                    shift(
                        buffer[i],
                        latLon.latitude,
                        latLon.longitude); // don't use the buffer here, since the contains function
                // of the BasicElevationModel is broken!
                // buffer[i]= shift(getLastUnmappedElevation(latLon.latitude, latLon.longitude),latLon.latitude,
                // latLon.longitude);
            } else {
                // System.out.println("NOT contained");
                sign = -1;
            }
        }

        return sign * ret;
    }

    @Override
    public double[] getUnmappedElevations(
            Sector sector, List<? extends LatLon> latlons, double[] targetResolution, double[] buffer) {
        double[] ret;
        synchronized (slaveMutex) {
            ret = slave.getUnmappedElevations(sector, latlons, targetResolution, buffer);
        }

        for (int i = 0; i != buffer.length; i++) {
            LatLon latLon = latlons.get(i);
            if (contains(latLon.latitude, latLon.longitude)) {
                // FIXME .. or better let the WWJ team do the lib fix ;-)
                buffer[i] =
                    shift(
                        buffer[i],
                        latLon.latitude,
                        latLon.longitude); // don't use the buffer here, since the contains function
                // of the BasicElevationModel is broken!
                // buffer[i]= shift(getLastUnmappedElevation(latLon.latitude, latLon.longitude),latLon.latitude,
                // latLon.longitude);
            }
        }

        return ret;
    }

    @Override
    public double[] getExtremeElevations(Angle latitude, Angle longitude) {
        double[] ret;
        synchronized (slaveMutex) {
            ret = slave.getExtremeElevations(latitude, longitude);
        }

        for (int i = 0; i != ret.length; i++) {
            ret[i] = shift(ret[i], latitude, longitude);
        }

        return ret;
    }

    abstract double getExtremeMinShift();

    abstract double getExtremeMaxShift();

    @Override
    public double[] getExtremeElevations(Sector sector) {
        double[] ret;
        synchronized (slaveMutex) {
            ret = slave.getExtremeElevations(sector);
        }

        for (int i = 0; i != ret.length; i++) {
            ret[i] = shift(ret[i], sector.getCentroid().latitude, sector.getCentroid().longitude);
        }

        return ret;
    }

    @Override
    public double getMaxElevation() {
        synchronized (slaveMutex) {
            return shift(slave.getMaxElevation(), null, null) + getExtremeMaxShift();
        }
    }

    @Override
    public double getMinElevation() {
        synchronized (slaveMutex) {
            return shift(slave.getMinElevation(), null, null) + getExtremeMinShift();
        }
    }

    boolean handleZeroAsUnavaliable = true;

    public void handleZeroAsUnavaliable(boolean handleZeroAsUnavaliable) {
        this.handleZeroAsUnavaliable = handleZeroAsUnavaliable;
    }

    public boolean isHandleZeroAsUnavaliable() {
        return this.handleZeroAsUnavaliable;
    }

    Angle lastLat;
    Angle lastLon;
    double lastUnmappedElevation;

    private double getLastUnmappedElevation(Angle latitude, Angle longitude) {
        // this has to be called in a synchronized way!
        if (lastLat != latitude || lastLon != longitude) {
            lastLat = latitude;
            lastLon = longitude;
            lastUnmappedElevation = slave.getUnmappedElevation(latitude, longitude);
        }

        return lastUnmappedElevation;
    }

    @Override
    public boolean contains(Angle latitude, Angle longitude) {
        // FIXME .. or better let the WWJ team do the lib fix ;-)
        // don't use only the contains function of the slave here, since the contains function of the
        // BasicElevationModel is broken!
        synchronized (slaveMutex) {
            if (!slave.contains(latitude, longitude)) {
                return false;
            }

            try {
                double unmappedElev = getLastUnmappedElevation(latitude, longitude);
                // System.out.println("lat"+latitude + " lon"+longitude + " unmappedElev="+unmappedElev + "
                // contains="+(unmappedElev !=
                // slave.getMissingDataSignal() && unmappedElev != 0));
                // TODO FIXME the != 0 is a workaround until pix4d are able to set the missingDataFlag, for now 0 means
                // no data ... this could
                // make strange spikes in the elevation model
                return unmappedElev != slave.getMissingDataSignal()
                    && (!handleZeroAsUnavaliable || (Math.abs(unmappedElev) > 0.001 && unmappedElev > -1000));
            } catch (Exception e) {
                Debug.getLog().log(Level.FINE, "slave contains function seems to be wrong", e);
                return false;
            }
        }
    }

    @Override
    public double getElevation(Angle latitude, Angle longitude) {
        synchronized (slaveMutex) {
            return shift(slave.getElevation(latitude, longitude), latitude, longitude);
        }
    }

    @Override
    public void composeElevations(Sector sector, List<? extends LatLon> latlons, int tileWidth, double[] buffer)
            throws Exception {
        synchronized (slaveMutex) {
            slave.composeElevations(sector, latlons, tileWidth, buffer);
        }

        Iterator<? extends LatLon> it = latlons.iterator();
        for (int i = 0; i != buffer.length; ++i) {
            LatLon latLon = it.next();
            buffer[i] = shift(buffer[i], latLon.latitude, latLon.longitude);
        }
    }

    @Override
    public double getBestResolution(Sector sector) {
        synchronized (slaveMutex) {
            return slave.getBestResolution(sector);
        }
    }

    @Override
    public Object setValue(String key, Object value) {
        synchronized (slaveMutex) {
            return slave.setValue(key, value);
        }
    }

    @Override
    public AVList setValues(AVList avList) {
        synchronized (slaveMutex) {
            return slave.setValues(avList);
        }
    }

    @Override
    public Object getValue(String key) {
        synchronized (slaveMutex) {
            return slave.getValue(key);
        }
    }

    @Override
    public Collection<Object> getValues() {
        synchronized (slaveMutex) {
            return slave.getValues();
        }
    }

    @Override
    public String getStringValue(String key) {
        synchronized (slaveMutex) {
            return slave.getStringValue(key);
        }
    }

    @Override
    public Set<Entry<String, Object>> getEntries() {
        synchronized (slaveMutex) {
            return slave.getEntries();
        }
    }

    @Override
    public boolean hasKey(String key) {
        synchronized (slaveMutex) {
            return slave.hasKey(key);
        }
    }

    @Override
    public Object removeKey(String key) {
        synchronized (slaveMutex) {
            return slave.removeKey(key);
        }
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        synchronized (slaveMutex) {
            slave.addPropertyChangeListener(propertyName, listener);
        }
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        synchronized (slaveMutex) {
            slave.removePropertyChangeListener(propertyName, listener);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized (slaveMutex) {
            slave.addPropertyChangeListener(listener);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        synchronized (slaveMutex) {
            slave.removePropertyChangeListener(listener);
        }
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        synchronized (slaveMutex) {
            slave.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    @Override
    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        synchronized (slaveMutex) {
            slave.firePropertyChange(propertyChangeEvent);
        }
    }

    @Override
    public AVList copy() {
        synchronized (slaveMutex) {
            return slave.copy();
        }
    }

    @Override
    public AVList clearList() {
        synchronized (slaveMutex) {
            return slave.clearList();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (slaveMutex) {
            slave.propertyChange(evt);
        }
    }

    @Override
    public void onMessage(Message msg) {
        synchronized (slaveMutex) {
            slave.onMessage(msg);
        }
    }

    @Override
    public String getRestorableState() {
        synchronized (slaveMutex) {
            return slave.getRestorableState();
        }
    }

    @Override
    public void restoreState(String stateInXml) {
        synchronized (slaveMutex) {
            slave.restoreState(stateInXml);
        }
    }

    @Override
    public void dispose() {
        synchronized (slaveMutex) {
            slave.dispose();
        }
    }

    @Override
    public String getName() {
        synchronized (slaveMutex) {
            return slave.getName();
        }
    }

    @Override
    public void setName(String name) {
        synchronized (slaveMutex) {
            slave.setName(name);
        }
    }

    @Override
    public boolean isNetworkRetrievalEnabled() {
        synchronized (slaveMutex) {
            return slave.isNetworkRetrievalEnabled();
        }
    }

    @Override
    public void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled) {
        synchronized (slaveMutex) {
            slave.setNetworkRetrievalEnabled(networkRetrievalEnabled);
        }
    }

    @Override
    public long getExpiryTime() {
        synchronized (slaveMutex) {
            return slave.getExpiryTime();
        }
    }

    @Override
    public void setExpiryTime(long expiryTime) {
        synchronized (slaveMutex) {
            slave.setExpiryTime(expiryTime);
        }
    }

    @Override
    public double getLocalDataAvailability(Sector sector, Double targetResolution) {
        synchronized (slaveMutex) {
            return slave.getLocalDataAvailability(sector, targetResolution);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        synchronized (slaveMutex) {
            slave.setEnabled(enabled);
        }
    }

    @Override
    public boolean isEnabled() {
        synchronized (slaveMutex) {
            return slave.isEnabled();
        }
    }

    @Override
    public double[] getBestResolutions(Sector sector) {
        synchronized (slaveMutex) {
            return slave.getBestResolutions(sector);
        }
    }

    @Override
    public void setExtremesCachingEnabled(boolean enabled) {
        synchronized (slaveMutex) {
            slave.setExtremesCachingEnabled(enabled);
        }
    }

    @Override
    public boolean isExtremesCachingEnabled() {
        synchronized (slaveMutex) {
            return slave.isExtremesCachingEnabled();
        }
    }

    @Override
    public double getUnmappedLocalSourceElevation(Angle latitude, Angle longitude) {
        synchronized (slaveMutex) {
            return shift(slave.getUnmappedLocalSourceElevation(latitude, longitude), latitude, longitude);
        }
    }
}
