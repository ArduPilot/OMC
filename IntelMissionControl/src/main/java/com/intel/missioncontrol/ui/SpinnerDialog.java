/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Similar to {@link TextInputDialog} but with spinner */
@SuppressWarnings("restriction")
@Deprecated
public class SpinnerDialog<T> extends Dialog<T> {

    private static final String BUTTON_BAR = ".button-bar";

    private static final String CSS_CONTROLS = "/com/intel/missioncontrol/styles/controls.css";
    private static final String CSS_COLORS_LIGHT = "/com/intel/missioncontrol/styles/themes/colors-light.css";
    private static final String CSS_DIALOG = "/com/intel/missioncontrol/styles/dialog.css";

    private static final String STYLE_GRID = "grid";
    private static final String STYLE_CONTENT = "content";
    private static final String STYLE_TEXT_INPUT_DIALOG = "text-input-dialog";
        private static final String STYLE_PRIMARY_BUTTON = "primary-button";
    private static final String STYLE_SECONDARY_BUTTON = "secondary-button";

    private static final String KEY_DIALOG_CONFIRM_HEADER = "Dialog.confirm.header";
    private static final String KEY_DIALOG_CONFIRM_TITLE = "Dialog.confirm.title";

    private static final double GRID_HGAP_VALUE = ScaleHelper.emsToPixels(0.83);

    private final GridPane grid;
    private final Label label;
    private final Spinner<T> spinner;

    public SpinnerDialog(@NamedArg("valueFactory") SpinnerValueFactory<T> valueFactory) {
        Expect.notNull(valueFactory, "valueFactory");

        final DialogPane dialogPane = getDialogPane();

        spinner = createSpinner(valueFactory);

        label = createContentLabel(dialogPane.getContentText());
        label.textProperty().bind(dialogPane.contentTextProperty());

        grid = createGrid();

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(ControlResources.getString(KEY_DIALOG_CONFIRM_TITLE));

        initDialogPane(dialogPane);
        initButtons(dialogPane);
        updateGrid();

        setResultConverter(
            (dialogButton) -> {
                ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
                return data == ButtonData.OK_DONE ? spinner.getValueFactory().getValue() : null;
            });
    }

    private void initDialogPane(final DialogPane dialogPane) {
        dialogPane.setHeaderText(ControlResources.getString(KEY_DIALOG_CONFIRM_HEADER));

        dialogPane.getStylesheets().add(CSS_DIALOG);
        dialogPane.getStylesheets().add(CSS_CONTROLS);
        dialogPane.getStylesheets().add(CSS_COLORS_LIGHT);

        dialogPane.getStyleClass().add(STYLE_TEXT_INPUT_DIALOG);
    }

    private void initButtons(final DialogPane dialogPane) {
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button btnOk = (Button)dialogPane.lookupButton(ButtonType.OK);
        btnOk.getStyleClass().add(STYLE_PRIMARY_BUTTON);
        Button btnCancel = (Button)dialogPane.lookupButton(ButtonType.CANCEL);
        btnCancel.getStyleClass().add(STYLE_SECONDARY_BUTTON);

        spinner.addEventHandler(
            KeyEvent.KEY_RELEASED,
            event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    btnOk.fire();
                }
            });
    }

    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(label, 0, 0);
        grid.add(spinner, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> spinner.requestFocus());
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(GRID_HGAP_VALUE);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.getStyleClass().add(STYLE_GRID);
        return grid;
    }

    private Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add(STYLE_CONTENT);
        label.setWrapText(true);
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        return label;
    }

    private Spinner<T> createSpinner(SpinnerValueFactory<T> valueFactory) {
        Spinner<T> spinner = new AutoCommitSpinner<T>();
        spinner.setValueFactory(valueFactory);
        spinner.getEditor().setTextFormatter(new TextFormatter<>(valueFactory.getConverter(), valueFactory.getValue()));
        spinner.setEditable(true);

        GridPane.setHgrow(spinner, Priority.ALWAYS);
        GridPane.setFillWidth(spinner, true);

        return spinner;
    }

}
