/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Vector;

public class CorridorHelper {

    Vector<Vec4> pathVec;
    Vector<Vec4> diffsNormalRot;

    Vector<Vec4> normals;
    Vector<Vec4> origins;

    boolean isValid;
    double centerLength;
    boolean looped;

    public CorridorHelper(Vector<Vec4> path, boolean looped) {
        this.looped = looped;
        setPath(path);
    }

    public CorridorHelper(Vector<Vec4> path) {
        this(path, false);
    }

    /**
     * derive a new corridor helper, which uses a segment of another one
     *
     * @param corrHelper
     * @param start in meters on the center path from its start from the parent CorridorHelper
     * @param end in meters on the center path from its start from the parent CorridorHelper
     */
    public CorridorHelper(CorridorHelper corrHelper, double start, double end) {
        @SuppressWarnings("unchecked")
        Vector<Vec4> corners = (Vector<Vec4>)corrHelper.pathVec.clone(); // flat copy is ok
        if (corners.size() < 2) {
            setPath(corners);
            computeHelper();
            return;
        }

        // System.out.println("init corners:" + corners.size() + corrHelper.isValidMinGroundDistanceOK());
        // System.out.println("init derived CorridorHelper:" + start + " \t "+ " \t "+end+" \t
        // "+corrHelper.getCenterLength());
        double len = corrHelper.getCenterLength();
        end = len - end; // transform into distance to end

        if (end + start >= len) {
            // non valid!
            return;
        }

        // System.out.println("from end:" + end);
        if (end > 0) {
            Vec4 last = corners.get(corners.size() - 1);

            for (int i = corners.size() - 2; i >= 0; i--) {
                Vec4 c = corners.get(i);
                double runLength = c.distanceTo3(last);
                if (runLength >= end) {
                    last = last.add3(c.subtract3(last).multiply3(end / runLength));
                    corners.set(i + 1, last);
                    while (corners.size() > i + 2) {
                        corners.remove(i + 2);
                    }

                    break;
                }

                end -= runLength;
                last = c;
            }
        }

        // System.out.println("haldWay corners:" + corners.size());

        if (start > 0 && corners.size() > 1) {
            // System.out.println("fromStart:"+start);
            Vec4 last = corners.get(0);
            for (int i = 1; i < corners.size(); i++) {
                Vec4 c = corners.get(i);
                double runLength = c.distanceTo3(last);
                if (runLength >= start) {
                    // System.out.println("detected point to split");
                    last = last.add3(c.subtract3(last).multiply3(start / runLength));
                    corners.set(i - 1, last);
                    for (int k = 0; k < i - 1; k++) {
                        corners.remove(0);
                    }

                    break;
                }

                start -= runLength;
                last = c;
            }
        }

        // System.out.println("final corners:" + corners.size());

        setPath(corners);
        computeHelper();
    }

    public void setPath(Vector<Vec4> path) {
        this.pathVec = path;
        computeHelper();
    }

    static final Matrix rotRight = Matrix.fromRotationZ(Angle.POS90);

    private void computeHelper() {
        if (pathVec == null || pathVec.size() < 2) {
            return;
        }

        diffsNormalRot = new Vector<Vec4>(pathVec.size() - 1);
        removeLoops(pathVec);
        if (looped) {
            Vec4 last = pathVec.lastElement();
            Vec4 first = pathVec.firstElement();
            pathVec.add(first);
            pathVec.insertElementAt(last, 0);
        }

        Vec4 last = pathVec.get(0);
        for (int i = 1; i != pathVec.size(); i++) {
            Vec4 next = pathVec.get(i);
            this.centerLength += next.distanceTo2(last);
            // System.out.println("len:"+this.centerLength);

            Vec4 diff = next.subtract3(last).transformBy3(rotRight).normalize3();
            diffsNormalRot.add(diff);
            last = next;
        }
        // System.out.println("diffsNormalRot:"+diffsNormalRot);

        normals = new Vector<Vec4>(pathVec.size());
        origins = new Vector<Vec4>(pathVec.size());
        last = diffsNormalRot.get(0);
        normals.add(last);
        origins.add(pathVec.get(0));
        for (int i = 1; i != diffsNormalRot.size(); i++) {
            Vec4 next = diffsNormalRot.get(i);

            Vec4 normalCenter = last.add3(next).normalize3();
            double cos2 = last.dot3(normalCenter);
            Vec4 normal1 = last.add3(normalCenter);
            Vec4 normal2 = next.add3(normalCenter);
            double len = Math.sqrt(2 / ((cos2 + 1) * normal1.getLengthSquared3()));
            normal1 = normal1.multiply3(len);
            normal2 = normal2.multiply3(len);
            normals.add(normal1);
            normals.add(normal2);
            origins.add(pathVec.get(i));
            origins.add(pathVec.get(i));

            last = next;
        }

        normals.add(last);
        origins.add(pathVec.get(pathVec.size() - 1));

        isValid = true;
        // System.out.println("normals:"+normals);
    }

