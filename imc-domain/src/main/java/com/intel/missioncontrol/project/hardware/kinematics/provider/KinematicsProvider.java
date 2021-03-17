/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics.provider;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.IOrientation;
import com.intel.missioncontrol.geometry.Vec3;
import com.intel.missioncontrol.geospatial.Convert;
import com.intel.missioncontrol.geospatial.Position;
import com.intel.missioncontrol.project.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.project.hardware.IPayloadConfiguration;
import com.intel.missioncontrol.project.hardware.IPayloadMountConfiguration;
import com.intel.missioncontrol.project.hardware.SnapPointId;
import com.intel.missioncontrol.project.hardware.kinematics.AffineTransform;
import com.intel.missioncontrol.project.hardware.kinematics.IKinematicChain;
import com.intel.missioncontrol.project.hardware.kinematics.IKinematicsProvider;
import com.intel.missioncontrol.project.hardware.kinematics.IPayloadKinematicChain;
import com.intel.missioncontrol.project.hardware.kinematics.KinematicChainState;
import com.intel.missioncontrol.project.hardware.kinematics.KinematicSegment;
import com.intel.missioncontrol.project.hardware.kinematics.PayloadKinematicChain;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 * Provides a kinematic chain representation of drone hardware geometry, allowing for coordinate transforms, e.g.
 * between payload position and GNSS antenna position. Such positions are denoted by snap point IDs, originating in the
 * platform description. The kinematic chain may include movable segments of a gimbal, with some of them controllable
 * (e.g. gimbal pitch angle).
 */
public class KinematicsProvider implements IKinematicsProvider {

    private final Map<IHardwareConfiguration, IKinematicChain> kinematicChainCache = new WeakHashMap<>();

    private final IKinematicChainFactory kinematicChainFactory;

    @Inject
    public KinematicsProvider(IKinematicChainFactory kinematicChainFactory) {
        this.kinematicChainFactory = kinematicChainFactory;
    }

    @Override
    public IKinematicChain getOrCreateKinematicChain(IHardwareConfiguration hardwareConfiguration) {
        synchronized (kinematicChainCache) {
            IKinematicChain kinematicChain = kinematicChainCache.get(hardwareConfiguration);
            if (kinematicChain != null) {
                return kinematicChain;
            }

            IKinematicChain res = kinematicChainFactory.createFromHardwareConfiguration(hardwareConfiguration);
            kinematicChainCache.put(hardwareConfiguration, res);
            return res;
        }
    }

    @Override
    public IPayloadKinematicChain getOrCreatePayloadKinematicChain(
            IHardwareConfiguration hardwareConfiguration, int payloadMountIndex, int payloadIndex) {
        IKinematicChain kinematicChain = getOrCreateKinematicChain(hardwareConfiguration);

        // TODO cache

        // find corresponding payload nodal point snap point ID
        int nPayloadMounts = hardwareConfiguration.getPayloadMounts().size();
        if (payloadMountIndex < 0 || payloadMountIndex >= nPayloadMounts) {
            throw new IllegalArgumentException("payloadMountIndex out of range");
        }

        IPayloadMountConfiguration payloadMountConfiguration =
            hardwareConfiguration.getPayloadMounts().get(payloadMountIndex);
        int nPayloads = payloadMountConfiguration.getPayloads().size();

        if (payloadIndex < 0 || payloadIndex >= nPayloads) {
            throw new IllegalArgumentException("payloadIndex out of range");
        }

        IPayloadConfiguration payloadConfiguration = payloadMountConfiguration.getPayloads().get(payloadIndex);

        // payload snap point, where payload attaches to the gimbal:
        SnapPointId payloadSnapPointId = new SnapPointId(payloadConfiguration.getPayloadSnapPointId());

        KinematicSegment segment = kinematicChain.getKinematicSegmentMap().get(payloadSnapPointId);
        if (segment == null) {
            throw new IllegalArgumentException(
                "hardwareConfiguration: missing snap point id " + payloadSnapPointId.getName());
        }

        // follow kinematic chain to end:
        List<KinematicSegment> nextSegments;
        do {
            // find next segment
            final KinematicSegment seg = segment;
            nextSegments =
                kinematicChain
                    .getKinematicSegmentMap()
                    .values()
                    .stream()
                    .filter(s -> seg.equals(s.getPrevious()))
                    .collect(Collectors.toList());
            if (nextSegments.size() > 1) {
                throw new IllegalArgumentException(
                    "hardwareConfiguration: Error: Multiple snap points attached to payload snap point:"
                        + payloadSnapPointId.getName());
            }

            if (nextSegments.size() == 1) {
                segment = nextSegments.get(0);
            }
        } while (nextSegments.size() > 0);

        return new PayloadKinematicChain(kinematicChain, segment.getSnapPointId());
    }

    /**
     * Use the given kinematic chain state to transform a WGS84 Position associated with a snap point on a kinematic
     * chain of a drone with a given earth frame orientation to a WGS84 Position corresponding to another snap point.
     */
    public Position transformWGS84Position(
            KinematicChainState kinematicChainState,
            SnapPointId sourceSnapPointId,
            IOrientation sourceOrientation,
            Position sourceSnapPointPosition,
            SnapPointId targetSnapPointId) {
        AffineTransform earthFrameToSourceFrameTransform = AffineTransform.fromOrientation(sourceOrientation);

        AffineTransform transform =
            earthFrameToSourceFrameTransform.chain(
                kinematicChainState.getTransformPath(sourceSnapPointId, targetSnapPointId));

        // The transform changes the coordinate system of a fixed point, while we need to transform a
        // point in a fixed system here => use inverse transform
        Vec3 earthFrameOffset = transform.inverse().getOffset();

        return shiftPositionByEarthFrameOffset(sourceSnapPointPosition, earthFrameOffset);
    }

    /**
     * Offset a position in WGS84 by the given vector, in meters, in the earth frame fixed at the given position. Offset
     * coordinates see https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft)
     */
    private Position shiftPositionByEarthFrameOffset(Position position, Vec3 earthFrameOffset) {
        Vec3 ecef =
            Convert.geodeticToEcef(
                position.getLatitudeRadians(), position.getLongitudeRadians(), position.getElevation());

        // add offset in ecef frame.
        // earth frame: x north, y east, z down.

        // ecef frame:
        // X: tangent to the globe and pointing East.
        // Y: tangent to the globe and pointing to the North Pole
        // Z: globe normal at (latitude, longitude, metersElevation).
        // The origin is mapped to the cartesian position of (latitude, longitude, metersElevation).

        //noinspection SuspiciousNameCombination
        ecef.addInplace(new Vec3(earthFrameOffset.y, earthFrameOffset.x, -earthFrameOffset.z));

        Position positionWithOffset = Convert.ecefToGeodetic(ecef);

        // TODO check accuracy
        return positionWithOffset;
    }

}
