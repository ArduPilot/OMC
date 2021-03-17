/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.intel.missioncontrol.mission.IMissionInfo;
import com.intel.missioncontrol.mission.MissionConstants;
import de.saxsys.mvvmfx.ViewModel;
import java.io.File;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class MissionItemViewModel implements ViewModel {

    public static final int MAX_PREVIEW_SCREENSHOT_WIDTH = 200;
    public static final int MAX_PREVIEW_SCREENSHOT_HEIGHT = 150;

    private static final Image DEFAULT_THUMBNAIL_IMG;

    static {
        String DEFAULT_THUMBNAIL = "/com/intel/missioncontrol/gfx/gfx_project-preview(fill=theme-gray-BB).svg";
        DEFAULT_THUMBNAIL_IMG =
            new Image(Thread.currentThread().getContextClassLoader().getResource(DEFAULT_THUMBNAIL).toExternalForm());
    }

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    private final IMissionInfo missionInfo;
    private final StringProperty lastUpdate;
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(DEFAULT_THUMBNAIL_IMG);

    public MissionItemViewModel(IMissionInfo missionInfo) {
        this.missionInfo = missionInfo;
        this.lastUpdate = new ReadOnlyStringWrapper(FORMATTER.format(missionInfo.getLastModified().toInstant()));

        String url = getScreenshotUrl(missionInfo);
        if (url != null) {
            Image img = new Image(url, MAX_PREVIEW_SCREENSHOT_WIDTH, MAX_PREVIEW_SCREENSHOT_HEIGHT, false, true, true);
            img.progressProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= 1) {
                            image.set(img);
                        }
                    });
        }
    }

    private String getScreenshotUrl(IMissionInfo missionInfo) {
        File screenshot =
            new File(
                missionInfo.getFolder(),
                MissionConstants.FOLDER_NAME_SCREENSHOT
                    + File.separator
                    + MissionConstants.MISSION_SCREENSHOT_LOW_RES_FILENAME);
        if (!screenshot.exists()) {
            // fallback for legacy missions
            screenshot =
                new File(
                    missionInfo.getFolder(),
                    MissionConstants.FOLDER_NAME_SCREENSHOT
                        + File.separator
                        + MissionConstants.MISSION_SCREENSHOT_FILENAME);
        }

        String url = null;
        try {
            if (screenshot.isFile()) {
                url = screenshot.toURL().toExternalForm();
            }
        } catch (MalformedURLException e) {
            // don't handle
        }

        return url;
    }

    public IMissionInfo getMissionInfo() {
        return missionInfo;
    }

    public String getName() {
        return missionInfo.getName();
    }

    public int getFlightPlansCount() {
        return missionInfo.getLoadedFlightPlans().size();
    }

    public int getFlighsCount() {
        return missionInfo.getFlightLogs().size();
    }

    public int getDatasetsCount() {
        return missionInfo.getLoadedDataSets().size();
    }

    public boolean hasFlightPlans() {
        return missionInfo.getLoadedFlightPlans().size() != 0;
    }

    public boolean hasFlights() {
        return missionInfo.getFlightLogs().size() != 0;
    }

    public boolean hasDatasets() {
        return missionInfo.getLoadedDataSets().size() != 0;
    }

    public ReadOnlyStringProperty lastUpdateProperty() {
        return lastUpdate;
    }

    public ReadOnlyObjectProperty<Image> imageProperty() {
        return image;
    }
}
