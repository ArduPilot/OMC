/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.warnings;

import com.intel.missioncontrol.ui.RootView;
import com.intel.missioncontrol.ui.controls.ItemsView;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

@SuppressWarnings("WeakerAccess")
public class WarningsPopoverView extends RootView<WarningsPopoverViewModel> {

    // Class needs to be public, otherwise it can't be properly initialized.
    public static class WarningItemView extends HBox implements JavaView<WarningItemViewModel> {

        @InjectViewModel
        private WarningItemViewModel viewModel;

        public void initialize() {
            Label messageLabel = new Label();
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(Double.POSITIVE_INFINITY);
            messageLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            messageLabel.textProperty().bind(viewModel.messageProperty());

            HBox actions = new HBox();
            actions.setSpacing(10);

            var firstResolveActionCommand = viewModel.getFirstResolveActionCommand();
            if (firstResolveActionCommand != null) {
                Hyperlink link = new Hyperlink();
                link.textProperty().bind(viewModel.firstResolveActionTextProperty());
                link.setOnAction(event -> firstResolveActionCommand.execute());
                actions.getChildren().add(link);
            }

            var secondResolveActionCommand = viewModel.getSecondResolveActionCommand();
            if (secondResolveActionCommand != null) {
                Hyperlink link = new Hyperlink();
                link.textProperty().bind(viewModel.secondResolveActionTextProperty());
                link.setOnAction(event -> secondResolveActionCommand.execute());
                actions.getChildren().add(link);
            }

            VBox container = new VBox(messageLabel, actions);
            container.setMaxWidth(Double.POSITIVE_INFINITY);
            switch (viewModel.categoryProperty().get()) {
            case BLOCKING:
                {
                    container.getStyleClass().add("icon-warning");
                    break;
                }
            case NORMAL:
                {
                    break;
                }
            case NOTICE:
                {
                    container.getStyleClass().add("notice");
                    break;
                }
            }

            getChildren().add(container);
        }

    }

    public static class SeparatorViewModel implements ViewModel {}

    public static class SeparatorView extends Separator implements JavaView<SeparatorViewModel> {
        @SuppressWarnings("unused")
        public void initialize() {
            // MvvmFX complains if there's no initialize method
        }
    }

    @InjectViewModel
    private WarningsPopoverViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private ItemsView<ViewModel> itemsView;

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private final ObservableList<ViewModel> renderList = FXCollections.observableArrayList();
    private final ListChangeListener<WarningItemViewModel> listChangeListener = change -> updateList();

    @Override
    protected void initializeView() {
        super.initializeView();

        viewModel.warningsProperty().addListener(new WeakListChangeListener<>(listChangeListener));
        updateList();

        itemsView.addViewFactory(
            WarningItemViewModel.class,
            vm -> FluentViewLoader.javaView(WarningItemView.class).viewModel(vm).load().getView());
        itemsView.addViewFactory(
            SeparatorViewModel.class,
            vm -> FluentViewLoader.javaView(SeparatorView.class).viewModel(vm).load().getView());
        itemsView.setItems(renderList);
    }

    private void updateList() {
        renderList.clear();
        int count = viewModel.warningsProperty().size();
        for (int i = 0; i < count; ++i) {
            renderList.add(viewModel.warningsProperty().get(i));
            if (i < count - 1) {
                renderList.add(new SeparatorViewModel());
            }
        }
    }

}
