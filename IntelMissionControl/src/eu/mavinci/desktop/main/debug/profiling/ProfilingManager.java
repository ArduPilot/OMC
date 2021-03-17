/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling;

import com.logicstyle.samplr.Request;
import com.logicstyle.samplr.RequestManager;
import com.logicstyle.samplr.RequestRecorderRequestProcessor;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.desktop.main.debug.IProfilingListener;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.core.IAppListener;
import eu.mavinci.desktop.main.debug.profiling.requests.AppCloseRequest;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfilingManager implements IProfilingManager, IAppListener {

    private static Logger LOGGER = LoggerFactory.getLogger(ProfilingManager.class);

    AppCloseRequest closeRequest;

    WeakListenerList<IProfilingListener> listeners = new WeakListenerList<>("ProfilingManager");

    public ProfilingManager(File profilingsFolder, boolean isEnabled) {
        enableProfiling = isEnabled;
        LOGGER.info("enableProfiling=" + enableProfiling);

        if (isEnabled) {
            requestManager =
                new RequestManager()
                    .withRequestProcessor(new MThreadSamplingRequestProcessor())
                    .withRequestProcessor(new RequestRecorderRequestProcessor())
                    .withResultsProcessor(new MFileResultsArchiver(profilingsFolder))
                    .withRequestTimeout(300000L);
        } else {
            requestManager =
                new RequestManager()
                    .withRequestProcessor(new MStartStopTimingRequestProcessor())
                    .withRequestTimeout(300000L);
        }

        requestManager.start();

        // to always profile app startup/closing
        Application.addApplicationListener(this);
    }

    static RequestManager requestManager;

    public boolean enableProfiling = false;
    public boolean uiReadyLoaded = false;

    public void requestStarting(Request request) {
        if (!uiReadyLoaded) {
            return;
        }

        requestManager.requestStarting(request);
        for (IProfilingListener listener : listeners) {
            listener.requestStarted(request);
        }
    }

    public void requestFinished(Request request) {
        if (!uiReadyLoaded) {
            return;
        }

        requestManager.requestFinished(request);
        for (IProfilingListener listener : listeners) {
            listener.requestFinished(request);
        }
    }

    @Override
    public boolean isActive() {
        return enableProfiling;
    }

    @Override
    public boolean appRequestClosing() {
        return true;
    }

    @Override
    public void appIsClosing() {
        closeRequest = new AppCloseRequest();
        requestStarting(closeRequest);
    }

    public void guiClosingDone() {
        if (closeRequest != null) {
            requestFinished(closeRequest);
        }

        enableProfiling = false;
        requestManager.shutdown();
    }

    @Override
    public void addListener(IProfilingListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IProfilingListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void guiReadyLoaded() {
        uiReadyLoaded = true;
    }

}
