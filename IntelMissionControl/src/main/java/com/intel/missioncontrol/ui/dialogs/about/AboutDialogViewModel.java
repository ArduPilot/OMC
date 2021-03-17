/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.about;

import com.google.inject.Inject;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ManifestInfo;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import com.intel.missioncontrol.ui.update.AvailableUpdate;
import com.intel.missioncontrol.ui.update.IUpdateManager;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.licence.AllowedUser;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.licence.Licence;
import eu.mavinci.core.update.EnumUpdateTargets;
import eu.mavinci.core.update.UpdateURL;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MACaddrSource;
import eu.mavinci.desktop.main.core.Application;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by akorotenko on 7/19/17. */
@SuppressLinter(
    value = {"IllegalViewModelMethod", "ViewClassInViewModel"},
    reviewer = "mstrauss",
    justification = "legacy file"
)
public class AboutDialogViewModel extends DialogViewModel {

    private static final Logger log = LoggerFactory.getLogger(AboutDialogViewModel.class);

    static final String REFRESH_DIALOG_EVENT = "REFRESH_ABOUT_DIALOG";

    private static final String AUTO_UPDATE_ON = "com.intel.missioncontrol.ui.dialogs.AboutDialogView.autoUpdateOn";
    private static final String AUTO_UPDATE_OFF = "com.intel.missioncontrol.ui.dialogs.AboutDialogView.autoUpdateOff";
    private static final String UPDATE_AVAILABLE =
        "com.intel.missioncontrol.ui.dialogs.AboutDialogView.version.updateAvailable";
    private static final String UP_TO_DATE = "com.intel.missioncontrol.ui.dialogs.AboutDialogView.version.upToDate";
    private static final String UNKNOWN = "com.intel.missioncontrol.ui.dialogs.AboutDialogView.version.unknown";
    // licensetab
    private static final String INSTALL_CUSTOM_LICENSE =
        "com.intel.missioncontrol.ui.dialogs.AboutDialogView.installCustomLicense";
    private static final String REMOVE_CUSTOM_LICENSE =
        "com.intel.missioncontrol.ui.dialogs.AboutDialogView.removeCustomLicense";
    private static final String LIFETIME_LICENSE = "never";
    private static final String MAX_USER_LEVEL = "com.intel.missioncontrol.ui.dialogs.AboutDialogView.maxUserLevel";
    private static final String IS_BETA_TESTING = "com.intel.missioncontrol.ui.dialogs.AboutDialogView.isBetaTesting";
    private static final String EDITIONS = "com.intel.missioncontrol.ui.dialogs.AboutDialogView.editions";
    private static final String BUILDIN_LICENSE_VERSION =
        "com.intel.missioncontrol.ui.dialogs.AboutDialogView.version.license.buildInVersion";
    ////// licensetab end
    private static final String ABOUT_DIALOG_VIEW_COPYRIGHT =
        "com.intel.missioncontrol.ui.dialogs.AboutDialogView.copyright";

    private StringProperty copyright = new SimpleStringProperty();
    private StringProperty contactEmailAddress = new SimpleStringProperty();
    private StringProperty lastCheckedDate = new SimpleStringProperty();
    private StringProperty updateAvailableText = new SimpleStringProperty();
    private BooleanProperty updateAvailable = new SimpleBooleanProperty();
    private BooleanProperty checkForUpdatesInProgress = new SimpleBooleanProperty();
    // licensetab
    private StringProperty serialNumber = new SimpleStringProperty();
    private StringProperty licenseVersion = new SimpleStringProperty();
    private StringProperty registeredTo = new SimpleStringProperty();
    private StringProperty maxUserLevel = new SimpleStringProperty();
    private StringProperty isBetaTesting = new SimpleStringProperty();
    private StringProperty editions = new SimpleStringProperty();
    private BooleanProperty isBuildInLicense = new SimpleBooleanProperty();
    private StringProperty licCurrentVersion = new SimpleStringProperty("v 1.0.123");
    private StringProperty licCurrentBuild = new SimpleStringProperty("build 20171001-142335");
    private StringProperty licLastCheckedDate = new SimpleStringProperty();
    private StringProperty licUpToDateText = new SimpleStringProperty();
    private StringProperty licUpdateAvailableText = new SimpleStringProperty();
    private BooleanProperty licUpdateAvailable = new SimpleBooleanProperty();
    private BooleanProperty licCheckForUpdatesInProgress = new SimpleBooleanProperty();
    //// licensetab end
    private ListProperty<LicenseItem> availableThirdPartyLicenses =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private ObjectProperty<LicenseItem> selectedThirdPartyLicense = new SimpleObjectProperty<>();
    private StringProperty legalNotice = new SimpleStringProperty();

