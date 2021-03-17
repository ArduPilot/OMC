/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.beans.property.VariantQuantityProperty;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.mission.SrsPosition;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.SrsSettings;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.IParameterizedCommand;
import com.intel.missioncontrol.ui.commands.ParameterizedDelegateCommand;
import com.intel.missioncontrol.ui.dialogs.DialogService;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.flight.WindDirection;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.helper.Pair;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;

public class AoiTransformationTabViewModel extends ViewModelBase<AreaOfInterest> {

    private final IApplicationContext applicationContext;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final BooleanProperty chooseRefPointButtonPressed = new SimpleBooleanProperty(false);
    private final Command toggleChooseRefPointCommand;
    private final IMapView mapView;
    private final IMapController mapController;
    private final ISelectionManager selectionManager;
    private SrsPosition originPositionWrapper;
    private SrsPosition originPosition2nd;

    private final QuantityProperty<Dimension.Angle> originYawQuantity;
    private final SimpleDoubleProperty oridingYaw = new SimpleDoubleProperty(0.);

    private final QuantityProperty<Dimension.Percentage> scaleQuantity;
    private final SimpleDoubleProperty scale = new SimpleDoubleProperty(0.);

    private final ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentX = new SimpleObjectProperty<>();
    private final ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentY = new SimpleObjectProperty<>();
    private final ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentZ = new SimpleObjectProperty<>();
    private final QuantityProperty<Dimension.Length> modelAxisOffsetXQuantitiy;
    private final QuantityProperty<Dimension.Length> modelAxisOffsetYQuantitiy;
    private final QuantityProperty<Dimension.Length> modelAxisOffsetZQuantitiy;
    private final DoubleProperty modelAxisOffsetX = new SimpleDoubleProperty();
    private final DoubleProperty modelAxisOffsetY = new SimpleDoubleProperty();
    private final DoubleProperty modelAxisOffsetZ = new SimpleDoubleProperty();

    private final DoubleProperty originElevation = new SimpleDoubleProperty();
    private final QuantityProperty<Dimension.Length> originElevationQuantity;

    private final ObjectProperty<CPicArea.ModelAxis> swapSource = new SimpleObjectProperty<>(CPicArea.ModelAxis.Xplus);
    private final ObjectProperty<CPicArea.ModelAxis> swapTarget = new SimpleObjectProperty<>(CPicArea.ModelAxis.Yplus);

    private final ListProperty<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>> modelAxisTransformations =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private IParameterizedCommand<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>> deleteCommand;

    private ChangeListener<InputMode> mouseModesChangeListener =
        ((observable, oldValue, newValue) -> {
            if (newValue.equals(InputMode.SET_MODEL_ORIGIN)) {
                chooseRefPointButtonPressed.setValue(true);
            } else {
                chooseRefPointButtonPressed.setValue(false);
            }
        });