    public void removeLoops(Vector<Vec4> pathVec) {
        // if (true) return;
        // testing from the left starting each segment if it intersects.
        // prefer intersections towards the end of the path, so test them starting from the end
        // System.out.println("remove loops from vec of size:"+pathVec.size());
        Vec4 a1 = pathVec.get(0);
        for (int i = 1; i < pathVec.size() - 2; i++) {
            Vec4 a2 = pathVec.get(i);
            Vec4 b2 = pathVec.get(pathVec.size() - 1);
            for (int k = pathVec.size() - 2; k > i; k--) {
                Vec4 b1 = pathVec.get(k);
                Vec4 intersection = intersect(a1, a2, b1, b2);

                if (intersection != null) {
                    // System.out.println("i:" + i + " k:"+k);
                    // System.out.println("inters:"+intersection);
                    // if an intersection was found, remove the loop starting end ending in this intersection
                    pathVec.set(i, intersection);
                    int iNeu = i + 1;
                    for (int m = k + 1; m != pathVec.size(); m++) {
                        pathVec.set(iNeu, pathVec.get(m));
                        iNeu++;
                    }

                    while (pathVec.size() > iNeu) {
                        pathVec.remove(iNeu);
                    }

                    a2 = intersection;
                    break; // continue to search for further loops in the remaining tail of this plan
                }

                b2 = b1;
            }

            a1 = a2;
        }
    }

