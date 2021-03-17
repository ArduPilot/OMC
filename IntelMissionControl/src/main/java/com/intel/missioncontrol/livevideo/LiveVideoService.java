/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.livevideo;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.drone.ICamera;
import com.intel.missioncontrol.drone.IVideoStream;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
import com.intel.missioncontrol.ui.livevideo.IUILiveVideoStream;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveVideoService implements ILiveVideoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveVideoService.class);

    private final AsyncListProperty<LiveVideoStream> streamList =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<LiveVideoStream>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncListProperty<IUILiveVideoStream> uiStreamList =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IUILiveVideoStream>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final Collection<LiveVideoStream> streamsToRelease = new Vector<>();

    private final Thread runner = new Thread(new MinAvRunner());

    // TODO: this might connection/videostream mapping might belong somewhere else and should be checked for
    // race-conditions
    //    private final Map<IReadOnlyConnectionItem, Set<LiveVideoStream>> connectionStreamMap = new HashMap<>();

    private class StreamListHelper {
        Map<IReadOnlyConnectionItem, ConnectionItem> connectionItems = new HashMap<>();

        private class CameraItem {
            final ConnectionItem parent;
            private final ReadOnlyAsyncListProperty<? extends IVideoStream> streamsProperty;
            Map<IVideoStream, LiveVideoStream> streamItems = new HashMap<>();
            final AsyncProperty<IVideoStream> stream =
                new SimpleAsyncObjectProperty<>(
                    this, new PropertyMetadata.Builder<IVideoStream>().initialValue(null).create());
            LiveVideoStream liveVideoStream = null;

            CameraItem(ICamera camera, ConnectionItem connectionItem) {
                parent = connectionItem;
                streamsProperty = camera.videoStreamsProperty();
                try (LockedList<? extends IVideoStream> initialItems = streamsProperty.lock()) {
                    for (IVideoStream item : initialItems) {
                        streamItems.put(item, buildAndInsertLiveVideoStream(item));
                    }

                    streamsProperty.addListener(this::streamListChangeHandler);
                } catch (Exception e) {
                    throw new RuntimeException("Couldn't lock on streamsProperty");
                }
            }

            private void streamListChangeHandler(ListChangeListener.Change<? extends IVideoStream> change) {
                while (change.next()) {
                    for (IVideoStream item : change.getAddedSubList()) {
                        streamItems.put(item, buildAndInsertLiveVideoStream(item));
                    }

                    for (IVideoStream item : change.getRemoved()) {
                        final LiveVideoStream deletedStream = streamItems.remove(item);
                        streamList.remove(deletedStream);
                    }
                }
            }

            private LiveVideoStream buildAndInsertLiveVideoStream(IVideoStream videoStream) {
                LiveVideoStream liveVideoStream = new LiveVideoStream(videoStream);
                String name = videoStream.getStreamName();
                name += " -- " + parent.connection.getName();
                liveVideoStream.setDescription(name);
                streamList.add(liveVideoStream);
                return liveVideoStream;
            }

            void forget() {
                streamsProperty.removeListener(this::streamListChangeHandler);
                streamItems.entrySet().removeIf(
                        e -> {
                            streamList.remove(e.getValue());
                            return true;
                        });
            }
        }

        private class ConnectionItem {
            IReadOnlyConnectionItem connection;

            private final ReadOnlyAsyncListProperty<? extends ICamera> camerasProperty;
            Map<ICamera, CameraItem> cameraItems = new HashMap<>();

            ConnectionItem(IReadOnlyConnectionItem connection) {
                this.connection = connection;
                camerasProperty = droneConnectionService.getConnectedDrone(connection).camerasProperty();
                // TODO: is there any better work-around for this initial reading?
                try (LockedList<? extends ICamera> initialItems = camerasProperty.lock()) {
                    for (ICamera item : initialItems) {
                        cameraItems.put(item, new CameraItem(item, this));
                    }

                    camerasProperty.addListener(this::cameraListChangeHandler);
                } catch (Exception e) {
                    throw new RuntimeException("Couldn't lock on camerasProperty");
                }
            }

            void forget() {
                camerasProperty.removeListener(this::cameraListChangeHandler);
                cameraItems
                    .entrySet()
                    .removeIf(
                        e -> {
                            e.getValue().forget();
                            return true;
                        });
            }

            private void cameraListChangeHandler(ListChangeListener.Change<? extends ICamera> change) {
                while (change.next()) {
                    for (ICamera item : change.getAddedSubList()) {
                        cameraItems.put(item, new CameraItem(item, this));
                    }

                    for (ICamera item : change.getRemoved()) {
                        cameraItems.remove(item).forget();
                    }
                }
            }
        }

        private final IDroneConnectionService droneConnectionService;

        StreamListHelper(IDroneConnectionService droneConnectionService) {
            this.droneConnectionService = droneConnectionService;
            droneConnectionService
                .connectedDroneConnectionItemsProperty()
                .addListener(this::connectionListChangeHandler);
        }

        private void connectionListChangeHandler(ListChangeListener.Change<? extends IReadOnlyConnectionItem> change) {
            while (change.next()) {
                for (IReadOnlyConnectionItem item : change.getAddedSubList()) {
                    connectionItems.put(item, new ConnectionItem(item));
                }

                for (IReadOnlyConnectionItem item : change.getRemoved()) {
                    connectionItems.remove(item).forget();
                }
            }
        }

    }

    private final StreamListHelper streamListHelper;

    @Inject
    public LiveVideoService(IPathProvider pathProvider, IDroneConnectionService droneConnectionService) {
        final Path sdpFilePath = pathProvider.getSettingsDirectory().resolve("livevideo.sdp");
        if (Files.exists(sdpFilePath)) {
            sdpFilePath.toFile().delete();
        }
        // we need to create it
        File sdpFile = sdpFilePath.toFile();

        String sdpConfig =
            String.join(
                "\n",
                "v=0",
                "o=grayhawk 0 0 IN IP4 127.0.0.1",
                "s=Blackfly S BFS-U3-200S6C live view stream",
                "c=IN IP4 224.1.1.1",
                "t=0 0",
                "m=video 1234 RTP/AVP 96",
                "a=rtpmap:96 H264/90000",
                "a=fmtp:96 profile-level-id=640020; sprop-parameter-sets=Z2QAIKzSAVgc2X/8AAQABEAAAAMAQAAADzgAAAtxsAACJVFve9yg\\,aM48MA\\=\\=");

        try {
            FileWriter sdpConfigWriter = new FileWriter(sdpFile);
            sdpConfigWriter.write(sdpConfig);
            sdpConfigWriter.close();
        } catch (IOException e) {
            LOGGER.error("cant write SDP file: " + sdpFile, e);
        }

        uiStreamList.bindContent(streamList);

        LiveVideoStream localhostStream = new LiveVideoStream(null);
        localhostStream.setDescription("Example local stream");
        localhostStream.setUri(sdpFilePath.toString());
        streamList.add(localhostStream);

        streamListHelper = new StreamListHelper(droneConnectionService);

        runner.start();
    }

    @Override
    public ReadOnlyAsyncListProperty<IUILiveVideoStream> streamListProperty() {
        return uiStreamList;
    }

    @Override
    public void shutdown() {
        runner.interrupt();
    }

    interface IMinAv extends Library {
        IMinAv instance = Native.load("minav", IMinAv.class);

        int PIXFMT_BGRA = 1; // FIMXE this needs to stay in sync with minav.h
        int LOGLVL_QUIET = 0;
        int LOGLVL_INFO = 1;
        int LOVLVL_VERBOSE = 2;

        void init(int loglevel);

        Pointer start(String uri, int timeout);

        int get(Pointer ctx, Memory buf, int width, int height, int format);

        int get_dimension(Pointer ctx, IntByReference width, IntByReference height);

        int check(Pointer[] ctxs, int[] status, int count, int timeout);

        void stop(Pointer ctx);

        void deinit();
    }

    private class MinAvRunner implements Runnable {

        private final IMinAv minAvHandle = IMinAv.instance;

        private final int LOOP_SLEEP_MS = 50;

        @Override
        public void run() {
            minAvHandle.init(IMinAv.LOGLVL_INFO);

            while (!Thread.interrupted()) {
                Collection<LiveVideoStream> streamsToActivate = new ArrayList<>();
                Collection<LiveVideoStream> streamsToDisable = new ArrayList<>();
                List<LiveVideoStream> streamsToPoll = new ArrayList<>(); // we need it to be ordered
                try (LockedList<LiveVideoStream> streams = streamList.lock()) {
                    for (LiveVideoStream stream : streams) {
                        switch (stream.getAction()) {
                        case ACTIVATE:
                            streamsToActivate.add(stream);
                            break;
                        case DEACTIVATE:
                            streamsToDisable.add(stream);
                            break;
                        case POLL:
                            streamsToPoll.add(stream);
                            break;
                        }
                    }
                }

                for (LiveVideoStream stream : streamsToActivate) {
                    stream.activate(minAvHandle);
                }

                for (LiveVideoStream stream : streamsToDisable) {
                    stream.stop();
                }

                if (!streamsToPoll.isEmpty()) pollStreams(streamsToPoll);

                synchronized (streamsToRelease) {
                    for (LiveVideoStream stream : streamsToRelease) {
                        stream.release();
                    }

                    streamsToRelease.clear();
                }

                if (streamsToActivate.isEmpty() && streamsToPoll.isEmpty() && streamsToDisable.isEmpty()) {
                    try {
                        Thread.sleep(LOOP_SLEEP_MS);
                    } catch (InterruptedException ignore) {

                    }
                }
            }

            minAvHandle.deinit();
        }

        private void pollStreams(List<LiveVideoStream> streams) {
            // do get on every thing to pass its own stuff
            for (LiveVideoStream stream : streams) {
                stream.request();
            }

            // do check on all waiting ones
            List<LiveVideoStream> activeStreams =
                streams.stream().filter(LiveVideoStream::isRequestActive).collect(Collectors.toList());

            Pointer[] actCtxs = activeStreams.stream().map(LiveVideoStream::getMinavCtx).toArray(Pointer[]::new);

            int[] statusCtxs = new int[actCtxs.length];

            int newDataCount = minAvHandle.check(actCtxs, statusCtxs, actCtxs.length, LOOP_SLEEP_MS);

            // check for new data
            if (newDataCount == 0) return;
            for (int i = 0; i < actCtxs.length; ++i) {
                if (statusCtxs[i] != 0) activeStreams.get(i).feed();
            }
        }
    }

}
