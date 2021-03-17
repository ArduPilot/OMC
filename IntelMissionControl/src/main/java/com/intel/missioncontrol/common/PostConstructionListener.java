/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.common;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.intel.missioncontrol.EnvironmentOptions;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PostConstructionListener implements TypeListener {

    public interface ProgressListener {
        void onProgress(double progress);
    }

    private class InjectionListener<T> implements com.google.inject.spi.InjectionListener<T> {
        @Override
        public void afterInjection(T instance) {
            ++count;

            if (EnvironmentOptions.INIT_OBJECT_COUNT <= 0) {
                System.out.println(PostConstructionListener.class.getSimpleName() + ".afterInjection{" + count + "}");
            } else {
                int newProgress =
                    (int)(Math.min(Math.max((double)count / (double)EnvironmentOptions.INIT_OBJECT_COUNT, 0), 1) * 100.0);

                if (newProgress > lastProgress) {
                    lastProgress = newProgress;

                    for (ProgressListener listener : progressListeners) {
                        listener.onProgress((double)newProgress / 100.0);
                    }
                }
            }

            for (Method method : instance.getClass().getDeclaredMethods()) {
                if (!method.isAnnotationPresent(PostConstruct.class)) {
                    continue;
                }

                try {
                    if (!method.canAccess(instance)) {
                        method.setAccessible(true);
                    }

                    method.invoke(instance);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static PostConstructionListener INSTANCE = new PostConstructionListener();

    private final List<ProgressListener> progressListeners = new ArrayList<>();
    private int count;
    private int lastProgress;

    private PostConstructionListener() {}

    public static PostConstructionListener getInstance() {
        return INSTANCE;
    }

    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    @Override
    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        typeEncounter.register(new InjectionListener<>());
    }

}
