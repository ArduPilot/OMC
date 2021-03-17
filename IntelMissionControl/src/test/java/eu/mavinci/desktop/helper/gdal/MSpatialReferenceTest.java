/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.gdal;

import static org.junit.jupiter.api.Assertions.*;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.intel.missioncontrol.Bootstrapper;
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
import com.intel.missioncontrol.common.GeoMath;
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
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.asyncfx.AsyncFX;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;

class MSpatialReferenceTest extends TestBase{

    @Test
    public void converting_to_Mercator_And_Back_works() throws Exception {
        SrsManager srsManager = DependencyInjector.getInstance().getInstanceOf(SrsManager.class);
        System.out.println();

        //"EPSG:3395" Mercator

        MSpatialReference ref = srsManager.getSrsByIdOrDefault("EPSG:3395");

        Position pos1 = new Position(Angle.fromDegrees(88), Angle.fromDegrees(120), 0);

        Position pos2 = new Position(Angle.fromDegrees(40.01), Angle.fromDegrees(10.01), 0);
        Position pos3 = new Position(Angle.fromDegrees(40.01), Angle.fromDegrees(10.00), 0);
        Position pos4 = new Position(Angle.fromDegrees(40.00), Angle.fromDegrees(10.01), 0);

        Vec4 vec1 = ref.fromWgs84(pos1);
        Vec4 vec2 = ref.fromWgs84(pos2);
        Vec4 vec3 = ref.fromWgs84(pos3);
        Vec4 vec4 = ref.fromWgs84(pos4);

        Position pos1Restored = ref.toWgs84(new Vec4(vec1.x, vec1.y));
        Position pos2Restored = ref.toWgs84(new Vec4(vec2.x, vec2.y));
        Position pos3Restored = ref.toWgs84(new Vec4(vec3.x, vec3.y));
        Position pos4Restored = ref.toWgs84(new Vec4(vec4.x, vec4.y));

        Assertions.assertTrue(pos1Restored.latitude.degrees - pos1.latitude.degrees < 1e-8);
        Assertions.assertTrue(pos1Restored.longitude.degrees - pos1.longitude.degrees < 1e-8);

        Assertions.assertTrue(pos2Restored.latitude.degrees - pos2.latitude.degrees < 1e-8);
        Assertions.assertTrue(pos2Restored.longitude.degrees - pos2.longitude.degrees < 1e-8);

        Assertions.assertTrue(pos3Restored.latitude.degrees - pos3.latitude.degrees < 1e-8);
        Assertions.assertTrue(pos3Restored.longitude.degrees - pos3.longitude.degrees < 1e-8);

        Assertions.assertTrue(pos4Restored.latitude.degrees - pos4.latitude.degrees < 1e-8);
        Assertions.assertTrue(pos4Restored.longitude.degrees - pos4.longitude.degrees < 1e-8);
    }

}
