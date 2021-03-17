/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.settings.RtkBasePosition;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.AutoCommitNumberTextFieldTableCell;
import com.intel.missioncontrol.ui.controls.AutoCommitTextFieldTableCell;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.RTKBasePositionsSettingsViewModel;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class RTKBasePositionsSettingsView extends ViewBase<RTKBasePositionsSettingsViewModel> {

    @InjectViewModel
    private RTKBasePositionsSettingsViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    public VBox layoutRoot;

    @FXML
    private TableView<RtkBasePosition> settingsTable;

    @FXML
    private TableColumn<RtkBasePosition, RtkBasePosition> name;

    @FXML
    private TableColumn<RtkBasePosition, RtkBasePosition> lonORx;

    @FXML
    private TableColumn<RtkBasePosition, RtkBasePosition> latORy;

    @FXML
    private TableColumn<RtkBasePosition, RtkBasePosition> alstWSG84ORz;

    @FXML
    private TableColumn<RtkBasePosition, RtkBasePosition> antennaHeight;

    @FXML
    private TableColumn<RtkBasePosition, Hyperlink> srs;

    @FXML
    private TableColumn<RtkBasePosition, RtkBasePosition> geoidOffset;

    @FXML
    private TableColumn<RtkBasePosition, ImageView> deleteRow;

    private final IDialogContextProvider dialogContextProvider;

    @Inject
    public RTKBasePositionsSettingsView(IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
    }

    private float calculateColumnWidth(float columnWidth) {
        final float scaleIncrement = 0.25f;
        final float scaleFactor = (float)ScaleHelper.getScaleFactor() - 1;

        return columnWidth + (scaleFactor / scaleIncrement);
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        final float NAME_COLUMN_WIDTH_COEFFICIENT = 89f / 755;
        final float LONORX_COLUMN_WIDTH_COEFFICIENT = calculateColumnWidth(39f) / 755;
        final float LATORY_COLUMN_WIDTH_COEFFICIENT = calculateColumnWidth(39f) / 755;
        final float ALSTWSG84ORZ_COLUMN_WIDTH_COEFFICIENT = 90f / 755;
        final float ANTENNAHEIGHT_COLUMN_WIDTH_COEFFICIENT = 83f / 755;
        final float SRSTARGET_COLUMN_WIDTH_COEFFICIENT = 62f / 755;
        final float GEOIDOFFSET_COLUMN_WIDTH_COEFFICIENT = 64f / 755;
        final double DELETEROW_BUTTON_WIDTH = ScaleHelper.emsToPixels(1);
        final double DELETEROW_BUTTON_HEIGHT = ScaleHelper.emsToPixels(1);

        final double SETTINGS_TABLE_HEIGHT_SUBSTRACTOR = ScaleHelper.emsToPixels(10);
        final double SETTINGS_TABLE_CELL_HEIGHT = ScaleHelper.emsToPixels(2.20);

        name.minWidthProperty().bind(settingsTable.widthProperty().multiply(NAME_COLUMN_WIDTH_COEFFICIENT));
        lonORx.minWidthProperty().bind(settingsTable.widthProperty().multiply(LONORX_COLUMN_WIDTH_COEFFICIENT));
        latORy.minWidthProperty().bind(settingsTable.widthProperty().multiply(LATORY_COLUMN_WIDTH_COEFFICIENT));
        alstWSG84ORz
            .minWidthProperty()
            .bind(settingsTable.widthProperty().multiply(ALSTWSG84ORZ_COLUMN_WIDTH_COEFFICIENT));
        antennaHeight
            .minWidthProperty()
            .bind(settingsTable.widthProperty().multiply(ANTENNAHEIGHT_COLUMN_WIDTH_COEFFICIENT));
        srs.minWidthProperty().bind(settingsTable.widthProperty().multiply(SRSTARGET_COLUMN_WIDTH_COEFFICIENT));
        geoidOffset
            .minWidthProperty()
            .bind(settingsTable.widthProperty().multiply(GEOIDOFFSET_COLUMN_WIDTH_COEFFICIENT));

        name.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        name.setCellFactory(callback -> new AutoCommitTextFieldTableCell<>(RtkBasePosition::nameProperty));

        lonORx.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        lonORx.setCellFactory(callback -> new AutoCommitNumberTextFieldTableCell<>(RtkBasePosition::lonOrXProperty));

        latORy.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        latORy.setCellFactory(callback -> new AutoCommitNumberTextFieldTableCell<>(RtkBasePosition::latOrYProperty));

        alstWSG84ORz.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        alstWSG84ORz.setCellFactory(
            callback -> new AutoCommitNumberTextFieldTableCell<>(RtkBasePosition::altOrZProperty));

        antennaHeight.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        antennaHeight.setCellFactory(
            callback -> new AutoCommitNumberTextFieldTableCell<>(RtkBasePosition::antennaHeightProperty));

        srs.setCellValueFactory(
            dataItem -> {
                Hyperlink link = new Hyperlink();
                link.textProperty().bind(dataItem.getValue().srsTargetProperty());
                link.onActionProperty()
                    .set(event -> viewModel.onSrsLinkClicked(dataItem.getValue().srsTargetProperty()));
                return new SimpleObjectProperty<>(link);
            });

        geoidOffset.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        geoidOffset.setCellFactory(
            callback -> new AutoCommitNumberTextFieldTableCell<>(RtkBasePosition::geoIdOffsetProperty));

        deleteRow.setCellValueFactory(
            dataItem -> {
                ImageView imageview =
                    new ImageView(
                        new Image("/com/intel/missioncontrol/icons/icon_close(fill=theme-button-text-color).svg"));
                imageview.setFitWidth(DELETEROW_BUTTON_WIDTH);
                imageview.setFitHeight(DELETEROW_BUTTON_HEIGHT);
                imageview.setPreserveRatio(true);
                imageview.setVisible(true);
                return new SimpleObjectProperty<>(imageview);
            });

        settingsTable.itemsProperty().bind(viewModel.settingsListProperty());

        settingsTable
            .prefHeightProperty()
            .bind(layoutRoot.heightProperty().subtract((SETTINGS_TABLE_HEIGHT_SUBSTRACTOR)));

        settingsTable.setFixedCellSize(SETTINGS_TABLE_CELL_HEIGHT);
        settingsTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected RTKBasePositionsSettingsViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    private void addRowClicked(final ActionEvent event) {
        viewModel.addSetting();
    }

    @FXML
    public void onMouseClicked(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || event.getClickCount() != 1) {
            return;
        }

        Node selectedNode = event.getPickResult().getIntersectedNode();
        if (selectedNode == null || !(selectedNode instanceof TableCell)) {
            return;
        }

        TableCell selectedCell = (TableCell)selectedNode;
        if (selectedCell.getTableColumn() != deleteRow) {
            return;
        }

        final int selectedRowIndex = selectedCell.getTableRow().getIndex();
        if (viewModel.settingsListProperty().size() <= selectedRowIndex) {
            return;
        }

        viewModel.deleteSetting(selectedRowIndex);
    }
}