    private final ILanguageHelper languageHelper;
    private final GeneralSettings generalSettings;

    private DateTimeFormatter displayTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm");

    @InjectScope
    private MainScope mainScope;

    private final INavigationService navigationService;
    private final IUpdateManager updateManager;
    private final IPathProvider pathProvider;
    private final ILicenceManager licenceManager;

    @Inject
    public AboutDialogViewModel(
            INavigationService navigationService,
            ILanguageHelper languageHelper,
            ISettingsManager settingsManager,
            IPathProvider pathProvider,
            IUpdateManager updateManager,
            ILicenceManager licenceManager) {
        this.navigationService = navigationService;
        this.languageHelper = languageHelper;
        this.updateManager = updateManager;
        this.pathProvider = pathProvider;
        this.licenceManager = licenceManager;
        generalSettings = settingsManager.getSection(GeneralSettings.class);
        copyright.setValue(languageHelper.getString(ABOUT_DIALOG_VIEW_COPYRIGHT, LocalDateTime.now().getYear()));
        contactEmailAddress.set(UpdateURL.getSupportEmail());

        setLastChecked(updateManager.getLastCheckedDateTime());
        licenceManager.activeLicenceProperty().addListener((observable, oldValue, newValue) -> resetLicenseTab());
        resetLicenseTab();

        availableThirdPartyLicenses.addAll(enumerateThirdPartyLicenses());
        selectedThirdPartyLicenseProperty().set(availableThirdPartyLicenses.get(0));
        legalNoticeProperty().set(getOutboundLicence());
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        mainScope.subscribe(REFRESH_DIALOG_EVENT, (key, payload) -> publish(REFRESH_DIALOG_EVENT));
    }

    public StringProperty lastCheckedDateProperty() {
        return lastCheckedDate;
    }

    public ReadOnlyStringProperty contactEmailAddressProperty() {
        return contactEmailAddress;
    }

    // licensetab
    private void resetLicenseTab() {
        Licence licence = licenceManager.getActiveLicence();
        licCurrentVersion.setValue("v " + licence == null ? null : licence.getLicenceId());
        setLicenseLastChecked(updateManager.getLastCheckedDateTime());
        contactEmailAddress.set(UpdateURL.getSupportEmail());
        if (licence != null && licence.getLicenceId() != null) {
            serialNumber.set(licence.getLicenceId());
            isBuildInLicense.set(licence.isBuildInLicence());
            if (isBuildInLicenseToProperty().get()) {
                licenseVersion.set(languageHelper.getString(BUILDIN_LICENSE_VERSION));
            } else {
                licenseVersion.set(licence.getHumanReadableVersion());
            }

            maxUserLevel.set(
                languageHelper.getString(MAX_USER_LEVEL)
                    + " "
                    + languageHelper.toFriendlyName(licence.getMaxOperationLevel()));
            isBetaTesting.set(
                (licence.isBetaTesting()
                    ? languageHelper.getString(IS_BETA_TESTING)
                        + " "
                        + languageHelper.getString("com.intel.missioncontrol.ui.toggle.Yes")
                    : " "));

            editions.set(languageHelper.getString(EDITIONS) + " " + getEditionList(licence));

            Set<String> localMacs = MACaddrSource.getMACs();
            AllowedUser user = licence.detectMatchingUser(localMacs);
            if (user != null) {
                registeredTo.set(user.getDisplayName());
            }
        }
    }

