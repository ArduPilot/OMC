/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.connection.RtkStatistic;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.RtkStatisticsViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.Pane;

public class RtkStatisticsView extends ViewBase<RtkStatisticsViewModel> {

    @InjectViewModel
    private RtkStatisticsViewModel viewModel;

    @FXML
    private ScrollPane statisticsPane;

    @FXML
    private TableView<StatisticData> statistics;

    @FXML
    private TableColumn<StatisticData, String> variables;

    @FXML
    private TableColumn<StatisticData, Node> values;

    @FXML
    private TableView<RtkStatistic> detailedStatistics;

    @FXML
    private TableColumn<RtkStatistic, Image> status;

    @Override
    public void initializeView() {
        super.initializeView();

        statisticsPane.visibleProperty().bind(viewModel.isConnectedProperty());

        SimpleListProperty<StatisticData> statisticData = new SimpleListProperty<>(FXCollections.observableArrayList());
        statisticData.bindContent(viewModel.statisticDataProperty());
        statistics.setPrefWidth(ScaleHelper.emsToPixels(7.5 + 15));
        statistics.setItems(statisticData);

        variables.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        variables.setPrefWidth(ScaleHelper.emsToPixels(7.5));
        values.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getNode()));
        values.setPrefWidth(ScaleHelper.emsToPixels(15));

        SimpleListProperty<RtkStatistic> items = new SimpleListProperty<>(FXCollections.observableArrayList());
        items.bindContent(viewModel.detailedStatisticsItemsProperty());
        detailedStatistics.setItems(items);
        status.setCellFactory(
            param ->
                new TableCell<RtkStatistic, Image>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setGraphic(new ImageView(item));
                            setAlignment(Pos.CENTER);
                        }
                    }
                });
        detailedStatistics
            .prefHeightProperty()
            .bind(
                viewModel
                    .detailedStatisticsItemsProperty()
                    .sizeProperty()
                    .multiply(ScaleHelper.emsToPixels(4))
                    .add(ScaleHelper.emsToPixels(3)));
    }

    @Override
    protected Parent getRootNode() {
        return statisticsPane;
    }

    @Override
    public RtkStatisticsViewModel getViewModel() {
        return viewModel;
    }

}
