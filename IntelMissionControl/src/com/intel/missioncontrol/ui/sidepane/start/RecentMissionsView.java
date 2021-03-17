/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.controls.ActivityButton;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class RecentMissionsView extends FancyTabView<RecentMissionsViewModel> {

    @InjectViewModel @MonotonicNonNull
    private RecentMissionsViewModel viewModel;

    @FXML @MonotonicNonNull
    private ItemsView<ViewModel> itemsView;

    @FXML @MonotonicNonNull
    private ActivityButton createMissionButton;

    @FXML @MonotonicNonNull
    private ActivityButton openMissionButton;

    @FXML @MonotonicNonNull
    private ActivityButton cloneMissionButton;

    private ToggleGroup toggleGroup = new ToggleGroup();
    private Map<ToggleButton, MissionItemViewModel> map = new WeakHashMap<>();
    private ChangeListener<MissionItemViewModel> missionItemViewModelChangeListener;
    private ChangeListener<Toggle> toggleChangeListener;
    private ChangeListener<ObservableList<ViewModel>> missionListHasBeenRefreshedListened;

    @Override
    protected void initializeView() {
        super.initializeView();

        itemsView.addViewFactory(
            MissionItemViewModel.class,
            vm -> {
                ViewTuple<MissionItemView, MissionItemViewModel> viewTuple =
                    FluentViewLoader.fxmlView(MissionItemView.class).viewModel(vm).load();
                ToggleButton toggleButton = (ToggleButton)viewTuple.getView();
                map.put(toggleButton, viewTuple.getViewModel());
                toggleButton.setToggleGroup(toggleGroup);
                toggleButton.setOnMouseClicked(this::buttonClicked);
                toggleButton.disableProperty().bind(viewModel.isBusyProperty());
                return viewTuple.getView();
            });

        itemsView.addViewFactory(
            DateItemViewModel.class,
            vm -> FluentViewLoader.javaView(DateItemView.class).viewModel(vm).load().getView());

        itemsView.itemsProperty().bind(viewModel.itemsProperty());
        missionListHasBeenRefreshedListened =
            new ChangeListener<>() {
                @Override
                public void changed(
                        ObservableValue<? extends ObservableList<ViewModel>> observable,
                        ObservableList<ViewModel> oldValue,
                        ObservableList<ViewModel> newValue) {
                    if (newValue.size() == 0) {
                        viewModel.selectedItemProperty().set(null);
                        map = new WeakHashMap<>();
                        toggleGroup = new ToggleGroup();
                        toggleGroup
                            .selectedToggleProperty()
                            .addListener(new WeakChangeListener<>(toggleChangeListener));
                    }
                }
            };
        viewModel.itemsProperty().addListener(new WeakChangeListener<>(missionListHasBeenRefreshedListened));

        toggleChangeListener =
            (observable, oldValue, newValue) -> {
                if (newValue instanceof ToggleButton) {
                    MissionItemViewModel vm = map.get(newValue);
                    viewModel.selectedItemProperty().set(vm);
                }
            };
        toggleGroup.selectedToggleProperty().addListener(new WeakChangeListener<>(toggleChangeListener));

        missionItemViewModelChangeListener =
            (observable, oldValue, newValue) -> {
                for (Toggle toggle : toggleGroup.getToggles()) {
                    ToggleButton toggleButton = (ToggleButton)toggle;
                    if (map.get(toggleButton) == newValue) {
                        toggle.setSelected(true);
                    }
                }
            };
        viewModel.selectedItemProperty().addListener(new WeakChangeListener<>(missionItemViewModelChangeListener));

        createMissionButton.setOnAction(
            event -> invokeButtonCommand(createMissionButton, viewModel.getCreateMissionCommand()));
        createMissionButton.disableProperty().bind(viewModel.getCreateMissionCommand().notExecutableProperty());

        openMissionButton.setOnAction(
            event -> invokeButtonCommand(openMissionButton, viewModel.getOpenMissionCommand()));
        openMissionButton.disableProperty().bind(viewModel.getOpenMissionCommand().notExecutableProperty());

        cloneMissionButton.setOnAction(
            event -> invokeButtonCommand(cloneMissionButton, viewModel.getCloneMissionCommand()));
        cloneMissionButton.disableProperty().bind(viewModel.getCloneMissionCommand().notExecutableProperty());
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private void buttonClicked(MouseEvent event) {
        if (event.getClickCount() < 2) {
            return;
        }

        invokeButtonCommand(openMissionButton, viewModel.getOpenMissionCommand());
    }

    private void invokeButtonCommand(ActivityButton button, ICommand command) {
        final ActivityButton[] allButtons =
            new ActivityButton[] {createMissionButton, openMissionButton, cloneMissionButton};
        Arrays.asList(allButtons)
            .forEach(
                b -> {
                    b.isBusyProperty().unbind();
                    b.setIsBusy(false);
                });

        button.isBusyProperty().bind(viewModel.isBusyProperty());
        command.execute();
    }

    @FXML
    public void openFromDisk() {
        viewModel.openFromDisk();
    }

}
