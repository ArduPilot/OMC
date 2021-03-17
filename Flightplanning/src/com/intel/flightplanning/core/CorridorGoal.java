/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

public class CorridorGoal extends Goal {
    private int corridorMinLines = 3;
    private float corridorWidthInMeter;

    CorridorGoal(Builder b) {
        super(b);
        this.corridorMinLines = b.corridorMinLines;
        this.corridorWidthInMeter = b.corridorWidthInMeter;
    }

    public int getCorridorMinLines() {
        return corridorMinLines;
    }

    public float getCorridorWidthInMeter() {
        return corridorWidthInMeter;
    }

    public static class Builder extends Goal.Builder<Builder> {
        protected int corridorMinLines;
        protected float corridorWidthInMeter;

        public Builder setCorridorMinLines(int corridorMinLines) {
            this.corridorMinLines = corridorMinLines;
            return this;
        }

        public Builder setCorridorWidthInMeter(float corridorWidthInMeter) {
            this.corridorWidthInMeter = corridorWidthInMeter;
            return this;
        }

        @Override
        public Goal createGoal() {
            return new CorridorGoal(this);
        }

    }

}
