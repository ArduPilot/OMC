/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.laanc.airmap;

import com.google.inject.Inject;
import org.asyncfx.beans.property.PropertyPath;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

public class LaancAirmapDialogView extends DialogView<LaancAirmapDialogViewModel> {

    @InjectViewModel
    private LaancAirmapDialogViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private Pane layoutRoot;

    @FXML
    private Button copyLinkButton;

    @FXML
    private Button closeButton;

    private ReadOnlyObjectProperty<Window> window;

    @FXML
    private Label imageLabel;

    @Override
    protected void initializeView() {
        super.initializeView();

        copyLinkButton.disableProperty().bind(viewModel.getProceedCommand().notExecutableProperty());
        copyLinkButton.setOnAction(event -> viewModel.getProceedCommand().execute());

        closeButton.disableProperty().bind(viewModel.getCloseCommand().notExecutableProperty());
        closeButton.setOnAction(event -> viewModel.getCloseCommand().execute());

        window = PropertyPath.from(layoutRoot.sceneProperty()).selectReadOnlyObject(Scene::windowProperty);
        window.addListener(
            (observable, oldValue, newValue) -> {
                newValue.heightProperty()
                    .addListener(
                        observable1 -> {
                            if (layoutRoot.getMinHeight() <= 0) {
                                layoutRoot.setMinHeight(layoutRoot.getHeight());
                            }
                        });

                newValue.sizeToScene();
                newValue.setWidth(layoutRoot.getPrefWidth());
            });

        imageLabel.setGraphic(new ImageView(viewModel.getImage()));
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(LaancAirmapDialogView.class.getName() + ".title"));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

}
