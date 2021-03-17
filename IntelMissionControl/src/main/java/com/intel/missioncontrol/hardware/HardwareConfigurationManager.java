/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.inject.Inject;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.core.plane.AirplaneType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HardwareConfigurationManager implements IHardwareConfigurationManager {

    private static final String DEFAULT_CAMERA_ID = "Falcon-SonyRX1R2";
    private static final String DEFAULT_LENS_ID = "Falcon-SonyRX1R2-35mm";
    private static final String DEFAULT_PLATFORM = "Falcon 8+";

    private static final String DEFAULT_DJI_CAMERA_ID = "DJIPhantom4Pro-DefaultCamera";
    private static final String DEFAULT_DJI_LENS_ID = "DJIPhantom4Pro-DefaultLens";
    private static final String DEFAULT_DJI_PLATFORM = "DJIPhantom4Pro";

    private static final String DEFAULT_GRAYHAWK_CAMERA_ID = "GrayHawk-imx183";
    private static final String DEFAULT_GRAYHAWK_LENS_ID = "GrayHawk-imx183-16mm";
    private static final String DEFAULT_GRAYHAWK_PLATFORM = "GrayHawk";

    private final List<IPlatformDescription> platformDescriptions = new ArrayList<>();
    private final List<IGenericCameraDescription> cameraDescriptions = new ArrayList<>();
    private final List<ILensDescription> lensDescriptions = new ArrayList<>();

    private final ILicenceManager licenceManager;

    @Inject
    public HardwareConfigurationManager(IDescriptionProvider pathProvider, ILicenceManager licenceManager) {
        platformDescriptions.clear();
        platformDescriptions.addAll(pathProvider.getPlatformDescriptions());
        cameraDescriptions.clear();
        cameraDescriptions.addAll(pathProvider.getCameraDescriptions());
        lensDescriptions.clear();
        lensDescriptions.addAll(pathProvider.getLensDescriptions());
        this.licenceManager = licenceManager;
    }

    private <T, V> T findDescriptionInList(List<T> descriptions, Function<T, V> attributeExtractor, V attribute) {
        return descriptions
            .stream()
            .filter(description -> Objects.equals(attributeExtractor.apply(description), attribute))
            .findFirst()
            .orElseThrow(() -> new DescriptionNotFoundException("attribute:" + attribute));
    }

    @Override
    public IHardwareConfiguration getImmutableDefault() {
        Licence licence = licenceManager.getActiveLicence();
        IPlatformDescription platformDescription;
        ILensConfiguration lens;
        IGenericCameraConfiguration camera;
        if (licence.isGrayHawkEdition()) {
            platformDescription = getPlatformDescription(DEFAULT_GRAYHAWK_PLATFORM);
            lens = new LensConfiguration(getLensDescription(DEFAULT_GRAYHAWK_LENS_ID));
            camera = new GenericCameraConfiguration(getCameraDescription(DEFAULT_GRAYHAWK_CAMERA_ID), lens);
        } else if (licence.isFalconEdition()) {
            platformDescription = getPlatformDescription(DEFAULT_PLATFORM);
            lens = new LensConfiguration(getLensDescription(DEFAULT_LENS_ID));
            camera = new GenericCameraConfiguration(getCameraDescription(DEFAULT_CAMERA_ID), lens);
        } else {
            platformDescription = getPlatformDescription(DEFAULT_DJI_PLATFORM);
            lens = new LensConfiguration(getLensDescription(DEFAULT_DJI_LENS_ID));
            camera = new GenericCameraConfiguration(getCameraDescription(DEFAULT_DJI_CAMERA_ID), lens);
        }

        return HardwareConfiguration.createImmutable(platformDescription, camera);
    }

    @Override
    public ILensConfiguration getLensConfiguration(String lensId) {
        return new LensConfiguration(getLensDescription(lensId));
    }

    @Override
    public IGenericCameraConfiguration getCameraConfiguration(String cameraId, String lensId) {
        return new GenericCameraConfiguration(getCameraDescription(cameraId), getLensConfiguration(lensId));
    }

    @Override
    public IHardwareConfiguration getHardwareConfiguration(String platformId) {
        return createHardwareConfiguration(getPlatformDescription(platformId));
    }

    @Override
    public IHardwareConfiguration getHardwareConfiguration(AirplaneType type) {
        return createHardwareConfiguration(getPlatformDescription(type));
    }

    private IHardwareConfiguration createHardwareConfiguration(IPlatformDescription platformDescription) {
        IGenericCameraDescription cameraDescription = getFirstCompatibleCameraOrDefault(platformDescription);
        ILensDescription lensDescription = getFirstCompatibleLensOrDefault(cameraDescription);

        ILensConfiguration lens = new LensConfiguration(lensDescription);
        IGenericCameraConfiguration camera = new GenericCameraConfiguration(cameraDescription, lens);
        return new HardwareConfiguration(platformDescription, camera);
    }

    @Override
    public IGenericCameraDescription[] getCameras() {
        return cameraDescriptions.toArray(new IGenericCameraDescription[0]);
    }

    @Override
    public ILensDescription[] getLenses() {
        return lensDescriptions.toArray(new ILensDescription[0]);
    }

    @Override
    public IPlatformDescription[] getPlatforms() {
        return platformDescriptions.toArray(new IPlatformDescription[0]);
    }

    public IPlatformDescription getPlatformDescription(AirplaneType airplaneType) throws DescriptionNotFoundException {
        return findDescriptionInList(platformDescriptions, IPlatformDescription::getAirplaneType, airplaneType);
    }

    @Override
    public IPlatformDescription getPlatformDescription(String platformId) throws DescriptionNotFoundException {
        return findDescriptionInList(platformDescriptions, IPlatformDescription::getId, platformId);
    }

    @Override
    public IGenericCameraDescription getCameraDescription(String id) {
        return findDescriptionInList(cameraDescriptions, IGenericCameraDescription::getId, id);
    }

    @Override
    public ILensDescription getLensDescription(String id) {
        return findDescriptionInList(lensDescriptions, ILensDescription::getId, id);
    }

    public IPlatformDescription[] getCompatiblePlatforms(@Nullable IGenericCameraDescription camera) {
        if (camera == null) {
            return new IPlatformDescription[0];
        }

        return platformDescriptions
            .stream()
            .filter(platform -> platform.getCompatibleCameraIds().contains(camera.getId()))
            .toArray(IPlatformDescription[]::new);
    }

    @Override
    public IGenericCameraDescription[] getCompatibleCameras(@Nullable IPlatformDescription platform) {
        if (platform == null) {
            return new IGenericCameraDescription[0];
        }

        return cameraDescriptions
            .stream()
            .filter(camera -> platform.getCompatibleCameraIds().contains(camera.getId()))
            .toArray(IGenericCameraDescription[]::new);
    }

    public IGenericCameraDescription getFirstCompatibleCameraOrDefault(@Nullable IPlatformDescription platform) {
        if (platform == null) {
            return getCameraDescription(DEFAULT_CAMERA_ID);
        }

        final List<String> cameras = platform.getCompatibleCameraIds();
        if (cameras.isEmpty()) {
            return getCameraDescription(DEFAULT_CAMERA_ID);
        }

        return getCameraDescription(cameras.get(0));
    }

    public ILensDescription getFirstCompatibleLensOrDefault(@Nullable IGenericCameraDescription cameraDescription) {
        if (cameraDescription == null) {
            return getLensDescription(DEFAULT_LENS_ID);
        }

        final List<String> lenses = cameraDescription.getCompatibleLensIds();
        if (lenses.isEmpty()) {
            return getLensDescription(DEFAULT_LENS_ID);
        }

        return getLensDescription(lenses.get(0));
    }

    public boolean isCompatible(@Nullable IPlatformDescription platform, @Nullable IGenericCameraDescription camera) {
        if (platform == null || camera == null) {
            return false;
        }

        return platform.getCompatibleCameraIds().contains(camera.getId());
    }

    public boolean isCompatible(@Nullable IGenericCameraDescription camera, @Nullable ILensDescription lens) {
        if (camera == null || lens == null) {
            return false;
        }

        return camera.getCompatibleLensIds().contains(lens.getId());
    }

    public ILensDescription[] getCompatibleLenses(@Nullable IGenericCameraDescription camera) {
        if (camera == null) {
            return new ILensDescription[0];
        }

        return lensDescriptions
            .stream()
            .filter(lens -> camera.getCompatibleLensIds().contains(lens.getId()))
            .toArray(ILensDescription[]::new);
    }

}
