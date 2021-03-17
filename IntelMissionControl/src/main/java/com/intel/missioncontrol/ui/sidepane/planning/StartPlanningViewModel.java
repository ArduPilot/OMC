/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.api.IFlightPlanService;
import com.intel.missioncontrol.api.IFlightPlanTemplateService;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.helper.SystemInformation;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.dialogs.IVeryUglyDialogHelper;
import com.intel.missioncontrol.ui.dialogs.ProgressTask;
import com.intel.missioncontrol.ui.dialogs.laanc.airmap.LaancAirmapDialogViewModel;
import com.intel.missioncontrol.ui.dialogs.warnings.UnresolvedWarningsDialogViewModel;
import com.intel.missioncontrol.ui.menu.MainMenuModel;
import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import com.intel.missioncontrol.ui.validation.IValidationService;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.core.plane.AirplaneType;
import eu.mavinci.desktop.gui.doublepanel.planemain.FlightplanExportTypes;
import eu.mavinci.desktop.helper.FileFilter;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;
import eu.mavinci.flightplan.exporter.IFlightplanExporter;
import eu.mavinci.flightplan.exporter.IFlightplanExporterFactory;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.client.utils.URIBuilder;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressLinter(
    value = {"IllegalViewModelMethod", "ViewClassInViewModel"},
    reviewer = "mstrauss",
    justification = "Legacy file, needs to be refactored."
)
public class StartPlanningViewModel extends ViewModelBase {

