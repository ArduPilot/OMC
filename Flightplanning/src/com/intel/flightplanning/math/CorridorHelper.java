/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.math;


import com.intel.flightplanning.core.MinMaxPair;
import com.jme3.math.FastMath;
import com.jme3.math.Line;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import jdk.jshell.spi.ExecutionControl;

public class CorridorHelper {

    List<Vector3f> pathVec;
    List<Vector3f> diffsNormalRot;

    List<Vector3f> normals;
    List<Vector3f> origins;

    boolean isValid;
    float centerLength;
    boolean looped;

    public CorridorHelper(List<Vector3f> path, boolean looped) {
        this.looped = looped;
        setPath(path);
    }

    public CorridorHelper(Vector<Vector3f> path) {
        this(path, false);
    }

    /**
     * derive a new corridor helper, which uses a segment of another one
     *
     * @param corrHelper
     * @param start in meters on the center path from its start from the parent CorridorHelper
     * @param end in meters on the center path from its start from the parent CorridorHelper
     */
    public CorridorHelper(CorridorHelper corrHelper, float start, float end) {
        @SuppressWarnings("unchecked")
        ArrayList<Vector3f> corners = (ArrayList<Vector3f>) ((ArrayList<Vector3f>) corrHelper.pathVec).clone(); // flat copy is ok
        if (corners.size() < 2) {
            setPath(corners);
            computeHelper();
            return;
        }

        // System.out.println("init corners:" + corners.size() + corrHelper.isValidMinGroundDistanceOK());
        // System.out.println("init derived CorridorHelper:" + start + " \t "+ " \t "+end+" \t
        // "+corrHelper.getCenterLength());
        float len = corrHelper.getCenterLength();
        end = len - end; // transform into distance to end

        if (end + start >= len) {
            // non valid!
            return;
        }

        // System.out.println("from end:" + end);
        if (end > 0) {
            Vector3f last = corners.get(corners.size() - 1);

            for (int i = corners.size() - 2; i >= 0; i--) {
                Vector3f c = corners.get(i);
                float runLength = c.distance(last);
                if (runLength >= end) {
                    last = last.add(c.subtract(last).mult(end / runLength));
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
            Vector3f last = corners.get(0);
            for (int i = 1; i < corners.size(); i++) {
                Vector3f c = corners.get(i);
                float runLength = c.distance(last);
                if (runLength >= start) {
                    // System.out.println("detected point to split");
                    last = last.add(c.subtract(last).mult(start / runLength));
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

    public void setPath(List<Vector3f> path) {
        this.pathVec = path;
        computeHelper();
    }

    static final Matrix3f rotRight = (new Quaternion()).fromAngleAxis(FastMath.PI/2, new Vector3f(0,0,1)).toRotationMatrix();

    private void computeHelper() {
        if (pathVec == null || pathVec.size() < 2) {
            return;
        }

        diffsNormalRot = new Vector<Vector3f>(pathVec.size() - 1);
        removeLoops(pathVec);
        if (looped) {
            Vector3f last = pathVec.get(0);
            Vector3f first = pathVec.get(pathVec.size()-1);
            pathVec.add(first);
            pathVec.add(0,last);
        }

        Vector3f last = pathVec.get(0);
        for (int i = 1; i != pathVec.size(); i++) {
            Vector3f next = pathVec.get(i);
            this.centerLength += next.distance(last);
            // System.out.println("len:"+this.centerLength);
            var d = next.subtract(last);
            Vector3f diff = rotRight.multLocal(d).normalize();
            diffsNormalRot.add(diff);
            last = next;
        }
        // System.out.println("diffsNormalRot:"+diffsNormalRot);

        normals = new Vector<Vector3f>(pathVec.size());
        origins = new Vector<Vector3f>(pathVec.size());
        last = diffsNormalRot.get(0);
        normals.add(last);
        origins.add(pathVec.get(0));
        for (int i = 1; i != diffsNormalRot.size(); i++) {
            Vector3f next = diffsNormalRot.get(i);

            Vector3f normalCenter = last.add(next).normalize();
            float cos2 = last.dot(normalCenter);
            Vector3f normal1 = last.add(normalCenter);
            Vector3f normal2 = next.add(normalCenter);
            float len = (float) Math.sqrt(2 / ((cos2 + 1) * normal1.lengthSquared()));
            normal1 = normal1.mult(len);
            normal2 = normal2.mult(len);
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

    public void removeLoops(List<Vector3f> pathVec) {
        // if (true) return;
        // testing from the left starting each segment if it intersects.
        // prefer intersections towards the end of the path, so test them starting from the end
        // System.out.println("remove loops from vec of size:"+pathVec.size());
        Vector3f a1 = pathVec.get(0);
        for (int i = 1; i < pathVec.size() - 2; i++) {
            Vector3f a2 = pathVec.get(i);
            Vector3f b2 = pathVec.get(pathVec.size() - 1);
            for (int k = pathVec.size() - 2; k > i; k--) {
                Vector3f b1 = pathVec.get(k);
                Vector3f intersection = intersect(a1, a2, b1, b2);

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
    public static Vector3f intersect(Vector3f a1, Vector3f a2, Vector3f b1, Vector3f b2) {
        // a1+t(a2-a1)=b1+s(b2-b1)
        // a1-b1=-t(a2-a1)+s(b2-b1)
        // dab=-t da + sdb
        // dab=[-da,db](t,s)
        // [-da,db]^-1 dab=(t,s)

        Vector3f da = a2.subtract(a1);
        Vector3f db = b2.subtract(b1);
        Vector3f dab = a1.subtract(b1);
        // System.out.println("da:"+da);
        // System.out.println("db:"+db);
        // System.out.println("dab:"+dab);

        float det = -da.x * db.y + da.y * db.x;
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

                float dbx = (float) intersection.mean();
                return new Vector3f(b1.x + dbx, b1.y + db.y * dbx / db.x,0);
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

                float dby = (float) intersection.mean();
                return new Vector3f(b1.x + db.x * dby / db.y, b1.y + dby,0);
            }
        }

        det = 1 / det;
        Matrix4f minv = new Matrix4f(det * db.y, det * -db.x, 0, 0, det * da.y, det * -da.x, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);

        Vector3f ts = minv.mult(dab);
        // System.out.println("ts:"+ts);
        if (ts.x > 1 || ts.x < 0) {
            return null;
        }

        if (ts.y > 1 || ts.y < 0) {
            return null;
        }

        return b1.add(db.mult(ts.y));
    }

    public boolean isValid() {
        return isValid;
    }

    public static final float MINIMAL_POSSIBLE_SHIFT = 0.01f;

    public List<Vector3f> getShifted(float shiftMeters) {

        if (Math.abs(shiftMeters) < MINIMAL_POSSIBLE_SHIFT) {
            return pathVec;
        }

        List<Vector3f> outVec = new ArrayList<>(origins.size());

        for (int i = 0; i != origins.size(); i++) {
            Vector3f origin = origins.get(i);
            Vector3f direction = normals.get(i);
            Vector3f vec = origin.add(direction.mult(shiftMeters));
            outVec.add(vec);
        }

        if (looped) {
            outVec.remove(0);
            outVec.remove(outVec.size() - 1);
        }

        removeLoops(outVec);
        if (looped) {
            outVec.add(outVec.get(0));
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
            outVec.add(outVec.get(0));
        }

        return outVec;
    }

    public Vector3f getShiftedPoint(float shiftMeters, float runLenghtRelative) {
        List<Vector3f> path = getShifted(shiftMeters);
        // System.out.println("path:"+path + " shiftMeters:"+shiftMeters+" runLenghtRelative:"+runLenghtRelative);
        if (runLenghtRelative <= 0) {
            return path.get(0);
        } else if (runLenghtRelative >= 1) {
            return path.get(0);
        }

        float len = 0;
        Vector3f last = path.get(0);
        for (int i = 1; i != path.size(); i++) {
            Vector3f next = path.get(i);
            len += last.distance(next);
            last = next;
        }

        len *= runLenghtRelative;

        last = path.get(0);
        for (int i = 1; i != path.size(); i++) {
            Vector3f next = path.get(i);
            float thisLen = last.distance(next);
            if (thisLen > len) {
                len /= thisLen;
                return last.add(next.subtract(last).mult(len));
            }

            len -= thisLen;
            last = next;
        }

        return path.get(0);
    }

    public List<Vector3f> getHull(float width, float curEndY, float curStartX, float curEndX) {
        width /= 2;
        return getHull(width, -width);
    }

    public List<Vector3f> getHull(float widthRight, float widthLeft) {
        if (pathVec == null) {
            return null;
        }

        List<Vector3f> out = getShifted(widthRight);
        List<Vector3f> back = getShifted(widthLeft);
        for (int i = back.size() - 1; i >= 0; i--) {
            out.add(back.get(i));
        }

        return out;
    }

    public float getCenterLength() {
        return centerLength;
    }

    public float getClosestPointDistance(Vector3f v) {
        throw new UnsupportedOperationException();
//        Vector3f last = pathVec.get(0);
//        float offset = 0;
//        float bestDistanceForClosestPoint = float.POSITIVE_INFINITY;
//        float fromStartForClosestPoint = 0;
//        for (int i = 1; i != pathVec.size(); i++) {
//            Vector3f next = pathVec.get(i);
//
//
//            Vector3f n = Line.nearestPointOnSegment(last, next, v);
//            float dist = v.distance(n);
//            if (dist < bestDistanceForClosestPoint) {
//                bestDistanceForClosestPoint = dist;
//                fromStartForClosestPoint = offset + last.distance(n);
//            }
//
//            offset += next.distance(last);
//            last = next;
//        }
//
//        return fromStartForClosestPoint;
    }

}
