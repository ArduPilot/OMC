/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.camerasettings;

import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.ExpertSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.desktop.gui.asctec.F8pkinematics_test;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

/** This class helps Camera class to loose weight */
public class CameraHelper {

    public static final String KEY = "eu.mavinci.plane.Camera";
    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();

    public static double computePlgDataDelay(CPhotoLogLine line, IHardwareConfiguration hardwareConfiguration) {
        // if (gpsType != GPStype.GPS) return 0;
        IGenericCameraDescription cameraDescription =
            hardwareConfiguration.getPayload(IGenericCameraConfiguration.class).getDescription();
        double camDelayMS = cameraDescription.getCameraDelay().convertTo(Unit.MILLISECOND).getValue().doubleValue();
        switch (line.type) {
        case DELAY_30MS:
            return camDelayMS - CPhotoLogLine.DATA_DELAY_PHOTO2;
        case FLASH:
            return 0;
        case TIGGER:
            return camDelayMS + CPhotoLogLine.DATA_DELAY_INSTANT;
        default:
            break;
        }

        Debug.getLog().severe("delay type: " + line.type + " was not defines");
        return camDelayMS;
    }

    public static Matrix getCorrectedPlaneStateTransform(
            CPhotoLogLine line, double additionalDelaySec, IHardwareConfiguration hardwareConfiguration) {

        // TODO FIXME this angles are looking strange, am I realy shure about them?
        // double time = gpsType != GPStype.GPS ? 0 : computePLGdataDelayMS(line);
        double time = computePlgDataDelay(line, hardwareConfiguration);
        time /= 1000;
        time += additionalDelaySec;
        // System.out.println("shiftTimeOrientation="+time);
        // return Quaternion.fromRotationXYZ(Angle.fromDegrees(line.pitch + line.pitchrate * time), Angle
        // .fromDegrees(-line.roll- line.rollrate * time), Angle.fromDegrees(-line.yaw - line.yawrate *
        // time)).multiply(getCameraJustage());
        // time = 0;
        // MAVinci roll is -1*the usual convention for roll....
        // http://de.wikipedia.org/wiki/Eulersche_Winkel#Luftfahrtnorm_.28DIN_9300.29_.28Yaw-Pitch-Roll.2C_Z.2C_Y.E2.80.99.2C_X.E2.80.99.E2.80.99.29
        // Matrix.fromRotationXYZ(..) is related to wikipedia artikel via -\phi = x, -\theta=y, -\Psi = Z (all singes
        // are neglected!!)
        // but in that, the order of the rotation application is WRONG!

        double actPitch = line.cameraPitch + line.cameraPitchRate * time;
        double actRoll = line.cameraRoll + line.cameraRollRate * time;
        double actYaw = line.cameraYaw + line.cameraYawRate * time;
        // Angle pitch = Angle.fromDegrees(actPitch);
        // Angle roll = Angle.fromDegrees(actRoll);
        // Angle yaw= Angle.fromDegrees(actYaw);
        // return Matrix.fromRotationXYZ(pitch.multiply(-1),roll, yaw).getTranspose().multiply(getCameraJustage());
        // //was working quite
        // well, singnes and relation pitch<->x,... checked, but maybe order is wrong, or inverse, or ALL signes are
        // wrong...
        // if (getMinRepTime() <= 0.41){
        // return MathHelper.matrixFromRollPitchYaw(actRoll, actPitch,
        // actYaw).getTranspose().multiply(getCameraJustage());
        // }
        // if (getMinRepTime() <= 0.31){
        // roll=Angle.ZERO;
        // pitch=Angle.ZERO;
        // }
        // if (getMinRepTime() <= 0.21){
        // yaw = Angle.fromDegrees(line.heading);
        // }
        // if (getMinRepTime() <= 0.11){
        // yaw = Angle.ZERO;
        // }

        // System.out.println();
        // System.out.println("pitch "+line.pitch + "\t -> " + pitch);
        // System.out.println("pitch "+line.roll + "\t -> " + roll);
        // System.out.println("yaw "+line.yaw + "\t -> " + yaw);

        // pitch = Angle.ZERO;
        // Matrix transform = Matrix.IDENTITY;
        // transform = transform.multiply(Matrix.fromRotationZ(yaw));
        // transform = transform.multiply(Matrix.fromRotationY(yaw));
        // transform = transform.multiply(Matrix.fromRotationX(yaw));
        //
        // return transform.getTranspose() .multiply(getCameraJustage());

        // to get real angles in NED frame like in wikipedia:
        // http://de.wikipedia.org/wiki/Roll-Nick-Gier-Winkel
        // roll = roll.multiply(-1);
        //
        // Matrix transform = Matrix.IDENTITY;
        // transform = transform.multiply(Matrix.fromRotationY(roll.multiply(-1)));
        // transform = transform.multiply(Matrix.fromRotationX(pitch.multiply(-1)));
        // transform = transform.multiply(Matrix.fromRotationZ(yaw)); //-1*-1=+1 -> Z is pointing in the wrong direction
        // AND rotation has
        // different orientation
        return MathHelper.getRollPitchYawTransformationMAVinicAngles(actRoll, actPitch, actYaw).getTranspose();
    }

