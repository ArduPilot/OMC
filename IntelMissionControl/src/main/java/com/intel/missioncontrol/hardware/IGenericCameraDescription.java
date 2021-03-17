/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import eu.mavinci.core.flightplan.camera.FilterTypes;
import java.util.List;

/** IGenericCameraDescription is a type of payload. Represents a generic set of camera parameters */
public interface IGenericCameraDescription extends IPayloadDescription {

    String NAME_PROPERTY = "name";
    String CCD_HEIGHT_PROPERTY = "ccdHeight";
    String CCD_WIDTH_PROPERTY = "ccdWidth";
    String CCD_RESX_PROPERTY = "ccdResX";
    String CCD_RESY_PROPERTY = "ccdResY";
    String CCD_X_TRANSL_PROPERTY = "ccdXTransl";
    String CCD_Y_TRANSL_PROPERTY = "ccdYTransl";
    String CAMERA_DELAY_PROPERTY = "cameraDelay";
    String ENSO_CALIB_FILE_PROPERTY = "ensoCalibFile";
    String ICAROS_CALIB_FILE_PROPERTY = "icarosCalibFile";
    String MENCI_CALIB_FILE_PROPERTY = "menciCalibFile";
    String PIX4D_CALIB_FILE_PROPERTY = "pix4dCalibFile";
    String AGISOFT_CALIB_FILE_PROPERTY = "agisoftCalibFile";
    String FILTER_TYPE_PROPERTY = "filterType";
    String SD_CAPACITY_PROPERTY = "sdCapacityInGB";
    String EXIF_MODEL_PROPERTY = "exifModel";
    String IS_PROJECTABLE_TO_GROUND_PROPERTY = "isProjectableToGround";
    String ONE_OVER_EXPOSURE_TIME_PROPERTY = "oneOverExposureTime";
    String IS_EXPOSURE_TIME_FIXED = "isExposureTimeFixed";
    String PICTURE_SIZE_PROPERTY = "pictureSizeInMB";
    String BAND_NAMES_SPLIT_PROPERTY = "bandNamesSplit";
    String IS_PROVIDING_FEEDBACK_PROPERTY = "isProvidingFeedback";
    String VIDEO_STREAM_URI_PROPERTY = "videoStreamUri";
    String COMPATIBLE_LENS_IDS_PROPERTY = "compatibleLensIds";

    Quantity<Length> getCcdHeight();

    int getCcdResX();

    int getCcdResY();

    Quantity<Length> getCcdWidth();

    Quantity<Length> getCcdXTransl();

    Quantity<Length> getCcdYTransl();

    Quantity<Time> getCameraDelay();

    String getEnsoCalibFile();

    String getIcarosCalibFile();

    String getMenciCalibFile();

    String getPix4dCalibFile();

    String getAgisoftCalibFile();

    FilterTypes getFilterType();

    double getSdCapacityInGB();

    List<String> getExifModels();

    boolean isProjectableToGround();

    Quantity<Time> getOneOverExposureTime();

    boolean isExposureTimeFixed();

    double getPictureSizeInMB();

    String[] getBandNamesSplit();

    String getName();

    boolean isProvidingFeedback();

    /**
     * Returns the URI of this camera's video stream if defined by the camera description, or null otherwise
     * (e.g. no video is available or URI is determined by other means).
     */
    String getVideoStreamUri();

    List<String> getCompatibleLensIds();

    default IMutableGenericCameraDescription asMutable() {
        return (IMutableGenericCameraDescription)this;
    }

}
