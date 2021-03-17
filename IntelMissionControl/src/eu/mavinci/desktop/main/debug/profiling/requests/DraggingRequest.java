/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import eu.mavinci.desktop.main.debug.profiling.MRequest;
import gov.nasa.worldwind.geom.Position;

public class DraggingRequest extends MRequest {

    Object draggedObject;
    Position posTo;

    public DraggingRequest(Object draggedObject, Position posTo) {
        super(50L, 10);
        this.draggedObject = draggedObject;
        this.posTo = posTo;
    }

    @Override
    public String toString() {
        return super.toString() + "\nDraggingRequest{draggedObject=" + draggedObject + ";posTo=" + posTo + "}\n";
    }

    static long slowest = 0;
    static long noSampled = 0;

    @Override
    public synchronized boolean isSlowestUpToNow(long duration) {
        if (duration > slowest) {
            slowest = duration;
            return true;
        }

        return false;
    }

    public synchronized void sampleThis() {
        noSampled++;
    }

    @Override
    public long getCountUpToNow() {
        return noSampled;
    }

}
