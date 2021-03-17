/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.RootView;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import eu.mavinci.core.obfuscation.IKeepClassname;
import eu.mavinci.desktop.helper.FileFilter;
import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.controlsfx.dialog.ProgressDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DialogService implements IDialogService, IKeepClassname {

    public static class StringStringConverter extends StringConverter<String> {
        @Override
        public String toString(String string) {
            if (string.isEmpty()) {
                return null;
            }

            return string;
        }

        @Override
        public String fromString(String string) {
            if (string.isEmpty()) {
                return null;
            }

            return string;
        }
    }

    private static class GenericInputView<T> extends VBox implements IKeepClassname {
        final ObjectProperty<T> value = new SimpleObjectProperty<>();
        final BooleanProperty invalid = new SimpleBooleanProperty();

        GenericInputView(
                ILanguageHelper languageHelper,
                String message,
                boolean allowEmptyValue,
                StringConverter<T> converter,
                Runnable closeHandler,
                String defaultInput) {
            invalid.set(!allowEmptyValue);

            var textField = new TextField();
            textField
                .textProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        try {
                            value.set(converter.fromString(newValue));
                            invalid.set(value.get() == null && !allowEmptyValue);
                        } catch (RuntimeException e) {
                            value.set(null);
                            invalid.set(true);
                        }
                    });
            textField.setText(defaultInput);

            var okButton = new Button(languageHelper.getString(getClass().getName() + ".OK"));
            okButton.getStyleClass().add("primary-button");
            okButton.disableProperty().bind(invalid);
            okButton.setOnAction(event -> closeHandler.run());

            var cancelButton = new Button(languageHelper.getString(getClass().getName() + ".CANCEL"));
            cancelButton.getStyleClass().add("secondary-button");
            cancelButton.setOnAction(
                event -> {
                    value.set(null);
                    closeHandler.run();
                });

            var buttonArea = new HBox(okButton, cancelButton);
            VBox.setVgrow(buttonArea, Priority.NEVER);
            buttonArea.getStyleClass().add("footer");

            var content = new VBox();
            VBox.setVgrow(content, Priority.ALWAYS);
            content.getStyleClass().add("content");

            if (message != null && !message.isEmpty()) {
                content.getChildren().add(new Label(message));
            }

            content.getChildren().add(textField);
            getChildren().add(content);
            getChildren().add(new Separator());
            getChildren().add(buttonArea);
            getStyleClass().add("dialog");
        }
    }

    // FIXME: This is broken beyond repair. The dialog won't work if the app language is anything other than English.
    private static class GenericView<T> extends VBox implements IKeepClassname {
        final ObjectProperty<ButtonType> value = new SimpleObjectProperty<>();
        final BooleanProperty invalid = new SimpleBooleanProperty();

        GenericView(
                ILanguageHelper languageHelper,
                String message,
                StringConverter<T> converter,
                Runnable closeHandler,
                ButtonType[] buttons,
                AlertType information) {
            HBox buttonArea = new HBox();
            for (ButtonType button : buttons) {
                var myButton = new Button(languageHelper.getString(GenericView.class, button.getText()));
                if (button == buttons[0]) {
                    myButton.getStyleClass().add("primary-button");
                } else {
                    myButton.getStyleClass().add("secondary-button");
                }

                myButton.disableProperty().bind(invalid);
                myButton.setOnAction(
                    event -> {
                        value.set(button);
                        closeHandler.run();
                    });
                buttonArea.getChildren().add(myButton);
            }

            VBox.setVgrow(buttonArea, Priority.NEVER);
            buttonArea.getStyleClass().add("footer");

            var content = new HBox();
            HBox.setHgrow(content, Priority.ALWAYS);

            content.getStyleClass().add("content");

            if (information != null) {
                Button skipButton = new Button();
                skipButton.setBackground(Background.EMPTY);
                switch (information) {
                case WARNING:
                    skipButton.getStyleClass().addAll("large-transparent-icon-button", "icon-warning");
                    break;
                case ERROR:
                    skipButton.getStyleClass().addAll("large-transparent-icon-button", "icon-stop-red");
                    break;
                case INFORMATION:
                    skipButton.getStyleClass().addAll("large-transparent-icon-button", "icon-notice");
                    break;
                case CONFIRMATION:
                default:
                    skipButton.getStyleClass().addAll("large-transparent-icon-button", "icon-help-red");
                }

                content.getChildren().add(skipButton);
            }

            if (message != null && !message.isEmpty()) {
                Label label = new Label(message);
                content.setAlignment(Pos.CENTER);
                content.getChildren().add(label);
            }

            getChildren().add(content);
            getChildren().add(buttonArea);
            getStyleClass().add("dialog");
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DialogService.class);

    private static final Image WINDOW_ICON =
        new Image(
            DialogService.class
                .getResource("/com/intel/missioncontrol/app-icon/mission-control-icon.png")
                .toExternalForm());

    private static final String POPOVER_STYLESHEET = "/com/intel/missioncontrol/styles/popover.css";
    private static final double POPOVER_MIN_DISTANCE_TO_EDGE = ScaleHelper.emsToPixels(0.5);
    private static final double POPOVER_ARROW_SIZE = ScaleHelper.emsToPixels(0.75);

    private final ILanguageHelper languageHelper;
    private final ISettingsManager settingsManager;
    private final IDialogContextProvider dialogContextProvider;
    private final Map<Object, Path> dialogCurrentFolder = new WeakHashMap<>();
    private Map<Class<? extends ViewModel>, Class<? extends RootView<? extends ViewModelBase>>> dialogMap;

    @Inject
    public DialogService(
            ILanguageHelper languageHelper,
            ISettingsManager settingsManager,
            IDialogContextProvider dialogContextProvider) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
        this.dialogContextProvider = dialogContextProvider;
    }

    public void setDialogMap(Map<Class<? extends ViewModel>, Class<? extends RootView<? extends ViewModelBase>>> map) {
        dialogMap = map;
    }

    @Override
    public <ViewModelType extends DialogViewModel> Future<ViewModelType> requestDialogAsync(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass) {
        Expect.notNull(
            ownerViewModel, "ownerViewModel",
            dialogViewModelClass, "dialogViewModelClass");
        return requestDialogImplAsync(ownerViewModel, dialogViewModelClass, null, true, false);
    }

    @Override
    public <ViewModelType extends DialogViewModel> Future<ViewModelType> requestDialogAsync(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass, boolean modal) {
        Expect.notNull(
            ownerViewModel, "ownerViewModel",
            dialogViewModelClass, "dialogViewModelClass");
        return requestDialogImplAsync(ownerViewModel, dialogViewModelClass, null, modal, false);
    }

    @Override
    public <ViewModelType extends DialogViewModel, PayloadType> Future<ViewModelType> requestDialogAsync(
            ViewModel ownerViewModel,
            Class<ViewModelType> dialogViewModelClass,
            Supplier<PayloadType> payloadSupplier) {
        Expect.notNull(
            ownerViewModel, "ownerViewModel",
            dialogViewModelClass, "dialogViewModelClass",
            payloadSupplier, "payloadSupplier");
        return requestDialogImplAsync(ownerViewModel, dialogViewModelClass, payloadSupplier, true, false);
    }

    @Override
    public <ViewModelType extends DialogViewModel, PayloadType> Future<ViewModelType> requestDialogAsync(
            ViewModel ownerViewModel,
            Class<ViewModelType> dialogViewModelClass,
            Supplier<PayloadType> payloadSupplier,
            boolean modal) {
        Expect.notNull(
            ownerViewModel, "ownerViewModel",
            dialogViewModelClass, "dialogViewModelClass",
            payloadSupplier, "payloadSupplier");
        return requestDialogImplAsync(ownerViewModel, dialogViewModelClass, payloadSupplier, modal, false);
    }

    @Override
    public <ViewModelType extends DialogViewModel> ViewModelType requestDialogAndWait(
            ViewModel ownerViewModel, Class<ViewModelType> dialogViewModelClass) {
        Expect.notNull(
            ownerViewModel, "ownerViewModel",
            dialogViewModelClass, "dialogViewModelClass");
        return requestDialogImplAsync(ownerViewModel, dialogViewModelClass, null, true, true).getUnchecked();
    }

    @Override
    public <ViewModelType extends DialogViewModel, PayloadType> ViewModelType requestDialogAndWait(
            ViewModel ownerViewModel,
            Class<ViewModelType> dialogViewModelClass,
            Supplier<PayloadType> payloadSupplier) {
        Expect.notNull(
            ownerViewModel, "ownerViewModel",
            dialogViewModelClass, "dialogViewModelClass",
            payloadSupplier, "payloadSupplier");
        return requestDialogImplAsync(ownerViewModel, dialogViewModelClass, payloadSupplier, true, true).getUnchecked();
    }

    private <T> Optional<ButtonType> requestDialogImpl(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            StringConverter<T> converter,
            ButtonType[] buttons,
            AlertType information,
            boolean wait) {
        Expect.notNull(ownerViewModel, "ownerViewModel", converter, "converter");

        if (!Platform.isFxApplicationThread()) {
            return Dispatcher.platform()
                .getLaterAsync(
                    () -> requestDialogImpl(ownerViewModel, title, message, converter, buttons, information, wait))
                .getUnchecked();
        }

        Window ownerWindow = getWindowFromViewModel(ownerViewModel);
        var stage = new Stage();
        stage.setTitle(
            title == null || title.isEmpty()
                ? languageHelper.getString(DialogService.class, "defaultDialogTitle")
                : title);
        stage.getIcons().add(WINDOW_ICON);
        var root = new GenericView<>(languageHelper, message, converter, stage::close, buttons, information);
        root.getStylesheets().addAll(settingsManager.getSection(GeneralSettings.class).getTheme().getStylesheets());
        var scene = new Scene(root);
        stage.setScene(scene);
        stage.initOwner(ownerWindow);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UTILITY);

        final ObjectProperty<ButtonType> ret = new SimpleObjectProperty<ButtonType>();
        stage.setOnHidden(
            event -> {
                ret.set(root.value.get());
            });

        if (wait) {
            stage.showAndWait();
        } else {
            stage.show();
        }

        return Optional.ofNullable(ret.get());
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    private <ViewModelType extends DialogViewModel, PayloadType> Future<ViewModelType> requestDialogImplAsync(
            ViewModel ownerViewModel,
            Class<ViewModelType> dialogViewModelClass,
            @Nullable Supplier<PayloadType> payloadSupplier,
            boolean modal,
            boolean wait) {
        Expect.notNull(ownerViewModel, "ownerViewModel", dialogMap, "dialogMap");

        if (!Platform.isFxApplicationThread()) {
            return Dispatcher.platform()
                .getLaterAsync(
                    () -> requestDialogImplAsync(ownerViewModel, dialogViewModelClass, payloadSupplier, modal, wait))
                .getUnchecked();
        }

        Class<? extends DialogView<ViewModelType>> viewClass;

        try {
            viewClass = (Class<? extends DialogView<ViewModelType>>)dialogMap.get(dialogViewModelClass);
        } catch (ClassCastException ex) {
            throw new RuntimeException("Can't show dialog for view model " + dialogViewModelClass.getName(), ex);
        }

        if (viewClass == null) {
            throw new RuntimeException(
                "The view model class " + dialogViewModelClass.getName() + " is not bound to a view class.");
        }

        ViewTuple<? extends DialogView<ViewModelType>, ViewModelType> viewTuple;
        Window ownerWindow = null;

        try {
            if (ownerViewModel != null) {
                Context context = dialogContextProvider.getContext(ownerViewModel);
                if (context == null) {
                    throw new RuntimeException(
                        "The view model "
                            + dialogViewModelClass.getName()
                            + " was requested in the scope of "
                            + ownerViewModel.getClass().getName()
                            + ", but no view context was found. Did you associate the view context"
                            + " of a suitable view using "
                            + IDialogContextProvider.class.getSimpleName()
                            + "?");
                }

                var initializerScope =
                    ViewModelBase.Accessor.newInitializerScope(payloadSupplier != null ? payloadSupplier.get() : null);
                viewTuple =
                    FluentViewLoader.fxmlView(viewClass).context(context).providedScopes(initializerScope).load();
                ownerWindow = getWindowFromViewModel(ownerViewModel);
            } else {
                viewTuple = FluentViewLoader.fxmlView(viewClass).load();
            }
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            SettableFuture<ViewModelType> future = SettableFuture.create();
            future.setException(e);
            return Futures.fromListenableFuture(future);
        }

        if (ownerWindow == null) {
            ownerWindow = WindowHelper.getPrimaryStage();
        }

        final Scene scene = new Scene(viewTuple.getView());
        StageStyle stageStyle = StageStyle.DECORATED;

        Parent view = viewTuple.getView();
        if (view instanceof Region) {
            Region region = (Region)view;
            if (region.getMinWidth() != Region.USE_COMPUTED_SIZE && region.getMaxWidth() != Region.USE_COMPUTED_SIZE
                    || region.getMinHeight() != Region.USE_COMPUTED_SIZE
                        && region.getMaxHeight() != Region.USE_COMPUTED_SIZE) {
                stageStyle = StageStyle.UTILITY;
            }
        }

        final ViewModelType viewModel = viewTuple.getViewModel();
        Stage stage = new Stage();
        stage.initOwner(ownerWindow);
        stage.initStyle(stageStyle);
        stage.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);
        stage.titleProperty().bind(viewTuple.getCodeBehind().titleProperty());
        stage.getIcons().add(viewTuple.getCodeBehind().getIcon());
        stage.setResizable(stageStyle != StageStyle.UTILITY);
        stage.setScene(scene);
        stage.setOnCloseRequest(
            event -> {
                if (!viewModel.canClose()) {
                    event.consume();
                }
            });

        stage.setOnShown(
            event -> {
                final double top = scene.getY();
                final double left = scene.getX();
                final double right = stage.getWidth() - scene.getWidth() - scene.getX();
                final double bottom = stage.getHeight() - scene.getHeight() - scene.getY();
                final double verticalChrome = top + bottom;
                final double horizontalChrome = left + right;

                if (view instanceof Region) {
                    Region region = (Region)view;
                    stage.maxHeightProperty()
                        .bind(
                            Bindings.createDoubleBinding(
                                () ->
                                    region.getMaxHeight() >= 0
                                        ? region.getMaxHeight() + verticalChrome
                                        : Double.POSITIVE_INFINITY,
                                region.maxHeightProperty()));

                    stage.minHeightProperty()
                        .bind(
                            Bindings.createDoubleBinding(
                                () -> region.getMinHeight() >= 0 ? region.getMinHeight() + verticalChrome : 0,
                                region.minHeightProperty()));

                    stage.maxWidthProperty()
                        .bind(
                            Bindings.createDoubleBinding(
                                () ->
                                    region.getMaxWidth() >= 0
                                        ? region.getMaxWidth() + horizontalChrome
                                        : Double.POSITIVE_INFINITY,
                                region.maxWidthProperty()));

                    stage.minWidthProperty()
                        .bind(
                            Bindings.createDoubleBinding(
                                () -> region.getMinWidth() >= 0 ? region.getMinWidth() + horizontalChrome : 0,
                                region.minWidthProperty()));
                }
            });

        DialogViewModel.Accessor.setCloseHandler(viewModel, stage::close);
        SettableFuture<ViewModelType> future = SettableFuture.create();
        stage.setOnHidden(
            event -> {
                try {
                    viewModel.onClosing();
                } catch (RuntimeException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    future.set(viewModel);
                }
            });

        if (wait) {
            stage.showAndWait();
        } else {
            stage.show();
        }

        return Futures.fromListenableFuture(future);
    }

    @Override
    public <ViewModelType extends ViewModelBase> Future<ViewModelType> requestPopoverDialogAsync(
            ViewModel ownerViewModel, Class<ViewModelType> popoverDialogViewModelClass, Point2D location) {
        return requestPopoverDialogAsync(ownerViewModel, popoverDialogViewModelClass, null, location);
    }

    @SuppressWarnings("unchecked")
    public <ViewModelType extends ViewModelBase, PayloadType> Future<ViewModelType> requestPopoverDialogAsync(
            ViewModel ownerViewModel, Class<ViewModelType> popoverDialogViewModelClass,
            @Nullable Supplier<PayloadType> payloadSupplier,Point2D location) {
        if (!Platform.isFxApplicationThread()) {
            return Dispatcher.platform()
                .getLaterAsync(() -> requestPopoverDialogAsync(ownerViewModel, popoverDialogViewModelClass, location))
                .getUnchecked();
        }

        Expect.notNull(dialogMap, "dialogMap");
        Class<? extends RootView<ViewModelType>> viewClass;

        try {
            viewClass = (Class<? extends RootView<ViewModelType>>)dialogMap.get(popoverDialogViewModelClass);
        } catch (ClassCastException ex) {
            throw new RuntimeException(
                "Can't show popover dialog for view model " + popoverDialogViewModelClass.getName(), ex);
        }

        if (viewClass == null) {
            throw new RuntimeException(
                "The view model class " + popoverDialogViewModelClass.getName() + " is not bound to a view class.");
        }

        ViewTuple<? extends RootView<ViewModelType>, ViewModelType> viewTuple;
        Window ownerWindow = null;

        try {
            if (ownerViewModel != null) {
                Context context = dialogContextProvider.getContext(ownerViewModel);
                if (context == null) {
                    throw new RuntimeException(
                        "The view model "
                            + popoverDialogViewModelClass.getName()
                            + " was requested in the scope of "
                            + ownerViewModel.getClass().getName()
                            + ", but no view context was found. Did you associate the view context"
                            + " of a suitable view using "
                            + IDialogContextProvider.class.getSimpleName()
                            + "?");
                }
                var initializerScope =
                        ViewModelBase.Accessor.newInitializerScope(payloadSupplier != null ? payloadSupplier.get() : null);
                viewTuple = FluentViewLoader.fxmlView(viewClass).context(context).providedScopes(initializerScope).load();
                ownerWindow = getWindowFromViewModel(ownerViewModel);
            } else {
                viewTuple = FluentViewLoader.fxmlView(viewClass).load();
            }
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            SettableFuture<ViewModelType> future = SettableFuture.create();
            future.setException(e);
            return Futures.fromListenableFuture(future);
        }

        if (ownerWindow == null) {
            ownerWindow = WindowHelper.getPrimaryStage();
        }

        Bounds rootBounds;
        Parent root = WindowHelper.getRoot(ownerWindow.getScene());
        if (root instanceof Pane) {
            rootBounds = root.localToScreen(new BoundingBox(0, 0, ((Pane)root).getWidth(), ((Pane)root).getHeight()));
        } else {
            rootBounds = root.localToParent(new BoundingBox(0, 0, 0, 0));
        }

        StackPane contentPane = new StackPane(viewTuple.getView());
        contentPane.getStyleClass().add("popover-content");
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        final double arrowOffset = POPOVER_ARROW_SIZE * 2;
        Polygon arrow = new Polygon();
        arrow.getStyleClass().add("arrow");
        arrow.getPoints().addAll(0.0, 0.0, POPOVER_ARROW_SIZE, POPOVER_ARROW_SIZE, POPOVER_ARROW_SIZE * 2, 0.0);
        VBox.setVgrow(arrow, Priority.NEVER);

        VBox rootPane = new VBox(contentPane, arrow);
        rootPane.getStylesheets().add(DialogService.class.getResource(POPOVER_STYLESHEET).toExternalForm());

        final SettableFuture<ViewModelType> future = SettableFuture.create();
        Popup popup = new Popup();
        popup.getContent().add(rootPane);
        popup.setAutoHide(true);
        popup.setAutoFix(false);
        popup.setOnHidden(event -> future.set(viewTuple.getViewModel()));
        popup.setOnShown(
            event -> {
                final double y = location.getY() - popup.getHeight();
                double x = location.getX() - POPOVER_ARROW_SIZE - arrowOffset;
                double dx = x + popup.getWidth() + POPOVER_MIN_DISTANCE_TO_EDGE - rootBounds.getMaxX();
                if (dx > 0) {
                    x -= dx;
                }

                arrow.setTranslateX(location.getX() - x - POPOVER_ARROW_SIZE);
                rootPane.setOpacity(0);
                rootPane.setTranslateY(POPOVER_ARROW_SIZE);
                popup.setX(x);
                popup.setY(y);

                FadeTransition fade = new FadeTransition(Duration.millis(100));
                fade.setNode(rootPane);
                fade.setToValue(1);
                fade.play();

                TranslateTransition translation = new TranslateTransition(Duration.millis(100));
                translation.setNode(rootPane);
                translation.setToY(0);
                translation.setInterpolator(Interpolator.EASE_OUT);
                translation.play();
                translation.setOnFinished(
                    actionEvent -> {
                        rootPane.setPrefWidth(rootPane.getWidth());
                        rootPane.setPrefHeight(rootPane.getHeight());
                    });
            });

        ViewModelType viewModel = viewTuple.getViewModel();
        if (viewModel instanceof DialogViewModel) {
            DialogViewModel viewModelDialog = (DialogViewModel)viewModel;
            DialogViewModel.Accessor.setCloseHandler(viewModelDialog, popup::hide);
        }

        popup.show(ownerWindow, 0, -30000);

        return Futures.fromListenableFuture(future);
    }

    @Override
    public <T> void requestProgressDialog(Task<T> task, String title, String header) {
        requestProgressDialogImpl(null, task, title, header, false);
    }

    @Override
    public <T> void requestProgressDialog(ViewModel ownerViewModel, Task<T> task, String title, String header) {
        requestProgressDialogImpl(ownerViewModel, task, title, header, false);
    }

    @Override
    public <T> void requestProgressDialogAndWait(Task<T> task, String title, String header) {
        requestProgressDialogImpl(null, task, title, header, true);
    }

    @Override
    public <T> void requestProgressDialogAndWait(ViewModel ownerViewModel, Task<T> task, String title, String header) {
        requestProgressDialogImpl(ownerViewModel, task, title, header, true);
    }

    private <T> void requestProgressDialogImpl(
            @Nullable ViewModel ownerViewModel, Task<T> task, String title, String header, boolean wait) {
        final ProgressDialog dialog = new ProgressDialog(task);
        dialog.initOwner(
            ownerViewModel != null ? getWindowFromViewModel(ownerViewModel) : WindowHelper.getPrimaryStage());
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(languageHelper.getString(title));
        dialog.setHeaderText(languageHelper.getString(header));
        DialogPane dialogPane = dialog.getDialogPane();

        GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);
        ObservableList<String> stylesheets = dialogPane.getStylesheets();
        String[] existingStylesheets = new String[stylesheets.size()];
        stylesheets.toArray(existingStylesheets);
        stylesheets.clear();
        stylesheets.addAll(generalSettings.themeProperty().get().getStylesheets());
        stylesheets.addAll(existingStylesheets);

        for (ButtonType type : dialogPane.getButtonTypes()) {
            Button btnStyles = (Button)dialogPane.lookupButton(type);
            if (type.getButtonData().isDefaultButton()) {
                btnStyles.getStyleClass().add("primary-button");
            } else {
                btnStyles.getStyleClass().add("secondary-button");
            }
        }

        if (wait) {
            dialog.showAndWait();
        } else {
            dialog.show();
        }
    }

    @Override
    public Path requestFileOpenDialog(@Nullable ViewModel ownerViewModel) {
        return requestFileChooserImpl(ownerViewModel, false, false, null, null, null, null)[0];
    }

    @Override
    public Path requestFileOpenDialog(@Nullable ViewModel ownerViewModel, String title) {
        return requestFileChooserImpl(ownerViewModel, false, false, title, null, null, null)[0];
    }

    @Override
    public Path requestFileOpenDialog(@Nullable ViewModel ownerViewModel, String title, Path initialFolder) {
        return requestFileChooserImpl(ownerViewModel, false, false, title, null, initialFolder, null)[0];
    }

    @Override
    public Path requestFileOpenDialog(
            @Nullable ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        Path[] files =
            requestFileChooserImpl(ownerViewModel, false, false, title, folderPersistenceToken, initialFolder, null);
        return files[0];
    }

    @Override
    public Path requestFileOpenDialog(
            @Nullable ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... fileFilters) {
        Path[] path = requestFileChooserImpl(ownerViewModel, false, false, title, null, initialFolder, fileFilters);
        return path.length>0?path[0]:null;
    }

    @Override
    public Path requestFileOpenDialog(
            @Nullable ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... fileFilters) {
        Path[] files =
            requestFileChooserImpl(
                ownerViewModel, false, false, title, folderPersistenceToken, initialFolder, fileFilters);
        return files[0];
    }

    @Override
    public Path[] requestMultiFileOpenDialog(@Nullable ViewModel ownerViewModel) {
        return requestFileChooserImpl(ownerViewModel, false, true, null, null, null, null);
    }

    @Override
    public Path[] requestMultiFileOpenDialog(@Nullable ViewModel ownerViewModel, String title) {
        return requestFileChooserImpl(ownerViewModel, false, true, title, null, null, null);
    }

    @Override
    public Path[] requestMultiFileOpenDialog(@Nullable ViewModel ownerViewModel, String title, Path initialFolder) {
        return requestFileChooserImpl(ownerViewModel, false, true, title, null, initialFolder, null);
    }

    @Override
    public Path[] requestMultiFileOpenDialog(
            @Nullable ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        return requestFileChooserImpl(ownerViewModel, false, true, title, folderPersistenceToken, initialFolder, null);
    }

    @Override
    public Path[] requestMultiFileOpenDialog(
            @Nullable ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... fileFilters) {
        return requestFileChooserImpl(ownerViewModel, false, true, title, null, initialFolder, fileFilters);
    }

    @Override
    public Path[] requestMultiFileOpenDialog(
            @Nullable ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... fileFilters) {
        return requestFileChooserImpl(
            ownerViewModel, false, true, title, folderPersistenceToken, initialFolder, fileFilters);
    }

    @Override
    public Path requestFileSaveDialog(@Nullable ViewModel ownerViewModel) {
        return requestFileChooserImpl(ownerViewModel, true, false, null, null, null, null)[0];
    }

    @Override
    public Path requestFileSaveDialog(@Nullable ViewModel ownerViewModel, String title) {
        return requestFileChooserImpl(ownerViewModel, true, false, title, null, null, null)[0];
    }

    @Override
    public Path requestFileSaveDialog(@Nullable ViewModel ownerViewModel, String title, Path initialFolder) {
        return requestFileChooserImpl(ownerViewModel, true, false, title, null, initialFolder, null)[0];
    }

    @Override
    public Path requestFileSaveDialog(
            @Nullable ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        return requestFileChooserImpl(ownerViewModel, true, false, title, folderPersistenceToken, initialFolder, null)[
            0];
    }

    @Override
    public Path requestFileSaveDialog(
            @Nullable ViewModel ownerViewModel, String title, Path initialFolder, FileFilter... fileFilters) {
        return requestFileChooserImpl(ownerViewModel, true, false, title, null, initialFolder, fileFilters)[0];
    }

    @Override
    public Path requestFileSaveDialog(
            @Nullable ViewModel ownerViewModel,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... fileFilters) {
        return requestFileChooserImpl(
            ownerViewModel, true, false, title, folderPersistenceToken, initialFolder, fileFilters)[0];
    }

    private Path[] requestFileChooserImpl(
            @Nullable ViewModel ownerViewModel,
            boolean saveDialog,
            boolean openMultipleFiles,
            String title,
            Object folderPersistenceToken,
            Path initialFolder,
            FileFilter... extensionFilter) {
        final FileChooser fileChooser = new FileChooser();

        if (title != null) {
            fileChooser.setTitle(title);
        }

        if (folderPersistenceToken != null) {
            Path currentFolder = dialogCurrentFolder.get(folderPersistenceToken);
            if (currentFolder != null) {
                initialFolder = currentFolder;
            }
        }

        if (initialFolder == null || !Files.exists(initialFolder)) {
            initialFolder = Paths.get(System.getProperty("user.home"));
        }

        fileChooser.setInitialDirectory(initialFolder.toFile());
        if (extensionFilter != null && extensionFilter.length > 0) {
            fileChooser
                .getExtensionFilters()
                .addAll(
                    Arrays.stream(extensionFilter)
                        .map(entry -> new FileChooser.ExtensionFilter(entry.getName(), entry.getExtensions()))
                        .collect(Collectors.toList()));
        }

        Window ownerWindow = null;
        if (ownerViewModel != null) {
            ownerWindow = getWindowFromViewModel(ownerViewModel);
        }

        if (ownerWindow == null) {
            ownerWindow = WindowHelper.getPrimaryStage();
        }

        if (saveDialog) {
            var file = fileChooser.showSaveDialog(ownerWindow);
            return file != null ? new Path[] {file.toPath()} : new Path[0];
        }

        if (openMultipleFiles) {
            List<File> list = fileChooser.showOpenMultipleDialog(ownerWindow);
            Path[] paths =
                list == null || list.isEmpty() ? new Path[0] : list.stream().map(File::toPath).toArray(Path[]::new);

            if (folderPersistenceToken != null && paths.length > 0) {
                dialogCurrentFolder.put(folderPersistenceToken, paths[0].getParent());
            }

            return paths;
        }

        File file = fileChooser.showOpenDialog(ownerWindow);
        if (folderPersistenceToken != null && file != null) {
            dialogCurrentFolder.put(folderPersistenceToken, Paths.get(file.getPath()));
        }

        return file != null ? new Path[] {file.toPath()} : new Path[0];
    }

    @Override
    public Path requestDirectoryChooser(@Nullable ViewModel ownerViewModel) {
        return requestDirectoryChooserImpl(ownerViewModel, null, null, null);
    }

    @Override
    public Path requestDirectoryChooser(@Nullable ViewModel ownerViewModel, String title) {
        return requestDirectoryChooserImpl(ownerViewModel, title, null, null);
    }

    @Override
    public Path requestDirectoryChooser(@Nullable ViewModel ownerViewModel, String title, Path initialFolder) {
        return requestDirectoryChooserImpl(ownerViewModel, title, null, initialFolder);
    }

    @Override
    public Path requestDirectoryChooser(
            @Nullable ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        return requestDirectoryChooserImpl(ownerViewModel, title, folderPersistenceToken, initialFolder);
    }

    @Override
    public <T> Future<T> requestInputDialogAsync(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            StringConverter<T> converter,
            boolean allowEmptyValue) {
        return requestInputDialogImplAsync(ownerViewModel, title, message, null, converter, allowEmptyValue, false);
    }

    @Override
    public String requestInputDialogAndWait(
            ViewModel ownerViewModel, @Nullable String title, @Nullable String message, boolean allowEmptyValue) {
        var future =
            requestInputDialogImplAsync(
                ownerViewModel, title, message, null, new StringStringConverter(), allowEmptyValue, true);
        if (future.isCancelled()) {
            return null;
        }

        return future.getUnchecked();
    }

    @Override
    public <T> T requestInputDialogAndWait(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            @Nullable String defaultInput,
            StringConverter<T> converter,
            boolean allowEmptyValue) {
        var future =
            requestInputDialogImplAsync(ownerViewModel, title, message, defaultInput, converter, allowEmptyValue, true);
        if (future.isCancelled()) {
            return null;
        }

        return future.getUnchecked();
    }

    @Override
    public <T> boolean requestConfirmation(
            ViewModel ownerViewModel, @Nullable String title, @Nullable String message, StringConverter<T> converter) {
        Optional<ButtonType> result =
            requestDialogImpl(
                ownerViewModel,
                title,
                message,
                converter,
                new ButtonType[] {ButtonType.YES, ButtonType.NO},
                null,
                true);
        return result.orElse(ButtonType.NO) == ButtonType.YES;
    }

    @Override
    public <T> boolean requestConfirmation(ViewModel ownerViewModel, @Nullable String title, @Nullable String message) {
        Optional<ButtonType> result =
            requestDialogImpl(
                ownerViewModel,
                title,
                message,
                new DialogService.StringStringConverter(),
                new ButtonType[] {ButtonType.YES, ButtonType.NO},
                null,
                true);
        return result.orElse(ButtonType.NO) == ButtonType.YES;
    }

    @Override
    public <T> boolean requestConfirmation(@Nullable String title, @Nullable String message) {
        Optional<ButtonType> result =
            requestDialogImpl(
                WindowHelper.getPrimaryViewModel(),
                title,
                message,
                new DialogService.StringStringConverter(),
                new ButtonType[] {ButtonType.YES, ButtonType.NO},
                null,
                true);

        return result.orElse(ButtonType.NO) == ButtonType.YES;
    }

    private Optional<ButtonType> requestConfirmation(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            ButtonType[] buttons,
            AlertType information) {
        return requestDialogImpl(
            ownerViewModel, title, message, new DialogService.StringStringConverter(), buttons, information, true);
    }

    private <T> Future<T> requestInputDialogImplAsync(
            ViewModel ownerViewModel,
            @Nullable String title,
            @Nullable String message,
            @Nullable String defaultInput,
            StringConverter<T> converter,
            boolean allowEmptyValue,
            boolean wait) {
        Expect.notNull(ownerViewModel, "ownerViewModel", converter, "converter");

        if (!Platform.isFxApplicationThread()) {
            return Dispatcher.platform()
                .getLaterAsync(
                    () ->
                        requestInputDialogImplAsync(
                            ownerViewModel, title, message, defaultInput, converter, allowEmptyValue, wait))
                .getUnchecked();
        }

        Window ownerWindow = getWindowFromViewModel(ownerViewModel);
        var stage = new Stage();
        stage.setTitle(
            title == null || title.isEmpty()
                ? languageHelper.getString(getClass().getName() + ".defaultDialogTitle")
                : title);
        stage.getIcons().add(WINDOW_ICON);
        var root =
            new GenericInputView<>(languageHelper, message, allowEmptyValue, converter, stage::close, defaultInput);
        root.getStylesheets().addAll(settingsManager.getSection(GeneralSettings.class).getTheme().getStylesheets());
        var scene = new Scene(root);
        stage.setScene(scene);
        stage.initOwner(ownerWindow);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.UTILITY);

        var future = SettableFuture.<T>create();
        stage.setOnHidden(
            event -> {
                if (root.value.get() == null) {
                    future.cancel(true);
                } else {
                    future.set(root.value.get());
                }
            });

        if (wait) {
            stage.showAndWait();
        } else {
            stage.show();
        }

        return Futures.fromListenableFuture(future);
    }

    private Path requestDirectoryChooserImpl(
            @Nullable ViewModel ownerViewModel, String title, Object folderPersistenceToken, Path initialFolder) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();

        if (title != null) {
            directoryChooser.setTitle(title);
        }

        if (folderPersistenceToken != null) {
            Path currentFolder = dialogCurrentFolder.get(folderPersistenceToken);
            if (currentFolder != null) {
                initialFolder = currentFolder;
            }
        }

        if (initialFolder == null || !Files.exists(initialFolder)) {
            initialFolder = Paths.get(System.getProperty("user.home"));
        }

        directoryChooser.setInitialDirectory(initialFolder.toFile());
        Window ownerWindow = null;
        if (ownerViewModel != null) {
            ownerWindow = getWindowFromViewModel(ownerViewModel);
        }

        if (ownerWindow == null) {
            ownerWindow = WindowHelper.getPrimaryStage();
        }

        File file = directoryChooser.showDialog(ownerWindow);
        if (folderPersistenceToken != null && file != null) {
            dialogCurrentFolder.put(folderPersistenceToken, Paths.get(file.getPath()));
        }

        return file != null ? file.toPath() : null;
    }

    private Window getWindowFromViewModel(ViewModel viewModel) {
        GetWindowRequest getWindowRequest = new GetWindowRequest();
        viewModel.publish(GET_WINDOW_REQUEST, getWindowRequest);
        Window ownerWindow = getWindowRequest.getWindow();
        if (ownerWindow == null) {
            throw new RuntimeException(
                "The view associated with the calling view model did not provide its containing window. "
                    + "Please ensure that it extends "
                    + ViewBase.class.getName()
                    + " or one of its subclasses.");
        }

        return ownerWindow;
    }

    @Override
    public void showInfoMessage(String title, String message) {
        requestConfirmation(
            WindowHelper.getPrimaryViewModel(),
            title,
            message,
            new ButtonType[] {ButtonType.OK},
            AlertType.INFORMATION);
        return;
    }

    @Override
    public void showWarningMessage(String title, String message) {
        requestConfirmation(
            WindowHelper.getPrimaryViewModel(), title, message, new ButtonType[] {ButtonType.OK}, AlertType.WARNING);
        return;
    }

    @Override
    public void showErrorMessage(String title, String message) {
        requestConfirmation(
            WindowHelper.getPrimaryViewModel(), title, message, new ButtonType[] {ButtonType.OK}, AlertType.ERROR);
        return;
    }

    @Override
    public DialogResult requestCancelableConfirmation(String title, String message) {
        Optional<ButtonType> result =
            requestConfirmation(
                WindowHelper.getPrimaryViewModel(),
                title,
                message,
                new ButtonType[] {ButtonType.YES, ButtonType.NO, ButtonType.CANCEL},
                AlertType.CONFIRMATION);
        if (!result.isPresent() || result.get() == ButtonType.CANCEL) {
            return DialogResult.CANCEL;
        }

        return result.get() == ButtonType.YES ? DialogResult.YES : DialogResult.NO;
    }

    @Override
    public DialogResult requestCancelableConfirmation(String title, String message, ButtonType[] buttons) {
        Optional<ButtonType> result =
            requestConfirmation(WindowHelper.getPrimaryViewModel(), title, message, buttons, AlertType.CONFIRMATION);

        if (!result.isPresent() || result.get() == buttons[2]) {
            return DialogResult.CANCEL;
        }

        return result.get() == buttons[0] ? DialogResult.YES : DialogResult.NO;
    }

    @Nullable
    public String showChoicesDialog(String header, String okButtonText, List<String> samples) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, samples);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().remove(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType(okButtonText, ButtonBar.ButtonData.OK_DONE));
        setGenericStyle(dialog);
        VeryUglyDialogHelper.initModalityOrPutToFront(dialog);
        return dialog.showAndWait().orElse(null);
    }

    @Override
    public void openDirectory(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (Exception e) {
            LOGGER.error("Cannot open path \"" + path + "\" due to", e);
        }
    }

    private void setGenericStyle(Dialog dialog) {
        dialog.initStyle(StageStyle.UTILITY);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().clear();
        dialogPane
            .getStylesheets()
            .add(getClass().getResource("/com/intel/missioncontrol/styles/controls.css").toExternalForm());
        dialogPane
            .getStylesheets()
            .add(getClass().getResource("/com/intel/missioncontrol/styles/themes/colors-light.css").toExternalForm());
        dialogPane
            .getStylesheets()
            .add(getClass().getResource("/com/intel/missioncontrol/styles/dialog.css").toExternalForm());
        dialogPane.getStyleClass().add("st-dialog");

        for (ButtonType type : dialogPane.getButtonTypes()) {
            Button btnStyles = (Button)dialogPane.lookupButton(type);
            if (type.getButtonData().isDefaultButton()) {
                btnStyles.getStyleClass().add("primary-button");
            } else {
                btnStyles.getStyleClass().add("secondary-button");
            }
        }
    }

}
