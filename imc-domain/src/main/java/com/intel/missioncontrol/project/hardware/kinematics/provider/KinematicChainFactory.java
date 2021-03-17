/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics.provider;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.Vec3;
import com.intel.missioncontrol.project.hardware.GnssAntennaDescription;
import com.intel.missioncontrol.project.hardware.GpsType;
import com.intel.missioncontrol.project.hardware.IDescriptionProvider;
import com.intel.missioncontrol.project.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.project.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.project.hardware.IGimbalSegmentDescription;
import com.intel.missioncontrol.project.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.project.hardware.ILensConfiguration;
import com.intel.missioncontrol.project.hardware.IPayloadConfiguration;
import com.intel.missioncontrol.project.hardware.IPayloadDescription;
import com.intel.missioncontrol.project.hardware.IPayloadMountConfiguration;
import com.intel.missioncontrol.project.hardware.IPayloadMountDescription;
import com.intel.missioncontrol.project.hardware.IPayloadSnapPointDescription;
import com.intel.missioncontrol.project.hardware.ISnapPointDescription;
import com.intel.missioncontrol.project.hardware.LensDescription;
import com.intel.missioncontrol.project.hardware.PlatformDescription;
import com.intel.missioncontrol.project.hardware.SnapPointDescription;
import com.intel.missioncontrol.project.hardware.SnapPointId;
import com.intel.missioncontrol.project.hardware.kinematics.IKinematicChain;
import com.intel.missioncontrol.project.hardware.kinematics.KinematicChain;
import com.intel.missioncontrol.project.hardware.kinematics.KinematicChainState;
import com.intel.missioncontrol.project.hardware.kinematics.KinematicSegment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KinematicChainFactory implements IKinematicChainFactory {

    private final IDescriptionProvider descriptionProvider;

    @Inject
    KinematicChainFactory(IDescriptionProvider descriptionProvider) {
        this.descriptionProvider = descriptionProvider;
    }

    public IKinematicChain createFromHardwareConfiguration(IHardwareConfiguration hardwareConfiguration) {
        List<KinematicSegment> kinematicSegments = new ArrayList<>();

        KinematicSegment kinematicSegment = KinematicSegment.BODY_ORIGIN;
        kinematicSegments.add(kinematicSegment);

        PlatformDescription platformDescription =
            descriptionProvider.getPlatformDescriptionById(hardwareConfiguration.getDescriptionId());

        // gimbal segments on payload mounts:
        int nPayloadMounts = hardwareConfiguration.getPayloadMounts().size();
        for (int iPayloadMount = 0; iPayloadMount < nPayloadMounts; iPayloadMount++) {
            IPayloadMountConfiguration payloadMountConfiguration =
                hardwareConfiguration.getPayloadMounts().get(iPayloadMount);
            String payloadMountDescId = payloadMountConfiguration.getDescriptionId();

            IPayloadMountDescription payloadMountDescription =
                platformDescription
                    .getCompatiblePayloadMounts()
                    .stream()
                    .filter(desc -> desc.getId().equals(payloadMountDescId))
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new IllegalArgumentException(
                                "hardwareConfiguration: invalid payload mount description id"));

            int angleIndex = 0;
            for (IGimbalSegmentDescription gimbalSegmentDescription : payloadMountDescription.getGimbalSegments()) {
                kinematicSegment =
                    KinematicSegmentFactory.createFromGimbalSegment(
                        gimbalSegmentDescription, angleIndex, kinematicSegment);
                kinematicSegments.add(kinematicSegment);
                angleIndex++;
            }

            // configured payloads at this payload mount:
            int nPayloads = payloadMountConfiguration.getPayloads().size();
            for (int iPayload = 0; iPayload < nPayloads; iPayload++) {
                IPayloadConfiguration payloadConfiguration = payloadMountConfiguration.getPayloads().get(iPayload);
                String payloadDescriptionId = payloadConfiguration.getDescriptionId();
                IPayloadDescription payloadDescription =
                    descriptionProvider.getPayloadDescriptionById(payloadDescriptionId);

                SnapPointId payloadSnapPointId = new SnapPointId(payloadConfiguration.getPayloadSnapPointId());

                // payload snap points (where payloads attach to gimbal):
                IPayloadSnapPointDescription payloadSnapPointDescription =
                    payloadMountDescription
                        .getPayloadSnapPoints()
                        .stream()
                        .filter(sp -> sp.getId().equals(payloadSnapPointId))
                        .findFirst()
                        .orElse(null);
                if (payloadSnapPointDescription == null) {
                    throw new IllegalArgumentException(
                        "hardwareConfiguration: Invalid payload configuration: invalid snap point"
                            + payloadSnapPointId);
                }

                Optional<KinematicSegment> previous =
                    kinematicSegments
                        .stream()
                        .filter(s -> s.getSnapPointId().equals(payloadSnapPointDescription.getAttachedToSnapPointId()))
                        .findFirst();
                if (previous.isEmpty()) {
                    throw new IllegalArgumentException(
                        "hardwareConfiguration: Error in payload mount description: payload snap point "
                            + payloadSnapPointDescription.getId().getName()
                            + " refers to unknown snap point ID "
                            + payloadSnapPointDescription.getAttachedToSnapPointId());
                }

                KinematicSegment payloadSnapPointKinematicSegment =
                    KinematicSegmentFactory.createFromPayloadSnapPoint(payloadSnapPointDescription, previous.get());
                kinematicSegments.add(payloadSnapPointKinematicSegment);

                if (payloadConfiguration instanceof IGenericCameraConfiguration && payloadDescription != null) {
                    IGenericCameraDescription cameraDescription =
                        descriptionProvider.getGenericCameraDescriptionById(payloadConfiguration.getDescriptionId());
                    IGenericCameraConfiguration cameraConfig = (IGenericCameraConfiguration)payloadConfiguration;

                    // lens mount snap point:
                    SnapPointId lensMountSnapPointId =
                        new SnapPointId(
                            "PAYLOAD_"
                                + payloadDescription.getId()
                                + "@"
                                + iPayloadMount
                                + ":"
                                + iPayload
                                + "_LENSMOUNT");
                    SnapPointDescription lensMountSnapPointDefinition =
                        new SnapPointDescription(
                            lensMountSnapPointId, payloadSnapPointId, cameraDescription.getLensMountOffset(), null);

                    KinematicSegment lensMountKinematicSegment =
                        KinematicSegmentFactory.createFromSnapPointDescription(
                            lensMountSnapPointDefinition, payloadSnapPointKinematicSegment);
                    kinematicSegments.add(lensMountKinematicSegment);

                    // nodal point snap point:
                    ILensConfiguration lensConfig = cameraConfig.getLens();
                    if (lensConfig == null) {
                        throw new IllegalArgumentException(
                            "hardwareConfiguration: Invalid camera configuration: No lens configured");
                    }

                    LensDescription lensDescription =
                        descriptionProvider.getLensDescriptionById(lensConfig.getDescriptionId());
                    if (lensDescription == null) {
                        throw new IllegalArgumentException(
                            "hardwareConfiguration: Invalid lens configuration: Invalid lens ID: "
                                + lensConfig.getDescriptionId());
                    }

                    SnapPointId nodalPointSnapPointId =
                        new SnapPointId(
                            "PAYLOAD_"
                                + cameraDescription.getId()
                                + "@"
                                + iPayloadMount
                                + ":"
                                + iPayload
                                + "_NODAL_POINT");

                    // optical axis is X axis
                    Vec3 nodalPointOffset = new Vec3(lensDescription.getNodalPointShiftFromLensMount(), 0.0f, 0.0f);
                    SnapPointDescription nodalPointSnapPointDescription =
                        new SnapPointDescription(nodalPointSnapPointId, lensMountSnapPointId, nodalPointOffset, null);

                    KinematicSegment nodalPointKinematicSegment =
                        KinematicSegmentFactory.createFromSnapPointDescription(
                            nodalPointSnapPointDescription, lensMountKinematicSegment);
                    kinematicSegments.add(nodalPointKinematicSegment);
                } else {
                    throw new IllegalArgumentException(
                        "hardwareConfiguration: Unsupported payload type of "
                            + payloadConfiguration.getDescriptionId());
                }
            }
        }

        // Additional snap points:
        Map<SnapPointId, ISnapPointDescription> additionalSnapPoints = new HashMap<>();
        // defaults, might be overwritten:
        additionalSnapPoints.put(
            SnapPointId.WAYPOINT_POSITION_OUT,
            new SnapPointDescription(SnapPointId.WAYPOINT_POSITION_OUT, SnapPointId.BODY_ORIGIN, Vec3.zero(), null));
        additionalSnapPoints.put(
            SnapPointId.POSITION_TELEMETRY_IN,
            new SnapPointDescription(SnapPointId.POSITION_TELEMETRY_IN, SnapPointId.BODY_ORIGIN, Vec3.zero(), null));

        for (ISnapPointDescription snapPointDescription : platformDescription.getAdditionalSnapPoints()) {
            additionalSnapPoints.put(snapPointDescription.getId(), snapPointDescription);
        }

        for (ISnapPointDescription snapPointDescription : additionalSnapPoints.values()) {
            Optional<KinematicSegment> previous =
                kinematicSegments
                    .stream()
                    .filter(s -> s.getSnapPointId().equals(snapPointDescription.getAttachedToSnapPointId()))
                    .findFirst();
            if (previous.isEmpty()) {
                throw new IllegalArgumentException(
                    "Error in platform description: snap point "
                        + snapPointDescription.getId()
                        + " refers to unknown snap point ID "
                        + snapPointDescription.getAttachedToSnapPointId());
            }

            kinematicSegment =
                KinematicSegmentFactory.createFromSnapPointDescription(snapPointDescription, previous.get());
            kinematicSegments.add(kinematicSegment);
        }

        // GNSS antenna:
        String gnssAntennaId = hardwareConfiguration.getGnssAntennaId();
        Optional<GnssAntennaDescription> gnssAntennaDesc =
            gnssAntennaId != null
                ? platformDescription
                    .getCompatibleGnssAntennas()
                    .stream()
                    .filter(a -> a.getId().getName().equals(gnssAntennaId))
                    .findFirst()
                : Optional.empty();

        final GnssAntennaDescription gnssAntennaDescription =
            gnssAntennaDesc.orElseGet(
                () ->
                    new GnssAntennaDescription(
                        SnapPointId.GNSS_ANTENNA, SnapPointId.BODY_ORIGIN, Vec3.zero(), 0, GpsType.GPS));

        Optional<KinematicSegment> previous =
            kinematicSegments
                .stream()
                .filter(s -> s.getSnapPointId().equals(gnssAntennaDescription.getAttachedToSnapPointId()))
                .findFirst();
        if (previous.isEmpty()) {
            throw new IllegalArgumentException(
                "Error in payload mount description: gnss antenna refers to unknown snap point ID "
                    + gnssAntennaDescription.getAttachedToSnapPointId());
        }

        kinematicSegment = KinematicSegmentFactory.createFromGnssAntenna(gnssAntennaDescription, previous.get());
        kinematicSegments.add(kinematicSegment);

        // create kinematic chain:
        KinematicChain res = new KinematicChain(kinematicSegments);

        // ensure the body origin and gnss antenna exist and there's a path between them:
        KinematicChainState state = new KinematicChainState(res, res.getDefaultGimbalStateVector());
        state.getTransformPath(SnapPointId.BODY_ORIGIN, SnapPointId.GNSS_ANTENNA);

        return res;
    }

}
