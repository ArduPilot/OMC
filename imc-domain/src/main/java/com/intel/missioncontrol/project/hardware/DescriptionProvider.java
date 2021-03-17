/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.serialization.CompositeSerializable;
import com.intel.missioncontrol.serialization.JsonSerializer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptionProvider implements IDescriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionProvider.class);
    private static final String DESCRIPTION_FILE_EXT = ".json";
    public static final String PLATFORMS = "platforms";
    public static final String CAMERAS = "cameras";
    public static final String LENSES = "lenses";

    private final List<PlatformDescription> platformDescriptions = new ArrayList<>();
    private final List<GenericCameraDescription> camerasDescriptions = new ArrayList<>();
    private final List<LensDescription> lensDescriptions = new ArrayList<>();

    private final JsonSerializer serializer = new JsonSerializer();

    public DescriptionProvider(Path rootPath) {
        // the dependency to file extractor makes sure that files got extracted before

        platformDescriptions.clear();
        camerasDescriptions.clear();
        lensDescriptions.clear();

        fillDescriptions(rootPath.resolve(PLATFORMS), platformDescriptions, PlatformDescription.class);
        fillDescriptions(rootPath.resolve(CAMERAS), camerasDescriptions, GenericCameraDescription.class);
        fillDescriptions(rootPath.resolve(LENSES), lensDescriptions, LensDescription.class);
    }

    private <T extends CompositeSerializable> void fillDescriptions(
            @UnderInitialization DescriptionProvider this, final Path dir, List<T> descriptions, Class<T> cls) {
        Expect.notNull(dir, "dir");
        Expect.notNull(descriptions, "descriptions");
        Expect.isTrue(Files.isDirectory(dir), "dir", String.format("%s is not a directory", dir));

        List<Path> files;
        try (var stream = Files.walk(dir, 1)) {
            files =
                stream.skip(1)
                    .filter(f -> Files.isRegularFile(f) && f.toString().toLowerCase().endsWith(DESCRIPTION_FILE_EXT))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            files = new ArrayList<>();
        }

        for (Path file : files) {
            try (InputStream stream = new FileInputStream(file.toFile())) {
                final T description = serializer.deserialize(stream, cls);
                descriptions.add(description);
            } catch (Exception e) {
                LOGGER.error(e.getMessage() + " [" + file + "]");
            }
        }
    }

    @Override
    public List<PlatformDescription> getPlatformDescriptions() {
        return new ArrayList<>(platformDescriptions);
    }

    @Override
    public List<IGenericCameraDescription> getCameraDescriptions() {
        return new ArrayList<>(camerasDescriptions);
    }

    @Override
    public List<LensDescription> getLensDescriptions() {
        return new ArrayList<>(lensDescriptions);
    }

    @Override
    public PlatformDescription getPlatformDescriptionById(String id) {
        if (id == null) {
            return null;
        }

        Optional<PlatformDescription> descriptionOptional =
            platformDescriptions
                .stream()
                .filter(platformDescription -> platformDescription.getId().equals(id))
                .findFirst();
        if (descriptionOptional.isEmpty()) {
            return null;
        }

        return descriptionOptional.get();
    }

    @Override
    public IGenericCameraDescription getGenericCameraDescriptionById(String id) {
        IPayloadDescription res = getPayloadDescriptionById(id);
        if (res instanceof IGenericCameraDescription) {
            return (IGenericCameraDescription)res;
        } else {
            throw new IllegalArgumentException("id: not an IGenericCameraDescription");
        }
    }

    @Override
    public IPayloadDescription getPayloadDescriptionById(String id) {
        if (id == null) {
            return null;
        }

        Optional<GenericCameraDescription> descriptionOptional =
            camerasDescriptions
                .stream()
                .filter(genericCameraDescription -> genericCameraDescription.getId().equals(id))
                .findFirst();
        if (descriptionOptional.isEmpty()) {
            return null;
        }

        return descriptionOptional.get();
    }

    @Override
    public LensDescription getLensDescriptionById(String id) {
        if (id == null) {
            return null;
        }

        Optional<LensDescription> descriptionOptional =
            lensDescriptions.stream().filter(lensDescription -> lensDescription.getId().equals(id)).findFirst();
        if (descriptionOptional.isEmpty()) {
            return null;
        }

        return descriptionOptional.get();
    }
}
