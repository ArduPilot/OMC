/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.flightplan.computation.FlightplanVertex;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Triangle;
import gov.nasa.worldwind.geom.Vec4;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MTriangle extends Triangle {

    public Vec4 normal;
    public Vector<MTriangle> neighbors = new Vector<MTriangle>(3);
    public final MinMaxPair minMaxX;
    public final MinMaxPair minMaxY;
    public final MinMaxPair minMaxZ;

    public MTriangle(Vec4 a, Vec4 b, Vec4 c) {
        super(a, b, c);
        Vec4 ab = b.subtract3(a);
        Vec4 ac = c.subtract3(a);
        normal = ac.cross3(ab).normalize3();
        minMaxX = new MinMaxPair(a.x, b.x, c.x);
        minMaxY = new MinMaxPair(a.y, b.y, c.y);
        minMaxZ = new MinMaxPair(a.z, b.z, c.z);
        //		System.out.println("minMaxX:"+minMaxX+ " minMaxY:"+minMaxY +" minMaxZ:"+minMaxZ + " a:"+a+" b:"+b+ " c:"+c);
    }

    public void setNeighbors(Vector<MTriangle> neighbors) {
        this.neighbors = neighbors;
    }

    public Vector<MTriangle> getNeighbors() {
        return neighbors;
    }

    public Vec4 getNormal() {
        return normal;
    }

    public MTriangle transform(Matrix transform) {
        return new MTriangle(
            getA().transformBy4(transform), getB().transformBy4(transform), getC().transformBy4(transform));
    }

    public static class ClosestPointResult {
        public Vec4 closestOnTriangle;
        public boolean onBorder;
    }

    public ClosestPointResult closestPoint(Vec4 vec) {
        Vec4 offset = getC();
        Vec4 vecRel = vec.subtract3(offset);
        Vec4 closestRelOnSurface = vecRel.subtract3(normal.multiply3(vecRel.dot3(normal)));

        Vec4 closestOnPane = offset.add3(closestRelOnSurface);
        ClosestPointResult result = new ClosestPointResult();
        if (contains(closestOnPane)) {
            result.closestOnTriangle = closestOnPane;
            return result;
        }

        result.onBorder = true;

        Vec4 vAB = Line.nearestPointOnSegment(getA(), getB(), closestOnPane);
        double distAB = vAB.distanceTo3(vec);

        Vec4 vAC = Line.nearestPointOnSegment(getA(), getC(), closestOnPane);
        double distAC = vAC.distanceTo3(vec);

        Vec4 vBC = Line.nearestPointOnSegment(getB(), getC(), closestOnPane);
        double distBC = vBC.distanceTo3(vec);

        if (distAB < distAC && distAB < distBC) {
            result.closestOnTriangle = vAB;
        } else if (distBC < distAC && distBC < distAB) {
            result.closestOnTriangle = vBC;
        } else {
            result.closestOnTriangle = vAC;
        }

        return result;
    }

    public Vector<FlightplanVertex> rasterTriangle(double stepXYZ) {
        Vector<FlightplanVertex> result = new Vector<>();
        Vec4 offset = getC();
        Vec4 va = getA().subtract3(offset);
        Vec4 vb = getB().subtract3(offset);
        double la = va.getLength3();
        double lb = vb.getLength3();
        Vec4 vea = va.divide3(la);
        Vec4 veb = vb.divide3(lb);
        Vec4 vh = va.subtract3(veb.multiply3(va.dot3(veb)));
        double h = vh.getLength3();
        double stepA = stepXYZ / h * la;
        double stepB = stepXYZ;

        int linesCount = (int)(h / stepXYZ);
        //		int oldVertCount = allPoints.size();
        double posA = (h % stepXYZ) * .5;
        for (int sa = 0; sa <= linesCount; sa++) {
            double l = (1 - posA / la) * lb;
            int dotCount = (int)(l / stepB);

            double posB = (l % stepB) * .5;
            for (int sb = 0; sb <= dotCount; sb++) {
                Vec4 v = offset.add3(vea.multiply3(posA).add3(veb.multiply3(posB)));
                FlightplanVertex vertex = new FlightplanVertex(v, null);
                vertex.setTriangle(this);
                result.add(vertex);
                //				System.out.println(posA+" "+ posB + " -> "+ v);
                posB += stepB;
            }

            posA += stepA;
        }

        return result;
    }

    public static class DistanceResult {
        double distance;
        Vec4 closestOnTriangle;
        Vec4 closestOnSegment;
    }

    /**
     * check the distance of this triangle to a line segment E<->F in case it is fore sure bigger then maxSearch, dont
     * go into details, just early report not close
     *
     * @param E
     * @param F
     * @param maxSearch
     * @return null in case they are not within maxSearch
     */
    public DistanceResult getDistanceToLinesegment(Vec4 E, Vec4 F, double maxSearch) {
        double distance2 = 0;
        double tmp;
        // check if the bounding boxes of the triangle and the line are even close enough!
        //		maxSearch/=10;
        double maxSearch2 = maxSearch * maxSearch;
        if (maxSearch != Double.POSITIVE_INFINITY) {
            MinMaxPair minMaxX = new MinMaxPair(E.x, F.x);
            tmp = minMaxX.getDistanceTo(this.minMaxX);
            if (tmp > maxSearch) {
                //				System.out.println("edge ok1");
                return null;
            }

            tmp = tmp * tmp;
            distance2 += tmp;
            MinMaxPair minMaxY = new MinMaxPair(E.y, F.y);
            tmp = minMaxY.getDistanceTo(this.minMaxY);
            //			System.out.println("B:"+tmp);
            if (tmp > maxSearch) {
                //				System.out.println("edge ok2");
                return null;
            }

            tmp = tmp * tmp;
            distance2 += tmp;
            if (distance2 > maxSearch2) {
                //				System.out.println("edge ok3");
                return null;
            }

            MinMaxPair minMaxZ = new MinMaxPair(E.z, F.z);
            tmp = minMaxZ.getDistanceTo(this.minMaxZ);
            //			System.out.println("C:"+tmp);
            if (tmp > maxSearch) {
                //				System.out.println("edge ok4");
                return null;
            }

            tmp = tmp * tmp;
            distance2 += tmp;
            if (distance2 > maxSearch2) {
                //				System.out.println("edge ok5");
                return null;
            }
            //			if (distance2!=0){
            //				System.out.println("NON NULL!!!!");
            //			}
        }

        DistanceResult result = new DistanceResult();

        ClosestPointResult resE = closestPoint(E);
        ClosestPointResult resF = closestPoint(E);

        // closest point is on the line segment resE.closestOnTriangle <-> resF.closestOnTriangle
        MathHelper.LineSegment shortest =
            MathHelper.shortestLineBetween(
                new MathHelper.LineSegment(resE.closestOnTriangle, resF.closestOnTriangle),
                new MathHelper.LineSegment(E, F),
                true);
        result.distance = shortest.first.distanceTo3(shortest.second);
        result.closestOnSegment = shortest.first; // TODO maybe this is switched??
        result.closestOnTriangle = shortest.second; // TODO maybe this is switched??
        return result;
    }

    public boolean isTriangleIntersectingOrInsideBox(
            double[] boxStart, double[] boxEnd, double sx, double sy, double sz) {
        if (minMaxX.min > boxEnd[0]) {
            return false;
        }

        if (minMaxX.max < boxStart[0]) {
            return false;
        }

        if (minMaxY.min > boxEnd[1]) {
            return false;
        }

        if (minMaxY.max < boxStart[1]) {
            return false;
        }

        if (minMaxZ.min > boxEnd[2]) {
            return false;
        }

        if (minMaxZ.max < boxStart[2]) {
            return false;
        }

        if ((getA().getX() < boxEnd[0] && getA().getX() > boxStart[0])
                && (getA().getY() < boxEnd[1] && getA().getY() > boxStart[1])
                && (getA().getZ() < boxEnd[2] && getA().getZ() > boxStart[2])) {
            return true;
        }

        if ((getB().getX() < boxEnd[0] && getB().getX() > boxStart[0])
                && (getB().getY() < boxEnd[1] && getB().getY() > boxStart[1])
                && (getB().getZ() < boxEnd[2] && getB().getZ() > boxStart[2])) {
            return true;
        }

        if ((getC().getX() < boxEnd[0] && getC().getX() > boxStart[0])
                && (getC().getY() < boxEnd[1] && getC().getY() > boxStart[1])
                && (getC().getZ() < boxEnd[2] && getC().getZ() > boxStart[2])) {
            return true;
        }

        boolean intersect = doesTriangleIntersectWithBox(boxStart, boxEnd, sx, sy, sz);
        if (intersect) {
            return true;
        }

        return false;
    }

    public boolean doesTriangleIntersectWithBox(double[] boxStart, double[] boxEnd, double sx, double sy, double sz) {

        // the box should be axis aligned
        double triangleMin, triangleMax;
        double boxMin, boxMax;

        // Test the box normals (x-, y- and z-axes)
        ArrayList<Vec4> boxNormals = new ArrayList<>();

        boxNormals.add(new Vec4(1, 0, 0, 1));
        boxNormals.add(new Vec4(0, 1, 0, 1));
        boxNormals.add(new Vec4(0, 0, 1, 1));

        ArrayList<Vec4> boxVertices = new ArrayList<>();

        boxVertices.add(Vec4.fromArray3(boxStart, 0));
        boxVertices.add(boxVertices.get(0).add3(boxNormals.get(0).multiply3(sx)));
        boxVertices.add(boxVertices.get(0).add3(boxNormals.get(1).multiply3(sy)));
        boxVertices.add(boxVertices.get(0).add3(boxNormals.get(2).multiply3(sz)));
        boxVertices.add(boxVertices.get(0).add3(boxNormals.get(0).multiply3(sx)).add3(boxNormals.get(1).multiply3(sy)));
        boxVertices.add(boxVertices.get(0).add3(boxNormals.get(0).multiply3(sx)).add3(boxNormals.get(2).multiply3(sz)));
        boxVertices.add(boxVertices.get(0).add3(boxNormals.get(1).multiply3(sy)).add3(boxNormals.get(2).multiply3(sz)));
        boxVertices.add(Vec4.fromArray3(boxEnd, 0));

        ArrayList<Vec4> triangleVertices = new ArrayList<>();
        triangleVertices.add(getA());
        triangleVertices.add(getB());
        triangleVertices.add(getC());

        // bStart = new double[] {boxStart.,max}

        for (int i = 0; i < 3; i++) {
            // Vec4 n = boxNormals.get(i);

            double minmax[] = project(triangleVertices, boxNormals.get(i));

            triangleMin = minmax[0];
            triangleMax = minmax[1];

            // System.out.println("min:" +triangleMin + "\tmax:" + triangleMax +
            // "\tboxStart" + boxStart[i] + "\tboxEnd" + boxEnd[i] );

            if (triangleMax < boxStart[i] || triangleMin > boxEnd[i]) {
                return false;
            }
        }

        // leave out this fast test, come back later

        // Test the triangle normal
        double triangleOffset = getNormal().dot3(getA());
        double minmax[] = project(boxVertices, getNormal());
        boxMin = minmax[0];
        boxMax = minmax[1];
        if (boxMax < triangleOffset || boxMin > triangleOffset) return false; // No intersection possible.

        /*
         * // Test the nine edge cross-products
         */
        ArrayList<Vec4> triangleEdges = new ArrayList<>();
        triangleEdges.add(getA().subtract3(getB()));
        triangleEdges.add(getB().subtract3(getC()));
        triangleEdges.add(getC().subtract3(getA()));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // The box normals are the same as it's edge tangents
                Vec4 axis = triangleEdges.get(i).cross3(boxNormals.get(j));
                minmax = project(boxVertices, axis);
                boxMin = minmax[0];
                boxMax = minmax[1];

                minmax = project(triangleVertices, axis);
                triangleMin = minmax[0];
                triangleMax = minmax[1];
                if (boxMax < triangleMin || boxMin > triangleMax) return false; // No intersection possible
            }
        }

        return true;
    }

    public double[] project(List<Vec4> points, Vec4 axis) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (Vec4 p : points) {
            double val = axis.dot3(p);
            if (val < min) min = val;
            if (val > max) max = val;
        }

        return new double[] {min, max};
    }
}