    public static Position shiftPosition(
            CPhotoLogLine line,
            double estimatedStartAltWgs84,
            Vec4 rtkOffset,
            IHardwareConfiguration hardwareConfiguration) {
        return shiftPosition(line, estimatedStartAltWgs84, rtkOffset, 0, true, hardwareConfiguration);
    }

    public static Position shiftPosition(
            CPhotoLogLine line,
            double estimatedStartAltWgs84,
            Vec4 rtkOffset,
            double additionalDelaySec,
            boolean enableLevelArm,
            IHardwareConfiguration hardwareConfiguration) {
        // System.out.println("\nline:" + line);
        // System.out.println("rtkOffset:" + rtkOffset);
        IPlatformDescription platformDescription = hardwareConfiguration.getPlatformDescription();
        IGenericCameraDescription cameraDescription =
            hardwareConfiguration.getPayload(IGenericCameraConfiguration.class).getDescription();

        Position p =
            Position.fromDegrees(
                line.lat,
                line.lon,
                ((line.fixType == GPSFixType.rtkFixedBL || line.fixType == GPSFixType.rtkFloatingBL)
                    ? ((line.gps_ellipsoid_cm + line.gps_altitude_cm) / 100.)
                    : (line.getAltInM() + estimatedStartAltWgs84)));
        if (globe == null) {
            return p;
        }

        // System.out.println("posInsideShift="+p + " estimatedAlt: " + estimatedStartAltWGS84 + " @ " + line);
        // if (isDebuggingMode()) return p;

        // double shiftDistance = gpsType != GPStype.GPS ? 0 : line.groundSpeed_cms /100. *
        // (line.type == PhotoLogLineType.FLASH ? 0 : gpsDelayMS) //disable
        // this if gps is not delayed
        double gpsDelayMS = platformDescription.getGpsDelay().convertTo(Unit.MILLISECOND).getValue().doubleValue();
        double shiftDistance =
            line.groundSpeed_cms
                / 100.
                * (gpsDelayMS + line.time_since_last_fix + computePlgDataDelay(line, hardwareConfiguration))
                / 1000.;
        // System.out.println("shiftDistance:"+shiftDistance);
        if (additionalDelaySec != 0) {
            shiftDistance += additionalDelaySec * line.groundSpeed_cms / 100.;
        }

        Position p1 =
            shiftDistance == 0
                ? p
                : new Position(
                    LatLon.rhumbEndPosition(p, Math.toRadians(line.heading), shiftDistance / globe.getRadiusAt(p)),
                    p.getAltitude());
        // System.out.println("new Method->"+end);

        // return new Position(end,p.getAltitude());

        final ExpertSettings expertSettings =
            DependencyInjector.getInstance().getInstanceOf(ExpertSettings.class);

        if (platformDescription.isInCopterMode()
                && line.fixType == GPSFixType.rtkFixedBL
                && expertSettings.getCameraRtkNewLevelArm()) {
            double nodalPoint = expertSettings.getCameraNodalPoint();
            double phaseCenter = expertSettings.getCameraPhaseCenter();

            double[] offsets =
                F8pkinematics_test.getOffsets(
                    line.planeRoll,
                    line.planePitch,
                    line.planeYaw,
                    line.cameraRoll,
                    line.cameraPitch,
                    nodalPoint,
                    phaseCenter);
            if ((offsets[0] != 0 || offsets[1] != 0 || offsets[2] != 0)) {
                // offset[2] to the sky was positive in the camera system, now it is negative
                // offset[0} to the tail was negative and in camera system, now it is to the North and in global system,
                // so now its positive when camera_yaw is 0--- so does it mean that camera is little bit in front of
                // antenna
                // ???
                // offset[1] to the right wing in camera system was positive, now if it looks to the West it is positive
                // in
                // global system
                // (8cm to add to the antenna position to move to the true camera position left if axis is looking West)

                // this is already in world coordinates
                Vec4 gpsOffset = new Vec4(-offsets[1], offsets[0], offsets[2]);
                /* They X axis is mapped to the vector tangent to the globe and pointing East. The Y
                 * axis is mapped to the vector tangent to the globe and pointing to the North Pole. The Z axis is mapped to the
                 * globe normal at (latitude, longitude, metersElevation). The origin is mapped to the cartesian position of
                 * (latitude, longitude, metersElevation).
                 */
                Matrix mat =
                    globe.computeSurfaceOrientationAtPosition(
                        Angle.fromDegreesLatitude(line.lat),
                        Angle.fromDegreesLongitude(line.lon),
                        (line.gps_altitude_cm + line.gps_ellipsoid_cm) / 100.0);
                Vec4 shifted = gpsOffset.transformBy4(mat);
                p1 = globe.computePositionFromPoint(shifted);
            }
        } else {
            double gpsOffsetToRightWingInM =
                cameraDescription.getOffsetToRightWing().convertTo(Unit.METER).getValue().doubleValue();
            double gpsOffsetToSkyInM =
                cameraDescription.getOffsetToSky().convertTo(Unit.METER).getValue().doubleValue();
            double gpsOffsetToTailInM =
                cameraDescription.getOffsetToTail().convertTo(Unit.METER).getValue().doubleValue();

            if (enableLevelArm && (gpsOffsetToRightWingInM != 0 || gpsOffsetToSkyInM != 0 || gpsOffsetToTailInM != 0)) {
                Vec4 gpsOffset = new Vec4(-gpsOffsetToRightWingInM, gpsOffsetToTailInM, -gpsOffsetToSkyInM);

                if (platformDescription.isInCopterMode()) {
                    line = line.clone(); // don't taint source data!
                    // for falcon the old level arm approach is anyway a hack, and its better to not take pitch into
                    // account, since the level arm values are designed to work for pitch 0
                    line.cameraPitch = 0;
                }

                Matrix plane = getCorrectedPlaneStateTransform(line, additionalDelaySec, hardwareConfiguration);
                Matrix globeMatrix = globe.computeModelCoordinateOriginTransform(p1);
                p1 = globe.computePositionFromPoint(gpsOffset.transformBy3(plane).transformBy4(globeMatrix));
            }
        }

        if (rtkOffset != null && rtkOffset.dotSelf3() > 0) {
            Vec4 v = globe.computePointFromPosition(p1);
            // System.out.println("rtk shift Vec: " + rtkOffset);
            p1 = globe.computePositionFromPoint(v.add3(rtkOffset)); // .transformBy4(mGlobe));
        }

        return p1;
    }

