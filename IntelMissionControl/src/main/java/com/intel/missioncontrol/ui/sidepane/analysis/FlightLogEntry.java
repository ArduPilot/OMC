/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import static eu.mavinci.desktop.helper.MFileFilter.photoJsonFilter;

import com.google.common.util.concurrent.Futures;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;

public class FlightLogEntry {

    private final StringProperty name;
    private final ObjectProperty<File> path;
    private final ObjectProperty<Instant> date = new SimpleObjectProperty<>();
    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>();
    private final ObjectProperty<File> imageFolder = new SimpleObjectProperty<>();
    private final IntegerProperty triggerCount = new SimpleIntegerProperty();
    private final IntegerProperty imageCount = new SimpleIntegerProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty();
    private final BooleanProperty readingLogFile = new SimpleBooleanProperty();
    private final BooleanProperty hasRtk = new SimpleBooleanProperty();
    private final BooleanProperty isFalcon = new SimpleBooleanProperty();
    private final BooleanProperty isJson = new SimpleBooleanProperty();
    private final BooleanProperty isAscTecLog = new SimpleBooleanProperty();
    private final List<CPhotoLogLine> imageTriggers = new ArrayList<>();

    public FlightLogEntry(File path, boolean needToParse) {
        String name = path.getName();
        if (name == null || name.isEmpty()) {
            // for example a falcon logile directly in a SD card main directory without any subfolder in
            // between... actually this should be forbidden to the user... but they do weared things!
            name = "";
        }

        this.name = new ReadOnlyStringWrapper(name);
        this.path = new SimpleObjectProperty<>(path);
        if (!path.isDirectory()
                && photoJsonFilter.accept(path.getName())
                && FileHelper.fetchFotos(path.getParentFile(), MFileFilter.jpegFilter.getWithoutFolders()) != null) {
            imageFolder.set(path.getParentFile());
            isFalcon.set(true);
            isJson.set(true);
        }

        if (MFileFilter.ascTecLogFolder.acceptTrinityLog(path)) {
            isFalcon.set(true);
            isAscTecLog.set(true);
        }

        this.readingLogFile.set(true);

        if (needToParse) {
            LogFileHelper.parseLogAsync(path).whenDone(this::logParsingFinished);
        }
    }

    @Override
    public String toString() {
        return name
            + "\t"
            + path
            + "\tselected:"
            + isSelected()
            + "\tisReadingLogFile:"
            + isReadingLogFile()
            + "\thasImageFolder:"
            + hasImageFolder();
    }

    private void logParsingFinished(Future<LogFileHelper.LogParserCacheEntry> future) {
        LogFileHelper.LogParserCacheEntry cacheEntry = Futures.getUnchecked(future);
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.run(
            () -> {
                if (future.isSuccess()) {
                    triggerCount.set(cacheEntry.allLogs.size());
                    hasRtk.set(cacheEntry.hasRtk);
                    if (!cacheEntry.allLogs.isEmpty()) {
                        CPhotoLogLine first = cacheEntry.allLogs.first();
                        date.set(Instant.ofEpochMilli(Math.round(first.getTimestamp() * 1000)));
                        CPhotoLogLine last = cacheEntry.allLogs.last();
                        setDuration(Duration.ofMillis(Math.round((last.getTimestamp() - first.getTimestamp()) * 1000)));
                        imageTriggers.addAll(cacheEntry.allLogs);
                    }
                    if(imageFolder.get() != null) {
                        FileHelper.GetFotosResult images =
                                FileHelper.fetchFotos(List.of(imageFolder.get()));
                        imageCount.set(images.fotos.size());
                    }
                }

                readingLogFile.set(false);
            });
    }

    public ReadOnlyBooleanProperty readingLogFileProperty() {
        return readingLogFile;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public ReadOnlyObjectProperty<Instant> dateProperty() {
        return date;
    }

    public ReadOnlyObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public ReadOnlyIntegerProperty triggerCountProperty() {
        return triggerCount;
    }

    public ReadOnlyIntegerProperty imageCountProperty() {
        return imageCount;
    }

    public boolean isReadingLogFile() {
        return readingLogFile.get();
    }

    public boolean isSelected() {
        return selected.get();
    }

    public boolean isNotSelected() {
        return !selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public String getName() {
        return name.get();
    }

    public Instant getDate() {
        return date.get();
    }

    public Duration getDuration() {
        return duration.get();
    }

    public int getImageCount() {
       return imageCount.get();
    }

    public int getTriggerCount() {
        return triggerCount.get();
    }

    public boolean hasRtk() {
        return hasRtk.get();
    }

    public boolean isIsFalcon() {
        return isFalcon.get();
    }
    public boolean isIsJson() {
        return isJson.get();
    }
    public void setDuration(Duration duration) {
        this.duration.set(duration);
    }

    public ReadOnlyObjectProperty<File> pathProperty() {
        return path;
    }

    public File getPath() {
        return path.get();
    }

    public List<CPhotoLogLine> getImageTriggers() {
        return imageTriggers;
    }

    public ReadOnlyObjectProperty<File> imageFolderProperty() {
        return imageFolder;
    }

    public boolean hasImageFolder() {
        return imageFolder.get() != null;
    }

    public File getImageFolder() {
        return imageFolder.get();
    }

}
