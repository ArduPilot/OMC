/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.javafx;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicGpuResourceCache;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.util.Logging;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

class WWContext {

    private static final long FALLBACK_TEXTURE_CACHE_SIZE = 60000000;

    private static final WWContext INSTANCE = new WWContext();

    private final Map<Model, Integer> modelReferenceCount = new HashMap<>();
    private final GpuResourceCache sharedGpuResourceCache;

    private WWContext() {
        long cacheSize = Configuration.getLongValue(AVKey.TEXTURE_CACHE_SIZE, FALLBACK_TEXTURE_CACHE_SIZE);
        sharedGpuResourceCache = new BasicGpuResourceCache((long)(0.8 * cacheSize), cacheSize);
    }

    static WWContext getInstance() {
        return INSTANCE;
    }

    synchronized void addRef(Model model) {
        modelReferenceCount.merge(model, 1, (a, b) -> a + b);
    }

    synchronized void release(Model model) {
        Integer count = modelReferenceCount.get(model);
        if (count == null) {
            throw new IllegalStateException("Reference count is zero.");
        }

        modelReferenceCount.put(model, count - 1);

        if (count == 1) {
            // Dispose all the layers
            // TODO: Need per-window dispose for layers
            LayerList layers = model.getLayers();
            if (layers != null) {
                for (Layer layer : layers) {
                    try {
                        layer.dispose();
                    } catch (Exception e) {
                        Logging.logger()
                            .log(
                                Level.SEVERE,
                                Logging.getMessage("WorldWindowGLCanvas.ExceptionWhileShuttingDownWorldWindow"),
                                e);
                    }
                }
            }
        }
    }

    GpuResourceCache getSharedGpuResourceCache() {
        return sharedGpuResourceCache;
    }

}
