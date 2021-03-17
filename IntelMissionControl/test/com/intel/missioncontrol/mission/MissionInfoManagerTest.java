/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.intel.missioncontrol.TestPathProvider;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.hardware.HardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.test.categories.UITest;
import com.intel.missioncontrol.test.utils.MockingHelper;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import de.saxsys.mvvmfx.testingutils.jfxrunner.JfxRunner;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JfxRunner.class)
@Ignore
public class MissionInfoManagerTest {
    public static final String DEMO_SESSION_NAME = "DEMO";

    ISettingsManager settingsManager;
    ISrsManager srsManager;

    @Before
    public void setUp() throws Exception {
        IPathProvider pathProvider = new TestPathProvider();

        var modules = MockingHelper.createTestModules();

        Injector injector = Guice.createInjector(modules);
        MvvmFX.setCustomDependencyInjector(key -> injector.getInstance(key));
        // DependencyInjector.getInstance().
        settingsManager = DependencyInjector.getInstance().getInstanceOf(ISettingsManager.class);
        srsManager = DependencyInjector.getInstance().getInstanceOf(ISrsManager.class);
        Path folder = settingsManager.getSection(PathSettings.class).getProjectFolder();
        //        mi = new MissionInfo(folder.toAbsolutePath());
        //        mavinciObjectFactory = DependencyInjector.getInstance().getInstanceOf(MavinciObjectFactory.class);
        //    }
    }

    @Test
    @UITest
    public void readFromFile() {
        var languageHelper = DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);
        var hardwareConfigurationManager =
            DependencyInjector.getInstance().getInstanceOf(HardwareConfigurationManager.class);
        var mavinciObjectFactory = DependencyInjector.getInstance().getInstanceOf(MavinciObjectFactory.class);

        MissionInfoManager mi = new MissionInfoManager(settingsManager, srsManager);
        Path projectsDirectory = settingsManager.getSection(PathSettings.class).getProjectFolder();
        Path missionFolder = projectsDirectory.resolve(DEMO_SESSION_NAME);
        boolean alreadyCreated = Files.exists(missionFolder);
        var srsManager = DependencyInjector.getInstance().getInstanceOf(ISrsManager.class);

        if (alreadyCreated) {
            try {
                FileUtils.deleteDirectory(missionFolder.toFile());
            } catch (IOException e) {
                Debug.getLog().log(Level.SEVERE, "Could not delete Demo Session data on disk", e);
            }
        }

        try {
            FileHelper.scanFilesJarAndWriteToDisk(
                MFileFilter.allFilterNonSVN, "com/intel/missioncontrol/demoSessions/falcon/", missionFolder.toFile());
            //            Mission mission = missionManager.openMission(missionFolder);
            //            openMission(mission, false);
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "Could not store Demo Session data on disk", e);
        }

        // loadedMissionManager.loadMissionAsync(mission);
        IMissionInfo missionInfo = null;

        try {
            missionInfo = mi.readFromFile(missionFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(missionInfo.getLoadedFlightPlans());

        Mission mission =
            new Mission(
                missionInfo,
                mavinciObjectFactory,
                settingsManager,
                mi,
                languageHelper,
                hardwareConfigurationManager,
                srsManager);

        System.out.println(mission.getFirstFlightPlan().nameProperty());
    }

}
