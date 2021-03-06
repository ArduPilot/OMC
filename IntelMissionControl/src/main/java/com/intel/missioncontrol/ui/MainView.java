/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.VisibilityTracker;
import com.intel.missioncontrol.diagnostics.PerformanceTracker;
import com.intel.missioncontrol.diagnostics.WorldWindProfiler;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.map.MapInputEvent;
import com.intel.missioncontrol.map.credits.MapCreditView;
import com.intel.missioncontrol.map.credits.MapCreditViewModel;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.map.worldwind.WWDispatcher;
import com.intel.missioncontrol.map.worldwind.WWElevationModel;
import com.intel.missioncontrol.map.worldwind.WWMapModel;
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.ui.accessibility.ShortcutLayerPresenter;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.NavBarDialogView;
import com.intel.missioncontrol.ui.navbar.NavBarView;
import com.intel.missioncontrol.ui.navbar.layers.MapLayersView;
import com.intel.missioncontrol.ui.navbar.settings.SettingsView;
import com.intel.missioncontrol.ui.navbar.tools.ToolsView;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.navigation.SidePaneTab;
import com.intel.missioncontrol.ui.notifications.MainStatus;
import com.intel.missioncontrol.ui.search.SearchView;
import com.intel.missioncontrol.ui.sidepane.FancyTabPane;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import gov.nasa.worldwind.javafx.WWGLNode;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.asyncfx.concurrent.Dispatcher;

public class MainView extends DialogView<MainViewModel> implements AutoCloseable {

    private final WWDispatcher mapDispatcher;
    private final IDialogContextProvider dialogContextProvider;
    private final ILanguageHelper languageHelper;
    private final WWMapModel mapModel;
    private final IWWMapView mapView;
    private final WorldWindowProvider worldWindowProvider;
    private final VisibilityTracker visibilityTracker;
    private final WorldWindProfiler profiler;

    @FXML
    private Pane layoutRoot;

    @FXML
    private Parent demoMissionWarningView;

    @FXML
    private FancyTabPane<SidePaneTab> fancyTabPane;

    @FXML
    private Parent mapLayersView;

    @FXML
    private MapLayersView mapLayersViewController;

    @FXML
    private Parent toolsView;

    @FXML
    private ToolsView toolsViewController;

    @FXML
    private Parent settingsView;

    @FXML
    private SettingsView settingsViewController;

    @FXML
    private WWGLNode worldWindNode;

    @FXML
    private ItemsView<MapCreditViewModel> mapCreditsView;

    @FXML
    private Pane colorLegendContainer;

    @FXML
    private VBox windowContentContainer;

    @FXML
    private StackPane navBarDialogContainer;

    @FXML
    private Button statusBarSrsButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Label statusBarNotificationLabel;

    @FXML
    private ToggleButton warningsButton;

    @FXML
    private ToggleButton backgroundTasksButton;

    @FXML
    private ToggleButton linkBoxButton;

    @FXML
    private SearchView searchViewController;

    @InjectViewModel
    private MainViewModel viewModel;

    @InjectContext
    private Context context;

    private ShortcutLayerPresenter shortcutLayerPresenter; // store a reference so it doesn't get GC'd
    private RotateTransition backgroundTasksIconRotation;

