/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import java.security.InvalidParameterException;

/**
 * Defines the parameters for that a flight plan should have when being generated. This class should be considered
 * immutable.
 *
 * <p>For convenience a Builder is provided for generating a valid Goal instance. Classes that extend a Goal can
 * implement their own Builder that extends the Goal.Builder class. For an example, @see CorridorGoal.
 */
public class Goal {

    /** A defining feature is the overlap between images in flight direction and lateral to flight direction. */
    private float overlapInFlight = -1; // in %

    private float overlapInFlightMin = -1; // in %
    private float overlapInFlightMax = 1;
    private float overlapParallel = -1; // in %
    private float overlapParallelMin = -1;
    private float overlapParallelMax = 1;
    private float targetDistance;
    private float targetGSD;
    private float targetAlt;
    private float cropHeightMinEnabled;
    private float cropHeightMaxEnabled;
    private float minGroundDistance;
    private float cropHeightMin;
    private float cropHeightMax;
    private float minObjectDistance;
    private float maxObjectDistance;

    public Goal(
            float overlapInFlight,
            float overlapInFlightMin,
            float overlapParallel,
            float targetGSD,
            float targetAlt,
            float cropHeightMinEnabled,
            float cropHeightMaxEnabled,
            float minGroundDistance,
            float cropHeightMin,
            float cropHeightMax,
            float minObjectDistance,
            float maxObjectDistance,
            float targetDistance) {
        if (overlapInFlight < 0 || overlapInFlight > 100) {
            throw new InvalidParameterException("overlapInFlight needs to be in [0,100]");
        }

        if (overlapInFlightMin > 100) {
            throw new InvalidParameterException("overlapInFlightMin can not exceed 100 per-cent");
        }

        if (overlapParallel < 0 || overlapParallel > 100) {
            throw new InvalidParameterException("overlapParallel needs to be in [0,100]");
        }

        if (targetGSD <= 0f) {
            throw new InvalidParameterException("targetGSD needs to be positive non-zero");
        }

        if (targetDistance <= 0f) {
            throw new InvalidParameterException("targetDistance needs to be positive");
        }

        // a target altitude could potentially be negative when inspecting a structure from beneath
        // if(targetAlt < 0f) { //okay :) }

        if (minGroundDistance < 0f) {
            throw new InvalidParameterException("minGroundDistance needs to be positive");
        }

        if (maxObjectDistance < 0f) {
            throw new InvalidParameterException("minObjectDistance needs to be positive");
        }

        this.overlapInFlight = overlapInFlight;
        this.overlapInFlightMin = overlapInFlightMin;
        this.overlapParallel = overlapParallel;
        this.targetGSD = targetGSD;
        this.targetAlt = targetAlt;
        this.cropHeightMinEnabled = cropHeightMinEnabled;
        this.cropHeightMaxEnabled = cropHeightMaxEnabled;
        this.minGroundDistance = minGroundDistance;
        this.cropHeightMin = cropHeightMin;
        this.cropHeightMax = cropHeightMax;
        this.minObjectDistance = minObjectDistance;
        this.maxObjectDistance = maxObjectDistance;
        this.targetDistance = targetDistance;
    }

    /** To ensure that a goal is never in an invalid state */
    private Goal() {}

    Goal(Builder b) {
        if (b.overlapInFlight < 0 || b.overlapInFlight > 100) {
            throw new InvalidParameterException("overlapInFlight needs to be in [0,100]");
        }

        if (b.overlapInFlightMin > 100) {
            throw new InvalidParameterException("overlapInFlightMin can not exceed 100 per-cent");
        }

        if (b.overlapParallel < 0 || b.overlapParallel > 100) {
            throw new InvalidParameterException("overlapParallel needs to be in [0,100]");
        }

        if (b.targetGSD <= 0f) {
            throw new InvalidParameterException("targetGSD needs to be positive non-zero");
        }

        if (b.targetDistance <= 0f) {
            throw new InvalidParameterException("targetDistance needs to be positive");
        }

        // a target altitude could potentially be negative when inspecting a structure from beneath
        // if(targetAlt < 0f) { //okay :) }

        if (b.minGroundDistance < 0f) {
            throw new InvalidParameterException("minGroundDistance needs to be positive");
        }

        if (b.maxObjectDistance < 0f) {
            throw new InvalidParameterException("minObjectDistance needs to be positive");
        }

        this.overlapInFlight = b.overlapInFlight;
        this.overlapInFlightMin = b.overlapInFlightMin;
        this.overlapParallel = b.overlapParallel;
        this.targetGSD = b.targetGSD;
        this.targetAlt = b.targetAlt;
        this.cropHeightMinEnabled = b.cropHeightMinEnabled;
        this.cropHeightMaxEnabled = b.cropHeightMaxEnabled;
        this.minGroundDistance = b.minGroundDistance;
        this.cropHeightMin = b.cropHeightMin;
        this.cropHeightMax = b.cropHeightMax;
        this.minObjectDistance = b.minObjectDistance;
        this.maxObjectDistance = b.maxObjectDistance;
        this.targetDistance = b.targetDistance;
    }

