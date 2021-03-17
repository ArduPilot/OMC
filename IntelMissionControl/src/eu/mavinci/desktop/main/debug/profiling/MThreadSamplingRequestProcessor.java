/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling;

import com.logicstyle.samplr.Request;
import com.logicstyle.samplr.RequestContext;
import com.logicstyle.samplr.RequestProcessor;
import com.logicstyle.samplr.ResultFile;
import com.logicstyle.samplr.ThreadSamplingRequestProcessor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot.NoDataAvailableException;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MThreadSamplingRequestProcessor extends RequestProcessor<ThreadSamplingRequestProcessor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MThreadSamplingRequestProcessor.class);

    private final SamplrMonitorThread samplrMonitorThread;
    private final long monitoringInterval = 500;

    private final long samplingInterval = 20;
    private final SamplingThread samplingThread;

    private final Map<RequestContext, SamplingRequestContext> ongoingRequests;
    private final Map<RequestContext, SamplingRequestContext> samplingRequests;
    private final Map<Long, StackTraceSnapshotBuilder> snapshotBuilders;
    private final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
    private long requestLengthSamplingThreshold;

    public MThreadSamplingRequestProcessor() {
        ongoingRequests = new ConcurrentHashMap<RequestContext, SamplingRequestContext>();
        samplingRequests = new ConcurrentHashMap<RequestContext, SamplingRequestContext>();
        snapshotBuilders = new ConcurrentHashMap<Long, StackTraceSnapshotBuilder>();
        samplingThread = new SamplingThread();
        samplrMonitorThread = new SamplrMonitorThread();
        samplingThread.start();
        samplrMonitorThread.start();
    }

    public MThreadSamplingRequestProcessor withRequestLengthSamplingThreshold(long requestLengthThreshold) {
        setRequestLengthSamplingThreshold(requestLengthThreshold);
        return this;
    }

    public long getRequestLengthSamplingThreshold() {
        return requestLengthSamplingThreshold;
    }

    public void setRequestLengthSamplingThreshold(long requestLengthSamplingThreshold) {
        this.requestLengthSamplingThreshold = requestLengthSamplingThreshold;
    }

    public void startMeasuring(RequestContext context) {
        startMonitoring(context);
    }

    public void stopMeasuring(RequestContext context) {
        SamplingRequestContext samplingContext = ongoingRequests.get(context);
        if (samplingContext == null) {
            return; // not monitoring this request
        }

        @SuppressWarnings("unchecked")
        List<ResultFile> resultList = Collections.EMPTY_LIST;

        try {
            if (samplingContext.isSampling()) {
                samplingRequests.remove(context);
                samplingContext.setSampleEndTime(System.currentTimeMillis());

                StackTraceSnapshotBuilder snapshotBuilder = snapshotBuilders.get(context.getRequest().getThreadId());
                if (snapshotBuilder == null) {
                    return;
                }

                snapshotBuilders.remove(context.getRequest().getThreadId());
                CPUResultsSnapshot snapshot;
                try {
                    snapshot = snapshotBuilder.createSnapshot(System.currentTimeMillis());
                } catch (NoDataAvailableException ex) {
                    // throw new RuntimeException(ex);
                    return; // not a real problem... just too short to get some samples... I GUESS !!
                }

                LoadedSnapshot ls =
                    new LoadedSnapshot(snapshot, ProfilingSettingsPresets.createCPUPreset(), null, null);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(bout);
                ls.save(out);
                out.flush();

                ResultFile samplingFile = new ResultFile();

                samplingFile.setName("request-sampling.nps");
                samplingFile.setContent(new ByteArrayInputStream(bout.toByteArray()));

                bout = new ByteArrayOutputStream();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(bout));
                pw.println("Sampling start time: " + new Date(samplingContext.getSampleStartTime()));
                pw.println("Sampling end time: " + new Date(samplingContext.getSampleEndTime()));
                pw.println(
                    "Sampling duration: "
                        + (samplingContext.getSampleEndTime() - samplingContext.getSampleStartTime()));
                pw.println(
                    "Request duration: "
                        + (samplingContext.getRequestContext().getRequest().getEndTime()
                            - samplingContext.getRequestContext().getRequest().getStartTime()));
                pw.flush();

                ResultFile infoFile = new ResultFile();
                infoFile.setName("sampling-info.txt");
                infoFile.setContent(new ByteArrayInputStream(bout.toByteArray()));

                resultList = Arrays.asList(new ResultFile[] {samplingFile, infoFile});

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

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            context.measurementFinished(context.getRequest(), this, resultList);
            ongoingRequests.remove(context);
        }
    }

    class SamplrMonitorThread extends Thread {

        public SamplrMonitorThread() {
            super("Samplr monitor thread");
            setDaemon(true);
        }

        private boolean keepRunning = true;

        @Override
        public void run() {
            while (keepRunning) {
                for (SamplingRequestContext r : ongoingRequests.values()) {
                    checkShouldSample(r);
                }

                try {
                    Thread.sleep(monitoringInterval);
                } catch (InterruptedException ex) {
                }
            }
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

        if (context.getRequestContext().getRequest() instanceof MRequest) {
            MRequest mRequest = (MRequest)context.getRequestContext().getRequest();
            if (mRequest.shouldMeasure()) {
                startSampling(context);
                return true;
            }
        } else if (requestLengthSamplingThreshold > 0
                && (System.currentTimeMillis() - context.getRequestContext().getRequest().getStartTime())
                    > requestLengthSamplingThreshold) {
            startSampling(context);
            return true;
        }

        return false;
    }

    class SamplingThread extends Thread {

        private final Object waitLock = new Object();
        private boolean keepRunning = true;

        public SamplingThread() {
            super("Samplr sampling thread");
            setDaemon(true);
        }

        public void wakeUp() {
            synchronized (waitLock) {
                waitLock.notifyAll();
            }
        }

        @Override
        public void run() {
            while (keepRunning) {
                if (samplingRequests.isEmpty()) {
                    synchronized (waitLock) {
                        try {
                            waitLock.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }

                long[] threadIds = new long[samplingRequests.size()];
                int i = 0;
                for (SamplingRequestContext context : samplingRequests.values()) {
                    Request request = context.getRequestContext().getRequest();

                    if (!request.isFinished()) {
                        threadIds[i] = request.getThreadId();
                        i++;
                        if (i > threadIds.length - 1) {
                            break;
                        }
                    }
                }

                if (i == 0) {
                    continue;
                }

                if (i < threadIds.length) {
                    // Unlikely, but thread can have been terminated between size() and values()
                    long[] tmp = threadIds;
                    threadIds = new long[i];
                    for (int j = 0; j < i; j++) {
                        threadIds[j] = tmp[j]; // HERE I HAVE APPLIED A FIX!!
                    }
                }

                ThreadInfo[] ti = mxBean.getThreadInfo(threadIds, Integer.MAX_VALUE);

                if (ti != null) {
                    for (ThreadInfo t : ti) {
                        if (t != null) {
                            StackTraceSnapshotBuilder builder = snapshotBuilders.get(t.getThreadId());
                            if (builder != null) // builder is null if thread has finished
                            {
                                builder.addStacktrace(new ThreadInfo[] {t}, System.nanoTime());
                            }
                        }
                    }
                }
            }

            try {
                Thread.sleep(samplingInterval);
            } catch (InterruptedException ex) {
            }
        }
    }

    void startSampling(SamplingRequestContext context) {
        samplingRequests.put(context.getRequestContext(), context);
        snapshotBuilders.put(context.getRequestContext().getRequest().getThreadId(), new StackTraceSnapshotBuilder());
        context.setSampleStartTime(System.currentTimeMillis());
        samplingThread.wakeUp();
    }
}
