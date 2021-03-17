/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Vector;
import net.java.joglutils.model.geometry.Model;

public class MMesh {
    public Position origin;
    public Vector<MTriangle> triangles;
    public double heading;
    public double tilt;
    public double roll;
    public Model model;
    MinMaxPair minMaxX = new MinMaxPair();
    MinMaxPair minMaxZ = new MinMaxPair();
    MinMaxPair minMaxY = new MinMaxPair();

    public MMesh() {}

    public void setTriangles(Vector<MTriangle> triangles) {
        this.triangles = triangles;
        for (MTriangle triangle : triangles) {
            minMaxX.enlarge(triangle.minMaxX);
            minMaxY.enlarge(triangle.minMaxY);
            minMaxZ.enlarge(triangle.minMaxZ);
        }
    }

    public void transform(
            Matrix transform,
            CPicArea.ModelAxisAlignment xAlign,
            double xOffset,
            CPicArea.ModelAxisAlignment yAlign,
            double yOffset,
            CPicArea.ModelAxisAlignment zAlign,
            double zOffset,
            double yawDeg) {
        Vec4 min = new Vec4(minMaxX.min, minMaxY.min, minMaxZ.min);
        Vec4 max = new Vec4(minMaxX.max, minMaxY.max, minMaxZ.max);
        min = min.transformBy4(transform);
        max = max.transformBy4(transform);
        minMaxX.reset();
        minMaxX.update(min.x);
        minMaxX.update(max.x);
        minMaxY.reset();
        minMaxY.update(min.y);
        minMaxY.update(max.y);
        minMaxZ.reset();
        minMaxZ.update(min.z);
        minMaxZ.update(max.z);

        transform =
            Matrix.fromRotationZ(Angle.fromDegrees(-yawDeg))
                .multiply(
                    Matrix.fromTranslation(
                            new Vec4(
                                -minMaxX.getOffset(xAlign) + xOffset,
                                -minMaxY.getOffset(yAlign) + yOffset,
                                -minMaxZ.getOffset(zAlign) + zOffset))
                        .multiply(transform));
        minMaxX.reset();
        minMaxY.reset();
        minMaxZ.reset();

        Vector<MTriangle> trianglesNew = new Vector<MTriangle>();
        for (MTriangle triangle : triangles) {
            var triangleNew = triangle.transform(transform);
            trianglesNew.add(triangleNew);
            minMaxX.enlarge(triangleNew.minMaxX);
            minMaxY.enlarge(triangleNew.minMaxY);
            minMaxZ.enlarge(triangleNew.minMaxZ);
        }

        triangles = trianglesNew;
        System.out.println("after transform");
        System.out.println("minMaxX:" + minMaxX);
        System.out.println("minMaxY:" + minMaxY);
        System.out.println("minMaxZ:" + minMaxZ);
    }

    public double checkCollision(double safetyDist, Vec4 from, Vec4 to) {
        for (MTriangle triangle : triangles) {
            MTriangle.DistanceResult res = triangle.getDistanceToLinesegment(from, to, safetyDist);
            if (res != null && res.distance < safetyDist) {
                return res.distance;
            }
        }

        return Double.POSITIVE_INFINITY;
    }
}
