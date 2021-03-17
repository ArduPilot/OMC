/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.ViewHelper;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.controls.ToggleButton;
import com.intel.missioncontrol.ui.sidepane.planning.widgets.FlightDirectionWidgetView;
import de.saxsys.mvvmfx.InjectViewModel;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.helper.Pair;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class AoiTransformationTabView extends ViewBase<AoiTransformationTabViewModel> {

    @FXML
    private Pane rootPane;

    @FXML
    private ToggleButton chooseRefPointPositionButton;

    @FXML
    private Spinner<VariantQuantity> refPointLatitudeSpinner;

    @FXML
    private Spinner<VariantQuantity> refPointLongitudeSpinner;

    @FXML
    private Spinner<Quantity<Dimension.Angle>> customDegreesSelector;

    @FXML
    private Spinner<Quantity<Dimension.Length>> refPointElevationSpinner;

    @FXML
    private AutoCommitSpinner<Quantity<Dimension.Percentage>> scale;

    @FXML
    private ImageView imgModelDirection;

    @FXML
    private ComboBox<CPicArea.ModelAxisAlignment> locationX;

    @FXML
    private ComboBox<CPicArea.ModelAxisAlignment> locationY;

    @FXML
    private ComboBox<CPicArea.ModelAxisAlignment> locationZ;

    @FXML
    private Spinner<Quantity<Dimension.Length>> offsetX;

    @FXML
    private Spinner<Quantity<Dimension.Length>> offsetY;

    @FXML
    private Spinner<Quantity<Dimension.Length>> offsetZ;

    @FXML
    private ComboBox<CPicArea.ModelAxis> swapSource;

    @FXML
    private ComboBox<CPicArea.ModelAxis> swapTarget;

    @FXML
    private TableColumn<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>, CPicArea.ModelAxis> sourceColumn;

    @FXML
    private TableColumn<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>, CPicArea.ModelAxis> targetColumn;

    @FXML
    private TableColumn<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>, Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>>
        deleteColumn;

    @FXML
    private TableView<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>> swapsTable;

    @InjectViewModel
    private AoiTransformationTabViewModel viewModel;

    private final IQuantityStyleProvider quantityStyleProvider;
    private final ISettingsManager settingsManager;
    private final ILanguageHelper languageHelper;
    private final EnumConverter<CPicArea.ModelAxisAlignment> converterAlignment;
    private final EnumConverter<CPicArea.ModelAxis> converterAxis;

    @Inject
    public AoiTransformationTabView(ISettingsManager settingsManager, ILanguageHelper languageHelper) {
        this.settingsManager = settingsManager;
        this.quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        this.languageHelper = languageHelper;
        this.converterAlignment = new EnumConverter<>(languageHelper, CPicArea.ModelAxisAlignment.class);
        this.converterAxis = new EnumConverter<>(languageHelper, CPicArea.ModelAxis.class);
    }

    @Override
    public void initializeView() {
        super.initializeView();

        chooseRefPointPositionButton
            .selectedProperty()
            .bindBidirectional(viewModel.chooseRefPointButtonPressedProperty());
        ViewHelper.initCoordinateSpinner(
            refPointLatitudeSpinner, viewModel.refPointLatitudeProperty(), quantityStyleProvider, true);

        ViewHelper.initCoordinateSpinner(
            refPointLongitudeSpinner, viewModel.refPointLongitudeProperty(), quantityStyleProvider, false);

        ViewHelper.initAutoCommitSpinner(
            refPointElevationSpinner,
            viewModel.refPointElevationProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            5,
            0.0,
            1000.0,
            1.0,
            false);

        ViewHelper.initAutoCommitSpinner(
            scale,
            viewModel.scaleQuantityProperty(),
            Unit.PERCENTAGE,
            settingsManager.getSection(GeneralSettings.class),
            1,
            0.,
            10000.,
            10.,
            false);

        imgModelDirection.rotateProperty().bind(viewModel.oridingYawProperty());
        imgModelDirection.setFitHeight(FlightDirectionWidgetView.IMAGE_FLIGHT_DIRECTION_FIT_HEIGHT);

        locationX.getItems().addAll(CPicArea.ModelAxisAlignment.values());
        locationX.setConverter(converterAlignment);
        locationX.valueProperty().bindBidirectional(viewModel.modelAxisAlignmentXProperty());
        locationY.getItems().addAll(CPicArea.ModelAxisAlignment.values());
        locationY.setConverter(converterAlignment);
        locationY.valueProperty().bindBidirectional(viewModel.modelAxisAlignmentYProperty());
        locationZ.getItems().addAll(CPicArea.ModelAxisAlignment.values());
        locationZ.setConverter(converterAlignment);
        locationZ.valueProperty().bindBidirectional(viewModel.modelAxisAlignmentZProperty());

        ViewHelper.initAutoCommitSpinner(
            offsetX,
            viewModel.modelAxisOffsetXQuantitiyProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            2,
            -1000.,
            1000.,
            1.,
            false);

        ViewHelper.initAutoCommitSpinner(
            offsetY,
            viewModel.modelAxisOffsetYQuantitiyProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            2,
            -1000.,
            1000.,
            1.,
            false);

        ViewHelper.initAutoCommitSpinner(
            offsetZ,
            viewModel.modelAxisOffsetZQuantitiyProperty(),
            Unit.METER,
            settingsManager.getSection(GeneralSettings.class),
            2,
            -1000.,
            1000.,
            1.,
            false);

        ViewHelper.initAutoCommitSpinner(
            customDegreesSelector,
            viewModel.originYawQuantityProperty(),
            Unit.DEGREE,
            settingsManager.getSection(GeneralSettings.class),
            2,
            FlightDirectionWidgetView.DIRECTION_MIN,
            FlightDirectionWidgetView.DIRECTION_MAX,
            FlightDirectionWidgetView.DIRECTION_STEP,
            true);

        swapSource.getItems().addAll(CPicArea.ModelAxis.values());
        swapSource.setConverter(converterAxis);
        swapSource.valueProperty().bindBidirectional(viewModel.swapSourceProperty());
        swapTarget.getItems().addAll(CPicArea.ModelAxis.values());
        swapTarget.setConverter(converterAxis);
        swapTarget.valueProperty().bindBidirectional(viewModel.swapTargetProperty());

        sourceColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue().first));
        sourceColumn.setCellFactory(callback -> new ModelAxisCell());

        targetColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue().second));
        targetColumn.setCellFactory(callback -> new ModelAxisCell());

        deleteColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        deleteColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(Pair<CPicArea.ModelAxis, CPicArea.ModelAxis> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            final Button btn = new Button();
                            btn.getStyleClass().addAll("transparent-icon-button", "icon-close");
                            btn.disableProperty().bind(viewModel.getDeleteCommand().notExecutableProperty());
                            btn.setOnAction((e) -> viewModel.getDeleteCommand().execute(item));
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            setGraphic(btn);
                            setAlignment(Pos.CENTER_LEFT);
                        }
                    }
                });

        swapsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        swapsTable.itemsProperty().bind(viewModel.modelAxisTransformationsProperty());
        swapsTable.setEditable(false);
    }

    @Override
    protected Parent getRootNode() {
        return rootPane;
    }

    @Override
    protected AoiTransformationTabViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void onToggleChooseRefPositionClicked() {
        viewModel.getToggleChooseRefPointCommand().execute();
    }

    @FXML
    public void setDirectionFromView() {
        viewModel.setFlightDirectionFromView();
    }

    @FXML
    public void setDirectionFromSecondPoint() {
        viewModel.setDirectionFromSecondPoint();
    }

    @FXML
    public void plus90() {
        viewModel.plus90();
    }

    @FXML
    public void minus90() {
        viewModel.minus90();
    }

    public void addClick(ActionEvent actionEvent) {
        viewModel.addSwap();
    }

    private class ModelAxisCell extends TableCell<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>, CPicArea.ModelAxis> {
        @Override
        protected void updateItem(CPicArea.ModelAxis item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(converterAxis.toString(item));
            }
        }
    }
}
