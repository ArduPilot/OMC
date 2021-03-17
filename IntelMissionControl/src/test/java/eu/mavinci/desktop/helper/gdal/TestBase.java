/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.gdal;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.intel.missioncontrol.FileExtractor;
import com.intel.missioncontrol.IFileExtractor;
import com.intel.missioncontrol.RepositoryProvider;
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
import com.intel.missioncontrol.hardware.DescriptionProvider;
import com.intel.missioncontrol.hardware.HardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IDescriptionProvider;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.mission.IMissionInfoManager;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionInfoManager;
import com.intel.missioncontrol.mission.MissionManager;
import com.intel.missioncontrol.modules.AoiValidationModule;
import com.intel.missioncontrol.modules.FlightValidationModule;
import com.intel.missioncontrol.modules.FlightplanValidationModule;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.modules.MatchingValidationModule;
import com.intel.missioncontrol.modules.NetworkModule;
import com.intel.missioncontrol.modules.SettingsModule;
import com.intel.missioncontrol.persistence.Repository;
import com.intel.missioncontrol.persistence.insight.InsightRepository;
import com.intel.missioncontrol.persistence.local.LocalRepository;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.test.utils.MockBaseModule;
import com.intel.missioncontrol.ui.dialogs.IProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.IVeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.VeryUglyDialogHelper;
import com.intel.missioncontrol.ui.sidepane.analysis.IMatchingService;
import com.intel.missioncontrol.ui.sidepane.analysis.MatchingService;
import com.intel.missioncontrol.ui.update.IUpdateManager;
import com.intel.missioncontrol.ui.update.UpdateManager;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ValidationService;
import com.intel.missioncontrol.utils.DefaultBackgroundTaskManager;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.guice.internal.MvvmfxModule;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.helper.GeoFenceDetector;
import eu.mavinci.core.helper.IGeoFenceDetector;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.LicenceManager;
import eu.mavinci.desktop.gui.wwext.IElevationModelUpdateHelper;
import eu.mavinci.desktop.gui.wwext.MapDataChangedHelper;
import eu.mavinci.desktop.main.debug.profiling.ProfilingManager;
import eu.mavinci.flightplan.exporter.FlightplanExporterFactory;
import eu.mavinci.flightplan.exporter.IFlightplanExporterFactory;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.asyncfx.AsyncFX;
import org.junit.jupiter.api.BeforeAll;
import org.testfx.api.FxToolkit;

public class TestBase {
    static {
        AsyncFX.setRunningTests(true);

        try {
            System.setProperty("testfx.verbose", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("glass.verbose", "true");
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.verbose", "true");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("prism.verbose", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
            /////////////////////////////////////
            //System.setProperty("GDAL_DATA", "C:\\Users\\ekorotko\\Documents\\work\\desktop\\IntelMissionControl\\src\\main\\resources\\gdal\\data");
            /////////////////////////////////////////
            FxToolkit.registerPrimaryStage();

            while (!FxToolkit.isFXApplicationThreadRunning()) {
                Thread.sleep(100);
            }
        } catch (TimeoutException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class RepositoriesModule extends AbstractModule {

        @Override
        protected void configure() {
            //bind(IPathProvider.class).to(TestPathProvider.class).asEagerSingleton();
            bind(Repository.class).toProvider(RepositoryProvider.class).asEagerSingleton();

            bind(Repository.class)
                    .annotatedWith(Names.named("cloud-repository"))
                    .to(InsightRepository.class)
                    .asEagerSingleton();
            bind(Repository.class)
                    .annotatedWith(Names.named("local-repository"))
                    .to(LocalRepository.class)
                    .asEagerSingleton();
        }
    }

    private static Set<Module> createModules() {
        final Set<Module> modules = new HashSet<>();
        modules.add(new RepositoriesModule());
        modules.add(new MockBaseModule());
        modules.add(new SettingsModule());
        modules.add(new NetworkModule());
        modules.add(new MapModule());
        modules.add(new FlightplanValidationModule());
        modules.add(new AoiValidationModule());
        modules.add(new MatchingValidationModule());
        modules.add(new FlightValidationModule());

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
                        bind(ISupportManager.class).to(SupportManager.class).in(Singleton.class);
                        bind(AoiWorkflowHints.class).in(Singleton.class);
                        bind(IProgressTaskFactory.class).to(ProgressTaskFactory.class).in(Singleton.class);
                        bind(IBackgroundTaskManager.class).to(DefaultBackgroundTaskManager.class).in(Singleton.class);
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
                        install(new FactoryModuleBuilder().build(Mission.Factory.class));
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

    public TestBase() {
        IPathProvider pathProvider = new TestPathProvider();

        final Set<Module> modules = createModules();

        Injector injector = Guice.createInjector(modules);
        MvvmFX.setCustomDependencyInjector(key -> injector.getInstance(key));
        // DependencyInjector.getInstance().
        var settingsManager = DependencyInjector.getInstance().getInstanceOf(ISettingsManager.class);
        Path folder = settingsManager.getSection(PathSettings.class).getProjectFolder();
        //        mi = new MissionInfo(folder.toAbsolutePath());
        //        mavinciObjectFactory = DependencyInjector.getInstance().getInstanceOf(MavinciObjectFactory.class);
        //    }
    }
}
