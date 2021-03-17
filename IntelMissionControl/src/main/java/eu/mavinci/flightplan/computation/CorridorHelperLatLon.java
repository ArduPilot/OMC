/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.util.Collection;
import java.util.Vector;

public class CorridorHelperLatLon {

    Collection<LatLon> path;
    LatLon center;
    Sector sector;
    Vector<Vec4> pathVec;
    Vector<Vec4> diffsNormalRot;

    Vector<Vec4> normals;
    Vector<Vec4> origins;

    Matrix transform4Inv;
    Matrix transform4;

    private final Globe globe;

    public CorridorHelperLatLon(Globe globe, Collection<LatLon> path) {
        this.globe = globe;
        setPath(path);
    }

    public void setPath(Collection<LatLon> path) {
        this.path = path;
        computeHelper();
    }

    static final Matrix rotRight = Matrix.fromRotationZ(Angle.POS90);

    private void computeHelper() {
        if (path.size() < 2) {
            return;
        }

        sector = Sector.boundingSector(path);
        center = sector.getCentroid();
        pathVec = new Vector<Vec4>(path.size());
        diffsNormalRot = new Vector<Vec4>(path.size() - 1);
        transform4Inv = globe.computeModelCoordinateOriginTransform(new Position(center, 0));
        transform4 = transform4Inv.getInverse();
        for (LatLon p : path) {
            Vec4 v = globe.computePointFromLocation(new Position(p, 0)).transformBy4(transform4);
            v = new Vec4(v.x, v.y);
            pathVec.add(v);
        }

        removeLoops(pathVec);
        Vec4 last = pathVec.get(0);
        for (int i = 1; i != pathVec.size(); i++) {
            Vec4 next = pathVec.get(i);
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

        // System.out.println("normals:"+normals);
    }

    public void removeLoops(Vector<Vec4> pathVec) {
        // testing from the left starting each segment if it intersects.
        // prefer intersections towards the end of the path, so test them starting from the end
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

    public Vector<LatLon> getShifted(double shiftMeters) {
        if (pathVec == null) {
            return null;
        }

        if (Math.abs(shiftMeters) < 0.01) {
            if (path instanceof Vector<?>) {
                return (Vector<LatLon>)path;
            } else {
                return new Vector<LatLon>(path);
            }
        }

        Vector<Vec4> outVec = new Vector<Vec4>(origins.size());

        for (int i = 0; i != origins.size(); i++) {
            Vec4 origin = origins.get(i);
            Vec4 direction = normals.get(i);
            Vec4 vec = origin.add3(direction.multiply3(shiftMeters));
            outVec.add(vec);
        }

        removeLoops(outVec);

        Vector<LatLon> out = new Vector<LatLon>(outVec.size());
        for (Vec4 vec : outVec) {
            Position pos = globe.computePositionFromPoint(vec.transformBy4(transform4Inv));
            out.add(pos);
        }

        return out;
    }

    public Vector<LatLon> getHull(double widht) {
        if (pathVec == null) {
            return null;
        }

        Vector<LatLon> out = getShifted(widht / 2);
        Vector<LatLon> back = getShifted(-widht / 2);
        for (int i = back.size() - 1; i >= 0; i--) {
            out.add(back.get(i));
        }

        return out;
    }

}
