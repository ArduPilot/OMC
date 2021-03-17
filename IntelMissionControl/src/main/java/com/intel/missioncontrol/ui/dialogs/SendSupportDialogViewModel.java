/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.api.support.ErrorCategory;
import com.intel.missioncontrol.api.support.ISupportManager;
import com.intel.missioncontrol.api.support.Priority;
import com.intel.missioncontrol.api.support.SupportConstants;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.MissionInfo;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.utils.validation.ObservableRuleBasedValidator;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import de.saxsys.mvvmfx.utils.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;
import org.asyncfx.collections.LockedList;

/** Created by akorotenko on 7/19/17. */
@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
@ScopeProvider(scopes = SendSupportDialogScope.class)
public class SendSupportDialogViewModel extends DialogViewModel {

    private static final String ESTIMATED_FILES_SIZE =
        "com.intel.missioncontrol.ui.dialogs.SendSupportDialogView.estimatedSizeLabel";

    private static final String MATCHINGS_ALERT =
        "com.intel.missioncontrol.ui.dialogs.SendSupportDialogView.matchingsWarning";
    private static final String MISSIONS_ALERT =
        "com.intel.missioncontrol.ui.dialogs.SendSupportDialogView.missionWarning";

    private static final String INVALID_EMAIL_ALERT_TITLE =
        "com.intel.missioncontrol.ui.dialogs.SendSupportDialogView.invalidEmailAlertTitle";
    private static final String INVALID_EMAIL_ALERT_MESSAGE =
        "com.intel.missioncontrol.ui.dialogs.SendSupportDialogView.invalidEmailAlertMessge";

    @InjectScope
    private MainScope mainScope;

    @Inject
    private IBackgroundTaskManager backgroundTaskManager;

    @InjectScope
    private SendSupportDialogScope sendSupportDialogScope;

    private final ReadOnlyListProperty<Priority> issuePriorities =
        new SimpleListProperty<>(FXCollections.observableArrayList(Priority.values()));
    private final ObjectProperty<Priority> selectedPriority = new SimpleObjectProperty<>();
    private final ReadOnlyListProperty<ErrorCategory> errorCategories =
        new SimpleListProperty<>(FXCollections.observableArrayList(ErrorCategory.values()));
    private final ObjectProperty<ErrorCategory> selectedCategory = new SimpleObjectProperty<>();
    private final StringProperty ticketId = new SimpleStringProperty();
    private final StringProperty problemDescription = new SimpleStringProperty();
    private final ObservableList<FileListCellViewModel> additionalFiles = FXCollections.observableArrayList();
    private final StringProperty estimatedFilesSize = new SimpleStringProperty();
    private final StringProperty currentMissionName = new SimpleStringProperty("");
    private final ObservableList<Files> baseFiles = FXCollections.observableArrayList();
    private final ReadOnlyBooleanWrapper requestHasBeenSentToSupport = new ReadOnlyBooleanWrapper();
    private final BooleanProperty useScreenshots = new SimpleBooleanProperty(false);
    private final BooleanProperty useSettingsAndLogs = new SimpleBooleanProperty(false);
    private final BooleanProperty useCurrentMission = new SimpleBooleanProperty(false);
    private final BooleanProperty showMissionSelectionWarning = new SimpleBooleanProperty(false);
    private final ObservableList<MatchingsTableRowData> matchings = FXCollections.observableArrayList();
    private final ObservableList<String> countryList = FXCollections.observableArrayList();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty countrySelected = new SimpleStringProperty();
    private final StringProperty emails = new SimpleStringProperty();
    private final IntegerProperty matchingsInternalStateChangedMarker = new SimpleIntegerProperty();

    private final IApplicationContext applicationContext;
    private final ILanguageHelper languageHelper;
    private final ISupportManager supportManager;
    private final IMissionManager missionManager;
    private final GeneralSettings generalSettings;
    private final IDialogService dialogService;
    private final IProgressTaskFactory progressTaskFactory;
    private final Mission.Factory missionFactory;

    private long size;
    private Validator matchingsValidator;
    private Validator missionValidator;

