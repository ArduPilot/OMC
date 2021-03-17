/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.intel.missioncontrol.ApplicationContext;
import com.intel.missioncontrol.FileExtractor;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.IFileExtractor;
import com.intel.missioncontrol.IInterProcessHandler;
import com.intel.missioncontrol.InterProcessHandler;
import com.intel.missioncontrol.LocalScope;
import com.intel.missioncontrol.TestPathProvider;
import com.intel.missioncontrol.api.ExportService;
import com.intel.missioncontrol.api.FlightPlanService;
import com.intel.missioncontrol.api.FlightPlanTemplateService;
import com.intel.missioncontrol.api.IExportService;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.api.IFlightPlanTemplateService;
import com.intel.missioncontrol.api.support.ISupportManager;
import com.intel.missioncontrol.api.support.SupportManager;
import com.intel.missioncontrol.api.workflow.AoiWorkflowHints;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.common.PathProvider;
import com.intel.missioncontrol.common.PostConstructionListener;
import com.intel.missioncontrol.hardware.DescriptionProvider;
import com.intel.missioncontrol.hardware.HardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IDescriptionProvider;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.helper.MBackgroundTaskManager;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.logging.ProvisioningLogger;
import com.intel.missioncontrol.logging.ReseatableFileAppender;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.SelectionManager;
import com.intel.missioncontrol.modules.AirplaneValidationModule;
import com.intel.missioncontrol.modules.AoiValidationModule;
import com.intel.missioncontrol.modules.FlightplanValidationModule;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.modules.MatchingValidationModule;
import com.intel.missioncontrol.modules.NetworkModule;
import com.intel.missioncontrol.modules.SettingsModule;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.test.categories.UITest;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.IProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.IVeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.MockDialogService;
import com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.VeryUglyDialogHelper;
import com.intel.missioncontrol.ui.navbar.connection.DetectedUavSource;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionAction;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionActionImpl;
import com.intel.missioncontrol.ui.navbar.connection.UavSource;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.notifications.INotificationDispatcher;
import com.intel.missioncontrol.ui.notifications.NotificationDispatcher;
import com.intel.missioncontrol.ui.sidepane.analysis.IMatchingService;
import com.intel.missioncontrol.ui.sidepane.analysis.MatchingService;
import com.intel.missioncontrol.ui.update.IUpdateManager;
import com.intel.missioncontrol.ui.update.UpdateManager;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ValidationService;
import com.intel.missioncontrol.utils.DefaultBackgroundTaskManager;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import com.intel.missioncontrol.utils.IVersionProvider;
import com.intel.missioncontrol.utils.VersionProvider;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.guice.internal.MvvmfxModule;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import de.saxsys.mvvmfx.testingutils.jfxrunner.JfxRunner;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.helper.GeoFenceDetector;
import eu.mavinci.core.helper.IGeoFenceDetector;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.LicenceManager;
import eu.mavinci.desktop.gui.wwext.IElevationModelUpdateHelper;
import eu.mavinci.desktop.gui.wwext.MapDataChangedHelper;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.SrsManager;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.profiling.ProfilingManager;
import eu.mavinci.flightplan.exporter.FlightplanExporterFactory;
import eu.mavinci.flightplan.exporter.IFlightplanExporterFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JfxRunner.class)
public class MissionTest {

    public static final String DEMO_SESSION_NAME = "DEMO";
    IMissionInfo mi;
    ISettingsManager settingsManager;
    MissionInfoManager missionInfoManager;
    IMissionManager missionManager;
    private MavinciObjectFactory mavinciObjectFactory;
    private IApplicationContext applicationContext;
    private INavigationService navigationService;

    public static boolean isDemo(IMissionInfo mission) {
        return Optional.ofNullable(mission)
            .map(IMissionInfo::getName)
            .filter(DEMO_SESSION_NAME::equalsIgnoreCase)
            .isPresent();
    }

