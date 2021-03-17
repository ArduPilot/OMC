/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import eu.mavinci.core.plane.AirplaneType;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IHardwareConfigurationManager {

    IHardwareConfiguration getImmutableDefault();

    ILensConfiguration getLensConfiguration(String lensId);

    IGenericCameraConfiguration getCameraConfiguration(String cameraId, String lensId);

    IHardwareConfiguration getHardwareConfiguration(String platformId);

    IHardwareConfiguration getHardwareConfiguration(AirplaneType type);

    IGenericCameraDescription[] getCameras();

    ILensDescription[] getLenses();

    IPlatformDescription[] getPlatforms();

    IPlatformDescription getPlatformDescription(String platformId);

    IGenericCameraDescription getCameraDescription(String id);

    ILensDescription getLensDescription(String id);

    IGenericCameraDescription[] getCompatibleCameras(@Nullable IPlatformDescription newValue);

    ILensDescription[] getCompatibleLenses(@Nullable IGenericCameraDescription cameraDescription);

    IPlatformDescription[] getCompatiblePlatforms(@Nullable IGenericCameraDescription camera);

    boolean isCompatible(
            @Nullable IPlatformDescription platformDescription, @Nullable IGenericCameraDescription cameraDescription);

    boolean isCompatible(
            @Nullable IGenericCameraDescription cameraDescription, @Nullable ILensDescription lensDescription);

    IGenericCameraDescription getFirstCompatibleCameraOrDefault(@Nullable IPlatformDescription platformDescription);

    ILensDescription getFirstCompatibleLensOrDefault(@Nullable IGenericCameraDescription cameraDescription);
}
