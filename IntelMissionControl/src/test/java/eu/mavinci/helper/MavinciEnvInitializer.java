/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper;

import com.intel.missioncontrol.TestPathProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.FlightplanFactory;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.FlightplanFactoryBase;
import eu.mavinci.flightplan.FlightplanManager;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.Point;
import eu.mavinci.plane.Airplane;
import gov.nasa.worldwind.geom.Position;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class MavinciEnvInitializer {
    private static TestPathProvider pathProvider;
    private Airplane airplane;

    public static File prepareNewAppBase() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        pathProvider = new TestPathProvider(baseDir.toPath());
        File settingsFolder = pathProvider.getSettingsDirectory().toFile();
        if (settingsFolder.exists()) {
            settingsFolder.delete();
        }

        settingsFolder.mkdir();
        pathProvider = new TestPathProvider(baseDir.toPath());
        return settingsFolder;
    }

    public void init() {
        FlightplanFactory.setFactory(new FlightplanFactoryBase());

        prepareNewAppBase();

        DependencyInjector.getInstance()
            .getInstanceOf(GeneralSettings.class)
            .getDefaultCameraFilenameProperty()
            .setValue("LumixGF1-Pancake.camera");

        // camera = CameraHelper.generateNewDefaultCam();
        // camera.setAirplaneType(AirplaneType.SIRIUS_PRO);

        // Application.cameras.add(camera);
        // Application.fireCameraListChanged();

        Application.preInit(pathProvider);
    }

    public void initSession() {
        airplane = new Airplane(null, null);
    }

    public PicArea createPicArea(Flightplan flightplan, double gsd, List<Position> positionList, PlanType planType)
            throws FlightplanContainerWrongAddingException, FlightplanContainerFullException {
        PicArea picArea = (PicArea)flightplan.addNewPicArea(planType);

        // Set GSD, setAlt is also inside
        picArea.setGsd(gsd);
        // AltitudeCalculator calc = new AltitudeCalculator(flightplan.getCameraDescription(),
        // flightplan.getPlatformDescription());
        // picArea.setAlt(calc.getAlt());

        // Corners
        for (Position pos : positionList) {
            picArea.getCorners()
                .addToFlightplanContainer(new Point(pos.getLatitude().getDegrees(), pos.getLongitude().getDegrees()));
        }

        return picArea;
    }

    public Flightplan createFlightplan(String fileName) {
        Flightplan flightplan = new Flightplan();
        flightplan.setFile(new File(fileName));
        // try {
        MavinciEnvInitializer.prepareNewAppBase();
        // DescriptionManager descriptionManager = new DescriptionManager(new TestPathProvider(null));
        //// descriptionManager.loadFromDisk();
        // flightplan.setPlatformDescription(descriptionManager.getPlatforms().get(0));
        // } catch (IOException e) {
        //    LOGGER.warn("Could not assign descriptions to flightplan.", e);
        // }
        getFlightplanManager().add(flightplan);

        return flightplan;
    }

    public Flightplan loadFlightplan(String fileName) {
        Flightplan flightplan = new Flightplan();
        try {
            flightplan.open(new File(fileName));
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog().severe("Cannot load a flight plan from a file " + fileName + " : " + e.getCause());
        }

        return flightplan;
    }

    public FlightplanManager getFlightplanManager() {
        return airplane.getFPmanager();
    }

    public Airplane getAirplane() {
        return airplane;
    }

    public static String getBaseDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        return Paths.get(baseDir.getPath(), pathProvider.getSettingsDirectory().getFileName().toString()).toString();
    }

    public static File getSettingsFile() {
        return pathProvider.getSettingsFile().toFile();
    }
}
