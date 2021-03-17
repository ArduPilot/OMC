/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.Localizable;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.DriveInformation;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionConstants;
import com.intel.missioncontrol.settings.DisplaySettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.menu.MainMenuModel;
import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.CopyLogsTask;
import com.intel.missioncontrol.ui.sidepane.analysis.tasks.CreateDatasetTask;
import com.intel.missioncontrol.ui.validation.IValidationService;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import com.intel.missioncontrol.ui.validation.matching.AvailableSpaceToCopyValidator;
import com.intel.missioncontrol.ui.validation.matching.LogsAndImagesMatchesValidator;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.desktop.helper.FileFilter;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import org.apache.commons.lang3.StringUtils;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Strand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataImportViewModel extends ViewModelBase {

    private enum AddFlightLogsMenuIds implements Localizable {
        CAPTION,
        DRIVES_LIST,
        CONNECTED_UAV,
        PROJECT_FOLDER,
        CUSTOM_LOCATION
    }

    public static final String NO_PHOTO_TITLE = "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.noPhoto.title";
    public static final String NO_PHOTO_MESSAGE =
        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.noPhoto.message";
    public static final String NO_PHOTO_FOLDER_MESSAGE =
        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.noPhotoFolder.message";
    public static final String PHOTO_FOLDER_MESSAGE =
        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.imagesCountLabel";

    public static final String NO_LOGFILES_TITLE =
        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.noLogs.title";
    public static final String NO_LOGFILES_MESSAGE =
        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.noLogs.message";
    public static final String CANT_DETECT_HW_TITLE =
        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.noHW.title";
    public static final String CANT_DETECT_HW_MESSAGE =
        "com.intel.missioncontrol.ui.analysis.AnalysisCreateView.noHW.message";
    private static final Logger LOGGER = LoggerFactory.getLogger(DataImportViewModel.class);

    // private static String LINKBOX = "\\\\192.168.200.254\\IMC";
    private static String LINKBOX = "\\\\launchbox.internal\\imc";

    @InjectScope
    private MainScope mainScope;

    private final ListProperty<FlightLogEntry> flightLogs =
        new SimpleListProperty<>(FXCollections.observableArrayList(a -> new Observable[] {a.selectedProperty()}));

    private final BooleanProperty logFolderContainsImages = new SimpleBooleanProperty();

    private final BooleanProperty flightLogSelectionAvailable = new SimpleBooleanProperty();
    private final BooleanProperty imageSelectionAvailable = new SimpleBooleanProperty();
    private final BooleanProperty flightPlanSelectionAvailable = new SimpleBooleanProperty();

    private final ReadOnlyStringProperty missionName;
    private final ReadOnlyListProperty<FlightPlan> flightPlans;
    private final ListProperty<FlightPlan> selectedFlightPlans =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<File> images = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final IntegerProperty triggersCount = new SimpleIntegerProperty();
    private final IntegerProperty imagesCount = new SimpleIntegerProperty();
    private final StringProperty imageFolder = new SimpleStringProperty();
    private final StringProperty imageFolderValid = new SimpleStringProperty();

    private final ObjectProperty<File> targetFolder = new SimpleObjectProperty<>();
    private final UIAsyncBooleanProperty copyFiles = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty eraseLogs = new UIAsyncBooleanProperty(this);
    private final ObjectProperty<ValidationStatus> cameraImagesValidationStatus = new SimpleObjectProperty<>();
    private final ObjectProperty<ValidationStatus> dataSetSizeValidationStatus = new SimpleObjectProperty<>();

    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final IBackgroundTaskManager backgroundTaskManager;
    private final IHardwareConfigurationManager hardwareConfigurationManager;
    private final IValidationService validationService;
    private final Strand strand = new Strand();
    private final MenuModel addFlightLogsMenuModel;
    private final Command importCommand;
    private final Command browseImagesCommand;
    private final Command showHelpCommand;
    private final Command renameMissionCommand;
    private final Command updateDriveListCommand;
    private final InvalidationListener validatorsChangedListener = observable -> initValidators();
    private final DisplaySettings displaySettings;
    private final GeneralSettings generalSettings;
    private final IMapView mapView;
    private final ISelectionManager selectionManager;
    private final ILicenceManager licenceManager;
    private final SimpleObjectProperty<Matching> currentMatching = new SimpleObjectProperty();

    private final FilteredList<FlightLogEntry> notSelectedFlightLogs =
        new FilteredList<>(flightLogs, FlightLogEntry::isNotSelected);
    private final BooleanProperty isEveryLogSelected = new SimpleBooleanProperty();
    private Property<Boolean> selectionCheckBoxProperty = new SimpleBooleanProperty();
    private boolean updatesLogList;

    // what is it and how it works ??
    // it is not empty only in case if we add logs from some folder containing jsons and images ??
    private Optional<File> folderForLogList = Optional.empty();

    @Inject
    public DataImportViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IBackgroundTaskManager backgroundTaskManager,
            IHardwareConfigurationManager hardwareConfigurationManager,
            IValidationService validationService,
            IMapView mapView,
            ISelectionManager selectionManager,
            GeneralSettings generalSettings,
            DisplaySettings displaySettings,
            ILicenceManager licenceManager) {
        this.mapView = mapView;
        this.displaySettings = displaySettings;
        this.generalSettings = generalSettings;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.backgroundTaskManager = backgroundTaskManager;
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.validationService = validationService;
        this.selectionManager = selectionManager;
        this.licenceManager = licenceManager;

        currentMatching.bind(
            PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                .selectReadOnlyObject(Mission::currentMatchingProperty));
        missionName =
            PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                .selectReadOnlyString(Mission::nameProperty);

        flightPlans =
            PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                .selectReadOnlyList(Mission::flightPlansProperty);

        addFlightLogsMenuModel =
            MenuModel.build(
                AddFlightLogsMenuIds.CAPTION,
                MenuModel.group(AddFlightLogsMenuIds.DRIVES_LIST),
                MenuModel.group(
                    MenuModel.item(
                        AddFlightLogsMenuIds.CONNECTED_UAV,
                        languageHelper.toFriendlyName(AddFlightLogsMenuIds.CONNECTED_UAV))),
                MenuModel.group(
                    MenuModel.item(
                        AddFlightLogsMenuIds.PROJECT_FOLDER,
                        languageHelper.toFriendlyName(AddFlightLogsMenuIds.PROJECT_FOLDER)),
                    MenuModel.item(
                        AddFlightLogsMenuIds.CUSTOM_LOCATION,
                        languageHelper.toFriendlyName(AddFlightLogsMenuIds.CUSTOM_LOCATION))));

        addFlightLogsMenuModel.setText(languageHelper.toFriendlyName(AddFlightLogsMenuIds.CAPTION));

        addFlightLogsMenuModel
            .find(AddFlightLogsMenuIds.CONNECTED_UAV)
            .visibleProperty()
            .bind(
                generalSettings
                    .operationLevelProperty()
                    .isEqualTo(OperationLevel.DEBUG)
                    .or(licenceManager.isGrayHawkEditionProperty()));

        addFlightLogsMenuModel
            .find(AddFlightLogsMenuIds.PROJECT_FOLDER)
            .setActionHandler(
                () -> showAddFlightLogsDialog(applicationContext.getCurrentLegacyMission().getDirectory()));

        addFlightLogsMenuModel
            .find(AddFlightLogsMenuIds.CUSTOM_LOCATION)
            .setActionHandler(this::showAddFlightLogsDialog);

        eraseLogs.bindBidirectional(generalSettings.eraseLogsAfterCopyProperty());

        flightLogSelectionAvailable.bind(flightLogs.emptyProperty().not());
        imageSelectionAvailable.bind(flightLogs.emptyProperty().not().and(logFolderContainsImages.not()));
        flightPlanSelectionAvailable.bind(flightLogs.emptyProperty().not());

        applicationContext.currentLegacyMissionProperty().addListener(observable -> refreshLogList(null));

        imagesCount.bind(images.sizeProperty());
        imagesCount.addListener(
            (observable, oldValue, newValue) -> {
                Matching matching = currentMatching.get();
                if (matching != null) {
                    matching.toImportImagesCountProperty().set(newValue.longValue());
                }
            });

        triggersCount.addListener(
            (observable, oldValue, newValue) -> {
                updateMatching(newValue);
            });

        triggersCount.bind(
            Bindings.createIntegerBinding(
                () ->
                    flightLogs
                        .stream()
                        .filter(FlightLogEntry::isSelected)
                        .mapToInt(FlightLogEntry::getImageCount)
                        .sum(),
                flightLogs));

        images.addListener(
            (observable, oldValue, newValue) -> {
                Matching matching = currentMatching.get();
                if (matching != null) {
                    matching.toImportImagesProperty().set(newValue);
                }
            });

        imageFolder.addListener(
            (observable, oldValue, newValue) -> {
                if (this.images.size() > 0) System.out.println(this.images.get(0).getParent() + " / " + newValue);

                if (newValue == null || newValue.trim().isEmpty()) {
                    this.images.clear();
                    imageFolderValid.set(null);
                    return;
                }

                if (!(this.images.size() > 0 && this.images.get(0).getParent().equals(newValue))) {
                    try {
                        Path[] files = {Paths.get(newValue)};
                        if (files[0].toFile().exists()) {
                            FileHelper.GetFotosResult images =
                                FileHelper.fetchFotos(
                                    List.of(files).stream().map(Path::toFile).collect(Collectors.toList()));
                            this.images.clear();
                            this.images.addAll(images.fotos);
                            imageFolderValid.set(
                                languageHelper.getString(PHOTO_FOLDER_MESSAGE) + this.images.getSize());
                            return;
                        }
                    } catch (Exception e) {
                        LOGGER.info("Path not found/invalid");
                    }

                    this.images.clear();
                    imageFolderValid.set(languageHelper.getString(NO_PHOTO_FOLDER_MESSAGE));
                }

                Matching matching = currentMatching.get();
                if (matching != null) {
                    matching.toImportImageSourceFolderProperty().set(newValue);
                }
            });

        targetFolder.addListener(
            (observable, oldValue, newValue) -> {
                Matching matching = currentMatching.get();
                if (matching != null) {
                    matching.toImportTargetFolderProperty().set(newValue);
                }
            });

        validationService.addValidatorsChangedListener(validatorsChangedListener);
        refreshLogList(null);

        flightLogs.addListener(
            (ListChangeListener<? super FlightLogEntry>)
                c -> {
                    updatePreview();
                    inputImageFolder();
                    updateMatching();
                });

        importCommand =
            new DelegateCommand(
                this::transferData,
                applicationContext
                    .currentMissionIsNoDemo()
                    .and(imagesCount.greaterThan(0).or(triggersCount.greaterThan(0))));

        browseImagesCommand = new DelegateCommand(this::browseImages);

        showHelpCommand = new DelegateCommand(() -> navigationService.navigateTo(SidePanePage.VIEW_DATASET_HELP));

        renameMissionCommand = new DelegateCommand(applicationContext::renameCurrentMission);

        updateDriveListCommand = new DelegateCommand(this::updateDriveList);

        isEveryLogSelected.bind(
            Bindings.createBooleanBinding(() -> notSelectedFlightLogs.isEmpty(), notSelectedFlightLogs));

        isEveryLogSelected.addListener((observable, oldValue, newValue) -> updateSelectionCheckBox());
        selectionCheckBoxProperty.addListener(
            (observable, oldValue, newValue) -> updateLogList(selectionCheckBoxProperty.getValue()));
    }

    private void updateMatching(Number newValue) {
        Matching matching = currentMatching.get();
        if (matching != null) {
            matching.toImportTriggersCountProperty().set(newValue.longValue());
            matching.toImportFlightLogProperty()
                .set(flightLogs.stream().filter(FlightLogEntry::isSelected).count() > 0);
        }
    }

    private void updateMatching() {
        Matching matching = currentMatching.get();
        if (matching != null) {
            matching.toImportFlightLogProperty()
                .set(flightLogs.stream().filter(FlightLogEntry::isSelected).count() > 0);
        }
    }

    private void updateSelectionCheckBox() {
        updatesLogList = true;
        selectionCheckBoxProperty.setValue(isEveryLogSelected.get());
        updatesLogList = false;
    }

    private void updateLogList(Boolean checkBoxPropertyValue) {
        if (!updatesLogList) {
            for (FlightLogEntry flightLog : flightLogs) {
                flightLog.setSelected(checkBoxPropertyValue);
            }
        }
    }

    public ReadOnlyStringProperty missionNameProperty() {
        return missionName;
    }

    public ReadOnlyBooleanProperty logFolderContainsImagesProperty() {
        return logFolderContainsImages;
    }

    public ReadOnlyBooleanProperty flightLogSelectionAvailableProperty() {
        return flightLogSelectionAvailable;
    }

    public ReadOnlyBooleanProperty flightPlanSelectionAvailableProperty() {
        return flightPlanSelectionAvailable;
    }

    public Property<Boolean> copyFilesProperty() {
        return copyFiles;
    }

    public Property<Boolean> eraseLogsProperty() {
        return eraseLogs;
    }

    public Property<String> imageFolderProperty() {
        return imageFolder;
    }

    public ReadOnlyIntegerProperty triggersCountProperty() {
        return triggersCount;
    }

    public StringProperty imagesFolderValidTextProperty() {
        return imageFolderValid;
    }

    public ReadOnlyIntegerProperty imagesCountProperty() {
        return imagesCount;
    }

    public ReadOnlyListProperty<FlightPlan> flightPlansProperty() {
        return flightPlans;
    }

    public ReadOnlyListProperty<FlightPlan> selectedFlightPlansProperty() {
        return selectedFlightPlans;
    }

    public ReadOnlyListProperty<FlightLogEntry> flightLogsProperty() {
        return flightLogs;
    }

    public ReadOnlyObjectProperty<MenuModel> datasetMenuModelProperty() {
        return mainScope.datasetMenuModelProperty();
    }

    public ReadOnlyObjectProperty<ValidationStatus> cameraImagesValidationStatusProperty() {
        return cameraImagesValidationStatus;
    }

    public ReadOnlyObjectProperty<ValidationStatus> dataSetSizeValidationStatusProperty() {
        return dataSetSizeValidationStatus;
    }

    public Command getImportCommand() {
        return importCommand;
    }

    public Command getBrowseImagesCommand() {
        return browseImagesCommand;
    }

    public Command getShowHelpCommand() {
        return showHelpCommand;
    }

    public Command getRenameMissionCommand() {
        return renameMissionCommand;
    }

    public Command getUpdateDriveListCommand() {
        return updateDriveListCommand;
    }

    public MenuModel getAddFlightLogsMenuModel() {
        return addFlightLogsMenuModel;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        currentMatching.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null && newValue.getStatus() == MatchingStatus.NEW) {
                    imageFolder.set(null);
                    // selectedFlightLogs.clear();
                }
            });

        validationService.addValidatorsChangedListener((observable) -> initValidators());
        navigationService
            .sidePanePageProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == SidePanePage.DATA_IMPORT) {
                        newDatasetCreated();
                    }
                });

        MenuModel mainMenuModel = mainScope.mainMenuModelProperty().get();
        mainMenuModel
            .find(MainMenuModel.Dataset.IMPORT_LOG_ONLY)
            .setActionHandler(
                this::transferData, triggersCount.greaterThan(0).and(applicationContext.currentMissionIsNoDemo()));

        mainMenuModel
            .find(MainMenuModel.Dataset.IMPORT_LOG_ONLY)
            .visibleProperty()
            .bind(
                generalSettings
                    .operationLevelProperty()
                    .isNotEqualTo(OperationLevel.USER)
                    .or(licenceManager.isGrayHawkEditionProperty()));

        initValidators();
        updatePreview();
    }

    private void updateDriveList() {
        List<MenuModel> driveItems = new ArrayList<>();
        for (final DriveInformation driveInfo : DriveInformation.getDrives()) {
            MenuModel menuModel =
                MenuModel.item(languageHelper.toFriendlyName(driveInfo.getType()) + " " + driveInfo.getName());
            menuModel.setActionHandler(() -> showAddFlightLogsDialog(driveInfo));
            driveItems.add(menuModel);
        }

        addFlightLogsMenuModel.find(AddFlightLogsMenuIds.DRIVES_LIST).getChildren().setAll(driveItems);
    }

    private void newDatasetCreated() {
        // search for folder to start log browsing
        File initialFolder = FileHelper.tryToFindInitialLogSdCard();

        if (initialFolder == null) {
            folderForLogList = Optional.empty();
            refreshLogList(null);
            this.images.clear();
            imageFolderValid.set(null);
            imageFolder.set(null);
            logFolderContainsImages.set(false);

            if (displaySettings.getWorkflowHints().dataTransferProperty().get()) {
                Toast toast =
                    Toast.of(ToastType.INFO)
                        .setShowIcon(true)
                        .setText(
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.sidepane.analysis.DataImportViewModel.hintInsertLogsSD"))
                        .setCloseable(true)
                        .setAction(
                            languageHelper.getString(
                                "com.intel.missioncontrol.api.workflow.AoiWorkflowHints.aoi.hintGotIt"),
                            true,
                            true,
                            () -> displaySettings.getWorkflowHints().dataTransferProperty().set(false),
                            Platform::runLater)
                        .create();
                applicationContext.addToast(toast);
            }
        } else {
            showAddFlightLogsDialog(initialFolder.toPath());
        }
    }

    private void initValidators() {
        ValidatorBase<?> tmp;
        Matching matching = currentMatching.get();
        if (matching == null) {
            return;
        }

        tmp = validationService.getValidator(LogsAndImagesMatchesValidator.class, matching);
        cameraImagesValidationStatus.set(tmp != null ? tmp.getValidationStatus() : null);

        tmp = validationService.getValidator(AvailableSpaceToCopyValidator.class, matching);
        dataSetSizeValidationStatus.set(tmp != null ? tmp.getValidationStatus() : null);
    }

    private void browseImages() {

        // LOGGER.info("start check for path");
        Dispatcher.platform()
            .runLater(
                () -> {
                    try {
                        File initialFolder = null;
                        Licence activeLicence = licenceManager.activeLicenceProperty().get();
                        if (activeLicence != null
                                && (activeLicence.getMaxOperationLevel() == OperationLevel.DEBUG
                                    || activeLicence.isGrayHawkEdition())) {
                            // LOGGER.info("Check for linkbox");
                            File linkbox = new File(LINKBOX);
                            if (linkbox.exists()) { // TODO waits for timeout => takes too long!
                                File linkboxMission =
                                    new File(LINKBOX, applicationContext.getCurrentMission().getName());
                                if (linkboxMission.exists()) {
                                    initialFolder = linkboxMission;
                                } else {
                                    initialFolder = linkbox;
                                }
                            }
                            // TODO password automatically if not working without
                            if (initialFolder == null) {
                                LOGGER.info("Could not find linkbox");
                            }
                        }

                        if (initialFolder == null) {
                            initialFolder = FileHelper.tryToFindInitialImagesSdCard();
                        }

                        if (initialFolder == null) {
                            initialFolder = applicationContext.getCurrentLegacyMission().getDirectory().toFile();
                        }

                        File finalInitialFolder = initialFolder;
                        Dispatcher.platform()
                            .run(
                                () -> {
                                    Path[] files =
                                        dialogService.requestMultiFileOpenDialog(
                                            this,
                                            languageHelper.getString(
                                                "com.intel.missioncontrol.ui.analysis.AnalysisView.browseImages.title"),
                                            finalInitialFolder.toPath(),
                                            FileFilter.JPEG,
                                            FileFilter.XMP,
                                            FileFilter.RAW);

                                    if (files.length == 0) {
                                        return;
                                    }

                                    FileHelper.GetFotosResult images =
                                        FileHelper.fetchFotos(
                                            List.of(files).stream().map(Path::toFile).collect(Collectors.toList()));
                                    this.images.clear();
                                    this.images.addAll(images.fotos);
                                    imageFolderValid.set(
                                        languageHelper.getString(PHOTO_FOLDER_MESSAGE) + this.images.getSize());
                                    imageFolder.set(images.picFolder.getAbsolutePath());
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void openImageFolder() {
        if (StringUtils.isNotEmpty(imageFolder.get())) {
            try {
                FileHelper.openFile(new File(imageFolder.get()));
            } catch (Exception e) {
                LOGGER.error("Could not browse images by path " + imageFolder.get(), e);
            }
        }
    }

    private void transferData() {
        final Matching matching = currentMatching.get();
        final List<FlightLogEntry> selectedFlightLogs = getSelectedFlightLogs();
        final File imageFolder =
            this.imageFolder.get() == null
                ? selectedFlightLogs.get(0).getImageFolder()
                : new File(this.imageFolder.get());
        final List<Flightplan> flightPlans =
            selectedFlightPlans.stream().map(FlightPlan::getLegacyFlightplan).collect(Collectors.toList());
        final List<File> logFiles =
            selectedFlightLogs.stream().map(FlightLogEntry::getPath).collect(Collectors.toList());
        final FileHelper.GetFotosResult photoResultHelper;

        if (logFolderContainsImages.get()) {
            photoResultHelper = new FileHelper.GetFotosResult();
            photoResultHelper.fotos = new Vector<>();

            final List<File> imgFolders =
                selectedFlightLogs
                    .stream()
                    .filter(FlightLogEntry::hasImageFolder)
                    .map(FlightLogEntry::getImageFolder)
                    .collect(Collectors.toList());
            final List<String> imgFolderPaths =
                imgFolders.stream().map(File::getAbsolutePath).collect(Collectors.toList());
            final String[] imgFolderPathStr = new String[imgFolderPaths.size()];

            int index = 0;

            for (final File imgFolder : imgFolders) {
                photoResultHelper.fotos.addAll(
                    FileHelper.getSDcardJPEGs(imgFolder, MFileFilter.jpegFilter.getWithoutFolders()));
                imgFolderPathStr[index] = imgFolder.getAbsolutePath();
                index = index + 1;
            }

            photoResultHelper.picFolder = new File(StringUtils.getCommonPrefix(imgFolderPathStr));
        } else {
            photoResultHelper = new FileHelper.GetFotosResult();
            photoResultHelper.picFolder = this.imageFolder.get() == null ? null : new File(this.imageFolder.get());
            photoResultHelper.fotos = new Vector<>(this.images);
        }

        if (generalSettings.getOperationLevel() == OperationLevel.USER && photoResultHelper.fotos.isEmpty()) {
            dialogService.showWarningMessage(
                languageHelper.getString(NO_PHOTO_TITLE), languageHelper.getString(NO_PHOTO_MESSAGE));
            return;
        }

        if (logFiles.isEmpty() && imagesCount.lessThan(1).get()) {
            dialogService.showWarningMessage(
                languageHelper.getString(NO_LOGFILES_TITLE), languageHelper.getString(NO_LOGFILES_MESSAGE));
            return;
        }

        final IHardwareConfiguration hardwareConfig =
            getHardwareConfig(selectedFlightLogs, flightPlans, photoResultHelper.fotos);
        if (hardwareConfig == null) {
            return;
        }

        Debug.getLog()
            .log(
                Level.INFO,
                "Guessed this hardware configuration:  platform:"
                    + hardwareConfig.getPlatformDescription().getId()
                    + "  cam:"
                    + hardwareConfig.getPrimaryPayload().getDescription().getId()
                    + "  lens:"
                    + hardwareConfig
                        .getPrimaryPayload(IGenericCameraConfiguration.class)
                        .getLens()
                        .getDescription()
                        .getId());

        CreateDatasetTask task =
            new CreateDatasetTask(
                mapView,
                imageFolder,
                applicationContext.currentLegacyMissionProperty().get().getDirectoryFile(),
                flightPlans,
                logFiles,
                photoResultHelper,
                hardwareConfig,
                languageHelper,
                eraseLogs.get(),
                matching,
                applicationContext,
                navigationService,
                generalSettings,
                selectionManager,
                applicationContext.getCurrentLegacyMission(),
                copyFiles.get());
        matching.dataTransferBackgroundTaskProperty().set(task);

        backgroundTaskManager.submitTask(task);
    }

    private IHardwareConfiguration getHardwareConfig(
            List<FlightLogEntry> selectedFlightLogs, List<Flightplan> flightPlans, Vector<File> photos) {
        IHardwareConfiguration hardwareConfig = null;

        try {
            final File photography = photos.isEmpty() ? null : photos.firstElement();
            final boolean isFalcon = (selectedFlightLogs.size() > 0) && selectedFlightLogs.get(0).isIsFalcon();

            hardwareConfig =
                Matching.guessHardwareConfiguration(hardwareConfigurationManager, photography, isFalcon, flightPlans);
        } catch (final Exception exception) {
            Debug.getLog().log(Level.WARNING, "Could not extract exif information from sample image", exception);
            dialogService.showWarningMessage(
                languageHelper.getString(CANT_DETECT_HW_TITLE), languageHelper.getString(CANT_DETECT_HW_MESSAGE));
        }

        return hardwareConfig;
    }

    private void showAddFlightLogsDialog() {
        Path originPath =
            dialogService.requestDirectoryChooser(
                this,
                languageHelper.getString(DataImportViewModel.class.getName() + ".selectFlightLogFolder"),
                this,
                applicationContext.getCurrentLegacyMission().getDirectory());

        if (originPath != null) {
            showAddFlightLogsDialog(originPath);
        }
    }

    private void showAddFlightLogsDialog(Path originPath) {
        showAddFlightLogsDialog(originPath.toString());
    }

    private void showAddFlightLogsDialog(DriveInformation driveInfo) {
        File drive = new File(driveInfo.getName());
        File folder = FileHelper.tryToFindInitialLogSdCardFolder(drive);
        if (folder == null) {
            folder = drive;
        }

        showAddFlightLogsDialog(folder.getAbsolutePath());
    }

    private void showAddFlightLogsDialog(String originPath) {
        strand.runLater(
            () -> {
                try {
                    List<FlightLogEntry> flightLogsNew = LogFileHelper.getLogsInFolder(new File(originPath), true);
                    boolean containsImageFolders = flightLogsNew.stream().anyMatch(FlightLogEntry::hasImageFolder);
                    Dispatcher.platform().run(() -> showAddFlightLogsDialog(originPath, containsImageFolders));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    private void showAddFlightLogsDialog(String originPath, boolean containsImageFolders) {
        // check if this is a logfolder with images -> skip copy step!
        logFolderContainsImages.set(containsImageFolders);
        if (containsImageFolders) {
            folderForLogList = Optional.of(new File(originPath));
            refreshLogList(null);
            inputImageFolder();
        } else {
            folderForLogList = Optional.empty();
            refreshLogList(null);
            CopyLogsTask copyLogsTask =
                dialogService
                    .requestDialogAndWait(this, AddFlightLogsViewModel.class, () -> originPath)
                    .getDialogResult();

            updatePreview();
            if (copyLogsTask == null) {
                return;
            }

            copyLogsTask.setOnFinished(event -> refreshLogList(copyLogsTask.getCopyTargets()));
            if (copyLogsTask.hasFinished()) {
                refreshLogList(copyLogsTask.getCopyTargets());
            }
        }
    }

    private void refreshLogList(List<File> copiedFiles) {
        Mission mission = applicationContext.getCurrentLegacyMission();
        if (mission != null) {
            targetFolder.setValue(mission.getDirectoryFile());
            final File missionFlightLogsFolder = MissionConstants.getFlightLogsFolder(mission.getDirectory());

            Dispatcher.platform()
                .runLater(
                    () -> {
                        List<FlightLogEntry> flightLogsNew =
                            LogFileHelper.getLogsInFolder(missionFlightLogsFolder, true);
                        // compatibility: for storing the log paths into the mission info file (if there are logs copied
                        // into the mission folder)
                        if (!flightLogsNew.isEmpty() && mission.flightLogsProperty().isEmpty()) {
                            flightLogsNew.forEach(log -> mission.addFlightLog(log.getPath()));
                        }

                        if (folderForLogList.isPresent()) {
                            flightLogsNew.addAll(LogFileHelper.getLogsInFolder(folderForLogList.get(), true));
                        }

                        // restore old selection
                        for (FlightLogEntry oldLog : flightLogs.get()) {
                            for (FlightLogEntry newLog : flightLogsNew) {
                                if (oldLog.getPath().equals(newLog.getPath())) {
                                    newLog.setSelected(oldLog.isSelected());
                                    break;
                                }
                            }
                        }

                        // add just copied files to selection
                        if (copiedFiles != null) {
                            for (File target : copiedFiles) {
                                for (FlightLogEntry newLog : flightLogsNew) {
                                    if (target.equals(newLog.getPath())) {
                                        newLog.setSelected(true);
                                        break;
                                    }
                                }
                            }
                        }

                        Dispatcher.platform()
                            .run(
                                () -> {
                                    flightLogs.setAll(flightLogsNew);
                                });
                    });
        }
    }

    private void updatePreview() {
        Matching matching = currentMatching.get();
        if (matching == null || matching.getStatus() != MatchingStatus.NEW) {
            return;
        }

        matching.previewLogfiles(getSelectedFlightLogs(), mapView);
    }

    private void inputImageFolder() {
        boolean hasImages = false;
        for (FlightLogEntry flightLogEntry : getSelectedFlightLogs()) {
            if (flightLogEntry.hasImageFolder()) {
                hasImages = true;
            }
        }

        logFolderContainsImages.set(hasImages);
    }

    public List<FlightLogEntry> getSelectedFlightLogs() {
        return flightLogs.stream().filter(FlightLogEntry::isSelected).collect(Collectors.toList());
    }

    public Property<Boolean> selectionCheckBoxProperty() {
        return selectionCheckBoxProperty;
    }
}