    @Inject
    public SendSupportDialogViewModel(
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            ISupportManager supportManager,
            IMissionManager missionManager,
            IDialogService dialogService,
            IProgressTaskFactory progressTaskFactory,
            ISettingsManager settingsManager,
            Mission.Factory missionFactory) {
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.supportManager = supportManager;
        this.missionManager = missionManager;
        this.dialogService = dialogService;
        this.progressTaskFactory = progressTaskFactory;
        this.missionFactory = missionFactory;
        generalSettings = settingsManager.getSection(GeneralSettings.class);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        MvvmFX.getNotificationCenter()
            .subscribe(
                FileListCellViewModel.CLOSE_FILE_EVENT,
                (key, payload) -> {
                    File file = (File)payload[0];
                    handleFileRemoved(file);
                });

        Mission mission = null;
        if (applicationContext.getCurrentMission() != null) {
            mission = applicationContext.getCurrentMission();
            useCurrentMission.setValue(true);
        } else if (!missionManager.recentMissionInfosProperty().isEmpty()) {
            try (LockedList<MissionInfo> missionInfos = missionManager.recentMissionInfosProperty().lock()) {
                MissionInfo info = missionInfos.iterator().next();
                mission = missionFactory.create(info);
            }
        }

        if (mission != null) {
            currentMissionName.bind(mission.nameProperty());
            matchings.addAll(
                mission.getMatchings()
                    .stream()
                    .filter(matching -> matching.getMatchingFolder() != null)
                    .map(matching -> new MatchingsTableRowData(matching.getName(), matching.getMatchingFolder()))
                    .collect(Collectors.toList()));
        }

        estimateFilesSize();

        requestHasBeenSentToSupport.setValue(false);
        useScreenshots.addListener((observable, oldValue, newValue) -> estimateFilesSize());
        useSettingsAndLogs.addListener((observable, oldValue, newValue) -> estimateFilesSize());
        useCurrentMission.addListener(
            (observable, oldValue, newValue) -> {
                if (selectedCategory.getValue() == ErrorCategory.BEHAVED_UNPREDICTABLY && newValue == false) {
                    showMissionSelectionWarning.setValue(true);
                } else {
                    showMissionSelectionWarning.setValue(false);
                }

                estimateFilesSize();
            });

        selectedCategory.addListener(
            (observable, oldValue, newValue) -> {
                useSettingsAndLogs.setValue(false);
                useCurrentMission.setValue(false);

                switch (newValue) {
                case CLOSED_UNEXPECTEDLY:
                case LICENSE_ISSUE:
                case OTHER_ISSUES:
                    useSettingsAndLogs.setValue(true);
                    break;
                case BEHAVED_UNPREDICTABLY:
                case POST_PROCESSING_ISSUE:
                    useSettingsAndLogs.setValue(true);
                    useCurrentMission.setValue(true);
                    break;
                }
            });

        countryList.clear();
        String[] isoCountries = Locale.getISOCountries();
        for (String name : isoCountries) {
            Locale obj = new Locale("", name);
            String eachcountry = obj.getDisplayCountry();
            if (!eachcountry.isEmpty() && !countryList.contains(eachcountry)) {
                countryList.add(eachcountry);
            }
        }

        Collections.sort(countryList);

        // read last support entry
        fullName.setValue(generalSettings.fullNameInSupportProperty().getValue());
        countrySelected.setValue(generalSettings.countryInSupportProperty().getValue());
        emails.setValue(generalSettings.emailsInSupportProperty().getValue());

        initValidators();
    }

