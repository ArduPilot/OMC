/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionManager;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.core.helper.VectorNonEqual;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.logfile.ALogReader;
import eu.mavinci.plane.simjava.AirplaneSim;
import java.io.File;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MavinciObjectFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavinciObjectFactory.class);

    private final Predicate<Object> notNull = Predicates.notNull();

    @Inject
    private IHardwareConfigurationManager hardwareConfigurationManager;

    @Inject
    private IApplicationContext applicationContext;

    @Inject
    private ILanguageHelper languageHelper;

    public void initFlightPlan(Flightplan flightPlan, String fileName, File folder) {
        String file =
            folder.getAbsolutePath() + File.separator + fileName + "." + MissionManager.FLIGHT_PLAN_FILE_EXTENSION;
        initFlightPlan(flightPlan, new File(file));
    }

    public void initFlightPlan(Flightplan legacyFlightplan, File flightPlanFile) {
        legacyFlightplan.setFile(flightPlanFile);

        if (flightPlanFile.exists()) {
            try {
                legacyFlightplan.open(flightPlanFile);
            } catch (InvalidFlightPlanFileException e) {
                Debug.getLog()
                    .log(Level.WARNING, "Cannot read mission file " + flightPlanFile + " : " + e.getCause());
                applicationContext.addToast(
                    Toast.of(ToastType.ALERT).setText(languageHelper.getString("com.intel.missioncontrol.helper.cannotOpen") + flightPlanFile).setShowIcon(true).create());
            }
        }
    }

    public Flightplan flightPlanFromTemplate(File flightPlanTemplate) throws InvalidFlightPlanFileException {
        Flightplan flightplan = new Flightplan();
        flightplan.setFile(flightPlanTemplate);
        flightplan.open(flightPlanTemplate);

        return flightplan;
    }

    public PicArea createPicArea(Mission mission, PlanType planType) {
        Flightplan legacyFlightplan = mission.currentFlightPlanProperty().get().getLegacyFlightplan();
        CPicArea newPicArea = legacyFlightplan.addNewPicArea(planType);

        if ((newPicArea == null) || (!(newPicArea instanceof PicArea))) {
            throw new IllegalStateException("newPicArea is not of type PicArea");
        }

        PicArea picArea = (PicArea)newPicArea;
        picArea.setName(getNewPicAreaName(planType, legacyFlightplan));

        return picArea;
    }

    private String getNewPicAreaName(PlanType planType, Flightplan legacyFlightplan) {

        // goal: we want a name like Polygon $N, where $N is the lowest number not in use
        //
        // first, we get a list of all existing CPicAreas by means of the flightplanvisitor
        // then we propose a new name $TYPE + $N, where $N=1 and loop through the existing
        // picareas to see if the name is taken or not. We return the first that's available

        VectorNonEqual<IFlightplanRelatedObject> elements = new VectorNonEqual<>();
        final AFlightplanVisitor visitor =
            new AFlightplanVisitor() {
                @Override
                public boolean visit(IFlightplanRelatedObject element) {
                    if (element instanceof CPicArea) {
                        elements.add(element);
                    }

                    return false;
                }
            };

        legacyFlightplan.applyFpVisitor(visitor, false);

        boolean duplicated = false;
        int index = 1;

        // proposed name
        String planTypeName = languageHelper.getString(AreaOfInterest.AOI_PREFIX + planType.toString());
        String proposedName = planTypeName + " " + index;

        do {
            duplicated = false;
            for (IFlightplanRelatedObject element : elements) {
                if (((CPicArea)element).getName().equalsIgnoreCase(proposedName)) {
                    duplicated = true;
                    index += 1;
                    proposedName =
                        languageHelper.getString(AreaOfInterest.AOI_PREFIX + planType.toString()) + " " + index;
                    break;
                }
            }
        } while (duplicated);

        return proposedName;
    }

    public AirplaneSim createAirplaneSimulator(IPlatformDescription platformDescription, IAirplane airplane) {
        notNull.apply(platformDescription);
        notNull.apply(airplane);

        AirplaneSim airplaneSim = new AirplaneSim(airplane.getRootHandler(), true, airplane.getHardwareConfiguration());
        // to trigger airplane fireConnectionState when we will set connector
        airplane.disconnectSilently();
        airplane.setAirplaneConnector(airplaneSim);

        return airplaneSim;
    }

    public ALogReader createLogReader(IAirplane airplane, File logFile) {
        ALogReader logReader = ALogReader.logReaderFactory(airplane, logFile, hardwareConfigurationManager);

        return logReader;
    }

    public IPlatformDescription[] getAvailablePlatformDescriptions() {
        return hardwareConfigurationManager.getPlatforms();
    }

}
