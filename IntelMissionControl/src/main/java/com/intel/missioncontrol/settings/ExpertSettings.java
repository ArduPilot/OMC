/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import eu.mavinci.desktop.gui.wwext.search.CombinedGazetteer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Deprecated(since = "Switch to async properties")
@SettingsMetadata(section = "expertSettings")
public class ExpertSettings implements ISettings {

    private final AsyncBooleanProperty enableBluetooth =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    // BackendState
    private final IntegerProperty allowReconnectAfterMs =
        new SimpleIntegerProperty(this, "allowReconnectAfterMs", 5000);

    // FTPManager
    private final AsyncBooleanProperty enableAutoPLGdownload =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final StringProperty geoCoderProvider =
        new SimpleStringProperty(this, "geocoderProvider", CombinedGazetteer.GEOCODER_PROVIDER_MAPBOX);

    private final AsyncBooleanProperty disableSingleThreading =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    // WWFactory
    private StringProperty mapsFetchPoolSize = new SimpleStringProperty(this, "mapsFetchPoolSize");
    private StringProperty mapsFetchQueueSize = new SimpleStringProperty(this, "mapsFetchQueueSize");
    private SimpleStringProperty retrieverPoolSize = new SimpleStringProperty(this, "retrieverPoolSize");
    private SimpleStringProperty retrieverQueueSize = new SimpleStringProperty(this, "retrieverQueueSize");

    // AerialPinholeImageLayer
    private final IntegerProperty maxTextureSize = new SimpleIntegerProperty(this, "MaxTextureSize", 4 * 1024);

    // ContourLinesLayer
    private final IntegerProperty contourLinesMaxVisLines =
        new SimpleIntegerProperty(this, "contourLinesMaxVisLines", 50);
    private final IntegerProperty contourLinesStep = new SimpleIntegerProperty(this, "contourLinesStep", 10);
    private final IntegerProperty contourLinesStepBold = new SimpleIntegerProperty(this, "contourLinesStepBold", 40);
    private final IntegerProperty contourLinesStepBoldBlue =
        new SimpleIntegerProperty(this, "contourLinesStepBoldBlue", 80);
    private final IntegerProperty contourLinesWidth = new SimpleIntegerProperty(this, "contourLinesWidth", 1);
    private final IntegerProperty contourLinesWidthBold = new SimpleIntegerProperty(this, "contourLinesWidthBold", 2);

    // FPsim
    private final AsyncBooleanProperty computePreviewSim =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    // AirplaneAscTecConnector
    private final AsyncBooleanProperty autoTakeoff =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    // cameraSettings
    private final AsyncBooleanProperty cameraRtkNewLevelArm =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    // CameraHelper
    private DoubleProperty cameraNodalPoint = new SimpleDoubleProperty(this, "cameraNodalPoint", 0);
    private DoubleProperty cameraPaseCenter = new SimpleDoubleProperty(this, "cameraPhaseCenter", 3.72);

    // Export flightplan to ANP
    private final AsyncBooleanProperty enableSaveTempFileAscTec =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    // Dataset
    // Export dataset to 3dParty
    private final AsyncBooleanProperty exportUseAgisoftLevelArmOptimisation =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private StringProperty exportPhotoScanTesting = new SimpleStringProperty(this, "exportPhotoScanTesting", "");

    private final AsyncBooleanProperty exportBentleyAngles =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    // ExifTool
    private final AsyncBooleanProperty useExifLevelArmProcessing =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncBooleanProperty useExif90Deg =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final IntegerProperty exifMatchingYawNeg = new SimpleIntegerProperty(this, "exifMatchingYawNeg", 1);
    private final IntegerProperty exifMatchingPitchNeg = new SimpleIntegerProperty(this, "exifMatchingPitchNeg", 1);
    private final IntegerProperty exifMatchingRollNeg = new SimpleIntegerProperty(this, "exifMatchingRollNeg", 1);
    private final IntegerProperty exifMatchingYawAdd = new SimpleIntegerProperty(this, "exifMatchingYawAdd", 0);
    private final IntegerProperty exifMatchingPitchAdd = new SimpleIntegerProperty(this, "exifMatchingPitchAdd", 0);