    public float getOverlapInFlightMax() {
        return overlapInFlightMax;
    }

    public float getOverlapParallelMin() {
        return overlapParallelMin;
    }

    public float getOverlapParallelMax() {
        return overlapParallelMax;
    }

    public float getTargetDistance() {
        return targetDistance;
    }

    public float getOverlapInFlight() {
        return overlapInFlight;
    }

    public float getOverlapInFlightMin() {
        return overlapInFlightMin;
    }

    public float getOverlapParallel() {
        return overlapParallel;
    }

    public float getCropHeightMinEnabled() {
        return cropHeightMinEnabled;
    }

    public float getCropHeightMaxEnabled() {
        return cropHeightMaxEnabled;
    }

    public float getCropHeightMin() {
        return cropHeightMin;
    }

    public float getCropHeightMax() {
        return cropHeightMax;
    }

    public float getMinObjectDistance() {
        return minObjectDistance;
    }

    public float getMaxObjectDistance() {
        return maxObjectDistance;
    }

    public float getTargetGSD() {
        return targetGSD;
    }

    public float getTargetAlt() {
        return targetAlt;
    }

    public float getMinGroundDistance() {
        return minGroundDistance;
    }

    public static class Builder<T extends Builder<T>> {
        private float overlapInFlight;
        private float overlapInFlightMin = 0f;
        private float overlapParallel;
        private float targetGSD;
        private float targetAlt = -1f;
        private float targetDistance;
        private float cropHeightMinEnabled = -1f;
        private float cropHeightMaxEnabled = -1f;
        private float minGroundDistance = 0f;
        private float cropHeightMin = -1f;
        private float cropHeightMax = -1f;
        private float minObjectDistance = 0f;
        private float maxObjectDistance = 1e32f;

        public Builder setOverlapInFlight(float overlapInFlight) {
            this.overlapInFlight = overlapInFlight;
            return this;
        }

        public Builder setTargetDistance(float targetDistance) {
            this.targetDistance = targetDistance;
            return this;
        }

        public Builder setOverlapInFlightMin(float overlapInFlightMin) {
            this.overlapInFlightMin = overlapInFlightMin;
            return this;
        }

        public Builder setOverlapParallel(float overlapParallel) {
            this.overlapParallel = overlapParallel;
            return this;
        }

        public Builder setTargetGSD(float targetGSD) {
            this.targetGSD = targetGSD;
            return this;
        }

        public Builder setTargetAlt(float targetAlt) {
            this.targetAlt = targetAlt;
            return this;
        }

        public Builder setCropHeightMinEnabled(float cropHeightMinEnabled) {
            this.cropHeightMinEnabled = cropHeightMinEnabled;
            return this;
        }

        public Builder setCropHeightMaxEnabled(float cropHeightMaxEnabled) {
            this.cropHeightMaxEnabled = cropHeightMaxEnabled;
            return this;
        }

        public Builder setMinGroundDistance(float minGroundDistance) {
            this.minGroundDistance = minGroundDistance;
            return this;
        }

        public Builder setCropHeightMin(float cropHeightMin) {
            this.cropHeightMin = cropHeightMin;
            return this;
        }

        public Builder setCropHeightMax(float cropHeightMax) {
            this.cropHeightMax = cropHeightMax;
            return this;
        }

        public Builder setMinObjectDistance(float minObjectDistance) {
            this.minObjectDistance = minObjectDistance;
            return this;
        }

        public Builder setMaxObjectDistance(float maxObjectDistance) {
            this.maxObjectDistance = maxObjectDistance;
            return this;
        }

        public Goal createGoal() {
            return new Goal(
                overlapInFlight,
                overlapInFlightMin,
                overlapParallel,
                targetGSD,
                targetAlt,
                cropHeightMinEnabled,
                cropHeightMaxEnabled,
                minGroundDistance,
                cropHeightMin,
                cropHeightMax,
                minObjectDistance,
                maxObjectDistance,
                targetDistance);
        }
    }
}
