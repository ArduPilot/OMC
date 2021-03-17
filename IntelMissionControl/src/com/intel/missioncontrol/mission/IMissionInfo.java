/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;

public interface IMissionInfo {

    File getFolder();

    Path getFolderPath();

    String getName();

    void setFolder(File file);

    void setName(String name);

    Date getLastModified();

    void setLastModified(Date date);

    double getMaxLatitude();

    void setMaxLatitude(double maxLat);

    double getMinLatitude();

    void setMinLatitude(double minLat);

    double getMaxLongitude();

    void setMaxLongitude(double maxLon);

    double getMinLongitude();

    void setMinLongitude(double minLon);

    OptionalDouble getMaxElev();

    void setMaxElev(OptionalDouble d);

    OptionalDouble getMinElev();

    void setMinElev(OptionalDouble d);

    String getSrsId();

    void setSrsId(String id);

    String getSrsName();

    void setSrsName(String name);

    String getSrsWkt();

    void setSrsWkt(String srsWkt);

    String getSrsOrigin();

    void setSrsOrigin(String origin);

    List<String> getLoadedFlightPlans();

    void setLoadedFlightPlans(List<String> flightPlans);

    List<String> getLoadedDataSets();

    void setLoadedDataSets(List<String> dataSets);

    Sector getSector();

    void copyValues(IMissionInfo missionInfo);

    LatLon getStartCoordinates();

    void setStartCoordinates(LatLon startCoordinates);

    List<String> getFlightLogs();

    void setFlightLogs(List<String> flightLogs);
}
