/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;
import com.intel.missioncontrol.geometry.EulerAngles;
import com.intel.missioncontrol.geometry.IOrientation;
import com.intel.missioncontrol.project.hardware.SnapPointId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Kinematic model of a drone gimbal consisting of kinematic segments with hinge joints at unspecified angles. Multiple
 * payloads may be attached to multiple snap points within the chain.
 */
public class KinematicChain implements IKinematicChain {

    // map from snap point IDs to each kinematic segment that provides the corresponding snap point.
    private final Map<SnapPointId, KinematicSegment> kinematicSegmentMap;

    private final GimbalStateVector defaultGimbalStateVector;

    // target resolution of gimbal state vector solver
    private static final double TARGET_RESOLUTION_DEG = 0.5;
    private final GriddedAngularOptimizer griddedAngularOptimizer;

    // grid resolution of attitude constraints solver
    private static final double GRID_RESOLUTION_DEG = 0.5;
    private final StateVectorGrid attitudeSolverGrid;

    // List of controllable kinematic segments. Each entry corresponds to one entry of a GimbalStateVector, in the same
    // order.
    private final List<KinematicSegment> controllableSegments;

    public KinematicChain(List<KinematicSegment> kinematicSegments) {
        kinematicSegmentMap = new LinkedHashMap<>();

        for (KinematicSegment s : kinematicSegments) {
            if (kinematicSegmentMap.containsKey(s.getSnapPointId())) {
                throw new IllegalArgumentException("Duplicate snap point ID " + s.getSnapPointId());
            }

            kinematicSegmentMap.put(s.getSnapPointId(), s);
        }

        controllableSegments =
            kinematicSegments
                .stream()
                .filter(seg -> seg.getControllableSegmentIndex() >= 0)
                .collect(Collectors.toList());

        defaultGimbalStateVector = new GimbalStateVector(new double[getGimbalStateVectorLength()]);

        List<KinematicSegment> controllableSegments = getControllableSegments();

        List<Arc> constraintArcs =
            controllableSegments.stream().map(KinematicSegment::getConstraintArc).collect(Collectors.toList());

        // Span an n-dimensional grid over the full allowed range of all relevant gimbal angles
        attitudeSolverGrid = new StateVectorGrid(constraintArcs, GRID_RESOLUTION_DEG);

        griddedAngularOptimizer = new GriddedAngularOptimizer(constraintArcs);
    }

    /**
     * Given a snap point ID and a gimbal state vector, get a transform that allows to go from the drone body frame to a
     * snap point-referenced frame (e.g. payload snap point).
     */
    @Override
    public AffineTransform getTransformFromBody(SnapPointId payloadSnapPointId, IGimbalStateVector stateVector) {
        KinematicChainState state = createState(stateVector);
        return state.getTransformPath(SnapPointId.BODY_ORIGIN, payloadSnapPointId);
    }

    @Override
    public KinematicChainState createState(IGimbalStateVector gimbalStateVector) {
        if (!(gimbalStateVector instanceof GimbalStateVector)) {
            throw new IllegalArgumentException("Invalid state vector type");
        }

        return new KinematicChainState(this, (GimbalStateVector)gimbalStateVector);
    }

    /**
     * Determine the earth frame attitude constraints for a snap point's orientation, with a horizontal drone body and
     * default roll angle about the snap point's X axis.
     */
    @Override
    public AttitudeConstraints calculateAttitudeConstraintsAtDefaultRoll(SnapPointId snapPointId) {
        AffineTransform defaultOrientationTransform = getTransformFromBody(snapPointId, getDefaultGimbalStateVector());

        EulerAngles defaultOrientation =
            defaultOrientationTransform.getEulerAngles(AffineTransform.GimbalLockMode.YAW_ZERO);

        return calculateAttitudeConstraintsAtRollAngleDeg(snapPointId, defaultOrientation.getRollDeg());
    }

