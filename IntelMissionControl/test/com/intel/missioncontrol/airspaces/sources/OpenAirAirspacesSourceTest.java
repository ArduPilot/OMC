/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources;

import com.intel.missioncontrol.airspaces.cache.airspace.AirspacesGeometryIndex;

import gov.nasa.worldwind.geom.Sector;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class OpenAirAirspacesSourceTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void sourceContains_airspacesFromFilesListedInGivenFolder() throws Exception {
        File directory = temporaryFolder.newFolder();
        copyAirspacesFileToFolder("twoAirspaces.txt", directory);
        copyAirspacesFileToFolder("threeAirspaces.txt", directory);

        OpenAirAirspacesSource sources = new OpenAirAirspacesSource(directory, new AirspacesGeometryIndex());

        Sector bb = Sector.fromDegrees(-90, 90, -180, 179.9);
        assertThat(sources.getAirspacesWithin(bb).size(), is(5));
    }

    private void copyAirspacesFileToFolder(String fileName, File directory) throws IOException {
        URL systemResource = ClassLoader.getSystemResource("com/intel/missioncontrol/airspaces/sources/bundled/" + fileName);
        Files.copy(systemResource.openStream(), Paths.get(new File(directory, fileName).toURI()), StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void parseBiggerFiles_germany() throws Exception {
        File directory = temporaryFolder.newFolder();
        copyAirspacesFileToFolder("Germany_CW29_2017.txt", directory);

        OpenAirAirspacesSource sources = new OpenAirAirspacesSource(directory, new AirspacesGeometryIndex());

        Sector bb = Sector.fromDegrees(-90, 90, -180, 179.9);
        assertThat(sources.getAirspacesWithin(bb).size(), is(560));
    }

    @Test
    public void parseBiggerFiles_swiss() throws Exception {
        File directory = temporaryFolder.newFolder();
        copyAirspacesFileToFolder("Switzerland_170507_CH.txt", directory);

        OpenAirAirspacesSource sources = new OpenAirAirspacesSource(directory, new AirspacesGeometryIndex());

        Sector bb = Sector.fromDegrees(-90, 90, -180, 179.9);
        assertThat(sources.getAirspacesWithin(bb).size(), is(236));
    }

    @Test
    public void parseBiggerFiles_ukraine() throws Exception {
        File directory = temporaryFolder.newFolder();
        copyAirspacesFileToFolder("Ukraine_2017.txt", directory);

        OpenAirAirspacesSource sources = new OpenAirAirspacesSource(directory, new AirspacesGeometryIndex());

        Sector bb = Sector.fromDegrees(-90, 90, -180, 179.9);
        assertThat(sources.getAirspacesWithin(bb).size(), is(387));
    }
}
