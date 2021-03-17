/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.AoiSummaryItemView;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.AoiSummaryItemViewModel;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.internal.viewloader.ResourceBundleManager;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class AoiSummaryComponent extends GridPane implements Initializable {

    private final IQuantityStyleProvider quantityStyleProvider;
    private final ILanguageHelper languageHelper;
    private final AreaOfInterest aoi;

    public AoiSummaryComponent(IQuantityStyleProvider quantityStyleProvider, ILanguageHelper languageHelper, AreaOfInterest aoi) {
        this.quantityStyleProvider = quantityStyleProvider;
        this.languageHelper = languageHelper;
        this.aoi = aoi;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AoiSummaryComponent.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setResources(ResourceBundleManager.getInstance().getGlobalResourceBundle());
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AoiSummaryFactory aoiSummaryFactory = new AoiSummaryFactory(quantityStyleProvider, languageHelper);
        AoiSummaryScope scope = aoiSummaryFactory.getAoiSummary(aoi);
        loadItems(scope.getKeyValues());
    }

    private void loadItems(Map<String, ObservableValue<String>> keyValues) {
        int index = 0;

        for (Map.Entry<String, ObservableValue<String>> entry : keyValues.entrySet()) {
            Parent item = loadItem(entry);

            int columnIndex = index % 2;
            int rowIndex = index / 2;
            add(item, columnIndex, rowIndex);

            index++;
        }
    }

    private Parent loadItem(Map.Entry<String, ObservableValue<String>> entry) {
        ViewTuple<AoiSummaryItemView, AoiSummaryItemViewModel> loadedView =
            FluentViewLoader.fxmlView(AoiSummaryItemView.class).load();

        AoiSummaryItemViewModel itemViewModel = loadedView.getViewModel();
        itemViewModel.keyProperty().setValue(entry.getKey());
        itemViewModel.valueProperty().bind(entry.getValue());

        return loadedView.getView();
    }

}
