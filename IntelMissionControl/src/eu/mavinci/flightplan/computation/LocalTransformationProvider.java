/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.flightplan.ITransformationProvider;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import org.openide.util.NotImplementedException;

public class LocalTransformationProvider implements ITransformationProvider {

    Position referencePoint;
    Matrix transform4 = null;
    Matrix transform4Inv = null;
    Matrix transform3 = null;
    Matrix transform3Inv = null;
    Angle xAxisYaw;
    double xShift;
    double yShift;
    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();

    public LocalTransformationProvider(
            Position referencePoint, Angle xAxisYaw, double xShift, double yShift, boolean transformAlt) {
        this.referencePoint = transformAlt ? referencePoint : new Position(referencePoint, 0);
        this.xAxisYaw = xAxisYaw;
        this.xShift = xShift;
        this.yShift = yShift;
        transform4Inv = globe.computeModelCoordinateOriginTransform(this.referencePoint);
        transform4 = transform4Inv.getInverse();
        transform3 = Matrix.fromRotationZ(xAxisYaw);
        transform3Inv = transform3.getInverse();
    }

    @Override
    public double getHeightOffsetToGlobal() {
        return referencePoint.elevation;
    }

    @Override
    public Vec4 transformToLocal(LatLon latLon) {
        return globe.computePointFromLocation(latLon)
            .transformBy4(transform4)
            .transformBy3(transform3)
            .subtract3(xShift, yShift, 0);
    }

    @Override
    public Vec4 transformToLocalInclAlt(Position pos) {
        return globe.computePointFromPosition(pos)
            .transformBy4(transform4)
            .transformBy3(transform3)
            .subtract3(xShift, yShift, 0);
    }

    @Override
    public Vec4 compensateCamCentrency(Vec4 vec, boolean isForward, boolean isRot90) {
        throw new NotImplementedException();
    }

    public double compensateCamCentrency(double parallelCoordinate, boolean isForward) {
        throw new NotImplementedException();
    }

    @Override
    public Position transformToGlobe(Vec4 vec) {
        if (vec == null) {
            return null;
        }

        return globe.computePositionFromPoint(
            vec.add3(xShift, yShift, 0).transformBy3(transform3Inv).transformBy4(transform4Inv));
    }

    @Override
    public Vec4 transformToGlobalNorthing(Vec4 vec) {
        return vec.transformBy3(transform3Inv);
    }

    @Override
    public Vec4 transformToLocal(Vec4 vec) {
        return vec.transformBy3(transform3);
    }

    @Override
    public Angle transformYawToLocal(Angle yaw) {
        return yaw.subtract(xAxisYaw); // TODO FIXME.. I dont know if it has to be add or substract
    }

    @Override
    public Angle transformYawToGlobal(Angle yaw) {
        return yaw.add(xAxisYaw); // TODO FIXME.. I dont know if it has to be add or substract
    }

}