    public static Matrix getCameraJustageTransform(IHardwareConfiguration hardwareConfiguration) {
        return getCameraJustageTransform(0, hardwareConfiguration);
    }

    public static Matrix getCorrectedStateTransform(CPhotoLogLine line, IHardwareConfiguration hardwareConfiguration) {
        return getCorrectedStateTransform(line, 0, 0, hardwareConfiguration);
    }

    public static Matrix getCorrectedStateTransform(
            CPhotoLogLine line,
            double cameraYawJustageOffsetDeg,
            double additionalDelaySec,
            IHardwareConfiguration hardwareConfiguration) {
        return getCorrectedPlaneStateTransform(line, additionalDelaySec, hardwareConfiguration)
            .multiply(getCameraJustageTransform(cameraYawJustageOffsetDeg, hardwareConfiguration));
        // return Matrix.fromRotationXYZ(Angle.ZERO,Angle.ZERO, yaw).getTranspose().multiply(getCameraJustage());
    }

    public static Matrix getCameraJustageTransform(
            double cameraYawJustageOffsetDeg, IHardwareConfiguration hardwareConfiguration) {

        // TODO now we have here all info to take into account gimble + camera --- let configuration do the math

        // Quaternion cameraState = Quaternion.fromRotationXYZ(Angle
        // .fromDegrees(roll), Angle.fromDegrees(pitch), Angle
        // .fromDegrees(-yaw));
        // return cameraState;
        // return Matrix.fromRotationXYZ(Angle
        // .fromDegrees(-roll), Angle.fromDegrees(-pitch), Angle
        // .fromDegrees(yaw)).getTranspose();

        /*
         * GPS/INS rotation specifies position of GPS/IMU unit with respect to the camera. Thats why it should applied to local vector from
         * the left in order: M_z(yaw), M_x(pitch), M_y (-roll) (reversed photoscan order)
         */
        IGenericCameraDescription cameraDescription =
            hardwareConfiguration.getPayload(IGenericCameraConfiguration.class).getDescription();

        return MathHelper.getRollPitchYawTransformation(
            cameraDescription.getRoll().convertTo(Unit.DEGREE).getValue().doubleValue(),
            cameraDescription.getPitch().convertTo(Unit.DEGREE).getValue().doubleValue(),
            cameraDescription.getYaw().convertTo(Unit.DEGREE).getValue().doubleValue() + cameraYawJustageOffsetDeg);
    }