    /**
     * Determine the earth frame attitude constraints for a snap point assuming a horizontal drone body and the given
     * earth frame roll angle about the snap point's x axis.
     */
    @Override
    public AttitudeConstraints calculateAttitudeConstraintsAtRollAngleDeg(
            SnapPointId snapPointId, double earthFrameRollDeg) {
        if (!kinematicSegmentMap.containsKey(snapPointId)) {
            throw new IllegalArgumentException("Payload snap point " + snapPointId + " not found");
        }

        // Find approximation for attitude constraints by finding euler angles of optical axis transforms
        // corresponding to a grid over all combinations of state vector gimbal joint angles.
        Arc pitchRange = null;
        Map<GimbalStateVector, AffineTransform> orientationTransformsMap = getPayloadOrientationsMap(snapPointId);
        for (AffineTransform orientationTransform : orientationTransformsMap.values()) {
            EulerAngles eulerAngles = orientationTransform.getEulerAngles(AffineTransform.GimbalLockMode.NAN);

            // If roll is close to payloadRollDeg, extend arc range for pitch to include orientation.
            // Also, include if near gimbal lock (pitch close to +-90°, yaw and roll become indistinguishable).
            double rollDeg = eulerAngles.getRollDeg();
            double pitchDeg = eulerAngles.getPitchDeg();
            if (Double.isNaN(rollDeg)
                    || Arc.undirectedDistanceDeg(earthFrameRollDeg, rollDeg) <= GRID_RESOLUTION_DEG
                    || Arc.undirectedDistanceDeg(-90.0, pitchDeg) <= GRID_RESOLUTION_DEG
                    || Arc.undirectedDistanceDeg(90.0, pitchDeg) <= GRID_RESOLUTION_DEG) {
                double pitchRad = eulerAngles.getPitchRad();
                if (Double.isNaN(pitchRad)) {
                    continue;
                }

                if (pitchRange == null) {
                    pitchRange = new Arc(pitchRad, pitchRad);
                } else {
                    pitchRange = pitchRange.extendToInclude(pitchRad);
                }
            }
        }

        // Assume zero angle as default if undefined
        if (pitchRange == null) {
            pitchRange = new Arc(0.0, 0.0);
        }

        // round to resolution:
        double p1 = getClosestMultiple(pitchRange.getMinAngleDeg(), 2 * GRID_RESOLUTION_DEG);
        double p2 = getClosestMultiple(pitchRange.getMaxAngleDeg(), 2 * GRID_RESOLUTION_DEG);
        if (p1 > p2) {
            p2 = p1 + pitchRange.getArcLengthDeg();
        }

        pitchRange = Arc.fromAnglesDeg(p1, p2);

        return new AttitudeConstraints(pitchRange, earthFrameRollDeg);
    }

    private Map<GimbalStateVector, AffineTransform> getPayloadOrientationsMap(SnapPointId snapPointId) {
        Map<GimbalStateVector, AffineTransform> res = new HashMap<>();

        List<double[]> gimbalStateVectors = attitudeSolverGrid.getStateVectors();

        for (double[] stateVectorAnglesDeg : gimbalStateVectors) {
            GimbalStateVector v = new GimbalStateVector(stateVectorAnglesDeg);
            AffineTransform bodyFrameTransform = getTransformFromBody(snapPointId, v);
            res.put(v, bodyFrameTransform);
        }

        return res;
    }

    /**
     * Assuming a horizontal drone body, determine a gimbal state vector that results in the given payload pitch and
     * roll, and an arbitrary yaw. pitchDeg: 0: horizontal optical axis, -90: looking down vertically. rollDeg: payload
     * rotation about its optical axis, clockwise, in degrees.
     *
     * <p>payloadMountIndex and payloadIndex refer to a configured payload on a payload mount in the hardware *
     * configuration.
     *
     * <p>The resulting gimbal state vector is not necessarily unique. Preference is given to a solution near
     * referenceStateVector, or the default gimbal state vector (zero angles) if referenceStateVector is null.
     *
     * <p>Returns null if no solution is found.
     */
    public GimbalStateSolution solveForBodyFramePayloadPitchAndRoll(
            SnapPointId payloadSnapPointId, double pitchDeg, double rollDeg, IGimbalStateVector referenceStateVector) {
        return solveForBodyFramePayloadOrientation(
            payloadSnapPointId, new EulerAngles(Double.NaN, pitchDeg, rollDeg), referenceStateVector);
    }

    @Override
    public List<KinematicSegment> getControllableSegments() {
        return controllableSegments;
    }

    @Override
    public int getGimbalStateVectorLength() {
        return getControllableSegments().size();
    }

    /**
     * Assuming a horizontal drone body, determine a gimbal state vector that results in the given payload optical axis
     * orientation. Any yaw/pitch/roll angles that are NaN are ignored and taken to be arbitrary.
     *
     * <p>The resulting gimbal state vector is not necessarily unique. Preference is given to a solution near
     * referenceStateVector.
     *
     * <p>Returns null if no solution is found.
     */
    @Override
    public GimbalStateSolution solveForBodyFramePayloadOrientation(
            SnapPointId payloadSnapPointId, IOrientation payloadOrientation, IGimbalStateVector referenceStateVector) {
        return solveForEarthFramePayloadOrientation(
            payloadSnapPointId, payloadOrientation, EulerAngles.ZERO, referenceStateVector);
    }

