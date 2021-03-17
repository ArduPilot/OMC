/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.CreateDatasetSubTasks;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.CreateDatasetTask;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.IUpdateProgressMessage;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import eu.mavinci.core.flightplan.CPhotoLogLine;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatching;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.PhotoCube;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingAlgorithmA;
import eu.mavinci.desktop.helper.FileHelper;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;
import java.io.File;
import java.io.IOException;
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
            IBackgroundTaskManager.BackgroundTask backgroundTask,
            IQuantityStyleProvider quantityStyleProvider)
            throws Exception {
        importImages(files, languageHelper, legacyMatching, backgroundTask, null, null, true, quantityStyleProvider);
    }

    public static void importImages(
            List<File> images,
            MapLayerMatching legacyMatching,
            CreateDatasetTask datasetTask,
            IUpdateProgressMessage updateMethod,
            boolean copyImages,
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider)
            throws Exception {
        importImages(
            images.stream().map(File::toPath).toArray(Path[]::new),
            languageHelper,
            legacyMatching,
            null,
            datasetTask,
            updateMethod,
            copyImages,
            quantityStyleProvider);
    }

    private static void importImages(
            Path[] images,
            ILanguageHelper languageHelper,
            MapLayerMatching legacyMatching,
            IBackgroundTaskManager.BackgroundTask backgroundTask,
            CreateDatasetTask datasetTask,
            IUpdateProgressMessage updateMethod,
            boolean copyImages,
            IQuantityStyleProvider quantityStyleProvider)
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
        final var elevationModel = StaticInjector.getInstance(IElevationModel.class);

        // copy image
        if (copyImages) {
            for (var image : images) {
                if (backgroundTask != null) {
                    backgroundTask.updateMessage(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.analysis.AnalysisViewModel.import.exif.progress",
                            images.length,
                            index / (double)(3 * images.length) * 100));
                    backgroundTask.updateProgress(index, 3 * images.length);
                } else if (datasetTask != null && updateMethod != null) {
                    updateMethod.update(
                        CreateDatasetSubTasks.COPY_IMAGES,
                        index,
                        images.length,
                        image.getFileName(),
                        index,
                        images.length);
                    if (datasetTask.isCancelled()) {
                        return;
                    }
                }

                index = index + 1;

                try {
                    File fileTarget = new File(legacyMatching.getImagesFolder(), image.getFileName().toString());
                    FileHelper.copyFile(image.toFile(), fileTarget);

                } catch (final Exception e) {
                    LOGGER.warn("cant copy image: " + image, e);
                    moveImageAndWarn(copyImages, image);
                }
            }
        }

        // calculate altitude, load exifInfos:
        index = 0;
        for (var image : images) {
            if (backgroundTask != null) {
                backgroundTask.updateMessage(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisViewModel.import.exif.progress",
                        images.length,
                        (index + images.length) / (double)(3 * images.length) * 100));
                backgroundTask.updateProgress(index + 1 * images.length, 3 * images.length);
                backgroundTask.updateProgress(index, 3 * images.length);
            } else if (datasetTask != null && updateMethod != null) {
                updateMethod.update(
                    CreateDatasetSubTasks.LOAD_IMAGES, index, images.length, image.getFileName(), index, images.length);
                if (datasetTask.isCancelled()) {
                    return;
                }
            }

            index = index + 1;
            File fileTarget = new File(legacyMatching.getImagesFolder(), image.getFileName().toString());
            if (!copyImages) {
                fileTarget = image.toFile();
            }

            try {
                imgCount += 1;
                try {
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
                        elevationModel.getElevationAsGoodAsPossible(
                            LatLon.fromDegrees(photoLogLine.lat, photoLogLine.lon));

                    photoLogLines.add(photoLogLine);
                    photos.add(photoCube);
                } catch (final Exception e) {
                    LOGGER.warn("cant import image: " + image, e);
                    moveImageAndWarn(copyImages, fileTarget.toPath());
                    imgCount -= 1;
                }
            } catch (final Exception e) {
                LOGGER.warn("cant import image: " + image, e);
                moveImageAndWarn(copyImages, fileTarget.toPath()); // TODO evtl. dont move because later matched.
            }
        }

        if (imgCount > 0) {
            groundAltSum /= imgCount;
        } else {
            LOGGER.warn("Can't import " + images.length + " image(s), images maybe without positions information");
            StaticInjector.getInstance(IApplicationContext.class)
                .addToast(
                    Toast.of(ToastType.ALERT)
                        .setText(
                            "Can't import " + images.length + " image(s), images maybe without positions information")
                        .create());
            return;
        }

        // insert into layer
        index = 0;
        final double groundAltAvgCm = groundAltSum * 100;
        Position firstBaseStationPosition = null;
        Double distance = 0.;
        for (var image : images) {
            if (backgroundTask != null) {
                backgroundTask.updateMessage(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.analysis.AnalysisViewModel.import.exif.progress",
                        images.length,
                        (index + images.length) / (double)(3 * images.length) * 100));
                backgroundTask.updateProgress(index + 2 * images.length, 3 * images.length);
            } else if (datasetTask != null && updateMethod != null) {
                updateMethod.update(
                    CreateDatasetSubTasks.CREATE_LAYERS,
                    index,
                    images.length,
                    image.getFileName(),
                    index,
                    images.length);
                if (datasetTask.isCancelled()) {
                    return;
                }
            }

            final var photoCube = photos.get(index);
            File fileTarget = new File(legacyMatching.getImagesFolder(), image.getFileName().toString());
            if (!copyImages) {
                fileTarget = image.toFile();
            }

            try {
                if (!photoCube.photoFiles[0].getFile().getAbsolutePath().equals(fileTarget.getAbsolutePath())) {
                    // already moved to error
                    continue;
                }

                // Latest GPS will win! according to info in BOX-1174
                // for RTK no other tagging algo will be necessary, leave this as is.
                // only for RTK
                final var photoLogLine = photoLogLines.get(index);
                Position baseStationPosition = photoCube.photoFiles[0].getExif().getBaseStationPosition();
                legacyMatching.setRTKAvaiable(baseStationPosition);
                if (legacyMatching.isRTKposAvaiable()) {
                    distance = getDistance(firstBaseStationPosition, baseStationPosition, distance);
                    if (firstBaseStationPosition == null) {
                        firstBaseStationPosition = baseStationPosition;
                    }

                    photoLogLine.alt -= baseStationPosition.getAltitude() * 100;
                    photoLogLine.fixType =
                        GPSFixType.parseMeta(
                            photoCube.photoFiles[0].getExif().getBaseStationFixType(), photoLogLine.fixType);
                } else if (photoLogLine.dji_altitude) {
                    photoLogLine.gps_altitude_cm = photoLogLine.alt + groundAltAvgCm;
                } else {
                    photoLogLine.alt -= groundAltAvgCm;
                }

                index = index + 1;

                final var match = new MapLayerMatch(photoCube, photoLogLine, legacyMatching);
                photoCube.setMatch(match);
                legacyMatching.getPicsLayer().addMapLayer(match);
            } catch (final Exception exception) {
                LOGGER.warn("cant import image: " + image, exception);
                moveImageAndWarn(copyImages, fileTarget.toPath());
            }
        }

        if (distance > 0) {
            QuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
            quantityFormat.setMaximumFractionDigits(2);

            String text =
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.analysis.AnalysisViewModel.import.exif.progress.RTKDistance",
                    quantityFormat.format(Quantity.of(distance, Unit.METER), UnitInfo.LOCALIZED_LENGTH));

            LOGGER.warn(text);
            StaticInjector.getInstance(IApplicationContext.class)
                .addToast(Toast.of(ToastType.ALERT).setText(text).create());
        }

        if ((datasetTask != null && datasetTask.isCancelled())
                || (backgroundTask != null && backgroundTask.isCancelled())) {
            return; // TODO IMC-3137 here should no dataset be created
        }

        if (datasetTask == null) {
            try {
                LOGGER.info("Started generating previews for the dataset");
                if (backgroundTask != null) {
                    backgroundTask.updateMessage(
                        languageHelper.getString(
                            "com.intel.missioncontrol.ui.analysis.AnalysisViewModel.import.exif.progress.thumb",
                            legacyMatching.getPicsLayer().sizeMapLayer()));
                    backgroundTask.updateProgress(index, legacyMatching.getPicsLayer().sizeMapLayer());
                    if (backgroundTask.isCancelled()) return;
                } else if (datasetTask != null && updateMethod != null) {
                    if (datasetTask.isCancelled()) {
                        return;
                    }
                }

                legacyMatching.getPicsLayer().generatePreview(backgroundTask, updateMethod, datasetTask);
                if ((datasetTask != null && datasetTask.isCancelled())
                        || (backgroundTask != null && backgroundTask.isCancelled())) {
                    return; // TODO IMC-3137 here ok to generate dataset, thumbs will be generated automatically later
                    // on
                }
            } catch (Exception e) {
                LOGGER.warn("Error while generating previews for the dataset ", e);
            }
        }

        if (modelMismatch) {
            LOGGER.warn(
                "Imported data from different cameras at once, please check: " + lastModel + " != " + nextModel);
        }
    }

    private static Double getDistance(
            Position firstBaseStationPosition, Position baseStationPosition, Double distance) {
        if (firstBaseStationPosition == null) {
            return 0.;
        } else {
            // TODO IMC-661 define significant shift"
            if (!firstBaseStationPosition.equals(baseStationPosition)) {
                Double distance2 =
                    LatLon.ellipsoidalDistance(
                        firstBaseStationPosition,
                        baseStationPosition,
                        Earth.WGS84_EQUATORIAL_RADIUS,
                        Earth.WGS84_POLAR_RADIUS);
                Double elev = Math.abs(firstBaseStationPosition.getAltitude() - baseStationPosition.getAltitude());
                if (Double.isNaN(distance2)) return elev;
                return Math.sqrt(Math.max(distance, distance2 * distance2 + elev * elev));
            } else {
                return distance;
            }
        }
    }

    private static void moveImageAndWarn(boolean copyImages, Path image) throws IOException {
        if (copyImages
                && !image.getParent()
                    .toString()
                    .contains(TaggingAlgorithmA.UNMATCHED_FOLDER)) { // if already in unmatched folder dont copy
            File targetFolderUnmatched = new File(image.getParent().toFile(), TaggingAlgorithmA.UNMATCHED_FOLDER);
            targetFolderUnmatched.mkdirs();
            FileHelper.move(image.toFile(), new File(targetFolderUnmatched, image.toFile().getName()));
        } else {
            LOGGER.info("do not move file to other folder: " + image);
        }
    }
}
