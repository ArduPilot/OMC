package com.intel.missioncontrol.ui.sidepane.start;

import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.mission.MissionInfo;
import de.saxsys.mvvmfx.ViewModel;
import java.io.File;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.image.Image;

@SuppressLinter(
    value = "ViewClassInViewModel",
    reviewer = "mstrauss",
    justification = "Okay for javafx.scene.image.Image"
)
public class ProjectItemViewModel implements ViewModel {

    public static final int MAX_THUMBNAIL_WIDTH = 200;
    public static final int MAX_THUMBNAIL_HEIGHT = 150;

    @SuppressWarnings("ConstantConditions")
    private static final Image DEFAULT_THUMBNAIL_IMG =
        new Image(
            Thread.currentThread()
                .getContextClassLoader()
                .getResource("/com/intel/missioncontrol/gfx/gfx_project-preview(fill=theme-gray-BB).svg")
                .toExternalForm(),
            MAX_THUMBNAIL_WIDTH,
            MAX_THUMBNAIL_HEIGHT,
            true,
            true);

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    private final MissionInfo missionInfo;
    private final String lastUpdate;
    private final ReadOnlyObjectWrapper<Image> image = new ReadOnlyObjectWrapper<>(DEFAULT_THUMBNAIL_IMG);
    private Image delayedImage;

    public ProjectItemViewModel(MissionInfo missionInfo) {
        this.missionInfo = missionInfo;
        this.lastUpdate = FORMATTER.format(missionInfo.getLastModified().toInstant());

        String url = getScreenshotUrl(missionInfo);
        if (url != null) {
            delayedImage = new Image(url, MAX_THUMBNAIL_WIDTH, MAX_THUMBNAIL_HEIGHT, false, true, true);
            delayedImage
                .progressProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= 1) {
                            image.set(delayedImage);
                            delayedImage = null;
                        }
                    });
        }
    }

    private String getScreenshotUrl(MissionInfo missionInfo) {
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

    public MissionInfo getMissionInfo() {
        return missionInfo;
    }

    public String getName() {
        return missionInfo.getName();
    }

    public int getFlightPlansCount() {
        return missionInfo.getLoadedFlightPlans().size();
    }

    public int getFlightsCount() {
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

    public boolean isRemote() {
        return missionInfo.isRemote();
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public ReadOnlyObjectProperty<Image> imageProperty() {
        return image.getReadOnlyProperty();
    }

}