    private void initValidators() {
        BooleanBinding oneSelectedMatching =
            Bindings.createBooleanBinding(
                () ->
                    matchings
                        .stream()
                        .anyMatch(
                            matchingsTableRowData ->
                                matchingsTableRowData.getMatchingImagesUsage() == MatchingImagesUsage.PREVIEW
                                    || matchingsTableRowData.getMatchingImagesUsage() == MatchingImagesUsage.ALL),
                matchings,
                matchingsInternalStateChangedMarker);

        BooleanBinding postProcessingSelected =
            Bindings.createBooleanBinding(
                () -> selectedCategory.getValue() == ErrorCategory.POST_PROCESSING_ISSUE, selectedCategory);

        BooleanBinding postProcessingSelectedFlags =
            Bindings.createBooleanBinding(
                () ->
                    selectedCategory.getValue() == ErrorCategory.POST_PROCESSING_ISSUE
                        && useCurrentMission.getValue()
                        && useSettingsAndLogs.getValue(),
                selectedCategory,
                useCurrentMission,
                useSettingsAndLogs);

        matchingsValidator =
            new ObservableRuleBasedValidator(
                postProcessingSelected
                    .not()
                    .or(postProcessingSelectedFlags.and(oneSelectedMatching).or(postProcessingSelectedFlags.not())),
                ValidationMessage.warning(languageHelper.getString(MATCHINGS_ALERT)));

        BooleanBinding behavedUnpredictably =
            Bindings.createBooleanBinding(
                () ->
                    selectedCategory.getValue() != ErrorCategory.BEHAVED_UNPREDICTABLY
                        || selectedCategory.getValue() == ErrorCategory.BEHAVED_UNPREDICTABLY
                            && useCurrentMission.getValue(),
                selectedCategory,
                useCurrentMission);
        missionValidator =
            new ObservableRuleBasedValidator(
                behavedUnpredictably, ValidationMessage.warning(languageHelper.getString(MISSIONS_ALERT)));
    }

    private void handleFileRemoved(File file) {
        additionalFiles.removeIf(fileListCellViewModel -> fileListCellViewModel.getFile().equals(file));
        estimateFilesSize();
    }

    public void handleFilesDropped(List<File> files) {
        for (File file : files) {
            additionalFiles.add(new FileListCellViewModel(file));
        }

        estimateFilesSize();
    }

    public void handleMatchingUsageChanging() {
        matchingsInternalStateChangedMarker.set(matchingsInternalStateChangedMarker.add(1).getValue());
        estimateFilesSize();
    }

    public void sendToSupport() {
        if (!validateEmails()) {
            Alert InvalidEmailAlert = new Alert(Alert.AlertType.NONE);
            InvalidEmailAlert.initStyle(StageStyle.UTILITY);
            InvalidEmailAlert.setTitle(languageHelper.getString(INVALID_EMAIL_ALERT_TITLE));
            InvalidEmailAlert.setContentText(languageHelper.getString(INVALID_EMAIL_ALERT_MESSAGE));
            InvalidEmailAlert.getDialogPane().getButtonTypes().add(ButtonType.OK);
            InvalidEmailAlert.show();
            return;
        }

        ObservableList<String> emailsObservaleList = FXCollections.observableArrayList();
        String emailsInString = emailsProperty().getValue();
        String[] emailsList = emailsInString.split(";");
        for (String email : emailsList) {
            email = email.trim();
            emailsObservaleList.add(email);
        }

        ProgressTask task =
            progressTaskFactory.getForSendSupport(
                selectedCategory.get(),
                selectedPriority.get(),
                problemDescription.get(),
                getOptions(),
                additionalFiles.stream().map(FileListCellViewModel::getFile).collect(Collectors.toList()),
                emailsObservaleList,
                matchings,
                size,
                fullName.get(),
                countrySelected.get(),
                ticketId.get());

        generalSettings.fullNameInSupportProperty().setValue(fullName.getValue());
        generalSettings.countryInSupportProperty().setValue(countrySelected.getValue());
        generalSettings.emailsInSupportProperty().setValue(emails.getValue());
        backgroundTaskManager.submitTask(task);
        requestHasBeenSentToSupport.set(true);

        getCloseCommand().execute();
    }

    public void openPreviewFilesDialog() {
        List<File> ff = supportManager.getFilesForRequest(getOptions(), matchings);
        List<FileListCellViewModel> files = ff.stream().map(FileListCellViewModel::new).collect(Collectors.toList());
        files.addAll(additionalFiles);
        sendSupportDialogScope.setFiles(FXCollections.observableArrayList(files));
        dialogService.requestDialogAndWait(this, PreviewFilesDialogViewModel.class);
    }

