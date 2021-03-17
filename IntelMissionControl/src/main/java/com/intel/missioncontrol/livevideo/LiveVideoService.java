/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.livevideo;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveVideoService implements ILiveVideoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveVideoService.class);

    private static AtomicInteger liveVideoServiceThreadCounter = new AtomicInteger(0);

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

    private final Thread runner = new Thread(new MinAvRunner(), "LiveVideoService-MinAvRunner-" + liveVideoServiceThreadCounter.incrementAndGet());

    // TODO: this might connection/videostream mapping might belong somewhere else and should be checked for
    // race-conditions
    //    private final Map<IReadOnlyConnectionItem, Set<LiveVideoStream>> connectionStreamMap = new HashMap<>();

    private class StreamListHelper {
        Map<IReadOnlyConnectionItem, ConnectionItem> connectionItems = new HashMap<>();

        private class CameraItem {
            final ConnectionItem parent;
            private final ReadOnlyAsyncListProperty<? extends IVideoStream> streamsProperty;
            Map<IVideoStream, LiveVideoStream> streamItems = new HashMap<>();

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
                        deletedStream.release();
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
                for (LiveVideoStream stream: streamItems.values()) {
                    stream.release();
                }
                streamItems.clear();
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
                for (CameraItem item: cameraItems.values()) {
                    item.forget();
                }
                cameraItems.clear();
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
    public LiveVideoService(IDroneConnectionService droneConnectionService) {
        uiStreamList.bindContent(streamList);

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

        void init(int loglevel);

        Pointer start(String uri, int timeout);

        int get(Pointer ctx, Memory buf, int width, int height, int format);

        int get_state(Pointer ctx, byte[] buf, IntByReference size);

        int check(Pointer[] ctxs, int[] status, int count, int timeout);

        void stop(Pointer ctx);

        void deinit();
    }

    private class MinAvRunner implements Runnable {

        private final IMinAv minAvHandle = IMinAv.instance;
        private final int LOOP_SLEEP_MS = 50;
        private final int MAX_PROTOBUF_SIZE = 4096;
        private final byte[] protobuf = new byte[MAX_PROTOBUF_SIZE];
        private final IntByReference pbSize = new IntByReference();

        @Override
        public void run() {
            minAvHandle.init(MinAvProtobuf.LogLevel.QUIET_VALUE);

            while (!Thread.interrupted()) {
                Collection<LiveVideoStream> streamsToActivate = new ArrayList<>();
                Collection<LiveVideoStream> streamsToStop = new ArrayList<>();
                List<LiveVideoStream> streamsToPoll = new ArrayList<>(); // we need it to be ordered
                try (LockedList<LiveVideoStream> streams = streamList.lock()) {
                    Iterator<LiveVideoStream> streamIterator = streams.iterator();
                    while (streamIterator.hasNext()) {
                        LiveVideoStream stream = streamIterator.next();
                        switch (stream.getAction()) {
                            case ACTIVATE:
                                streamsToActivate.add(stream);
                                break;
                            case DEACTIVATE:
                                streamsToStop.add(stream);
                                break;
                            case POLL:
                                streamsToPoll.add(stream);
                                break;
                            case REMOVE:
                                streamsToStop.add(stream);
                                streamIterator.remove();
                        }
                    }
                }

                for (LiveVideoStream stream : streamsToActivate) {
                    final LiveVideoStream.StartSettings settings = stream.getStartSettings();
                    final Pointer minavCtx = minAvHandle.start(settings.uri, settings.timeout);

                    if (minavCtx != Pointer.NULL) {
                        stream.setMinAvCtx(minavCtx);
                        stream.setActive(true);
                    } else {
                        throw new RuntimeException("MinAv context couldn't be created");
                    }
                }

                for (LiveVideoStream stream : streamsToStop) {
                    final Pointer minavCtx = stream.getMinAvCtx();
                    minAvHandle.stop(minavCtx);
                    stream.setActive(false);
                }

                if (!streamsToPoll.isEmpty()) {
                    try {
                        pollStreams(streamsToPoll);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }

                if (streamsToActivate.isEmpty() && streamsToPoll.isEmpty() && streamsToStop.isEmpty()) {
                    try {
                        Thread.sleep(LOOP_SLEEP_MS);
                    } catch (InterruptedException ignore) {

                    }
                }
            }

            minAvHandle.deinit();
        }

        private void pollStreams(List<LiveVideoStream> streams) throws InvalidProtocolBufferException {
            Pointer[] actCtxs = streams.stream().map(LiveVideoStream::getMinAvCtx).toArray(Pointer[]::new);

            int[] statusCtxs = new int[actCtxs.length];

            int newDataCount = minAvHandle.check(actCtxs, statusCtxs, actCtxs.length, LOOP_SLEEP_MS);

            // check for new data
            if (newDataCount == 0) return;
            for (int i = 0; i < actCtxs.length; ++i) {
                MinAvProtobuf.SelectStatus status = MinAvProtobuf.SelectStatus.forNumber(statusCtxs[i]);
                LiveVideoStream stream = streams.get(i);

                if (status == MinAvProtobuf.SelectStatus.NO_UPDATE) continue;

                boolean callGet = false;

                if (status == MinAvProtobuf.SelectStatus.STATE_UPDATE) {
                    pbSize.setValue(MAX_PROTOBUF_SIZE);
                    if (MinAvProtobuf.ReturnStatus.OK_VALUE != minAvHandle.get_state(actCtxs[i], protobuf, pbSize))
                        throw new InvalidProtocolBufferException("get_state() error: MAX_PROTOBUF_SIZE to small");
                    callGet = stream
                        .stateUpdate(
                            MinAvProtobuf.StateDescription.parseFrom(
                                ByteBuffer.wrap(protobuf, 0, pbSize.getValue())));
                } else if (status == MinAvProtobuf.SelectStatus.NEW_DATA) {
                    callGet = stream.newData();
                }

                if (callGet) {
                    LiveVideoStream.GetSettings getSettings = stream.getGetSettings();
                    minAvHandle.get(
                            stream.getMinAvCtx(),
                            getSettings.getMemory(),
                            getSettings.getWidth(),
                            getSettings.getHeight(),
                            getSettings.getFormat()
                    );
                }
            }
        }
    }

}
