/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.update;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.desktop.main.core.Application;
import java.awt.Desktop;
import java.io.IOException;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UpdateItemView extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(UpdateItemView.class);

    private final CheckBox checkbox;
    private final ProgressBar progressBar;
    private static ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);
    private final Text progressText;
    private final String downloadingString;
    private final String downloadCompleteString;

    Button getSkipButton() {
        return skipButton;
    }

    private final Button skipButton;

    UpdateItemView(String target, String version, String currentVersion, Boolean isFirstItem) {
        String updateAvailableString =
            languageHelper.getString(
                "com.intel.missioncontrol.ui.update.UpdateView.updateAvailable", target, currentVersion);
        String newVersionString =
            languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateView.newVersion", version);
        downloadingString = languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateView.downloading");
        downloadCompleteString =
            languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateView.downloadComplete");
        String hyperlinkText = languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateView.hyperlink");
        String tooltipText = languageHelper.getString("com.intel.missioncontrol.ui.update.UpdateView.tooltip");

        setId(target);

        if (!isFirstItem) {
            setTop(new Separator());
        }

        checkbox = new CheckBox();
        checkbox.setSelected(true);
        checkbox.setStyle("-fx-padding: 0 0.5em 0 0");

        setAlignment(checkbox, Pos.CENTER_LEFT);
        setLeft(checkbox);

        Text oldVersionText = new Text(updateAvailableString);
        Text newVersionText = new Text(newVersionString);
        newVersionText.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);

        Hyperlink hyperlink = new Hyperlink(hyperlinkText);
        hyperlink.setStyle("-fx-padding: 0 0 0 0.5em");

        hyperlink.setOnMouseClicked(
            event -> {
                try {
                    Desktop.getDesktop().browse(Application.CHANGELOG_URI);
                } catch (IOException e) {
                    log.error("Could not open change log!", e);
                }
            });

        VBox versionInfo = new VBox(oldVersionText, new FlowPane(newVersionText, hyperlink));

        setCenter(versionInfo);
        versionInfo.setStyle("-fx-padding: 1.4em 0 0 0");

        skipButton = new Button();
        skipButton.setBackground(Background.EMPTY);
        skipButton.setTooltip(new Tooltip(tooltipText));
        skipButton.getStyleClass().addAll("large-transparent-icon-button", "icon-close");

        setRight(skipButton);

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);

        progressText = new Text();
        progressText.setVisible(false);
        setBottom(new VBox(new HBox(progressBar, progressText)));
    }

    CheckBox getCheckbox() {
        return this.checkbox;
    }

    void onDownloadStarted() {
        progressBar.setProgress(0);
        progressBar.setVisible(true);
        progressText.setText(downloadingString);
        progressText.setVisible(true);
        skipButton.setDisable(true);
    }

    void onDownloadInterrupted() {
        progressBar.setVisible(false);
        progressText.setVisible(false);
        skipButton.setDisable(false);
    }

    void onUpdateProgress(long currentFileSize, long contentLength) {
        progressBar.setProgress(1.0 * currentFileSize / contentLength);
    }

    void onDownloadComplete() {
        progressBar.setProgress(1.0);
        progressText.setText(downloadCompleteString);
        skipButton.setDisable(false);
    }
}
