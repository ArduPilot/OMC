/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.local;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.sun.nio.file.ExtendedWatchEventModifier;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects changes to files contained within a directory. In recursive mode, changes are also detected for files
 * contained within subdirectories.
 *
 * <p>Note: In recursive mode, the implementation registers watchers for every subdirectory, and adds or removes these
 * watchers as the directory structure changes. When running on Windows, this can lead to problems because WatchService
 * opens a handle to the watched directory, which in some cases prevents users from making changes to the parent
 * directory. To overcome this problem, the Windows implementation uses an internal API (ExtendedWatchEventModifier) of
 * the jdk.unsupported module instead of registering watchers for every subdirectory.
 */
public class DirectoryWatcher implements AutoCloseable {

    public interface Handler {
        void fileAdded(Path path);

        void fileDeleted(Path path);

        void fileModified(Path path);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWatcher.class);
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final Cleaner cleaner = Cleaner.create();

    private final EphemeralSet<Path> recentlyModifiedFiles = new EphemeralSet<>(Duration.ofMillis(50));
    private final List<DirectoryWatcher> subWatchers = new ArrayList<>();
    private final Cleaner.Cleanable cleanable;
    private final Path path;
    private final Handler handler;
    private final boolean recursive;
    private WatchKey key;

    DirectoryWatcher(Path path, boolean recursive, Handler handler) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("'" + path + "' is not a directory.");
        }

        this.path = path;
        this.handler = handler;
        this.recursive = recursive;
        this.key = WatchServiceManager.getInstance().register(this, path, recursive);
        this.cleanable = cleaner.register(this, WatchServiceManager::releaseInstance);

        if (recursive && !WINDOWS) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        subWatchers.add(new DirectoryWatcher(entry, true, handler));
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public synchronized void close() {
        cleanable.clean();

        for (DirectoryWatcher subWatcher : subWatchers) {
            subWatcher.close();
        }
    }

    private WatchKey getKey() {
        return key;
    }

    private synchronized void handleEvents(List<WatchEvent<?>> events) {
        for (WatchEvent<?> event : events) {
            if (event.kind() == ENTRY_CREATE) {
                Path fullPath = path.resolve((Path)event.context());
                if (!WINDOWS && recursive && Files.isDirectory(fullPath)) {
                    subWatchers.add(new DirectoryWatcher(fullPath, true, handler));
                }

                handler.fileAdded(fullPath);
            } else if (event.kind() == ENTRY_DELETE) {
                Path fullPath = path.resolve((Path)event.context());
                ListIterator<DirectoryWatcher> it = subWatchers.listIterator();
                while (it.hasNext()) {
                    DirectoryWatcher subWatcher = it.next();
                    if (subWatcher.path.equals(fullPath)) {
                        subWatcher.close();
                        it.remove();
                        break;
                    }
                }

                handler.fileDeleted(fullPath);
            } else if (event.kind() == ENTRY_MODIFY) {
                Path fullPath = path.resolve((Path)event.context());

                // If a file is changed, we receive two change events: one for the file contents, and one
                // for the file metadata (last modified date). We filter out these repeated events by treating
                // change events for a single file that are separated by less than 50 ms as being the same event.
                //
                if (!recentlyModifiedFiles.contains(fullPath)) {
                    recentlyModifiedFiles.add(fullPath);
                    handler.fileModified(fullPath);
                }
            }
        }
    }

    private static class WatchServiceManager {
        private static WatchServiceManager instance;
        private static AtomicInteger refCount = new AtomicInteger();

        private final WatchService watchService;
        private final List<WeakReference<DirectoryWatcher>> watchers = new ArrayList<>();
        private final List<DirectoryWatcher> addedWatchers = new ArrayList<>();
        private boolean watchersLocked;
        private Future currentFuture;

        private WatchServiceManager() {
            WatchService watchService = null;

            try {
                watchService = FileSystems.getDefault().newWatchService();
            } catch (IOException ex) {
                LOGGER.warn("Watch service is not available.", ex);
            }

            this.watchService = watchService;

            if (watchService != null) {
                currentFuture = Dispatcher.background().runLaterAsync(this::run);
            }
        }

        static synchronized WatchServiceManager getInstance() {
            if (refCount.getAndIncrement() == 0) {
                instance = new WatchServiceManager();
            }

            return instance;
        }

        @SuppressWarnings("SynchronizeOnNonFinalField")
        static synchronized void releaseInstance() {
            if (refCount.decrementAndGet() == 0) {
                synchronized (instance) {
                    if (instance.currentFuture != null) {
                        instance.currentFuture.cancel(true);
                    }

                    instance = null;
                }
            }
        }

        synchronized WatchKey register(DirectoryWatcher watcher, Path path, boolean recursive) {
            if (watchService != null) {
                try {
                    WatchKey key;

                    if (WINDOWS && recursive) {
                        key =
                            path.register(
                                watchService,
                                new WatchEvent.Kind[] {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY},
                                ExtendedWatchEventModifier.FILE_TREE);
                    } else {
                        key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    }

                    if (watchersLocked) {
                        addedWatchers.add(watcher);
                    } else {
                        watchers.add(new WeakReference<>(watcher));
                    }

                    return key;
                } catch (IOException ex) {
                    LOGGER.warn("Error registering path with watch service.", ex);
                }
            }

            return null;
        }

        private void run() {
            try {
                WatchKey key = watchService.poll(1000, TimeUnit.MILLISECONDS);
                if (key == null) {
                    synchronized (this) {
                        currentFuture = Dispatcher.background().runLaterAsync(this::run);
                        return;
                    }
                }

                synchronized (this) {
                    watchersLocked = true;
                    ListIterator<WeakReference<DirectoryWatcher>> it = watchers.listIterator();
                    while (it.hasNext()) {
                        DirectoryWatcher watcher = it.next().get();
                        if (watcher == null) {
                            it.remove();
                            continue;
                        }

                        if (watcher.getKey() == key) {
                            try {
                                watcher.handleEvents(key.pollEvents());
                            } catch (Exception ignored) {
                                // This might happen if the event handler throws an exception.
                            }
                        }
                    }

                    key.reset();

                    for (DirectoryWatcher addedWatcher : addedWatchers) {
                        watchers.add(new WeakReference<>(addedWatcher));
                    }

                    addedWatchers.clear();
                    watchersLocked = false;
                    currentFuture = Dispatcher.background().runLaterAsync(this::run);
                }
            } catch (ClosedWatchServiceException | InterruptedException ignored) {
            } catch (Exception ex) {
                LOGGER.error("Watch service encountered an unexpected error. ", ex);
            }
        }
    }

}
