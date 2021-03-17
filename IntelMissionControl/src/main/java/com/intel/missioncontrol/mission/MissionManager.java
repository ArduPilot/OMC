/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.api.support.SupportConstants;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.SystemInformation;
import com.intel.missioncontrol.map.worldwind.impl.IScreenshotManager;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.ui.MainViewModel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.start.DateItemViewModel;
import com.intel.missioncontrol.ui.sidepane.start.ProjectItemViewModel;
import de.saxsys.mvvmfx.ViewModel;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.flightplan.Flightplan;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javax.imageio.ImageIO;
import javax.inject.Provider;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MissionManager implements IMissionManager {

    public static final String FLIGHT_PLAN_FILE_EXTENSION = "fml";

    private static final Logger LOGGER = LoggerFactory.getLogger(MissionManager.class);

    private static final String SR_FOLDER_ALREADY_EXISTS = "folderAlreadyExists";
    private static final String SR_FILE_NOT_FOUND = "fileNotFound";
    private static final String SR_ATTRIBUTES_ERROR = "attributesError";
    private static final String SR_ACCESS_DENIED = "accessDenied";
    private static final String SR_CANNOT_MOVE_PROJECT = "cannotMoveProject";
    private static final String SR_CANNOT_RENAME_PROJECT = "cannotRenameProject";
    private static final String SR_CANNOT_REMOVE_PATH = "cannotRemovePath";
    private static final String SR_OLD_PATH_HAS = "oldPathHas";
    private MainViewModel mainViewModel;

    private final UIAsyncListProperty<ViewModel> items =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<ViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncListProperty<MissionInfo> recentMissionInfos =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<MissionInfo>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final Mission.Factory missionFactory;
    private final ISettingsManager settingsManager;
    private final PathSettings pathSettings;
    private final IMissionInfoManager missionInfoManager;
    private final ILanguageHelper languageHelper;
    private final Provider<IScreenshotManager> screenshotManager;
    private final IApplicationContext applicationContext;

    private boolean initialized;

    @Inject
    public MissionManager(
            IApplicationContext applicationContext,
            Mission.Factory missionFactory,
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            IMissionInfoManager missionInfoManager,
            Provider<IScreenshotManager> screenshotManager) {
        this.applicationContext = applicationContext;
        this.missionFactory = missionFactory;
        this.settingsManager = settingsManager;
        this.pathSettings = settingsManager.getSection(PathSettings.class);
        this.languageHelper = languageHelper;
        this.missionInfoManager = missionInfoManager;
        this.screenshotManager = screenshotManager;

        recentMissionInfos.addListener(
            (ListChangeListener<? super MissionInfo>)
                change -> {
                    var projectFolder = pathSettings.getProjectFolder();
                    var recentMissions = pathSettings.getReferencedProjects();
                    while (change.next()) {
                        if (change.wasAdded()) {
                            for (var item : change.getAddedSubList()) {
                                var directory = item.getFolder().toPath();
                                if (!directory.startsWith(projectFolder) && !recentMissions.contains(directory)) {
                                    recentMissions.add(directory);
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public Mission createNewMission() throws IOException {
        File file = createMissionFile();
        MissionInfo missionInfo = new MissionInfo(file.toPath());

        return Dispatcher.platform()
            .getLaterAsync(
                () -> {
                    Mission tmp = missionFactory.create(missionInfo);
                    recentMissionInfos.add(missionInfo);
                    return tmp;
                })
            .getUnchecked();
    }

    private File createMissionFile() throws IOException {
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String folderName = dateFormat.format(today);
        return createMissionFile(folderName);
    }

    private File createMissionFile(String folderName) throws IOException {
        File missionFolder = Paths.get(pathSettings.getProjectFolder().toString(), folderName).toFile();

        try {
            Files.createDirectory(Paths.get(missionFolder.toURI()));
        } catch (FileAlreadyExistsException e) {
            throw new IOException(
                languageHelper.getString(MissionManager.class, SR_FOLDER_ALREADY_EXISTS, missionFolder));
        } catch (NoSuchFileException e) {
            throw new IOException(languageHelper.getString(MissionManager.class, SR_FILE_NOT_FOUND, missionFolder));
        } catch (UnsupportedOperationException e) {
            throw new IOException(languageHelper.getString(MissionManager.class, SR_ATTRIBUTES_ERROR, missionFolder));
        } catch (SecurityException e) {
            throw new IOException(languageHelper.getString(MissionManager.class, SR_ACCESS_DENIED, missionFolder));
        }

        LOGGER.info("Created mission folder: {}", folderName);

        // create folder structure
        File flightplanFolder = new File(missionFolder, MissionConstants.FOLDER_NAME_FLIGHTPLANS);
        File logFolder = new File(missionFolder, MissionConstants.FOLDER_NAME_LOGFILES);
        File kmlFolder = new File(missionFolder, MissionConstants.FOLDER_NAME_KML);
        File planeConfigFolder = new File(missionFolder, MissionConstants.FOLDER_NAME_PLANECONFIG);
        File fligtplanAutosaveFolder =
            new File(
                missionFolder
                    + File.separator
                    + MissionConstants.FOLDER_NAME_FLIGHTPLANS
                    + File.separator
                    + MissionConstants.FOLDER_NAME_AUTOSAVE);
        File planeConfigAutosaveFolder =
            new File(
                missionFolder
                    + File.separator
                    + MissionConstants.FOLDER_NAME_PLANECONFIG
                    + File.separator
                    + MissionConstants.FOLDER_NAME_AUTOSAVE);
        File ftpFolder = new File(missionFolder, MissionConstants.FOLDER_NAME_FLIGHT_LOGS);
        File matchingsFolder = new File(missionFolder, MissionConstants.FOLDER_NAME_MATCHINGS);
        File screenshotFolder = new File(missionFolder, MissionConstants.FOLDER_NAME_SCREENSHOT);

        File[] folders = {
            missionFolder,
            flightplanFolder,
            logFolder,
            kmlFolder,
            planeConfigFolder,
            fligtplanAutosaveFolder,
            planeConfigAutosaveFolder,
            ftpFolder,
            matchingsFolder,
            screenshotFolder
        };
        FileHelper.mkdirs(folders);

        return missionFolder;
    }

    private void renameLegacyFolders(File base) {
        File old = new File(base, "datasets");
        old.renameTo(MissionConstants.getMatchingsFolder(base));

        old = new File(base, "flightplans");
        old.renameTo(MissionConstants.getFlightplanFolder(base));

        old = new File(base, "ftp");
        old.renameTo(MissionConstants.getFlightLogsFolder(base));

        old = new File(base, "kml");
        old.renameTo(MissionConstants.getKMLFolder(base));

        old = new File(base, "log");
        old.renameTo(MissionConstants.getLogFolder(base));

        old = new File(base, "planeconfig");
        old.renameTo(MissionConstants.getPlaneConfigFolder(base));

        old = new File(base, "screenshot");
        old.renameTo(MissionConstants.getScreenshotFolder(base));
    }

    private void createMissionFolders(File base) {
        // if sub-folderstructure doen't exist, than create it now!
        File[] folders = {
            base,
            MissionConstants.getFlightplanFolder(base),
            MissionConstants.getLogFolder(base),
            MissionConstants.getKMLFolder(base),
            MissionConstants.getPlaneConfigFolder(base),
            MissionConstants.getFlightplanAutosaveFolder(base),
            MissionConstants.getPlaneConfigAutosaveFolder(base),
            MissionConstants.getFlightLogsFolder(base),
            MissionConstants.getMatchingsFolder(base),
            MissionConstants.getScreenshotFolder(base)
        };
        try {
            FileHelper.mkdirs(folders);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refreshRecentMissionInfos() {
        Path folder = settingsManager.getSection(PathSettings.class).getProjectFolder();
        var missionPaths = new ArrayList<Path>();
        try (var paths = Files.list(folder)) {
            missionPaths.addAll(paths.collect(Collectors.toList()));
        } catch (IOException e) {
            // do nothing
        }

        try (LockedList<Path> referencedProjects = pathSettings.getReferencedProjects().lock()) {
            referencedProjects.removeIf(path -> !Files.exists(path));
            missionPaths.addAll(referencedProjects.stream().distinct().collect(Collectors.toList()));
        }

        var missions =
            missionPaths
                .stream()
                .filter(path -> isMissionFolder(path.toFile()))
                .map(
                    (path -> {
                        renameLegacyFolders(path.toFile());
                        createMissionFolders(path.toFile());
                        try {
                            return missionInfoManager.readFromFile(path);
                        } catch (IOException e) {
                            return new MissionInfo(path);
                        }
                    }))
                .filter(mission -> !deleteEmptyMissionInt(mission))
                .collect(Collectors.toList());

        recentMissionInfos.clear(); // make sure it changes, so listeners will see update also inside missions
        recentMissionInfos.setAll(missions);

        initialized = true;
    }

    public UIAsyncListProperty<ViewModel> recentMissionListItems() {
        return items;
    }

    @Override
    public MissionInfo getByRemoteId(String remoteId) {
        try (var lockedList = recentMissionInfosProperty().get().lock()) {
            for (var mi : lockedList) {
                if (mi.getRemoteId() != null && mi.getRemoteId().equals(remoteId)) {
                    return mi;
                }
            }
        }

        return null;
    }

    @Override
    public void moveRecentMissions(String oldPath, String newPath) {
        Path folder = settingsManager.getSection(PathSettings.class).getProjectFolder();
        var missionPaths = new ArrayList<Path>();
        try (var paths = Files.walk(folder, 1)) {
            missionPaths.addAll(paths.collect(Collectors.toList()));
        } catch (IOException e) {
            applicationContext.addToast(Toast.of(ToastType.ALERT).setText(e.getMessage()).setShowIcon(true).create());
            return;
        }

        missionPaths.forEach(
            path -> {
                try {
                    if (!oldPath.equals(path.toString())) {
                        Files.move(path, new File(newPath).toPath().resolve(path.getFileName()));
                        LOGGER.info(
                            "Moved Mission: " + path + " => " + new File(newPath).toPath().resolve(path.getFileName()));
                    }
                } catch (Exception e) {
                    applicationContext.addToast(
                        Toast.of(ToastType.ALERT)
                            .setText(
                                languageHelper.getString(
                                    MissionManager.class, SR_CANNOT_MOVE_PROJECT, path, e.getMessage()))
                            .setShowIcon(true)
                            .create());

                    LOGGER.warn("Cannot move project: " + path + "\n" + e.getMessage());
                }
            });

        missionPaths = new ArrayList<>();
        try (var paths = Files.walk(folder, 1)) {
            missionPaths.addAll(paths.collect(Collectors.toList()));
        } catch (IOException e) {
            applicationContext.addToast(Toast.of(ToastType.ALERT).setText(e.getMessage()).setShowIcon(true).create());
            return;
        }

        if (missionPaths.size() == 1 && missionPaths.get(0).toString().equals(oldPath)) {
            try {
                missionPaths.get(0).toFile().delete();
                LOGGER.info("Removed old path: " + missionPaths.get(0));
            } catch (Exception e) {
                applicationContext.addToast(
                    Toast.of(ToastType.ALERT)
                        .setText(
                            languageHelper.getString(
                                MissionManager.class, SR_CANNOT_REMOVE_PATH, missionPaths.get(0), e.getMessage()))
                        .setShowIcon(true)
                        .create());
            }
        } else {
            applicationContext.addToast(
                Toast.of(ToastType.INFO)
                    .setText(languageHelper.getString(MissionManager.class, SR_OLD_PATH_HAS, (missionPaths.size() - 1)))
                    .create());

            LOGGER.info(
                "Old path has " + (missionPaths.size() - 1) + " mission paths after trying to move the contents.");
        }
    }

    public void refreshRecentMissionListItems() {
        AsyncObservableList<MissionInfo> missionsInfo = recentMissionInfosProperty();

        if (missionsInfo.isEmpty()) {
            items.clear();
            return;
        }

        List<MissionInfo> sortedList = new ArrayList<>();
        try (LockedList<MissionInfo> lock = missionsInfo.lock()) {
            sortedList.addAll(lock);
        }

        sortedList.sort(Comparator.comparing(MissionInfo::getLastModified).reversed());

        List<ViewModel> newItems = new ArrayList<>();
        Date lastHeaderDate = sortedList.get(0).getLastModified();
        newItems.add(new DateItemViewModel(lastHeaderDate.toInstant()));
        for (MissionInfo missionInfo : sortedList) {
            if (missionInfo.getLastModified().getYear() != lastHeaderDate.getYear()
                    || missionInfo.getLastModified().getMonth() != lastHeaderDate.getMonth()) {
                lastHeaderDate = missionInfo.getLastModified();
                newItems.add(new DateItemViewModel(lastHeaderDate.toInstant()));
            }

            newItems.add(new ProjectItemViewModel(missionInfo));
        }

        items.clear();
        items.setAll(newItems);
    }

    @Override
    public AsyncListProperty<MissionInfo> recentMissionInfosProperty() {
        if (!initialized) {
            refreshRecentMissionInfos();
        }

        return recentMissionInfos;
    }

    @Override
    public Mission openMission(Path directory) {
        List<Mission> list;
        try (LockedList<MissionInfo> missionInfos = recentMissionInfos.lock()) {
            list =
                missionInfos
                    .stream()
                    .filter(missionInfo -> missionInfo.getFolderPath().equals(directory))
                    .map(missionFactory::create)
                    .collect(Collectors.toList());
        }

        Mission result;
        if (!list.isEmpty()) {
            result = list.get(0);
        } else {
            renameLegacyFolders(directory.toFile());
            createMissionFolders(directory.toFile());

            MissionInfo info;
            try {
                info = missionInfoManager.readFromFile(directory);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                info = new MissionInfo(directory);
            }

            result = missionFactory.create(info);
            recentMissionInfos.add(info);
        }

        return result;
    }

    @Override
    public Mission openMission(MissionInfo info) {
        return openMission(info.getFolderPath());
    }

    @Override
    public Mission cloneMission(Mission mission) throws IOException {
        Expect.notNull(mission, "mission");

        Dispatcher.platform().runLaterAsync(() -> makeDefaultScreenshot(mission)).getUnchecked();
        final Path currentMissionPath = mission.getDirectory();
        if (!Files.exists(currentMissionPath)) {
            // not even path is available
            throw new FileNotFoundException(currentMissionPath.toString());
        }

        String targetName = verifyForDuplicates(String.format("Clone of %s", mission.getName()));
        final String sessionDefaultBaseFolder = pathSettings.getProjectFolder().toString();
        final Path targetPath = Paths.get(sessionDefaultBaseFolder, targetName);
        if (Files.exists(targetPath)) {
            throw new IOException(targetName + " already exists.");
        }

        File flightplansFolder = MissionConstants.getFlightplanFolder(mission.getDirectory());

        File resultFile = createMissionFile(targetName);
        Dispatcher.platform().runLaterAsync(() -> mission.setDirectory(targetPath)).getUnchecked();
        mission.flightPlansProperty()
            .forEach(
                fp -> {
                    if (fp.hasUnsavedChanges()) {
                        if (fp.canBeSaved()) {
                            File newFile =
                                new File(
                                    MissionConstants.getFlightplanFolder(resultFile.getAbsoluteFile()).getPath()
                                        + File.separator
                                        + fp.nameProperty().get()
                                        + MissionConstants.FLIGHT_PLAN_EXT);
                            fp.getLegacyFlightplan().setFile(newFile);
                            fp.getLegacyFlightplan().save(null);
                            LOGGER.info("Flight plan: " + fp.nameProperty().get() + " -> saved");
                        } else {
                            LOGGER.info("Flight plan: " + fp.nameProperty().get() + " -> not saveable");
                            mission.flightPlansProperty().remove(fp);
                        }
                    } else {
                        LOGGER.info(
                            "Flight plan: "
                                + fp.nameProperty().get()
                                + " -> already saved "
                                + fp.getLegacyFlightplan().getFile());
                        updateFlightPlanLocation(mission, fp, flightplansFolder);
                    }
                });

        File newFlightplansFolder = MissionConstants.getFlightplanFolder(resultFile);

        mission.setMissionEmpty(false);
        mission.save();

        File[] sourceFiles = flightplansFolder.listFiles(MFileFilter.fmlFilter);
        if (sourceFiles != null && sourceFiles.length > 0) {
            for (File source : sourceFiles) {
                File target = new File(newFlightplansFolder, source.getName());
                LOGGER.info("Copy flight plan: " + source + " -> " + target);
                try {
                    FileHelper.copyFile(source, target);
                } catch (IOException e) {
                    LOGGER.info(
                        "Copy flight plan: " + source + " -> " + target + " -> saved with error", e); // Add Toast
                }
            }
        }

        if (!Files.exists(MissionConstants.getNewtConfigFile(mission.getDirectory()))) {
            mission.save();
        }

        MissionInfo missionInfo = missionInfoManager.readFromFile(resultFile.toPath());
        return Dispatcher.platform()
            .getLaterAsync(
                () -> {
                    makeDefaultScreenshot(mission);
                    Mission tmp = missionFactory.create(missionInfo);
                    tmp.setMissionEmpty(false);
                    recentMissionInfos.add(missionInfo);
                    LOGGER.info("Successfully cloned mission {}", mission.getName());
                    return tmp;
                })
            .getUnchecked();
    }

    private String verifyForDuplicates(String filename) throws IOException {
        int copyN = 2;
        String newFileName = filename + " (" + copyN + ")";
        if (missionExists(filename)) {
            while (missionExists(newFileName)) {
                copyN++;
                newFileName = filename + " (" + copyN + ")";
            }
        } else {
            newFileName = filename;
        }

        return newFileName;
    }

    @Override
    public void renameMission(Mission mission, String newName) throws RuntimeException {
        renameMissionWithoutNameValidation(mission, newName);
        // in order to update a timestamp
        saveMission(mission);
    }

    private void renameMissionWithoutNameValidation(Mission mission, String newName) throws RuntimeException {
        File flightplansFolder = MissionConstants.getFlightplanFolder(mission.getDirectory());
        File matchingsFolder = MissionConstants.getMatchingsFolder(mission.getDirectory());
        var projectFolder = mission.getDirectory().getParent();
        var flightLogsFolder = mission.getFlightLogsFolder();
        var sourceMissionFolder = projectFolder.resolve(mission.getName());
        var targetMissionFolder = projectFolder.resolve(newName);

        try {
            Files.move(sourceMissionFolder, targetMissionFolder);
        } catch (IOException e) {
            applicationContext.addToast(
                Toast.of(ToastType.ALERT)
                    .setText(languageHelper.getString(MissionManager.class, SR_CANNOT_RENAME_PROJECT, e.getMessage()))
                    .setShowIcon(true)
                    .create());

            throw new RuntimeException(e);
        }

        pathSettings.getReferencedProjects().remove(sourceMissionFolder);
        Dispatcher.platform().runLaterAsync(() -> mission.setDirectory(targetMissionFolder)).getUnchecked();
        mission.flightPlansProperty().forEach(fp -> updateFlightPlanLocation(mission, fp, flightplansFolder));
        mission.matchingsProperty().forEach(matching -> updateMatchingLocation(mission, matching, matchingsFolder));
        updateflightLogsLocation(mission, flightLogsFolder);
    }

    private void updateflightLogsLocation(Mission mission, File oldLogsFolder) {
        int i = 0;
        for (File flightLog : mission.flightLogsProperty().get()) {
            if (flightLog.getParent().equals(oldLogsFolder.getAbsolutePath())) {
                mission.flightLogsProperty().set(i, new File(mission.getFlightLogsFolder(), flightLog.getName()));
            }

            i++;
        }
    }

    private void updateFlightPlanLocation(Mission mission, FlightPlan flightPlan, File sourceMissionFolder) {
        Flightplan legacyFlightplan = flightPlan.getLegacyFlightplan();
        String fpFileName = null;
        if (legacyFlightplan.getFile() != null
                && legacyFlightplan.getFile().getParentFile().equals(sourceMissionFolder)) {
            fpFileName = legacyFlightplan.getFile().getName();
        } else {
            return;
        }

        if (fpFileName == null) {
            fpFileName = String.format("%s.fml", flightPlan.getName());
        }

        try {
            legacyFlightplan.setFile(
                new File(MissionConstants.getFlightplanFolder(mission.getDirectory()), fpFileName));
        } catch (IllegalStateException e) {
            LOGGER.warn("Failed to update flight plan location: {}", flightPlan.getName(), e);
        }
    }

    private void updateMatchingLocation(Mission mission, Matching matching, File sourceMissionFolder) {
        AMapLayerMatching legacyMatching = matching.getLegacyMatching();
        if (legacyMatching == null) {
            return;
        }

        File matchingFolder = legacyMatching.getMatchingFolder();
        if (matchingFolder == null) {
            return;
        }

        String matchingFileName;
        if (legacyMatching.getResourceFile() != null && sourceMissionFolder.equals(matchingFolder.getParentFile())) {
            matchingFileName = legacyMatching.getName();
        } else {
            return;
        }

        try {
            legacyMatching.setFile(
                new File(MissionConstants.getMatchingsFolder(mission.getDirectory()), matchingFileName));
        } catch (IOException e) {
            LOGGER.warn("Failed to update matching location: {}", matching.getName(), e);
        }
    }

    @Override
    public boolean deleteEmptyMissions() {
        boolean successful = true;
        for (MissionInfo missionInfo : recentMissionInfosProperty().get()) {
            Path missionFolder = missionInfo.getFolderPath();
            if (Files.isDirectory(missionFolder)) {
                if (!hasNonEmptyFolder(missionFolder)) {
                    if (deleteFileRecursively(missionFolder)) {
                        LOGGER.info(
                            "The empty mission {} was successfully deleted! ", missionFolder.getFileName().toString());
                    } else {
                        successful = false;
                        LOGGER.info("Failed to delete empty mission: {}", missionFolder.getFileName().toString());
                    }
                }
            }
        }

        return successful;
    }

    /**
     * Deletes recursively all files from a directory structure.
     *
     * @param dirPath the path of the parent directory to be deleted
     * @return true if the directory structure was successfully deleted
     */
    private boolean deleteFileRecursively(Path dirPath) {
        try {
            Files.walkFileTree(
                dirPath,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            return true;
        } catch (IOException e) {
            LOGGER.error(String.format("Error deleting directory %s", dirPath), e);
        }

        return false;
    }

    private boolean isDemoMission(String name) {
        if (name.equals(Mission.DEMO_MISSION_NAME)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteDemoMission(Mission mission) {
        if (mission == null) {
            return false;
        }

        Path missionFolder = mission.getDirectory();
        if (isDemoMission(mission.getName())) {
            if (deleteFileRecursively(missionFolder)) {
                LOGGER.info("Demo mission {} was successfully deleted! ", missionFolder.getFileName().toString());
                recentMissionInfos.remove(mission);
                refreshRecentMissionInfos();
                return true;
            }
        }

        refreshRecentMissionInfos();
        return false;
    }

    @Override
    public boolean deleteEmptyMission(Mission mission) {
        if (mission == null) {
            return false;
        }

        if (deleteEmptyMissionInt(mission.getMissionInfo())) {
            refreshRecentMissionInfos();
            return true;
        }

        return false;
    }

    private boolean deleteEmptyMissionInt(MissionInfo missionInfo) {
        // let's not delete current mission even if it is empty
        if (missionInfo != null && !isCurrentMission(missionInfo)) {
            Path missionFolder = missionInfo.getFolderPath();
            if (Files.isDirectory(missionFolder)) {
                if (!hasNonEmptyFolder(missionFolder) && !hasLoadedData(missionInfo) && !hasMetaData(missionInfo)) {
                    if (deleteFileRecursively(missionFolder)) {
                        LOGGER.info(
                            "The empty mission {} was successfully deleted! ", missionFolder.getFileName().toString());
                        recentMissionInfos.remove(missionInfo);
                        return true;
                    } else {
                        LOGGER.debug("Mission not empty.");
                        return false;
                    }
                }
            }
        }

        return false;
    }

    private boolean isCurrentMission(MissionInfo missionInfo) {
        Mission currentMission = applicationContext.currentMissionProperty().get();
        if (currentMission == null) {
            return false;
        }

        return currentMission.getMissionInfo().equals(missionInfo);
    }

    private boolean hasLoadedData(MissionInfo missionInfo) {
        return missionInfo.getLoadedFlightPlans().size() != 0
            || missionInfo.getLoadedDataSets().size() != 0
            || missionInfo.getFlightLogs().size() != 0;
    }

    private boolean hasMetaData(MissionInfo missionInfo) {
        File meta = new File(missionInfo.getFolderPath().toFile().getParent(), SupportConstants.FILENAME_META);
        if (meta.exists()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isMissionEmpty(MissionInfo mission) {
        boolean result = false;
        Path missionFolder = mission.getFolderPath();
        if (Files.isDirectory(missionFolder)) {
            if (!hasNonEmptyFolder(missionFolder)) {
                result = true;
            }
        }

        return result;
    }

    @Override
    public boolean isMissionCloneable(MissionInfo mission) {
        List<Path> folderContents;
        try (var stream = Files.walk(MissionConstants.getFlightplanFolder(mission.getFolder()).toPath(), 1)) {
            folderContents = stream.skip(1).collect(Collectors.toList());
        } catch (IOException e) {
            folderContents = new ArrayList<>();
        }

        for (Path content : folderContents) {
            // Disregard files, acquire folders
            final String contentName = content.getFileName().toString();
            if (contentName.endsWith(FLIGHT_PLAN_FILE_EXTENSION)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void saveMission(Mission currentMission) {
        currentMission.save(false);
    }

    /**
     * This method goes recursively through all folders that are directly contained in a directory, and searches for
     * folders that are not empty and have regular files as content. Returns true if there is at least one folder that
     * is not empty.
     *
     * @param parentDir is the main directory to check for content
     * @return true if there is at least one folder that is not empty
     */
    private boolean hasNonEmptyFolder(Path parentDir) {
        List<Path> folderContents;
        try (var stream = Files.walk(parentDir, 1)) {
            folderContents = stream.skip(1).collect(Collectors.toList());
        } catch (IOException e) {
            folderContents = new ArrayList<>();
        }

        for (Path content : folderContents) {
            final String contentName = content.getFileName().toString();
            if (MissionConstants.LEGACY_CONFIG_FILENAME.equals(contentName)
                    || MissionConstants.NEW_CONFIG_FILENAME.equals(contentName)
                    || MissionConstants.SETTINGS_TEMP_FILE_NAME.equals(contentName)
                    || MissionConstants.MISSION_SCREENSHOT_FILENAME.equals(contentName)
                    || MissionConstants.MISSION_SCREENSHOT_LOW_RES_FILENAME.equals(contentName)) {
                // ignore existence of this files
                continue;
            }

            if (Files.isDirectory(content)) {
                // scan subfolder if he is empty, if not, make a shortcut and report back immediately
                if (hasNonEmptyFolder(content)) {
                    return true;
                }
            } else {
                // any other file then the ones listed above will cause a non empty reporting back
                return true;
            }
        }

        return false;
    }

    private boolean resideInMatchingsDir(File content) {
        return Optional.ofNullable(content.toPath().getParent())
            .map(c -> c.endsWith(MissionConstants.FOLDER_NAME_MATCHINGS))
            .orElse(false);
    }

    @Override
    public boolean isMissionFolder(File parentDir) {
        return parentDir.isDirectory()
            && (Files.exists(MissionConstants.getLegacyConfigFile(parentDir))
                || Files.exists(MissionConstants.getNewtConfigFile(parentDir)));
    }

    @Override
    public boolean missionHasClone(Mission sourceMission) {
        String clonedName = "Clone of " + sourceMission.getName();
        return missionExists(clonedName);
    }

    @Override
    public boolean missionExists(String missionName) {
        boolean exists = false;
        try (LockedList<MissionInfo> missionInfos = recentMissionInfos.lock()) {
            exists = missionInfos.stream().anyMatch(mission -> mission.getName().equals(missionName));
        }

        if (!exists) {
            Path targetPath = Paths.get(pathSettings.getProjectFolder().toString(), missionName);
            exists = Files.exists(targetPath);
        }

        return exists;
    }

    @Override
    public Mission loadMissionInTemplateMode(Mission currentMission, List<FlightPlanTemplate> templates) {
        templates.forEach(
            template -> {
                template.getFlightPlan().isNameSetProperty().set(true);
                currentMission.addFlightPlanTemplate(template);
            });

        return currentMission;
    }

    @Override
    public Mission unloadMissionFromTemplateMode(Mission currentMission) {
        List<FlightPlanTemplate> templates = Lists.newArrayList(currentMission.flightPlanTemplatesProperty().get());
        templates.forEach(currentMission::closeFlightPlanTemplate);
        currentMission.currentFlightPlanTemplateProperty().set(null);
        return currentMission;
    }

    @Override
    public boolean isValidMissionName(String name) {
        return !isInvalidMissionName(name);
    }

    private boolean isInvalidMissionName(String newMissionName) {
        if (newMissionName == null || newMissionName.isEmpty()) {
            return true;
        }

        try (var recentMissionInfosRef = recentMissionInfos.lock()) {
            for (MissionInfo mission : recentMissionInfosRef) {
                if (newMissionName.equalsIgnoreCase(mission.getName())) {
                    return true;
                }
            }
        }

        if (newMissionName.contains(File.separator) || newMissionName.contains("/") || newMissionName.contains("..")) {
            return true;
            // do not allow the char for folder separator to be present, neither
            // ..
        }

        if (SystemInformation.isWindows()) {
            String[] reservedWindowsNames = SystemInformation.reservedWindowsNames();
            for (String name : reservedWindowsNames) {
                if (name.equalsIgnoreCase(newMissionName)) {
                    return true;
                } // do not allow windows reserved names to be used
            }
        }

        if (newMissionName.equalsIgnoreCase(Mission.DEMO_MISSION_NAME)) {
            return true;
        }

        if (newMissionName.endsWith(".")
                || !newMissionName.trim().equals(newMissionName)) { // valid filename must not end with dot or space
            return true;
        }

        String completName =
            settingsManager.getSection(PathSettings.class).getProjectFolder() + File.separator + newMissionName;
        File tryToRename = null;

        try {
            tryToRename = new File(completName);
            if (tryToRename.createNewFile()) {
                return false; // enable rename button
            }
        } catch (IOException e) {
            LOGGER.warn("Invalid name for mission.", e);
        } finally {
            if (tryToRename != null && tryToRename.exists()) {
                final boolean isDeleted = tryToRename.delete();
                if (!isDeleted) {
                    LOGGER.warn("Cannot delete file {}", tryToRename);
                }
            }
        }

        return true;
    }

    @Override
    public void makeDefaultScreenshot(Mission mission) {
        if (mission == null) {
            return;
        }

        File screenshotFile =
            new File(
                MissionConstants.getScreenshotFolder(mission.getDirectory()),
                MissionConstants.MISSION_SCREENSHOT_FILENAME);
        if (!screenshotFile.exists()) {
            makeScreenshot(mission);
        }
    }

    @Override
    public void makeScreenshot(Mission mission) {
        if (mission == null) {
            return;
        }

        try {
            File folder = MissionConstants.getScreenshotFolder(mission.getDirectory());
            if (folder == null || !folder.exists()) {
                // the mission was deleted since it was empty... so no screenshot needed
                return;
            }

            BufferedImage image = screenshotManager.get().makeAllLayersScreenshot();
            File file = new File(folder, MissionConstants.MISSION_SCREENSHOT_FILENAME);
            File fileLowRes = new File(folder, MissionConstants.MISSION_SCREENSHOT_LOW_RES_FILENAME);
            javax.imageio.ImageIO.write(image, "jpeg", file);

            BufferedImage bufImgLowRes =
                new BufferedImage(
                    ProjectItemViewModel.MAX_THUMBNAIL_WIDTH,
                    ProjectItemViewModel.MAX_THUMBNAIL_HEIGHT,
                    image.getType());
            Graphics2D g = bufImgLowRes.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g.drawImage(
                image, 0, 0, ProjectItemViewModel.MAX_THUMBNAIL_WIDTH, ProjectItemViewModel.MAX_THUMBNAIL_HEIGHT, null);
            g.dispose();
            ImageIO.write(bufImgLowRes, "jpeg", fileLowRes);

            LOGGER.info("Created screenshot of mission: " + mission.getDirectory());
        } catch (Exception e) {
            LOGGER.warn("Cannot create screenshot of mission: " + mission.getDirectory(), e);
        }
    }

    @Override
    public void setMainViewModel(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Override
    public MainViewModel getMainViewModel() {
        return mainViewModel;
    }
}
