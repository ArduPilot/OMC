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

public interface IMutableGenericCameraDescription extends IGenericCameraDescription, IMutablePayloadDescription {

    void setId(String value);

    void setCcdHeight(Quantity<Length> value);

    void setCcdResX(int value);

    void setCcdResY(int value);

    void setCcdWidth(Quantity<Length> value);

    void setCcdXTransl(Quantity<Length> value);

    void setCcdYTransl(Quantity<Length> value);

    void setCameraDelay(Quantity<Time> value);

    void setEnsoCalibFile(String value);

    void setIcarosCalibFile(String value);

    void setMenciCalibFile(String value);

    void setPix4dCalibFile(String value);

    void setAgisoftCalibFile(String value);

    void setFilterType(FilterTypes value);

    void setSdCapacityInGB(double value);

    void setExifModels(List<String> value);

    void setIsProjectableToGround(boolean value);

    void setOneOverExposureTime(Quantity<Time> value);

    void setPictureSizeInMB(double value);

    void setBandNamesSplit(String[] value);

    void setName(String value);

    void setIsProvidingFeedback(boolean value);

    void setVideoStreamUri(String uri);

    void setCompatibleLensIds(List<String> value);

}
