/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling;

import com.logicstyle.samplr.Request;
import eu.mavinci.core.obfuscation.IKeepClassname;

public abstract class MRequest extends Request implements IKeepClassname {

    long requestLengthSamplingThreshold;
    long maximalRequestToSample;

    public MRequest(long requestLengthSamplingThreshold, long maximalRequestToSample) {
        this.requestLengthSamplingThreshold = requestLengthSamplingThreshold;
        this.maximalRequestToSample = maximalRequestToSample;
    }

    public boolean shouldMeasure() {
        boolean ret =
            System.currentTimeMillis() - getStartTime() >= requestLengthSamplingThreshold
                && (maximalRequestToSample < 0 || getCountUpToNow() < maximalRequestToSample);
        if (ret) {
            sampleThis();
        }

        return ret;
    }

    public abstract void sampleThis();

    public abstract boolean isSlowestUpToNow(long duration);

    public abstract long getCountUpToNow();
}
