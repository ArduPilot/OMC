/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

public class GdalGeoTransformFactory implements ITransformFactory, IConformalTransformFactory {

    private final SpatialReference wgs84;

    public GdalGeoTransformFactory() {
        wgs84 = new SpatialReference();
        wgs84.SetWellKnownGeogCS("WGS84");
    }

    @Override
    public ITransform<ProjectedPosition> createFromReference(LatLon reference) {
        SpatialReference ref = new SpatialReference();
        ref.SetWellKnownGeogCS("WGS84");
        //same result more or less as SetOS(...)
        ref.SetTM(reference.getLatitude(), reference.getLongitude(), 1, 0, 0);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            ProjectedPosition createPosition(double m1, double m2, double m3) {
                return new ProjectedPosition(m1, m2, m3);
            }

            @Override
            public Position transform(ProjectedPosition vec) {
                double[] coords = coordTransformBack.TransformPoint(vec.x, vec.y, vec.z);
                return Position.fromDegrees(coords[1], coords[0], coords[2]);
            }

            @Override
            public ProjectedPosition transform(Position pos) {
                double[] coords =
                        coordTransform.TransformPoint(
                                pos.getLongitude(), pos.getLatitude(), pos.getElevation());
                return createPosition(coords[0], coords[1], coords[2]);
            }
        };
    }

    @Override
    public ITransform<UndefinedPosition> createFromWkt(String wkt) {
        return new Wgs84TransformBase<>(wgs84, new SpatialReference(wkt)) {
            @Override
            UndefinedPosition createPosition(double m1, double m2, double m3) {
                return new UndefinedPosition(m1, m2, m3);
            }

            @Override
            public Position transform(UndefinedPosition vec) {
                return transform(vec.m1, vec.m2, vec.m3);
            }
        };
    }

    @Override
    public ITransform<ProjectedPosition2D> createProjected2DFromWkt(String wkt) {
        return new Wgs84TransformBase<>(wgs84, new SpatialReference(wkt)) {
            @Override
            ProjectedPosition2D createPosition(double m1, double m2, double m3) {
                return new ProjectedPosition2D(m1, m2);
            }

            @Override
            public Position transform(ProjectedPosition2D vec) {
                return transform(vec.x, vec.y, 0);
            }
        };
    }

    @Override
    public ITransform<ProjectedPosition> createProjectedFromWkt(String wkt) {
        return new Wgs84TransformBase<>(wgs84, new SpatialReference(wkt)) {
            @Override
            ProjectedPosition createPosition(double m1, double m2, double m3) {
                return new ProjectedPosition(m1, m2, m3);
            }

            @Override
            public Position transform(ProjectedPosition vec) {
                return transform(vec.x, vec.y, vec.z);
            }
        };
    }

    @Override
    public ITransform<GeographicLatLon> createGeographic2DFromWkt(String wkt) {
        return new Wgs84TransformBase<>(wgs84, new SpatialReference(wkt)) {
            @Override
            GeographicLatLon createPosition(double m1, double m2, double m3) {
                return new GeographicLatLon(m1, m2);
            }

            @Override
            public Position transform(GeographicLatLon vec) {
                return transform(vec.latitude, vec.longitude, 0);
            }
        };
    }

    @Override
    public ITransform<GeographicPosition> createGeographicFromWkt(String wkt) {
        return new Wgs84TransformBase<>(wgs84, new SpatialReference(wkt)) {
            @Override
            GeographicPosition createPosition(double m1, double m2, double m3) {
                return new GeographicPosition(m1, m2, m3);
            }

            @Override
            public Position transform(GeographicPosition vec) {
                return transform(vec.latitude, vec.longitude, vec.elevation);
            }
        };
    }

    @Override
    public ITransform<GeocentricPosition> createGeocentricFromWkt(String wkt) {
        return new Wgs84TransformBase<>(wgs84, new SpatialReference(wkt)) {
            @Override
            GeocentricPosition createPosition(double m1, double m2, double m3) {
                return new GeocentricPosition(m1, m2, m3);
            }

            @Override
            public Position transform(GeocentricPosition vec) {
                return transform(vec.x, vec.y, vec.z);
            }
        };
    }

    @Override
    public ITransform<UndefinedPosition> createFromEpsg(int epsg) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromEPSG(epsg);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            UndefinedPosition createPosition(double m1, double m2, double m3) {
                return new UndefinedPosition(m1, m2, m3);
            }

            @Override
            public Position transform(UndefinedPosition vec) {
                return transform(vec.m1, vec.m2, vec.m2);
            }
        };
    }

    @Override
    public ITransform<ProjectedPosition2D> createProjected2DFromEpsg(int epsg) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromEPSG(epsg);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            ProjectedPosition2D createPosition(double m1, double m2, double m3) {
                return new ProjectedPosition2D(m1, m2);
            }

            @Override
            public Position transform(ProjectedPosition2D vec) {
                return transform(vec.x, vec.y, 0);
            }
        };
    }

    @Override
    public ITransform<ProjectedPosition> createProjectedFromEpsg(int epsg) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromEPSG(epsg);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            ProjectedPosition createPosition(double m1, double m2, double m3) {
                return new ProjectedPosition(m1, m2, m3);
            }

            @Override
            public Position transform(ProjectedPosition vec) {
                return transform(vec.x, vec.y, vec.z);
            }
        };
    }

    @Override
    public ITransform<GeographicLatLon> createGeographic2DFromEpsg(int epsg) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromEPSG(epsg);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            GeographicLatLon createPosition(double m1, double m2, double m3) {
                return new GeographicLatLon(m1, m2);
            }

            @Override
            public Position transform(GeographicLatLon vec) {
                return transform(vec.latitude, vec.longitude, 0);
            }
        };
    }

    @Override
    public ITransform<GeographicPosition> createGeographicFromEpsg(int epsg) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromEPSG(epsg);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            GeographicPosition createPosition(double m1, double m2, double m3) {
                return new GeographicPosition(m1, m2, m3);
            }

            @Override
            public Position transform(GeographicPosition vec) {
                return transform(vec.latitude, vec.longitude, vec.elevation);
            }
        };
    }

    @Override
    public ITransform<GeocentricPosition> createGeocentricFromEpsg(int epsg) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromEPSG(epsg);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            GeocentricPosition createPosition(double m1, double m2, double m3) {
                return new GeocentricPosition(m1, m2, m3);
            }

            @Override
            public Position transform(GeocentricPosition vec) {
                return transform(vec.x, vec.y, vec.z);
            }
        };
    }

    @Override
    public ITransform<UndefinedPosition> createFromProj4(String proj4) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromProj4(proj4);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            UndefinedPosition createPosition(double m1, double m2, double m3) {
                return new UndefinedPosition(m1, m2, m3);
            }

            @Override
            public Position transform(UndefinedPosition vec) {
                return transform(vec.m1, vec.m2, vec.m3);
            }
        };
    }

    @Override
    public ITransform<ProjectedPosition2D> createProjected2DFromProj4(String proj4) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromProj4(proj4);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            ProjectedPosition2D createPosition(double m1, double m2, double m3) {
                return new ProjectedPosition2D(m1, m2);
            }

            @Override
            public Position transform(ProjectedPosition2D vec) {
                return transform(vec.x, vec.y, 0);
            }
        };
    }

    @Override
    public ITransform<ProjectedPosition> createProjectedFromProj4(String proj4) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromProj4(proj4);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            ProjectedPosition createPosition(double m1, double m2, double m3) {
                return new ProjectedPosition(m1, m2, m3);
            }

            @Override
            public Position transform(ProjectedPosition vec) {
                return transform(vec.x, vec.y, vec.z);
            }
        };
    }

    @Override
    public ITransform<GeographicLatLon> createGeographic2DFromProj4(String proj4) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromProj4(proj4);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            GeographicLatLon createPosition(double m1, double m2, double m3) {
                return new GeographicLatLon(m1, m2);
            }

            @Override
            public Position transform(GeographicLatLon vec) {
                return transform(vec.latitude, vec.longitude, 0);
            }
        };
    }

    @Override
    public ITransform<GeographicPosition> createGeographicFromProj4(String proj4) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromProj4(proj4);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            GeographicPosition createPosition(double m1, double m2, double m3) {
                return new GeographicPosition(m1, m2, m3);
            }

            @Override
            public Position transform(GeographicPosition vec) {
                return transform(vec.latitude, vec.longitude, vec.elevation);
            }
        };
    }

    @Override
    public ITransform<GeocentricPosition> createGeocentricFromProj4(String proj4) {
        SpatialReference ref = new SpatialReference();
        ref.ImportFromProj4(proj4);

        return new Wgs84TransformBase<>(wgs84, ref) {
            @Override
            GeocentricPosition createPosition(double m1, double m2, double m3) {
                return new GeocentricPosition(m1, m2, m3);
            }

            @Override
            public Position transform(GeocentricPosition vec) {
                return transform(vec.x, vec.y, vec.z);
            }
        };
    }

    private abstract static class Wgs84TransformBase<T> implements ITransform<T> {
        protected final CoordinateTransformation coordTransform;
        protected final CoordinateTransformation coordTransformBack;

        Wgs84TransformBase(SpatialReference wgs84, SpatialReference target) {
            coordTransform = CoordinateTransformation.CreateCoordinateTransformation(wgs84, target);
            coordTransformBack = CoordinateTransformation.CreateCoordinateTransformation(target, wgs84);
        }

        @Override
        public T transform(Position pos) {
            double[] coords = coordTransform.TransformPoint(pos.getLatitude(), pos.getLongitude(), pos.getElevation());
            return createPosition(coords[0], coords[1], coords[2]);
        }

        Position transform(double x, double y, double z) {
            double[] coords = coordTransformBack.TransformPoint(x, y, z);
            return Position.fromDegrees(coords[0], coords[1], coords[2]);
        }

        abstract T createPosition(double m1, double m2, double m3);
    }

}
