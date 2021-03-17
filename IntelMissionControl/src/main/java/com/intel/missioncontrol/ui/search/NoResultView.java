/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.search;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

public class NoResultView extends Button implements JavaView<NoResultViewModel> {

    @InjectViewModel
    private NoResultViewModel viewModel;

    @Inject
    private ILanguageHelper languageHelper;

    public void initialize() {
        getStyleClass().add("search-result-button");
        setDisabled(true);
        HBox container = new HBox();
        container.setSpacing(ScaleHelper.emsToPixels(0.5));
        container.setAlignment(Pos.CENTER);
        container
            .getChildren()
            .add(
                new Label(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.search.SearchViewModel.noItemsFound", viewModel.getSearchText())));
        setGraphic(container);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }
}