    /*
     * (non-Javadoc)
     * @see
     * eu.mavinci.plane.ICamera#getCorrectedStateQuatForRotationRates(eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoLogLine)
     */
    public static Matrix getCorrectedStateTransformForRotationRates(
            CPhotoLogLine line, IHardwareConfiguration hardwareConfiguration) {

        // Angle pitch = Angle.fromDegrees(line.pitchrate ).multiply(-1);
        // Angle roll = Angle.fromDegrees(line.rollrate);
        // Angle yaw= Angle.fromDegrees(line.yawrate);
        // return Matrix.fromRotationXYZ(pitch,roll, yaw).getTranspose().multiply(getCameraJustageTransform());
        // TODO now we have here all info to take into account gimble + camera --- let configuration do the math

        double actPitch = line.cameraPitchRate;
        double actRoll = line.cameraRollRate;
        double actYaw = line.cameraYawRate;
        return MathHelper.getRollPitchYawTransformationMAVinicAngles(actRoll, actPitch, actYaw)
            .getTranspose()
            .multiply(getCameraJustageTransform(hardwareConfiguration));
    }

    public static double getFocalLength35mm(IHardwareConfiguration hardwareConfiguration) {
        IGenericCameraConfiguration cam = hardwareConfiguration.getPayload(IGenericCameraConfiguration.class);
        IGenericCameraDescription cameraDescription = cam.getDescription();
        double ccdWidth = cameraDescription.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double ccdHeight = cameraDescription.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double diag35mm = Math.sqrt(24 * 24 + 36 * 36);
        double diag = Math.sqrt(ccdHeight * ccdHeight + ccdWidth * ccdWidth);
        return cam.getLens().getDescription().getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue()
            * diag35mm
            / diag;
    }

    public static double getMaxFPdiameter(IPlatformDescription platformDescription) {
        return getMaxFPdiameter(platformDescription, 0);
    }

    public static double getMaxFPdiameter(IPlatformDescription platformDescription, double alt) {
        double d = platformDescription.getMaxLineOfSight().convertTo(Unit.METER).getValue().doubleValue() * 2;
        if (alt >= d) {
            return 0;
        }

        return Math.sqrt(d * d - 4 * alt * alt);
    }

    public static double getMaxFPlength(IPlatformDescription platformDescription) {
        return platformDescription.getMaxFlightTime().convertTo(Unit.MINUTE).getValue().doubleValue()
            * 60
            * platformDescription.getMaxPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();
    }

    /////////////////////////////////////////////////////////////////////// 77

    public static OrientationData getCorrectedOrientation(
            CPhotoLogLine line, IHardwareConfiguration hardwareConfiguration) {
        return getCorrectedOrientation(line, 0, hardwareConfiguration);
    }

    public static OrientationData getCorrectedOrientation(
            CPhotoLogLine line, double cameraYawJustageOffsetDeg, IHardwareConfiguration hardwareConfiguration) {
        // System.out.println("orrientation correct: r" + line.roll + " p"+line.pitch + " y" + line.yaw);
        Matrix m = getCorrectedStateTransform(line, cameraYawJustageOffsetDeg, 0, hardwareConfiguration).getTranspose();
        // Matrix m = Matrix.fromQuaternion(q);
        OrientationData o = new OrientationData();

        double[] ret = MathHelper.transformationToRollPitchYawMavinciAngles(m);

        o.roll = ret[0];
        o.pitch = ret[1];
        o.yaw = ret[2];

        // System.out.println("=> r" + o.roll + " p" + o.pitch + " y" + o.yaw);
        // System.out.println();
        return o;
    }

