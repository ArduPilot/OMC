/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.api.test;

import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.test.rules.ResourceBundleInitializer;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.FlightplanFactory;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.FlightplanManager;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.helper.MavinciEnvInitializer;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.test.rules.GuiceInitializer;
import eu.mavinci.test.rules.MavinciInitializer;
import gov.nasa.worldwind.geom.Position;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
@Ignore("Just for manual run")
public class FlightPlanXmlGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightPlanXmlGenerator.class);

    @ClassRule
    public static GuiceInitializer guiceInitializer = new GuiceInitializer();

    @Rule
    public ResourceBundleInitializer resourceBundleInitializer = new ResourceBundleInitializer();

    @Rule @ClassRule
    public static MavinciInitializer mavinciInitializer = new MavinciInitializer();

    @Test
    public void test() throws FlightplanContainerWrongAddingException, FlightplanContainerFullException {
        MavinciEnvInitializer mavinciEnvInitializer = mavinciInitializer.getMavinciEnvInitializer();

        String baseFolder = MavinciEnvInitializer.getBaseDir();
        String fileName = baseFolder + File.separator + "testFlightPlan.xml";

        mavinciEnvInitializer.initSession();

        Flightplan flightplan = mavinciEnvInitializer.createFlightplan(fileName);

        flightplan.getLandingpoint().setMode(LandingModes.DESC_CIRCLE);
        flightplan.getLandingpoint().setLatLon(123.1, 345.1);

        // Create two PicAreas
        double gsd1 = 123.789;
        List<Position> positionList1 =
            Arrays.asList(Position.fromDegrees(123, 345), Position.fromDegrees(12, 34), Position.fromDegrees(120, 340));
        double gsd2 = 100.99;
        List<Position> positionList2 =
            Arrays.asList(Position.fromDegrees(23, 45), Position.fromDegrees(10, 4), Position.fromDegrees(102, 304));

        PicArea picArea1 = mavinciEnvInitializer.createPicArea(flightplan, gsd1, positionList1, PlanType.TOWER);
        AreaOfInterest areaOfInterest1 = new AreaOfInterest(picArea1, picArea1);
        picArea1.computeFlightlinesWithLastPlaneSilent();
        picArea1.addToFlightplanContainer(FlightplanFactory.getFactory().newCWaypoint(3, 4, picArea1));
        picArea1.addToFlightplanContainer(FlightplanFactory.getFactory().newCPhoto(true, 12, 23, picArea1));

        PicArea picArea2 = mavinciEnvInitializer.createPicArea(flightplan, gsd2, positionList2, PlanType.TOWER);
        AreaOfInterest areaOfInterest2 = new AreaOfInterest(picArea2, picArea2);
        picArea2.computeFlightlinesWithLastPlaneSilent();
        picArea2.addToFlightplanContainer(FlightplanFactory.getFactory().newCWaypoint(1, 1, picArea2));
        picArea2.addToFlightplanContainer(FlightplanFactory.getFactory().newCPhoto(picArea2));

        // Add picAreas to the plan
        flightplan.addToFlightplanContainer(picArea1);
        flightplan.addToFlightplanContainer(picArea2);

        flightplan.setLearningmode(false);

        flightplan.getLengthInMeter();

        // Recalculate FlightPlan
        IAirplane airplane = mavinciEnvInitializer.getAirplane();
        FlightplanManager flightplanManager = airplane.getFPmanager();

        flightplanManager.resetupFlightplan(flightplan);

        flightplan.save(new File(baseFolder));

        LOGGER.debug("Saved XML: {}", flightplan.toXML());
    }

    @Test
    public void testLoad() {
        String fileName = "src/test/resources/flightplans/testFlightPlan.xml";

        MavinciEnvInitializer mavinciEnvInitializer = mavinciInitializer.getMavinciEnvInitializer();
        mavinciEnvInitializer.initSession();

        Flightplan flightplan = mavinciEnvInitializer.loadFlightplan(fileName);

        flightplan.sizeOfFlightplanContainer();

        LOGGER.debug("Loaded XML {}", flightplan.toXML());
    }
}
