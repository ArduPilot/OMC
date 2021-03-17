/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Angle;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Speed;
import com.intel.missioncontrol.measure.FillMode;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityArithmetic;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.WayPoint;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.controls.AutoCommitSpinner;
import com.intel.missioncontrol.ui.controls.AutoCommitTextFieldTableCell;
import com.intel.missioncontrol.ui.controls.QuantitySpinnerTableCell;
import com.intel.missioncontrol.ui.controls.QuantitySpinnerValueFactory;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.controls.VariantQuantitySpinnerValueFactory;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import gov.nasa.worldwind.globes.Earth;
import java.util.Comparator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class EditWaypointsView extends DialogView<EditWaypointsViewModel> {

    public static final double LAT_LON_STEP = 0.00001;
    public static final double ALT_STEP = 0.00001;
    public HBox displayCoordinatesBox;

    @InjectViewModel
    private EditWaypointsViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    public CheckBox latCheckbox;

    @FXML
    public AutoCommitSpinner<VariantQuantity> latSpinner;

    @FXML
    public CheckBox lonCheckbox;

    @FXML
    public AutoCommitSpinner<VariantQuantity> lonSpinner;

    @FXML
    public CheckBox altCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Length>> altSpinner;

    @FXML
    public CheckBox rollCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Angle>> rollSpinner;

    @FXML
    public CheckBox pitchCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Angle>> pitchSpinner;

    @FXML
    public CheckBox yawCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Angle>> yawSpinner;

    @FXML
    public ComboBox<EditWaypointsViewModel.TriggerChangeType> triggerComboBox;

    @FXML
    public CheckBox speedCheckbox;

    @FXML
    public AutoCommitSpinner<Quantity<Speed>> speedSpinner;

    @FXML
    public TextField noteTextField;

    @FXML
    private CheckBox selectionCheckBox;

    @FXML
    private TableView<WayPoint> tableView;

    @FXML
    private TableColumn<WayPoint, WayPoint> warningColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> numberInFlightColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> checkedColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> latColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> lonColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> altitudeAboveRColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> altitudeAboveTakeoffColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> rollColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> pitchColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> yawColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> triggerColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> groundDistanceColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> noteColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> speedColumn;

    @FXML
    public TableColumn<WayPoint, WayPoint> stopAtWayPointColumn;

    @FXML
    private TableColumn<WayPoint, WayPoint> deleteColumn;

    @FXML
    public VBox lowerBulkSettingsBox;

    @FXML
    private ToggleSwitch recalculateSwitch;

    @FXML
    private Button deleteWaypointsButton;

    @FXML
    private Button applyButton;

    private final ILanguageHelper languageHelper;
    private final ISettingsManager settingsManager;
    private boolean updatingSelection;

    @Inject
    public EditWaypointsView(ILanguageHelper languageHelper, ISettingsManager settingsManager) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(EditWaypointsView.class.getName() + ".title"));
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        final int maxAngleFractionDigits = 6;
        final int significantAngleDigits = 8;
        final var minYaw = Quantity.of(0, Unit.DEGREE);
        final var maxYaw = Quantity.of(360, Unit.DEGREE);

        IQuantityStyleProvider quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        QuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setMaximumFractionDigits(maxAngleFractionDigits);
        quantityFormat.setSignificantDigits(significantAngleDigits);

        selectionCheckBox.setOnAction(
            event -> selectionCheckBox.getContextMenu().show(selectionCheckBox, Side.BOTTOM, 0, 0));

        lonCheckbox.selectedProperty().bindBidirectional(viewModel.latLonAddCheckedProperty());
        latCheckbox.selectedProperty().bindBidirectional(viewModel.latLonAddCheckedProperty());
        altCheckbox.selectedProperty().bindBidirectional(viewModel.altAddCheckedProperty());
        rollCheckbox.selectedProperty().bindBidirectional(viewModel.rollAddCheckedProperty());
        pitchCheckbox.selectedProperty().bindBidirectional(viewModel.pitchAddCheckedProperty());
        yawCheckbox.selectedProperty().bindBidirectional(viewModel.yawAddCheckedProperty());
        speedCheckbox.selectedProperty().bindBidirectional(viewModel.speedAddCheckedProperty());
        triggerComboBox.valueProperty().bindBidirectional(viewModel.triggerChangeStatusProperty());
        noteTextField.textProperty().bindBidirectional(viewModel.noteChangeProperty());
        triggerComboBox.itemsProperty().bind(viewModel.availableTriggerChangeTypesProperty());

        lowerBulkSettingsBox.disableProperty().bind(viewModel.bulkEditableProperty().not());

        latCheckbox
            .selectedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // When the checkbox is selected, we want to enter a difference in angle/meters
                    if (newValue) {
                        var latChange = viewModel.latChangeProperty().get();
                        switch (latChange.getDimension()) {
                        case LENGTH:
                            {
                            }

                            break;
                        case ANGLE:
                            {
                                double dLat = latChange.convertTo(Unit.DEGREE).getValue().doubleValue();
                                double dMetre =
                                    Math.log(Math.tan(Math.PI / 4 + Math.toRadians(dLat) / 2))
                                        * Earth.WGS84_EQUATORIAL_RADIUS;
                                viewModel.latChangeProperty().set(VariantQuantity.of(dMetre, Unit.METER));
                            }

                            break;
                        default:
                            // do nothing
                            break;
                        }
                    } else {
                        viewModel.latChangeProperty().set(VariantQuantity.of(0, Unit.DEGREE));
                    }
                });

        lonCheckbox
            .selectedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    // When the checkbox is selected, we want to enter a difference in angle/meters
                    if (newValue) {
                        var lonChange = viewModel.lonChangeProperty().get();
                        switch (lonChange.getDimension()) {
                        case LENGTH:
                            {
                            }

                            break;
                        case ANGLE:
                            {
                                double dLon = lonChange.convertTo(Unit.DEGREE).getValue().doubleValue();
                                double dMetre = Math.toRadians(dLon) * Earth.WGS84_EQUATORIAL_RADIUS;
                                viewModel.lonChangeProperty().set(VariantQuantity.of(dMetre, Unit.METER));
                            }

                            break;
                        default:
                            // do nothing
                            break;
                        }
                    } else {
                        viewModel.lonChangeProperty().set(VariantQuantity.of(0, Unit.DEGREE));
                    }
                });

        var angleSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Angle.class,
                Quantity.of(-360, Unit.DEGREE),
                Quantity.of(360, Unit.DEGREE),
                1,
                true,
                significantAngleDigits,
                maxAngleFractionDigits);

        var lengthSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Length.class,
                Quantity.of(-Double.MAX_VALUE, Unit.METER),
                Quantity.of(Double.MAX_VALUE, Unit.METER),
                1,
                true,
                8,
                5);

        {
            var valueFactory =
                new VariantQuantitySpinnerValueFactory(
                    quantityStyleProvider,
                    QuantityArithmetic.LATITUDE,
                    viewModel.latChangeProperty(),
                    angleSettings,
                    lengthSettings);

            latSpinner.setValueFactory(valueFactory);
            latSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new VariantQuantitySpinnerValueFactory(
                    quantityStyleProvider,
                    QuantityArithmetic.LONGITUDE,
                    viewModel.lonChangeProperty(),
                    angleSettings,
                    lengthSettings);
            lonSpinner.setValueFactory(valueFactory);
            lonSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.altChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    Quantity.of(-Double.MAX_VALUE, Unit.METER),
                    Quantity.of(Double.MAX_VALUE, Unit.METER),
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.altChangeProperty());
            altSpinner.setValueFactory(valueFactory);
            altSpinner.setEditable(true);
        }

        {
            var rageRoll = viewModel.getMaxRoll().subtract(viewModel.getMinRoll());
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.rollChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    rageRoll.multiply(-1),
                    rageRoll,
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.rollChangeProperty());
            rollSpinner.setValueFactory(valueFactory);
            rollSpinner.setEditable(true);
        }

        {
            var ragePitch = viewModel.getMaxPitch().subtract(viewModel.getMinPitch());
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.pitchChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    ragePitch.multiply(-1),
                    ragePitch,
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.pitchChangeProperty());
            pitchSpinner.setValueFactory(valueFactory);
            pitchSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.yawChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    maxYaw.multiply(-1),
                    maxYaw,
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.yawChangeProperty());
            yawSpinner.setValueFactory(valueFactory);
            yawSpinner.setEditable(true);
        }

        {
            var valueFactory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    viewModel.speedChangeProperty().getUnitInfo(),
                    significantAngleDigits,
                    maxAngleFractionDigits,
                    viewModel.getMaxSpeedMps().multiply(-1),
                    viewModel.getMaxSpeedMps(),
                    1.0,
                    false);
            valueFactory.valueProperty().bindBidirectional(viewModel.speedChangeProperty());
            speedSpinner.setValueFactory(valueFactory);
            speedSpinner.setEditable(true);
        }

        warningColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        warningColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(WayPoint item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            graphicProperty().unbind();
                            setGraphic(null);
                        } else {
                            ObjectBinding<ImageView> binding =
                                Bindings.createObjectBinding(
                                    () -> {
                                        if (!item.warningProperty().get()) {
                                            return null;
                                        }

                                        ImageView imageView =
                                            new ImageView(
                                                "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg");
                                        imageView.setFitHeight(16);
                                        imageView.setFitWidth(16);
                                        return imageView;
                                    },
                                    item.warningProperty());

                            graphicProperty().bind(binding);
                        }
                    }
                });
        warningColumn.setEditable(false);
        warningColumn.setComparator(Comparator.comparingInt(wp -> wp.warningProperty().get() ? 1 : 0));

        numberInFlightColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        numberInFlightColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(WayPoint item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            textProperty().unbind();
                            setText(null);
                        } else {
                            textProperty().bind(item.numberInFlightProperty().asString());
                        }
                    }
                });
        numberInFlightColumn.setComparator(Comparator.comparingInt(wp -> wp.numberInFlightProperty().get()));

        checkedColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        checkedColumn.setCellFactory(
            callback ->
                new TableCell<>() {

                    CheckBox checkBox;
                    boolean filtersAdded = false;

                    @Override
                    protected void updateItem(WayPoint item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            checkBox = null;
                        } else {
                            checkBox = new CheckBox();
                            checkBox.selectedProperty().bindBidirectional(item.selectedProperty());
                            checkBox.mouseTransparentProperty().set(true);

                            if (!filtersAdded) {
                                addEventFilter(
                                    MouseEvent.MOUSE_PRESSED,
                                    new EventHandler<MouseEvent>() {
                                        @Override
                                        public void handle(MouseEvent event) {
                                            if (!event.isShiftDown()) {
                                                event.consume();
                                            } else {
                                                // nop
                                            }
                                        }
                                    });
                                addEventFilter(
                                    MouseEvent.MOUSE_CLICKED,
                                    new EventHandler<MouseEvent>() {
                                        @Override
                                        public void handle(MouseEvent event) {
                                            if (!event.isShiftDown()) {
                                                checkBox.selectedProperty().set(!checkBox.selectedProperty().get());
                                                event.consume();
                                            } else {
                                                // nop
                                            }
                                        }
                                    });
                                filtersAdded = true;
                            }

                            setGraphic(checkBox);
                        }
                    }
                });

        latColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        latColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<WayPoint, WayPoint, Angle>(
                        quantityStyleProvider,
                        WayPoint::latProperty,
                        Quantity.of(-180, Unit.DEGREE),
                        Quantity.of(180, Unit.DEGREE),
                        8,
                        6,
                        FillMode.ZERO_FILL,
                        LAT_LON_STEP,
                        true);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        latColumn.setComparator(Comparator.comparingDouble(wp -> wp.getLegacyWaypoint().getLat()));

        lonColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        lonColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<WayPoint, WayPoint, Angle>(
                        quantityStyleProvider,
                        WayPoint::lonProperty,
                        Quantity.of(-180, Unit.DEGREE),
                        Quantity.of(180, Unit.DEGREE),
                        8,
                        6,
                        FillMode.ZERO_FILL,
                        LAT_LON_STEP,
                        true);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        lonColumn.setComparator(Comparator.comparingDouble(wp -> wp.getLegacyWaypoint().getLon()));

        altitudeAboveRColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        altitudeAboveRColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<WayPoint, WayPoint, Length>(
                        quantityStyleProvider,
                        WayPoint::altitudeAboveRProperty,
                        Quantity.of(-Double.MAX_VALUE, Unit.METER),
                        Quantity.of(Double.MAX_VALUE, Unit.METER),
                        4,
                        2,
                        FillMode.NO_FILL,
                        1,
                        false);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        altitudeAboveRColumn.setComparator(Comparator.comparingInt(wp -> wp.getLegacyWaypoint().getAlt()));

        altitudeAboveTakeoffColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        altitudeAboveTakeoffColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<WayPoint, WayPoint, Length>(
                        quantityStyleProvider,
                        WayPoint::altAboveTakeoffProperty,
                        Quantity.of(-Double.MAX_VALUE, Unit.METER),
                        Quantity.of(Double.MAX_VALUE, Unit.METER),
                        4,
                        2,
                        FillMode.NO_FILL,
                        1,
                        false);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        altitudeAboveTakeoffColumn.setComparator(Comparator.comparingInt(wp -> wp.getLegacyWaypoint().getAlt()));

        rollColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        rollColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<WayPoint, WayPoint, Angle>(
                        quantityStyleProvider,
                        WayPoint::rollProperty,
                        viewModel.getMinRoll(),
                        viewModel.getMaxRoll(),
                        4,
                        2,
                        FillMode.NO_FILL,
                        1,
                        false);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        rollColumn.setComparator(Comparator.comparingDouble(wp -> wp.getLegacyWaypoint().getOrientation().getRoll()));

        pitchColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        pitchColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<WayPoint, WayPoint, Angle>(
                        quantityStyleProvider,
                        WayPoint::pitchProperty,
                        viewModel.getMinPitch(),
                        viewModel.getMaxPitch(),
                        4,
                        1,
                        FillMode.NO_FILL,
                        1,
                        false);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        pitchColumn.setComparator(Comparator.comparingDouble(wp -> wp.getLegacyWaypoint().getOrientation().getPitch()));

        yawColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        yawColumn.setCellFactory(
            callback -> {
                var cell =
                    new QuantitySpinnerTableCell<WayPoint, WayPoint, Angle>(
                        quantityStyleProvider, WayPoint::yawProperty, minYaw, maxYaw, 4, 1, FillMode.NO_FILL, 1, true);
                cell.editableProperty().bind(viewModel.editableProperty());
                return cell;
            });
        yawColumn.setComparator(Comparator.comparingDouble(wp -> wp.getLegacyWaypoint().getOrientation().getYaw()));

        triggerColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        triggerColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(WayPoint item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            var triggerSwitch = new CheckBox();
                            triggerSwitch
                                .selectedProperty()
                                .bindBidirectional(item.triggerImageHereCopterModeProperty());
                            triggerSwitch.disableProperty().bind(viewModel.editableProperty().not());
                            setGraphic(triggerSwitch);
                        }
                    }
                });
        triggerColumn.setComparator(
            (o1, o2) ->
                Boolean.compare(
                    o1.getLegacyWaypoint().isTriggerImageHereCopterMode(),
                    o2.getLegacyWaypoint().isTriggerImageHereCopterMode()));

        groundDistanceColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        // TODO, this column isnt readable anyway, so is there a quantity formatted cell without a spinner?
        groundDistanceColumn.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    WayPoint::groundDistanceProperty,
                    Quantity.of(0, Unit.METER),
                    Quantity.of(1e6, Unit.METER),
                    4,
                    2,
                    FillMode.NO_FILL,
                    1,
                    false));
        groundDistanceColumn.setComparator(
            Comparator.comparingDouble(
                wp ->
                    wp.groundDistanceProperty().get() == null
                        ? Double.POSITIVE_INFINITY
                        : wp.groundDistanceProperty().get().getValue().doubleValue()));
        groundDistanceColumn.setEditable(false);

        noteColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        noteColumn.setCellFactory(callback -> new AutoCommitTextFieldTableCell<>(WayPoint::bodyProperty));
        noteColumn.setComparator(Comparator.comparing(wp -> wp.getLegacyWaypoint().getBody()));

        speedColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        speedColumn.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    quantityStyleProvider,
                    WayPoint::speedProperty,
                    Quantity.of(0, Unit.METER_PER_SECOND),
                    viewModel.getMaxSpeedMps(),
                    4,
                    2,
                    FillMode.NO_FILL,
                    1,
                    false));
        speedColumn.setComparator(Comparator.comparingDouble(wp -> wp.getLegacyWaypoint().getSpeedMpSec()));

        stopAtWayPointColumn.setCellValueFactory(
            cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));

        stopAtWayPointColumn.setCellFactory(
            callback ->
                new QuantitySpinnerTableCell<>(
                    IQuantityStyleProvider.NEUTRAL,
                    WayPoint::stopHereTimeCopterProperty,
                    Quantity.of(0, Unit.MILLISECOND),
                    Quantity.of(Double.MAX_VALUE, Unit.MILLISECOND),
                    6,
                    6,
                    FillMode.NO_FILL,
                    1,
                    false));
        stopAtWayPointColumn.setComparator(
            Comparator.comparingDouble(wp -> wp.getLegacyWaypoint().getStopHereTimeCopter()));

        deleteColumn.setCellValueFactory(cellDataFeatures -> new ReadOnlyObjectWrapper<>(cellDataFeatures.getValue()));
        deleteColumn.setCellFactory(
            callback ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(WayPoint item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            final Button btn = new Button();
                            btn.getStyleClass().addAll("flat-icon-button", "icon-trash");
                            btn.disableProperty()
                                .bind(viewModel.getDeleteSelectedWaypointsCommand().notExecutableProperty());
                            btn.setOnAction(
                                e -> {
                                    if (!tableView.getSelectionModel().getSelectedItems().contains(item)) {
                                        int rowIndex = tableView.getItems().indexOf(item);
                                        tableView.getSelectionModel().clearAndSelect(rowIndex);
                                    }

                                    deleteAndResort();
                                });
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            setGraphic(btn);
                            setAlignment(Pos.CENTER_LEFT);
                        }
                    }
                });

        viewModel
            .waypointsProperty()
            .addListener(
                (ListChangeListener<WayPoint>)
                    change -> {
                        if (updatingSelection) {
                            return;
                        }

                        updatingSelection = true;

                        while (change.next()) {
                            if (change.wasAdded()) {
                                for (int i = change.getFrom(); i < change.getTo(); ++i) {
                                    tableView.getItems().add(i, viewModel.waypointsProperty().get(i));
                                }
                            }

                            if (change.wasRemoved()) {
                                for (WayPoint wp : change.getRemoved()) {
                                    tableView.getItems().remove(wp);
                                }
                            }
                        }

                        int selectedCount = 0;
                        tableView.getSelectionModel().clearSelection();
                        for (WayPoint wp : viewModel.waypointsProperty()) {
                            if (wp.isSelected()) {
                                ++selectedCount;
                                int rowIndex = tableView.getItems().indexOf(wp);
                                tableView.getSelectionModel().select(rowIndex);
                            }
                        }

                        updateSelectionCheckBoxState(selectedCount);
                        updatingSelection = false;
                    });

        tableView
            .getSelectionModel()
            .getSelectedItems()
            .addListener(
                (ListChangeListener<WayPoint>)
                    change -> {
                        if (updatingSelection) {
                            return;
                        }

                        updatingSelection = true;

                        for (WayPoint wp : viewModel.waypointsProperty()) {
                            wp.setSelected(false);
                        }

                        for (WayPoint wp : tableView.getSelectionModel().getSelectedItems()) {
                            wp.setSelected(true);
                        }

                        updateSelectionCheckBoxState(tableView.getSelectionModel().getSelectedItems().size());
                        updatingSelection = false;
                    });

        viewModel.selectedWayPointProperty().bind(tableView.getSelectionModel().selectedItemProperty());
        tableView.editableProperty().bind(viewModel.editableProperty());
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getItems().setAll(viewModel.waypointsProperty());

        recalculateSwitch.selectedProperty().bindBidirectional(viewModel.editableProperty());

        applyButton.disableProperty().bind(viewModel.getApplyChangesCommand().notExecutableProperty());
        applyButton.setOnAction(
            event -> {
                viewModel.getApplyChangesCommand().execute();
                tableView.sort();
            });

        deleteWaypointsButton
            .disableProperty()
            .bind(viewModel.getDeleteSelectedWaypointsCommand().notExecutableProperty());
        deleteWaypointsButton.setOnAction(
            event -> {
                deleteAndResort();
            });
    }

    private void deleteAndResort() {
        viewModel.getDeleteSelectedWaypointsCommand().execute();
        tableView.sort();
    }

    private void updateSelectionCheckBoxState(int selectedCount) {
        if (selectedCount == 0) {
            selectionCheckBox.setSelected(false);
            selectionCheckBox.setIndeterminate(false);
        } else if (selectedCount < viewModel.waypointsProperty().size()) {
            selectionCheckBox.setSelected(true);
            selectionCheckBox.setIndeterminate(true);
        } else {
            selectionCheckBox.setSelected(true);
            selectionCheckBox.setIndeterminate(false);
        }
    }

    @FXML
    private void selectAllClicked() {
        viewModel.getSelectCommand().execute(EditWaypointsViewModel.SelectionFilter.ANY);
    }

    @FXML
    private void deselectAllClicked() {
        viewModel.getDeselectCommand().execute(EditWaypointsViewModel.SelectionFilter.ANY);
    }

    @FXML
    private void invertSelectionClicked() {
        viewModel.getInvertSelectionCommand().execute();
    }

    @FXML
    private void selectNoImgClicked() {
        viewModel.getSelectCommand().execute(EditWaypointsViewModel.SelectionFilter.NO_IMAGE);
    }

    @FXML
    private void deselectNoImgClicked() {
        viewModel.getDeselectCommand().execute(EditWaypointsViewModel.SelectionFilter.NO_IMAGE);
    }

    @FXML
    private void selectWarnClicked() {
        viewModel.getSelectCommand().execute(EditWaypointsViewModel.SelectionFilter.HAS_WARNINGS);
    }

}