    /**
     * all 4 corners, of an image where the center of the left border is pointing north, and the center of the upper
     * border is pointing West results are the same frame as in @see gov.nasa.worldwind.globes.EllipsoidalGlobe.
     * computeSurfaceOrientationAtPosition(gov.nasa.worldwind.geom.Position)
     *
     * <p>They X axis is mapped to the vector tangent to the globe and pointing East. The Y axis is mapped to the vector
     * tangent to the Globe and pointing to the North Pole. The Z axis is mapped to the Globe normal (pointing to sky)
     *
     * <p>The coordinates are specified in counter-clockwise order with the first coordinate corresponding to the
     * lower-left corner of the overlayed image.
     */
    public static Vec4[] getCornerDirections(IHardwareConfiguration hardwareConfiguration) {

        // all 4 corners, of an image where the center of the left border is
        // pointing north, and the center of the upper border is pointing West
        // results are the same frame as in @see
        // gov.nasa.worldwind.globes.EllipsoidalGlobe.computeSurfaceOrientationAtPosition(gov.nasa.worldwind.geom.Position)
        //
        // They X axis is mapped to the vector tangent to the globe and pointing East. The Y
        // axis is mapped to the vector tangent to the Globe and pointing to the North Pole. The Z axis is mapped to the
        // Globe normal at (latitude, longitude, metersElevation).
        // The coordinates must be specified in counter-clockwise order with the first coordinate
        // corresponding to the lower-left corner of the overlayed image.

        // System.out.println();
        // System.out.println("camName" + getName());
        // System.out.println("witdth" + ccdWidth+ " shiftY:" + ccdshiftYTransl);
        // System.out.println("height" + ccdHeight + " shiftX:" + ccdXTransl);
        IGenericCameraConfiguration cameraConfiguration =
            hardwareConfiguration.getPayload(IGenericCameraConfiguration.class);
        IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();
        ILensDescription lensDescription = cameraConfiguration.getLens().getDescription();

        double length = lensDescription.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double y = cameraDescription.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue() / 2;
        double x = cameraDescription.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue() / 2;
        double yTransl = cameraDescription.getCcdYTransl().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double xTransl = cameraDescription.getCcdXTransl().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        return new Vec4[] {
            new Vec4(y + yTransl, -x + xTransl, -length), new Vec4(y + yTransl, x + xTransl, -length),
            new Vec4(-y + yTransl, x + xTransl, -length), new Vec4(-y + yTransl, -x + xTransl, -length),
        };
    }

    public static Vec4 getCenterDirection(IHardwareConfiguration hardwareConfiguration) {
        IGenericCameraConfiguration cameraConfiguration =
            hardwareConfiguration.getPayload(IGenericCameraConfiguration.class);
        IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();
        ILensDescription lensDescription = cameraConfiguration.getLens().getDescription();

        double length = lensDescription.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        return new Vec4(
            cameraDescription.getCcdYTransl().convertTo(Unit.MILLIMETER).getValue().doubleValue(),
            cameraDescription.getCcdXTransl().convertTo(Unit.MILLIMETER).getValue().doubleValue(),
            -length);
    }

