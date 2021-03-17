/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.intel.missioncontrol.geometry.EulerAngles;
import com.intel.missioncontrol.geometry.Vec3;
import com.intel.missioncontrol.geospatial.Convert;
import com.intel.missioncontrol.geospatial.Position;
import com.intel.missioncontrol.project.hardware.DescriptionProvider;
import com.intel.missioncontrol.project.hardware.GenericCameraConfiguration;
import com.intel.missioncontrol.project.hardware.HardwareConfiguration;
import com.intel.missioncontrol.project.hardware.IDescriptionProvider;
import com.intel.missioncontrol.project.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.project.hardware.LensConfiguration;
import com.intel.missioncontrol.project.hardware.PayloadMountConfiguration;
import com.intel.missioncontrol.project.hardware.SnapPointId;
import com.intel.missioncontrol.project.hardware.kinematics.provider.IKinematicChainFactory;
import com.intel.missioncontrol.project.hardware.kinematics.provider.KinematicChainFactory;
import com.intel.missioncontrol.project.hardware.kinematics.provider.KinematicsProvider;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KinematicsProviderTest {

    private IKinematicsProvider kinematicsProvider;
    private IDescriptionProvider descriptionProvider;

    private IKinematicChain kinematicChain;
    private IPayloadKinematicChain payloadKinematicChain;

    private static Injector initInjector() {
        final Set<Module> modules = new HashSet<>();

        String project = new File("").getAbsolutePath();
        Path descriptions = Path.of(project, "\\src\\test\\resources\\com\\intel\\missioncontrol\\descriptions");
        DescriptionProvider descriptionProvider = new DescriptionProvider(descriptions);
        Assertions.assertNotEquals(0, descriptionProvider.getPlatformDescriptions().size());

        modules.add(
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(IKinematicChainFactory.class).to(KinematicChainFactory.class).in(Singleton.class);
                    bind(IKinematicsProvider.class).to(KinematicsProvider.class).in(Singleton.class);
                    bind(IDescriptionProvider.class).toInstance(descriptionProvider);
                }
            });

        return Guice.createInjector(modules);
    }

    @BeforeEach
    void init() {
        Injector injector = initInjector();

        descriptionProvider = injector.getInstance(IDescriptionProvider.class);
        kinematicsProvider = injector.getInstance(IKinematicsProvider.class);

        IHardwareConfiguration hardwareConfiguration = createTestHardwareConfiguration();

        // The full kinematic chain of the hardware configuration:
        kinematicChain = kinematicsProvider.getOrCreateKinematicChain(hardwareConfiguration);

        // The partial kinematic chain of the hardware configuration from the payload's nodal point to the drone body
        // and antenna:
        payloadKinematicChain = kinematicsProvider.getOrCreatePayloadKinematicChain(hardwareConfiguration, 0, 0);
    }

    private IHardwareConfiguration createTestHardwareConfiguration() {
        String lensId = "TestCameraLens";
        String cameraId = "TestCamera";
        String platformId = "TestDrone";

        if (descriptionProvider.getLensDescriptionById(lensId) == null) {
            Assertions.fail("Invalid lens ID: " + lensId);
        }

        if (descriptionProvider.getPlatformDescriptionById(platformId) == null) {
            Assertions.fail("Invalid platform ID: " + platformId);
        }

        if (descriptionProvider.getGenericCameraDescriptionById(cameraId) == null) {
            Assertions.fail("Invalid camera ID: " + cameraId);
        }

        LensConfiguration lens = new LensConfiguration();
        lens.descriptionIdProperty().set(lensId);

        GenericCameraConfiguration cameraConfiguration = new GenericCameraConfiguration();
        cameraConfiguration.descriptionIdProperty().set(cameraId);
        cameraConfiguration.payloadSnapPointIdProperty().set("CamSnapPoint");
        cameraConfiguration.lensProperty().set(lens);

        PayloadMountConfiguration mountConfiguration = new PayloadMountConfiguration();
        mountConfiguration.descriptionIdProperty().set("TestGimbal");
        mountConfiguration.getPayloads().add(cameraConfiguration);

        HardwareConfiguration hardwareConfiguration = new HardwareConfiguration();
        hardwareConfiguration.descriptionIdProperty().set(platformId);
        hardwareConfiguration.getPayloadMounts().add(mountConfiguration);
        hardwareConfiguration.gnssAntennaIdProperty().set("Antenna1");

        return hardwareConfiguration;
    }

    @Test
    void Correct_GimbalStateVector_Is_Calculated_From_Payload_Orientation_And_Drone_Attitude() {

        // Given drone body attitude in earth frame: (see
        // https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft) )
        EulerAngles droneAttitude = new EulerAngles(10, 20, -5);

        // Given payload orientation in earth frame, with arbitrary yaw:
        EulerAngles payloadOrientation = new EulerAngles(Double.NaN, 30, 0);

        // Run solver:
        GimbalStateSolution gimbalStateSolution =
            payloadKinematicChain.solveForEarthFramePayloadOrientation(
                payloadOrientation, droneAttitude, kinematicChain.getDefaultGimbalStateVector());

        if (!gimbalStateSolution.exactSolutionFound()) {
            Assertions.fail("No gimbal state matching payload orientation found");
        }

        // Resulting joint angles of the full kinematic chain:
        GimbalStateVector gimbalStateVector = (GimbalStateVector)gimbalStateSolution.getStateVector();

        double tolerance = 1e-3;
        Assertions.assertArrayEquals(new double[] {5.0, 10.0}, gimbalStateVector.getAnglesDeg(), tolerance);
    }

    @Test
    void Correct_GnssAntenna_Offset_Is_Calculated_From_Payload_Orientation_And_Gimbal_State_Vector() {

        // Given gimbal state vector, values correspond to gimbal joint angles along the kinematic chain, in degrees,
        // ordered from drone body to payload:
        GimbalStateVector gimbalStateVector =
            new GimbalStateVector(
                new double[] {0, -90}); // gimbal looking vertically down for horizontal drone body, with no roll

        // Set source to payload nodal point:
        SnapPointId sourceSnapPointId = payloadKinematicChain.getNodalPointSnapPointId();

        // Given payload orientation in earth frame:
        EulerAngles sourceOrientation = new EulerAngles(0, -90, 0); // payload looking down, oriented north

        // Set target to GNSS antenna position:
        SnapPointId targetSnapPointId = SnapPointId.GNSS_ANTENNA;

        Vec3 earthFrameOffset =
            AffineTransform.fromOrientation(sourceOrientation)
                .chain(
                    kinematicChain
                        .createState(gimbalStateVector)
                        .getTransformPath(sourceSnapPointId, targetSnapPointId))
                .inverse()
                .getOffset();

        // check expected values:

        // drone body frame (= earth frame for zero drone attitude yaw/pitch/roll):
        // offset from camera nodal point, via lens mount snap point, CamSnapPoint, PitchAxisSnapPoint,
        // RollAxisSnapPoint to drone body origin, ant then to GNSS antenna.
        // The 90 degrees pitch joint angle affects all segments starting from the PitchAxisSnapPoint (x -> z, z -> -x).
        double dx =
            0.0 // lensMount.offset.z
                + 0.025 // payloadSnapPoint.offset.z;
                - 0.138 // pitchSegment.offset.x
                - 0.3164 // rollSegment.offset.x
                + 0.2; // antenna.offset.x;

        double dy = 0;
        double dz =
            -0.02 // nodalPointShiftFromLensMount
                - 0.1 // lensMount.offset.x
                - -0.001 // payloadSnapPoint.offset.x
                - 0.0 // pitchSegment.offset.z
                - 0.0035; // rollSegment.offset.z

        Vec3 expected = new Vec3(dx, dy, dz);

        Assertions.assertEquals(expected.x, earthFrameOffset.x, 1e-10);
        Assertions.assertEquals(expected.y, earthFrameOffset.y, 1e-10);
        Assertions.assertEquals(expected.z, earthFrameOffset.z, 1e-10);
    }

    @Test
    void Correct_GnssAntenna_Position_Is_Calculated_From_Payload_Pose_And_Gimbal_State_Vector() {

        // Given gimbal state vector, values correspond to gimbal joint angles along the kinematic chain, in degrees,
        // ordered from drone body to payload:
        GimbalStateVector gimbalStateVector =
            new GimbalStateVector(
                new double[] {0, 0}); // gimbal looking horizontal for horizontal drone body, with no roll

        // Source position given at payload nodal point:
        SnapPointId sourceSnapPointId = payloadKinematicChain.getNodalPointSnapPointId();

        // Given WGS84 position of the payload's nodal point:
        Position sourcePosition = Position.fromDegrees(0.000000, 0.000000, 0.0);

        // Given payload orientation in earth frame:
        EulerAngles sourceOrientation = new EulerAngles(0, 0, 0); // payload looking north, horizontally

        // Target position is GNSS antenna position:
        SnapPointId targetSnapPointId = SnapPointId.GNSS_ANTENNA;

        Position gnssAntennaPosition =
            kinematicsProvider.transformWGS84Position(
                kinematicChain.createState(gimbalStateVector),
                sourceSnapPointId,
                sourceOrientation,
                sourcePosition,
                targetSnapPointId);

        // check expected values:

        // drone body frame (= earth frame for zero drone attitude yaw/pitch/roll):
        // offset from camera nodal point, via lens mount snap point, CamSnapPoint, PitchAxisSnapPoint,
        // RollAxisSnapPoint to drone body origin, and then to GNSS antenna:
        double dx =
            -0.02 // nodalPointShiftFromLensMount
                - 0.1 // lensMount.offset.x
                - -0.001 // payloadSnapPoint.offset.x
                - 0.138 // pitchSegment.offset.x
                - 0.3164 // rollSegment.offset.x
                + 0.2; // antenna.offset.x;

        double dy = 0;
        double dz =
            -0.0 // lensMount.offset.z
                - 0.025 // payloadSnapPoint.offset.z;
                - 0.0 // pitchSegment.offset.z
                - 0.0035; // rollSegment.offset.z

        Position expected =
            Convert.ecefToGeodetic(
                Convert.geodeticToEcef(
                        sourcePosition.getLatitudeRadians(),
                        sourcePosition.getLongitudeRadians(),
                        sourcePosition.getElevation())
                    .add(new Vec3(dy, dx, -dz))); // earth frame: north,east,down / ecef: east,north,up

        Assertions.assertEquals(expected.getLatitude(), gnssAntennaPosition.getLatitude(), 1e-10);
        Assertions.assertEquals(expected.getLongitude(), gnssAntennaPosition.getLongitude(), 1e-10);
        Assertions.assertEquals(expected.getElevation(), gnssAntennaPosition.getElevation(), 1e-10);
    }

    @Test
    void Correct_Payload_Nodal_Point_Is_Calculated_From_GNSS_Antenna_Pose_And_Gimbal_State_Vector() {

        // Given gimbal state vector, values correspond to gimbal joint angles along the kinematic chain, in degrees,
        // ordered from drone body to payload:
        GimbalStateVector gimbalStateVector =
            new GimbalStateVector(
                new double[] {0, 0}); // gimbal looking horizontal for horizontal drone body, with no roll

        // Source position given at GNSS antenna:
        SnapPointId sourceSnapPointId = SnapPointId.GNSS_ANTENNA;

        // Given WGS84 position of the GNSS antenna:
        Position sourcePosition = Position.fromDegrees(0.000000, 0.000000, 0.0);

        // Given GNSS antenna orientation in earth frame:
        EulerAngles sourceOrientation = new EulerAngles(0, 0, 0); // payload looking north, horizontally

        // Target position is payload nodal point:
        SnapPointId targetSnapPointId = payloadKinematicChain.getNodalPointSnapPointId();

        Position payloadNodalPointPosition =
            kinematicsProvider.transformWGS84Position(
                kinematicChain.createState(gimbalStateVector),
                sourceSnapPointId,
                sourceOrientation,
                sourcePosition,
                targetSnapPointId);

        // check expected values:

        // drone body frame (= earth frame for zero drone attitude yaw/pitch/roll):
        // offset from GNSS antenna to drone body origin, and then via RollAxisSnapPoint,
        // PitchAxisSnapPoint, CamSnapPoint and lens mount snap point to the camera nodal point.
        //
        double dx =
            0.02 // nodalPointShiftFromLensMount
                + 0.1 // lensMount.offset.x
                + -0.001 // payloadSnapPoint.offset.x
                + 0.138 // pitchSegment.offset.x
                + 0.3164 // rollSegment.offset.x
                - 0.2; // antenna.offset.x;

        double dy = 0;
        double dz =
            0.0 // lensMount.offset.z
                + 0.025 // payloadSnapPoint.offset.z;
                + 0.0 // pitchSegment.offset.z
                + 0.0035; // rollSegment.offset.z

        Position expected =
            Convert.ecefToGeodetic(
                Convert.geodeticToEcef(
                        sourcePosition.getLatitudeRadians(),
                        sourcePosition.getLongitudeRadians(),
                        sourcePosition.getElevation())
                    .add(new Vec3(dy, dx, -dz))); // earth frame: north,east,down / ecef: east,north,up

        Assertions.assertEquals(expected.getLatitude(), payloadNodalPointPosition.getLatitude(), 1e-10);
        Assertions.assertEquals(expected.getLongitude(), payloadNodalPointPosition.getLongitude(), 1e-10);
        Assertions.assertEquals(expected.getElevation(), payloadNodalPointPosition.getElevation(), 1e-10);
    }

}