    /**
     * Given a drone body orientation (attitude), determine a gimbal state vector that results in a payload optical axis
     * orientation as close to the given orientation as possible. Orientations are given in earth frame.
     * https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft)
     *
     * <p>If the yaw angle of the payload orientation is NaN, yaw is ignored and taken to be arbitrary.
     *
     * <p>The resulting gimbal state vector is not necessarily unique. Preference is given to a solution near
     * referenceStateVector.
     *
     * <p>
     */
    @Override
    public GimbalStateSolution solveForEarthFramePayloadOrientation(
            SnapPointId payloadSnapPointId,
            IOrientation payloadOrientation,
            IOrientation droneBodyOrientation,
            IGimbalStateVector referenceStateVector) {
        IGimbalStateVector ref = (referenceStateVector == null) ? getDefaultGimbalStateVector() : referenceStateVector;

        AffineTransform droneBodyTransform = AffineTransform.fromOrientation(droneBodyOrientation);
        AffineTransform payloadTargetTransform = AffineTransform.fromOrientation(payloadOrientation);

        boolean ignoreYaw = Double.isNaN(payloadOrientation.getYawDeg());

        Function<double[], Double> getDistanceDegFnc =
            stateVector -> {
                AffineTransform bodyFrameTransform =
                    getTransformFromBody(payloadSnapPointId, new GimbalStateVector(stateVector));
                AffineTransform earthFrameTransform = droneBodyTransform.chain(bodyFrameTransform);
                // rotational distance from target payload orientation to current payload orientation
                return earthFrameTransform.calculateRotationalDistanceDeg(payloadTargetTransform, ignoreYaw);
            };

        GriddedAngularOptimizer.Result res =
            griddedAngularOptimizer.optimize(
                gimbalStateVector -> {
                    double distanceDeg = getDistanceDegFnc.apply(gimbalStateVector);

                    if (distanceDeg > 10.0 * TARGET_RESOLUTION_DEG) {
                        return distanceDeg;
                    }

                    // if distance is small, also take into account distance from reference (with low weight):
                    // get distance (root mean square) to reference state vector:
                    double referenceDistanceDeg = ref.getDistanceMeasure(new GimbalStateVector(gimbalStateVector));

                    return distanceDeg + (TARGET_RESOLUTION_DEG / 360.0) * referenceDistanceDeg;
                },
                TARGET_RESOLUTION_DEG);

        GimbalStateVector solution = res != null ? new GimbalStateVector(res.getStateVectorDeg()) : null;
        solution = solution != null ? roundToResolution(solution, TARGET_RESOLUTION_DEG) : defaultGimbalStateVector;
        double d = getDistanceDegFnc.apply(solution.getAnglesDeg());

        return new GimbalStateSolution(solution, d, d <= TARGET_RESOLUTION_DEG);
    }

    /** Create a gimbal state vector from angles, limiting the angles to constraint ranges. */
    private GimbalStateVector createGimbalStateVector(double[] stateVectorAnglesDeg) {
        int n = stateVectorAnglesDeg.length;
        double[] res = new double[n];
        for (int i = 0; i < n; i++) {
            Arc constraintArc = controllableSegments.get(i).getConstraintArc();
            res[i] = constraintArc.limitToArcDeg(stateVectorAnglesDeg[i]);
        }

        return new GimbalStateVector(res);
    }

    /**
     * Round GimbalStateVector angles to the given numerical resolution, ensuring all angles stay within allowed limits.
     */
    private GimbalStateVector roundToResolution(
            GimbalStateVector stateVector, @SuppressWarnings("SameParameterValue") double resolutionDeg) {
        int n = stateVector.getAnglesDeg().length;
        double[] res = new double[n];
        for (int i = 0; i < n; i++) {
            double roundedAngleDeg = getClosestMultiple(stateVector.getAnglesDeg()[i], resolutionDeg);
            Arc constraintArc = controllableSegments.get(i).getConstraintArc();
            res[i] = constraintArc.limitToArcDeg(roundedAngleDeg);
        }

        return new GimbalStateVector(res);
    }

    /** return the multiple of base that is closest to value. */
    private static double getClosestMultiple(double value, double base) {
        return base * Math.round(value / base);
    }

    @Override
    public Map<SnapPointId, KinematicSegment> getKinematicSegmentMap() {
        return kinematicSegmentMap;
    }

    public GimbalStateVector getDefaultGimbalStateVector() {
        return defaultGimbalStateVector;
    }
}
