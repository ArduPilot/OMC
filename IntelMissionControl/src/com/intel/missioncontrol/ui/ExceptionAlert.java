/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionAlert {

    public static void showAndWait(Exception exception) {
        FXMLLoader loader = new FXMLLoader(ExceptionAlert.class.getResource("ExceptionAlert.fxml"));

        try (StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter)) {
            final Region view = loader.load();
            ExceptionAlert alert = loader.getController();
            Stage stage = new Stage();
            Image appIcon =
                new Image(
                    ExceptionAlert.class
                        .getResource("/com/intel/missioncontrol/app-icon/mission-control-icon.png")
                        .toExternalForm());
            stage.getIcons().add(appIcon);
            alert.stage = stage;
            exception.printStackTrace(printWriter);
            alert.textArea.setText(stringWriter.toString());
            stage.setScene(new Scene(view));
            stage.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private TextArea textArea;

    private Stage stage;

    @FXML
    private void copyToClipboardClicked() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), null);
    }

    @FXML
    private void closeClicked() {
        stage.close();
    }

}
