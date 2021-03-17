/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CFMLWriter;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FlightPlanService implements IFlightPlanService {

    private static Logger LOGGER = LoggerFactory.getLogger(FlightPlanService.class);

    private static final String FML = ".fml";
    private ListProperty<FlightPlanTemplate> templateList =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleDateFormat FLIGHT_PLAN_NAME_DATE_PART_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private final ILanguageHelper languageHelper;

    @Inject
    public FlightPlanService(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    // Seems no-one is calling getFirstTemplate - fix issue #8797 null
    // pointer dereference, by removing this code.
    //    public static final FlightPlanTemplate getFirstTemplate() {
    //        return new FlightPlanTemplate(null, null);
    //    }

    @Override
    public FlightPlan createFlightPlan(FlightPlanTemplate template, Mission mission) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveFlightPlan(Mission mission, FlightPlan flightPlan) {
        if (flightPlan == null) {
            return;
        }

        Flightplan legacyFlightPlan = flightPlan.getLegacyFlightplan();
        legacyFlightPlan.setName(flightPlan.getName());
        legacyFlightPlan.setFile(
            new File(
                MissionConstants.getFlightplanFolder(mission.getDirectoryFile()).getAbsolutePath(),
                buildFileName(flightPlan)));
        if (mission.save(false)) {
            try {
                legacyFlightPlan.saveToLocation(
                    new File(
                        MissionConstants.getFlightplanFolder(mission.getDirectoryFile()).getAbsolutePath(),
                        buildFileName(flightPlan)));
                DependencyInjector.getInstance()
                    .getInstanceOf(IApplicationContext.class)
                    .addToast(
                        Toast.of(ToastType.INFO)
                            .setText(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.planning.FlightplanView.saved", flightPlan.getName()))
                            .create());
            } catch (IllegalStateException e) {
                LOGGER.warn("cant save flight plan", e);
                DependencyInjector.getInstance()
                    .getInstanceOf(IApplicationContext.class)
                    .addToast(
                        Toast.of(ToastType.ALERT)
                            .setText(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.planning.FlightplanView.saveError", e.getMessage()))
                            .setShowIcon(true)
                            .create());
            }
        } else {
            DependencyInjector.getInstance()
                .getInstanceOf(IApplicationContext.class)
                .addToast(
                    Toast.of(ToastType.INFO)
                        .setText(
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.planning.FlightplanView.saveCanceled"))
                        .create());
        }
    }

    @Override
    public String cloneFlightPlanLocally(Flightplan flightPlan, String fpFullPath) {
        if (fpFullPath.endsWith(FML)) {
            fpFullPath = fpFullPath.substring(0, fpFullPath.length() - FML.length());
        }

        CFMLWriter writer = new CFMLWriter();
        // TODO when saveAs -> asks already for overwriting, not mentioned here, also saves: "name (2) (2).fml"
        try {
            String cloneFilePath = verifyForDuplicates(fpFullPath);
            writer.writeFlightplan(flightPlan, new File(cloneFilePath + FML));
            return cloneFilePath;
        } catch (IOException e) {
            LOGGER.warn("cant clone flight plan", e);
        }

        return null;
    }

    private String verifyForDuplicates(String filepath) throws IOException {
        File aFile = new File(filepath + FML);
        int fileNo = 1;
        String newFileName = "";
        if (aFile.exists() && !aFile.isDirectory()) {
            while (aFile.exists()) {
                fileNo++;
                newFileName = filepath + " (" + fileNo + ")";
                aFile = new File(newFileName + FML);
            }
        } else if (!aFile.exists()) {
            newFileName = filepath;
        }

        return newFileName;
    }

    @Override
    public String generateDefaultName(PlanType type) {
        return String.format(
            "%s_%s",
            languageHelper.getString("com.intel.missioncontrol.api.FlightPlanService." + type),
            FLIGHT_PLAN_NAME_DATE_PART_FORMAT.format(new Date()));
    }

    @Override
    public void renameFlightPlan(Mission mission, FlightPlan flightPlan, String flightPlanName) {
        File sourceFp = flightPlan.getLegacyFlightplan().getFile();
        File targetFp = new File(sourceFp.getParentFile(), flightPlanName + FML);
        if (sourceFp.exists()) {
            boolean result = sourceFp.renameTo(targetFp);
            if (!result) {
                throw new RuntimeException("Can't rename fp to " + flightPlanName);
            }
        }

        flightPlan.getLegacyFlightplan().setFile(targetFp);
        flightPlan.getLegacyFlightplan().setName(flightPlanName);
        flightPlan.rename(flightPlanName);
        flightPlan.save();
    }

    @Override
    public void remove(Mission mission, FlightPlan flightPlan) {
        mission.flightPlansProperty().remove(flightPlan);
        File fpFile = flightPlan.getLegacyFlightplan().getFile();
        if (fpFile.exists()) {
            if (!fpFile.delete()) {
                LOGGER.error("Unable to delete flight plan " + fpFile.getAbsolutePath());
            }
        }
    }

    @Override
    public void updateTemplateAoi(FlightPlan currentFlightplan, AreaOfInterest areaOfInterest) {
        areaOfInterest.updateDefaultsWithCurrentValues();
        File file = currentFlightplan.getLegacyFlightplan().getFile();
        Flightplan flightplan = new Flightplan();
        try {
            flightplan.open(file);
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog().severe("Cannot update a template AOI " + file + " : " + e.getCause());
            return;
        }

        flightplan.updatePicAreaTemplate(areaOfInterest.getType(), areaOfInterest.getPicAreaTemplate());
        flightplan.save(file);
    }

    private String buildFileName(FlightPlan flightPlan) {
        return flightPlan.getName() + FML;
    }
}
