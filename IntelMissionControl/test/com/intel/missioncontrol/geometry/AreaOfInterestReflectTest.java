/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import de.saxsys.mvvmfx.testingutils.jfxrunner.JfxRunner;
import de.saxsys.mvvmfx.testingutils.jfxrunner.TestInJfxThread;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.helper.MavinciEnvInitializer;
import eu.mavinci.test.rules.GuiceInitializer;
import eu.mavinci.test.rules.MavinciInitializer;
import gov.nasa.worldwind.geom.Position;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testfx.api.FxToolkit;

@Ignore(
    "1) No implementation for com.intel.missioncontrol.helper.MBackgroundTaskManager was bound.\n"
        + "  while locating com.intel.missioncontrol.helper.MBackgroundTaskManager")
@RunWith(JfxRunner.class)
public class AreaOfInterestReflectTest {

    @BeforeClass
    public static void globalSetup() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @ClassRule
    public static GuiceInitializer guiceInitializer = new GuiceInitializer();

    @Rule @ClassRule
    public static MavinciInitializer mavinciInitializer = new MavinciInitializer();

    @Test
    @TestInJfxThread
    public void testReflectionChangeGsd()
            throws FlightplanContainerWrongAddingException, FlightplanContainerFullException {
        MavinciEnvInitializer mavinciEnvInitializer = mavinciInitializer.getMavinciEnvInitializer();

        Flightplan flightplan = mavinciEnvInitializer.createFlightplan("");

        double gsd = 0.4;
        List<Position> positionList =
            Arrays.asList(Position.fromDegrees(123, 345), Position.fromDegrees(12, 34), Position.fromDegrees(120, 340));

        PicArea picArea = mavinciEnvInitializer.createPicArea(flightplan, gsd, positionList, PlanType.TOWER);
        AreaOfInterest areaOfInterest = new AreaOfInterest(picArea, picArea);

        // Test Aoi -> picArea
        gsd = 0.05;
        areaOfInterest.gsdProperty().setValue(gsd);

        Assert.assertEquals(gsd, picArea.getGsd(), 0);

        // Test picArea -> Aoi
        gsd = 0.02;
        picArea.setGsd(gsd);
        Assert.assertEquals(gsd, areaOfInterest.gsdProperty().getValue().doubleValue(), 0);
    }

    @Test
    @TestInJfxThread
    public void testReflectionChangeAlt()
            throws FlightplanContainerWrongAddingException, FlightplanContainerFullException {
        MavinciEnvInitializer mavinciEnvInitializer = mavinciInitializer.getMavinciEnvInitializer();

        mavinciEnvInitializer.initSession();

        Flightplan flightplan = mavinciEnvInitializer.createFlightplan("");

        double gsd = 0.3;
        List<Position> positionList =
            Arrays.asList(Position.fromDegrees(13, 45), Position.fromDegrees(112, 134), Position.fromDegrees(12, 34));

        PicArea picArea = mavinciEnvInitializer.createPicArea(flightplan, gsd, positionList, PlanType.POLYGON);
        AreaOfInterest areaOfInterest = new AreaOfInterest(picArea, picArea);

        flightplan.addToFlightplanContainer(picArea);

        // Test Aoi -> picArea
        double alt = 120;
        areaOfInterest.altProperty().setValue(alt);

        Assert.assertEquals(alt, picArea.getAlt(), 0);

        // Test picArea -> Aoi
        alt = 32;
        picArea.setAlt(alt);
        Assert.assertEquals(alt, areaOfInterest.altProperty().getValue().doubleValue(), 0);
    }
}