    public static final String FLIGHT_PLAN_TEMPLATE_EVENT = "FLIGHT_PLAN_TEMPLATE_EVENT";
    public static final String USE_ACTION = "USE";
    public static final String CREATE_ACTION = "CREATE";
    public static final String DELETE_ACTION = "DELETE_ACTION";
    public static final String REFRESH_ACTION = "REFRESH";
    public static final String IMPORT_ACTION = "IMPORT";
    public static final String UPDATE_ACTION = "UPDATE";
    public static final String EXPORT_DIALOG_TITLE =
        "com.intel.missioncontrol.ui.planning.FlightplanViewModel.exportDialog.title";
    public static final String EXPORT_DIALOG_HEADER =
        "com.intel.missioncontrol.ui.planning.FlightplanViewModel.exportDialog.header";
    private static final Logger logger = LoggerFactory.getLogger(StartPlanningViewModel.class);
    private static final String SAVE_AS_FLIGHT_PLAN_DIALOG_TITLE =
        "com.intel.missioncontrol.ui.SidePaneView.dialog.saveAsFlightPlan.title";
    private static final String SAVE_FLIGHT_PLAN_DIALOG_TITLE =
        "com.intel.missioncontrol.ui.SidePaneView.dialog.saveFlightPlan.title";
    private static final String SAVE_AS_FLIGHT_PLAN_EXTENSION_NAME =
        "com.intel.missioncontrol.ui.SidePaneView.dialog.saveAsFlightPlan.extension.Name";
    private static final String SAVE_AS_FLIGHT_PLAN_EXTENSION_REGEXP =
        "com.intel.missioncontrol.ui.SidePaneView.dialog.saveAsFlightPlan.extension.Regexp";
    private static final String ALL_FILES_FLIGHT_PLAN_EXTENSION_NAME =
        "com.intel.missioncontrol.ui.SidePaneView.dialog.allFilesFlightPlan.extension.Name";
    private static final String ALL_FILES_FLIGHT_PLAN_EXTENSION_REGEXP =
        "com.intel.missioncontrol.ui.SidePaneView.dialog.allFilesFlightPlan.extension.Regexp";
    private static final String SHOW_ON_MAP =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsLocationView.btnShowOnMap";
    private static final FileChooser.ExtensionFilter ACP_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.ACP.getDescription(), "*.acp");
    private static final FileChooser.ExtensionFilter ANP_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.ANP.getDescription(), "*.anp");
    private static final FileChooser.ExtensionFilter CSV_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.CSV.getDescription(), "*.csv");
    private static final FileChooser.ExtensionFilter KML_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.KML.getDescription(), "*.kml");
    private static final FileChooser.ExtensionFilter GPX_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.GPX.getDescription(), "*.gpx");
    private static final FileChooser.ExtensionFilter RTE_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.RTE.getDescription(), "*.rte");
    private static final FileChooser.ExtensionFilter FPL_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.FPL.getDescription(), "*.fpl");
    private static final FileChooser.ExtensionFilter CSV_FALCON_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.ASCTECCSV.getDescription(), "*.csv");
    private static final FileChooser.ExtensionFilter CSV_FALCON_JPG_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.ASCTECCSVJPG.getDescription(), "*.csv");
    private static final FileChooser.ExtensionFilter LCSV_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.LCSV.getDescription(), "*.csv");
    private static final FileChooser.ExtensionFilter PLAN_FILTER =
        new FileChooser.ExtensionFilter(FlightplanExportTypes.QGroundControlPlan.getDescription(), "*.plan");

    private static final List<FileChooser.ExtensionFilter> SPECIAL_FILTERS =
        Arrays.asList(
            CSV_FILTER,
            KML_FILTER,
            GPX_FILTER,
            RTE_FILTER,
            FPL_FILTER,
            CSV_FALCON_FILTER,
            CSV_FALCON_JPG_FILTER,
            ANP_FILTER,
            PLAN_FILTER);

    private static final List<FileChooser.ExtensionFilter> FILTERS_DJI = Arrays.asList(LCSV_FILTER);

    private static final List<FileChooser.ExtensionFilter> FILTERS_FALCON = Arrays.asList(ACP_FILTER);

    private static final String FML = ".fml";

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private PlanningScope planningScope;

    private final IApplicationContext applicationContext;
    private final IValidationService validationService;
    private final INavigationService navigationService;
    private final ILanguageHelper languageHelper;
    private final IDialogService dialogService;
    private final IFlightPlanTemplateService flightPlanTemplateService;
    private final NotificationCenter notificationCenter;
    private final MavinciObjectFactory mavinciObjectFactory;
    private final ILicenceManager licenceManager;
    private final IFlightplanExporterFactory flightplanExporterFactory;
    private final IElevationModel elevationModel;

    private final ReadOnlyStringProperty missionName;
    private final SimpleListProperty<FlightPlan> currentFlightPlans =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final IFlightPlanService flightPlanService;
    private final GeneralSettings generalSettings;
    private final IVeryUglyDialogHelper dialogHelper;
    private final IMapView mapView;
    private final IMapController mapController;
    private BooleanProperty isBusy = new SimpleBooleanProperty();
    private BooleanProperty flightPlanSaveable = new SimpleBooleanProperty();
    private ObjectProperty<FlightPlanTemplate> selectedTemplate = new SimpleObjectProperty<>();
    private ListProperty<FlightPlanTemplate> availableTemplates =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private ObjectProperty<AltitudeAdjustModes> selectedTerrainMode = new SimpleObjectProperty<>();

    private final Command openAoiScreenCommand;
    private final Command showTemplateManagementCommand;
    private final Command renameMissionCommand;

    @Inject
    public StartPlanningViewModel(
            IApplicationContext applicationContext,
            IValidationService validationService,
            INavigationService navigationService,
            ILanguageHelper languageHelper,
            IDialogService dialogService,
            IFlightPlanService flightPlanService,
            IFlightPlanTemplateService flightPlanTemplateService,
            NotificationCenter notificationCenter,
            MavinciObjectFactory mavinciObjectFactory,
            ISettingsManager settingsManager,
            IVeryUglyDialogHelper dialogHelper,
            IMapView mapView,
            IMapController mapController,
            IFlightplanExporterFactory flightplanExporterFactory,
            IElevationModel elevationModel,
            ILicenceManager licenceManager) {
        this.mapView = mapView;
        this.mapController = mapController;
        this.applicationContext = applicationContext;
        this.validationService = validationService;
        this.navigationService = navigationService;
        this.languageHelper = languageHelper;
        this.dialogService = dialogService;
        this.flightPlanService = flightPlanService;
        this.flightPlanTemplateService = flightPlanTemplateService;
        this.notificationCenter = notificationCenter;
        this.mavinciObjectFactory = mavinciObjectFactory;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.dialogHelper = dialogHelper;
        this.flightplanExporterFactory = flightplanExporterFactory;
        this.elevationModel = elevationModel;
        this.licenceManager = licenceManager;

        this.missionName =
            PropertyPath.from(applicationContext.currentMissionProperty()).selectReadOnlyString(Mission::nameProperty);

        openAoiScreenCommand = new DelegateCommand(this::openAoiScreen);

        showTemplateManagementCommand =
            new DelegateCommand(
                () ->
                    dialogService.requestDialogAndWait(
                        StartPlanningViewModel.this, FlightPlanTemplateManagementViewModel.class));
        renameMissionCommand = new DelegateCommand(applicationContext::renameCurrentMission);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        availableTemplates.addAll(flightPlanTemplateService.getFlightPlanTemplates());
        notificationCenter.subscribe(
            FLIGHT_PLAN_TEMPLATE_EVENT,
            (key, payload) -> {
                int payLoadCount = Optional.ofNullable(payload).map(pl -> pl.length).orElse(0);
                if (payload != null && payLoadCount > 1) {
                    FlightPlanTemplate template;
                    String action = (String)payload[0];
                    Object objectTransfered = payload[1];
                    switch (action) {
                    case USE_ACTION:
                        selectedTemplate.set((FlightPlanTemplate)objectTransfered);
                        openAoiScreenCommand.execute();
                        break;
                    case CREATE_ACTION:
                        availableTemplates.add((FlightPlanTemplate)objectTransfered);
                        break;
                    case DELETE_ACTION:
                        template = (FlightPlanTemplate)objectTransfered;
                        availableTemplates.remove(template);
                        break;
                    case REFRESH_ACTION:
                        template = (FlightPlanTemplate)objectTransfered;
                        int index = availableTemplates.indexOf(template);
                        availableTemplates.remove(template);
                        availableTemplates.add(index, template);
                        break;
                    case IMPORT_ACTION:
                        importTemplate((File)objectTransfered);
                        break;
                    case UPDATE_ACTION:
                        if (payLoadCount > 2) {
                            updateTemplateWith((File)objectTransfered, (String)payload[2]);
                        }

                        break;
                    default:
                        break;
                    }
                }
            });

        flightPlanSaveable.bind(
            PropertyPath.from(planningScope.currentFlightplanProperty())
                .selectReadOnlyBoolean(FlightPlan::saveableProperty));

        MenuModel menuModel = mainScope.mainMenuModelProperty().get();
        menuModel
            .find(MainMenuModel.FlightPlan.CLONE)
            .setActionHandler(
                () -> {
                    FlightPlan currentFP = planningScope.currentFlightplanProperty().get();
                    applicationContext.getCurrentMission().setCurrentFlightPlan(null);
                    cloneFlightPlan(currentFP);
                },
                flightPlanSaveable.and(applicationContext.currentMissionIsNoDemo()));

        menuModel
            .find(MainMenuModel.FlightPlan.SAVE)
            .setActionHandler(
                this::saveFlightPlan,
                flightPlanSaveable
                    .and(
                        PropertyPath.from(planningScope.currentFlightplanProperty())
                            .selectReadOnlyBoolean(FlightPlan::hasUnsavedChangesProperty))
                    .and(applicationContext.currentMissionIsNoDemo()));

        menuModel
            .find(MainMenuModel.FlightPlan.OPEN)
            .setActionHandler(
                this::openFlightPlan,
                applicationContext
                    .currentMissionProperty()
                    .isNotNull()
                    .and(applicationContext.currentMissionIsNoDemo()));

        MenuModel flightPlanMenuModel = mainScope.flightPlanMenuModelProperty().get();
        flightPlanMenuModel
            .find(FlightPlanMenuModel.MenuIds.OPEN)
            .setActionHandler(
                this::openFlightPlan,
                applicationContext
                    .currentMissionProperty()
                    .isNotNull()
                    .and(applicationContext.currentMissionIsNoDemo()));

        menuModel
            .find(MainMenuModel.FlightPlan.SAVE_AS)
            .setActionHandler(
                this::saveAsFlightPlanChooser, flightPlanSaveable.and(applicationContext.currentMissionIsNoDemo()));
        menuModel
            .find(MainMenuModel.FlightPlan.RECALCULATE)
            .setActionHandler(
                this::recalculateFlightPlan,
                planningScope
                    .currentFlightplanProperty()
                    .isNotNull()
                    .and(
                        PropertyPath.from(planningScope.currentFlightplanProperty())
                            .selectReadOnlyBoolean(FlightPlan::saveableProperty)));

        menuModel
            .find(MainMenuModel.FlightPlan.REVERT_CHANGES)
            .setActionHandler(this::revertFlightPlanChanges, planningScope.currentFlightplanProperty().isNotNull());

        menuModel
            .find(MainMenuModel.FlightPlan.EXPORT)
            .setActionHandler(
                this::exportFlightPlan,
                planningScope
                    .currentFlightplanProperty()
                    .isNotNull()
                    .and(
                        PropertyPath.from(planningScope.currentFlightplanProperty())
                            .selectReadOnlyBoolean(FlightPlan::saveableProperty))
                    .and(validationService.canExportFlightProperty()));
        menuModel
            .find(MainMenuModel.FlightPlan.AIRMAP_LAANC)
            .setActionHandler(
                this::airmapLaancApprove,
                planningScope
                    .currentFlightplanProperty()
                    .isNotNull()
                    .and(
                        PropertyPath.from(planningScope.currentFlightplanProperty())
                            .selectReadOnlyBoolean(FlightPlan::saveableProperty))
                    .and(validationService.canExportFlightProperty()));

        menuModel
            .find(MainMenuModel.FlightPlan.SAVE_AS_TEMPLATE)
            .setActionHandler(this::saveFlightPlanAsTemplate, flightPlanSaveable);
        menuModel
            .find(MainMenuModel.FlightPlan.SAVE_AS_TEMPLATE)
            .visibleProperty()
            .bind(generalSettings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG));

        menuModel
            .find(MainMenuModel.FlightPlan.UPDATE_PARENT_TEMPLATE)
            .setActionHandler(this::updateParentTemplate, planningScope.currentFlightplanProperty().isNotNull());
        menuModel
            .find(MainMenuModel.FlightPlan.UPDATE_PARENT_TEMPLATE)
            .visibleProperty()
            .bind(generalSettings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
        menuModel
            .find(MainMenuModel.FlightPlan.SAVE_AND_FlY)
            .setActionHandler(
                this::saveFlightPlan,
                flightPlanSaveable
                    .and(validationService.canExportFlightProperty())
                    .and(
                        licenceManager
                            .isGrayHawkEditionProperty()
                            .or(licenceManager.maxOperationLevelProperty().isEqualTo(OperationLevel.DEBUG)))
                    .and(applicationContext.currentMissionIsNoDemo()));
        menuModel
            .find(MainMenuModel.FlightPlan.SAVE_AND_FlY)
            .visibleProperty()
            .bind(generalSettings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG));

        selectedTerrainMode.set(AltitudeAdjustModes.FOLLOW_TERRAIN);
    }

    private void updateParentTemplate() {
        if (!dialogService.requestConfirmation(
                languageHelper.getString(StartPlanningViewModel.class.getName() + ".updateHardwarePresetsTitle"),
                languageHelper.getString(StartPlanningViewModel.class.getName() + ".updateHardwarePresetsText"))) {
            return;
        }

        Flightplan legacyFlightPlan =
            Optional.ofNullable(applicationContext.getCurrentMission())
                .map(mission -> mission.currentFlightPlanProperty().get())
                .map(FlightPlan::getLegacyFlightplan)
                .get();
        String templateName = legacyFlightPlan.getBasedOnTemplate();
        notificationCenter.publish(
            StartPlanningViewModel.FLIGHT_PLAN_TEMPLATE_EVENT,
            StartPlanningViewModel.UPDATE_ACTION,
            legacyFlightPlan.getFile(),
            templateName);
    }

    private void saveFlightPlanAsTemplate() {
        Flightplan legacyFlightPlan =
            Optional.ofNullable(applicationContext.getCurrentMission())
                .map(mission -> mission.currentFlightPlanProperty().get())
                .map(FlightPlan::getLegacyFlightplan)
                .get();
        File fpFile = legacyFlightPlan.getFile();
        String originalFpName = legacyFlightPlan.getName();
        String templateName = flightPlanTemplateService.generateTemplateName(originalFpName);
        legacyFlightPlan.setName(templateName);
        File templateFile = flightPlanTemplateService.generateTemplateFile(fpFile);
        legacyFlightPlan.saveToLocation(templateFile);
        legacyFlightPlan.setName(originalFpName);
        notificationCenter.publish(
            StartPlanningViewModel.FLIGHT_PLAN_TEMPLATE_EVENT, StartPlanningViewModel.IMPORT_ACTION, templateFile);
    }

    private List<FileChooser.ExtensionFilter> getExtensions() {
        List<FileChooser.ExtensionFilter> extensions = new ArrayList<>();
        Licence licence = licenceManager.getActiveLicence();
        if (licence.isFalconEdition()) {
            extensions.addAll(FILTERS_FALCON);
        }

        if (licence.isDJIEdition()) {
            extensions.addAll(FILTERS_DJI);
        }

        if (generalSettings.getOperationLevel() == OperationLevel.DEBUG) {
            extensions.addAll(SPECIAL_FILTERS);
        }

        return extensions;
    }

    private FileChooser.ExtensionFilter getSelectedFilter(IPlatformDescription platformDescription) {
        if (platformDescription.getName().toUpperCase().startsWith("DJI")) {
            return LCSV_FILTER;
        }

        switch (platformDescription.getAirplaneType()) {
        case SIRIUS_BASIC:
        case SIRIUS_PRO:
            return KML_FILTER;
        case FALCON8:
            return CSV_FALCON_FILTER;
        case FALCON8PLUS:
            return ACP_FILTER;
        default:
            throw new IllegalArgumentException("Unknown UAV type " + platformDescription.getName());
        }
    }

    private String getRootPath() {
        return SystemInformation.isWindows() ? System.getenv("SystemDrive") + File.separator : File.separator;
    }

    private File getInitialDirectory(AirplaneType airplaneType) throws FileNotFoundException {
        String lastExportFolder = generalSettings.getLastFlightPlanExportFolder();
        if (lastExportFolder != null && !lastExportFolder.isEmpty()) {
            File folderFile = new File(lastExportFolder);
            if (folderFile.exists()) {
                return folderFile;
            }
        }

        switch (airplaneType) {
        case FALCON8PLUS:
            return new File(getRootPath());
        case SIRIUS_BASIC:
        case FALCON8:
            return getDefaultFlightPlansDir();
        default:
            throw new IllegalArgumentException("Unknown UAV type " + airplaneType);
        }
    }

    private void saveInitialDirectory(File file) {
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }

        if (file == null) {
            return;
        }

        generalSettings.lastFlightPlanExportFolderProperty().set(file.getAbsolutePath());
    }

    private void airmapLaancApprove() {
        FlightPlan currentFlightplan = planningScope.getCurrentFlightplan();
        if (currentFlightplan != null && currentFlightplan.getSector() != null) {
            airmapLaancApprove(currentFlightplan, elevationModel);
        }
    }

    public static void airmapLaancApprove(FlightPlan currentFlightplan, IElevationModel elevationModel) {
        Sector s = currentFlightplan.getSector();
        String pointGeoJSON = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[";
        boolean first = true;
        ArrayList<LatLon> corners = new ArrayList<LatLon>(5);
        corners.addAll(Arrays.asList(s.getCorners()));
        corners.add(corners.get(0));
        for (LatLon latLon : corners) {
            if (!first) {
                pointGeoJSON += ",";
            }

            first = false;
            pointGeoJSON +=
                "["
                    + MathHelper.round(latLon.longitude.degrees, 5)
                    + ","
                    + MathHelper.round(latLon.latitude.degrees, 5)
                    + "]"; // 5 digits are apprx. 1m resolution
        }

        pointGeoJSON += "]]}}";
        MinMaxPair minMaxElev = elevationModel.getMaxElevation(s);
        double bufferToSectorInMeter = 20;
        /*
        Position takeoff = currentFlightplan.getLegacyFlightplan().getTakeoffPosition();
        LocalDateTime dateStart = LocalDateTime.now();
        LocalDateTime dateEnd = LocalDateTime.now().plus(Duration.ofHours(1));
        try {
            dateEnd =
                dateEnd.plus(
                    Duration.ofSeconds(
                        Math.round(currentFlightplan.getLegacyFlightplan().getFPsim().getSimResult().flightTime)));
        } catch (Exception e) {
            logger.warn("cant add estimated flight duration to landing time", e);
        }

        String start = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateStart);
        String end = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateEnd);
        String pilot = "IMC_Pilot";

        try {
            pilot +=
                ":"
                    + DependencyInjector.getInstance()
                        .getInstanceOf(ISettingsManager.class)
                        .getSection(GeneralSettings.class)
                        .fullNameInSupportProperty()
                        .get();
        } catch (Exception e) {
            logger.info("cant extract pilot name from settings", e);
        }
        */

        try {
            URI deepLink =
                new URIBuilder()
                    .setScheme("https")
                    .setHost("www.airmap.com")
                    .setPath("create_flight/v1/")
                    .addParameter("geometry", pointGeoJSON)
                    // .addParameter("takeoff_latitude", "" + takeoff.latitude.degrees) // not applicable for poylgone
                    // missions
                    // .addParameter("takeoff_longitude", "" + takeoff.longitude.degrees) // not applicable for poylgone
                    // missions
                    .addParameter("altitude", "" + (currentFlightplan.getMaxElev().getAsDouble() - minMaxElev.min))
                    // .addParameter("pilot_id", pilot) //set by the app
                    // .addParameter("start_time", start) //not configureable
                    // .addParameter("end_time", end)//not configureable
                    .addParameter("buffer", "" + bufferToSectorInMeter) // only used for points, not for poylgones
                    .build();
            URI dynamicLink =
                new URIBuilder(
                        URI.create(
                            "https://xjy5t.app.goo.gl/?apn=com.airmap.airmap&isi=1042824733&ibi=com.airmap.AirMap&efr=1&ofl=https://www.airmap.com/airspace-authorization/&utm_source=partner&utm_medium=deeplink&utm_campaign=laanc"))
                    .addParameter("link", deepLink.toString())
                    .build();

            logger.info(
                "height: "
                    + (currentFlightplan.getMaxElev().getAsDouble() - minMaxElev.min)
                    + " => "
                    + currentFlightplan.getMaxElev().getAsDouble()
                    + "-"
                    + minMaxElev.min);
            logger.info("deepLink: " + deepLink);
            logger.info("dynamicLink: " + dynamicLink);

            String qrCodeFile =
                currentFlightplan.getResourceFile().getParent() + "\\" + currentFlightplan.getName() + "_qrCode.png";

            generateQrCode(dynamicLink, qrCodeFile);
            showLaancAirmap(new String[] {dynamicLink.toString(), qrCodeFile});
        } catch (Exception e) {
            logger.error("Error generating LAANC approval URL", e);
        }
    }

    private static void generateQrCode(URI dynamicLink, String qrCodeFile) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = qrCodeWriter.encode(dynamicLink.toString(), BarcodeFormat.QR_CODE, 500, 500);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", new File(qrCodeFile).toPath());
        } catch (WriterException e) {
            logger.error("Error generating LAANC approval URL", e);
        } catch (IOException e) {
            logger.error("Error generating LAANC approval URL", e);
        }
    }

    private static void showLaancAirmap(String[] qrCode) {
        IDialogService dialogService;
        dialogService = DependencyInjector.getInstance().getInstanceOf(IDialogService.class);
        LaancAirmapDialogViewModel vm =
            dialogService.requestDialogAndWait(
                WindowHelper.getPrimaryViewModel(), LaancAirmapDialogViewModel.class, () -> qrCode);
        if (!vm.getDialogResult()) {
            return;
        }
    }

    private void exportFlightPlan() {
        UnresolvedWarningsDialogViewModel vm =
            dialogService.requestDialogAndWait(this, UnresolvedWarningsDialogViewModel.class);
        if (!vm.getDialogResult()) {
            return;
        }

        FlightPlan currentFlightplan = planningScope.getCurrentFlightplan();
        if (currentFlightplan != null) {
            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle(languageHelper.getString("com.intel.missioncontrol.ui.SidePaneViewModel.export"));
            fileChooser.setInitialFileName(currentFlightplan.getName());
            fileChooser.getExtensionFilters().addAll(getExtensions());
            fileChooser.setSelectedExtensionFilter(
                getSelectedFilter(
                    planningScope.selectedHardwareConfigurationProperty().get().getPlatformDescription()));
            try {
                fileChooser.setInitialDirectory(
                    getInitialDirectory(
                        planningScope
                            .selectedHardwareConfigurationProperty()
                            .get()
                            .getPlatformDescription()
                            .getAirplaneType()));
            } catch (FileNotFoundException e) {
                logger.error("Error opening file for cloning ", e);
            }

            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                String description = fileChooser.getSelectedExtensionFilter().getDescription();
                FlightplanExportTypes type = FlightplanExportTypes.fromDescription(description);
                IFlightplanExporter exporter = flightplanExporterFactory.createExporter(type);
                saveInitialDirectory(file);
                ProgressTask progressMonitor =
                    new ProgressTask(languageHelper.getString(EXPORT_DIALOG_TITLE), dialogService, 0) {
                        @Override
                        protected Void call() throws Exception {
                            exporter.export(currentFlightplan.getLegacyFlightplan(), file, this);
                            return null;
                        }
                    };

                // TODO: WWJFX - removed / check correct changed?
                dialogHelper.createProgressDialogFromTask(
                    progressMonitor,
                    // mainScope.getAppWindow(),
                    languageHelper.getString(EXPORT_DIALOG_TITLE),
                    languageHelper.getString(EXPORT_DIALOG_HEADER));
            }
        }
    }

    public boolean isFlightPlanFromCurrentMission(File selectedFp) {
        try {
            return !selectedFp.getParentFile().equals(getDefaultFlightPlansDir());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean alreadyHasSuchFile(File selectedFp) {
        try {
            Path potentialFlightPlanLocation;
            potentialFlightPlanLocation = getDefaultFlightPlansDir().toPath().resolve(selectedFp.getName());
            return Files.exists(potentialFlightPlanLocation);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void openExternalFlightPlanFileMove(File selectedFp) throws IOException, InvalidFlightPlanFileException {
        Path potentialFlightPlanLocation = getDefaultFlightPlansDir().toPath().resolve(selectedFp.getName());
        Files.copy(selectedFp.toPath(), potentialFlightPlanLocation, StandardCopyOption.REPLACE_EXISTING);
        openFlightPlanFile(potentialFlightPlanLocation.toFile());
    }

    public void openFlightPlanFile(File selectedFp) throws InvalidFlightPlanFileException {
        Mission currentMission = applicationContext.getCurrentMission();
        FlightPlan loadedFlightPlan = currentMission.loadFlightPlan(selectedFp.toPath());
        Expect.notNull(loadedFlightPlan, "loadedFlightPlan");
        currentMission.addFlightPlan(loadedFlightPlan, false);
        currentMission.setCurrentFlightPlan(loadedFlightPlan);
    }

    private void openFlightPlan() {
        Path[] selectedFiles =
            dialogService.requestMultiFileOpenDialog(
                StartPlanningViewModel.this,
                languageHelper.getString("com.intel.missioncontrol.ui.SidePaneView.dialog.openFlightPlan.title"),
                getDefaultFlightPlansDir().toPath(),
                FileFilter.FLIGHTPLAN,
                FileFilter.ALL);

        if (selectedFiles == null || selectedFiles.length == 0) {
            return;
        }

        for (Path selectedPath : selectedFiles) {
            File selectedFp = selectedPath.toFile();
            try {
                if (isFlightPlanFromCurrentMission(selectedFp)) {
                    // ask user if he wants to import file from non-mission folder
                    boolean importAnswer =
                        dialogService.requestConfirmation(
                            languageHelper.getString("com.intel.missioncontrol.ui.SidePaneView.dialog.import.title"),
                            languageHelper.getString("com.intel.missioncontrol.ui.SidePaneView.dialog.import.content"));
                    if (importAnswer) {
                        if (alreadyHasSuchFile(selectedFp)) {
                            // ask user if he wants to override existing file
                            boolean overwriteAnswer =
                                dialogService.requestConfirmation(
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.SidePaneView.dialog.override.title"),
                                    languageHelper.getString(
                                        "com.intel.missioncontrol.ui.SidePaneView.dialog.override.content"));
                            if (!overwriteAnswer) {
                                return;
                            }
                        }

                        openExternalFlightPlanFileMove(selectedFp);
                    } else {
                        openFlightPlanFile(selectedFp);
                    }
                } else {
                    openFlightPlanFile(selectedFp);
                }
            } catch (Exception e) {
                logger.error("Error opening file " + selectedFp.getAbsolutePath(), e);
                dialogService.showErrorMessage(
                    languageHelper.getString("com.intel.missioncontrol.ui.SidePaneView.dialog.errorFile.title"),
                    languageHelper.getString("com.intel.missioncontrol.ui.SidePaneView.dialog.errorFile.content"));
            }
        }
    }

    public ReadOnlyBooleanProperty isBusyProperty() {
        return isBusy;
    }

    public ReadOnlyStringProperty missionNameProperty() {
        return missionName;
    }

    public ReadOnlyListProperty<FlightPlanTemplate> availableTemplatesProperty() {
        return availableTemplates;
    }

    public Command getOpenAoiScreenCommand() {
        return openAoiScreenCommand;
    }

    public Command getShowTemplateManagementCommand() {
        return showTemplateManagementCommand;
    }

    public Command getRenameMissionCommand() {
        return renameMissionCommand;
    }

    private void openAoiScreen() {
        isBusy.set(true);
        navigationService.disable();
        Mission currentMission = applicationContext.getCurrentMission();
        String tempName = generateTemporaryName(currentMission);
        Dispatcher.background()
            .getLaterAsync(
                () -> {
                    FlightPlan newFlightPlan = selectedTemplate.get().produceFlightPlan();
                    newFlightPlan.nameProperty().setValue(tempName);
                    newFlightPlan
                        .getLegacyFlightplan()
                        .getPhotoSettings()
                        .setAltitudeAdjustMode(selectedTerrainMode.get());
                    return newFlightPlan;
                })
            .whenSucceeded(
                newFlightPlan -> {
                    currentMission.initNewFlightPlan(newFlightPlan);
                    currentMission.addFlightPlan(newFlightPlan, true);
                    currentMission.setCurrentFlightPlan(newFlightPlan);

                    navigationService.enable();
                    if (newFlightPlan.areasOfInterestProperty().isEmpty() && !newFlightPlan.isTemplate()) {
                        navigationService.navigateTo(SidePanePage.CHOOSE_AOI);
                    } else {
                        navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN);
                    }
                },
                Dispatcher.platform())
            .whenDone(
                future -> {
                    isBusy.set(false);
                    navigationService.enable();
                },
                Dispatcher.platform());
    }

    public ObjectProperty<FlightPlanTemplate> selectedTemplateProperty() {
        return selectedTemplate;
    }

    private String generateTemporaryName(Mission currentMission) {
        long countOfUnnamedFlightPlans =
            currentMission.flightPlansProperty().stream().filter(fp -> !fp.isNameSetProperty().get()).count();
        String suffix = countOfUnnamedFlightPlans > 0 ? String.valueOf(countOfUnnamedFlightPlans + 1L) : "";
        return String.format(
                "%s %s",
                languageHelper.getString("com.intel.missioncontrol.ui.planning.NewFlightPlanViewModel.newFlightPlan"),
                suffix)
            .trim();
    }

    public MenuModel getFlightPlanMenuModel() {
        return mainScope.flightPlanMenuModelProperty().get();
    }

    private void importTemplate(File file) {
        try {
            FlightPlanTemplate templateImported = flightPlanTemplateService.importFrom(file);
            if (templateImported == null) {
                logger.warn(
                    "The flight plan of {} file cannot be saved as template. Save flight plan first, please",
                    file.getAbsolutePath());
            } else {
                availableTemplates.add(templateImported);
            }
        } catch (IOException e) {
            logger.error(String.format("Failure on a flight plan import from %s", file), e);
        }
    }

    private void updateTemplateWith(File file, final String templateName) {
        FlightPlanTemplate template =
            availableTemplates.stream().filter(t -> t.getName().equals(templateName)).findFirst().orElse(null);
        if (template == null) {
            logger.warn("The '{}' flight plan template is not available already.", templateName);
            return;
        }

        try {
            flightPlanTemplateService.updateTemplateWith(template, file);
        } catch (Exception e) {
            logger.error(String.format("Failure on '%s' flight plan template", templateName), e);
            return;
        }

        int index = availableTemplates.indexOf(template);
        availableTemplates.remove(template);
        availableTemplates.add(index, template);
    }

    private void recalculateFlightPlan() {
        mapController.setMouseMode(InputMode.DEFAULT);
        applicationContext.getCurrentMission().recalculateCurrentFlightPlan();
    }

    private void revertFlightPlanChanges() {
        Mission currentMission = applicationContext.getCurrentMission();
        FlightPlan currentFlightPlan = planningScope.getCurrentFlightplan();

        currentMission.flightPlansProperty().remove(currentFlightPlan);
        currentMission.closeFlightPlan(currentFlightPlan);

        FlightPlan newCurrentFlightPlan;

        File legacyFlightPlanFile = currentFlightPlan.getLegacyFlightplan().getFile();
        if (!legacyFlightPlanFile.exists()) {
            newCurrentFlightPlan = planningScope.selectedTemplateProperty().get().produceFlightPlan();
        } else {
            try {
                newCurrentFlightPlan = currentMission.loadFlightPlan(legacyFlightPlanFile.toPath());
            } catch (InvalidFlightPlanFileException e) {
                logger.warn("cant revert flight plan changes " + legacyFlightPlanFile.toPath(), e);
                return;
            }
        }

        Expect.notNull(newCurrentFlightPlan, "newCurrentFlightPlan");
        currentMission.addFlightPlan(newCurrentFlightPlan, false);
        currentMission.setCurrentFlightPlan(newCurrentFlightPlan);

        planningScope.publish(PlanningScope.EVENT_ON_FLIGHT_PLAN_REVERT_CHANGES, null);
    }

    private void saveAsFlightPlanChooser() {
        String title = languageHelper.getString(SAVE_AS_FLIGHT_PLAN_DIALOG_TITLE);
        Path selectedFile =
            dialogService.requestFileSaveDialog(
                StartPlanningViewModel.this,
                title,
                getDefaultFlightPlansDir().toPath(),
                FileFilter.FLIGHTPLAN,
                FileFilter.ALL);

        if (selectedFile != null) {
            FlightPlan currentFlightPlan = planningScope.getCurrentFlightplan();
            cloneFlightPlan(currentFlightPlan, selectedFile.toAbsolutePath().toString());
        }
    }

    private void saveFlightPlan() {
        FlightPlan currentFlightPlan = planningScope.getCurrentFlightplan();

        if (!applicationContext.currentMissionProperty().isNotNull().get() && currentFlightPlan.canBeSaved()) {
            return;
        }

        flightPlanService.saveFlightPlan(applicationContext.getCurrentMission(), planningScope.getCurrentFlightplan());
        planningScope.publishFlightplanSave();
        Optional.ofNullable(applicationContext.getCurrentMission())
            .ifPresent(mission -> mission.setMissionEmpty(false));
    }

    private File getDefaultFlightPlansDir() {
        Mission currentMission = applicationContext.getCurrentMission();
        return MissionConstants.getFlightplanFolder(currentMission.getDirectory());
    }

    private void focusFlightPlanOnMap(FlightPlan flightPlan) {
        if (flightPlan.getCenter() != null) {
            mapView.goToSectorAsync(flightPlan.getSector(), flightPlan.getMaxElev());
        }
    }

    private void cloneFlightPlan(FlightPlan flightPlan) {
        String newName =
            languageHelper.getString("com.intel.missioncontrol.api.flightplan.clone") + flightPlan.getName();
        cloneFlightPlan(flightPlan, getDefaultFlightPlansDir() + File.separator + newName);
    }

    private void cloneFlightPlan(FlightPlan flightPlan, String fpFullPath) {
        final Mission mission = applicationContext.getCurrentMission();

        String clonedPath = flightPlanService.cloneFlightPlanLocally(flightPlan.getLegacyFlightplan(), fpFullPath);
        if (null == clonedPath) {
            return;
        }

        FlightPlan clonedFlightPlan;
        try {
            clonedFlightPlan = applicationContext.getCurrentMission().loadFlightPlan(Paths.get(clonedPath + FML));
        } catch (Exception e) {
            logger.warn("cant clone flight plan " + fpFullPath, e);
            return;
        }

        Expect.notNull(clonedFlightPlan, "clonedFlightPlan");
        File f = new File(clonedPath);
        clonedFlightPlan.rename(f.getName());
        clonedFlightPlan.save();

        navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN);

        ReadOnlyListProperty<FlightPlan> flightPlans = mission.flightPlansProperty();
        flightPlans.add(clonedFlightPlan);

        Expect.notNull(clonedFlightPlan, "clonedFlightPlan");
        mission.setCurrentFlightPlan(clonedFlightPlan);

        focusFlightPlanOnMap(clonedFlightPlan);
        if (clonedFlightPlan.areasOfInterestProperty().isEmpty() && !clonedFlightPlan.isTemplate()) {
            navigationService.navigateTo(SidePanePage.CHOOSE_AOI);
        } else {
            Optional.ofNullable(navigationService.getWorkflowStep())
                .filter(wfs -> wfs != WorkflowStep.FLIGHT)
                .ifPresent(wfs -> navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN));
        }

        currentFlightPlans.bindContent(mission.flightPlansProperty());
    }

    public Property<AltitudeAdjustModes> selectedTerrainModeProperty() {
        return selectedTerrainMode;
    }

    public void navigateToElevationSettings() {
        navigationService.navigateTo(SettingsPage.AIRSPACES_PROVIDERS);
    }

}
