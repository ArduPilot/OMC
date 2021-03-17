/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import de.saxsys.mvvmfx.InjectViewModel;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javax.inject.Inject;

public class FlightplanOptionView extends ViewBase<FlightplanOptionViewModel> {

    @InjectViewModel
    private FlightplanOptionViewModel viewModel;

    @FXML
    private Pane rootNode;

    @FXML
    private TableView<FlightPlan> tableView;

    @FXML
    private TableColumn<FlightPlan, FlightPlan> flightplanActiveColumn;

    @FXML
    private TableColumn<FlightPlan, FlightPlan> flightplanNameColumn;

    @FXML
    private TableColumn<FlightPlan, FlightPlan> flightplanTotalWaypointsColumn;

    @FXML
    private TableColumn<FlightPlan, FlightPlan> flightplanDurationColumn;

    private final QuantityFormat quantityFormat;

    @Inject
    public FlightplanOptionView(ISettingsManager settingsManager) {
        quantityFormat = new AdaptiveQuantityFormat(settingsManager.getSection(GeneralSettings.class));
        quantityFormat.setSignificantDigits(2);
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    public FlightplanOptionViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        configureTable(
            tableView,
            flightplanActiveColumn,
            flightplanNameColumn,
            flightplanTotalWaypointsColumn,
            flightplanDurationColumn);

        tableView.itemsProperty().bind(viewModel.availableFlightPlansListProperty());

        // tableView.getSelectionModel().selectedItemProperty() is readonly, need manual bidirectional binding:
        tableView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> viewModel.selectedFlightplanProperty().setValue(newValue));

