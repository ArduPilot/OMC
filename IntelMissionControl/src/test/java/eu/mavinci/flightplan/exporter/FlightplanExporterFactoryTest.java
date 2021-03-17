/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import static org.junit.Assert.assertTrue;

import com.intel.missioncontrol.test.rules.ResourceBundleInitializer;
import eu.mavinci.desktop.gui.doublepanel.planemain.FlightplanExportTypes;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;
import eu.mavinci.helper.MavinciEnvInitializer;
import eu.mavinci.test.rules.GuiceInitializer;
import eu.mavinci.test.rules.MavinciInitializer;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@Ignore("'java.lang.NullPointerException' no 'flightplans/example.fml' file")
@RunWith(JUnit4.class)
public class FlightplanExporterFactoryTest {
    @ClassRule
    public static final MavinciInitializer MAVINCI_INITIALIZER = new MavinciInitializer();

    @ClassRule
    public static GuiceInitializer guiceInitializer = new GuiceInitializer();

    @Rule
    public ResourceBundleInitializer resourceBundleInitializer = new ResourceBundleInitializer();

    private File baseFolder;

    @Before
    public void setUp() throws Exception {
        baseFolder = MavinciEnvInitializer.prepareNewAppBase();
    }

    @Test
    public void testCsvExporter() throws Exception {
        testExporter("export-flightplan.csv", FlightplanExportTypes.CSV);
    }

    @Test
    @Ignore("Must be fixed by INMAV-1700")
    public void testKmlExporter() throws Exception {
        testExporter("export-flightplan.kml", FlightplanExportTypes.KML);
    }

    @Test
    public void testFplExporter() throws Exception {
        testExporter("export-flightplan.fpl", FlightplanExportTypes.FPL);
    }

    @Test
    public void testGpxExporter() throws Exception {
        testExporter("export-flightplan.gpx", FlightplanExportTypes.GPX);
    }

    @Test
    public void testRteExporter() throws Exception {
        testExporter("export-flightplan.rte", FlightplanExportTypes.RTE);
    }

    public File newFile(String fileName) throws IOException {
        File file = new File(baseFolder, fileName);
        if (!file.createNewFile()) {
            throw new IOException("a file with the name \'" + fileName + "\' already exists in the test folder");
        } else {
            return file;
        }
    }

    private void testExporter(String targetFilename, FlightplanExportTypes type) throws Exception {
        Flightplan flightplan = createFlightplan();
        File targetFile = new File(baseFolder, targetFilename);
        IFlightplanExporterFactory flightplanExporter = new FlightplanExporterFactory(null, null, null, null, null);
        IFlightplanExporter exporter = flightplanExporter.createExporter(type);
        exporter.export(flightplan, targetFile, null);
        assertTrue("A file must not be empty!", targetFile.length() != 0);
    }

    private Flightplan createFlightplan() {
        Flightplan flightplan = new Flightplan();
        ClassLoader classLoader = getClass().getClassLoader();
        File flightplanFile = new File(classLoader.getResource("flightplans/example.fml").getFile());
        try {
            flightplan.open(flightplanFile);
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog()
                .severe("Cannot load an example mission from a file " + flightplanFile + " : " + e.getCause());
        }

        return flightplan;
    }
}
