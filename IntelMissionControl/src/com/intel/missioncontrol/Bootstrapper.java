/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.intel.missioncontrol.airspaces.services.Airmap2AirspaceService;
import com.intel.missioncontrol.airspaces.services.AirspaceServiceProvider;
import com.intel.missioncontrol.airspaces.services.BundledAirspaceService;
import com.intel.missioncontrol.airspaces.services.LegacyAirspaceManagerConfiguration;
import com.intel.missioncontrol.airspaces.services.LocationAwareAirspaceService;
import com.intel.missioncontrol.api.ExportService;
import com.intel.missioncontrol.api.FlightPlanService;
import com.intel.missioncontrol.api.FlightPlanTemplateService;
import com.intel.missioncontrol.api.IExportService;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.api.IFlightPlanTemplateService;
import com.intel.missioncontrol.api.support.ISupportManager;
import com.intel.missioncontrol.api.support.SupportManager;
import com.intel.missioncontrol.api.workflow.AoiWorkflowHints;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.common.PathProvider;
import com.intel.missioncontrol.common.PostConstructionListener;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.connector.ConnectionSupplier;
import com.intel.missioncontrol.connector.ConnectorIdGenerator;
import com.intel.missioncontrol.connector.ConnectorService;
import com.intel.missioncontrol.connector.IConnectionSupplier;
import com.intel.missioncontrol.connector.IConnectorService;
import com.intel.missioncontrol.connector.RandomUuidConnectorIdGenerator;
import com.intel.missioncontrol.diagnostics.DebugToastReporter;
import com.intel.missioncontrol.diagnostics.Debugger;
import com.intel.missioncontrol.diagnostics.PerformanceMonitorView;
import com.intel.missioncontrol.diagnostics.PerformanceMonitorViewModel;
import com.intel.missioncontrol.hardware.DescriptionProvider;
import com.intel.missioncontrol.hardware.HardwareConfigurationManager;
import com.intel.missioncontrol.hardware.IDescriptionProvider;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.MBackgroundTaskManager;
import com.intel.missioncontrol.helper.SystemInformation;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.map.elevation.ElevationModelFactoryEGM;
import com.intel.missioncontrol.mission.IMissionInfo;
import com.intel.missioncontrol.mission.IMissionInfoManager;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.MissionInfoManager;
import com.intel.missioncontrol.mission.MissionManager;
import com.intel.missioncontrol.modules.AirplaneValidationModule;
import com.intel.missioncontrol.modules.AoiValidationModule;
import com.intel.missioncontrol.modules.BaseModule;
import com.intel.missioncontrol.modules.FlightplanValidationModule;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.modules.MatchingValidationModule;
import com.intel.missioncontrol.modules.NetworkModule;
import com.intel.missioncontrol.modules.SettingsModule;
import com.intel.missioncontrol.networking.DelegatingNetworkStatus;
import com.intel.missioncontrol.networking.proxy.ProxyManager;
import com.intel.missioncontrol.settings.ExpertSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.splashscreen.MailslotClient;
import com.intel.missioncontrol.splashscreen.SplashScreen;
import com.intel.missioncontrol.ui.DecoratedScene;
import com.intel.missioncontrol.ui.ExceptionAlert;
import com.intel.missioncontrol.ui.MainView;
import com.intel.missioncontrol.ui.MainViewModel;
import com.intel.missioncontrol.ui.RootView;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.DialogService;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.IProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.IVeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.PreviewFilesDialogView;
import com.intel.missioncontrol.ui.dialogs.PreviewFilesDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.ProgressTaskFactory;
import com.intel.missioncontrol.ui.dialogs.SendSupportDialogView;
import com.intel.missioncontrol.ui.dialogs.SendSupportDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.SendSupportRetryView;
import com.intel.missioncontrol.ui.dialogs.SendSupportRetryViewModel;
import com.intel.missioncontrol.ui.dialogs.VeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.about.AboutDialogView;
import com.intel.missioncontrol.ui.dialogs.about.AboutDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.connection.ConnectionDialogView;
import com.intel.missioncontrol.ui.dialogs.connection.ConnectionDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.AutomaticChecksDialogView;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.AutomaticChecksDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.EmergencyProceduresView;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.EmergencyProceduresViewModel;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.ManualChecklistDialogView;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.ManualChecklistDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.ManualControlsDialogView;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.ManualControlsDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.SetupEmergencyProceduresView;
import com.intel.missioncontrol.ui.dialogs.preflightchecks.SetupEmergencyProceduresViewModel;
import com.intel.missioncontrol.ui.dialogs.savechanges.SaveChangesDialogView;
import com.intel.missioncontrol.ui.dialogs.savechanges.SaveChangesDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.tasks.BackgroundTasksView;
import com.intel.missioncontrol.ui.dialogs.tasks.BackgroundTasksViewModel;
import com.intel.missioncontrol.ui.dialogs.tasks.MavlinkEventLogDialogView;
import com.intel.missioncontrol.ui.dialogs.tasks.MavlinkEventLogDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.warnings.UnresolvedWarningsDialogView;
import com.intel.missioncontrol.ui.dialogs.warnings.UnresolvedWarningsDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.warnings.WarningsPopoverView;
import com.intel.missioncontrol.ui.dialogs.warnings.WarningsPopoverViewModel;
import com.intel.missioncontrol.ui.imageio.ImageLoaderInstaller;
import com.intel.missioncontrol.ui.menu.MainMenuCommandManager;
import com.intel.missioncontrol.ui.menu.MenuBarView;
import com.intel.missioncontrol.ui.menu.MenuBarViewModel;
import com.intel.missioncontrol.ui.navbar.connection.DetectedUavSource;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionAction;
import com.intel.missioncontrol.ui.navbar.connection.UavConnectionActionImpl;
import com.intel.missioncontrol.ui.navbar.connection.UavSource;
import com.intel.missioncontrol.ui.navbar.connection.model.IRtkStatisticFactory;
import com.intel.missioncontrol.ui.navbar.connection.model.RtkStatisticFactory;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.NtripRequester;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.NtripRequesterImpl;
import com.intel.missioncontrol.ui.navbar.layers.GeoTiffExternalSourceView;
import com.intel.missioncontrol.ui.navbar.layers.GeoTiffExternalSourceViewModel;
import com.intel.missioncontrol.ui.notifications.INotificationDispatcher;
import com.intel.missioncontrol.ui.notifications.NotificationDispatcher;
import com.intel.missioncontrol.ui.sidepane.analysis.AddFlightLogsView;
import com.intel.missioncontrol.ui.sidepane.analysis.AddFlightLogsViewModel;
import com.intel.missioncontrol.ui.sidepane.analysis.IMatchingService;
import com.intel.missioncontrol.ui.sidepane.analysis.MatchingService;
import com.intel.missioncontrol.ui.sidepane.planning.EditWaypointsView;
import com.intel.missioncontrol.ui.sidepane.planning.EditWaypointsViewModel;
import com.intel.missioncontrol.ui.sidepane.planning.FlightPlanTemplateManagementView;
import com.intel.missioncontrol.ui.sidepane.planning.FlightPlanTemplateManagementViewModel;
import com.intel.missioncontrol.ui.sidepane.planning.aoi.AoiAdvancedParametersView;
import com.intel.missioncontrol.ui.sidepane.planning.aoi.AoiAdvancedParametersViewModel;
import com.intel.missioncontrol.ui.update.IUpdateManager;
import com.intel.missioncontrol.ui.update.UpdateManager;
import com.intel.missioncontrol.ui.update.UpdateView;
import com.intel.missioncontrol.ui.update.UpdateViewModel;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ValidationService;
import com.intel.missioncontrol.utils.DefaultBackgroundTaskManager;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.internal.MvvmfxModule;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.flightplan.FlightplanFactory;
import eu.mavinci.core.helper.GeoFenceDetector;
import eu.mavinci.core.helper.IGeoFenceDetector;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.LicenceManager;
import eu.mavinci.desktop.bluetooth.BluetoothManager;
import eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserView;
import eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserViewModel;
import eu.mavinci.desktop.gui.wwext.DataFileStoreUtil;
import eu.mavinci.desktop.gui.wwext.IElevationModelUpdateHelper;
import eu.mavinci.desktop.gui.wwext.MapDataChangedHelper;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.SrsManager;
import eu.mavinci.desktop.main.core.Application;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.profiling.ProfilingManager;
import eu.mavinci.flightplan.FlightplanFactoryBase;
import eu.mavinci.flightplan.exporter.FlightplanExporterFactory;
import eu.mavinci.flightplan.exporter.IFlightplanExporterFactory;
import eu.mavinci.plane.management.Airport;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thebuzzmedia.exiftool.ExifTool;

