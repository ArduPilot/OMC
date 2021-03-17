/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.drone.ICamera;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IVideoStream;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveVideoService implements ILiveVideoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveVideoService.class);

    private final BooleanProperty available = new SimpleBooleanProperty(true);

    private final AsyncListProperty<ILiveVideoStream> streamList = new SimpleAsyncListProperty<>(this);
    private final Collection<LiveVideoStream> streamsToRelease = new Vector<>();

    private ILiveVideoStream selectedStream = null;

    private final Thread runner = new Thread(new MinAvRunner());

    // TODO: this might connection/videostream mapping might belong somewhere else and should be checked for
    // race-conditions
    private final Map<IReadOnlyConnectionItem, Set<LiveVideoStream>> connectionStreamMap = new HashMap<>();

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

        this.streamList.set(FXAsyncCollections.observableArrayList());
        LiveVideoStream localhostStream = new LiveVideoStream(null);
        localhostStream.setDescription("Example local stream");
        localhostStream.setUri(sdpFilePath.toString());
        this.streamList.add(localhostStream);

        droneConnectionService
            .connectedDroneConnectionItemsProperty()
            .addListener(
                (ListChangeListener.Change<? extends IReadOnlyConnectionItem> change) -> {
                    while (change.next()) {
                        for (IReadOnlyConnectionItem connectionItem : change.getAddedSubList()) {
                            LOGGER.info("added: " + connectionItem);
                            IDrone drone = droneConnectionService.getConnectedDrone(connectionItem);
                            drone.camerasProperty()
                                .addListener(
                                    (ListChangeListener.Change<? extends ICamera> cameraChange) -> {
                                        while (cameraChange.next()) {
                                            for (ICamera camera : cameraChange.getAddedSubList()) {
                                                IVideoStream videoStream = camera.getVideoStream();
                                                if (videoStream == null) {
                                                    continue;
                                                }

                                                LOGGER.info(
                                                    "video: "
                                                        + videoStream.getStreamURI()
                                                        + " "
                                                        + videoStream.getStreamName());
                                                String name = videoStream.getStreamName();
                                                name += " -- " + connectionItem.getName();
                                                LiveVideoStream newStream = new LiveVideoStream(videoStream);
                                                newStream.setDescription(name);
                                                synchronized (connectionStreamMap) {
                                                    Set<LiveVideoStream> registeredStreams =
                                                        connectionStreamMap.getOrDefault(
                                                            connectionItem, new HashSet<>());
                                                    registeredStreams.add(newStream);
                                                    connectionStreamMap.putIfAbsent(connectionItem, registeredStreams);
                                                }

                                                streamList.add(newStream);
                                            }
                                            // TODO: I should also remove unregistering IVideoStreams
                                        }
                                    });
                        }

                        for (IReadOnlyConnectionItem connectionItem : change.getRemoved()) {
                            Set<LiveVideoStream> registeredStreams;
                            synchronized (connectionStreamMap) {
                                registeredStreams = connectionStreamMap.remove(connectionItem);
                            }

                            if (registeredStreams == null) continue;

                            removeRegisteredStreams(registeredStreams);
                        }
                    }
                });

        runner.start();
    }

    private void removeRegisteredStreams(Set<LiveVideoStream> removedStreams) {
        // NOTE: this function gets called asynchronously
        try (LockedList<ILiveVideoStream> lockedList = streamList.lock()) {
            lockedList.removeAll(removedStreams);
        }

        synchronized (streamsToRelease) {
            streamsToRelease.addAll(removedStreams);
        }
    }

    @Override
    public ReadOnlyAsyncListProperty<ILiveVideoStream> streamListProperty() {
        return streamList;
    }

    @Override
    public ReadOnlyBooleanProperty availableProperty() {
        return available;
    }

    @Override
    public ILiveVideoStream getSelectedStream() {
        return selectedStream;
    }

    public void setSelectedStream(ILiveVideoStream selectedStream) {
        this.selectedStream = selectedStream;
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
                try (LockedList<ILiveVideoStream> streams = streamList.lock()) {
                    // TODO, I don't want this explicit type conversion here!
                    for (ILiveVideoStream stream : streams) {
                        LiveVideoStream castStream = (LiveVideoStream)stream;
                        switch (castStream.getAction()) {
                        case ACTIVATE:
                            streamsToActivate.add(castStream);
                            break;
                        case DEACTIVATE:
                            streamsToDisable.add(castStream);
                            break;
                        case POLL:
                            streamsToPoll.add(castStream);
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
