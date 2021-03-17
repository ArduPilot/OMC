/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import eu.mavinci.core.obfuscation.IKeepAll;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class MissionInfo implements IMissionInfo, IKeepAll {

    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<Date> lastModified = new SimpleObjectProperty<>();
    private final ObjectProperty<File> folder = new SimpleObjectProperty<>();

    private final DoubleProperty minLatitude = new SimpleDoubleProperty();
    private final DoubleProperty maxLatitude = new SimpleDoubleProperty();
    private final DoubleProperty minLongitude = new SimpleDoubleProperty();
    private final DoubleProperty maxLongitude = new SimpleDoubleProperty();

    private final DoubleProperty maxElev = new SimpleDoubleProperty();
    private final DoubleProperty minElev = new SimpleDoubleProperty();
    private final BooleanProperty maxElevPresent = new SimpleBooleanProperty();
    private final BooleanProperty minElevPresent = new SimpleBooleanProperty();

    private final StringProperty srsId = new SimpleStringProperty();
    private final StringProperty srsName = new SimpleStringProperty();
    private final StringProperty srsWkt = new SimpleStringProperty();
    private final StringProperty srsOrigin = new SimpleStringProperty();

    private final ObjectProperty<LatLon> startCoordinates = new SimpleObjectProperty<>(LatLon.ZERO);

    private final ListProperty<String> loadedFlightPlans =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<String> loadedDataSets = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<String> flightLogs = new SimpleListProperty<>(FXCollections.observableArrayList());

    // for the gson
    public MissionInfo() {}

    public MissionInfo(Path folder) {
        this.folderProperty().setValue(folder.toFile());
        this.lastModified.setValue(new Date());
        name.setValue(folder.getFileName().toString());
    }

    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String name) {
        this.name.setValue(name);
    }

    public ObjectProperty<File> folderProperty() {
        return folder;
    }

    @Override
    public File getFolder() {
        return folder.get();
    }

    @Override
    public Path getFolderPath() {
        return folder.get().toPath();
    }

    @Override
    public void setFolder(File file) {
        folder.setValue(file);
    }

    @Override
    public Sector getSector() {
        return new Sector(
            Angle.fromDegrees(getMinLatitude()),
            Angle.fromDegrees(getMaxLatitude()),
            Angle.fromDegrees(getMinLongitude()),
            Angle.fromDegrees(getMaxLongitude()));
    }

    @Override
    public Date getLastModified() {
        return lastModified.getValue();
    }

    public ObjectProperty<Date> lastModifiedProperty() {
        return lastModified;
    }

    @Override
    public void setLastModified(Date date) {
        this.lastModified.setValue(date);
    }

    public DoubleProperty minLatitudeProperty() {
        return minLatitude;
    }

    public DoubleProperty maxLatitudeProperty() {
        return maxLatitude;
    }

    public DoubleProperty minLongitudeProperty() {
        return minLongitude;
    }

    public DoubleProperty maxLongitudeProperty() {
        return maxLongitude;
    }

    @Override
    public double getMaxLatitude() {
        return maxLatitude.getValue();
    }

    @Override
    public void setMaxLatitude(double maxLat) {
        this.maxLatitude.set(maxLat);
    }

    @Override
    public double getMinLatitude() {
        return minLatitude.getValue();
    }

    @Override
    public void setMinLatitude(double minLat) {
        this.minLatitude.setValue(minLat);
    }

    @Override
    public double getMaxLongitude() {
        return maxLongitude.getValue();
    }

    @Override
    public void setMaxLongitude(double maxLon) {
        this.maxLongitude.setValue(maxLon);
    }

    @Override
    public double getMinLongitude() {
        return minLongitude.getValue();
    }

    @Override
    public void setMinLongitude(double minLon) {
        this.minLongitude.setValue(minLon);
    }

    @Override
    public OptionalDouble getMaxElev() {
        return maxElevPresent.get() ? OptionalDouble.of(maxElev.getValue()) : OptionalDouble.empty();
    }

    @Override
    public void setMaxElev(OptionalDouble d) {
        boolean present = d.isPresent();
        maxElevPresent.setValue(present);
        if (present) {
            maxElev.setValue(d.getAsDouble());
        }
    }

    @Override
    public OptionalDouble getMinElev() {
        return minElevPresent.get() ? OptionalDouble.of(minElev.getValue()) : OptionalDouble.empty();
    }

    @Override
    public void setMinElev(OptionalDouble d) {
        boolean present = d.isPresent();
        minElevPresent.setValue(present);
        if (present) {
            minElev.setValue(d.getAsDouble());
        }
    }

    @Override
    public String getSrsId() {
        return srsId.getValue();
    }

    @Override
    public void setSrsId(String id) {
        this.srsId.setValue(id);
    }

    @Override
    public String getSrsName() {
        return srsName.getValue();
    }

    @Override
    public void setSrsName(String name) {
        this.srsName.setValue(name);
    }

    @Override
    public String getSrsWkt() {
        return srsWkt.getValue();
    }

    @Override
    public void setSrsWkt(String srsWkt) {
        this.srsWkt.setValue(srsWkt);
    }

    @Override
    public String getSrsOrigin() {
        return srsOrigin.getValue();
    }

    @Override
    public void setSrsOrigin(String origin) {
        this.srsOrigin.setValue(origin);
    }

    @Override
    public List<String> getLoadedFlightPlans() {
        return loadedFlightPlans;
    }

    @Override
    public void setLoadedFlightPlans(List<String> flightPlans) {
        loadedFlightPlans.setAll(flightPlans);
    }

    @Override
    public List<String> getLoadedDataSets() {
        return loadedDataSets;
    }

    @Override
    public void setLoadedDataSets(List<String> dataSets) {
        this.loadedDataSets.setAll(dataSets);
    }

    @Override
    public void copyValues(IMissionInfo missionInfo) {
        this.folder.set(missionInfo.getFolder());
        this.name.setValue(missionInfo.getName());
        this.lastModified.setValue(missionInfo.getLastModified());

        this.minLatitude.setValue(missionInfo.getMinLatitude());
        this.maxLatitude.setValue(missionInfo.getMaxLatitude());
        this.minLongitude.setValue(missionInfo.getMinLongitude());
        this.maxLongitude.setValue(missionInfo.getMaxLongitude());

        this.srsId.setValue(missionInfo.getSrsId());
        this.srsName.setValue(missionInfo.getSrsName());
        this.srsOrigin.setValue(missionInfo.getSrsOrigin());
        this.srsWkt.setValue(missionInfo.getSrsWkt());

        this.loadedFlightPlans.setAll(missionInfo.getLoadedFlightPlans());
        this.loadedDataSets.setAll(missionInfo.getLoadedDataSets());
    }

    @Override
    public LatLon getStartCoordinates() {
        return startCoordinates.get();
    }

    @Override
    public void setStartCoordinates(LatLon startCoordinates) {
        this.startCoordinates.set(startCoordinates);
    }

    @Override
    public List<String> getFlightLogs() {
        return flightLogs;
    }

    @Override
    public void setFlightLogs(List<String> flightLogs) {
        this.flightLogs.setAll(flightLogs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionInfo that = (MissionInfo)o;
        return Objects.equals(name.getValue(), that.name.getValue())
            && Objects.equals(lastModified.getValue(), that.lastModified.getValue())
            && Objects.equals(folder.getValue(), that.folder.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.getValue(), lastModified.getValue(), folder.getValue());
    }

    @Override
    public String toString() {
        return getName();
    }
}
