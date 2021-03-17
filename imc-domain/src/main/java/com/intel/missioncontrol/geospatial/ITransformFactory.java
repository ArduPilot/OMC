/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

public interface ITransformFactory {

    ITransform<UndefinedPosition> createFromWkt(String wkt);

    ITransform<GeocentricPosition> createGeocentricFromWkt(String wkt);

    ITransform<ProjectedPosition2D> createProjected2DFromWkt(String wkt);

    ITransform<ProjectedPosition> createProjectedFromWkt(String wkt);

    ITransform<GeographicLatLon> createGeographic2DFromWkt(String wkt);

    ITransform<GeographicPosition> createGeographicFromWkt(String wkt);

    ITransform<UndefinedPosition> createFromEpsg(int epsg);

    ITransform<ProjectedPosition2D> createProjected2DFromEpsg(int epsg);

    ITransform<ProjectedPosition> createProjectedFromEpsg(int epsg);

    ITransform<GeographicLatLon> createGeographic2DFromEpsg(int epsg);

    ITransform<GeographicPosition> createGeographicFromEpsg(int epsg);

    ITransform<GeocentricPosition> createGeocentricFromEpsg(int epsg);

    ITransform<UndefinedPosition> createFromProj4(String proj4);

    ITransform<GeocentricPosition> createGeocentricFromProj4(String proj4);

    ITransform<ProjectedPosition2D> createProjected2DFromProj4(String proj4);

    ITransform<ProjectedPosition> createProjectedFromProj4(String proj4);

    ITransform<GeographicLatLon> createGeographic2DFromProj4(String proj4);

    ITransform<GeographicPosition> createGeographicFromProj4(String proj4);

}
