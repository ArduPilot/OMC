/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class NavBarView extends ViewBase<NavBarViewModel> {

    public static final BooleanProperty cheeseBurgerMode = new SimpleBooleanProperty();

    public static final int DEFAULT_WIDTH = 48;
    private static final int EXPANDED_WIDTH = 252;
    private static final String NOT_CONNECTED_STYLE = "notConnectedButton";
    private static final String CONNECTED_STYLE = "connectedButton";
    private static final String UNFOCUSED_BUTTON_STYLE = "unfocused-navbar-button";

    @FXML
    private Pane layoutRoot;

    @FXML
    private Button expandButtonRegular;

    @FXML
    private Button expandButtonDeluxe;

    @FXML
    private ToggleGroup workflowToggleGroup;

    @FXML
    private ToggleGroup popupToggleGroup;

    @InjectViewModel
    private NavBarViewModel viewModel;

    @InjectContext
    private Context context;

    private final IDialogContextProvider dialogContextProvider;
    private boolean isExpanded = false;

    @Inject
    public NavBarView(IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected NavBarViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        Expect.notNull(
            expandButtonRegular, "expandButtonRegular",
            expandButtonDeluxe, "expandButtonDeluxe",
            workflowToggleGroup, "workflowToggleGroup",
            popupToggleGroup, "popupToggleGroup");

        dialogContextProvider.setContext(viewModel, context);

        layoutRoot.setMinWidth(DEFAULT_WIDTH);
        layoutRoot.setMaxWidth(DEFAULT_WIDTH);

        bindWorkflowStepButton(getToggleButton(WorkflowStep.PLANNING), WorkflowStep.PLANNING);
        bindWorkflowStepButton(getToggleButton(WorkflowStep.FLIGHT), WorkflowStep.FLIGHT);
        bindWorkflowStepButton(getToggleButton(WorkflowStep.DATA_PREVIEW), WorkflowStep.DATA_PREVIEW);

        bindNavBarDialogButton(getToggleButton(NavBarDialog.MAPLAYERS), NavBarDialog.MAPLAYERS);
        bindNavBarDialogButton(getToggleButton(NavBarDialog.TOOLS), NavBarDialog.TOOLS);
        bindNavBarDialogButton(getToggleButton(NavBarDialog.SETTINGS), NavBarDialog.SETTINGS);
        bindNavBarDialogButton(getToggleButton(NavBarDialog.CONNECTION), NavBarDialog.CONNECTION);

        // When the connection state changes, we update the Id of the connection button to reflect
        // the connection state in the icon.
        viewModel
            .connectionStateProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    getToggleButton(NavBarDialog.CONNECTION)
                        .setId(newValue == ConnectionState.CONNECTED ? CONNECTED_STYLE : NOT_CONNECTED_STYLE);
                });

        getToggleButton(NavBarDialog.CONNECTION)
            .setId(
                viewModel.connectionStateProperty().get() == ConnectionState.CONNECTED
                    ? CONNECTED_STYLE
                    : NOT_CONNECTED_STYLE);

        // When the current page changes programmatically, we need to manually toggle the corresponding radio button.
        //
        viewModel
            .currentWorkflowStepProperty()
            .addListener((observable, oldValue, newValue) -> onWorkflowStepChanged(newValue));

        viewModel
            .currentNavBarDialogProperty()
            .addListener((observable, oldValue, newValue) -> onPopupDialogChanged(newValue));

        // When the user clicks on a radio button and selects it, we need to update the current page property.
        //
        workflowToggleGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        viewModel.navigateTo((WorkflowStep)newValue.getUserData());
                    }
                });

        popupToggleGroup
            .selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        NavBarDialog navbarDialog = (NavBarDialog)newValue.getUserData();
                        viewModel.navigateTo(navbarDialog);
                    } else {
                        viewModel.navigateTo(NavBarDialog.NONE);
                    }
                });

        workflowToggleGroup.getToggles().forEach(toggle -> ((ButtonBase)toggle).setOnAction(evt -> toggleMenu(false)));
        popupToggleGroup.getToggles().forEach(toggle -> ((ButtonBase)toggle).setOnAction(evt -> toggleMenu(false)));

        onWorkflowStepChanged(viewModel.currentWorkflowStepProperty().get());
        onPopupDialogChanged(viewModel.currentNavBarDialogProperty().get());

        layoutRoot.addEventHandler(
            KeyEvent.KEY_RELEASED,
            event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    getViewModel().navigateTo(NavBarDialog.NONE);
                }
            });
    }

    private void bindWorkflowStepButton(Control button, WorkflowStep workflowStep) {
        BooleanBinding disableBinding =
            Bindings.createBooleanBinding(
                () -> !viewModel.workflowStepEnabledStateProperty().getOrDefault(workflowStep, true),
                viewModel.workflowStepEnabledStateProperty());
        button.disableProperty().bind(disableBinding);

        BooleanBinding visibleBinding =
            Bindings.createBooleanBinding(
                () -> viewModel.workflowStepAvailableStateProperty().getOrDefault(workflowStep, true),
                viewModel.workflowStepAvailableStateProperty());
        button.visibleProperty().bind(visibleBinding);
        button.managedProperty().bind(visibleBinding);
    }

    private void bindNavBarDialogButton(Control button, NavBarDialog navBarDialog) {
        BooleanBinding disableBinding =
            Bindings.createBooleanBinding(
                () -> !viewModel.navBarDialogEnabledStateProperty().getOrDefault(navBarDialog, true),
                viewModel.navBarDialogEnabledStateProperty());
        button.disableProperty().bind(disableBinding);

        BooleanBinding visibleBinding =
            Bindings.createBooleanBinding(
                () -> viewModel.navBarDialogAvailableStateProperty().getOrDefault(navBarDialog, true),
                viewModel.navBarDialogAvailableStateProperty());
        button.visibleProperty().bind(visibleBinding);
        button.managedProperty().bind(visibleBinding);
    }

    @FXML
    private void expandWithSpace(KeyEvent event) {
        if (event.getCode().equals(KeyCode.SPACE)) {
            toggleMenu(!isExpanded);
        }
    }

    @FXML
    private void expandClicked(MouseEvent event) {
        if (event.isAltDown() && event.isControlDown()) {
            boolean visible = expandButtonDeluxe.isVisible();
            cheeseBurgerMode.set(!visible);
            expandButtonRegular.setVisible(visible);
            expandButtonRegular.setManaged(visible);
            expandButtonDeluxe.setVisible(!visible);
            expandButtonDeluxe.setManaged(!visible);
        }

        toggleMenu(!isExpanded);
    }

    private void setUnfocusedButtonStyle(WorkflowStep workflowStep) {
        for (Toggle toggle : workflowToggleGroup.getToggles()) {
            ToggleButton button = (ToggleButton)toggle;
            if (toggle.getUserData() == workflowStep) {
                button.getStyleClass().add(UNFOCUSED_BUTTON_STYLE);
            } else {
                button.getStyleClass().remove(UNFOCUSED_BUTTON_STYLE);
            }
        }
    }

    private void onWorkflowStepChanged(WorkflowStep page) {
        popupToggleGroup.getToggles().forEach(toggle -> toggle.setSelected(false));
        if (page != WorkflowStep.NONE) {
            setUnfocusedButtonStyle(WorkflowStep.NONE);
            getToggleButton(page).setSelected(true);
        }
    }

    private void onPopupDialogChanged(NavBarDialog page) {
        if (page != NavBarDialog.NONE) {
            workflowToggleGroup.getToggles().forEach(toggle -> toggle.setSelected(false));
            setUnfocusedButtonStyle(viewModel.currentWorkflowStepProperty().get());
            getToggleButton(page).setSelected(true);
        } else {
            popupToggleGroup.getToggles().forEach(toggle -> toggle.setSelected(false));
            if (viewModel.currentWorkflowStepProperty().get() != WorkflowStep.NONE) {
                getToggleButton(viewModel.currentWorkflowStepProperty().get()).setSelected(true);
                setUnfocusedButtonStyle(WorkflowStep.NONE);
            }
        }
    }

    private ToggleButton getToggleButton(Enum page) {
        Expect.notNull(
            workflowToggleGroup, "workflowToggleGroup",
            popupToggleGroup, "popupToggleGroup");

        for (Toggle toggle : workflowToggleGroup.getToggles()) {
            Object userData = toggle.getUserData();
            if (userData == page) {
                return (ToggleButton)toggle;
            }
        }

        for (Toggle toggle : popupToggleGroup.getToggles()) {
            Object userData = toggle.getUserData();
            if (userData == page) {
                return (ToggleButton)toggle;
            }
        }

        throw new IllegalArgumentException("page");
    }

    private void toggleMenu(boolean expanded) {
        if (isExpanded == expanded) {
            return;
        }

        isExpanded = !isExpanded;
        Pane navBar = layoutRoot;

        KeyValue start1;
        KeyValue start2;
        KeyValue end1;
        KeyValue end2;

        if (isExpanded) {
            start1 = new KeyValue(navBar.minWidthProperty(), DEFAULT_WIDTH);
            start2 = new KeyValue(navBar.maxWidthProperty(), DEFAULT_WIDTH);
            end1 = new KeyValue(navBar.minWidthProperty(), EXPANDED_WIDTH, Interpolator.EASE_OUT);
            end2 = new KeyValue(navBar.maxWidthProperty(), EXPANDED_WIDTH, Interpolator.EASE_OUT);
        } else {
            start1 = new KeyValue(navBar.minWidthProperty(), EXPANDED_WIDTH);
            start2 = new KeyValue(navBar.maxWidthProperty(), EXPANDED_WIDTH);
            end1 = new KeyValue(navBar.minWidthProperty(), DEFAULT_WIDTH, Interpolator.EASE_OUT);
            end2 = new KeyValue(navBar.maxWidthProperty(), DEFAULT_WIDTH, Interpolator.EASE_OUT);
        }

        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.getKeyFrames()
            .addAll(new KeyFrame(Duration.ZERO, start1, start2), new KeyFrame(Duration.seconds(0.1), end1, end2));
        timeline.play();
    }

}