    @Inject
    public MainView(
            @Named(MapModule.DISPATCHER) Dispatcher mapDispatcher,
            IDialogContextProvider dialogContextProvider,
            ILanguageHelper languageHelper,
            WWElevationModel wwElevationModel,
            WWMapModel mapModel,
            IWWMapView mapView,
            WorldWindowProvider worldWindowProvider,
            VisibilityTracker visibilityTracker,
            WorldWindProfiler profiler) {
        this.mapDispatcher = (WWDispatcher)mapDispatcher;
        this.dialogContextProvider = dialogContextProvider;
        this.profiler = profiler;
        this.languageHelper = languageHelper;
        this.mapModel = mapModel;
        this.mapView = mapView;
        this.worldWindowProvider = worldWindowProvider;
        this.visibilityTracker = visibilityTracker;

        wwElevationModel
            .wwjElevationModelProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    worldWindNode.redraw();
                    Dispatcher.platform()
                        .runLater(
                            () -> {
                                worldWindNode.redraw();
                                Dispatcher.platform()
                                    .runLater(() -> worldWindNode.redraw(), java.time.Duration.ofMillis(1000));
                            },
                            java.time.Duration.ofMillis(1000));
                },
                Platform::runLater);
    }

    @Override
    public void initializeView() {
        super.initializeView();

        PerformanceTracker.setWWNode(worldWindNode);

        // Sets up the synchronization context to execute code on the WorldWind thread.
        mapDispatcher.setWWNode(worldWindNode);
        worldWindNode.setModel(mapModel.getWWModel());
        worldWindNode.setView(mapView);
        worldWindNode.addRenderingListener(visibilityTracker);
        worldWindNode.addRenderingListener(profiler);

        worldWindNode.addEventHandler(
            MapInputEvent.MAP_ACTION, event -> viewModel.getMapActionCommand().execute(event.getMapObject()));

        worldWindNode.addEventHandler(
            KeyEvent.KEY_RELEASED,
            event -> {
                if (event.getCode() == KeyCode.DELETE) {
                    viewModel.getDeleteSelectionCommand().execute();
                } else if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                    viewModel.getCancelCommand().execute();
                }
            });

        profiler.bindToWWD(worldWindNode);

        worldWindowProvider.provide(worldWindNode);

        worldWindNode.startRendering();

        shortcutLayerPresenter = new ShortcutLayerPresenter(layoutRoot);
        dialogContextProvider.setContext(viewModel, context);

        searchViewController.setOnAction(event -> worldWindNode.requestFocus());
        statusBarSrsButton.textProperty().bind(viewModel.srsNameProperty());
        updateStatusBarNotification(viewModel.mainStatusProperty().get());

        demoMissionWarningView.visibleProperty().bind(viewModel.demoMissionWarningVisibleProperty());
        demoMissionWarningView.managedProperty().bind(demoMissionWarningView.visibleProperty());

        fancyTabPane.selectedTabProperty().bind(viewModel.currentSidePaneTabProperty());

        viewModel
            .mainStatusProperty()
            .addListener(((observable, oldValue, newValue) -> updateStatusBarNotification(newValue)));

        Insets insets = new Insets(0, 0, 0, NavBarView.DEFAULT_WIDTH);
        StackPane.setMargin(windowContentContainer, insets);
        StackPane.setMargin(navBarDialogContainer, insets);

        colorLegendContainer.setMaxWidth(Region.USE_PREF_SIZE);
        colorLegendContainer.setMaxHeight(Region.USE_PREF_SIZE);
        colorLegendContainer.visibleProperty().bind(viewModel.coverageLegendVisibleProperty());
        colorLegendContainer.managedProperty().bind(colorLegendContainer.visibleProperty());

        mapLayersViewController
            .visibleProperty()
            .bind(viewModel.currentNavBarDialogProperty().isEqualTo(NavBarDialog.MAPLAYERS));
        mapLayersView.managedProperty().bind(mapLayersView.visibleProperty());

        toolsViewController
            .visibleProperty()
            .bind(viewModel.currentNavBarDialogProperty().isEqualTo(NavBarDialog.TOOLS));
        toolsView.managedProperty().bind(toolsView.visibleProperty());

        settingsViewController
            .visibleProperty()
            .bind(viewModel.currentNavBarDialogProperty().isEqualTo(NavBarDialog.SETTINGS));
        settingsView.managedProperty().bind(settingsView.visibleProperty());

        statusLabel.textProperty().bind(viewModel.currentStatusProperty());
        statusLabel.setGraphic(new CoordinateControl(viewModel.currentCoordinatesProperty()));
        statusLabel
            .contentDisplayProperty()
            .bind(
                Bindings.createObjectBinding(
                    () ->
                        viewModel.currentStatusProperty().get() != null
                            ? ContentDisplay.TEXT_ONLY
                            : ContentDisplay.GRAPHIC_ONLY,
                    viewModel.currentStatusProperty()));

        viewModel.initializeWorldWindow(worldWindNode);

        mapCreditsView.addViewFactory(
            MapCreditViewModel.class,
            vm -> FluentViewLoader.javaView(MapCreditView.class).viewModel(vm).load().getView());
        mapCreditsView.itemsProperty().bind(viewModel.mapCreditsProperty());

        if (WindowHelper.getPrimaryWorldWindow() == null) {
            WindowHelper.Accessor.setPrimaryWorldWindow(worldWindNode);
        }

        NavBarDialogView.group(toolsViewController, settingsViewController);

        warningsButton.setVisible(false);
        warningsButton.setManaged(false);
        warningsButton
            .selectedProperty()
            .addListener(
                observable -> {
                    if (!warningsButton.isSelected()) {
                        return;
                    }

                    viewModel
                        .getShowWarningsCommand()
                        .executeAsync(
                            warningsButton.localToScreen(warningsButton.getWidth() / 2, ScaleHelper.emsToPixels(0.25)))
                        .addListener(() -> warningsButton.setSelected(false), Platform::runLater);
                });

        linkBoxButton
            .selectedProperty()
            .addListener(
                observable -> {
                    if (!linkBoxButton.isSelected()) {
                        return;
                    }

                    viewModel
                        .getShowLinkBoxStatusCommand()
                        .executeAsync(
                            linkBoxButton.localToScreen(linkBoxButton.getWidth() / 2, ScaleHelper.emsToPixels(0.25)))
                        .addListener(() -> linkBoxButton.setSelected(false), Platform::runLater);
                });

        linkBoxButton.visibleProperty().bind(viewModel.linkBoxConnectedProperty());
        viewModel
            .linkBoxAuthorizedProperty()
            .addListener((
                 observableValue, oldValue, newValue) -> updateLinkBoxButtonStyle());
        updateLinkBoxButtonStyle();

        backgroundTasksButton
            .selectedProperty()
            .addListener(
                observable -> {
                    if (!backgroundTasksButton.isSelected()) {
                        return;
                    }

                    viewModel
                        .getShowBackgroundTasksCommand()
                        .executeAsync(
                            backgroundTasksButton.localToScreen(
                                backgroundTasksButton.getWidth() / 2, ScaleHelper.emsToPixels(0.25)))
                        .addListener(() -> backgroundTasksButton.setSelected(false), Platform::runLater);
                });

        viewModel.warningsCountProperty().addListener(this::handleWarningsCountChanged);
        viewModel.warningMessageCategoryProperty().addListener(this::handleCriticalWarningChanged);
        viewModel.backgroundTasksRunningProperty().addListener(this::handleBackgroundTasksRunning);

        layoutRoot.addEventFilter(
            KeyEvent.KEY_RELEASED,
            event -> {
                if (event.getCode() == KeyCode.F5
                        && event.isControlDown()
                        && viewModel.getReloadStylesheetsCommand().isExecutable()) {
                    viewModel.getReloadStylesheetsCommand().execute();
                }
            });
    }

    private void updateLinkBoxButtonStyle() {
        Dispatcher.platform().runLaterAsync(()-> {
            if(viewModel.linkBoxAuthorizedProperty().get()){
                linkBoxButton.getStyleClass().removeAll("critical");
            } else {
                linkBoxButton.getStyleClass().add("critical");
            }
        });
    }

    @Override
    public void close() {
        worldWindNode.shutdown();
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected MainViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return viewModel.titleProperty();
    }

    public Context getContext() {
        return context;
    }

    private void updateStatusBarNotification(MainStatus notification) {
        if (notification != null) {
            statusBarNotificationLabel.setVisible(true);
            statusBarNotificationLabel.setText(notification.getText());
        } else {
            statusBarNotificationLabel.setVisible(false);
        }
    }

    private void handleBackgroundTasksRunning(
            ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (backgroundTasksIconRotation != null) {
            backgroundTasksIconRotation.stop();
            backgroundTasksButton.getStyleClass().add("icon-bg-tasks");
            backgroundTasksButton.getStyleClass().remove("icon-progress");
        }

        if (newValue) {
            backgroundTasksButton.getStyleClass().remove("icon-bg-tasks");
            backgroundTasksButton.getStyleClass().add("icon-progress");
            backgroundTasksIconRotation = new RotateTransition(Duration.seconds(2));
            backgroundTasksIconRotation.setNode(backgroundTasksButton);
            backgroundTasksIconRotation.setFromAngle(0);
            backgroundTasksIconRotation.setToAngle(360);
            backgroundTasksIconRotation.setRate(0);
            backgroundTasksIconRotation.setInterpolator(Interpolator.LINEAR);
            backgroundTasksIconRotation.setCycleCount(Animation.INDEFINITE);
            backgroundTasksIconRotation.play();

            Timeline timeline =
                new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(backgroundTasksIconRotation.rateProperty(), 0)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(backgroundTasksIconRotation.rateProperty(), 1)));
            timeline.play();
        } else {
            double completedFrac = backgroundTasksButton.getRotate() / 360.0;
            Duration duration = Duration.seconds((1 - completedFrac) * 2.0);
            backgroundTasksIconRotation = new RotateTransition(duration);
            backgroundTasksIconRotation.setNode(backgroundTasksButton);
            backgroundTasksIconRotation.setInterpolator(Interpolator.LINEAR);
            backgroundTasksIconRotation.setToAngle(360);
            backgroundTasksIconRotation.play();

            Timeline timeline =
                new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(backgroundTasksIconRotation.rateProperty(), 1)),
                    new KeyFrame(Duration.seconds(4), new KeyValue(backgroundTasksIconRotation.rateProperty(), 0.1)));
            timeline.play();
        }
    }

    private void handleWarningsCountChanged(
            ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        int count = newValue.intValue();
        if (count > 0) {
            warningsButton.setVisible(true);
            warningsButton.setManaged(true);
            String text =
                count == 1
                    ? languageHelper.getString("com.intel.missioncontrol.ui.MainView.warningSingular")
                    : languageHelper.getString("com.intel.missioncontrol.ui.MainView.warningPlural", count);
            Dispatcher.platform().runLaterAsync(()->warningsButton.setText(text));
        } else {
            warningsButton.setVisible(false);
            warningsButton.setManaged(false);
        }
    }

    private void handleCriticalWarningChanged(
            ObservableValue<? extends ValidationMessageCategory> observable,
            ValidationMessageCategory oldValue,
            ValidationMessageCategory newValue) {
        warningsButton.getStyleClass().remove("critical");
        warningsButton.getStyleClass().remove("warning");
        warningsButton.getStyleClass().remove("icon-notice");
        warningsButton.getStyleClass().remove("icon-warning");
        if (newValue == null) {
            return;
        }

        switch (newValue) {
        case BLOCKING:
            warningsButton.getStyleClass().add("critical");
            warningsButton.getStyleClass().add("icon-warning");
            break;
        case NORMAL:
            warningsButton.getStyleClass().add("warning");
            warningsButton.getStyleClass().add("icon-warning");
            break;
        case NOTICE:
            warningsButton.getStyleClass().add("icon-notice");
            break;
        }
    }

    @FXML
    private void srsButtonClicked() {
        viewModel.getChangeSrsCommand().execute();
    }

    private static class CoordinateControl extends HBox {
        CoordinateControl(ObservableValue<String[]> coordinates) {
            setAlignment(Pos.CENTER_LEFT);
            Label x = new Label();
            x.setMinWidth(110);
            x.textProperty()
                .bind(
                    Bindings.createStringBinding(
                        () -> coordinates.getValue() != null ? coordinates.getValue()[0] : "", coordinates));
            getChildren().add(x);
            Label y = new Label();
            y.setMinWidth(110);
            y.textProperty()
                .bind(
                    Bindings.createStringBinding(
                        () -> coordinates.getValue() != null ? coordinates.getValue()[1] : "", coordinates));
            getChildren().add(y);
            Label z = new Label();
            z.textProperty()
                .bind(
                    Bindings.createStringBinding(
                        () ->
                            coordinates.getValue() != null && coordinates.getValue().length == 3
                                ? coordinates.getValue()[2]
                                : "",
                        coordinates));
            getChildren().add(z);
        }
    }

}
