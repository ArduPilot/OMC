/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.flightplan.ITransformationProvider;
import eu.mavinci.flightplan.computation.objectSurface.MTriangle;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Vector;

public class FlightplanVertex {

    private Orientation orientation;

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public boolean flag;//TODO remove it, only for debugging


    /** position in the flight path */
    public int idx;

    /** Center point of target (Camera points towards this point) */
    private Vec4 centerPoint = null;

    /** Planned drone position */
    private Vec4 wayPoint = null;

    /** "fence" point that should not be moved away in x-y dir */
    private boolean fixed = false;

    private Angle centerYawCourse;

    public Angle getCenterYawCourse() {
        return centerYawCourse;
    }

    public void setCenterYawCourse(Angle centerYawCourse) {
        this.centerYawCourse = centerYawCourse;
    }

    public double getFaceNormaleAngleTo(double stepXYZ, FlightplanVertex other) {
        Vec4 faceNormal = getFaceNormal(centerPoint, stepXYZ);
        Ensure.notNull(faceNormal, "faceNormal");
        Vec4 otherFace = other.getFaceNormal(centerPoint, stepXYZ);
        Ensure.notNull(otherFace, "otherFace");
        return Math.acos(faceNormal.dot3(otherFace));
    }

    public Vec4 getFaceNormal() {
        return triangle == null ? null : triangle.normal;
    }

    public Vec4 getFaceNormal(Vec4 atPoint, double stepXYZ) {
        if (triangle == null) return null;
        Vec4 normal = triangle.normal;
        for (MTriangle neighbor : triangle.getNeighbors()) {
            MTriangle.ClosestPointResult closest = neighbor.closestPoint(atPoint);
            double distance = closest.closestOnTriangle.distanceTo3(atPoint);
            double scale = 1 - distance / (stepXYZ);
            if (scale < 0) continue;
            if (scale > 1) scale = 1;
            normal = normal.add3(neighbor.normal.multiply3(scale));
        }

        return normal.normalize3();
    }

    public double getFaceNormaleAngleToOrZero(double stepXYZ, FlightplanVertex other) {
        return triangle == null ? 0 : getFaceNormaleAngleTo(stepXYZ, other);
    }

    //	public Angle getCenterYawCourse() {
    //		return centerYawCourse;
    //	}
    //
    //	public void setCenterYawCourse(Angle centerYawCourse) {
    //		this.centerYawCourse = centerYawCourse;
    //	}

    Vector<Vec4> neighbors;

    public void setNeigbourPointsOnImage(Vector<Vec4> neighbors) {
        this.neighbors = neighbors;
    }

    public Vector<Vec4> getNeigbourPointsOnImage() {
        return neighbors;
    }

    MTriangle triangle;

    public void setTriangle(MTriangle triangle) {
        this.triangle = triangle;
    }

    public MTriangle getTriangle() {
        return triangle;
    }

    public MinMaxPair getImageDistance() {
        if (neighbors == null) return null;
        MinMaxPair imageDistance = new MinMaxPair();
        Vec4 normal = getNormal();
        for (Vec4 v : neighbors) {
            Vec4 dV = v.subtract3(wayPoint);
            imageDistance.update(dV.dot3(normal));
        }

        return imageDistance;
    }

    /** ITransformationProvider to enable transformation from Vec4 to Position */
    private ITransformationProvider trafo = null;

    public FlightplanVertex(Vec4 centerPoint, Vec4 wayPoint, ITransformationProvider trafo) {
        this(centerPoint, wayPoint);
        this.trafo = trafo;
    }

    public FlightplanVertex(Vec4 centerPoint, Vec4 wayPoint) {
        this.centerPoint = centerPoint;
        this.wayPoint = wayPoint;
    }

    public FlightplanVertex(Vec4 centerPoint, Vec4 wayPoint, boolean fixed) {
        this(centerPoint, wayPoint);
        this.fixed = fixed;
    }

    public Vec4 getCenterPoint() {
        return this.centerPoint;
    }

    public Vec4 getWayPoint() {
        return this.wayPoint;
    }

    public Vec4 getCameraPointingLine(){
        return getWayPoint().subtract3(getCenterPoint());
    }

    public void setCenterPoint(Vec4 centerPoint) {
        this.centerPoint = centerPoint;
    }

    public void setWayPoint(Vec4 wayPoint) {
        this.wayPoint = wayPoint;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean isFixed) {
        this.fixed = isFixed;
    }

    public Position getCenterPointPosition() {
        if (trafo == null) {
            throw new IllegalStateException("ITransformationProvider trafo must be set for Position transformation");
        }

        return trafo.transformToGlobe(this.centerPoint);
    }

    public Position getWayPointPosition() {
        if (trafo == null) {
            throw new IllegalStateException("ITransformationProvider trafo must be set for Position transformation");
        }

        return trafo.transformToGlobe(this.wayPoint);
    }

    public FlightplanVertex clone() {
        return new FlightplanVertex(centerPoint, wayPoint, fixed);
    }

    /** Normal vector, pointing from wayPoint to centerPoint. */
    public Vec4 getNormal() {
        return getWayPoint().subtract3(getCenterPoint()).normalize3();
    }

    public String toString() {
        String res = "centerPoint=(" + centerPoint.x + ", " + centerPoint.y + ", " + centerPoint.z + ")";
        if (wayPoint != null) {
            res += ", wayPoint=(" + wayPoint.x + ", " + wayPoint.y + ", " + wayPoint.z + ")";
        }

        if (fixed) {
            res += ", fixed";
        }

        return res;
    }

    public double distanceAtCenter(FlightplanVertex curPoint) {
        return Math.sqrt(distanceAtCenterSquared(curPoint));
    }

    public double distanceAtWaypoint(FlightplanVertex curPoint) {
        return Math.sqrt(distanceAtWaypointSquared(curPoint));
    }

    public double distanceAtCenterSquared(FlightplanVertex curPoint) {
        return centerPoint.distanceToSquared3(curPoint.centerPoint);
    }

    public double distanceAtWaypointSquared(FlightplanVertex curPoint) {
        return wayPoint.distanceToSquared3(curPoint.wayPoint);
    }

    /** get the camera orientation within mavinci convention */
    public Orientation getRollPitchYaw(
            Vec4 flyingDirection, Angle yaw, Orientation lastOrientation, ITransformationProvider trafo) {
        return PointShiftingHelper.yawPitchRollFromYawAndNormal(
            flyingDirection, yaw, lastOrientation, trafo.transformToGlobalNorthing(getNormal()));
    }

    /** get the camera orientation within mavinci convention */
    /*public Orientation getRollPitchYaw(ITransformationProvider trafo) {
        return PointShiftingHelper.yawPitchRollFromYawAndNormal(centerYawCourse, trafo.transformToGlobalNorthing(getNormal()));
    }*/

    /** get the camera orientation within mavinci convention */
    public Orientation getRollPitchYawInArbitratyFrame(
            Vec4 flyingDirection, Angle lineYaw, Orientation lastOrientation) {
        return PointShiftingHelper.yawPitchRollFromYawAndNormal(flyingDirection, lineYaw, lastOrientation, getNormal());
    }
}