        viewModel
            .selectedFlightplanProperty()
            .addListener((observable, oldValue, newValue) -> tableView.selectionModelProperty().get().select(newValue));

    }

    private void configureTable(
            TableView<FlightPlan> tableView,
            TableColumn<FlightPlan, FlightPlan> flightplanActiveColumn,
            TableColumn<FlightPlan, FlightPlan> flightplanNameColumn,
            TableColumn<FlightPlan, FlightPlan> flightplanWaypointsColumn,
            TableColumn<FlightPlan, FlightPlan> flightplanDurationColumn) {
        setCellFactoryForColumn(
            flightplanActiveColumn,
            null,
            item ->
                Bindings.createObjectBinding(
                    () -> {
                        if (item != viewModel.activeFlightplanProperty().getValue()) {
                            return null;
                        } else {
                            ImageView imageView;
                            if (item == viewModel.selectedFlightplanProperty().getValue()) {
                                imageView =
                                    new ImageView(
                                        "/com/intel/missioncontrol/icons/icon_triangle-small(fill=theme-table-cell-text-selected-color).svg");
                            } else {
                                imageView = new ImageView("/com/intel/missioncontrol/icons/icon_triangle-small.svg");
                            }

                            imageView.setFitHeight(ScaleHelper.emsToPixels(2));
                            imageView.setFitWidth(ScaleHelper.emsToPixels(2));
                            return imageView;
                        }
                    },
                    viewModel.activeFlightplanProperty(),
                    viewModel.selectedFlightplanProperty()));

        setCellFactoryForColumn(
            flightplanNameColumn,
            null,
            item ->
                Bindings.createObjectBinding(
                    () -> {
                        HBox hBox = new HBox();
                        hBox.getStyleClass().add("table-cell");
                        //                        hBox.getStyleClass().add("dbg");
                        Label flightPlanNameLabel = new Label(item.getName());
                        Tooltip tooltip = new Tooltip();
                        tooltip.textProperty().set(item.getName());
                        flightPlanNameLabel.setTooltip(tooltip);
                        if (item != viewModel.activeFlightplanProperty().getValue()) {
                            hBox.getChildren().add(flightPlanNameLabel);
                        } else {
                            Number flightPlanProgress = viewModel.activeFlightPlanProgressProperty().getValue();
                            String flightPlanProgressString = " (" + flightPlanProgress + "%)";
                            Label flightPlanProgressLabel = new Label(flightPlanProgressString);
                            flightPlanProgressLabel.setStyle("-fx-font-weight:bold");
                            flightPlanProgressLabel.setMinWidth(ScaleHelper.emsToPixels(3.5));
                            flightPlanProgressLabel.setPrefWidth(ScaleHelper.emsToPixels(3.5));
                            hBox.getChildren().add(flightPlanNameLabel);
                            hBox.getChildren().add(flightPlanProgressLabel);
                        }

                        hBox.setStyle("-fx-padding:0em;");
                        hBox.setAlignment(Pos.CENTER_LEFT);
                        Label combinedLabel = new Label();
                        combinedLabel.setGraphic(hBox);
                        return combinedLabel.getGraphic();
                    },
                    viewModel.activeFlightPlanProgressProperty(),
                    viewModel.activeFlightplanProperty(),
                    viewModel.availableFlightPlansListProperty(),
                    item.nameProperty()));
        // TODO add percentageCompleteByDistance as label at the end of the name and show if flightplan==active, make
        // sure to shorten the name when too long and not the percentage. probably needs to go into graphic binding as
        // it will be two labels or something

        setCellFactoryForColumn(
            flightplanWaypointsColumn,
            null,
            item ->
                Bindings.createObjectBinding(
                    () -> {
                        HBox hBox = new HBox();
                        hBox.getStyleClass().add("table-cell");
                        Label totalWaypointsLabel =
                            new Label(item.waypointsProperty().sizeProperty().getValue().toString());

                        if (item != viewModel.activeFlightplanProperty().getValue()) {
                            hBox.getChildren().add(totalWaypointsLabel);
                        } else {
                            Number currentWaypoint =
                                viewModel.activeNextWaypointIndexProperty().getValue().intValue() + 1;
                            Label currentWaypointLabel = new Label(currentWaypoint.toString());
                            currentWaypointLabel.setStyle("-fx-font-weight:bold");
                            hBox.getChildren().add(currentWaypointLabel);
                            hBox.getChildren().add(new Label("/"));
                            hBox.getChildren().add(totalWaypointsLabel);
                        }

                        hBox.setAlignment(Pos.CENTER);
                        Label combinedLabel = new Label();
                        combinedLabel.setGraphic(hBox);
                        return combinedLabel.getGraphic();
                    },
                    viewModel.activeNextWaypointIndexProperty(),
                    viewModel.activeFlightplanProperty(),
                    viewModel.availableFlightPlansListProperty(),
                    item.waypointsProperty()));

        setCellFactoryForColumn(
            flightplanDurationColumn,
            item ->
                Bindings.createStringBinding(
                    () -> {
                        if (item.getLegacyFlightplan() == null
                                || item.getLegacyFlightplan().getFPsim() == null
                                || item.getLegacyFlightplan().getFPsim().getSimResult() == null) {
                            return null;
                        } else {
                            double flightTimeInSeconds =
                                item.getLegacyFlightplan().getFPsim().getSimResult().flightTime;
                            Quantity<Dimension.Time> flightTime =
                                (Quantity.of(flightTimeInSeconds, Unit.SECOND).convertTo(Unit.MINUTE));
                            return quantityFormat.format(flightTime);
                        }
                    },
                    item.waypointsProperty(),
                    viewModel.availableFlightPlansListProperty()),
            null);

    }

    private void setCellFactoryForColumn(
            TableColumn<FlightPlan, FlightPlan> column,
            Function<FlightPlan, ObservableValue<String>> textBinding,
            Function<FlightPlan, ObservableValue<Node>> graphicBinding) {
        column.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        column.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(FlightPlan item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            if (textBinding != null) {
                                textProperty().unbind();
                                setText(null);
                                setTooltip(null);
                            }

                            if (graphicBinding != null) {
                                graphicProperty().unbind();
                                setGraphic(null);
                            }
                        } else {
                            if (textBinding != null) {
                                textProperty().bind(textBinding.apply(item));
                                Tooltip tooltip = new Tooltip();
                                setWrapText(true);
                                tooltip.textProperty().bind(textProperty());
                                setTooltip(tooltip);
                            }

                            if (graphicBinding != null) {
                                graphicProperty().bind(graphicBinding.apply(item));
                            }
                        }
                    }
                });
        column.setSortable(false);
    }

}
