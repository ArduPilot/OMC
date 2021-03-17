/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.warnings;

import com.google.inject.Inject;
import org.asyncfx.beans.property.PropertyPath;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Window;

public class UnresolvedWarningsDialogView extends DialogView<UnresolvedWarningsDialogViewModel> {

    // Class needs to be public, otherwise it can't be properly initialized.
    public static class WarningItemView extends HBox implements JavaView<WarningItemViewModel> {

        @InjectViewModel
        private WarningItemViewModel viewModel;

        public void initialize() {
            setSpacing(5);
            setAlignment(Pos.BASELINE_LEFT);
            Circle circle = new Circle(0, 0, 2, Color.BLACK);
            circle.setTranslateY(-2);
            getChildren().add(circle);
            Label label = new Label();
            label.setWrapText(true);
            label.textProperty().bind(viewModel.messageProperty());
            getChildren().add(label);
        }

    }

    @InjectViewModel
    private UnresolvedWarningsDialogViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private Pane layoutRoot;

    @FXML
    private VBox unresolvedWarningsPane;

    @FXML
    private ItemsView<WarningItemViewModel> itemsView;

    @FXML
    private TextField commentTextField;

    @FXML
    private CheckBox ignoreWarningsCheckBox;

    @FXML
    private Button proceedButton;

    @FXML
    private Button closeButton;

    @FXML
    private Label disclaimer;

    @FXML
    private Label missionLogLine1;

    @FXML
    private VBox missionLogText;

    private ReadOnlyObjectProperty<Window> window;

    @Override
    protected void initializeView() {
        super.initializeView();

        itemsView.addViewFactory(
            WarningItemViewModel.class,
            vm -> FluentViewLoader.javaView(WarningItemView.class).viewModel(vm).load().getView());
        itemsView.itemsProperty().bind(viewModel.importantWarningsProperty());

        unresolvedWarningsPane.visibleProperty().bind(viewModel.importantWarningsProperty().emptyProperty().not());
        unresolvedWarningsPane.managedProperty().bind(unresolvedWarningsPane.visibleProperty());

        commentTextField.textProperty().bindBidirectional(viewModel.commentProperty());
        ignoreWarningsCheckBox.selectedProperty().bindBidirectional(viewModel.ignoreWarningsProperty());

        proceedButton.disableProperty().bind(viewModel.getProceedCommand().notExecutableProperty());
        proceedButton.setOnAction(event -> viewModel.getProceedCommand().execute());

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

        disclaimer.visibleProperty().bind(viewModel.showDisclaimerProperty());
        disclaimer.managedProperty().bind(viewModel.showDisclaimerProperty());

        missionLogLine1.visibleProperty().bind(viewModel.needsExtraConfirmationProperty());
        missionLogLine1.managedProperty().bind(viewModel.needsExtraConfirmationProperty());
        missionLogText.visibleProperty().bind(viewModel.needsExtraConfirmationProperty());
        missionLogText.managedProperty().bind(viewModel.needsExtraConfirmationProperty());
        ignoreWarningsCheckBox.visibleProperty().bind(viewModel.needsExtraConfirmationProperty());
        ignoreWarningsCheckBox.managedProperty().bind(viewModel.needsExtraConfirmationProperty());
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(UnresolvedWarningsDialogView.class.getName() + ".title"));
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
