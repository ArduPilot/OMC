/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ManualControlsDialogView extends DialogView<ManualControlsDialogViewModel> {

    private static final String ICON_UP = "/com/intel/missioncontrol/icons/icon_fly_up.svg";
    private static final String ICON_DOWN = "/com/intel/missioncontrol/icons/icon_fly_down.svg";
    private static final String ICON_LTURN = "/com/intel/missioncontrol/icons/icon_rotate_ccw.svg";
    private static final String ICON_RTURN = "/com/intel/missioncontrol/icons/icon_rotate_cw.svg";

    private final LanguageHelper languageHelper;

    @FXML
    private VBox rootlayout;

    @FXML
    private Button upButton;

    @FXML
    private Button lTurnButton;

    @FXML
    private Button rTurnButton;

    @FXML
    private Button downButton;

    @FXML
    private CheckBox showKBShortcutsCheckBox;

    @InjectViewModel
    private ManualControlsDialogViewModel viewModel;

    private Image upIcon;
    private Image downIcon;
    private Image lTurnIcon;
    private Image rTurnIcon;

    @Inject
    public ManualControlsDialogView(LanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightchecks.ManualControlsDialogView.title"));
    }

    @Override
    protected Parent getRootNode() {
        return rootlayout;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        updateTextNGraph(false);

        rootlayout.addEventHandler(
            KeyEvent.KEY_RELEASED,
            event -> {
                switch (event.getCode()) {
                case Q:
                    viewModel.getUpCommand().execute();
                    break;
                case A:
                    viewModel.getDownCommand().execute();
                    break;
                case Z:
                    viewModel.getlTurnCommand().execute();
                    break;
                case X:
                    viewModel.getrTurnCommand().execute();
                    break;
                }
            });
    }

    public void onUpButtonClicked(ActionEvent actionEvent) {
        viewModel.getUpCommand().execute();
    }

    public void onLTurnButtonClicked(ActionEvent actionEvent) {
        viewModel.getlTurnCommand().execute();
    }

    public void onForwardButtonClicked(ActionEvent actionEvent) {
        viewModel.getForwardCommand().execute();
    }

    public void onRTurnButtonClicked(ActionEvent actionEvent) {
        viewModel.getrTurnCommand().execute();
    }

    public void onDownButtonClicked(ActionEvent actionEvent) {
        viewModel.getDownCommand().execute();
    }

    public void onLeftButtonClicked(ActionEvent actionEvent) {
        viewModel.getLeftCommand().execute();
    }

    public void onBackButtonClicked(ActionEvent actionEvent) {
        viewModel.getBackCommand().execute();
    }

    public void onRightButtonClicked(ActionEvent actionEvent) {
        viewModel.getRightCommand().execute();
    }

    public void onShowKBShortcutsChecked(ActionEvent actionEvent) {
        updateTextNGraph(showKBShortcutsCheckBox.isSelected());
    }

    private void updateTextNGraph(Boolean isShortCut) {
        if (isShortCut) {
            Text up = new Text("Q");
            up.setStyle("-fx-font-weight: bold");
            upButton.setGraphic(up);

            Text down = new Text("A");
            down.setStyle("-fx-font-weight: bold");
            downButton.setGraphic(down);

            Text lTurn = new Text("Z");
            lTurn.setStyle("-fx-font-weight: bold");
            lTurnButton.setGraphic(lTurn);

            Text rTurn = new Text("X");
            rTurn.setStyle("-fx-font-weight: bold");
            rTurnButton.setGraphic(rTurn);
        } else {
            upButton.setGraphic(new ImageView(getUpIcon()));
            downButton.setGraphic(new ImageView(getDownIcon()));
            lTurnButton.setGraphic(new ImageView(getLTurnIcon()));
            rTurnButton.setGraphic(new ImageView(getrTurnIcon()));
        }
    }

    private Image getUpIcon() {
        if (upIcon == null) {
            upIcon = new Image(ICON_UP);
        }

        return upIcon;
    }

    private Image getDownIcon() {
        if (downIcon == null) {
            downIcon = new Image(ICON_DOWN);
        }

        return downIcon;
    }

    private Image getLTurnIcon() {
        if (lTurnIcon == null) {
            lTurnIcon = new Image(ICON_LTURN);
        }

        return lTurnIcon;
    }

    private Image getrTurnIcon() {
        if (rTurnIcon == null) {
            rTurnIcon = new Image(ICON_RTURN);
        }

        return rTurnIcon;
    }
}
