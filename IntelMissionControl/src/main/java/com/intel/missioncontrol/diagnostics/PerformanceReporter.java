/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import static com.sun.jna.platform.win32.WinNT.FILE_ATTRIBUTE_NORMAL;
import static com.sun.jna.platform.win32.WinNT.FILE_SHARE_READ;
import static com.sun.jna.platform.win32.WinNT.GENERIC_WRITE;
import static com.sun.jna.platform.win32.WinNT.OPEN_EXISTING;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import java.util.List;
import javafx.application.Platform;
import org.asyncfx.AsyncFX;
import org.asyncfx.beans.property.UIDispatcher;
import org.asyncfx.concurrent.Dispatcher;

public class PerformanceReporter {

    private static final String MAILSLOT_NAME = "imc-perfmon-2019-4-3";
    private static final WorldWindProfiler worldWindProfiler =
        WorldWindProfiler.getProfilers().stream().findFirst().orElseThrow();
    private static PerformanceReporter INSTANCE;

    private WinNT.HANDLE file;
    private Thread thread;
    private volatile boolean running = true;

    private PerformanceReporter() {
        ensureFile();
        thread = new Thread(this::run);
        thread.setDaemon(true);
        thread.start();
    }

    public static synchronized void start() {
        if (INSTANCE == null) {
            INSTANCE = new PerformanceReporter();
        }

        UIDispatcher.setDispatcher(Dispatcher.platform()::runLater, Platform::isFxApplicationThread);
    }

    public static synchronized void stop() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }

        UIDispatcher.setDispatcher(Platform::runLater, Platform::isFxApplicationThread);
    }

    private void run() {
        while (running) {
            ByteArrayDataOutput buffer = ByteStreams.newDataOutput();

            // Corresponds to DispatcherData in Perfmon
            buffer.writeInt(1); // DispatcherData.Id
            buffer.writeInt(AsyncFX.getThreadCount());
            buffer.writeInt(AsyncFX.getAsyncSubmitCount());
            buffer.writeInt(AsyncFX.getElidedAsyncSubmitCount());
            buffer.writeInt(AsyncFX.getPlatformSubmitCount());
            buffer.writeInt(AsyncFX.getPlatformSubmitMillis());
            buffer.writeInt(AsyncFX.getPlatformAwaitedCount());
            buffer.writeInt(AsyncFX.getPlatformAwaitedMillis());

            // Corresponds to SceneGraphData in Perfmon
            buffer.writeInt(2); // SceneGraphData.Id
            buffer.writeInt((int)PerformanceTracker.getSceneTracker().getInstantFPS());
            buffer.writeInt((int)PerformanceTracker.getSceneTracker().getInstantPulses());

            // Corresponds to WWData in Perfmon
            List<FrameInfo> frames = worldWindProfiler.getAndClearFrames();
            if (!frames.isEmpty()) {
                buffer.writeInt(3); // WWData.Id
                buffer.writeInt(
                    PerformanceTracker.getWWNode() != null ? PerformanceTracker.getWWNode().getDisplayedFrames() : 0);
                buffer.writeInt(frames.size());

                for (var frame : frames) {
                    buffer.writeBoolean(frame.isSegmentStart());
                    buffer.writeInt(frame.getDrawCalls());
                    buffer.writeInt(frame.getReadPixelsCount());
                    buffer.writeInt(frame.getRenderDurationMillis());
                    buffer.writeInt(frame.getFrameDurationMillis());
                    buffer.writeInt(frame.getTotalDurationMillis());
                    buffer.writeInt(frame.getRenderedFrames());
                    buffer.writeInt(frame.getAdditionalData().size());

                    for (var additionalData : frame.getAdditionalData()) {
                        write7BitEncodedInt(buffer, additionalData.getKey().length());
                        buffer.writeChars(additionalData.getKey());
                        write7BitEncodedInt(buffer, additionalData.getValue().length());
                        buffer.writeChars(additionalData.getValue());
                    }
                }
            }

            sendMessage(buffer.toByteArray());

            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void write7BitEncodedInt(ByteArrayDataOutput output, int value) {
        long v = Integer.toUnsignedLong(value);
        while (v >= 0x80) {
            output.writeByte((byte)(v | 0x80));
            v >>= 7;
        }

        output.writeByte((byte)v);
    }

    private void sendMessage(byte[] message) {
        if (!ensureFile()) {
            return;
        }

        IntByReference bytesWritten = new IntByReference();
        if (!Kernel32.INSTANCE.WriteFile(file, message, message.length, bytesWritten, null)) {
            Kernel32.INSTANCE.CloseHandle(file);
            file = null;
        }
    }

    private void close() {
        running = false;

        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }

        thread = null;

        if (file != null) {
            Kernel32.INSTANCE.CloseHandle(file);
        }
    }

    private boolean ensureFile() {
        if (file != null) {
            return true;
        }

        file =
            Kernel32.INSTANCE.CreateFile(
                "\\\\.\\mailslot\\" + MAILSLOT_NAME,
                GENERIC_WRITE,
                FILE_SHARE_READ,
                null,
                OPEN_EXISTING,
                FILE_ATTRIBUTE_NORMAL,
                null);

        if (Pointer.nativeValue(file.getPointer()) == -1) {
            file = null;
            return false;
        }

        return true;
    }

}