    /** Returning a vector of size 4 with theese values (in meter) upX,lowX,upY,lowY */
    public static double[] getSizeInFlight(double alt, IHardwareConfiguration hardwareConfiguration) {
        Vec4[] cs = getCornerDirections(hardwareConfiguration);
        Plane p = new Plane(0, 0, 1, 0);
        Matrix just = getCameraJustageTransform(hardwareConfiguration);
        // System.out.println("just="+just);
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double[] footprintXd = new double[4];
        double[] footprintYd = new double[4];

        Vec4 origin = new Vec4(0, 0, alt);

        for (int i = 0; i != 4; ++i) {
            Vec4 c = cs[i].transformBy3(just);
            Line ray = new Line(origin, c);
            if (p.intersectDistance(ray) < 0) {
                return new double[] {};
            }

            Vec4 intersec = p.intersect(ray);
            footprintXd[i] = intersec.x;
            footprintYd[i] = intersec.y;
            maxX = Math.max(maxX, intersec.x);
            maxY = Math.max(maxY, intersec.y);
            minX = Math.min(minX, intersec.x);
            minY = Math.min(minY, intersec.y);
            // System.out.println(i+" x="+intersec.x + " y="+intersec.y);
        }

        // bestimme dreieckstumpf, untere kante liegt auf der x-achse
        int idCornerLeft = -1;
        int idCornerRight = -1;

        for (int i = 0; i != 4; ++i) {
            if (footprintXd[i] == minX) {
                idCornerLeft = i;
            }

            if (footprintXd[i] == maxX) {
                idCornerRight = i;
            }
        }

        // System.out.println("idCornerLeft:"+idCornerLeft);
        // System.out.println("idCornerRight:"+idCornerRight);

        int idTop1 = -1;
        int idTop2 = -1;
        for (int i = 0; i != 4; ++i) {
            if (i != idCornerLeft && i != idCornerRight) {
                if (idTop1 == -1) {
                    idTop1 = i;
                } else if (idTop2 == -1) {
                    idTop2 = i;
                }
            }
        }
        // System.out.println("idTop1:"+idTop1);
        // System.out.println("idTop2:"+idTop2);

        double hightTop1 = 0;
        double hightTop2 = 0;
        for (int i = 0; i != 4; i++) {
            int j = (i + 1) % 4;
            double x1 = footprintXd[i];
            double x2 = footprintXd[j];
            if (x1 == x2) {
                continue;
            }

            double y1 = footprintYd[i];
            double y2 = footprintYd[j];
            // System.out.println("i="+i);
            // System.out.println("x1="+x1);
            // System.out.println("x2="+x2);

            // find crossing edge for top1
            double x = footprintXd[idTop1];
            // System.out.println("x id1:"+x);
            if ((x1 >= x && x2 <= x) || (x1 <= x && x2 >= x)) {
                // System.out.println("cross for id1 at i="+i);
                double y = y1 + (y2 - y1) / (x2 - x1) * (x - x1);
                hightTop1 = Math.max(hightTop1, Math.abs(y - footprintYd[idTop1]));
            }

            // find crossing edge for top2
            x = footprintXd[idTop2];
            // System.out.println("x id2:"+x);
            if ((x1 >= x && x2 <= x) || (x1 <= x && x2 >= x)) {
                // System.out.println("cross for id2 at i="+i);
                double y = y1 + (y2 - y1) / (x2 - x1) * (x - x1);
                hightTop2 = Math.max(hightTop2, Math.abs(y - footprintYd[idTop2]));
            }
        }
        // System.out.println("hightTop1:"+hightTop1);
        // System.out.println("hightTop2:"+hightTop2);

        int idTopHigher = -1;
        double hightTopHigher = 0;
        double hightTopLower = 0;

        if (hightTop1 > hightTop2) {
            idTopHigher = idTop1;
            hightTopHigher = hightTop1;
            hightTopLower = hightTop2;
        } else {
            idTopHigher = idTop2;
            hightTopHigher = hightTop2;
            hightTopLower = hightTop1;
        }

        // System.out.println("idTopHigher:"+idTopHigher);
        // System.out.println("hightTopHigher:"+hightTopHigher);
        // System.out.println("hightTopLower:"+hightTopLower);

        double hightTopLeft = 0;
        double hightTopRight = 0;
        int idTopLeft = -1;
        int idTopRight = -1;
        if (footprintXd[idTop1] > footprintXd[idTop2]) {
            idTopLeft = idTop2;
            idTopRight = idTop1;
            hightTopLeft = hightTop2;
            hightTopRight = hightTop1;
        } else {
            idTopLeft = idTop1;
            idTopRight = idTop2;
            hightTopLeft = hightTop1;
            hightTopRight = hightTop2;
        }

        // System.out.println("idTopLeft:"+idTopLeft);
        // System.out.println("idTopRight:"+idTopRight);
        // System.out.println("hightTopLeft:"+hightTopLeft);
        // System.out.println("hightTopRight:"+hightTopRight);

        // berechne höhe und x position von virtueller spitze
        @SuppressWarnings("checkstyle:localvariablename")
        double dXleft = footprintXd[idTopLeft] - footprintXd[idCornerLeft];
        @SuppressWarnings("checkstyle:localvariablename")
        double dXright = footprintXd[idCornerRight] - footprintXd[idTopRight];

        double cutLeftDx;
        double cutRightDx;
        double high;
        double det = hightTopLeft * dXright + hightTopRight * dXleft;
        double g = footprintXd[idCornerRight] - footprintXd[idCornerLeft];
        if (det != 0) {
            det = 1 / det;
            double alpha = det * hightTopRight * g;

            double virtTopDx = alpha * dXleft;
            double highVirt = alpha * hightTopLeft;

            // System.out.println("dYleft:"+dXleft);
            // System.out.println("dYright:"+dXright);
            // System.out.println("det:"+det);
            // System.out.println("g:"+g);
            // System.out.println("alpha:"+alpha);
            // System.out.println("virtTopDx:"+virtTopDx);
            // System.out.println("highVirt:"+highVirt);

            // berechne linken und rechten X-pos des cuts
            // fallunterscheidung
            if (hightTop1 > highVirt * 0.5 && hightTop2 > highVirt * 0.5) {
                cutLeftDx = virtTopDx * 0.5;
                cutRightDx = (g + virtTopDx) * 0.5;
                high = highVirt * 0.5;
                // System.out.println("case1");
            } else if (hightTopLower * 2 < hightTopHigher) {
                double topHigherDx = footprintXd[idTopHigher] - footprintXd[idCornerLeft];
                cutLeftDx = topHigherDx * 0.5;
                cutRightDx = (g + topHigherDx) * 0.5;
                high = hightTopHigher * 0.5;
                // System.out.println("case2");
            } else {
                high = hightTopLower;
                cutLeftDx = high / highVirt * virtTopDx;
                cutRightDx = g - high / highVirt * (g - virtTopDx);
                // System.out.println("case3");
            }
        } else {
            cutLeftDx = 0;
            cutRightDx = g;
            high = hightTopLower;
        }

        // System.out.println("cutLeftDx:"+cutLeftDx);
        // System.out.println("cutRightDx:"+cutRightDx);
        // System.out.println("high:"+high);

        double cutLeftX = cutLeftDx + footprintXd[idCornerLeft];
        double cutRightX = cutRightDx + footprintXd[idCornerLeft];

        // System.out.println("cutLeftX:"+cutLeftX);
        // System.out.println("cutRightX:"+cutRightX);

        double cutLeftYmax = Double.NEGATIVE_INFINITY;
        double cutRightYmax = Double.NEGATIVE_INFINITY;
        double cutLeftYmin = Double.POSITIVE_INFINITY;
        double cutRightYmin = Double.POSITIVE_INFINITY;

        // sometimes due numerically inaccuarcy, the left and right x value is out of range,
        // so no cut will be found. to prevent this, set the X values to the actually extream values

        cutLeftX = Math.max(cutLeftX, footprintXd[idCornerLeft]);
        cutRightX = Math.min(cutRightX, footprintXd[idCornerRight]);

        // back to original polygon, calculate their the REAL
        for (int i = 0; i != 4; i++) {
            int j = (i + 1) % 4;
            double x1 = footprintXd[i];
            double x2 = footprintXd[j];
            // System.out.println("x1="+x1+ "\tx2="+x2);
            if (x1 == x2) {
                continue;
            }

            double y1 = footprintYd[i];
            double y2 = footprintYd[j];
            // System.out.println("y1="+y1+ "\ty2="+y2);

            // find crossing edge for left cut
            double x = cutLeftX;
            if ((x1 >= x && x2 <= x) || (x1 <= x && x2 >= x)) {
                if (MathHelper.isDifferenceTiny(x1, x2)) {
                    cutLeftYmax = Math.max(cutLeftYmax, y1);
                    cutLeftYmin = Math.min(cutLeftYmin, y1);
                    cutLeftYmax = Math.max(cutLeftYmax, y2);
                    cutLeftYmin = Math.min(cutLeftYmin, y2);
                } else {
                    double y = y1 + (y2 - y1) / (x2 - x1) * (x - x1);
                    // System.out.println("curLeft i=" + i + " y="+y);
                    cutLeftYmax = Math.max(cutLeftYmax, y);
                    cutLeftYmin = Math.min(cutLeftYmin, y);
                }
            }

            // find crossing edge for right cut
            x = cutRightX;
            if ((x1 >= x && x2 <= x) || (x1 <= x && x2 >= x)) {
                if (MathHelper.isDifferenceTiny(x1, x2)) {
                    cutRightYmax = Math.max(cutRightYmax, y1);
                    cutRightYmin = Math.min(cutRightYmin, y1);
                    cutRightYmax = Math.max(cutRightYmax, y2);
                    cutRightYmin = Math.min(cutRightYmin, y2);
                } else {
                    double y = y1 + (y2 - y1) / (x2 - x1) * (x - x1);
                    // System.out.println("curRight i=" + i + " y="+y);
                    cutRightYmax = Math.max(cutRightYmax, y);
                    cutRightYmin = Math.min(cutRightYmin, y);
                }
            }
        }

        // System.out.println("cutLeftYmax:"+cutLeftYmax);
        // System.out.println("cutLeftYmin:"+cutLeftYmin);
        //
        // System.out.println("cutRightYmax:"+cutRightYmax);
        // System.out.println("cutRightYmin:"+cutRightYmin);

        double cutLeftYmean = (cutLeftYmax + cutLeftYmin) * 0.5;
        double cutRightYmean = (cutRightYmax + cutRightYmin) * 0.5;

        double centrencyParallelFlight = (cutRightX + cutLeftX) * 0.5;
        double centrencyInFlight = (cutLeftYmean + cutRightYmean) * 0.5;
        double sizeParallelFlight = cutRightDx - cutLeftDx;
        double sizeInFlight = high;
        double leftOvershoot = cutLeftYmean - cutRightYmean;

        // calculate area efficiency
        double areaTotal = 0;
        for (int i = 0; i != 4; i++) {
            int j = (i + 1) % 4;
            double x1 = footprintXd[i];
            double x2 = footprintXd[j];
            double y1 = footprintYd[i];
            double y2 = footprintYd[j];
            areaTotal += (x1 + x2) * (y1 - y2);
        }

        areaTotal = Math.abs(0.5 * areaTotal);

        double areaInner = high * sizeParallelFlight;
        double efficiency = areaInner / areaTotal;

        double pixelEnlargingCenter =
            Math.sqrt(
                    alt * alt
                        + centrencyParallelFlight * centrencyParallelFlight
                        + centrencyInFlight * centrencyInFlight)
                / alt;

        // System.out.println("centrencyParallelFlight=" + centrencyParallelFlight);
        // System.out.println("centrencyInFlight=" + centrencyInFlight);
        // System.out.println("sizeParallelFlight=" + sizeParallelFlight);
        // System.out.println("sizeInFlight=" + sizeInFlight);
        // System.out.println("leftOvershoot=" + leftOvershoot);
        // System.out.println("efficiency=" + efficiency);
        // System.out.println("pixelEnlargingCenter="+pixelEnlargingCenter);

        return new double[] {
            centrencyParallelFlight,
            centrencyInFlight,
            sizeParallelFlight,
            sizeInFlight,
            leftOvershoot,
            efficiency,
            pixelEnlargingCenter
        };
    }

