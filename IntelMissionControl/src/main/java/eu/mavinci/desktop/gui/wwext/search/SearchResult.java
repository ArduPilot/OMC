/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.search;

import eu.mavinci.core.obfuscation.IKeepAll;
import eu.mavinci.geo.ILatLonReferenced;
import eu.mavinci.geo.ISectorReferenced;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.poi.PointOfInterest;
import eu.mavinci.geo.ILatLonReferenced;
import eu.mavinci.geo.ISectorReferenced;

import java.util.OptionalDouble;

public class SearchResult implements ILatLonReferenced, ISectorReferenced, IKeepAll {

    private String locationName;

    private LatLon latlon;
    private Double searchPrio;
    private Sector sector;
    private OptionalDouble maxElev = OptionalDouble.empty();
    private OptionalDouble minElev = OptionalDouble.empty();

    /** @return the latitude */
    public double getLatitude() {
        return latlon.getLatitude().degrees;
    }

    /** @return the longitude */
    public double getLongitude() {
        return latlon.getLongitude().degrees;
    }

    public Double getSearchPrio() {
        return searchPrio;
    }

    public SearchResult(PointOfInterest poi) {
        latlon = poi.getLatlon();
        locationName = poi.toString();
        searchPrio = Double.valueOf(poi.getValue(CombinedGazetteer.SEARCH_PRIO).toString());
    }

    public SearchResult(LatLon pos, String name) {
        latlon = pos;
        locationName = name;
        searchPrio = 0.;
    }

    public SearchResult(double lat, double lon, String name) {
        latlon = new LatLon(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
        locationName = name;
        searchPrio = 0.;
    }

    public SearchResult(PointOfInterest poi, Sector sector) {
        latlon = sector.getCentroid();// poi.getLatlon();
        locationName = poi.toString();
        searchPrio = Double.valueOf(poi.getValue(CombinedGazetteer.SEARCH_PRIO).toString());
        this.sector = sector;
    }

    /** used in the gui to show the name */
    @Override
    public String toString() {
        return locationName;
    }

    /** */
    @Override
    public LatLon getLatLon() {
        return latlon;
    }

    /** @return Human readable placename */
    public String getLocationName() {
        return locationName;
    }

    public Sector getSector() {
        return sector;
    }

    public OptionalDouble getMaxElev() {
        return maxElev;
    }

    public OptionalDouble getMinElev() {
        return minElev;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchResult that = (SearchResult)o;

        if (locationName != null ? !locationName.equals(that.locationName) : that.locationName != null) return false;
        if (latlon != null ? !latlon.equals(that.latlon) : that.latlon != null) return false;
        return searchPrio != null ? searchPrio.equals(that.searchPrio) : that.searchPrio == null;
    }

    @Override
    public int hashCode() {
        int result = locationName != null ? locationName.hashCode() : 0;
        result = 31 * result + (latlon != null ? latlon.hashCode() : 0);
        result = 31 * result + (searchPrio != null ? searchPrio.hashCode() : 0);
        return result;
    }
}
