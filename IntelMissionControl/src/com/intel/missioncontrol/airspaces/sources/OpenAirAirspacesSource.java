/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources;

import com.intel.missioncontrol.airspaces.cache.airspace.AirspacesGeometryIndex;
import eu.mavinci.airspace.IAirspace;
import eu.mavinci.airspace.OpenAirspaceParser;
import gov.nasa.worldwind.geom.Sector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public class OpenAirAirspacesSource implements AirspaceSource {
    private static final Logger LOG = LoggerFactory.getLogger(OpenAirAirspacesSource.class);

    private final File airspacesFolder;
    private final AirspacesGeometryIndex geometryIndex;

    public OpenAirAirspacesSource(File airspacesFolder, AirspacesGeometryIndex geometryIndex) {
        this.airspacesFolder = airspacesFolder;
        this.geometryIndex = geometryIndex;

        getAirspacesFilesStream().flatMap(this::toStreamOfAirspaces).forEach(geometryIndex::add);
    }

    @Override
    public List<IAirspace> getAirspacesWithin(Sector boundingBox) {
        return getCachedAirspacesWithin(boundingBox);
    }

    @Override
    public List<IAirspace> getCachedAirspacesWithin(Sector boundingBox) {
        return geometryIndex.search(boundingBox);
    }

    private Stream<File> getAirspacesFilesStream() {
        File[] files = airspacesFolder.listFiles(File::isFile);
        return files != null ? Stream.of(files) : Stream.empty();
    }

    private Stream<IAirspace> toStreamOfAirspaces(File airspaceFile) {
        try (InputStream is = new FileInputStream(airspaceFile)) {
            LOG.debug("OpenAir airspaces bundle detected {}", airspaceFile.getName());
            return parseBundle(is).stream();
        } catch (IOException e) {
            LOG.error("Error occurred during parsing airspace bundle: " + airspaceFile.getAbsolutePath(), e);
            return Stream.empty();
        }
    }

    private List<IAirspace> parseBundle(InputStream is) throws IOException {
        return new OpenAirspaceParser(is).getAirspaces();
    }
}