    private final AsyncBooleanProperty useExifRollPitchYaw =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty useExifNavigator =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final IntegerProperty exifPreviewWidth = new SimpleIntegerProperty(this, "exifPreviewWidth", 160);

    public boolean getEnableAutoPLGdownload() {
        return enableAutoPLGdownload.get();
    }

    public boolean getAutoTakeoff() {
        return autoTakeoff.get();
    }

    public int getContourLinesMaxVisLines() {
        return contourLinesMaxVisLines.get();
    }

    public int getContourLinesStep() {
        return contourLinesStep.get();
    }

    public int getContourLinesStepBold() {
        return contourLinesStepBold.get();
    }

    public int getContourLinesStepBoldBlue() {
        return contourLinesStepBoldBlue.get();
    }

    public int getContourLinesWidth() {
        return contourLinesWidth.get();
    }

    public int getContourLinesWidthBold() {
        return contourLinesWidthBold.get();
    }

    public int getMaxTextureSize() {
        return maxTextureSize.get();
    }

    public boolean getUseExifLevelArmProcessing() {
        return useExifLevelArmProcessing.get();
    }

    public boolean getUseExif90Deg() {
        return useExif90Deg.get();
    }

    public int getExifMatchingYawNeg() {
        return exifMatchingYawNeg.get();
    }

    public int getExifMatchingPitchNeg() {
        return exifMatchingPitchNeg.get();
    }

    public int getExifMatchingYawAdd() {
        return exifMatchingYawAdd.get();
    }

    public int getExifMatchingRollNeg() {
        return exifMatchingRollNeg.get();
    }

    public int getExifMatchingPitchAdd() {
        return exifMatchingPitchAdd.get();
    }

    public boolean getUseExifRollPitchYaw() {
        return useExifRollPitchYaw.get();
    }

    public boolean getUseExifNavigator() {
        return useExifNavigator.get();
    }

    public int getExifPreviewWidth() {
        return exifPreviewWidth.get();
    }

    public int getAllowReconnectAfterMs() {
        return allowReconnectAfterMs.get();
    }

    public boolean getExportUseAgisoftLevelArmOptimisation() {
        return exportUseAgisoftLevelArmOptimisation.get();
    }

    public String getExportPhotoScanTesting() {
        return exportPhotoScanTesting.get();
    }

    public boolean getCameraRtkNewLevelArm() {
        return cameraRtkNewLevelArm.get();
    }

    public double getCameraNodalPoint() {
        return cameraNodalPoint.get();
    }

    public double getCameraPhaseCenter() {
        return cameraPaseCenter.get();
    }

    public boolean getEnableBluetooth() {
        return enableBluetooth.get();
    }

    public boolean getExportBentleyAngles() {
        return exportBentleyAngles.get();
    }

    public boolean getEnableSaveTempFileAscTec() {
        return enableSaveTempFileAscTec.get();
    }

    public boolean getComputePreviewSim() {
        return computePreviewSim.get();
    }

    public String getGeocoderProvider() {
        return geoCoderProvider.get();
    }

    public String getMapsFetchPoolSize(int mapPoolSizeDefaultValue) {
        mapsFetchPoolSize =
            new SimpleStringProperty(this, "mapsFetchPoolSize", String.valueOf(mapPoolSizeDefaultValue));
        return mapsFetchPoolSize.get();
    };

    public String getMapsFetchQueueSize(int mapFetchQueueSizeDefaultValue) {
        mapsFetchQueueSize =
            new SimpleStringProperty(this, "mapsFetchQueueSize", String.valueOf(mapFetchQueueSizeDefaultValue));
        return mapsFetchQueueSize.get();
    }

    public String getRetrieverPoolSize(int retrieverPoolSizeDefaultValue) {
        retrieverPoolSize =
            new SimpleStringProperty(this, "retrieverPoolSize", String.valueOf(retrieverPoolSizeDefaultValue));
        return retrieverPoolSize.get();
    }

    public String getRetrieverQueueSize(int retrieverQueueSizeDefaultValue) {
        retrieverQueueSize =
            new SimpleStringProperty(this, "retrieverPoolSize", String.valueOf(retrieverQueueSizeDefaultValue));
        return retrieverQueueSize.get();
    }

    public AsyncBooleanProperty getDisableSingleThreadingProperty() {
        return disableSingleThreading;
    }

}
