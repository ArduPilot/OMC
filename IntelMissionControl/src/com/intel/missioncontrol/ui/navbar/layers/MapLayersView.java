/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.intel.missioncontrol.ui.navbar.layers;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.controls.ItemsView;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.NavBarDialogView;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;

public class MapLayersView extends NavBarDialogView<MapLayersViewModel> {

    @InjectViewModel
    private MapLayersViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Pane layoutRoot;

    @FXML
    private Parent transformRoot;

    @FXML
    private ItemsView<LayerViewModel> itemsView;

    @FXML
    private MenuItem clearTrackLog;

    @FXML
    private MenuItem clearUavImageCache;

    @FXML
    private MenuItem clearAll;

    private final IDialogContextProvider dialogContextProvider;

    @Inject
    public MapLayersView(IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);

        itemsView.addViewFactory(
            SimpleLayerViewModel.class,
            vm -> FluentViewLoader.javaView(SimpleLayerView.class).viewModel(vm).load().getView());

        itemsView.addViewFactory(
            LayerGroupViewModel.class,
            vm -> FluentViewLoader.javaView(LayerGroupView.class).viewModel(vm).load().getView());

        itemsView.itemsProperty().bind(viewModel.layerItemsProperty());

        clearAll.visibleProperty().bind(viewModel.clearMenuItemsVisibleProperty());
        clearTrackLog.visibleProperty().bind(viewModel.clearMenuItemsVisibleProperty());
        clearUavImageCache.visibleProperty().bind(viewModel.clearMenuItemsVisibleProperty());
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected Parent getTransformRoot() {
        return transformRoot;
    }

    @Override
    protected DialogViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    private void onCloseClicked() {
        viewModel.getCloseCommand().execute();
    }

    @FXML
    private void onRestoreDefaultsClicked() {
        viewModel.getRestoreDefaultsCommand().execute();
    }

    @FXML
    private void onOpenExternalSourceClicked() {
        viewModel.getOpenExternalSourcesCommand().execute();
    }

    @FXML
    public void addWmsServerClicked() {
        viewModel.getAddWmsServerCommand().execute();
    }

    @FXML
    public void addKmlShpFileClicked() {
        viewModel.getAddKmlShpFileCommand().execute();
    }

    @FXML
    public void clearAllClicked() {
        viewModel.getClearAllCommand().execute();
    }

    @FXML
    public void clearUavImageCacheClicked() {
        viewModel.getClearUavImageCacheCommand().execute();
    }

    @FXML
    public void clearTrackLogClicked() {
        viewModel.getClearTrackLogCommand().execute();
    }

}