    private Map<String, Boolean> getOptions() {
        Map<String, Boolean> options = new HashMap<>();
        options.put(SupportConstants.OPTION_SCREENSHOTS, useScreenshots.getValue());
        options.put(SupportConstants.OPTION_SESSION_SETTINGS, useCurrentMission.getValue());
        options.put(SupportConstants.OPTION_APPLICATION_SETTINGS, useSettingsAndLogs.getValue());
        return options;
    }

    private long recalculateAdditionalFilesSize() {
        return additionalFiles
            .stream()
            .mapToLong(
                model -> {
                    File file = model.getFile();
                    if (file.isDirectory()) {
                        try {
                            return Files.walk(Paths.get(file.toURI()))
                                .filter(path -> path.toFile().isFile())
                                .mapToLong(path -> path.toFile().length())
                                .sum();
                        } catch (IOException e) {
                            // something happened
                        }
                    }

                    return file.length();
                })
            .sum();
    }

    private void estimateFilesSize() {
        size = recalculateAdditionalFilesSize();
        size +=
            supportManager
                .getFilesForRequest(getOptions(), matchings)
                .stream()
                .mapToLong(value -> value.length())
                .sum();
        estimatedFilesSize.set(languageHelper.getString(ESTIMATED_FILES_SIZE, FileUtils.byteCountToDisplaySize(size)));
    }

    public BooleanProperty useScreenshotsProperty() {
        return useScreenshots;
    }

    public BooleanProperty useCurrentMissionProperty() {
        return useCurrentMission;
    }

    public BooleanProperty useSettingsAndLogsProperty() {
        return useSettingsAndLogs;
    }

    public ObservableList<Priority> getIssuePriorities() {
        return issuePriorities.get();
    }

    public StringProperty ticketIdProperty() {
        return ticketId;
    }

    public StringProperty problemDescriptionProperty() {
        return problemDescription;
    }

    public ObjectProperty<Priority> selectedPriorityProperty() {
        return selectedPriority;
    }

    public ReadOnlyListProperty<ErrorCategory> errorCategoriesProperty() {
        return errorCategories;
    }

    public ObjectProperty<ErrorCategory> selectedCategoryProperty() {
        return selectedCategory;
    }

    public ObservableList<FileListCellViewModel> getAdditionalFiles() {
        return additionalFiles;
    }

    public StringProperty estimatedFilesSizeProperty() {
        return estimatedFilesSize;
    }

    public StringProperty currentMissionNameProperty() {
        return currentMissionName;
    }

    public BooleanProperty showMissionSelectionWarningProperty() {
        return showMissionSelectionWarning;
    }

    public ReadOnlyBooleanProperty requestHasBeenSentToSupport() {
        return requestHasBeenSentToSupport.getReadOnlyProperty();
    }

    public BooleanBinding canUploadProperty() {
        return Bindings.createBooleanBinding(
            () ->
                (!Strings.isNullOrEmpty(problemDescription.getValue())
                        && !Strings.isNullOrEmpty(countrySelected.getValue())
                        && !Strings.isNullOrEmpty(fullName.getValue())
                        && !Strings.isNullOrEmpty(emails.getValue()))
                    && selectedPriority.isNotNull().get()
                    && selectedCategory.isNotNull().get(),
            problemDescription,
            selectedPriority,
            selectedCategory,
            countrySelected,
            fullName,
            emails);
    }

    public ObservableList<MatchingsTableRowData> getMatchings() {
        return matchings;
    }

    public ValidationStatus matchingsValidation() {
        return matchingsValidator.getValidationStatus();
    }

    public ValidationStatus missionValidation() {
        return missionValidator.getValidationStatus();
    }

    public ObservableList<String> getCountryList() {
        return countryList;
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public StringProperty countrySelectedProperty() {
        return countrySelected;
    }

    public StringProperty emailsProperty() {
        return emails;
    }

    public Boolean validateEmails() {
        //// validate email
        String emailforall = emailsProperty().getValue();
        String[] emailist = emailforall.split(";");
        String REGEX_EMAIL =
            "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        Pattern p = Pattern.compile(REGEX_EMAIL, Pattern.CASE_INSENSITIVE);
        for (String email : emailist) {
            email = email.trim();
            Matcher m = p.matcher(email);
            if (!(m.find() && m.group().equals(email))) {
                return false;
            }
        }

        return true;
    }
}
