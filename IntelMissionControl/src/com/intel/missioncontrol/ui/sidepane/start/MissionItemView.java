/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MissionItemView extends ViewBase<MissionItemViewModel> {

    private final ChangeListener<Image> imageChangeListener =
            (observable, oldValue, newValue) -> updateScreenshot(observable.getValue());

    @InjectViewModel
    private MissionItemViewModel viewModel;

    @FXML
    private RadioButton layoutRoot;

    @FXML
    private Label nameLabel;

    @FXML
    private Label lastUpdateLabel;

    @FXML
    private Tooltip fullName;

    @FXML
    private VBox flightPlansInfo;

    @FXML
    private VBox flightsInfo;

    @FXML
    private VBox datasetsInfo;

    @FXML
    private Label flightPlansCountLabel;

    @FXML
    private Label flightsCountLabel;

    @FXML
    private Label datasetsCountLabel;

    @FXML
    private Pane imagePane;

    private final ILanguageHelper languageHelper;

    @Inject
    public MissionItemView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        layoutRoot.getStyleClass().remove("radio-button");
        layoutRoot.getStyleClass().add("table-row-cell");

        nameLabel.setText(viewModel.getName());
        fullName.setText(languageHelper.getString("com.intel.missioncontrol.ui.sidepane.start.MissionItemView.tooltipPrefix")+ viewModel.getName());

        flightPlansInfo.setVisible(viewModel.hasFlightPlans());
        flightPlansInfo.setManaged(viewModel.hasFlightPlans());

        flightsInfo.setVisible(viewModel.hasFlights());
        flightsInfo.setManaged(viewModel.hasFlights());

        datasetsInfo.setVisible(viewModel.hasDatasets());
        datasetsInfo.setManaged(viewModel.hasDatasets());

        flightPlansCountLabel.setText(String.valueOf(viewModel.getFlightPlansCount()));
        flightsCountLabel.setText(String.valueOf(viewModel.getFlighsCount()));
        datasetsCountLabel.setText(String.valueOf(viewModel.getDatasetsCount()));

        lastUpdateLabel.setText(viewModel.lastUpdateProperty().get());
        Image image = viewModel.imageProperty().get();
        viewModel.imageProperty().addListener(new WeakChangeListener<>(imageChangeListener));
        updateScreenshot(image);
    }

    private void updateScreenshot(Image image) {
        final double aspectRatio = image.getWidth() / image.getHeight();

        imagePane
                .prefWidthProperty()
                .bind(Bindings.createDoubleBinding(() -> imagePane.getHeight() * aspectRatio, imagePane.heightProperty()));

        imagePane.setCache(true);
        imagePane.setCacheHint(CacheHint.SPEED);

        imagePane.setBackground(
                new Background(
                        new BackgroundImage(
                                image,
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, false))));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

}