    public static double getPixSizeInM(double distanceToCornerInM, IHardwareConfiguration hardwareConfiguration) {
        IGenericCameraConfiguration cameraConfiguration =
            hardwareConfiguration.getPayload(IGenericCameraConfiguration.class);
        IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();
        ILensDescription lensDescription = cameraConfiguration.getLens().getDescription();

        double focalLength = lensDescription.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double ccdHeight = cameraDescription.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double ccdWidth = cameraDescription.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        int ccdResX = cameraDescription.getCcdResX();
        int ccdResY = cameraDescription.getCcdResY();

        double x = ccdWidth / ccdResX;
        double y = ccdHeight / ccdResY;
        // double ccdDiag = Math.sqrt(ccdHeight*ccdHeight+ccdWidth*ccdWidth)/2;

        double distanceInM =
            distanceToCornerInM
                * focalLength
                / Math.sqrt(ccdHeight * ccdHeight / 4 + ccdWidth * ccdWidth / 4 + focalLength * focalLength);

        return (x + y) / 2 * distanceInM / focalLength;
    }

    // Deprecated for PicArea, wonly should be used for rough estimation
    // For precise computations use AltitudeGsdCalculator
    /** Returns estimated GSD in meter for a given distance to target. */
    public static double estimateGsdAtDistance(double distanceInM, IHardwareConfiguration hardwareConfiguration) {
        IGenericCameraDescription cameraDescription =
            hardwareConfiguration.getPayload(IGenericCameraConfiguration.class).getDescription();

        int ccdResX = cameraDescription.getCcdResX();
        int ccdResY = cameraDescription.getCcdResY();
        return estimateGsdAtDistance(distanceInM, ccdResX, ccdResY, hardwareConfiguration);
    }
    // Deprecated for PicArea, wonly should be used for rough estimation
    // For precise computations use AltitudeGsdCalculator
    public static double estimateGsdAtDistance(
            double distanceInM, int ccdResX, int ccdResY, IHardwareConfiguration hardwareConfiguration) {
        IGenericCameraConfiguration cameraConfiguration =
            hardwareConfiguration.getPayload(IGenericCameraConfiguration.class);
        IGenericCameraDescription cameraDescription = cameraConfiguration.getDescription();
        ILensDescription lensDescription = cameraConfiguration.getLens().getDescription();

        double focalLength = lensDescription.getFocalLength().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double ccdHeight = cameraDescription.getCcdHeight().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        double ccdWidth = cameraDescription.getCcdWidth().convertTo(Unit.MILLIMETER).getValue().doubleValue();
        return distanceInM / focalLength * (ccdWidth / ccdResX + ccdHeight / ccdResY) / 2;
    }

    public static OrientationData getCorrectedRotationRates(
            CPhotoLogLine line, IHardwareConfiguration hardwareConfiguration) {
        // System.out.println("orrientation correct: r" + line.roll + " p"+line.pitch + " y" + line.yaw);
        Matrix m = getCorrectedStateTransformForRotationRates(line, hardwareConfiguration).getTranspose();
        // Matrix m = Matrix.fromQuaternion(q);
        OrientationData o = new OrientationData();

        double[] ret = MathHelper.transformationToRollPitchYawMavinciAngles(m);

        o.roll = ret[0];
        o.pitch = ret[1];
        o.yaw = ret[2];

        // System.out.println("=> r" + o.roll + " p" + o.pitch + " y" + o.yaw);
        // System.out.println();
        return o;
    }

}
