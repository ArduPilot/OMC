/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.search;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ItemsView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SearchView extends ViewBase<SearchViewModel> {

    @InjectViewModel
    private SearchViewModel viewModel;

    @FXML
    private VBox layoutRoot;

    @FXML
    private TextField searchTextField;

    @FXML
    private Pane activityIndicator;

    @FXML
    private Button clearButton;

    @FXML
    private Button searchButton;

    @FXML
    private Rectangle clippingNode;

    @FXML
    private ItemsView<IResultViewModel> resultItemsView;

    private final ToggleGroup resultsToggleGroup = new ToggleGroup();
    private EventHandler<? super ActionEvent> onActionHandler;
    private boolean currentlyAnimating;
    private boolean animateAfterFinished;

    public void setOnAction(EventHandler<? super ActionEvent> handler) {
        onActionHandler = handler;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        searchTextField.textProperty().bindBidirectional(viewModel.searchTextProperty());

        searchTextField
            .focusedProperty()
            .addListener(
                ((observable, oldValue, newValue) -> {
                    if (newValue) {
                        Platform.runLater(() -> searchTextField.selectAll());
                    }
                }));

        searchTextField.setOnKeyReleased(
            event -> {
                if (event.getCode() == KeyCode.DOWN) {
                    handleSwitchToResultsList();
                } else if (event.getCode() == KeyCode.ENTER) {
                    handleItemClicked();
                }
            });

        activityIndicator.visibleProperty().bind(viewModel.isSearchingProperty());
        clearButton.visibleProperty().bind(viewModel.getClearCommand().executableProperty());
        clearButton.setOnAction(event -> viewModel.getClearCommand().execute());
        searchButton.disableProperty().bind(viewModel.getGoToCommand().notExecutableProperty());
        searchButton.setOnAction(event -> viewModel.getGoToCommand().execute());

        resultItemsView.addViewFactory(
            LocationResultViewModel.class,
            vm -> {
                ViewTuple<LocationResultView, LocationResultViewModel> viewTuple =
                    FluentViewLoader.javaView(LocationResultView.class).viewModel(vm).load();
                initToggleButton(viewTuple.getCodeBehind(), vm);
                return viewTuple.getView();
            },
            node -> resultsToggleGroup.getToggles().remove(node));

        resultItemsView.addViewFactory(
            PlaceResultViewModel.class,
            vm -> {
                ViewTuple<PlaceResultView, PlaceResultViewModel> viewTuple =
                    FluentViewLoader.javaView(PlaceResultView.class).viewModel(vm).load();
                initToggleButton(viewTuple.getCodeBehind(), vm);
                return viewTuple.getView();
            },
            node -> resultsToggleGroup.getToggles().remove(node));

        resultItemsView.addViewFactory(
            NoResultViewModel.class,
            vm -> FluentViewLoader.javaView(NoResultView.class).viewModel(vm).load().getView());

        resultItemsView.addViewFactory(
            SearchHistoryItemViewModel.class,
            vm -> {
                ViewTuple<SearchHistoryItemView, SearchHistoryItemViewModel> viewTuple =
                    FluentViewLoader.javaView(SearchHistoryItemView.class).viewModel(vm).load();
                initToggleButton(viewTuple.getCodeBehind(), vm);
                return viewTuple.getView();
            },
            node -> resultsToggleGroup.getToggles().remove(node));
        resultItemsView.itemsProperty().bind(viewModel.searchResultsProperty());

        viewModel.searchResultsProperty().addListener((observable, oldValue, newValue) -> searchResultsChanged());
        searchResultsChanged();

        resultsToggleGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        IResultViewModel result = (IResultViewModel)newValue.getUserData();
                        viewModel.selectedSearchResultProperty().set(result);
                    }
                });

        initResultsVisibleBinding();
        Animations.spinForever(activityIndicator);
    }

    private void searchResultsChanged() {
        // The animation of the search results box can only start after all the items have been created
        // by the ItemsView. However, there is no way to enforce the order in which listeners of
        // searchResultsProperty() are called, or to be more specific: there is no way to force this
        // listener to be called after the listener that is added by binding the ItemsView's itemsProperty()
        // to viewModel.searchResultsProperty().
        // To circumvent this problem, we start the animation one frame after the current frame.
        Platform.runLater(
            () -> {
                initResultsVisibleBinding();
                animateSearchResultsHeight();
            });
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    public SearchViewModel getViewModel() {
        return viewModel;
    }

    private void initToggleButton(ToggleButton button, IResultViewModel vm) {
        button.setToggleGroup(resultsToggleGroup);
        button.setUserData(vm);
        button.setOnMouseEntered(event -> resultsToggleGroup.selectToggle(button));
        button.setOnMouseClicked(event -> handleItemClicked());
        button.setOnKeyReleased(
            event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleItemClicked();
                }
            });
    }

    private void initResultsVisibleBinding() {
        List<ReadOnlyBooleanProperty> observables =
            resultsToggleGroup
                .getToggles()
                .stream()
                .map(toggle -> ((ToggleButton)toggle).focusedProperty())
                .collect(Collectors.toCollection(ArrayList::new));

        observables.add(searchTextField.focusedProperty());
        observables.add(viewModel.searchResultsProperty().emptyProperty());

        BooleanBinding resultsVisibleBinding =
            Bindings.createBooleanBinding(
                () -> {
                    for (int i = 0; i < observables.size() - 1; ++i) {
                        if (observables.get(i).get()) {
                            return !viewModel.searchResultsProperty().isEmpty();
                        }
                    }

                    return false;
                },
                observables.toArray(new Observable[0]));

        resultItemsView.visibleProperty().unbind();
        resultItemsView.managedProperty().unbind();
        resultItemsView.visibleProperty().bind(resultsVisibleBinding);
        resultItemsView.managedProperty().bind(resultsVisibleBinding);
    }

    private void animateSearchResultsHeight() {
        if (currentlyAnimating) {
            animateAfterFinished = true;
            return;
        }

        double buttonHeight = 0;
        if (!resultItemsView.getItems().isEmpty() && !resultItemsView.getItemsPane().getChildren().isEmpty()) {
            Region button = (Region)resultItemsView.getItemsPane().getChildren().get(0);
            button.applyCss();
            buttonHeight = button.getMaxHeight();
        }

        animateAfterFinished = false;
        currentlyAnimating = true;
        double currentHeight = clippingNode.getHeight();
        double newHeight = resultItemsView.getItems().size() * buttonHeight;
        double currentBackgroundHeight = currentHeight + ScaleHelper.emsToPixels(0.083);
        double newBackgroundHeight = newHeight + ScaleHelper.emsToPixels(0.083);

        Timeline timeline =
            new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(clippingNode.heightProperty(), currentHeight)),
                new KeyFrame(
                    Duration.ZERO, new KeyValue(resultItemsView.prefHeightProperty(), currentBackgroundHeight)),
                new KeyFrame(Duration.ZERO, new KeyValue(resultItemsView.minHeightProperty(), currentBackgroundHeight)),
                new KeyFrame(
                    Duration.millis(150),
                    new KeyValue(clippingNode.heightProperty(), newHeight, Interpolator.EASE_OUT)),
                new KeyFrame(
                    Duration.millis(150), new KeyValue(resultItemsView.prefHeightProperty(), newBackgroundHeight)),
                new KeyFrame(
                    Duration.millis(150), new KeyValue(resultItemsView.minHeightProperty(), newBackgroundHeight)));

        timeline.setOnFinished(
            event -> {
                currentlyAnimating = false;
                if (animateAfterFinished) {
                    animateSearchResultsHeight();
                }
            });

        timeline.play();
    }

    private void handleSwitchToResultsList() {
        if (!resultItemsView.getItems().isEmpty()) {
            Node firstChild = resultItemsView.getItemsPane().getChildren().get(0);
            if (firstChild instanceof ToggleButton) {
                resultsToggleGroup.selectToggle((ToggleButton)firstChild);
                firstChild.requestFocus();
            }
        }
    }

    private void handleItemClicked() {
        if (viewModel.getGoToCommand().isExecutable()) {
            viewModel.getGoToCommand().execute();
            if (onActionHandler != null) {
                onActionHandler.handle(new ActionEvent());
            }
        }
    }

}
