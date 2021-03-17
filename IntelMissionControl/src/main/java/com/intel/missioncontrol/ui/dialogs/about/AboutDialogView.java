/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.about;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import javafx.animation.Animation;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AboutDialogView extends DialogView<AboutDialogViewModel> {

    private static final int ICON_SIZE = 16;

    @InjectViewModel
    private AboutDialogViewModel viewModel;

    @FXML
    private VBox layoutRoot;

    @FXML
    private Label currentVersion;

    @FXML
    private Label currentBuild;

    @FXML
    private Tooltip currentBuildTooltip;

    @FXML
    private Label autoUpdate;

    @FXML
    private TabPane tabPane;

    @FXML
    private Label copyrightLabel;

    @FXML
    private Hyperlink contactEmailLink;

    @FXML
    private Label lastCheckedDateLabel;

    @FXML
    private Label updateAvailableLabel;

    @FXML
    public Hyperlink viewChangesLink;

    @FXML
    private Button checkForUpdatesButton;

    @FXML
    public Button installUpdateButton;

    @FXML
    public Label orLabel;

    // licensetab
    @FXML
    private Label licenseVersion;

    @FXML
    private Hyperlink installOrRemoveLic;

    @FXML
    private Label serialNumber;

    @FXML
    private Label maxUserLevel;

    @FXML
    private Label isBetaTesting;

    @FXML
    private Label editions;

    @FXML
    private Label licAutoUpdate;

    @FXML
    private Label licLastCheckedDateLabel;

    @FXML
    private Hyperlink licViewChanges;

    @FXML
    private Label licUpdateAvailableLabel;

    @FXML
    private Button licCheckForUpdatesButton;

    @FXML
    private Button licInstallOnlineUpdate;

    @FXML
    private Hyperlink licInstallLocalUpdate;

    @FXML
    private VBox serialNumberVBox;

    @FXML
    private VBox bottomVBox;
    //////////////// LicenseTab end

    @FXML
    private ComboBox<LicenseItem> thirdPartyLicensesComboBox;

    @FXML
    private TextArea thirdPartyLicenseTextArea;

    @FXML
    private TextArea legalNoticeTextArea;

    @Inject
    private ILanguageHelper languageHelper;

    private Animation checkForUpdatesButtonAnimation;

    @Override
    protected void initializeView() {
        super.initializeView();
        currentVersion.textProperty().set("v" + viewModel.getCurrentMajor());
        currentBuild.textProperty().set("build " + viewModel.getBuildNumber());
        currentBuildTooltip.setText(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.AboutDialogView.currentBuildTooltip",
                viewModel.getCurrentGitBranch(),
                viewModel.getBuildCommitId()));

        autoUpdate.textProperty().bind(viewModel.autoUpdateStatusProperty());
        copyrightLabel.textProperty().bind(viewModel.copyrightProperty());
        contactEmailLink.textProperty().bind(viewModel.contactEmailAddressProperty());

        lastCheckedDateLabel.textProperty().bind(viewModel.lastCheckedDateProperty());
        lastCheckedDateLabel.disableProperty().bind(viewModel.checkForUpdatesInProgressProperty());

        updateAvailableLabel.textProperty().bind(viewModel.updateAvailableTextProperty());
        updateAvailableLabel.disableProperty().bind(viewModel.checkForUpdatesInProgressProperty());

        viewModel.subscribe(AboutDialogViewModel.REFRESH_DIALOG_EVENT, (key, payload) -> refresh());

        viewChangesLink.visibleProperty().bind(viewModel.updateAvailableProperty());
        viewChangesLink.managedProperty().bind(viewChangesLink.visibleProperty());

        installUpdateButton.visibleProperty().bind(viewModel.updateAvailableProperty());
        installUpdateButton.managedProperty().bind(installUpdateButton.visibleProperty());

        // licenseTab
        serialNumber.textProperty().bind(viewModel.serialNumberProperty());
        licenseVersion.textProperty().bind(viewModel.licenseVersionProperty());
        maxUserLevel.textProperty().bind(viewModel.maxUserLevelToProperty());
        isBetaTesting.textProperty().bind(viewModel.isBetaTestingToProperty());
        editions.textProperty().bind(viewModel.editionsProperty());
        licAutoUpdate.textProperty().bind(viewModel.autoUpdateStatusProperty());
        licLastCheckedDateLabel.textProperty().bind(viewModel.licLastCheckedDateProperty());
        licUpdateAvailableLabel.textProperty().bind(viewModel.licUpdateAvailableTextProperty());
        licViewChanges.visibleProperty().bind(viewModel.licUpdateAvailableProperty());
        licInstallOnlineUpdate.visibleProperty().bind(viewModel.licUpdateAvailableProperty());
        licInstallOnlineUpdate.managedProperty().bind(viewModel.licUpdateAvailableProperty());
        // serialNumberVBox.visibleProperty().bind(viewModel.isBuildInLicenseToProperty().not());
        bottomVBox.visibleProperty().bind(viewModel.isBuildInLicenseToProperty().not());
        installOrRemoveLic.textProperty().bind(viewModel.installOrRemoveLicenseProperty());
        serialNumberVBox.managedProperty().bind(serialNumberVBox.visibleProperty());
        bottomVBox.managedProperty().bind(bottomVBox.visibleProperty());
        ////////// LicenseTab end

        thirdPartyLicensesComboBox.setItems(viewModel.availableThirdPartyLicensesProperty());
        thirdPartyLicensesComboBox.valueProperty().bindBidirectional(viewModel.selectedThirdPartyLicenseProperty());
        thirdPartyLicenseTextArea
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> viewModel.selectedThirdPartyLicenseProperty().get().getText(),
                    viewModel.selectedThirdPartyLicenseProperty()));

        try (InputStream svgInput =
            Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(
                    "/com/intel/missioncontrol/icons/icon_refresh(fill=theme-button-text-color).svg")) {
            Image iconImage = new Image(svgInput, ICON_SIZE, ICON_SIZE, false, false);
            checkForUpdatesButton.setGraphic(new ImageView(iconImage));
            licCheckForUpdatesButton.setGraphic(new ImageView(iconImage));
        } catch (IOException e) {
            Debug.getLog().log(Debug.WARNING, "AboutDialogView icon_refresh.svg: ", e);
        }

        viewModel
            .checkForUpdatesInProgressProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        if (checkForUpdatesButtonAnimation == null) {
                            checkForUpdatesButtonAnimation =
                                Animations.forButtonGraphicRotation(
                                    checkForUpdatesButton, Animations.ROTATION_CLOCK_WISE);
                        }

                        checkForUpdatesButtonAnimation.play();
                    } else {
                        if (checkForUpdatesButtonAnimation != null) {
                            checkForUpdatesButtonAnimation.stop();
                            checkForUpdatesButtonAnimation = null;
                        }
                    }
                });

        viewModel
            .checkForLicenseUpdatesInProgressProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        if (checkForUpdatesButtonAnimation == null) {
                            checkForUpdatesButtonAnimation =
                                Animations.forButtonGraphicRotation(
                                    licCheckForUpdatesButton, Animations.ROTATION_CLOCK_WISE);
                        }

                        checkForUpdatesButtonAnimation.play();
                    } else {
                        if (checkForUpdatesButtonAnimation != null) {
                            checkForUpdatesButtonAnimation.stop();
                            checkForUpdatesButtonAnimation = null;
                        }
                    }
                });
        legalNoticeTextArea
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> viewModel.legalNoticeProperty().get(), viewModel.legalNoticeProperty()));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected AboutDialogViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void onViewChangesClicked() {
        viewModel.openChangeLog();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString("com.intel.missioncontrol.ui.dialogs.AboutDialogView.title"));
    }

    @FXML
    public void onContactEmailClicked() {
        viewModel.openContactsEmail();
    }

    @FXML
    public void onChangeInSettingsClicked() {
        viewModel.goToSettings();
    }

    public void refresh() {
        tabPane.getSelectionModel().select(0);
    }

    @FXML
    public void onInstallUpdateClicked() {
        viewModel.getCheckForUpdatesAndShowDialogCommand().execute();
    }

    @FXML
    public void onRevertVersionClicked() {
        viewModel.openRevertDialog();
    }

    @FXML
    public void onCheckForUpdatesButtonClicked() {
        viewModel.getCheckForUpdatesCommand().execute();
    }

    @FXML
    public void onCheckForLicenseUpdatesButtonClicked() {
        viewModel.getCheckForLicenseUpdatesCommand().execute();
    }

    @FXML
    public void installOrRemoveLicense() throws Exception {
        Stage dstage = (Stage)layoutRoot.getScene().getWindow();
        viewModel.installOrRemoveLicense(dstage);
    }

    @FXML
    public void onInstallOnlineUpdateClicked() {
        viewModel.getCheckForLicenseUpdatesAndShowDialogCommand().execute();
    }

    @FXML
    public void onInstallLocalUpdateClicked() throws Exception {
        Stage dstage = (Stage)layoutRoot.getScene().getWindow();
        viewModel.updateLicenseFile(dstage);
    }

    @FXML
    public void updateLicenseFile() throws Exception {
        // Move parent dialog to back
        Stage dstage = (Stage)layoutRoot.getScene().getWindow();
        viewModel.updateLicenseFile(dstage);
    }

    @FXML
    public void onLicenseUpdateAvailabelClicked() {
        viewModel.openChangeLog();
    }

    @FXML
    public void onBuildClicked(MouseEvent mouseEvent) {
        Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(
                new StringSelection(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.dialogs.AboutDialogView.currentBuildToolkit",
                        viewModel.getCurrentMajor(),
                        viewModel.getBuildNumber(),
                        viewModel.getCurrentGitBranch(),
                        viewModel.getBuildCommitId())),
                null);
    }
}