    private String getEditionList(Licence licence) {
        List<String> editions = new ArrayList<>();
        if (licence.isFalconEdition()) {
            editions.add(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.dialogs.AboutDialogView.falcon", updateManager.getCurrentMajor()));
        }

        if (licence.isDJIEdition()) {
            editions.add(languageHelper.getString("com.intel.missioncontrol.ui.dialogs.AboutDialogView.dji"));
        }

        if (licence.isGrayHawkEdition()) {
            editions.add(languageHelper.getString("com.intel.missioncontrol.ui.dialogs.AboutDialogView.grayhawk"));
        }

        if (editions.isEmpty()) return "-";
        return StringUtils.join(editions, ", ");
    }

    public void updateLicenseFile(Stage dstage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(
            languageHelper.getString("com.intel.missioncontrol.ui.dialogs.LicenseTabView.openLicenseTitle"));
        fileChooser.setInitialDirectory(constructSelectDir());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MLF", "*.mlf"));

        // Move parent dialog to back
        if (dstage != null) {
            dstage.setAlwaysOnTop(false);
            dstage.toBack();
        }

        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.toFront();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            updateLicenseFile(file);
        }
    }

    private File constructSelectDir() {
        File licenceUpdatesDir =
            new File(
                pathProvider.getUpdatesDirectory().toFile().getPath()
                    + File.separator
                    + EnumUpdateTargets.LICENCE
                    + File.separator
                    + licenceManager.getActiveLicence().getLicenceId());

        return licenceUpdatesDir.exists() ? licenceUpdatesDir : FileHelper.getDownloadsFolder();
    }

    public void updateLicenseFile(File licenseFile) throws Exception {
        licenceManager.registerLicence(licenseFile);

        Licence licence = licenceManager.getActiveLicence();
        if (generalSettings.getOperationLevel().compareTo(licence.getMaxOperationLevel()) > 0) {
            generalSettings.setOperationLevel(licence == null ? OperationLevel.USER : licence.getMaxOperationLevel());
        }
    }

    public void installOrRemoveLicense(Stage dstage) throws Exception {
        if (isBuildInLicenseToProperty().get()) { // its default license, then open the file chooser
            updateLicenseFile(dstage);
        } else {
            licenceManager.resetToDefaultLicence();
        }
    }

    public StringProperty serialNumberProperty() {
        return serialNumber;
    }

    public StringProperty licenseVersionProperty() {
        return licenseVersion;
    }

    public StringProperty registeredToProperty() {
        return registeredTo;
    }

    public StringProperty maxUserLevelToProperty() {
        return maxUserLevel;
    }

    public StringProperty isBetaTestingToProperty() {
        return isBetaTesting;
    }

    public BooleanProperty isBuildInLicenseToProperty() {
        return isBuildInLicense;
    }

    public StringBinding installOrRemoveLicenseProperty() {
        return Bindings.createStringBinding(
            () -> {
                if (isBuildInLicenseToProperty().get()) {
                    return languageHelper.getString(INSTALL_CUSTOM_LICENSE);
                } else {
                    return languageHelper.getString(REMOVE_CUSTOM_LICENSE);
                }
            },
            isBuildInLicenseToProperty());
    }

    public BooleanProperty licUpdateAvailableProperty() {
        return licUpdateAvailable;
    }

    private void setLicenseLastChecked(LocalDateTime date) {
        if (date == null) {
            licLastCheckedDate.set(languageHelper.getString(UNKNOWN));
            licUpToDateText.set(languageHelper.getString(UNKNOWN));
            licUpdateAvailable.setValue(false);
            return;
        }

        licLastCheckedDate.set(date.format(displayTimeFormatter));
        if (updateManager.isUpdateAvailable(EnumUpdateTargets.LICENCE)) {
            AvailableUpdate update = updateManager.getAvailableUpdatesMap().get(EnumUpdateTargets.LICENCE);
            if (update != null)
                licUpdateAvailableText.set(languageHelper.getString(UPDATE_AVAILABLE, update.getVersion()));
            licUpdateAvailable.setValue(true);
        } else {
            licUpdateAvailableText.set(languageHelper.getString(UP_TO_DATE));
            licUpdateAvailable.setValue(false);
        }
    }

