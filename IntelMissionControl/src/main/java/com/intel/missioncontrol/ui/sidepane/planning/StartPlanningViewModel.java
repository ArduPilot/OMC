/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.api.IFlightPlanTemplateService;
import com.intel.missioncontrol.flightplanning.IFlightPlanningService;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.geo.Convert;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.helper.SystemInformation;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.project.FlightPlan;
import com.intel.missioncontrol.project.Mission;
import com.intel.missioncontrol.project.Project;
import com.intel.missioncontrol.project.hardware.AirplaneType;
import com.intel.missioncontrol.project.hardware.HardwareConfiguration;
import com.intel.missioncontrol.project.hardware.IDescriptionProvider;
import com.intel.missioncontrol.project.hardware.PlatformDescription;
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
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.flightplan.AirmapLaancService;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.desktop.gui.doublepanel.planemain.FlightplanExportTypes;
import eu.mavinci.desktop.helper.FileFilter;
import eu.mavinci.flightplan.exporter.IFlightplanExporter;
import eu.mavinci.flightplan.exporter.IFlightplanExporterFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncStringProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
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
    private final IFlightPlanningService flightPlanningService;
    private final INavigationService navigationService;
    private final ILanguageHelper languageHelper;
    private final IDescriptionProvider descriptionProvider;
    private final IDialogService dialogService;
    private final IFlightPlanTemplateService flightPlanTemplateService;
    private final NotificationCenter notificationCenter;
    private final MavinciObjectFactory mavinciObjectFactory;
    private final ILicenceManager licenceManager;
    private final IFlightplanExporterFactory flightplanExporterFactory;
    private final IElevationModel elevationModel;
    private final AirmapLaancService airmapLaancService;
    private final AsyncStringProperty missionName;

    private final GeneralSettings generalSettings;
    private final IVeryUglyDialogHelper dialogHelper;
    private final IMapView mapView;
    private final IMapController mapController;
    private BooleanProperty flightPlanSaveable = new SimpleBooleanProperty(true);
    private ObjectProperty<FlightPlanTemplate> selectedTemplate = new SimpleObjectProperty<>();
    private ListProperty<FlightPlanTemplate> availableTemplates =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final Command openAoiScreenCommand;
    private final Command showTemplateManagementCommand;
    private final Command renameMissionCommand;

    @Inject
    public StartPlanningViewModel(
            IApplicationContext applicationContext,
            IFlightPlanningService flightPlanningService,
            IValidationService validationService,
            INavigationService navigationService,
            ILanguageHelper languageHelper,
            IDescriptionProvider descriptionProvider,
            IDialogService dialogService,
            IFlightPlanTemplateService flightPlanTemplateService,
            NotificationCenter notificationCenter,
            MavinciObjectFactory mavinciObjectFactory,
            ISettingsManager settingsManager,
            IVeryUglyDialogHelper dialogHelper,
            IMapView mapView,
            IMapController mapController,
            IFlightplanExporterFactory flightplanExporterFactory,
            IElevationModel elevationModel,
            ILicenceManager licenceManager,
            AirmapLaancService airmapLaancService) {
        this.mapView = mapView;
        this.mapController = mapController;
        this.applicationContext = applicationContext;
        this.flightPlanningService = flightPlanningService;
        this.validationService = validationService;
        this.navigationService = navigationService;
        this.languageHelper = languageHelper;
        this.descriptionProvider = descriptionProvider;
        this.dialogService = dialogService;
        this.flightPlanTemplateService = flightPlanTemplateService;
        this.notificationCenter = notificationCenter;
        this.mavinciObjectFactory = mavinciObjectFactory;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.dialogHelper = dialogHelper;
        this.flightplanExporterFactory = flightplanExporterFactory;
        this.elevationModel = elevationModel;
        this.licenceManager = licenceManager;
        this.airmapLaancService = airmapLaancService;
        this.missionName =
            PropertyPath.from(applicationContext.currentProjectProperty()).selectAsyncString(Project::nameProperty);

        openAoiScreenCommand = new FutureCommand(this::openAoiScreen);

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

        MenuModel menuModel = mainScope.mainMenuModelProperty().get();
        menuModel
            .find(MainMenuModel.FlightPlan.CLONE)
            .setActionHandler(
                () -> {
                    Mission mission = applicationContext.currentMissionProperty().get();
                    // TODO ???
                    cloneMission(mission);
                },
                flightPlanSaveable.and(applicationContext.currentMissionIsNoDemo()));

//        menuModel
//            .find(MainMenuModel.FlightPlan.SAVE)
//            .setActionHandler(applicationContext::synchronizeCurrentProject, flightPlanSaveable);

        // NOT SUPPORTED
        /*      menuModel
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
         */
        menuModel
            .find(MainMenuModel.FlightPlan.RECALCULATE)
            .setActionHandler(this::recalculateFlightPlan, applicationContext.currentMissionProperty().isNotNull());

        menuModel
            .find(MainMenuModel.FlightPlan.REVERT_CHANGES)
            .setActionHandler(this::revertFlightPlanChanges, planningScope.currentFlightplanProperty().isNotNull());

        menuModel
            .find(MainMenuModel.FlightPlan.EXPORT)
            .setActionHandler(
                this::exportFlightPlan,
                applicationContext
                    .currentFlightPlanProperty()
                    .isNotNull()
                    .and(validationService.canExportFlightProperty()));
        menuModel
            .find(MainMenuModel.FlightPlan.AIRMAP_LAANC)
            .setActionHandler(
                this::airmapLaancApprove,
                applicationContext
                    .currentFlightPlanProperty()
                    .isNotNull()
                    .and(validationService.canExportFlightProperty()));
        // NOT SUPPORTED
        /*    menuModel
                    .find(MainMenuModel.FlightPlan.SAVE_AS_TEMPLATE)
                    .setActionHandler(this::saveFlightPlanAsTemplate, flightPlanSaveable);
        */

        // NOT SUPPORTED
        /*      menuModel
                 .find(MainMenuModel.FlightPlan.UPDATE_PARENT_TEMPLATE)
                 .setActionHandler(this::updateParentTemplate, planningScope.currentFlightplanProperty().isNotNull());
             menuModel
                 .find(MainMenuModel.FlightPlan.UPDATE_PARENT_TEMPLATE)
                 .visibleProperty()
                 .bind(generalSettings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG));

        */
        menuModel
            .find(MainMenuModel.FlightPlan.SAVE_AS_TEMPLATE)
            .visibleProperty()
            .bind(generalSettings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
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
    }

    // NOT SUPPORTED
    /* private void updateParentTemplate() {
            if (!dialogService.requestConfirmation(
                    languageHelper.getString(StartPlanningViewModel.class.getName() + ".updateHardwarePresetsTitle"),
                    languageHelper.getString(StartPlanningViewModel.class.getName() + ".updateHardwarePresetsText"))) {
                return;
            }

            Flightplan legacyFlightPlan =
                Optional.ofNullable(applicationContext.getCurrentProject())
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
                Optional.ofNullable(applicationContext.getCurrentProject())
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
    */
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

    private File getInitialDirectory(AirplaneType airplaneType) {
        String lastExportFolder = generalSettings.getLastFlightPlanExportFolder();
        if (lastExportFolder != null && !lastExportFolder.isEmpty()) {
            File folderFile = new File(lastExportFolder);
            if (folderFile.exists()) {
                return folderFile;
            }
        }

        switch (airplaneType) {
        case FALCON8PLUS:
        case SIRIUS_BASIC:
        case FALCON8:
            return new File(getRootPath());
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
        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission != null && currentMission.getSector() != null) {
            AirmapLaancService.LaancApprovalQr qrCode =
                airmapLaancService.airmapLaancApprove(currentMission, elevationModel);
            LaancAirmapDialogViewModel vm =
                dialogService.requestDialogAndWait(this, LaancAirmapDialogViewModel.class, () -> qrCode);
        }
    }

    private void exportFlightPlan() {
        UnresolvedWarningsDialogViewModel vm =
            dialogService.requestDialogAndWait(this, UnresolvedWarningsDialogViewModel.class);
        if (!vm.getDialogResult()) {
            return;
        }

        FlightPlan currentFlightplan = applicationContext.getCurrentFlightPlan();
        HardwareConfiguration hwConfig = currentFlightplan.getHardwareConfiguration();
        PlatformDescription description = descriptionProvider.getPlatformDescriptionById(hwConfig.getDescriptionId());
        if (currentFlightplan != null) {
            File initialFolder = getInitialDirectory(description.getAirplaneType());
            Path path =
                dialogService.requestFileOpenDialog(
                    this,
                    languageHelper.getString(StartPlanningViewModel.class, "export"),
                    initialFolder.toPath(),
                    FileFilter.ACP,
                    FileFilter.KML,
                    FileFilter.CSV);

            if (path != null) {
                FlightplanExportTypes type = FlightplanExportTypes.fromDescription(hwConfig.getDescriptionId());
                IFlightplanExporter exporter = flightplanExporterFactory.createExporter(type);
                saveInitialDirectory(path.toFile());
                ProgressTask progressMonitor =
                    new ProgressTask(languageHelper.getString(EXPORT_DIALOG_TITLE), dialogService, 0) {
                        @Override
                        protected Void call() throws Exception {
                            exporter.export(currentFlightplan, path.toFile(), this);
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

    // NOT SUPPORTED
    /*    public void openExternalFlightPlanFileMove(File selectedFp) throws IOException, InvalidFlightPlanFileException {
        Path potentialFlightPlanLocation = getDefaultFlightPlansDir().toPath().resolve(selectedFp.getName());
        Files.copy(selectedFp.toPath(), potentialFlightPlanLocation, StandardCopyOption.REPLACE_EXISTING);
        openFlightPlanFile(potentialFlightPlanLocation.toFile());
    }*/
    // NOT SUPPORTED
    /*
        public void openFlightPlanFile(File selectedFp) throws InvalidFlightPlanFileException {
            Project currentMission = applicationContext.getCurrentProject();
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
    */

    public ReadOnlyAsyncStringProperty missionNameProperty() {
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

    private Future<Void> openAoiScreen() {
        return Dispatcher.background()
            .getLaterAsync(
                () -> {
                    Mission mission = new Mission();
                    mission.nameProperty().setValue("TODO default name");
                    // TODO use hardware configuration from the selected template
                    return mission;
                })
            .whenSucceeded(
                mission -> {
                    //noinspection ConstantConditions
                    applicationContext.getCurrentProject().missionsProperty().add(mission);
                    applicationContext.currentMissionProperty().set(mission);
                },
                Dispatcher.platform())
            .cast();
    }

    public ObjectProperty<FlightPlanTemplate> selectedTemplateProperty() {
        return selectedTemplate;
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
        flightPlanningService.calculateFlightPlan();
    }

    private void revertFlightPlanChanges() {
        applicationContext.revertProjectChange();
        planningScope.publish(PlanningScope.EVENT_ON_FLIGHT_PLAN_REVERT_CHANGES, null);
    }

    private void saveFlightPlan() {
//        applicationContext.synchronizeCurrentProject();
//        planningScope.publishFlightplanSave();
    }

    private void focusFlightPlanOnMap(Mission mission) {
        if (mission.getOrigin() != null) {
            mapView.goToSectorAsync(Convert.toWWSector(mission.getSector()), mission.getMaxElev());
        }
    }

    private void cloneMission(Mission mission) {
        Mission clonedMission = new Mission(mission);

        applicationContext.currentProjectProperty().get().missionsProperty().add(clonedMission);
        applicationContext.currentMissionProperty().set(clonedMission);

        focusFlightPlanOnMap(clonedMission);
    }

    public void navigateToElevationSettings() {
        navigationService.navigateTo(SettingsPage.AIRSPACES_PROVIDERS);
    }

}
