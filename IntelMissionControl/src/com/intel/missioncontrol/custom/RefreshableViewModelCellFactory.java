/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.custom;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Special factory model which reuses already created views if they are not bound to any cells instead of creating new
 * one.
 *
 * <p>New node is created on demand when there is no free node available. Node is release when cell becomes empty.
 *
 * @param <T> Model class. Models must implement RefreshableModel interface to be able to update used state with the
 *     current one.
 */
public class RefreshableViewModelCellFactory<T extends RefreshableViewModel> implements Callback<ListView<T>, ListCell<T>> {

    private static class ViewRepository {
        private Map<ViewTuple<? extends FxmlView<? extends RefreshableViewModel>, ? extends RefreshableViewModel>, ListCell>
            views = new HashMap<>();
        private Class<? extends FxmlView<RefreshableViewModel>> viewClass;

        public ViewRepository(Class<? extends FxmlView<RefreshableViewModel>> viewClass) {
            this.viewClass = viewClass;
        }

        public void release(ListCell cell) {
            views.entrySet()
                .stream()
                .filter(entry -> cell.equals(entry.getValue()))
                .findFirst()
                .map(entry -> entry.setValue(null));
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public Parent getNode(RefreshableViewModel viewModel, ListCell cell) {
            Optional<ViewTuple<? extends FxmlView<? extends RefreshableViewModel>, ? extends RefreshableViewModel>> viewToUse =
                findViewForCell(cell);
            if (!viewToUse.isPresent()) {
                viewToUse = findFreeCell();
            }

            ViewTuple<? extends FxmlView<? extends RefreshableViewModel>, ? extends RefreshableViewModel> view =
                viewToUse.orElseGet(() -> (ViewTuple)loadView(viewModel));
            views.put(view, cell);
            view.getViewModel().refreshModel(viewModel);
            return view.getView();
        }

        protected Optional<ViewTuple<? extends FxmlView<? extends RefreshableViewModel>, ? extends RefreshableViewModel>>
                findViewForCell(ListCell cell) {
            return (Optional)
                views.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null && entry.getValue().equals(cell))
                    .map(Map.Entry::getKey)
                    .findFirst();
        }

        protected Optional<ViewTuple<? extends FxmlView<? extends RefreshableViewModel>, ? extends RefreshableViewModel>>
                findFreeCell() {
            return (Optional)
                views.entrySet().stream().filter(entry -> entry.getValue() == null).map(Map.Entry::getKey).findFirst();
        }

        private ViewTuple<? extends FxmlView<? extends RefreshableViewModel>, ? extends RefreshableViewModel> loadView(
                RefreshableViewModel viewModel) {
            return FluentViewLoader.fxmlView(viewClass).viewModel(viewModel).load();
        }
    }

    ViewRepository repository;

    public RefreshableViewModelCellFactory(Class<? extends FxmlView<? extends RefreshableViewModel>> viewClass) {
        repository = new ViewRepository((Class<? extends FxmlView<RefreshableViewModel>>)viewClass);
    }

    @Override
    public ListCell call(ListView param) {
        return new ListCell<RefreshableViewModel>() {
            @Override
            protected void updateItem(RefreshableViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    repository.release(this);
                } else {
                    setText(null);
                    Node currentNode = getGraphic();
                    Parent view = repository.getNode(item, this);
                    if (currentNode == null || !currentNode.equals(view)) {
                        setGraphic(view);
                    }
                }
            }

        };
    }
}
