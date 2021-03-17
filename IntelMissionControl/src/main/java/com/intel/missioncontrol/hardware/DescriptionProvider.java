/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.intel.missioncontrol.IFileExtractor;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.Expect;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptionProvider implements IDescriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionProvider.class);
    private static final String DESCRIPTION_FILE_EXT = ".json";

    private final List<PlatformDescription> platformDescriptions = new ArrayList<>();
    private final List<GenericCameraDescription> camerasDescriptions = new ArrayList<>();
    private final List<LensDescription> lensDescriptions = new ArrayList<>();

    private static final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(PayloadMountDescription.class, new PayloadMountDescription.Deserializer())
            .registerTypeAdapter(IConnectionProperties.class, new ConnectionProperties.Deserializer())
            .registerTypeAdapter(IMavlinkConnectionProperties.class, new MavlinkConnectionProperties.Deserializer())
            .registerTypeAdapter(IMavlinkFlightPlanOptions.class, new MavlinkFlightPlanOptions.Deserializer())
            .registerTypeAdapter(PlatformDescription.class, new PlatformDescription.Deserializer())
            .registerTypeAdapter(GenericCameraDescription.class, new GenericCameraDescription.Deserializer())
            .registerTypeAdapter(MavlinkParam.class, new MavlinkParam.Deserializer())
            .registerTypeAdapter(LensDescription.class, new LensDescription.Deserializer())
            .setExclusionStrategies()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    @Inject
    public DescriptionProvider(IPathProvider pathProvider, IFileExtractor fileExtractor) {
        // the dependency to file extractor makes sure that files got extracted before (as long as its not mocked by
        // dependency injector with a proxy class

        platformDescriptions.clear();
        camerasDescriptions.clear();
        lensDescriptions.clear();

        fillDescriptions(
            pathProvider.getPlatformDescriptionsDirectory(), platformDescriptions, PlatformDescription.class);
        fillDescriptions(
            pathProvider.getCameraDescriptionsDirectory(), camerasDescriptions, GenericCameraDescription.class);
        fillDescriptions(pathProvider.getLensDescriptionsDirectory(), lensDescriptions, LensDescription.class);
    }

    private <T> void fillDescriptions(
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
            try (InputStream stream = new FileInputStream(file.toFile());
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                final T description = gson.fromJson(reader, cls);
                descriptions.add(description);
            } catch (Exception e) {
                LOGGER.error(e.getMessage() + " [" + file + "]");
            }
        }
    }

    @Override
    public List<IPlatformDescription> getPlatformDescriptions() {
        return new ArrayList<>(platformDescriptions);
    }

    @Override
    public List<IGenericCameraDescription> getCameraDescriptions() {
        return new ArrayList<>(camerasDescriptions);
    }

    @Override
    public List<ILensDescription> getLensDescriptions() {
        return new ArrayList<>(lensDescriptions);
    }
}
