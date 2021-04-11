package com.intel.missioncontrol.ui.sidepane.start;

import com.google.inject.Inject;
import com.intel.insight.api.SearchService;
import com.intel.insight.datastructures.Annotation;
import com.intel.insight.datastructures.FlightRequest;
import com.intel.insight.datastructures.Project;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.api.IFlightPlanTemplateService;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.connection.ConnectionState;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.mission.MissionInfo;
import com.intel.missioncontrol.mission.MissionInfoManager;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.menu.MainMenuModel;
import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.AnalysisSettings;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.AsyncCommand;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.CommandContext;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedAsyncCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedFutureCommand;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.profiling.MRequest;
import eu.mavinci.desktop.main.debug.profiling.requests.OpenMissionRequest;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.PicAreaCorners;
import eu.mavinci.flightplan.Point;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectsViewModel extends ViewModelBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsViewModel.class);

    private static final String YEAR_MONTH_DAY_DATE_FORMAT = "yyyy-MM-dd";
    private static final String SR_NO_PROJECT_FOLDER = "noProjectFolder";

    private final UIAsyncListProperty<ViewModel> items =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<ViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final ObjectProperty<ProjectItemViewModel> selectedItem = new SimpleObjectProperty<>();

    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final IMissionManager missionManager;
    private final IDialogService dialogService;
    private final AsyncCommand refreshProjectListCommand;
    private final AsyncCommand createProjectCommand;
    private final AsyncCommand cloneCurrentProjectCommand;
    private final ParameterizedAsyncCommand<MissionInfo> openProjectCommand;
    private final ParameterizedAsyncCommand<MissionInfo> openSelectedProjectCommand;
    private final ParameterizedAsyncCommand<MissionInfo> cloneSelectedProjectCommand;
    private final Command openProjectFromDiskCommand;
    private final IProfilingManager profilingManager;
    private final ISelectionManager selectionManager;
    private final ISettingsManager settingsManager;
    private final IMapView mapView;
    private final ILanguageHelper languageHelper;
    private final IPathProvider pathProvider;
    private final Mission.Factory missionFactory;
    private final MissionInfoManager missionInfoManager;
    private final IFlightPlanTemplateService flightPlanTemplateService;
    private final IDroneConnectionService droneConnectionService;

    private final AsyncObjectProperty<FlightSegment> flightSegment =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<FlightSegment>().initialValue(FlightSegment.UNKNOWN).create());

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private FlightScope flightScope;

    private final ObjectProperty<FlightScope> flightScopeProperty = new SimpleObjectProperty<>();

    @SuppressWarnings("FieldCanBeLocal")
    private ChangeListener<ProjectItemViewModel> selectedItemChangedListener;

    @Inject
    public ProjectsViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            IMissionManager missionManager,
            IDialogService dialogService,
            Mission.Factory missionFactory,
            MissionInfoManager missionInfoManager,
            IProfilingManager profilingManager,
            IMapView mapView,
            ISelectionManager selectionManager,
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            IPathProvider pathProvider,
            IFlightPlanTemplateService flightPlanTemplateService,
            IDroneConnectionService droneConnectionService) {
        this.mapView = mapView;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.missionManager = missionManager;
        this.dialogService = dialogService;
        this.profilingManager = profilingManager;
        this.selectionManager = selectionManager;
        this.settingsManager = settingsManager;
        this.languageHelper = languageHelper;
        this.pathProvider = pathProvider;
        this.missionFactory = missionFactory;
        this.missionInfoManager = missionInfoManager;
        this.flightPlanTemplateService = flightPlanTemplateService;
        this.droneConnectionService = droneConnectionService;

        CommandContext commandContext = new CommandContext();

        refreshProjectListCommand = new FutureCommand(this::refreshProjectListAsync, commandContext);
        openProjectFromDiskCommand = new DelegateCommand(this::openFromDiskAsync, commandContext);
        createProjectCommand =
            new FutureCommand(
                this::createMissionAsync,
                droneConnectionService
                    .connectionStateProperty()
                    .isEqualTo(ConnectionState.NOT_CONNECTED)
                    .or(flightSegment.isEqualTo(FlightSegment.ON_GROUND)),
                commandContext);

        final ObjectBinding<MissionInfo> selectedMissionInfo =
            Bindings.createObjectBinding(
                () -> selectedItem.get() == null ? null : selectedItem.get().getMissionInfo(), selectedItem);

        openSelectedProjectCommand =
            new ParameterizedFutureCommand<>(this::openMissionAsync, selectedItem.isNotNull(), commandContext);
        openSelectedProjectCommand.parameterProperty().bind(selectedMissionInfo);

        openProjectCommand = new ParameterizedFutureCommand<>(this::openMissionAsync, commandContext);

        cloneSelectedProjectCommand =
            new ParameterizedFutureCommand<>(this::cloneMissionAsync, selectedItem.isNotNull(), commandContext);
        cloneSelectedProjectCommand.parameterProperty().bind(selectedMissionInfo);

        cloneCurrentProjectCommand =
            new FutureCommand(
                this::cloneCurrentMissionAsync,
                droneConnectionService
                    .connectionStateProperty()
                    .isEqualTo(ConnectionState.NOT_CONNECTED)
                    .or(flightSegment.isEqualTo(FlightSegment.ON_GROUND))
                    .and(applicationContext.currentMissionProperty().isNotNull().or(selectedItem.isNotNull())),
                commandContext);

        // TODO! - add sorting and dates
        // items.bindContent(missionManager.recentMissionInfosProperty(), (value -> new MissionItemViewModel(value)));
    }

    private Future<Void> refreshProjectListAsync() {
        AnalysisSettings analysisSettings = settingsManager.getSection(AnalysisSettings.class);

        String userName = analysisSettings.getInsightUsername();
        String password = analysisSettings.getInsightPassword();

        return Dispatcher.background()
            .runLaterAsync(
                () -> {
                    if (!analysisSettings.getInsightLoggedIn()) {
                        navigationService.navigateTo(SettingsPage.INSIGHT);
                        return;
                    }

                    if (applicationContext.currentMissionProperty().get() != null) {
                        // if unloading is cancelled by user input, we also abort creating a new mission
                        if (!applicationContext.unloadCurrentMission()) {
                            return;
                        }
                    }

                    var ss = new SearchService(userName, password);
                    var orderedMissions = ss.getAllOrderedMissions();
                    var projects = ss.getProjects();

                    for (var orderedMission : orderedMissions) {
                        for (var annotation : orderedMission.component1().getAnnotations()) {
                            // these are the AOIs for which the user has issued the "order me" command from within
                            // insight
                            var wasOrdered = false;
                            Project orderedProject = null;
                            for (var project : projects.getProjects()) {
                                System.out.println(
                                    project.getId()
                                        + "\t"
                                        + annotation.getProjectId()
                                        + "\t"
                                        + orderedMission.component2().getAnnotationId());
                                if (project.getId() != null
                                        && project.getId().equals(annotation.getProjectId())
                                        && annotation.getId().equals(orderedMission.component2().getAnnotationId())) {
                                    orderedProject = project;
                                    wasOrdered = true;
                                }
                            }

                            if (wasOrdered) {
                                createMissionFromInsight(annotation, orderedProject, orderedMission.component2());
                            }
                        }
                    }
                })
            .whenFailed(
                ex ->
                    applicationContext.addToast(
                        Toast.of(ToastType.ALERT).setText(ex.getCause().getMessage()).create()));
    }

    private void createMissionFromInsight(Annotation annotation, Project orderedProject, FlightRequest flightRequest) {
        try {
            long createdTimestamp = 0;
            Date date = null;
            try {
                date =
                    (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
                        .parse(flightRequest.getCreated().replaceAll("Z$", "+0000"));
                createdTimestamp = date.getTime();
            } catch (ParseException e) {
                LOGGER.error("Can't parse creation timestamp.", e);
            }

            if (settingsManager.getSection(AnalysisSettings.class).getInsightLatestDataOrderDownload().longValue()
                    >= createdTimestamp) {
                return;
            }

            String name =
                orderedProject == null
                    ? languageHelper.getString(
                        ProjectsViewModel.class, "insightNewMission.noName", flightRequest.getCreated())
                    : orderedProject.getName();
            name = name.replaceAll("[\\\\/:*?\"<>|]", "_");

            String fpName = flightRequest.getCreated();
            fpName = fpName.replaceAll("[\\\\/:*?\"<>|]", "_");
            fpName =
                flightRequest.getType().toLowerCase()
                    + "_"
                    + fpName.substring(0, fpName.length() - 5)
                        .replaceAll(Pattern.quote("T"), Matcher.quoteReplacement("_"));
            // search in existing missions:

            MissionInfo mi = missionManager.getByRemoteId(orderedProject.getId());
            Path projectFolder;
            Sector oldSector = null;
            if (mi != null) {
                name = mi.getName();
                projectFolder = pathProvider.getProjectsDirectory().resolve(name);
                oldSector = mi.getSector();

                if (date != null && mi.getLastModified().before(date)) {
                    mi.setLastModified(date);
                }
            } else {
                // dont overwrite existing downloads
                File freeFolder =
                    FileHelper.getNextFreeFilename(pathProvider.getProjectsDirectory().resolve(name).toFile());
                name = freeFolder.getName();
                projectFolder = pathProvider.getProjectsDirectory().resolve(name);
                mi = new MissionInfo(projectFolder, orderedProject.getId());
                if (date != null) {
                    mi.setLastModified(date);
                }
            }

            Flightplan fp =
                flightPlanTemplateService.getFlightPlanTemplates().get(0).produceFlightPlan().getLegacyFlightplan();
            fp.setMuteAutoRecalc(true);
            fp.setName(fpName);
            fp.setId(orderedProject.getId());
            fp.setNotes(flightRequest.getNotes());

            var features = annotation.getFeature().getType();
            var gsd = Double.parseDouble(flightRequest.getRatio()) / 100.; // cm -> meter
            var coords = annotation.getFeature().getGeometry().getCoordinates();

            int i = 0;
            for (var polygone : coords) {
                i++;
                PicArea picArea = new PicArea(fp);
                picArea.setDefaultsFromMasterPicArea(
                    fp.getPicAreaTemplate(
                        flightRequest.getType().equalsIgnoreCase("INSPECTION") ? PlanType.BUILDING : PlanType.POLYGON));
                picArea.setName(
                    languageHelper.getString(AreaOfInterest.AOI_PREFIX + picArea.getPlanType().toString()) + " " + i);
                picArea.setGsd(gsd);
                PicAreaCorners corners = picArea.getCorners();
                fp.addToFlightplanContainer(picArea);
                // picArea.setYaw(309); // hardcoded for now. TODO run optimization for shortest path automatically...
                // this is for the
                // Sagi demo in tanklager at 27th March 2019

                // GeoJSON polygone have first and last point identical and twice in each list --> IMC dont like that,
                // so lets drop last point
                for (var corner : polygone.subList(0, polygone.size() - 1)) {
                    Point p = new Point(corners);
                    p.setLatLon(corner.get(1), corner.get(0));
                    corners.addToFlightplanContainer(p);
                }
            }

            fp.setTakeoffPosition(fp.getSector().getCentroid());
            if (!fp.setMuteAutoRecalc(false)) {
                // in case template had no auto compute enabled, do it explicit here
                fp.doFlightplanCalculation();
            }

            if (fp.getPhotoSettings().getAltitudeAdjustMode().usesAbsoluteHeights()) {
                Thread.sleep(100);
                fp.doFlightplanCalculation(); // compute ONCE again, just in case terrain wasnt loaded jet
            }

            File flightPlansFolder = MissionConstants.getFlightplanFolder(projectFolder);
            flightPlansFolder.mkdirs();
            fp.saveToLocation(new File(flightPlansFolder, fp.getName() + ".fml"));
            String fpRef = FileHelper.makeRelativePathSysIndep(flightPlansFolder, fp.getResourceFile());
            if (!mi.getLoadedFlightPlans().contains(fpRef)) {
                mi.getLoadedFlightPlans().add(0, fpRef);
            }

            Sector wholeSector = fp.getSector();
            if (oldSector != null) {
                wholeSector = Sector.union(wholeSector, oldSector);
            }

            mi.setSector(wholeSector);

            missionInfoManager.saveToFile(mi);
            // this thread has to wait until the refresh is done, otherwise
            // multiple orders from the same proejcts would get splitted up
            Dispatcher.platform().runLaterAsync(this::refreshRecentMissionsList).getUnchecked();

            Toast toast =
                Toast.of(ToastType.INFO)
                    .setText(languageHelper.getString(ProjectsViewModel.class, "insightNewMissionDownloaded", name))
                    .setCommand(
                        languageHelper.getString(ProjectsViewModel.class, "insightNewMissionDownloaded.open"),
                        false,
                        true,
                        openProjectCommand,
                        mi)
                    .setCloseable(true)
                    .setTimeout(Toast.LONG_TIMEOUT)
                    .create();

            applicationContext.addToast(toast);

            settingsManager
                .getSection(AnalysisSettings.class)
                .insightLatestDataOrderDownloadProperty()
                .set(createdTimestamp);

        } catch (Exception e) {
            LOGGER.error(
                "cant download insight order:" + orderedProject + "  =>" + annotation + "  =>" + flightRequest, e);
        }
    }

    private void selectionChange(Object userData) {
        if (!(userData instanceof MissionInfo)) {
            return;
        }

        MissionInfo missionInfo = (MissionInfo)userData;

        try (LockedList<ViewModel> lockedList = items.lock()) {
            for (ViewModel item : lockedList) {
                if (item instanceof ProjectItemViewModel) {
                    MissionInfo otherMissionInfo = ((ProjectItemViewModel)item).getMissionInfo();
                    if (missionInfo.getFolder().equals(otherMissionInfo.getFolder())
                            && missionInfo.getName().equals(otherMissionInfo.getName())) {
                        selectedItem.set((ProjectItemViewModel)item);
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        refreshRecentMissionsList();
        navigationService.sidePanePageProperty().addListener(this::handleSidePanePageChanged);

        // Register handlers for the main menu
        MenuModel menuModel = mainScope.mainMenuModelProperty().get();
        menuModel.find(MainMenuModel.Project.NEW).setCommandHandler(createProjectCommand);
        menuModel.find(MainMenuModel.Project.CLONE).setCommandHandler(cloneCurrentProjectCommand);

        flightScopeProperty.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    flightScopeProperty
                        .get()
                        .currentDroneProperty()
                        .addListener(
                            (observableDrone, oldValueDrone, newValueDrone) -> {
                                if (newValueDrone != null) {
                                    flightSegment.bind(
                                        flightScopeProperty.get().currentDroneProperty().get().flightSegmentProperty());
                                } else {
                                    flightSegment.unbind();
                                    flightSegment.set(FlightSegment.ON_GROUND);
                                }
                            });
                }

                if (newValue != null && newValue.currentDroneProperty().get() != null) {
                    flightSegment.bind(flightScopeProperty.get().currentDroneProperty().get().flightSegmentProperty());
                } else {
                    flightSegment.unbind();
                    flightSegment.set(FlightSegment.ON_GROUND);
                }
            });
        flightScopeProperty.set(flightScope);
        selectionManager
            .currentSelectionProperty()
            .addListener((observable, oldValue, newValue) -> selectionChange(newValue), Dispatcher.platform()::run);

        selectedItem.addListener(
            new WeakChangeListener<>(
                selectedItemChangedListener =
                    (observable, oldValue, newValue) -> {
                        if (newValue == null) {
                            return;
                        }

                        MissionInfo missionInfo = newValue.getMissionInfo();
                        if (missionInfo == null) {
                            return;
                        }

                        selectionManager.setSelection(missionInfo);
                        mapView.goToSectorAsync(missionInfo.getSector(), OptionalDouble.empty());
                    }));

        missionManager.refreshRecentMissionListItems();
        items.bind(missionManager.recentMissionListItems());
    }

    public ReadOnlyListProperty<ViewModel> itemsProperty() {
        return items.getReadOnlyProperty();
    }

    public ObjectProperty<ProjectItemViewModel> selectedItemProperty() {
        return selectedItem;
    }

    public AsyncCommand getRefreshProjectListCommand() {
        return refreshProjectListCommand;
    }

    public AsyncCommand getCreateProjectCommand() {
        return createProjectCommand;
    }

    public AsyncCommand getOpenSelectedProjectCommand() {
        return openSelectedProjectCommand;
    }

    public AsyncCommand getCloneSelectedProjectCommand() {
        return cloneSelectedProjectCommand;
    }

    public Command getOpenProjectFromDiskCommand() {
        return openProjectFromDiskCommand;
    }

    private void handleSidePanePageChanged(
            ObservableValue<? extends SidePanePage> observable, SidePanePage oldValue, SidePanePage newValue) {
        if (newValue == SidePanePage.RECENT_MISSIONS) {
            refreshRecentMissionsList();
        }
    }

    private void refreshRecentMissionsList() {
        missionManager.refreshRecentMissionInfos();
        missionManager.refreshRecentMissionListItems();
    }

    private Future<Void> createMissionAsync() {
        if (applicationContext.currentMissionProperty().get() != null) {
            // if unloading is cancelled by user input, we also abort creating a new mission
            if (!applicationContext.unloadCurrentMission()) {
                return Futures.cancelled();
            }
        }

        return applicationContext
            .loadNewMissionAsync()
            .whenSucceeded(future -> navigationService.navigateTo(SidePanePage.START_PLANNING));
    }

    private Future<Void> openMissionAsync(MissionInfo missionInfo) {
        return openOrCloneMissionAsync(missionFactory.create(missionInfo), false);
    }

    private Future<Void> cloneMissionAsync(MissionInfo missionInfo) {
        return openOrCloneMissionAsync(missionFactory.create(missionInfo), true);
    }

    private Future<Void> cloneCurrentMissionAsync() {
        if (applicationContext.getCurrentMission() != null)
            return openOrCloneMissionAsync(applicationContext.getCurrentMission(), true);
        else {
            return cloneMissionAsync(selectedItem.get().getMissionInfo());
        }
    }

    private Future<Void> openOrCloneMissionAsync(Mission mission, boolean clone) {
        MRequest openMissionRequest = new OpenMissionRequest(mission);
        profilingManager.requestStarting(openMissionRequest);
        return (clone
                ? applicationContext.loadClonedMissionAsync(mission)
                : applicationContext.loadMissionAsync(mission))
            .whenDone(future -> profilingManager.requestFinished(openMissionRequest), Dispatcher.platform()::run)
            .whenSucceeded(
                future -> {
                    SidePanePage newPage =
                        mission.flightPlansProperty().isEmpty()
                            ? SidePanePage.START_PLANNING
                            : SidePanePage.EDIT_FLIGHTPLAN;
                    if (mission.flightPlansProperty().isEmpty()
                            && !mission.matchingsProperty().isEmpty()
                            && (mission.matchingsProperty().get(0).getStatus() != MatchingStatus.NEW
                                || mission.getMatchings().size() > 1)) {
                        navigationService.navigateTo(WorkflowStep.DATA_PREVIEW);
                    } else {
                        navigationService.navigateTo(newPage);
                        missionManager.refreshRecentMissionInfos();
                        missionManager.refreshRecentMissionInfos();
                    }
                },
                Dispatcher.platform()::run);
    }

    private Future<Void> openFromDiskAsync() {
        Path projectsDirectory = settingsManager.getSection(PathSettings.class).getProjectFolder();
        Path missionFolder = dialogService.requestDirectoryChooser(ProjectsViewModel.this, null, projectsDirectory);
        if (missionFolder == null) {
            return Futures.cancelled();
        }

        if (!missionManager.isMissionFolder(missionFolder.toFile())) {
            applicationContext.addToast(
                Toast.of(ToastType.INFO)
                    .setText(languageHelper.getString(ProjectsViewModel.class, SR_NO_PROJECT_FOLDER))
                    .create());
            return Futures.cancelled();
        }

        return openOrCloneMissionAsync(missionManager.openMission(missionFolder), false);
    }

}