    private Set<Module> createModules() {
        final Set<Module> modules = new HashSet<>();

        modules.add(new MockBaseModule());
        modules.add(new SettingsModule());
        modules.add(new NetworkModule());
        modules.add(new MapModule());
        modules.add(new FlightplanValidationModule());
        modules.add(new AirplaneValidationModule());
        modules.add(new AoiValidationModule());
        modules.add(new MatchingValidationModule());
        modules.add(
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(ILicenceManager.class).to(LicenceManager.class).in(Singleton.class);
                    bind(IMissionManager.class).to(MissionManager.class).in(Singleton.class);
                    bind(IHardwareConfigurationManager.class)
                        .to(HardwareConfigurationManager.class)
                        .in(Singleton.class);
                    bind(IValidationService.class).to(ValidationService.class).in(Singleton.class);
                    bind(IFlightPlanService.class).to(FlightPlanService.class).in(Singleton.class);
                    bind(IFlightPlanTemplateService.class).to(FlightPlanTemplateService.class).in(Singleton.class);
                    bind(IUpdateManager.class).to(UpdateManager.class).in(Singleton.class);
                    bind(UavSource.class).to(DetectedUavSource.class);
                    bind(UavConnectionAction.class).to(UavConnectionActionImpl.class);
                    bind(ISupportManager.class).to(SupportManager.class).in(Singleton.class);
                    bind(AoiWorkflowHints.class).in(Singleton.class);
                    bind(IProgressTaskFactory.class).to(ProgressTaskFactory.class).in(Singleton.class);
                    bind(IBackgroundTaskManager.class).to(DefaultBackgroundTaskManager.class).in(Singleton.class);
                    bind(MBackgroundTaskManager.class).to(DefaultBackgroundTaskManager.class);
                    bind(INotificationDispatcher.class).to(NotificationDispatcher.class).in(Singleton.class);
                    bind(IExportService.class).to(ExportService.class).in(Singleton.class);
                    bind(IMatchingService.class).to(MatchingService.class);
                    bind(IVeryUglyDialogHelper.class).to(VeryUglyDialogHelper.class).in(Singleton.class);
                    bind(IFileExtractor.class).to(FileExtractor.class);
                    bind(IDescriptionProvider.class).to(DescriptionProvider.class);
                    bind(IElevationModelUpdateHelper.class).to(MapDataChangedHelper.class).in(Singleton.class);
                    bind(IGeoFenceDetector.class).to(GeoFenceDetector.class).in(Singleton.class);
                    bind(ISrsManager.class).to(SrsManager.class).in(Singleton.class);
                    bind(IMissionInfoManager.class).to(MissionInfoManager.class).in(Singleton.class);
                    bind(IFlightplanExporterFactory.class).to(FlightplanExporterFactory.class).in(Singleton.class);
                    // bind(ILanguageHelper.class).to(MockLanguage.class).in(Singleton.class);
                }

                @Provides
                @Singleton
                IProfilingManager profilingManager(IPathProvider pathProvider, ISettingsManager settingsManager) {
                    return new ProfilingManager(
                        pathProvider.getProfilingDirectory().toFile(),
                        settingsManager.getSection(GeneralSettings.class).getProfilingEnabled());
                }
            });
        modules.add(new MvvmfxModule());
        return modules;
    }

    @Before
    public void setUp() throws Exception {
        IPathProvider pathProvider = new TestPathProvider();

        final Set<Module> modules = createModules();

        Injector injector = Guice.createInjector(modules);
        MvvmFX.setCustomDependencyInjector(key -> injector.getInstance(key));
        // DependencyInjector.getInstance().
        var settingsManager = DependencyInjector.getInstance().getInstanceOf(ISettingsManager.class);
        Path folder = settingsManager.getSection(PathSettings.class).getProjectFolder();
        mi = new MissionInfo(folder.toAbsolutePath());
        mavinciObjectFactory = DependencyInjector.getInstance().getInstanceOf(MavinciObjectFactory.class);
    }

    @Test
    @Ignore
    public void createMissionInfo() {
        Path projectsDirectory = settingsManager.getSection(PathSettings.class).getProjectFolder();
        Path folder = projectsDirectory.resolve(DEMO_SESSION_NAME);
        mi = new MissionInfo(folder.toAbsolutePath());
    }

    @Test
    @UITest
    public void createMissionObjects() {
        missionManager = DependencyInjector.getInstance().getInstanceOf(IMissionManager.class);
        settingsManager = DependencyInjector.getInstance().getInstanceOf(ISettingsManager.class);
        missionInfoManager = DependencyInjector.getInstance().getInstanceOf(MissionInfoManager.class);
        applicationContext = DependencyInjector.getInstance().getInstanceOf(IApplicationContext.class);
        navigationService = DependencyInjector.getInstance().getInstanceOf(INavigationService.class);
        var languageHelper = DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);
        var srsManager = DependencyInjector.getInstance().getInstanceOf(ISrsManager.class);
        var hardwareConfigurationManager =
            DependencyInjector.getInstance().getInstanceOf(HardwareConfigurationManager.class);
        var m =
            new Mission(
                mi,
                mavinciObjectFactory,
                settingsManager,
                missionInfoManager,
                languageHelper,
                hardwareConfigurationManager,
                srsManager);
    }

    private void openMission(Path missionFolder) {
        Path projectsDirectory = settingsManager.getSection(PathSettings.class).getProjectFolder();

        if (missionFolder == null) {
            return;
        }

        openMission(missionManager.openMission(missionFolder), false);
    }

    private void openDemoMission() {
        Path projectsDirectory = settingsManager.getSection(PathSettings.class).getProjectFolder();
        Path missionFolder = projectsDirectory.resolve(DEMO_SESSION_NAME);
        boolean alreadyCreated = Files.exists(missionFolder);
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
            Mission mission = missionManager.openMission(missionFolder);
            openMission(mission, false);
        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "Could not store Demo Session data on disk", e);
        }
    }

    private void openMission(final Mission mission, boolean clone) {
        (clone ? applicationContext.loadClonedMissionAsync(mission) : applicationContext.loadMissionAsync(mission))
            .onSuccess(
                future -> {
                    SidePanePage newPage =
                        mission.flightPlansProperty().isEmpty()
                            ? SidePanePage.START_PLANNING
                            : SidePanePage.EDIT_FLIGHTPLAN;

                    navigationService.navigateTo(newPage);
                    missionManager.refreshRecentMissionInfos();
                },
                Platform::runLater);
    }

    public class MockBaseModule extends AbstractModule {

        @Override
        protected void configure() {
            VersionProvider versionProvider = new VersionProvider();
            PathProvider pathProvider = new PathProvider();
            ReseatableFileAppender.setOutDir(pathProvider.getLogDirectory());
            bindListener(Matchers.any(), new ProvisioningLogger());
            bindListener(Matchers.any(), PostConstructionListener.getInstance());

            // Singleton scope
            bind(IVersionProvider.class).toInstance(versionProvider);
            bind(IPathProvider.class).toInstance(pathProvider);
            bind(IApplicationContext.class).to(ApplicationContext.class).in(Singleton.class);
            bind(IDialogService.class).to(MockDialogService.class).in(Singleton.class);
            // bind(IDialogContextProvider.class).to(DialogContextProvider.class).in(Singleton.class);
            bind(ILanguageHelper.class).to(LanguageHelper.class).in(Singleton.class);
            bind(IInterProcessHandler.class).to(InterProcessHandler.class).in(Singleton.class);

            // Local scope
            Scope localScope = LocalScope.getInstance();
            bind(INavigationService.class).to(NavigationService.class).in(localScope);
            bind(ISelectionManager.class).to(SelectionManager.class).in(localScope);
        }

    }
}
