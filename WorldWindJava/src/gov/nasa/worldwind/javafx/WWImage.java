/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.javafx;

import gov.nasa.worldwind.AbstractSceneController;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.event.NoOpInputHandler;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.ScheduledTaskService;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

public class WWImage extends WritableImage {

    private static class DummyScheduledTaskService implements ScheduledTaskService {
        @Override
        public void shutdown(boolean immediately) {}

        @Override
        public void addTask(Runnable runnable) {}

        @Override
        public ScheduledFuture<?> addScheduledTask(Runnable runnable, long delay, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public ScheduledFuture<?> addRepeatingTask(
                Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
            return null;
        }
    }

    private static class NoPickingSceneController extends AbstractSceneController {
        @Override
        protected void doRepaint(DrawContext dc) {
            this.applyView(dc);
            this.createTerrain(dc);
            this.preRender(dc);
            this.clearFrame(dc);
            this.draw(dc);
        }
    }

    public WWImage(int width, int height) {
        super(width, height);
    }

    public void snapshot(Model model, View view) {
        WWOffscreenDrawable wwd =
            new WWOffscreenDrawable(new DummyScheduledTaskService(), (int)getWidth(), (int)getHeight());
        wwd.setFrameRenderedHandler(this::frameRendered);
        //wwd.setInputHandler(new NoOpInputHandler());
        wwd.setSceneController(new NoPickingSceneController());
        wwd.initGpuResourceCache(WWOffscreenDrawable.getSharedGpuResourceCache());
        wwd.setModelAndView(model, view);
        wwd.redraw();
        wwd.shutdownNow();
    }

    private void frameRendered(ByteBuffer frame, int width, int height) {
        final int stride = width * 4;
        byte[] imageBuffer = new byte[stride * height];
        int lineStart = stride * (height - 1);
        while (frame.hasRemaining()) {
            frame.get(imageBuffer, lineStart, stride);
            lineStart -= stride;
        }

        getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), imageBuffer, 0, stride);
    }

}
