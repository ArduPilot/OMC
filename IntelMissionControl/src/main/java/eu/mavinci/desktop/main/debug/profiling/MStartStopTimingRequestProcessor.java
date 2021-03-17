/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling;

import com.logicstyle.samplr.RequestContext;
import com.logicstyle.samplr.RequestProcessor;
import com.logicstyle.samplr.ThreadSamplingRequestProcessor;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MStartStopTimingRequestProcessor extends RequestProcessor<ThreadSamplingRequestProcessor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MStartStopTimingRequestProcessor.class);

    private final Map<RequestContext, SamplingRequestContext> ongoingRequests;
    private final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

    public MStartStopTimingRequestProcessor() {
        ongoingRequests = new ConcurrentHashMap<RequestContext, SamplingRequestContext>();
    }

    public void startMeasuring(RequestContext context) {
        startMonitoring(context);
    }

    public void stopMeasuring(RequestContext context) {
        SamplingRequestContext samplingContext = ongoingRequests.get(context);
        if (samplingContext == null) {
            return; // not monitoring this request
        }

        try {
            if (samplingContext.isSampling()) {
                long startTime = samplingContext.getRequestContext().getRequest().getStartTime();
                long endTime = samplingContext.getRequestContext().getRequest().getEndTime();
                // printing always the duration
                LOGGER.info(
                    "PERFORMANCE Measuring: "
                        + context.getRequest().toString()
                        + " --> Request duration: "
                        + (endTime - startTime)
                        + " ms");
            }
        } finally {
            context.measurementFinished(context.getRequest(), this, Collections.EMPTY_LIST);
            ongoingRequests.remove(context);
        }
    }

    private void startMonitoring(RequestContext request) {
        SamplingRequestContext samplingContext = new SamplingRequestContext(request);
        ongoingRequests.put(request, samplingContext);
        checkShouldSample(samplingContext); // In case sampling for this case of request should begin immediately
    }

    private boolean checkShouldSample(SamplingRequestContext context) {
        if (context.isSampling()) {
            return false;
        }

        startSampling(context);
        return true;
    }

    void startSampling(SamplingRequestContext context) {
        context.setSampleStartTime(System.currentTimeMillis());
    }
}
