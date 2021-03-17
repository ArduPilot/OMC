/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling;

import com.logicstyle.samplr.RequestContext;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;

public class SamplingRequestContext {

    private RequestContext requestContext;
    private StackTraceSnapshotBuilder snapshotBuilder;
    private long sampleStartTime;
    private long sampleEndTime;

    public SamplingRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public long getSampleEndTime() {
        return sampleEndTime;
    }

    public void setSampleEndTime(long sampleEndTime) {
        this.sampleEndTime = sampleEndTime;
    }

    public long getSampleStartTime() {
        return sampleStartTime;
    }

    public void setSampleStartTime(long sampleStartTime) {
        this.sampleStartTime = sampleStartTime;
    }

    public boolean isSampling() {
        return sampleStartTime > 0;
    }

    public StackTraceSnapshotBuilder getSnapshotBuilder() {
        return snapshotBuilder;
    }

    public void setSnapshotBuilder(StackTraceSnapshotBuilder snapshotBuilder) {
        this.snapshotBuilder = snapshotBuilder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final SamplingRequestContext other = (SamplingRequestContext)obj;
        if (this.requestContext != other.requestContext
                && (this.requestContext == null || !this.requestContext.equals(other.requestContext))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.requestContext != null ? this.requestContext.hashCode() : 0);
        return hash;
    }
}
