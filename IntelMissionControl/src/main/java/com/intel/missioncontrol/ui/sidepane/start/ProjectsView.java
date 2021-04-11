package com.intel.missioncontrol.ui.sidepane.start;

import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.util.Map;
import java.util.WeakHashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;

public class ProjectsView extends FancyTabView<ProjectsViewModel> {

    @InjectViewModel
    private ProjectsViewModel viewModel;

    @FXML
    private ItemsView<ViewModel> itemsView;

    private final Map<ToggleButton, ProjectItemViewModel> map = new WeakHashMap<>();
    private ToggleGroup toggleGroup = new ToggleGroup();
    private ChangeListener<Toggle> toggleChangeListener;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ChangeListener<ProjectItemViewModel> selectedItemChangedListener;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ChangeListener<ObservableList<ViewModel>> itemsListChangedListener;

    @Override
    protected void initializeView() {
        super.initializeView();

        itemsView.addViewFactory(
            ProjectItemViewModel.class,
            vm -> {
                ViewTuple<ProjectItemView, ProjectItemViewModel> viewTuple =
                    FluentViewLoader.fxmlView(ProjectItemView.class).viewModel(vm).load();
                ToggleButton toggleButton = (ToggleButton)viewTuple.getView();
                map.put(toggleButton, viewTuple.getViewModel());
                toggleButton.setToggleGroup(toggleGroup);
                toggleButton.setOnMouseClicked(this::buttonClicked);
                toggleButton.disableProperty().bind(viewModel.getOpenSelectedProjectCommand().runningProperty());
                return viewTuple.getView();
            });

        itemsView.addViewFactory(
            DateItemViewModel.class,
            vm -> FluentViewLoader.javaView(DateItemView.class).viewModel(vm).load().getView());

        itemsView.itemsProperty().bind(viewModel.itemsProperty());

        viewModel
            .itemsProperty()
            .addListener(
                new WeakChangeListener<>(
                    itemsListChangedListener =
                        (observable, oldValue, newValue) -> {
                            if (newValue.size() == 0) {
                                viewModel.selectedItemProperty().set(null);
                                map.clear();
                                toggleGroup = new ToggleGroup();
                                toggleGroup
                                    .selectedToggleProperty()
                                    .addListener(new WeakChangeListener<>(toggleChangeListener));
                            }
                        }));

        toggleGroup
            .selectedToggleProperty()
            .addListener(
                new WeakChangeListener<>(
                    toggleChangeListener =
                        (observable, oldValue, newValue) -> {
                            if (newValue instanceof ToggleButton) {
                                ProjectItemViewModel vm = map.get(newValue);
                                viewModel.selectedItemProperty().set(vm);
                            }
                        }));

        viewModel
            .selectedItemProperty()
            .addListener(
                new WeakChangeListener<>(
                    selectedItemChangedListener =
                        (observable, oldValue, newValue) -> {
                            for (Toggle toggle : toggleGroup.getToggles()) {
                                ToggleButton toggleButton = (ToggleButton)toggle;
                                if (map.get(toggleButton) == newValue) {
                                    toggle.setSelected(true);
                                }
                            }
                        }));
    }

    @Override
    public ProjectsViewModel getViewModel() {
        return viewModel;
    }

    private void buttonClicked(MouseEvent event) {
        if (event.getClickCount() < 2) {
            return;
        }

        if (viewModel.getOpenSelectedProjectCommand().isExecutable()) {
            viewModel.getOpenSelectedProjectCommand().executeAsync();
        }
    }

}
