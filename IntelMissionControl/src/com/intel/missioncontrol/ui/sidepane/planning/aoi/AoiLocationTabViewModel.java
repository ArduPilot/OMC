/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.aoi;

import com.google.inject.Inject;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.geometry.AreaOfInterestCorner;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.commands.IParameterizedCommand;
import com.intel.missioncontrol.ui.commands.ParameterizedDelegateCommand;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.Point;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.util.converter.DoubleStringConverter;

public class AoiLocationTabViewModel extends ViewModelBase<AreaOfInterest> {

    private static final double ASPECT_MIN = 1.0;
    private static final double ASPECT_MAX = 5.0;
    private static final double ASPECT_STEP = 0.1;

    private static final String KEY_DIALOG_CONTENT = PicArea.KEY + ".aspectRatio.prompt";
    private static final String KEY_DIALOG_HEADER = PicArea.KEY + ".aspectRatio.msg";

    private final ISettingsManager settingsManager;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final BooleanProperty maximizeAoiVisible = new SimpleBooleanProperty();
    private final IMapView mapView;
    private final ISelectionManager selectionManager;
    private AreaOfInterest areaOfInterest;

    private IParameterizedCommand<AreaOfInterestCorner> deleteCommand;
    private ICommand addVertexCommand;
    private ICommand maximizeAoiCommand;

    @Inject
    public AoiLocationTabViewModel(
            ISettingsManager settingsManager,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IMapView mapView,
            ISelectionManager selectionManager) {
        this.mapView = mapView;
        this.settingsManager = settingsManager;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.selectionManager = selectionManager;
        maximizeAoiVisible.bind(
            settingsManager.getSection(GeneralSettings.class).operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
        selectionManager
            .currentSelectionProperty()
            .addListener((observable, oldValue, newValue) -> selectionChange(newValue), Dispatcher::dispatchToUI);
    }

    public ObservableValue<? extends ObservableList<AreaOfInterestCorner>> cornersProperty() {
        return areaOfInterest.cornerListProperty();
    }

    private final ObjectProperty<AreaOfInterestCorner> selection = new SimpleObjectProperty<>();

    public ObjectProperty<AreaOfInterestCorner> selectionProperty() {
        return selection;
    }

    @Override
    protected void initializeViewModel(AreaOfInterest aoi) {
        super.initializeViewModel(aoi);
        this.areaOfInterest = aoi;

        aoi.cornerListProperty()
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        selectionChange(selectionManager.getSelection());
                    }
                });

        deleteCommand =
            new ParameterizedDelegateCommand<>(
                this::deleteCorner, aoi.minCornerCountProperty().lessThan(aoi.cornerListProperty().sizeProperty()));
        maximizeAoiCommand =
            new DelegateCommand(
                this::maximieAoi,
                Bindings.createBooleanBinding(
                    () -> areaOfInterest.getType().canOptimizeCorners(), areaOfInterest.typeProperty()));

        addVertexCommand =
            new DelegateCommand(
                this::addVertex,
                Bindings.createBooleanBinding(
                    () -> {
                        return !aoi.getType().isCircular() || aoi.cornerListProperty().size() == 0;
                    },
                    aoi.cornerListProperty(),
                    aoi.typeProperty()));

        selection.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectionManager.setSelection(newValue.getLegacyPoint().getPoint());
                }
            });
    }

    private void addVertex() {
        final Position center = mapView.getCenterPosition();
        Point pointNew = areaOfInterest.addVertex(center, selectionProperty().get());
        selectionManager.setSelection(pointNew);
    }

    private void maximieAoi() {
        GeneralSettings settings = settingsManager.getSection(GeneralSettings.class);
        double aoiMaxAspectRatio = settings.aoiMaximizationAspectRatioProperty().get();
        Double aspectMax =
            dialogService.requestInputDialogAndWait(
                this,
                languageHelper.getString(KEY_DIALOG_HEADER),
                languageHelper.getString(KEY_DIALOG_CONTENT),
                aoiMaxAspectRatio + "",
                new DoubleStringConverter(),
                true);
        System.out.println("aspectMax:" + aspectMax);
        if (aspectMax == null) {
            return;
        }

        aspectMax = MathHelper.intoRange(aspectMax, ASPECT_MIN, ASPECT_MAX);
        settings.aoiMaximizationAspectRatioProperty().setValue(aspectMax);

        areaOfInterest.optimizeArea(aspectMax);
    }

    private void deleteCorner(AreaOfInterestCorner corner) {
        corner.deleteMe();
    }

    public IParameterizedCommand<AreaOfInterestCorner> getDeleteCommand() {
        return deleteCommand;
    }

    public ICommand getAddVertexCommand() {
        return addVertexCommand;
    }

    public ICommand getMaximizeAoiCommand() {
        return maximizeAoiCommand;
    }

    public AreaOfInterest getAreaOfInterest() {
        return areaOfInterest;
    }

    public BooleanProperty maximizeAoiVisibleProperty() {
        return maximizeAoiVisible;
    }

    public void dragVertex(int fromIdx, int toIdx) {
        areaOfInterest.dragVertex(fromIdx, toIdx);
    }

    public void selectionChange(Object userData) {
        if (userData instanceof Point) {
            Point point = (eu.mavinci.flightplan.Point)userData;
            if (point.getParent() == areaOfInterest.getPicArea().getCorners()) {
                selection.set(areaOfInterest.getWrapper(point));
                return;
            }
        }

        selection.set(null);
    }

}
