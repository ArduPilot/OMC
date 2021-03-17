/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.LanguageHelper;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import de.saxsys.mvvmfx.ViewModel;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javax.script.Bindings;

public class AutomaticChecksDialogView extends DialogView<AutomaticChecksDialogViewModel> {

    private static final String ICON_COMPLETE = "/com/intel/missioncontrol/icons/icon_complete(fill=theme-green).svg";
    private static final String ICON_ALERT = "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg";
    private static final String ICON_LOADING = "/com/intel/missioncontrol/icons/icon_progress.svg";

    @InjectViewModel
    private AutomaticChecksDialogViewModel viewModel;

    @FXML
    private VBox layoutRoot;

    @FXML
    private ItemsView<AutoCheckItemViewModel> itemsView;

    private Image completeIcon;
    private Image alertIcon;
    private Image loadingIcon;

    private final LanguageHelper languageHelper;

    @Inject
    public AutomaticChecksDialogView(LanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        itemsView.addViewFactory(
            AutoCheckItemViewModel.class,
            vm -> FluentViewLoader.javaView(AutoCheckItemView.class).viewModel(vm).load().getView());
        itemsView.itemsProperty().bind(viewModel.warningsProperty());
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.preflightchecks.AutomaticChecksDialogView.title"));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private Image getCompleteIcon() {
        if (completeIcon == null) {
            completeIcon = new Image(ICON_COMPLETE);
        }

        return completeIcon;
    }

    private Image getAlertIcon() {
        if (alertIcon == null) {
            alertIcon = new Image(ICON_ALERT);
        }

        return alertIcon;
    }

    private Image getLoadingIcon() {
        if (loadingIcon == null) {
            loadingIcon = new Image(ICON_LOADING);
        }

        return loadingIcon;
    }

    public void onDoneButtonClicked(ActionEvent actionEvent) {
        viewModel.getCloseCommand().execute();
    }

    // Class needs to be public, otherwise it can't be properly initialized.
    public static class AutoCheckItemView extends HBox implements JavaView<AutoCheckItemViewModel> {

        @InjectViewModel
        private AutoCheckItemViewModel viewModel;

        private LanguageHelper languageHelper;

        @Inject
        public AutoCheckItemView(LanguageHelper languageHelper) {
            this.languageHelper = languageHelper;
        }

        private RotateTransition rotation = null;
        public void initialize() {
            setSpacing(5);
            setAlignment(Pos.BASELINE_LEFT);
            setPadding(new Insets(5, 5, 5, 5));
            setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

            ImageView imageView = new ImageView();
            imageView.setFitWidth(16);
            imageView.setFitHeight(16);

            imageView.imageProperty().bind(
                    javafx.beans.binding.Bindings.createObjectBinding(
                            () -> {
                                if (viewModel.isInProgressProperty().get()) {
                                    rotation = new RotateTransition(Duration.millis(1000), imageView);
                                    rotation.setFromAngle(0);
                                    rotation.setToAngle(360);
                                    rotation.setInterpolator(Interpolator.LINEAR);
                                    rotation.setCycleCount(Animation.INDEFINITE);

                                    rotation.statusProperty()
                                            .addListener(
                                                    (obs, oldValue, newValue) -> {
                                                        if (newValue == Animation.Status.STOPPED) {
                                                            RotateTransition transition = new RotateTransition(Duration.millis(10), imageView);
                                                            transition.setFromAngle(imageView.getRotate());
                                                            transition.setToAngle(0);
                                                            transition.setCycleCount(1);
                                                            transition.setAutoReverse(true);
                                                            transition.play();
                                                        }
                                                    });
                                    rotation.play();
                                    return (new Image(ICON_LOADING));
                                }
                                else
                                if (viewModel.isOkMessageProperty().get()) {
                                    if (rotation != null) {
                                        rotation.stop();
                                        rotation = null;
                                    }
                                    return (new Image(ICON_COMPLETE));
                                }
                                else {
                                    if (rotation != null) {
                                        rotation.stop();
                                        rotation = null;
                                    }
                                    return (new Image(ICON_ALERT));
                                }
                            },
                viewModel.isInProgressProperty(), viewModel.isOkMessageProperty()));


            getChildren().add(imageView);

            Label label = new Label();
            label.setWrapText(true);
            label.textProperty().bind(viewModel.messageProperty());
            label.disableProperty().bind(viewModel.isInProgressProperty());
            getChildren().add(label);

            Pane pane = new Pane();
            HBox.setHgrow(pane, Priority.ALWAYS);
            getChildren().add(pane);

            if (!viewModel.isOkMessageProperty().get()) {
                Hyperlink hyperlink =
                        new Hyperlink(
                                languageHelper.getString(

                                        "com.intel.missioncontrol.ui.dialogs.preflightchecks.AutomaticChecksDialogView.howToFix")
                        );
                hyperlink.textProperty().bind(viewModel.firstResolveActionTextProperty());
                hyperlink.setAlignment(Pos.CENTER);
                hyperlink.setPadding(new Insets(1, 5, 1, 1));
                hyperlink.setPrefWidth(70);
                hyperlink.setOnAction(event -> viewModel.getFirstResolveActionCommand().execute());
                label.disableProperty().bind(viewModel.isInProgressProperty());

                getChildren().add(hyperlink);
                hyperlink.visibleProperty().bind(viewModel.numberOfResolveActionsProperty().greaterThan(0));
            }
        }

    }
}
