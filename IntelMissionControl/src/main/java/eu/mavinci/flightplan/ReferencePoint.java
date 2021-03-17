/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.mission.ReferencePointType;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IRecalculateable;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.geo.IPositionReferenced;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import org.asyncfx.concurrent.Dispatcher;

public class ReferencePoint extends Point
        implements IFlightplanPositionReferenced, IPositionReferenced, IRecalculateable {

    protected boolean isAuto = true;
    protected double
        altAboveR; // in meter, altitude above point R, if the instance is used as a reference point of the flightplan,
    // will be always 0 for the Fligh plane ref point(required by IFlightplanPositionReferenced
    // interface)
    protected double altitudeWgs84; // absolute height (independent of the IFlightplanPositionReferenced interface)
    protected double
        geoidSeparation; // geoid separatation at the point getLatLon(), updated together with altitudeWgs84
    protected double yaw;
    protected boolean isDefined;
    protected boolean hasYaw;
    protected boolean hasAlt;
    protected double elevation; // in m, elevation of the point above ground at this point
    private ReferencePointType refPointType = ReferencePointType.VERTEX;
    private int refPointOptionIndex;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof Point) {
            if (!super.equals(o)) return false;
            ReferencePoint origin = (ReferencePoint)o;
            return this.altAboveR == origin.altAboveR
                && this.altitudeWgs84 == origin.altitudeWgs84
                && this.geoidSeparation == origin.geoidSeparation
                && this.yaw == origin.yaw
                && this.isDefined == origin.isDefined
                && this.hasYaw == origin.hasYaw
                && this.hasAlt == origin.hasAlt
                && this.isAuto == origin.isAuto
                && this.elevation == origin.elevation;
        } else {
            return false;
        }
    }

    public ReferencePoint() {
        super(0, 0);
    }

    public ReferencePoint(double lat, double lon, double altAboveR, double yaw) {
        super(lat, lon);
        this.altAboveR = altAboveR;
        this.yaw = yaw;
        this.isDefined = true;
    }

    public ReferencePoint(
            IFlightplanContainer parent, double lat, double lon, double altAboveR, double yaw, boolean isDefined) {
        super(parent, lat, lon);
        this.altAboveR = altAboveR;
        this.yaw = yaw;
        this.isDefined = isDefined;
    }

    public ReferencePoint(IFlightplanContainer parent) {
        super(parent);
    }

    public ReferencePoint(ReferencePoint source) {
        super(source);
        this.altAboveR = source.altAboveR;
        this.altitudeWgs84 = source.altitudeWgs84;
        this.geoidSeparation = source.geoidSeparation;
        this.yaw = source.yaw;
        this.isDefined = source.isDefined;
        this.hasYaw = source.hasYaw;
        this.hasAlt = source.hasAlt;
        this.isAuto = source.isAuto;
        this.elevation = source.elevation;
    }

    public double getYaw() {
        return this.yaw;
    }

    public void setYaw(double yaw) {
        if (this.yaw == yaw) {
            return;
        }

        // originChanged(super.lat, super.lon, this.alt, yaw, this.isDefined);
        this.yaw = yaw;
        notifyStatementChanged();
    }

    public boolean setValues(ReferencePoint other) {
        return setValues(
            other.lat,
            other.lon,
            other.altAboveR,
            other.altitudeWgs84,
            other.geoidSeparation,
            other.yaw,
            other.isDefined,
            other.hasAlt,
            other.hasYaw,
            other.isAuto,
            other.elevation);
    }

    public boolean setValues(
            double lat,
            double lon,
            double alt,
            double altitudeWgs84,
            double geoidSeparation,
            double yaw,
            boolean isDefined,
            boolean hasAlt,
            boolean hasYaw,
            boolean isAuto,
            double elevation) {
        if (lat == this.lat
                && lon == this.lon
                && alt == this.altAboveR
                && altitudeWgs84 == this.altitudeWgs84
                && geoidSeparation == this.geoidSeparation
                && yaw == this.yaw
                && isDefined == this.isDefined
                && hasAlt == this.hasAlt
                && hasYaw == this.hasYaw
                && isAuto == this.isAuto
                && elevation == this.elevation) {
            return false;
        }

        // originChanged(lat, lon, alt, yaw, isDefined);
        this.lat = lat;
        this.lon = lon;
        this.yaw = yaw;
        this.altAboveR = alt;
        this.altitudeWgs84 = altitudeWgs84;
        this.geoidSeparation = geoidSeparation;
        this.isDefined = isDefined;
        this.hasYaw = hasYaw;
        this.hasAlt = hasAlt;
        this.isAuto = isAuto;
        this.elevation = elevation;
        notifyStatementChanged();
        return true;
    }

    public boolean isDefined() {
        return isDefined;
    }

    /** this method shifts the entire mission after while an change of the origin */

    /*
    private void originChanged(double lat, double lon, double alt, double yaw, boolean isDefined) {
        CFlightplan flightplan = getFlightplan();
        Ensure.notNull(flightplan, "flightplan");

        if (getParent() != flightplan) {
            if (getParent() instanceof IFlightplanChangeListener) {
                IFlightplanChangeListener o = (IFlightplanChangeListener)getParent();
                o.flightplanValuesChanged(this);
                return;
            }
        }

        // move to origin class
        if (lat == this.lat && lon == this.lon && alt == this.alt && yaw == this.yaw && isDefined == this.isDefined) {
            return;
        }

        // * should we preserve an altitude above the origin or above the ground ?
        final Matrix cm;
        final Matrix m;
        if (isDefined) {
            cm =
                (WWFactory.getGlobe()
                        .computeSurfaceOrientationAtPosition(
                            Angle.fromDegrees(getLat()), Angle.fromDegrees(getLon()), getAlt()))
                    .multiply(Matrix.fromRotationZ(Angle.fromDegrees(getYaw())))
                    .getInverse();
            m =
                (WWFactory.getGlobe()
                        .computeSurfaceOrientationAtPosition(Angle.fromDegrees(lat), Angle.fromDegrees(lon), alt))
                    .multiply(Matrix.fromRotationZ(Angle.fromDegrees(yaw)));
        } else {
            // cm = Matrix.IDENTITY;
            // m = (WWFactory.getGlobe().computeSurfaceOrientationAtPosition(Angle.fromDegrees(lat),
            // Angle.fromDegrees(lon),
            // alt)).multiply(Matrix.fromRotationY(Angle.fromDegrees(yaw))).getInverse();
            return;
        }

        try {
            flightplan.setMute(true);

            AFlightplanVisitor vis =
                new AFlightplanVisitor() {
                    @Override
                    public boolean visit(IFlightplanRelatedObject st) {
                        if (st instanceof Origin) {
                            // not overwriting myself, otherwise shifting is twice
                        } else if (st instanceof IFlightplanPositionReferenced) {
                            IFlightplanPositionReferenced wp = (IFlightplanPositionReferenced)st;
                            double lat = wp.getLat();
                            double lon = wp.getLon();
                            double alt = wp.getAltInMAboveFPRefPoint();

                            Vec4 local =
                                WWFactory.getGlobe()
                                    .computePointFromPosition(
                                        new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), alt))
                                    .transformBy4(cm);
                            Vec4 local2 = local.transformBy4(m);
                            Position global = WWFactory.getGlobe().computePositionFromPoint(local2);

                            wp.setLat(global.getLatitude().degrees);
                            wp.setLon(global.getLongitude().degrees);
                            // if(cFlightplan.isStartAltDefined()){
                            // cFlightplan.setStartAlt(global.getAltitude(), startGeoidSep);
                            // wp.setAltInMAboveFPRefPoint(global.getAltitude());
                            // }

                            // TODO FIXME IFlightplanLatLonReferenced
                            // starting procedure special stuff, see shift altitude action in action manager

                        } else if (st instanceof LandingPoint) {
                            LandingPoint wp = (LandingPoint)st;
                            double lat = wp.getLat();
                            double lon = wp.getLon();
                            double alt = wp.getAltInMAboveFPRefPoint();
                            // has second altitude

                            Vec4 local =
                                WWFactory.getGlobe()
                                    .computePointFromPosition(
                                        new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), alt))
                                    .transformBy4(cm);
                            Vec4 local2 = local.transformBy4(m);
                            Position global = WWFactory.getGlobe().computePositionFromPoint(local2);

                            wp.setLat(global.getLatitude().degrees);
                            wp.setLon(global.getLongitude().degrees);
                            // wp.setAltInMAboveFPRefPoint(global.getAltitude());
                        }
                        // TODO
                        return false;
                    }
                };

            flightplan.applyFpVisitor(vis, false);

            if (flightplan.isStartAltDefined()) { // reshift all altitudes!
                double startAltWgs84 = getAlt(); // plane.getAirplaneCache().getStartElevOverWGS84();
                double egmOffset =
                    EarthElevationModel.getEGM96Offset(
                        new LatLon(
                            Angle.fromDegrees(getLat()),
                            Angle.fromDegrees(getLon()))); // plane.getAirplaneCache().getStartElevEGMoffset();
                double toShift = flightplan.getRefPointAltWgs84WithElevation() - startAltWgs84;
                ShiftAllAltVisitor shiftAltVis = new ShiftAllAltVisitor(toShift);
                // shiftAltVis.startVisit(this);
                flightplan.applyFpVisitor(shiftAltVis, false);
                flightplan.setStartAlt(startAltWgs84, egmOffset);
            }

            // this.origin = origin;
        } finally {
            flightplan.setMute(false);
        }
    }*/

    protected static final String SEP = ", ";

    private static final String NAME = "Reference point: ";

    @Override
    public String toString() {
        return NAME + lat + SEP + lon + SEP + altAboveR + SEP + yaw + SEP + elevation;
    }

    public void fromString(String origin) {
        if (origin == null) {
            this.isDefined = false;
            return;
        }

        String[] parts = origin.split(NAME);
        if (parts.length > 1) {
            String[] parts2 = parts[1].split(SEP);
            if (parts2.length == 4) {
                this.lat = Double.parseDouble(parts2[0]);
                this.lon = Double.parseDouble(parts2[1]);
                this.altAboveR = Double.parseDouble(parts2[2]);
                this.yaw = Double.parseDouble(parts2[3]);
                this.elevation = Double.parseDouble(parts2[4]);
                this.hasYaw = true;
                this.hasAlt = true;
                this.isDefined = lat != 0 && lon != 0;
            }
        }
    }

    public void setDefined(boolean isDefined) {
        if (this.isDefined == isDefined) {
            return;
        }

        this.isDefined = isDefined;
        notifyStatementChanged();
    }

    public void setIsAuto(boolean isAuto) {
        if (this.isAuto == isAuto) {
            return;
        }

        this.isAuto = isAuto;
        notifyStatementChanged();
    }

    public boolean isAuto() {
        return isAuto;
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new ReferencePoint(this);
    }

    public boolean hasAlt() {
        return hasAlt;
    }

    public boolean hasYaw() {
        return hasYaw;
    }

    public void setHasAlt(boolean hasAlt) {
        if (this.hasAlt == hasAlt) {
            return;
        }

        this.hasAlt = hasAlt;
        notifyStatementChanged();
    }

    public void setHasYaw(boolean hasYaw) {
        if (this.hasYaw == hasYaw) {
            return;
        }

        this.hasYaw = hasYaw;
        notifyStatementChanged();
    }

    @Override
    protected void notifyStatementChanged() {
        super.notifyStatementChanged();
    }

    public void updateFromUAV(IAirplane plane) throws AirplaneCacheEmptyException {
        // TODO IMPLEMENT ME FOR PROPER IDL SUPPORT in IMC 1.1
        /*if (plane.isWriteable()) {
            try {
                double newStartAlt = plane.getAirplaneCache().getStartElevOverWGS84();
                double newEgmOffset = plane.getAirplaneCache().getStartElevEGMoffset();
                // use it somehow...
            } catch (AirplaneCacheEmptyException e) {
            }
        }*/
    }

    @Override
    public Flightplan getFlightplan() {
        return (Flightplan)super.getFlightplan();
    }

    @Override
    public void setAltInMAboveFPRefPoint(double altInM) {
        if (this.altAboveR == altInM) {
            return;
        }
        // originChanged(super.lat, super.lon, alt, this.yaw, this.isDefined);
        this.altAboveR = altInM;
        notifyStatementChanged();
    }

    @Override
    public double getAltInMAboveFPRefPoint() {
        return altAboveR;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
        Dispatcher.background().run(this::notifyStatementChanged);
    }

    // updating alt wgs84 at the current latlon
    public double updateAltitudeWgs84() {
        IElevationModel elevationModel = StaticInjector.getInstance(IElevationModel.class);
        IEgmModel egmModel = StaticInjector.getInstance(IEgmModel.class);

        LatLon latlon = getLatLon();
        altitudeWgs84 = elevationModel.getElevationAsGoodAsPossible(latlon);
        geoidSeparation = egmModel.getEGM96Offset(latlon);
        return this.altitudeWgs84;
    }

    @Override
    public Position getPosition() {
        return Position.fromDegrees(lat, lon, getAltInMAboveFPRefPoint());
    }

    public double getAltitudeWgs84() {
        return altitudeWgs84;
    }

    public double getGeoidSeparation() {
        return geoidSeparation;
    }

    public void resetAltitudes() {
        altitudeWgs84 = 0;
        geoidSeparation = 0;
    }

    public ReferencePointType getRefPointType() {
        return refPointType;
    }

    public void setRefPointType(ReferencePointType refPointType) {
        this.refPointType = refPointType;
    }

    public int getRefPointOptionIndex() {
        return refPointOptionIndex;
    }

    public void setRefPointOptionIndex(int refPointOptionIndex) {
        this.refPointOptionIndex = refPointOptionIndex;
    }

    @Override
    public void setLat(double lat) {
        if (this.lat != lat) {
            this.lat = lat;
            updateAltitudeWgs84();
            notifyStatementChanged();
        }
    }

    @Override
    public void setLon(double lon) {
        if (this.lon != lon) {
            this.lon = lon;
            updateAltitudeWgs84();
            notifyStatementChanged();
        }
    }

    @Override
    public void setLatLon(double lat, double lon) {
        if (this.lat != lat || this.lon != lon) {
            this.lat = lat;
            this.lon = lon;
            updateAltitudeWgs84();
            notifyStatementChanged();
        }
    }
    // without elevation
    public void setPosition(LatLon position) {
        if (position == null) {
            return;
        }

        double lat = position.latitude.degrees;
        double lon = position.longitude.degrees;
        if (this.lat != lat || this.lon != lon) {
            this.lat = lat;
            this.lon = lon;
            updateAltitudeWgs84();
            setDefined(true);
            notifyStatementChanged();
        } else {
            double previousAlt = altitudeWgs84;
            if (updateAltitudeWgs84() != previousAlt) {
                notifyStatementChanged();
            }
        }
    }

    @Override
    public boolean doSubRecalculationStage1() {
        if (isDefined) {
            updateAltitudeWgs84();
        }

        return true;
    }

    @Override
    public boolean doSubRecalculationStage2() {
        // do nothing
        return true;
    }
}