    public StringProperty licCurrentVersionProperty() {
        return licCurrentVersion;
    }

    public StringProperty licLastCheckedDateProperty() {
        return licLastCheckedDate;
    }

    public StringProperty licUpdateAvailableTextProperty() {
        return licUpdateAvailableText;
    }

    public StringProperty licUpToUpdateTextProperty() {
        return licUpToDateText;
    }

    public BooleanProperty checkForLicenseUpdatesInProgressProperty() {
        return licCheckForUpdatesInProgress;
    }

    Command getCheckForLicenseUpdatesCommand() {
        var checkForLicenseUpdatesCommand =
            new DelegateCommand(
                () -> {
                    Dispatcher background = Dispatcher.background();
                    background.run(
                        () -> {
                            if (!updateManager.isAnyUpdateAvailable()) {
                                log.debug("There are no new updates available");
                                Dispatcher platform = Dispatcher.platform();
                                platform.run(
                                    () -> {
                                        setLastChecked(updateManager.getLastCheckedDateTime());
                                        setLicenseLastChecked(updateManager.getLastCheckedDateTime());
                                    });
                            } else {
                                Dispatcher platform = Dispatcher.platform();
                                platform.run(
                                    () -> {
                                        log.debug("There are new updates available");
                                        setLastChecked(updateManager.getLastCheckedDateTime());
                                        setLicenseLastChecked(updateManager.getLastCheckedDateTime());
                                    });
                            }
                        });
                });

        checkForLicenseUpdatesInProgressProperty().bind(checkForLicenseUpdatesCommand.runningProperty());
        return checkForLicenseUpdatesCommand;
    }
    //////////////////// licensetab end

    public StringProperty updateAvailableTextProperty() {
        return updateAvailableText;
    }

    public BooleanProperty updateAvailableProperty() {
        return updateAvailable;
    }

    public StringProperty copyrightProperty() {
        return copyright;
    }

    public StringProperty legalNoticeProperty() {
        return legalNotice;
    }

    public StringBinding autoUpdateStatusProperty() {
        return Bindings.createStringBinding(
            () -> {
                if (generalSettings.softwareUpdateEnabledProperty().get()) {
                    return languageHelper.getString(AUTO_UPDATE_ON);
                } else {
                    return languageHelper.getString(AUTO_UPDATE_OFF);
                }
            },
            generalSettings.softwareUpdateEnabledProperty());
    }

    public BooleanProperty checkForUpdatesInProgressProperty() {
        return checkForUpdatesInProgress;
    }

    public ReadOnlyListProperty<LicenseItem> availableThirdPartyLicensesProperty() {
        return availableThirdPartyLicenses;
    }

    public ObjectProperty<LicenseItem> selectedThirdPartyLicenseProperty() {
        return selectedThirdPartyLicense;
    }

    void openChangeLog() {
        try {
            Desktop.getDesktop().browse(Application.CHANGELOG_URI);
        } catch (IOException e) {
            log.error("Could not open changelog", e);
        }
    }

    private void setLastChecked(LocalDateTime date) {
        if (date == null) {
            lastCheckedDate.set(languageHelper.getString(UNKNOWN));
            updateAvailableText.set(languageHelper.getString(UNKNOWN));
            updateAvailable.setValue(false);
            return;
        }

        lastCheckedDate.set(date.format(displayTimeFormatter));

        if (updateManager.isUpdateAvailable(EnumUpdateTargets.GUI)) {
            AvailableUpdate update = updateManager.getAvailableUpdatesMap().get(EnumUpdateTargets.GUI);
            updateAvailableText.set(languageHelper.getString(UPDATE_AVAILABLE, update.getVersion()));
            updateAvailable.setValue(true);
        } else {
            updateAvailableText.set(languageHelper.getString(UP_TO_DATE));
            updateAvailable.setValue(false);
        }
    }

    void openContactsEmail() {
        try {
            Desktop.getDesktop().mail(new URI("mailto:" + contactEmailAddress.get()));
            getCloseCommand().execute();
        } catch (IOException e) {
            log.error("Could not send email!", e);
        } catch (URISyntaxException e) {
            log.error("Could not build URI!", e);
        }
    }