    /**
     * computes the intersection point of two 2-dim line segments. if non exists, return null
     *
     * @param a1
     * @param a2
     * @param b1
     * @param b2
     * @return
     */
    public static Vec4 intersect(Vec4 a1, Vec4 a2, Vec4 b1, Vec4 b2) {
        // a1+t(a2-a1)=b1+s(b2-b1)
        // a1-b1=-t(a2-a1)+s(b2-b1)
        // dab=-t da + sdb
        // dab=[-da,db](t,s)
        // [-da,db]^-1 dab=(t,s)

        Vec4 da = a2.subtract3(a1);
        Vec4 db = b2.subtract3(b1);
        Vec4 dab = a1.subtract3(b1);
        // System.out.println("da:"+da);
        // System.out.println("db:"+db);
        // System.out.println("dab:"+dab);

        double det = -da.x * db.y + da.y * db.x;
        // System.out.println("det:"+det);
        if (det == 0) {
            det = -dab.x * db.y + dab.y * db.x;
            // System.out.println("det2:"+det);
            if (det != 0) {
                return null; //
            }

            if (Math.abs(db.x) > Math.abs(db.y)) {
                // System.out.println("Xcase");
                MinMaxPair minMaxA = new MinMaxPair(dab.x, dab.x + da.x);
                MinMaxPair minMaxB = new MinMaxPair(0, db.x);
                MinMaxPair intersection = minMaxA.intersect(minMaxB);
                // System.out.println("minMaxAB" + minMaxA + " " + minMaxB + " -> " + intersection);
                if (intersection == null) {
                    return null;
                }

                double dbx = intersection.mean();
                return new Vec4(b1.x + dbx, b1.y + db.y * dbx / db.x);
            } else if (db.y == 0) { // also db.x==0
                // System.out.println("Ycase-null");
                MinMaxPair minMaxA = new MinMaxPair(dab.y, dab.y + da.y);
                if (!minMaxA.contains(0)) {
                    return null;
                }

                return b1;
            } else {
                // System.out.println("Ycase");
                MinMaxPair minMaxA = new MinMaxPair(dab.y, dab.y + da.y);
                MinMaxPair minMaxB = new MinMaxPair(0, db.y);
                MinMaxPair intersection = minMaxA.intersect(minMaxB);
                // System.out.println("minMaxAB" + minMaxA + " " + minMaxB + " -> " + intersection);
                if (intersection == null) {
                    return null;
                }

                double dby = intersection.mean();
                return new Vec4(b1.x + db.x * dby / db.y, b1.y + dby);
            }
        }

        det = 1 / det;
        Matrix minv = new Matrix(det * db.y, det * -db.x, 0, 0, det * da.y, det * -da.x, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
        Vec4 ts = dab.transformBy3(minv);
        // System.out.println("ts:"+ts);
        if (ts.x > 1 || ts.x < 0) {
            return null;
        }

        if (ts.y > 1 || ts.y < 0) {
            return null;
        }

        return b1.add3(db.multiply3(ts.y));
    }

    public boolean isValid() {
        return isValid;
    }

    public static final double MINIMAL_POSSIBLE_SHIFT = 0.01;

    public Vector<Vec4> getShifted(double shiftMeters) {
        Ensure.notNull(pathVec, "pathVec");
        Ensure.notNull(normals, "normals");
        Ensure.notNull(origins, "origins");

        if (Math.abs(shiftMeters) < MINIMAL_POSSIBLE_SHIFT) {
            return pathVec;
        }

        Vector<Vec4> outVec = new Vector<Vec4>(origins.size());

        for (int i = 0; i != origins.size(); i++) {
            Vec4 origin = origins.get(i);
            Vec4 direction = normals.get(i);
            Vec4 vec = origin.add3(direction.multiply3(shiftMeters));
            outVec.add(vec);
        }

        if (looped) {
            outVec.remove(0);
            outVec.remove(outVec.size() - 1);
        }

        removeLoops(outVec);
        if (looped) {
            outVec.add(outVec.firstElement());
        }
        // ring shift two steps and remove loops again. without this inner corners on the last segment of the loop are
        // not shorted
        if (looped) {
            // unclose it
            outVec.remove(outVec.size() - 1);

            // 4x ring shift (3 seems to be minimal need, but better safe make one more)
            outVec.add(outVec.remove(0));
            outVec.add(outVec.remove(0));
            outVec.add(outVec.remove(0));
            outVec.add(outVec.remove(0));

            removeLoops(outVec);
            outVec.add(outVec.firstElement());
        }

        return outVec;
    }

    public Vec4 getShiftedPoint(double shiftMeters, double runLenghtRelative) {
        Vector<Vec4> path = getShifted(shiftMeters);
        Ensure.notNull(path, "path");
        // System.out.println("path:"+path + " shiftMeters:"+shiftMeters+" runLenghtRelative:"+runLenghtRelative);
        if (runLenghtRelative <= 0) {
            return path.firstElement();
        } else if (runLenghtRelative >= 1) {
            return path.lastElement();
        }

        double len = 0;
        Vec4 last = path.firstElement();
        for (int i = 1; i != path.size(); i++) {
            Vec4 next = path.get(i);
            len += last.distanceTo3(next);
            last = next;
        }

        len *= runLenghtRelative;

        last = path.firstElement();
        for (int i = 1; i != path.size(); i++) {
            Vec4 next = path.get(i);
            double thisLen = last.distanceTo3(next);
            if (thisLen > len) {
                len /= thisLen;
                return last.add3(next.subtract3(last).multiply3(len));
            }

            len -= thisLen;
            last = next;
        }

        return path.lastElement();
    }

    public Vector<Vec4> getHull(double width, double curEndY, double curStartX, double curEndX) {
        width /= 2;
        return getHull(width, -width);
    }

    public Vector<Vec4> getHull(double widthRight, double widthLeft) {
        if (pathVec == null) {
            return null;
        }

        Vector<Vec4> out = getShifted(widthRight);
        Vector<Vec4> back = getShifted(widthLeft);
        for (int i = back.size() - 1; i >= 0; i--) {
            out.add(back.get(i));
        }

        return out;
    }

    public double getCenterLength() {
        return centerLength;
    }

    public double getClosestPointDistance(Vec4 v) {
        Vec4 last = pathVec.firstElement();
        double offset = 0;
        double bestDistanceForClosestPoint = Double.POSITIVE_INFINITY;
        double fromStartForClosestPoint = 0;
        for (int i = 1; i != pathVec.size(); i++) {
            Vec4 next = pathVec.get(i);

            Vec4 n = Line.nearestPointOnSegment(last, next, v);
            double dist = v.distanceTo2(n);
            if (dist < bestDistanceForClosestPoint) {
                bestDistanceForClosestPoint = dist;
                fromStartForClosestPoint = offset + last.distanceTo2(n);
            }

            offset += next.distanceTo2(last);
            last = next;
        }

        return fromStartForClosestPoint;
    }

}