/** Sets up and starts the application. */
public class Bootstrapper extends javafx.application.Application {

    static final String MAILSLOT_NAME = UUID.randomUUID().toString().replaceAll("-", "");
    private static final String KEY_EXIFTOOL_PATH = "exiftool.path";
    private static final long START_TIME = System.currentTimeMillis();
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);
    private final InterProcessHandler interProcessHandler = new InterProcessHandler();
    private Injector injector; // dont init here, otherwise the ProxySetup happens too late
    private Exception startupException;
    private MailslotClient mailslotClient;
    private IApplicationContext applicationContext;

    // DON'T REMOVE THIS REFERENCE IS NEEDED TO MAKE SURE WE GET AN INSTANCE OF
    // THIS HELPER TO FIRE UP FLIGHTPLAN RECOMPUTATIONS
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private IElevationModelUpdateHelper elevationModelUpdateHelper;

    // Map dialog view models to their associated views. This mapping is used internally by DialogService.
    private Map<Class<? extends ViewModel>, Class<? extends RootView<? extends ViewModelBase>>> createDialogMap() {
        Map<Class<? extends ViewModel>, Class<? extends RootView<? extends ViewModelBase>>> map = new HashMap<>();
        map.put(PerformanceMonitorViewModel.class, PerformanceMonitorView.class);
        map.put(SendSupportRetryViewModel.class, SendSupportRetryView.class);
        map.put(SendSupportDialogViewModel.class, SendSupportDialogView.class);
        map.put(PreviewFilesDialogViewModel.class, PreviewFilesDialogView.class);
        map.put(SaveChangesDialogViewModel.class, SaveChangesDialogView.class);
        map.put(AboutDialogViewModel.class, AboutDialogView.class);
        map.put(GeoTiffExternalSourceViewModel.class, GeoTiffExternalSourceView.class);
        map.put(UpdateViewModel.class, UpdateView.class);
        map.put(FlightPlanTemplateManagementViewModel.class, FlightPlanTemplateManagementView.class);
        map.put(BackgroundTasksViewModel.class, BackgroundTasksView.class);
        map.put(UnresolvedWarningsDialogViewModel.class, UnresolvedWarningsDialogView.class);
        map.put(WarningsPopoverViewModel.class, WarningsPopoverView.class);
        map.put(AddFlightLogsViewModel.class, AddFlightLogsView.class);
        map.put(EditWaypointsViewModel.class, EditWaypointsView.class);
        map.put(AoiAdvancedParametersViewModel.class, AoiAdvancedParametersView.class);
        map.put(SetupEmergencyProceduresViewModel.class, SetupEmergencyProceduresView.class);
        map.put(AutomaticChecksDialogViewModel.class, AutomaticChecksDialogView.class);
        map.put(ManualChecklistDialogViewModel.class, ManualChecklistDialogView.class);
        map.put(EmergencyProceduresViewModel.class, EmergencyProceduresView.class);
        map.put(ManualControlsDialogViewModel.class, ManualControlsDialogView.class);
        map.put(SpatialReferenceChooserViewModel.class, SpatialReferenceChooserView.class);
        map.put(ConnectionDialogViewModel.class, ConnectionDialogView.class);
        map.put(MavlinkEventLogDialogViewModel.class, MavlinkEventLogDialogView.class);
        return map;
    }

    private Set<Module> createModules(IPathProvider pathProvider) {
        final Set<Module> modules = new HashSet<>();

        modules.add(new BaseModule(pathProvider));
        modules.add(new SettingsModule());
        modules.add(new NetworkModule());
        modules.add(new MapModule());
        modules.add(new AirplaneValidationModule());
        modules.add(new FlightplanValidationModule());
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
                    bind(IDescriptionProvider.class).to(DescriptionProvider.class);
                    bind(IElevationModelUpdateHelper.class).to(MapDataChangedHelper.class).in(Singleton.class);
                    bind(IGeoFenceDetector.class).to(GeoFenceDetector.class).in(Singleton.class);
                    bind(ISrsManager.class).to(SrsManager.class).in(Singleton.class);
                    bind(IMissionInfoManager.class).to(MissionInfoManager.class).in(Singleton.class);
                    bind(IFlightplanExporterFactory.class).to(FlightplanExporterFactory.class).in(Singleton.class);
                }

                @Provides
                @Singleton
                IProfilingManager profilingManager(IPathProvider pathProvider, ISettingsManager settingsManager) {
                    return new ProfilingManager(
                        pathProvider.getProfilingDirectory().toFile(),
                        settingsManager.getSection(GeneralSettings.class).getProfilingEnabled());
                }
            });
        modules.add(new AirspacesModule());
        modules.add(new MvvmfxModule());
        modules.add(new ConnectorsModule());
        return modules;
    }

    private static class ConnectorsModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ConnectorIdGenerator.class).to(RandomUuidConnectorIdGenerator.class);
            bind(IConnectorService.class).to(ConnectorService.class);
            bind(IConnectionSupplier.class).to(ConnectionSupplier.class);
            bind(IRtkStatisticFactory.class).to(RtkStatisticFactory.class);
            bind(NtripRequester.class).to(NtripRequesterImpl.class);
        }
    }

    private class AirspacesModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(LocationAwareAirspaceService.class).toProvider(AirspaceServiceProvider.class).asEagerSingleton();
            // bind(AirMap2Source.class).toInstance(new AirMap2Source()); //< todo: lazily load AirMap2Source from
            // provider
            bind(LocationAwareAirspaceService.class)
                .annotatedWith(Names.named("AirMap2AirspaceService"))
                .to(Airmap2AirspaceService.class)
                .asEagerSingleton();
            bind(LocationAwareAirspaceService.class)
                .annotatedWith(Names.named("BundledAirspaceService"))
                .to(BundledAirspaceService.class)
                .asEagerSingleton();
            bind(LegacyAirspaceManagerConfiguration.class).asEagerSingleton();
        }
    }

    private void checkPrerequisites() {
        if (SystemInformation.isWindows() && !SystemInformation.isWindowsVistaOrLater()) {
            LOGGER.error("Windows Vista or later is required to run this program.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Open Mission Control");
            alert.setHeaderText(null);
            alert.setContentText("Windows Vista or later is required to run this program.");
            alert.showAndWait();
            System.exit(0);
        }

        if (interProcessHandler.isAlreadyRunning()) {
            System.err.println("Open Mission Control is already running.");
            System.exit(0);
        }
    }

    private void initializeBootstrapAgent() {
        if (EnvironmentOptions.VERIFY_METHOD_ACCESS) {
            try {
                Class<?> bootstrapAgentClass =
                    getClass().getClassLoader().loadClass("com.intel.missioncontrol.BootstrapAgent");
                Method installMethod = bootstrapAgentClass.getMethod("install");
                installMethod.invoke(null);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to install com.intel.missioncontrol.BootstrapAgent", e);
            }
        }
    }

    private void initializeEnvironment() throws IOException, URISyntaxException {
        // use on ALL platforms windows line end chars for all output files
        // reading will work also with unix line end. this makes linux e.g.
        // dumps more readable with windows editors
        System.setProperty("line.separator", "\r\n");

        // maybe patch for some lookup problems on hostnames on bad routers...
        // which could freeze the whole application
        System.setProperty("java.net.preferIPv4Stack", "true");

        System.loadLibrary("gluegen-rt");
        System.loadLibrary("jogl_desktop");
        System.loadLibrary("jogl_mobile");
        System.loadLibrary("nativewindow_awt");
        System.loadLibrary("nativewindow_win32");
        System.loadLibrary("newt");

        // JavaFX app settings have to be loaded first
        LOGGER.debug("System.properties = {}", System.getProperties().entrySet());
        LOGGER.debug("Environment = {}", System.getenv());
        LOGGER.debug("Number of Processors = {}", Runtime.getRuntime().availableProcessors());
        LOGGER.debug("freeMemory = {}", Runtime.getRuntime().freeMemory());
        LOGGER.debug("maxMemory = {}", Runtime.getRuntime().maxMemory());
        LOGGER.debug("totalMemory = {}", Runtime.getRuntime().totalMemory());

        URI uri = getClass().getResource("/com/intel/missioncontrol/").toURI();
        if (uri.getScheme().equals("jar")) {
            FileSystems.newFileSystem(uri, Collections.emptyMap());
        }
    }

    private void initializeWorldWindConfiguration(IPathProvider pathProvider) {
        // this has to be the ultimatively first call into the WW source tree, otherwise it will get ignored!
        DataFileStoreUtil.setCacheLocation(pathProvider.getCacheDirectory().toFile().getAbsoluteFile());

        // WWFactory.configWW() which is called later also sets values to the WW Configuration
        // for example ELEVATION_MODEL_FACTORY was lost

        // TODO @mstrauss answer: why was it moved here ??? why not the whole WWFactory.configWW() ???
        Configuration.setValue(AVKey.NETWORK_STATUS_CLASS_NAME, DelegatingNetworkStatus.class.getName());
        Configuration.setValue(AVKey.AIRSPACE_GEOMETRY_CACHE_SIZE, 60 * 1000 * 1000L);
        Configuration.setValue(AVKey.ELEVATION_EXTREMES_LOOKUP_CACHE_SIZE, 20 * 1000 * 1000L);
        Configuration.setValue(AVKey.PLACENAME_LAYER_CACHE_SIZE, 5 * 1000 * 1000L);
        Configuration.setValue(AVKey.SECTOR_GEOMETRY_CACHE_SIZE, 40 * 1000 * 1000L);
        Configuration.setValue(AVKey.TEXTURE_CACHE_SIZE, 600 * 1000 * 1000L);
        // mapbox data was broken very often on large screens and 10MB cache... but this was somehow fuzzy...
        Configuration.setValue(AVKey.TEXTURE_IMAGE_CACHE_SIZE, 60 * 1000 * 1000L);
        Configuration.setValue(AVKey.TILED_RASTER_PRODUCER_CACHE_SIZE, 400 * 1000 * 1000L);

        // transform from EGM to WG84 based elevation model
        Configuration.setValue(AVKey.ELEVATION_MODEL_FACTORY, ElevationModelFactoryEGM.class.getName());

        // without this, elevation modell starts to oszillate as soon a lot of DEMs are loaded!
        Configuration.setValue(
            AVKey.ELEVATION_TILE_CACHE_SIZE, 512 * 1024 * 1024L); // default is only 5MB -> way too small!
    }

    private void initializeInjector(IPathProvider pathProvider) {
        injector = Guice.createInjector(createModules(pathProvider));

        MvvmFX.setCustomDependencyInjector(
            key -> {
                Object value = injector.getInstance(key);

                if (value instanceof ViewBase && value instanceof AutoCloseable) {
                    if (applicationContext == null) {
                        applicationContext = injector.getInstance(IApplicationContext.class);
                    }

                    applicationContext.addClosingListener(
                        new IApplicationContext.IClosingListener() {
                            WeakReference<AutoCloseable> ref = new WeakReference<>((AutoCloseable)value);

                            @Override
                            public void close() {
                                try {
                                    AutoCloseable closeable = ref.get();
                                    if (closeable != null) {
                                        closeable.close();
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        });
                }

                return value;
            });

        PostConstructionListener listener = PostConstructionListener.getInstance();
        listener.addProgressListener(
            progress -> {
                if (progress < 1) {
                    mailslotClient.SendMessage(Double.toString(progress));
                }
            });
    }

    private void initializeGlobals(IPathProvider pathProvider) {
        ISettingsManager settingsManager = injector.getInstance(ISettingsManager.class);
        IVersionProvider versionProvider = injector.getInstance(IVersionProvider.class);

        Debug.setThreadErrorHandler();
        ImageLoaderInstaller.installThreadContextClassLoader();
        Dispatcher.runOnUI(ImageLoaderInstaller::installThreadContextClassLoader);

        Application.preInit(pathProvider);

        ImageLoaderInstaller.installThreadContextClassLoader();
        ImageLoaderInstaller.install(settingsManager);

        if (Debugger.isAttached()) {
            DebugToastReporter.install(injector.getInstance(IApplicationContext.class));
        }

        if (versionProvider.isEclipseLaunched()) {
            System.setProperty(
                KEY_EXIFTOOL_PATH,
                new File(
                        versionProvider.getInstallDir().getAbsoluteFile().getParentFile().getParentFile(),
                        "lib32\\exiftool.exe")
                    .getAbsolutePath());
        } else {
            System.setProperty(
                KEY_EXIFTOOL_PATH,
                new File(versionProvider.getInstallDir().getAbsoluteFile(), "exiftool.exe").getAbsolutePath());
        }

        Dispatcher.post(
            () -> {
                // if later on application run exiftool is launched, this sometimes takes up to 30 sec on some
                // machines,
                // so better get it ready early
                long time = System.currentTimeMillis();
                ExifTool.instance.ensureDaemonIsRunning();
                Debug.getLog()
                    .log(
                        Level.INFO,
                        "Launching exiftool in background DONE. took=" + (System.currentTimeMillis() - time) + " ms");
            });

        Airport.setInstance(new Airport());
        FlightplanFactory.setFactory(new FlightplanFactoryBase());

        BluetoothManager.init();

        // TODO: Check if this can be removed:
        Font.loadFont(getClass().getResource("/fonts/IntelClear_Bd.ttf").toExternalForm(), 14);
        Font.loadFont(getClass().getResource("/fonts/IntelClear_Rg.ttf").toExternalForm(), 14);

        ResourceBundle bundle = ResourceBundle.getBundle("com/intel/missioncontrol/IntelMissionControl");
        MvvmFX.setGlobalResourceBundle(bundle);

        ((DialogService)injector.getInstance(IDialogService.class)).setDialogMap(createDialogMap());
        injector.getInstance(IApplicationContext.class).addClosingListener(Application::closeAppForce);
    }

    @Override
    public void init() {
        try {
            mailslotClient = new MailslotClient(MAILSLOT_NAME);

            checkPrerequisites();
            initializeBootstrapAgent();
            initializeEnvironment();

            // as it is stated in the method description " * <p><b>Must be called early in the Application lifecycle,
            // before network clients are crated!</b>"
            // so before airspaces, worldwind etc (everything that might init networking)
            ProxyManager.install();
            IPathProvider pathProvider = new PathProvider();

            initializeWorldWindConfiguration(pathProvider);
            initializeInjector(pathProvider);
            initializeGlobals(pathProvider);
            initializeRecentMissions();

            // this has to be called before flightplan templates --- because it has to initialize
            // networkStatusProvider before it might be called
            // in the templates initialization
            WWFactory.configWW(injector.getInstance(ExpertSettings.class));
            Dispatcher.post(
                () -> {

                    // warming up available templates

                    // TODO: what's this?
                    // templateService.getFlightPlanTemplates();

                    elevationModelUpdateHelper =
                        injector.getInstance(
                            IElevationModelUpdateHelper
                                .class); // DON'T REMOVE THIS REFERENCE IS NEEDED TO MAKE SURE WE GET AN
                    // INSTANCE OF
                    // THIS HELPER TO FIRE UP FLIGHTPLAN RECOMPUTATIONS
                });

        } catch (Exception ex) {
            startupException = ex;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (startupException != null) {
            LOGGER.error(startupException.getMessage(), startupException);
            ExceptionAlert.showAndWait(startupException);
            System.exit(-1);
        }

        WindowHelper.setCloaked(primaryStage, true);
        WindowHelper.Accessor.setPrimaryStage(primaryStage);

        ApplicationContext applicationContext = injector.getInstance(ApplicationContext.class);
        primaryStage.setOnCloseRequest(applicationContext);
        primaryStage.setOnHiding(applicationContext);
        primaryStage.setOnHidden(applicationContext);

        try {
            final Image appIcon =
                new Image(
                    getClass()
                        .getResource("/com/intel/missioncontrol/app-icon/mission-control-icon.png")
                        .toExternalForm());
            primaryStage.getIcons().add(appIcon);

            ViewTuple<MainView, MainViewModel> mainViewTuple =
                FluentViewLoader.fxmlView(MainView.class)
                    .providedScopes(ViewModelBase.Accessor.newInitializerScope(null))
                    .load();

            ViewTuple<MenuBarView, MenuBarViewModel> menuViewTuple =
                FluentViewLoader.fxmlView(MenuBarView.class).context(mainViewTuple.getCodeBehind().getContext()).load();

            DecoratedScene scene = new DecoratedScene((Pane)mainViewTuple.getView());
            scene.setCustomContent(menuViewTuple.getView());
            primaryStage.setScene(scene);
            primaryStage.titleProperty().bind(mainViewTuple.getViewModel().titleProperty());
            WindowHelper.Accessor.setPrimaryViewModel(mainViewTuple.getViewModel());

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double initWidth = bounds.getWidth() * 0.75;
            double initHeight = bounds.getWidth() * 0.5;
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setX(bounds.getMinX() + (bounds.getWidth() - initWidth) * 0.5);
            primaryStage.setY(bounds.getMinY() + (bounds.getHeight() - initHeight) * 0.5);
            primaryStage.setWidth(initWidth);
            primaryStage.setHeight(initHeight);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(720);
            primaryStage.setMaximized(true);
            primaryStage.show();

            Application.fireGuiReadyLoaded();

            mailslotClient.SendMessage("1");
            mailslotClient.close();
            WindowHelper.setCloaked(primaryStage, false);

            LOGGER.info("Startup: " + (System.currentTimeMillis() - START_TIME) + "ms");
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            ExceptionAlert.showAndWait(ex);
            System.exit(-1);
        }
    }

    @Override
    public void stop() throws Exception {
        interProcessHandler.close();
        super.stop();
    }

    private void initializeRecentMissions() {
        try (LockedList<IMissionInfo> missionInfos =
            injector.getInstance(IMissionManager.class).recentMissionInfosProperty().lock()) {
            missionInfos.stream().filter(MainMenuCommandManager::isDemo).findFirst().ifPresent(this::dropDemoMission);
        }
    }

    private void dropDemoMission(IMissionInfo mission) {
        File directory = mission.getFolder();
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            LOGGER.error("Unable to delete demo mission", e);
        }

        IMissionManager missionManager = injector.getInstance(IMissionManager.class);
        List<IMissionInfo> recentMissions = missionManager.recentMissionInfosProperty();
        recentMissions.remove(mission);
    }

}
