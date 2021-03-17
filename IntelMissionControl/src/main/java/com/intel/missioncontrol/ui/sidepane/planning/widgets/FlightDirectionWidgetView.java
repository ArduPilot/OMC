/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.PlanType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/** Flight Direction Widget class. */
public class FlightDirectionWidgetView extends VBox
        implements Initializable, FxmlView<FlightDirectionWidgetViewModel> {

    public static final double DIRECTION_MIN = 0.0;
    public static final double DIRECTION_MAX = 360.0;
    public static final double DIRECTION_STEP = 1.0;

    public static final double IMAGE_FLIGHT_DIRECTION_FIT_HEIGHT = 24;

    @FXML
    private Text labelFlightDirectionMetric;

    @FXML
    public Label spinnerFlightDirectionLabel;

    @FXML
    private HBox customDegreesPickerSection;

    @FXML
    private AutoCommitSpinner<Quantity<Angle>> customDegreesSelector;

    @FXML
    private ImageView imgFlightDirection;

    @FXML
    private MenuItem fromView;

    @FXML
    private MenuItem fromUav;

    @FXML
    private MenuItem optimizeForTerrainItem;

    @FXML
    private MenuItem shortestPathItem;

    @FXML
    private MenuItem plus90;

    @FXML
    private MenuItem minus90;

    @InjectViewModel
    private FlightDirectionWidgetViewModel viewModel;

    @Inject
    public ILanguageHelper languageHelper;

    @Inject
    private ISettingsManager settingsManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewHelper.initAutoCommitSpinner(
            customDegreesSelector,
            viewModel.flightDirectionQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            0,
            DIRECTION_MIN,
            DIRECTION_MAX,
            DIRECTION_STEP,
            true);

        imgFlightDirection.rotateProperty().bind(viewModel.flightDirectionProperty());

        updateMenuItemsVisibility(viewModel.aoiTypeProperty().getValue());

        viewModel
            .aoiTypeProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    updateMenuItemsVisibility(newValue);
                });
    }

    private void updateMenuItemsVisibility(PlanType type) {
        optimizeForTerrainItem.setVisible(type != null && type.canOptimizeYawForTerrain() && type != PlanType.WINDMILL);
        shortestPathItem.setVisible(type != null && type.canOptimizeYawForTime());
    }

    @FXML
    public void setDirectionFromView() {
        viewModel.setFlightDirectionFromView();
    }

    @FXML
    public void setDirectionFromUav() {
        viewModel.setFlightDirectionFromUav();
    }

    @FXML
    public void setDirectionShortestPath() {
        viewModel.setDirectionShortestPath();
    }

    @FXML
    public void setDirectionOptimizeForTerrain() {
        viewModel.setDirectionOptimizeForTerrain();
    }

    @FXML
    public void plus90() {
        viewModel.plus90();
    }

    @FXML
    public void minus90() {
        viewModel.minus90();
    }

}
