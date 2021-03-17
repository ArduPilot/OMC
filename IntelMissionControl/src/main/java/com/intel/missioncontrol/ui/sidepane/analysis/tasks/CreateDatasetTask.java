/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.tasks;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.DataImportHelper;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.AMapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ExifInfos;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ITaggingAlgorithm;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoCube;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingAlgorithmA;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingException;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingSyncVisWindow;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonGeotags.Geotag;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.InterruptedByUserException;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.profiling.requests.CreateDatasetRequest;
import eu.mavinci.flightplan.Flightplan;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventType;
import org.asyncfx.concurrent.Dispatcher;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Vladimir Iordanov */
public class CreateDatasetTask extends IBackgroundTaskManager.BackgroundTask {

    public static final String DONE_EVENT_NAME = "matchingCreated";
    public static final String FAIL_EVENT_NAME = "matchingCreationFailed";
    public static final Event DONE_EVENT = new Event(new EventType<>(EventType.ROOT, DONE_EVENT_NAME));
    public static final Event FAIL_EVENT = new Event(new EventType<>(EventType.ROOT, FAIL_EVENT_NAME));
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDatasetTask.class);

    private final File picFolder;
    private final File baseFolder;
    private final ITaggingAlgorithm taggingAlgorithm;
    private final List<Flightplan> flightPlans;
    private final boolean allowMultipleFeedbacks;
    private final List<File> logFiles;
    private final IQuantityStyleProvider quantityStyleProvider;
    private boolean isAscTecMatching;
    private final FileHelper.GetFotosResult getPhotosResult;
    private final String matchingName;
    private final IHardwareConfiguration hardwareConfiguration;
    private final ILanguageHelper languageHelper;
    private final boolean eraseLogsAfterCopying;
    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final CreateDatasetRequest createDatasetRequest;
    private final GeneralSettings generalSettings;
    private final IMapView mapView;
    private final ISelectionManager selectionManager;
    private final Mission mission;
    private final boolean copyImages;
    private final boolean eraseImages;
    private final boolean onlyIncludeImagesInPlan;

    private MapLayerMatching legacyMatching;
    private Matching matching;
    private TaggingException exception;
    private MatchingStatus oldMatchingStatus;

    public CreateDatasetTask(
            IMapView mapView,
            File picFolder,
            File baseFolder,
            List<Flightplan> flightPlans,
            List<File> logFiles,
            FileHelper.GetFotosResult getPhotosResult,
            IHardwareConfiguration hardwareConfiguration,
            ILanguageHelper languageHelper,
            boolean eraseLogsAfterCopying,
            Matching matching,
            IApplicationContext applicationContext,
            INavigationService navigationService,
            GeneralSettings generalSettings,
            ISelectionManager selectionManager,
            Mission mission,
            boolean copyImages,
            boolean eraseImages,
            boolean onlyIncludeImagesInPlan,
            IQuantityStyleProvider quantityStyleProvider) {
        super(matching.getName());
        this.picFolder = picFolder;
        this.baseFolder = baseFolder;
        this.flightPlans = flightPlans == null ? new ArrayList<Flightplan>() : flightPlans;
        this.logFiles = logFiles == null ? new ArrayList<File>() : logFiles;
        this.getPhotosResult = getPhotosResult;
        this.hardwareConfiguration = hardwareConfiguration;
        this.eraseLogsAfterCopying = eraseLogsAfterCopying;
        this.matching = matching;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.generalSettings = generalSettings;
        this.mapView = mapView;
        this.selectionManager = selectionManager;
        this.mission = mission;
        this.copyImages = copyImages;

        this.eraseImages = eraseImages && copyImages;
        this.onlyIncludeImagesInPlan = onlyIncludeImagesInPlan;

        matchingName = getName();
        this.name = "Importing " + matchingName;

        allowMultipleFeedbacks =
            hardwareConfiguration != null
                ? hardwareConfiguration
                    .getPrimaryPayload(IGenericCameraConfiguration.class)
                    .getDescription()
                    .getExifModels()
                    .contains("RedEdge")
                : false;
        taggingAlgorithm = TaggingAlgorithmA.createNewDefaultTaggingAlgorithm();
        this.languageHelper = languageHelper;
        this.quantityStyleProvider = quantityStyleProvider;

        createDatasetRequest = new CreateDatasetRequest(matchingName);
        legacyMatching = (MapLayerMatching)matching.getLegacyMatching();
    }

    @Override
    protected Void call() throws Exception {
        StaticInjector.getInstance(IProfilingManager.class).requestStarting(createDatasetRequest);
        doTransfer();
        // dont check here if legacy matching is loaded, since this will done delayed in the UI thread
        /*if (getLegacyMatching() == null && exception == null && !isCancelled()) {
            exception = new TaggingException("no dataset created", "target dataset cant be found");
        }*/

        if (exception != null) {
            exception.printStackTrace();
            throw exception;
        }

        return null;
    }

    @Override
    protected void cancelled() {
        if (matching.getStatus().equals(MatchingStatus.IMPORTED)) {
            succeeded();
            return;
        }

        Debug.printStackTrace("cancelled");
        matching.statusProperty().set(oldMatchingStatus);
        super.cancelled();
        fireEvent(FAIL_EVENT);
        StaticInjector.getInstance(IProfilingManager.class).requestFinished(createDatasetRequest);
    }

    @Override
    protected void failed() {
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.run(
            () -> {
                matching.statusProperty().set(oldMatchingStatus);
                applicationContext.addToast(
                    Toast.of(ToastType.ALERT).setText(getTaskException().getShortMessage()).setShowIcon(true).create());
            });
        super.failed();
        fireEvent(FAIL_EVENT);
        StaticInjector.getInstance(IProfilingManager.class).requestFinished(createDatasetRequest);
    }

    @Override
    protected void succeeded() {
        Toast.ToastBuilder toastBuilder =
            Toast.of(ToastType.INFO)
                .setText(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.transferCompleteMessage"));

        // if mission was closed in the meantime, dont show the action
        if (applicationContext.getCurrentMission() == mission) {
            toastBuilder =
                toastBuilder.setAction(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.transferActionLinkMessage"),
                    false,
                    true,
                    () -> {
                        selectionManager.setSelection(matching.getLegacyMatching());
                        mission.setCurrentMatching(matching);
                        navigationService.navigateTo(WorkflowStep.DATA_PREVIEW);
                        navigationService.navigateTo(SidePanePage.VIEW_DATASET);
                        mapView.goToSectorAsync(matching.getSector(), matching.getMaxElev());
                    },
                    Platform::runLater);
        }

        applicationContext.addToast(toastBuilder.create());

        super.succeeded();
        fireEvent(DONE_EVENT);
        StaticInjector.getInstance(IProfilingManager.class).requestFinished(createDatasetRequest);
    }

    private long[] progressOffset = new long[CreateDatasetSubTasks.values().length];

    private long maxValue = 0;

    private void updateProgressMessage(
            CreateDatasetSubTasks step, double subProgress, double subTotal, Object... msParams) {
        long progress;
        if (subTotal <= 0) {
            progress = progressOffset[step.ordinal()];
        } else {
            progress = Math.round(progressOffset[step.ordinal()] + step.duration * subProgress / subTotal);
        }

        updateProgress(progress, maxValue);
        updateMessage(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.analysis.CreateDatasetTask." + step.name(), msParams));
    }

    Toast transferInProgressToast;

    private void doTransfer() throws Exception {
        oldMatchingStatus = matching.getStatus();
        final long constructionTime = System.currentTimeMillis();

        File baseFolder = checkFolder();
        if (baseFolder == null) return;

        Dispatcher dispatcher = setStatusTransferring();

        // parsing logfile
        boolean jsonLogs = false;
        try {
            jsonLogs = parseLogfiles(jsonLogs);
        } catch (Exception e) {
            return;
        }
        // filename for legacyMatching file
        final File save = new File(baseFolder, AMapLayerMatching.DEFAULT_FILENAME);
        if (save.exists() && !save.delete()) {
            exception = new TaggingException("delete data failed", "Could not delete old files " + save);
            return;
        }

        if (isCancelled()) {
            return;
        }

        if (oldMatchingStatus.equals(MatchingStatus.NEW)) {
            legacyMatching.getPicsLayer().removeAllLayers(true);
        }

        // copy flightplans
        File fpFolder = new File(baseFolder, AMapLayerMatching.FOLDER_NAME_FLIGHTPLANS_SUBFOLDER);
        if (!fpFolder.exists() && !fpFolder.mkdirs()) {
            exception = new TaggingException("folder creation failed", "Could not create folder " + fpFolder);
            return;
        }

        int index = 0;
        boolean copiedLogs = false;

        for (Flightplan source : flightPlans) {
            updateProgressMessage(
                CreateDatasetSubTasks.COPY_LOG_FILES,
                index,
                flightPlans.size(),
                source.getName(),
                index,
                flightPlans.size());
            index++;
            try {
                if (!source.getResourceFile().exists()) {
                    applicationContext.addToast(
                        Toast.of(ToastType.INFO)
                            .setText(
                                languageHelper.getString(
                                        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.unableToSave")
                                    + source.getName())
                            .create());
                } else {
                    source.saveToLocation(new File(fpFolder, source.getResourceFile().getName()));
                    copiedLogs = true;
                }
            } catch (Exception e2) {
                applicationContext.addToast(
                    Toast.of(ToastType.INFO)
                        .setText(
                            languageHelper.getString(
                                    "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.unableToSave")
                                + source.getName())
                        .create());
                // exception = new TaggingException("Copy Mission Failes", "Unable to save mission to new
                // folder", e2);
                // return;
            }
        }

        if (!flightPlans.isEmpty()) {
            if (this.onlyIncludeImagesInPlan) {
                legacyMatching.setFlightplanSelection(setFlightplanSelection(flightPlans));
            }

            legacyMatching.setAreaEnabled(legacyMatching.getAreaEnabled() || this.onlyIncludeImagesInPlan);
            legacyMatching.setFlightplanEnabled(legacyMatching.getFlightplanEnabled() || this.onlyIncludeImagesInPlan);
        }

        if (isCancelled()) {
            return;
        }

        // copy logfile if nessesary
        boolean deleteLogs = this.eraseLogsAfterCopying;
        boolean jsonLog = false;

        index = 0;

        File targetFolder = new File(baseFolder, AMapLayerMatching.FOLDER_NAME_PICS_SUBFOLDER);
        TreeSet<File> targetFiles = getPhotosResult != null ? new TreeSet<>(getPhotosResult.fotos) : new TreeSet<>();

        boolean deletePics = eraseImages;
        for (File logFile : logFiles) {
            jsonLog = parseLogfile(logFile);
            isAscTecMatching = MFileFilter.ascTecLogFolder.acceptTrinityLog(logFile);
            updateProgressMessage(
                CreateDatasetSubTasks.COPY_LOG_FILES,
                index,
                logFiles.size(),
                logFile.getName(),
                index,
                logFiles.size());
            index++;
            if (isAscTecMatching) {
                if (logFile.getParentFile().equals(baseFolder)) {
                    deleteLogs = false;
                    continue;
                }

                String logFileName = logFile.getName();
                File fTarget = new File(baseFolder, "asctec_log_" + logFileName);
                if (fTarget.equals(logFile)) {
                    deleteLogs = false;
                    continue;
                }

                fTarget = FileHelper.getNextFreeFilename(fTarget);
                try {
                    FileHelper.copyDirectorySynchron(logFile, fTarget);
                } catch (Exception e2) {
                    exception =
                        new TaggingException(
                            "unable to copy logs;" + logFile,
                            "Unable to copy asctec log folder " + logFile + " to " + fTarget,
                            e2);
                    return;
                }
            } else {
                if (!logFile.exists()) {
                    deleteLogs = false;
                    continue;
                }

                if (logFile.getName().startsWith(CPhotoLogLine.PREFIX_MATCHED_PLG)
                        && FileHelper.equals(logFile.getParentFile(), baseFolder)) {
                    deleteLogs = false;
                    continue;
                }

                File logFileNew = new File(baseFolder, CPhotoLogLine.PREFIX_MATCHED_PLG + logFile.getName());

                if (jsonLog) {
                    // each image in a json log can be from a differnet folder, different from picFolder!
                    int baseSourcePathStringLen =
                        logFile.getParentFile().getParentFile().getAbsolutePath().length() + 1;

                    String name1 =
                        logFile.getAbsolutePath()
                            .substring(baseSourcePathStringLen)
                            .replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("_"));

                    logFileNew = new File(baseFolder, CPhotoLogLine.PREFIX_MATCHED_PLG + name1);
                }

                try {
                    FileHelper.copyFile(logFile, logFileNew);
                } catch (IOException e2) {
                    exception = new TaggingException("Unable to move images", "Unable to move Picture logfile", e2);
                    return;
                }
            }

            if (isCancelled()) {
                return;
            }

            // if rededge dataset, copy metadata if possible
            final File picFolderParent = picFolder != null ? picFolder.getParentFile() : null;
            if (picFolderParent != null && picFolderParent.exists()) {
                File[] auxFiles =
                    new File[] {new File(picFolderParent, "diag.dat"), new File(picFolderParent, "paramlog.dat")};

                index = 0;
                for (File fAux : auxFiles) {
                    updateProgressMessage(
                        CreateDatasetSubTasks.COPY_AUX_FILE,
                        index,
                        auxFiles.length,
                        fAux.getName(),
                        index,
                        auxFiles.length);
                    index++;
                    if (!fAux.exists()) {
                        continue;
                    }

                    try {
                        File fAuxTarget = new File(baseFolder, fAux.getName());
                        FileHelper.copyDirectorySynchron(fAux, fAuxTarget);
                    } catch (IOException e2) {
                        exception =
                            new TaggingException("unable to copy aux file", "copying this file failed: " + fAux, e2);
                        return;
                    }
                }
            }

            if (jsonLog) {
                // copy potentially falcon payload logfiles for backup
                File[] auxFiles = new File[] {new File(picFolder, "logs"), new File(picFolder, "occupancy.map")};

                index = 0;
                for (File fAux : auxFiles) {
                    updateProgressMessage(
                        CreateDatasetSubTasks.COPY_AUX_FILE,
                        index,
                        auxFiles.length,
                        fAux.getName(),
                        index,
                        auxFiles.length);
                    index++;
                    if (!fAux.exists()) {
                        continue;
                    }

                    try {
                        File fAuxTarget = new File(baseFolder, fAux.getName());
                        FileHelper.copyDirectorySynchron(fAux, fAuxTarget);
                    } catch (IOException e2) {
                        exception =
                            new TaggingException("unable to copy aux file", "copying this file failed: " + fAux, e2);
                        return;
                    }
                }
            }

            if (isCancelled()) {
                return;
            }

            // if nessesary, copy images to new place, make sure that names become unique, since some
            // compatibleCameraIds are using different folder to keep non unique file names

            if (!this.copyImages) {
                deletePics = false;
            }

            if (picFolder != null && !FileHelper.equals(targetFolder, picFolder)) {
                if (!targetFolder.exists() && !targetFolder.mkdirs()) {
                    exception =
                        new TaggingException("folder creation failed", "Could not create folder " + targetFolder);
                    return;
                }

                try {
                    int baseSourcePathStringLen = getPhotosResult.picFolder.getAbsolutePath().length() + 1;
                    index = 0;
                    final List<File> images = getPhotosResult.fotos;

                    for (File f : images) {
                        updateProgressMessage(
                            CreateDatasetSubTasks.COPY_IMAGES, index, images.size(), f.getName(), index, images.size());
                        index++;
                        if (isCancelled()) {
                            return;
                        }

                        if (f.isDirectory()) {
                            continue;
                        }

                        if (FileHelper.equals(targetFolder, f.getParentFile())) {
                            LOGGER.info(
                                "Image already available in dataset, images will not get deleted / removed from targetFiles: "
                                    + f.getAbsolutePath());
                            applicationContext.addToast(
                                Toast.of(ToastType.INFO)
                                    .setText("Image already available in dataset, images will not get deleted")
                                    .create());
                            deletePics = false;
                            targetFiles.remove(
                                f); // dont check this file later, belongs already to the dataset, this can be used
                            continue; // dont rename files if they dont have to be moved to another folder
                        }

                        if (jsonLog) {
                            // each image in a json log can be from a differnet folder
                            baseSourcePathStringLen = f.getParentFile().getParentFile().getAbsolutePath().length() + 1;
                        }

                        String name1 =
                            f.getAbsolutePath()
                                .substring(baseSourcePathStringLen)
                                .replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("_"));
                        File targetFileTmp = new File(targetFolder.getAbsolutePath(), name1);

                        File targetFile;
                        if (jsonLog) {
                            targetFile = targetFileTmp;
                        } else {
                            final String imgNameInject =
                                "_" + Math.round(taggingAlgorithm.getLogsAll().first().getTimestamp() * 1000);

                            if (name1.contains(imgNameInject)) {
                                targetFile =
                                    targetFileTmp; // dont copy all images again and again on second legacyMatching
                                // try
                            } else {
                                targetFile = FileHelper.injectIntoFileName(targetFileTmp, imgNameInject);
                            }
                        }

                        if (this.copyImages) {
                            if (targetFile.exists() && f.length() == targetFile.length()) {
                                // dont overwrite existing file -> speed up
                                continue;
                            }

                            FileHelper.copyFile(f, targetFile);
                        } else {
                            targetFile = f;
                        }
                    }

                } catch (Exception e1) {
                    exception = new TaggingException("could not move images", "copying images into project failed", e1);
                    return;
                }
            }
        }

        if (isCancelled()) {
            return;
        }

        matching.startTransferring(baseFolder, hardwareConfiguration);
        // jsonLog = false;
        {
            boolean taggingDone = false;
            index = 0;
            legacyMatching.getPicsLayer().setMute(true);
            for (File logFile : logFiles) {
                jsonLog = parseLogfile(logFile);
                int missedImages = 0;
                if (jsonLog) {
                    // taggingDone=true;
                    try (InputStream stream = new FileInputStream(logFile);
                        BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            updateProgressMessage(
                                CreateDatasetSubTasks.LOAD_IMAGES,
                                index,
                                taggingAlgorithm.getLogsAll().size(),
                                logFile.getName(),
                                index,
                                taggingAlgorithm.getLogsAll().size());
                            if (isCancelled()) {
                                return;
                            }

                            try {
                                strLine = strLine.trim();
                                if (strLine.startsWith("[")) {
                                    strLine = strLine.substring(1, strLine.length());
                                }

                                if (strLine.endsWith(",") || strLine.endsWith("]")) {
                                    strLine = strLine.substring(0, strLine.length() - 1);
                                }

                                strLine = strLine.trim();
                                if (strLine.isEmpty()) {
                                    continue;
                                }

                                Geotag geotag = Geotag.fromJson(strLine);
                                CPhotoLogLine plg = new CPhotoLogLine(geotag);

                                int baseSourcePathStringLen =
                                    logFile.getParentFile().getParentFile().getAbsolutePath().length() + 1;
                                String name1 =
                                    logFile.getParentFile()
                                            .getAbsolutePath()
                                            .substring(baseSourcePathStringLen)
                                            .replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("_"))
                                        + Matcher.quoteReplacement("_")
                                        + geotag.filename;
                                File image;
                                if (copyImages) {
                                    image = new File(targetFolder.getAbsolutePath(), name1);
                                } else {
                                    image = new File(logFile.getParentFile(), geotag.filename);
                                }
                                /*if (!fromMatchedData) {
                                    final String imgNameInject = logFile.getParentFile().getName();
                                    image = FileHelper.injectIntoFileName(image, imgNameInject);
                                }*/
                                if (image.exists()) {
                                    PhotoCube photoCube = new PhotoCube(image);
                                    MapLayerMatch match = new MapLayerMatch(photoCube, plg, legacyMatching);
                                    photoCube.setMatch(match);
                                    legacyMatching.getPicsLayer().addMapLayer(match);
                                    targetFiles.remove(new File(logFile.getParentFile(), geotag.filename));
                                } else {
                                    missedImages++;
                                }
                            } catch (Throwable e1) {
                                exception =
                                    new TaggingException(
                                        "No image found",
                                        "Can't add logfile " + logFile + " to dataset, no images found",
                                        e1);
                                return;
                            }

                            index++;
                        }
                    } catch (Exception e) {
                        exception = new TaggingException("log parser err", "can't load parse logfile " + logFile, e);
                        return;
                    }

                    if (missedImages > 0) {
                        applicationContext.addToast(
                            Toast.of(ToastType.ALERT)
                                .setText(
                                    "Can't add logfile "
                                        + logFile
                                        + "completely to dataset, "
                                        + missedImages
                                        + " images not found")
                                .create());
                    }
                } else {

                    // 1 time or for all logs (non jsons)?
                    if (!taggingDone) {
                        taggingDone = true;
                        // TODO IMC-3137 check what with cameraConfiguration:   I think to be removed. But does it work
                        // then?
                        IGenericCameraConfiguration cameraConfiguration =
                            hardwareConfiguration == null
                                ? null
                                : hardwareConfiguration.getPrimaryPayload(IGenericCameraConfiguration.class);
                        IGenericCameraDescription cameraDescription =
                            hardwareConfiguration == null ? null : cameraConfiguration.getDescription();
                        ILensDescription lensDescription =
                            hardwareConfiguration == null ? null : cameraConfiguration.getLens().getDescription();
                        IPlatformDescription platformDescription =
                            hardwareConfiguration == null ? null : hardwareConfiguration.getPlatformDescription();

                        ITaggingAlgorithm.ProgressCallbackImgLoading progressCallbackImgLoading =
                            new ITaggingAlgorithm.ProgressCallbackImgLoading() {

                                @Override
                                public void progress(File image, long no, long total) {
                                    updateProgressMessage(
                                        CreateDatasetSubTasks.LOAD_IMAGES, no, total, image.getName(), no, total);
                                }

                                @Override
                                public boolean isCanceled() {
                                    return CreateDatasetTask.this.isCancelled();
                                }
                            };

                        try {
                            File imageFolder;
                            if (copyImages) {
                                imageFolder = targetFolder;
                            } else {
                                imageFolder = picFolder;
                            }

                            taggingAlgorithm.loadPictures(
                                imageFolder,
                                true,
                                cameraDescription.getBandNamesSplit()
                                    .length, // TODO IMC-3137 ? from hardware description / evtl. for each image type
                                false,
                                progressCallbackImgLoading);
                        } catch (InterruptedByUserException e) {
                            return;
                        } catch (Throwable e1) {
                            exception = new TaggingException("Unable to load images", "Unable to load images", e1);
                            return;
                        }

                        if (isCancelled()) {
                            return;
                        }

                        ITaggingAlgorithm.ProgressCallbackOptimizing progressCallbackOptimizing =
                            new ITaggingAlgorithm.ProgressCallbackOptimizing() {
                                @Override
                                public void progress(String algName, long no, long total) {
                                    updateProgressMessage(
                                        CreateDatasetSubTasks.OPTIMIZE_DATASET, no, total, algName, no, total);
                                }

                                @Override
                                public boolean isCanceled() {
                                    return CreateDatasetTask.this.isCancelled();
                                }
                            };

                        try {
                            if (!taggingAlgorithm.getPhotosAll().isEmpty()) {
                                taggingAlgorithm.optimizeMatching(
                                    lensDescription
                                        .getMaxTimeVariation()
                                        .convertTo(Unit.SECOND)
                                        .getValue()
                                        .doubleValue(), // TODO IMC-3137 ? from hardware description / evtl. for each
                                    // image type
                                    platformDescription
                                        .getGpsType(), // TODO IMC-3137 ? from hardware description / evtl. for each
                                    // image type
                                    null,
                                    progressCallbackOptimizing);
                            } else {
                                int k = 0;
                                for (CPhotoLogLine log : taggingAlgorithm.getLogsAll()) {
                                    k++;
                                    File dummyFile = new File(targetFolder, "dummyImage_" + k + ".png");
                                    FileHelper.writeResourceToFile(
                                        "com/intel/missioncontrol/gfx/dummy-video.png", dummyFile);
                                    ExifInfos exifInfos = new ExifInfos();
                                    exifInfos.timestamp = log.getTimestamp();
                                    PhotoCube photo = new PhotoCube(dummyFile, exifInfos);
                                    photo.logTmp = log;
                                    taggingAlgorithm.getPhotosMatched().add(photo);

                                    targetFiles.remove(photo); // CHECK correct matched file
                                }
                            }

                        } catch (InterruptedByUserException e) {
                            return;
                        } catch (TaggingException e) {
                            exception = e;
                            return;
                        } catch (Throwable e1) {
                            exception =
                                new TaggingException(
                                    "Can't geotag data", "failed to find match between metadata and images", e1);
                            return;
                        }

                        if (isCancelled()) {
                            return;
                        }

                        try {
                            legacyMatching.setRTKAvaiable(taggingAlgorithm);
                            legacyMatching.setBandNames(
                                cameraDescription
                                    .getBandNamesSplit()); // ? from hardware description / evtl. for each image type
                            index = 0;
                            for (PhotoCube photoCube : taggingAlgorithm.getPhotosMatched()) {
                                updateProgressMessage(
                                    CreateDatasetSubTasks.CREATE_LAYERS,
                                    index,
                                    taggingAlgorithm.getPhotosMatched().size(),
                                    "",
                                    index,
                                    taggingAlgorithm.getPhotosMatched().size());
                                index++;
                                if (isCancelled()) {
                                    return;
                                }

                                if (photoCube.logTmp == null) {
                                    continue;
                                }

                                try {
                                    MapLayerMatch match =
                                        new MapLayerMatch(photoCube, photoCube.logTmp, legacyMatching);
                                    photoCube.setMatch(match);
                                    legacyMatching.getPicsLayer().addMapLayer(match);

                                    File sourceFile;
                                    if (copyImages) {
                                        //   match.getResourceFile().getName() => get original name! Gets renamed.
                                        sourceFile = new File(picFolder, match.getResourceFile().getName());
                                        if (!targetFiles.contains(sourceFile)) {
                                            final String imgNameInject =
                                                "_"
                                                    + Math.round(
                                                        taggingAlgorithm.getLogsAll().first().getTimestamp() * 1000);
                                            if (sourceFile.getName().contains(imgNameInject)) {
                                                String name1 =
                                                    match.getResourceFile().getName().replace(imgNameInject, "");
                                                sourceFile = new File(picFolder, name1);
                                            }
                                        }
                                    } else {
                                        sourceFile = match.getResourceFile();
                                    }

                                    try {
                                        targetFiles.remove(sourceFile);
                                    } catch (Throwable e2) {
                                        LOGGER.info("cant remove matched image from list", e2);
                                    }
                                } catch (Throwable e1) {
                                    exception =
                                        new TaggingException(
                                            "cant add image to dataset", "can't load single legacyMatching", e1);
                                    return;
                                }
                            }

                            if (isCancelled()) {
                                return;
                            }

                            // create sync statistics and write it to a file. in user level==Debugging, show the window
                            dispatcher.run(
                                () -> {
                                    new TaggingSyncVisWindow(
                                        taggingAlgorithm,
                                        new File(baseFolder, "syncPattern-" + System.currentTimeMillis() + ".png"),
                                        generalSettings.getOperationLevel() == OperationLevel.DEBUG,
                                        cameraConfiguration,
                                        hardwareConfiguration);
                                });

                            if (isCancelled()) {
                                return;
                            }
                        } catch (Throwable e) {
                            exception =
                                new TaggingException("finalization failed", "can't initialize final dataset", e);
                            return;
                        }
                    }
                }
            }
        }

        // images are left: put them additionally into it, using different import:

        if (targetFiles.size() > 0) {
            List<File> a = new ArrayList<File>(targetFiles);
            DataImportHelper.importImages(
                a,
                legacyMatching,
                this,
                this::updateProgressMessage,
                copyImages,
                languageHelper,
                quantityStyleProvider);
        }

        if (legacyMatching.getPicsLayer().sizeMapLayer() == 0 && flightPlans.isEmpty()) {
            LOGGER.error("No images imported!");
            deletePics = false;
            if (oldMatchingStatus.equals(MatchingStatus.NEW)) {
                exception = new TaggingException("No images imported", "No images imported");
                return;
            }
        }

        // import s from flightplans
        index = 0;
        for (Flightplan flightPlan : flightPlans) {
            updateProgressMessage(
                CreateDatasetSubTasks.IMPORT_FLIGHT_PLANS,
                index,
                flightPlans.size(),
                flightPlan.getName(),
                index,
                flightPlans.size());
            index++;
            if (isCancelled()) {
                return;
            }

            legacyMatching.getPicAreasLayer().tryAddPicAreasFromFlightplan(flightPlan);
        }

        if (isCancelled()) {
            return;
        }

        updateProgressMessage(CreateDatasetSubTasks.CALCULATE_LAYERS, 0, 1);
        legacyMatching.getCoverage().updateCameraCorners();
        // dont do the preview generation whith the layer above, because it is only a temporary one for
        // generating the settings file!
        updateProgressMessage(CreateDatasetSubTasks.SAVE_LAYERS, 0, 1);
        LOGGER.info("Matching done, will save: " + matchingName);
        this.legacyMatching.guessGoodFilters();
        this.legacyMatching.setVisible(true);
        this.legacyMatching.getPicAreasLayer().setVisible(true);
        this.legacyMatching.mapLayerVisibilityChanged(this.legacyMatching, true);
        dispatcher.run(
            () -> {
                matching.saveResourceFile();
                if (!mission.getMatchings().contains(matching)) {
                    mission.getMatchings().add(matching);
                }

                mission.save();
                matching.statusProperty().set(MatchingStatus.IMPORTED);
                LOGGER.info("saved matching: " + matchingName);
            });

        LOGGER.info("removing matched data: " + matchingName);
        if (eraseLogsAfterCopying) {
            if (deletePics && eraseImages) {
                index = 0;
                for (File f : getPhotosResult.fotos) {
                    updateProgressMessage(
                        CreateDatasetSubTasks.ERASE_SD,
                        index,
                        getPhotosResult.fotos.size(),
                        f.getName(),
                        index,
                        getPhotosResult.fotos.size());
                    try {
                        FileHelper.deleteDir(languageHelper, f.getParentFile(), true);
                    } catch (Exception e) {
                        LOGGER.info("issues deleting file", e);
                    }
                }
            }

            if (deleteLogs) {
                for (File log : logFiles) {
                    jsonLog = parseLogfile(log);
                    updateProgressMessage(
                        CreateDatasetSubTasks.ERASE_SD, index, logFiles.size(), log.getName(), index, logFiles.size());
                    try {
                        FileHelper.deleteDir(languageHelper, (jsonLog) ? log.getParentFile() : log, true);
                    } catch (Exception e) {
                        LOGGER.info("issues deleting file", e);
                    }
                }
            }
        }

        if (isCancelled()) {
            return;
        }

        final long thisTime = System.currentTimeMillis();
        final long usedTime = thisTime - constructionTime;
        LOGGER.info("Generated dataset, Duration: " + StringHelper.secToShortDHMS(usedTime / 1000.));

        try {
            LOGGER.info("Started generating previews for the dataset");
            updateProgressMessage(
                CreateDatasetSubTasks.GENERATE_THUMBFILES,
                0,
                legacyMatching.getPicsLayer().sizeMapLayer(),
                "",
                0,
                legacyMatching.getPicsLayer().sizeMapLayer());
            legacyMatching.getPicsLayer().generatePreview(this::updateProgressMessage, this);
            updateProgressMessage(
                CreateDatasetSubTasks.GENERATE_THUMBFILES,
                legacyMatching.getPicsLayer().sizeMapLayer(),
                legacyMatching.getPicsLayer().sizeMapLayer(),
                "",
                0,
                legacyMatching.getPicsLayer().sizeMapLayer());
        } catch (Exception e) {
            LOGGER.warn("Error while generating previews for the dataset ", e);
        }
    }

    private String setFlightplanSelection(List<Flightplan> flightPlans) {
        String[] flightplan = new String[1];
        flightPlans.forEach(
            f -> {
                flightplan[0] =
                    flightplan[0] != null
                        ? flightplan[0] + f.getResourceFile().getAbsolutePath() + ","
                        : f.getResourceFile().getAbsolutePath() + ",";
            });
        return flightplan[0];
    }

    private boolean parseLogfile(File logFile) throws Exception {
        int jsonCount = 0;
        int indexlogFiles = 0;
        boolean jsonLog = false;
        // for (File logFile : logFiles) {
        try {
            if (MFileFilter.photoJsonFilter.accept(logFile.getName())) {
                jsonLog = true;
            }
            // taggingAlgorithm.loadLogfile(logFile, allowMultipleFeedbacks);  already tagged using paresLogfile
        } catch (Exception e) {
            exception = new TaggingException("parsing failed", "unable to parse logfile: " + logFile, e);
            throw exception; // return;
        }
        // }

        return jsonLog;
    }

    private boolean parseLogfiles(boolean jsonLogs) throws Exception {
        int jsonCount = 0;
        int indexlogFiles = 0;

        for (File logFile : logFiles) {
            try {
                updateProgressMessage(
                    CreateDatasetSubTasks.PARSE_LOG_FILES,
                    indexlogFiles,
                    logFiles.size(),
                    logFile.getName(),
                    indexlogFiles,
                    logFiles.size());

                indexlogFiles++;
                if (MFileFilter.photoJsonFilter.accept(logFile.getName())) {
                    jsonCount++;
                    jsonLogs = true;
                }

                if (jsonCount != indexlogFiles && jsonCount != 0) {
                    final String[] message =
                        new String[] {"mixed log file types", "process JSON and non JSON logs at the same time"};
                    LOGGER.info("{}, {}", message[0], message[1]);
                    // exception = new TaggingException(message[0], message[1]);
                    // return;
                }

                taggingAlgorithm.loadLogfile(logFile, allowMultipleFeedbacks);
            } catch (Exception e) {
                exception = new TaggingException("parsing failed", "unable to parse logfile: " + logFile, e);
                throw exception; // return;
            }
        }

        if (logFiles.size() > 0) { // enabling without logfiles
            if (taggingAlgorithm.getLogsAll().isEmpty()) {
                final String[] message =
                    new String[] {"log data corrupted", "log data corrupted: no log entries found"};
                LOGGER.error("{}, {}", message[0], message[1]);
                exception = new TaggingException(message[0], message[1]);
                throw exception; // return;
            }

            if (taggingAlgorithm.getLogsAll().first().getTimestamp() <= 0) {
                CPhotoLogLine photoLogLine = taggingAlgorithm.getLogsAll().first();
                LOGGER.error("log data corrupted: log timestamps are zero: {}", photoLogLine);
                exception =
                    new TaggingException(
                        "log data corrupted", "log data corrupted: log timestamps are zero:" + photoLogLine);
                throw exception; // return;
            }
        }

        return jsonLogs;
    }

    @Nullable
    private File checkFolder() {
        File folder = new File(MissionConstants.getMatchingsFolder(this.baseFolder), matchingName);
        File baseFolder = folder;
        if (!baseFolder.exists() && !baseFolder.mkdirs()) {
            exception = new TaggingException("folder creation failed", "Could not create folder " + baseFolder);
            return null;
        }

        return baseFolder;
    }

    @NonNull
    private Dispatcher setStatusTransferring() {
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.run(
            () -> {
                matching.statusProperty().set(MatchingStatus.TRANSFERRING);
            });
        transferInProgressToast =
            Toast.of(ToastType.INFO)
                .setText(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.transferInProgressMessage"))
                .setOnDismissed(t -> transferInProgressToast = null)
                .create();

        applicationContext.addToast(transferInProgressToast);

        maxValue = 0;
        for (CreateDatasetSubTasks task : CreateDatasetSubTasks.values()) {
            progressOffset[task.ordinal()] = maxValue;
            maxValue += task.duration;
        }

        updateProgressMessage(CreateDatasetSubTasks.START, 0, 1);
        return dispatcher;
    }

    public TaggingException getTaskException() {
        return exception;
    }
}
