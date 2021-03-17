/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.intel.missioncontrol.airspaces.cache.airspace.AirspacesGeometryIndex;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenAirAirspacesSourceTest {

    @Test
    void sourceContains_airspacesFromFilesListedInGivenFolder(@TempDir File directory) throws Exception {
        copyAirspacesFileToFolder("twoAirspaces.txt", directory);
        copyAirspacesFileToFolder("threeAirspaces.txt", directory);

        OpenAirAirspacesSource sources = new OpenAirAirspacesSource(directory, new AirspacesGeometryIndex());

        Sector bb = Sector.fromDegrees(-90, 90, -180, 179.9);
        assertThat(sources.getAirspacesWithin(bb).size(), is(5));
    }

    private void copyAirspacesFileToFolder(String fileName, File directory) throws IOException {
        URL systemResource =
            ClassLoader.getSystemResource("com/intel/missioncontrol/airspaces/sources/bundled/" + fileName);
        Files.copy(
            systemResource.openStream(),
            Paths.get(new File(directory, fileName).toURI()),
            StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void parseBiggerFiles_germany(@TempDir File directory) throws Exception {
        copyAirspacesFileToFolder("Germany_CW29_2017.txt", directory);

        OpenAirAirspacesSource sources = new OpenAirAirspacesSource(directory, new AirspacesGeometryIndex());

        Sector bb = Sector.fromDegrees(-90, 90, -180, 179.9);
        assertThat(sources.getAirspacesWithin(bb).size(), is(560));
    }

    @Test
    void parseBiggerFiles_swiss(@TempDir File directory) throws Exception {
        copyAirspacesFileToFolder("Switzerland_170507_CH.txt", directory);

        OpenAirAirspacesSource sources = new OpenAirAirspacesSource(directory, new AirspacesGeometryIndex());

        Sector bb = Sector.fromDegrees(-90, 90, -180, 179.9);
        assertThat(sources.getAirspacesWithin(bb).size(), is(236));
    }

    @Test
    void parseBiggerFiles_ukraine(@TempDir File directory) throws Exception {
        copyAirspacesFileToFolder("Ukraine_2017.txt", directory);

        OpenAirAirspacesSource sources = new OpenAirAirspacesSource(directory, new AirspacesGeometryIndex());

        Sector bb = Sector.fromDegrees(-90, 90, -180, 179.9);
        assertThat(sources.getAirspacesWithin(bb).size(), is(387));
    }

}
