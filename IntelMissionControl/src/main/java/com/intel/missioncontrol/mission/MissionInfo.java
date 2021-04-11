package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.common.Expect;
import eu.mavinci.core.obfuscation.IKeepAll;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import javax.annotation.ParametersAreNonnullByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
public class MissionInfo implements IKeepAll {

    private @Nullable String remoteId;
    private String name;
    private Date lastModified;
    private File folder;

    private double minLatitude;
    private double maxLatitude;
    private double minLongitude;
    private double maxLongitude;

    private double maxElev;
    private double minElev;
    private boolean maxElevPresent;
    private boolean minElevPresent;

    private String srsId;
    private String srsName;
    private String srsWkt;
    private String srsOrigin;

    private LatLon startCoordinates = LatLon.ZERO;

    private final List<String> loadedFlightPlans = new ArrayList<>();
    private final List<String> loadedDataSets = new ArrayList<>();
    private final List<String> flightLogs = new ArrayList<>();

    // for the gson
    public MissionInfo() {}

    public MissionInfo(Path folder) {
        this.folder = folder.toFile();
        this.lastModified = new Date();
        this.name = folder.getFileName().toString();
    }

    public MissionInfo(Path folder, String remoteId) {
        this.folder = folder.toFile();
        this.lastModified = new Date();
        this.name = folder.getFileName().toString();
        this.remoteId = remoteId;
    }

    public @Nullable String getRemoteId() {
        return remoteId;
    }

    void setRemoteId(@Nullable String remoteId) {
        this.remoteId = remoteId;
    }

    public boolean isRemote() {
        return remoteId != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Expect.notNull(name, "name");
        this.name = name;
    }

    public File getFolder() {
        return folder;
    }

    public Path getFolderPath() {
        return folder.toPath();
    }

    public void setFolder(File file) {
        Expect.notNull(file, "file");
        this.folder = file;
    }

    public Sector getSector() {
        return new Sector(
            Angle.fromDegrees(getMinLatitude()),
            Angle.fromDegrees(getMaxLatitude()),
            Angle.fromDegrees(getMinLongitude()),
            Angle.fromDegrees(getMaxLongitude()));
    }

    public void setSector(Sector s) {
        setMinLatitude(s.getMinLatitude().degrees);
        setMaxLatitude(s.getMaxLatitude().degrees);
        setMinLongitude(s.getMinLongitude().degrees);
        setMaxLongitude(s.getMaxLongitude().degrees);
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date date) {
        Expect.notNull(date, "date");
        this.lastModified = date;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    void setMaxLatitude(double maxLat) {
        this.maxLatitude = maxLat;
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    void setMinLatitude(double minLat) {
        this.minLatitude = minLat;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }

    void setMaxLongitude(double maxLon) {
        this.maxLongitude = maxLon;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    void setMinLongitude(double minLon) {
        this.minLongitude = minLon;
    }

    public OptionalDouble getMaxElev() {
        return maxElevPresent ? OptionalDouble.of(maxElev) : OptionalDouble.empty();
    }

    void setMaxElev(OptionalDouble maxElev) {
        Expect.notNull(maxElev, "maxElev");
        boolean present = maxElev.isPresent();
        maxElevPresent = present;
        if (present) {
            this.maxElev = maxElev.getAsDouble();
        }
    }

    public OptionalDouble getMinElev() {
        return minElevPresent ? OptionalDouble.of(minElev) : OptionalDouble.empty();
    }

    void setMinElev(OptionalDouble minElev) {
        Expect.notNull(minElev, "minElev");
        boolean present = minElev.isPresent();
        minElevPresent = present;
        if (present) {
            this.minElev = minElev.getAsDouble();
        }
    }

    public String getSrsId() {
        return srsId;
    }

    void setSrsId(String id) {
        Expect.notNull(id, "id");
        this.srsId = id;
    }

    public String getSrsName() {
        return srsName;
    }

    void setSrsName(String name) {
        Expect.notNull(name, "name");
        this.srsName = name;
    }

    public String getSrsWkt() {
        return srsWkt;
    }

    void setSrsWkt(String srsWkt) {
        Expect.notNull(srsWkt, "srsWkt");
        this.srsWkt = srsWkt;
    }

    public String getSrsOrigin() {
        return srsOrigin;
    }

    void setSrsOrigin(String origin) {
        Expect.notNull(origin, "origin");
        this.srsOrigin = origin;
    }

    public @NotNull List<String> getLoadedFlightPlans() {
        return loadedFlightPlans;
    }

    void setLoadedFlightPlans(List<String> flightPlans) {
        Expect.notNull(flightPlans, "flightPlans");
        loadedFlightPlans.clear();
        loadedFlightPlans.addAll(flightPlans);
    }

    public @NotNull List<String> getLoadedDataSets() {
        return loadedDataSets;
    }

    void setLoadedDataSets(List<String> dataSets) {
        Expect.notNull(dataSets, "dataSets");
        loadedDataSets.clear();
        loadedDataSets.addAll(dataSets);
    }

    public LatLon getStartCoordinates() {
        return startCoordinates;
    }

    void setStartCoordinates(LatLon startCoordinates) {
        this.startCoordinates = startCoordinates;
    }

    public @NotNull List<String> getFlightLogs() {
        return flightLogs;
    }

    void setFlightLogs(List<String> flightLogs) {
        Expect.notNull(flightLogs, "flightLogs");
        this.flightLogs.clear();
        this.flightLogs.addAll(flightLogs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionInfo that = (MissionInfo)o;
        return Objects.equals(name, that.name)
            && Objects.equals(lastModified, that.lastModified)
            && Objects.equals(folder, that.folder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lastModified, folder);
    }

    @Override
    public String toString() {
        return getName();
    }

}