    void goToSettings() {
        navigationService.navigateTo(SettingsPage.GENERAL);
        getCloseCommand().execute();
    }

    void openRevertDialog() {
        Dispatcher.platform().runLater(updateManager::showRevertDialog);
        getCloseCommand().execute();
    }

    private List<LicenseItem> enumerateThirdPartyLicenses() {
        final String licenseDir = "/com/intel/missioncontrol/licensesInbound/";

        try {
            List<LicenseItem> list = new ArrayList<>();
            Stream<Path> walk = Files.walk(Paths.get(getClass().getResource(licenseDir).toURI()), 1);
            Iterator<Path> it = walk.iterator();
            while (it.hasNext()) {
                Path filePath = it.next();
                if (Files.isRegularFile(filePath)) {
                    String fileName = com.google.common.io.Files.getNameWithoutExtension(filePath.toString());
                    list.add(
                        new LicenseItem(fileName, new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8)));
                }
            }

            list.sort(Comparator.comparing(LicenseItem::getName, String.CASE_INSENSITIVE_ORDER));
            return list;
        } catch (IOException | URISyntaxException e) {
            return Collections.emptyList();
        }
    }

    private String getOutboundLicence() {
        String filefullname = "/com/intel/missioncontrol/licensesOutbound/Open Mission Control.txt";
        URL resourceUrl = getClass().getResource(filefullname);
        if (resourceUrl != null) {
            try {
                byte[] resourceAsBytes = Files.readAllBytes(Paths.get(resourceUrl.toURI()));
                return new String(resourceAsBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return "";
            }
        }

        return "";
    }

    Command getCheckForUpdatesCommand() {
        DelegateCommand checkForUpdatesCommand =
            new DelegateCommand(
                () -> {
                    Dispatcher dispatcher = Dispatcher.background();
                    dispatcher.run(
                        () -> {
                            if (!updateManager.isAnyUpdateAvailable()) {
                                log.debug("There are no new updates available");
                            } else {
                                Dispatcher.platform().run(() -> setLastChecked(updateManager.getLastCheckedDateTime()));
                            }
                        });
                });

        checkForUpdatesInProgressProperty().bind(checkForUpdatesCommand.runningProperty());
        return checkForUpdatesCommand;
    }

    Command getCheckForUpdatesAndShowDialogCommand() {
        var checkForUpdatesCommand =
            new DelegateCommand(
                () -> {
                    Dispatcher dispatcher = Dispatcher.background();
                    dispatcher.run(
                        () -> {
                            if (!updateManager.isAnyUpdateAvailable()) {
                                log.debug("There are no new updates available");
                            } else if (!updateManager.getAvailableUpdatesMap().isEmpty()) {
                                Dispatcher.platform().run(() -> updateManager.showDialogNow());
                            }

                            Dispatcher.platform().run(() -> getCloseCommand().execute());
                        });
                });

        return checkForUpdatesCommand;
    }

    Command getCheckForLicenseUpdatesAndShowDialogCommand() {
        var checkForLicenseUpdatesCommand =
            new DelegateCommand(
                () -> {
                    Dispatcher dispatcher = Dispatcher.background();
                    dispatcher.run(
                        () -> {
                            if (!updateManager.isAnyUpdateAvailable()) {
                                log.debug("There are no new updates available");
                            } else if (!updateManager.getAvailableUpdatesMap().isEmpty()) {
                                Dispatcher.platform().run(() -> updateManager.showDialogNow());
                            }

                            Dispatcher.platform().run(() -> getCloseCommand().execute());
                        });
                });

        return checkForLicenseUpdatesCommand;
    }

    public StringProperty editionsProperty() {
        return editions;
    }

    public String getCurrentGitBranch() {
        return ManifestInfo.getBranchName();
    }

    public String getBuildCommitId() {
        return ManifestInfo.getCommitHash();
    }

    public String getCurrentMajor() {
        return ManifestInfo.getVersion();
    }

    public String getBuildNumber() {
        return ManifestInfo.getCommitHash();
    }
}
