/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.test.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.intel.missioncontrol.FileExtractor;
import com.intel.missioncontrol.IFileExtractor;
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
import com.intel.missioncontrol.mission.MissionInfoManager;
import com.intel.missioncontrol.mission.MissionManager;
import com.intel.missioncontrol.modules.FlightplanValidationModule;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.modules.NetworkModule;
import com.intel.missioncontrol.modules.SettingsModule;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.DebugSettings;
import com.intel.missioncontrol.settings.DisplaySettings;
import com.intel.missioncontrol.settings.FalconDataExportSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.InternetConnectivitySettings;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.settings.PlaneSettings;
import com.intel.missioncontrol.settings.SettingsManager;
import com.intel.missioncontrol.settings.UpdateSettings;
import com.intel.missioncontrol.ui.dialogs.IProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.IVeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.VeryUglyDialogHelper;
import com.intel.missioncontrol.ui.sidepane.analysis.AnalysisSettings;
import com.intel.missioncontrol.ui.sidepane.analysis.IMatchingService;
import com.intel.missioncontrol.ui.sidepane.analysis.MatchingService;
import com.intel.missioncontrol.ui.update.IUpdateManager;
import com.intel.missioncontrol.ui.update.UpdateManager;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ValidationService;
import com.intel.missioncontrol.utils.DefaultBackgroundTaskManager;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.guice.internal.MvvmfxModule;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.helper.GeoFenceDetector;
import eu.mavinci.core.helper.IGeoFenceDetector;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.LicenceManager;
import eu.mavinci.desktop.gui.wwext.IElevationModelUpdateHelper;
import eu.mavinci.desktop.gui.wwext.MapDataChangedHelper;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.SrsManager;
import eu.mavinci.desktop.main.debug.profiling.ProfilingManager;
import eu.mavinci.flightplan.exporter.FlightplanExporterFactory;
import eu.mavinci.flightplan.exporter.IFlightplanExporterFactory;
import java.util.HashSet;
import java.util.Set;

public class MockingHelper {

    public static ISettingsManager createSettingManagerMock() {
//        ISettingsManager settingsManager = mock(SettingsManager.class);
        // when(settingsManager.getSection(GeneralSettings.class)).thenReturn(new GeneralSettings());
//        when(settingsManager.getSection(DebugSettings.class)).thenReturn(new DebugSettings());
//        when(settingsManager.getSection(DisplaySettings.class)).thenReturn(new DisplaySettings());
//        when(settingsManager.getSection(PathSettings.class)).thenReturn(new PathSettings(new TestPathProvider()));
//        when(settingsManager.getSection(InternetConnectivitySettings.class))
//            .thenReturn(new InternetConnectivitySettings());
//        when(settingsManager.getSection(AirspacesProvidersSettings.class)).thenReturn(new AirspacesProvidersSettings());
//        when(settingsManager.getSection(UpdateSettings.class)).thenReturn(new UpdateSettings());
//        when(settingsManager.getSection(AnalysisSettings.class)).thenReturn(new AnalysisSettings());
//        when(settingsManager.getSection(FalconDataExportSettings.class)).thenReturn(new FalconDataExportSettings());
//        when(settingsManager.getSection(PlaneSettings.class)).thenReturn(new PlaneSettings());
//        return settingsManager;
        return null;
    }

    public static Set<Module> createTestModules() {
        final Set<Module> modules = new HashSet<>();

        modules.add(new MockBaseModule());
        modules.add(new SettingsModule());
        modules.add(new NetworkModule());
        modules.add(new MapModule());
        modules.add(new FlightplanValidationModule());
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
}
