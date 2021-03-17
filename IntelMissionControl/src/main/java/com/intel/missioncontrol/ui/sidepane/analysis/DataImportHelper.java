/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.CreateDatasetSubTasks;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.CreateDatasetTask;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.IUpdateProgressMessage;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoCube;
import eu.mavinci.desktop.helper.FileHelper;
import gov.nasa.worldwind.geom.LatLon;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataImportHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataImportHelper.class);

    public static void importImages(
            Path[] files,
            ILanguageHelper languageHelper,
            MapLayerMatching legacyMatching,
            IBackgroundTaskManager.BackgroundTask backgroundTask)
            throws Exception {
        importImages(files, languageHelper, legacyMatching, backgroundTask, null, null, true);
    }

    public static void importImages(
            List<File> images,
            MapLayerMatching legacyMatching,
            CreateDatasetTask datasetTask,
            IUpdateProgressMessage updateMethod,
            boolean copyImages)
            throws Exception {
        importImages(
            images.stream().map(File::toPath).toArray(Path[]::new),
            null,
            legacyMatching,
            null,
            datasetTask,
            updateMethod,
            copyImages);
    }

    private static void importImages(
            Path[] images,
            ILanguageHelper languageHelper,
            MapLayerMatching legacyMatching,
            IBackgroundTaskManager.BackgroundTask backgroundTask,
            CreateDatasetTask datasetTask,
            IUpdateProgressMessage updateMethod,
            boolean copyImages)
            throws Exception {
        String lastModel = null;
        String nextModel = null;

        boolean modelMismatch = false;
        double groundAltSum = 0.0;
        int imgCount = 0;
        int index = 0;

        legacyMatching.getPicsLayer().setMute(true);

        final var photoLogLines = new ArrayList<CPhotoLogLine>();
        final var photos = new ArrayList<PhotoCube>();
        final var elevationModel = DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);

        for (var image : images) {
            if (backgroundTask != null) {
                backgroundTask.updateMessage(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisViewModel.import.exif.progress",
                        images.length,
                        index / (double)(2 * images.length) * 100));
                backgroundTask.updateProgress(index, 2 * images.length);
            } else if (datasetTask != null && updateMethod != null) {
                updateMethod.update(
                    CreateDatasetSubTasks.COPY_IMAGES, index, images.length, image.getFileName(), index, images.length);
                if (datasetTask.isCancelled()) {
                    return;
                }
            }

            index = index + 1;

            try {
                File fileTarget = new File(legacyMatching.getImagesFolder(), image.getFileName().toString());
                if (copyImages) {
                    FileHelper.copyFile(image.toFile(), fileTarget);
                } else {
                    fileTarget = image.toFile();
                }

                final var photoCube = new PhotoCube(fileTarget);
                final var exifInfos = photoCube.photoFiles[0].getExif();

                nextModel = exifInfos.model;
                if (lastModel == null) {
                    lastModel = nextModel;
                } else if (!lastModel.equals(nextModel)) {
                    modelMismatch = true;
                }

                var photoLogLine = new CPhotoLogLine(exifInfos);
                groundAltSum +=
                    elevationModel.getElevationAsGoodAsPossible(LatLon.fromDegrees(photoLogLine.lat, photoLogLine.lon));
                imgCount += 1;
                photoLogLines.add(photoLogLine);
                photos.add(photoCube);
            } catch (final Exception e) {
                LOGGER.warn("cant import image: " + image, e);
            }
        }

        if (imgCount > 0) {
            groundAltSum /= imgCount;
        } else { // TODO check error handling
            throw new Exception("Can't import images, images maybe without positions information");
        }

        index = 0;
        final int groundAltAvgCm = (int)Math.round(groundAltSum * 100);

        for (var image : images) {
            if (backgroundTask != null) {
                backgroundTask.updateMessage(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisViewModel.import.exif.progress",
                        images.length,
                        (index + images.length) / (double)(2 * images.length) * 100));
                backgroundTask.updateProgress(index + images.length, 2 * images.length);
            } else if (datasetTask != null && updateMethod != null) {
                updateMethod.update(
                    CreateDatasetSubTasks.LOAD_IMAGES, index, images.length, image.getFileName(), index, images.length);
                if (datasetTask.isCancelled()) {
                    return;
                }
            }

            try {
                final var photoLogLine = photoLogLines.get(index);
                final var photoCube = new PhotoCube(image.toFile());

                if (photoLogLine.dji_altitude) {
                    photoLogLine.gps_altitude_cm = photoLogLine.alt + groundAltAvgCm;
                } else {
                    photoLogLine.alt -= groundAltAvgCm;
                }

                index = index + 1;

                final var match = new MapLayerMatch(photoCube, photoLogLine, legacyMatching);
                photoCube.setMatch(match);
                legacyMatching.getPicsLayer().addMapLayer(match);
                match.generatePreview();
                // showing data
            } catch (final Exception exception) {
                LOGGER.warn("cant import image: " + image, exception);
            }
        }

        if (modelMismatch) {
            throw new Exception("Can't import data from different cameras at once: " + lastModel + " != " + nextModel);
        }
    }
}