    @Inject
    public AoiTransformationTabViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            ISelectionManager selectionManager,
            IMapView mapView,
            IMapController mapController) {
        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.mapView = mapView;
        this.mapController = mapController;
        this.selectionManager = selectionManager;
        GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        toggleChooseRefPointCommand =
            new de.saxsys.mvvmfx.utils.commands.DelegateCommand(
                () ->
                    new Action() {
                        @Override
                        protected void action() {
                            toggleChooseRefPosition();
                        }
                    });

        originPositionWrapper = new SrsPosition(generalSettings);
        originPosition2nd = new SrsPosition(generalSettings);

        SrsSettings srsSettings = settingsManager.getSection(SrsSettings.class);
        originPositionWrapper.setSrs(srsSettings.getApplicationSrs());
        originPosition2nd.setSrs(srsSettings.getApplicationSrs());

        srsSettings
            .applicationSrsProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    originPositionWrapper.setSrs(newValue);
                    originPosition2nd.setSrs(newValue);
                });

        originYawQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.ANGLE_DEGREES,
                Quantity.of(0.0, Unit.DEGREE));
        QuantityBindings.bindBidirectional(originYawQuantity, oridingYaw, Unit.DEGREE);

        scaleQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.PERCENTAGE,
                Quantity.of(0.0, Unit.PERCENTAGE));
        QuantityBindings.bindBidirectional(scaleQuantity, scale, Unit.FACTOR);

        modelAxisOffsetXQuantitiy =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(modelAxisOffsetXQuantitiy, modelAxisOffsetX, Unit.METER);

        modelAxisOffsetYQuantitiy =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(modelAxisOffsetYQuantitiy, modelAxisOffsetY, Unit.METER);

        modelAxisOffsetZQuantitiy =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(modelAxisOffsetZQuantitiy, modelAxisOffsetZ, Unit.METER);

        originElevationQuantity =
            new SimpleQuantityProperty<>(
                settingsManager.getSection(GeneralSettings.class),
                UnitInfo.LOCALIZED_LENGTH,
                Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(originElevationQuantity, originElevation, Unit.METER);
    }

    @Override
    protected void initializeViewModel(AreaOfInterest aoi) {
        super.initializeViewModel(aoi);
        mapController
            .mouseModeProperty()
            .addListener(new WeakChangeListener<>(mouseModesChangeListener), Dispatcher::dispatchToUI);

        originPositionWrapper.positionProperty().bindBidirectional(aoi.originPositionProperty());
        originElevation.bindBidirectional(aoi.originElevationProperty());
        oridingYaw.bindBidirectional(aoi.originYawProperty());
        scale.bindBidirectional(aoi.modelScaleProperty());
        modelAxisAlignmentX.bindBidirectional(aoi.modelAxisAlignmentXProperty());
        modelAxisAlignmentY.bindBidirectional(aoi.modelAxisAlignmentYProperty());
        modelAxisAlignmentZ.bindBidirectional(aoi.modelAxisAlignmentZProperty());

        modelAxisOffsetX.bindBidirectional(aoi.modelAxisOffsetXProperty());
        modelAxisOffsetY.bindBidirectional(aoi.modelAxisOffsetYProperty());
        modelAxisOffsetZ.bindBidirectional(aoi.modelAxisOffsetZProperty());

        modelAxisTransformations.bindBidirectional(aoi.modelAxisTransformationsProperty());

        deleteCommand = new ParameterizedDelegateCommand<>(this::deleteCorner);
    }

    private void deleteCorner(Pair<CPicArea.ModelAxis, CPicArea.ModelAxis> swap) {
        modelAxisTransformations.remove(swap);
    }

    public IParameterizedCommand<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>> getDeleteCommand() {
        return deleteCommand;
    }

    private void toggleChooseRefPosition() {
        if (mapController.getMouseMode() != InputMode.SET_MODEL_ORIGIN) {
            selectionManager.setSelection(
                applicationContext.getCurrentMission().getCurrentFlightPlan().getLegacyFlightplan().getRefPoint());
            mapController.setMouseMode(InputMode.SET_MODEL_ORIGIN);
        } else {
            mapController.tryCancelMouseModes(InputMode.SET_MODEL_ORIGIN);
        }
    }

    public BooleanProperty chooseRefPointButtonPressedProperty() {
        return chooseRefPointButtonPressed;
    }

    public Command getToggleChooseRefPointCommand() {
        return toggleChooseRefPointCommand;
    }

    public VariantQuantityProperty refPointLatitudeProperty() {
        return originPositionWrapper.latitudeQuantity();
    }

    public VariantQuantity getTakeOffLatitude() {
        return refPointLatitudeProperty().get();
    }

    public void setTakeOffLatitude(VariantQuantity value) {
        refPointLatitudeProperty().set(value);
    }

    public VariantQuantityProperty refPointLongitudeProperty() {
        return originPositionWrapper.longitudeQuantity();
    }

    public VariantQuantity getTakeOffLongitude() {
        return refPointLongitudeProperty().get();
    }

    public void setTakeOffLongitude(VariantQuantity value) {
        refPointLongitudeProperty().set(value);
    }

    public QuantityProperty<Dimension.Length> refPointElevationProperty() {
        return originElevationQuantity;
    }

    public SimpleDoubleProperty oridingYawProperty() {
        return oridingYaw;
    }

    public QuantityProperty<Dimension.Angle> originYawQuantityProperty() {
        return originYawQuantity;
    }

    public QuantityProperty<Dimension.Percentage> scaleQuantityProperty() {
        return scaleQuantity;
    }

    public void setFlightDirectionFromView() {
        double heading = mapView.getHeading().getDegrees();
        heading = WindDirection.normalizeAngle(heading);
        oridingYaw.setValue(heading);
    }

    public void setDirectionFromSecondPoint() {
        originPosition2nd.setPosition(originPositionWrapper.getPosition());
        String lat =
            dialogService.requestInputDialogAndWait(
                this,
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.planning.aoi.AoiTransformationTabView.LatTitle"),
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.planning.aoi.AoiTransformationTabView.LatMsg"),
                originPosition2nd.latitudeQuantity().get().getValue() + "",
                new DialogService.StringStringConverter(),
                true);
        if (lat == null) {
            return;
        }

        String lon =
            dialogService.requestInputDialogAndWait(
                this,
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.planning.aoi.AoiTransformationTabView.LonTitle"),
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.planning.aoi.AoiTransformationTabView.LonMsg"),
                originPosition2nd.longitudeQuantity().get().getValue() + "",
                new DialogService.StringStringConverter(),
                true);
        if (lon == null) {
            return;
        }

        originPosition2nd
            .latitudeQuantity()
            .set(VariantQuantity.of(Double.parseDouble(lat), originPosition2nd.latitudeQuantity().get().getUnit()));
        originPosition2nd
            .longitudeQuantity()
            .set(VariantQuantity.of(Double.parseDouble(lon), originPosition2nd.longitudeQuantity().get().getUnit()));
        Position p1 = originPositionWrapper.getPosition();
        Position p2 = originPosition2nd.getPosition();
        Angle a = LatLon.greatCircleAzimuth(p1, p2);
        oridingYaw.set(a.getDegrees());
    }

    public void plus90() {
        oridingYaw.setValue(WindDirection.normalizeAngle(oridingYaw.get() + 90));
    }

    public void minus90() {
        oridingYaw.setValue(WindDirection.normalizeAngle(oridingYaw.get() - 90));
    }

    public ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentZProperty() {
        return modelAxisAlignmentZ;
    }

    public ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentYProperty() {
        return modelAxisAlignmentY;
    }

    public ObjectProperty<CPicArea.ModelAxisAlignment> modelAxisAlignmentXProperty() {
        return modelAxisAlignmentX;
    }

    public ListProperty<Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>> modelAxisTransformationsProperty() {
        return modelAxisTransformations;
    }

    public QuantityProperty<Dimension.Length> modelAxisOffsetXQuantitiyProperty() {
        return modelAxisOffsetXQuantitiy;
    }

    public QuantityProperty<Dimension.Length> modelAxisOffsetYQuantitiyProperty() {
        return modelAxisOffsetYQuantitiy;
    }

    public QuantityProperty<Dimension.Length> modelAxisOffsetZQuantitiyProperty() {
        return modelAxisOffsetZQuantitiy;
    }

    public ObjectProperty<CPicArea.ModelAxis> swapSourceProperty() {
        return swapSource;
    }

    public ObjectProperty<CPicArea.ModelAxis> swapTargetProperty() {
        return swapTarget;
    }

    public void addSwap() {
        modelAxisTransformations.add(
            new Pair<CPicArea.ModelAxis, CPicArea.ModelAxis>(swapSource.get(), swapTarget.get()));
    }
}
