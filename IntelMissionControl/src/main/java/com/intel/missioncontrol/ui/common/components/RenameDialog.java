/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.components;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.SystemInformation;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.util.Optional;
import java.util.function.Function;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

/** Utility class which shows up a rename dialog. */
@Deprecated
public class RenameDialog {

    public static Optional<String> requestNewMissionName(
            String title,
            String name,
            String oldName,
            ILanguageHelper languageHelper,
            Function<String, Boolean> missionNameValidator) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        // Create the button Rename and add it to the dialog box.
        ButtonType btnRename =
            new ButtonType(languageHelper.getString("renameDialog.rename"), ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel =
            new ButtonType(
                oldName.equals(Mission.DEMO_MISSION_NAME)
                    ? languageHelper.getString("renameDialog.close")
                    : languageHelper.getString("renameDialog.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().setAll(btnRename, btnCancel);
        Button btnCancelStyles = (Button)dialog.getDialogPane().lookupButton(btnCancel);
        btnCancelStyles.getStyleClass().add("secondary-button");

        Button btnRenameStyles = (Button)dialog.getDialogPane().lookupButton(btnRename);
        btnRenameStyles.getStyleClass().add("primary-button");

        Node nodeBtnRename = dialog.getDialogPane().lookupButton(btnRename);
        nodeBtnRename.setDisable(true);

        // Create the textfield, set the text to the old Mission name and set
        // the style class.

        Label nameLabel = new Label(name);
        TextField txtRename = new TextField();
        txtRename.setText(oldName);
        txtRename.selectAll();

        VBox box = new VBox();
        box.getStyleClass().add("content");

        box.getChildren().add(nameLabel);
        box.getChildren().add(txtRename);

        // Add a listener (with lambda expression) do the textfield
        // to activate the Rename button only when the user input is different
        // from the old Mission name.
        txtRename
            .textProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    String newName = removeIllegalChars(newValue);
                    txtRename.setText(newName);
                    nodeBtnRename.setDisable(!missionNameValidator.apply(newName));
                });

        // Dialog box: set the title, set the padding inside, set the content
        // with the grid and
        // bind the user input to the button Rename click action.

        dialog.getDialogPane().getStyleClass().add("dialog");
        dialog.getDialogPane().setContent(box);
        txtRename.requestFocus();

        dialog.setResultConverter(
            dialogButton -> {
                if (dialogButton == btnRename) {
                    return txtRename.getText();
                }

                return null;
            });

        dialog.initOwner(WindowHelper.getPrimaryStage());

        setGenericStyle(dialog);

        return dialog.showAndWait();
    }

    private static void setGenericStyle(Dialog dialog) {
        dialog.initStyle(StageStyle.UTILITY);
        ISettingsManager settingsManager = DependencyInjector.getInstance().getInstanceOf(ISettingsManager.class);
        dialog.getDialogPane()
            .getStylesheets()
            .addAll(settingsManager.getSection(GeneralSettings.class).getTheme().getStylesheets());

        dialog.getDialogPane()
            .getStylesheets()
            .add(RenameDialog.class.getResource("/com/intel/missioncontrol/styles/controls.css").toExternalForm());
        dialog.getDialogPane()
            .getStylesheets()
            .add(
                RenameDialog.class
                    .getResource("/com/intel/missioncontrol/styles/themes/colors-light.css")
                    .toExternalForm());
        dialog.getDialogPane()
            .getStylesheets()
            .add(RenameDialog.class.getResource("/com/intel/missioncontrol/styles/dialog.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("st-dialog");

        for (ButtonType type : dialog.getDialogPane().getButtonTypes()) {
            Button btnStyles = (Button)dialog.getDialogPane().lookupButton(type);
            btnStyles.getStyleClass().clear();
            if (type.getButtonData().isDefaultButton()) {
                btnStyles.getStyleClass().add("primary-button");
            } else {
                btnStyles.getStyleClass().add("secondary-button");
            }
        }
    }
    // TODO make a list of illegal chars on other operating systems and change
    // the code to use the O.S. specific list
    public static String removeIllegalChars(String newValue) {
        String testString = "";
        if (newValue != null) {
            testString = ltrim(newValue);
        }

        int safeMargin = 50; // to ensure space for folders along the path
        if (testString.length() > SystemInformation.getMaxPath() - safeMargin) {
            testString = testString.substring(0, SystemInformation.getMaxPath() - safeMargin);
        }

        int length = testString.length();
        StringBuilder sb = new StringBuilder();

        String illegalChars = SystemInformation.windowsFilesFoldersIllegalChars();

        for (int index = 0; index < length; ++index) {
            char theChar = testString.charAt(index);
            if (illegalChars.indexOf(theChar) == -1) {
                if (theChar > 31) {
                    sb.append(theChar);
                }
            }
        }

        String name = sb.toString();
        if (name.contains("..")) {
            name = name.replaceAll("\\.\\.", ".");
        }

        return name;
    }

    private static String ltrim(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }

        return s.substring(i);
    }
}
