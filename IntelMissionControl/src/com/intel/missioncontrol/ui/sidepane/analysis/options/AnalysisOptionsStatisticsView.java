/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.PercentProgressBar;
import com.intel.missioncontrol.ui.sidepane.analysis.options.AnalysisOptionsStatisticsViewModel.CoverageItem;
import com.intel.missioncontrol.ui.sidepane.flight.AlertLevel;
import com.intel.missioncontrol.ui.sidepane.flight.UavIconHelper;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class AnalysisOptionsStatisticsView extends ViewBase<AnalysisOptionsStatisticsViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    private Label lblImagesFormat;

    @FXML
    private Label lblImageResolution;

    @FXML
    private Label lblPassedFilter;

    @FXML
    private ProgressBar passedFilterProgressBar;

    @FXML
    private Label lblToExport;

    @FXML
    private Label lblRtkFixed;

    @FXML
    private Label lblRtkFloat;

    @FXML
    private Label lblSbas;

    @FXML
    private Label lblAuto;

    @FXML
    private TableView<CoverageItem> coverageTable;

    @FXML
    private TableColumn<CoverageItem, String> coverageOrthoColumn;

    @FXML
    private TableColumn<CoverageItem, Boolean> coverageWarningColumn;

    @FXML
    private TableColumn<CoverageItem, String> coverageTotalColumn;

    @FXML
    private TableColumn<CoverageItem, Double> coverageRatioColumn;

    @InjectViewModel
    private AnalysisOptionsStatisticsViewModel viewModel;

    @FXML
    private GridPane rtkTypePanel;

    @FXML
    private Label rtkTypeHeadline;

    @Override
    public void initializeView() {
        super.initializeView();

        rtkTypeHeadline.visibleProperty().bind(viewModel.rtkStatisticVisibleProperty());
        rtkTypeHeadline.managedProperty().bind(viewModel.rtkStatisticVisibleProperty());
        rtkTypePanel.visibleProperty().bind(viewModel.rtkStatisticVisibleProperty());
        rtkTypePanel.managedProperty().bind(viewModel.rtkStatisticVisibleProperty());

        lblImagesFormat.textProperty().bind(viewModel.filteredPictureTypeProperty());
        lblImageResolution.textProperty().bind(viewModel.imageResolutionDescriptionProperty());
        lblPassedFilter.textProperty().bind(viewModel.progressDescriptionProperty());
        passedFilterProgressBar.progressProperty().bind(viewModel.progressProperty());
        lblToExport.textProperty().bind(viewModel.filteredPicturesSizeDescriptionProperty());
        lblRtkFixed.textProperty().bind(viewModel.rtkFixCountProperty().asString());
        lblRtkFloat.textProperty().bind(viewModel.rtkFloatCountProperty().asString());
        lblSbas.textProperty().bind(viewModel.diffGpsFixCountProperty().asString());
        lblAuto.textProperty().bind(viewModel.gpsFixCountProperty().asString());

        initCoverageTable();
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public AnalysisOptionsStatisticsViewModel getViewModel() {
        return viewModel;
    }

    private void initCoverageTable() {
        coverageWarningColumn.setCellFactory(column -> new WarningCell());
        coverageRatioColumn.setCellFactory(column -> new ProgressBarCell());

        coverageTable.itemsProperty().bind(viewModel.coverageItemsProperty());

        coverageTable.setFixedCellSize(ScaleHelper.emsToPixels(2.5));

        coverageTable
            .prefHeightProperty()
            .bind(coverageTable.fixedCellSizeProperty().multiply(Bindings.size(coverageTable.getItems()).add(1.01)));

        coverageTable.minHeightProperty().bind(coverageTable.prefHeightProperty());
        coverageTable.maxHeightProperty().bind(coverageTable.prefHeightProperty());

        coverageOrthoColumn.prefWidthProperty().bind(coverageTable.widthProperty().multiply(0.30));
        coverageWarningColumn.prefWidthProperty().bind(coverageTable.widthProperty().multiply(0.10));
        coverageTotalColumn.prefWidthProperty().bind(coverageTable.widthProperty().multiply(0.25));
        coverageRatioColumn.prefWidthProperty().bind(coverageTable.widthProperty().multiply(0.30));
    }

    private static class ProgressBarCell extends TableCell<CoverageItem, Double> {

        private final PercentProgressBar progressBar = new PercentProgressBar();

        public ProgressBarCell() {
            progressBar.setMaxWidth(Double.POSITIVE_INFINITY);
            setText(null);
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(Double ratio, boolean empty) {
            super.updateItem(ratio, empty);

            if ((empty) || (ratio == null) || ratio < 0) {
                setGraphic(null);
            } else {
                progressBar.setProgress(normalizeRatio(ratio.doubleValue()));
                setGraphic(progressBar);
            }
        }

        private double normalizeRatio(double ratio) {
            ratio = Math.max(ratio, 0.0);
            return Math.min(ratio, 1.0);
        }

    }

    private static class WarningCell extends TableCell<CoverageItem, Boolean> {

        private static final Image IMAGE = new Image(UavIconHelper.getAlertIconSvg(AlertLevel.RED, true));

        public WarningCell() {
            setText(null);
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(Boolean warning, boolean empty) {
            super.updateItem(warning, empty);

            if ((empty) || (warning == null) || (!warning)) {
                setGraphic(null);
            } else {
                ImageView imageView = createImageView();
                setGraphic(imageView);
            }
        }

        private ImageView createImageView() {
            ImageView imageView = new ImageView(IMAGE);
            imageView.setFitHeight(ScaleHelper.emsToPixels(1.1));
            imageView.setFitWidth(ScaleHelper.emsToPixels(1.1));
            return imageView;
        }

    }

}
