/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.map;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.livevideo.ILiveVideoService;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.ViewMode;
import com.intel.missioncontrol.map.api.LocateMeApi;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import gov.nasa.worldwind.geom.Sector;
import java.util.OptionalDouble;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.concurrent.Dispatcher;

public class ToolPaletteViewModel extends ViewModelBase {

    private static final double LOG_ZOOM_STEP = 0.04;
    private final UIAsyncBooleanProperty rulerModeEnabled = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty flatMapEnabled = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty viewModesVisible = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty liveVideoVisible = new UIAsyncBooleanProperty(this);
    private final BooleanProperty liveVideoEnabled = new SimpleBooleanProperty();
    private final UIAsyncObjectProperty<ViewMode> viewMode =
        new UIAsyncObjectProperty<>(
            this, new UIPropertyMetadata.Builder<ViewMode>().initialValue(ViewMode.DEFAULT).create());
    private final Command locateMeCommand;
    private final Command zoomInCommand;
    private final Command zoomOutCommand;
    private final IMapView mapView;

    @Inject
    public ToolPaletteViewModel(
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            INavigationService navigationService,
            LocateMeApi locateMeApi,
            IMapModel mapModel,
            IMapController mapController,
            IMapView mapView,
            IWWGlobes globes,
            ILinkBoxConnectionService linkBoxConnectionService,
            ILiveVideoService liveVideoService) {
        this.mapView = mapView;

        locateMeCommand =
            new DelegateCommand(
                () -> {
                    if (linkBoxConnectionService.getRTKStation().get().getRTKStationPosition().get() == null) {
                        LocateMeApi.SectorCeiling pos = locateMeApi.tryLocateCurrentPosition();
                        if (pos != null && pos.sector != null) {
                            mapView.goToSectorAsync(pos.sector, pos.maxHeightWgs84);
                        } else {
                            applicationContext.addToast(
                                Toast.of(ToastType.INFO)
                                    .setText(
                                        languageHelper.getString(
                                            "com.intel.missioncontrol.map.api.LocateMeApi.moveToCurrentPositionNotFound"))
                                    .create());
                        }
                    } else {
                        Sector sector =
                            Sector.boundingSector(
                                globes.getDefaultGlobe(),
                                linkBoxConnectionService.getRTKStation().get().getRTKStationPosition().get(),
                                100);
                        mapView.goToSectorAsync(sector, OptionalDouble.empty() /*pos.maxHeightWgs84*/);
                    }
                });

        zoomInCommand = new DelegateCommand(() -> doZoom(-1));
        zoomOutCommand = new DelegateCommand(() -> doZoom(+1));

        flatMapEnabled.bindBidirectional(mapView.flatEarthProperty());
        viewModesVisible.bind(navigationService.workflowStepProperty().isEqualTo(WorkflowStep.FLIGHT));
        viewMode.bindBidirectional(mapView.viewModeProperty());

        liveVideoVisible.addListener(
            (obs, oldValue, newValue) -> {
                if (oldValue) liveVideoEnabled.set(false);
            });
        liveVideoVisible.bind(viewModesVisible.and(liveVideoService.streamListProperty().emptyProperty().not()));

        mapController
            .mouseModeProperty()
            .addListener(
                (observable, oldValue, newValue) -> rulerModeEnabled.set(newValue == InputMode.ADD_MEASURMENT_POINTS),
                Dispatcher.platform()::run);

        rulerModeEnabled.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue) {
                    mapController.setMouseMode(InputMode.ADD_MEASURMENT_POINTS);
                } else {
                    mapController.tryCancelMouseModes(InputMode.ADD_MEASURMENT_POINTS);
                }
            });

        liveVideoEnabled.bindBidirectional(applicationContext.liveVideoActive);
    }

    private void doZoom(int sign) {
        double zoom = mapView.getZoom();
        double logZoom = zoom != 0 ? Math.log(zoom) : 0;
        mapView.setZoom(Math.exp(logZoom + sign * LOG_ZOOM_STEP));
    }

    public Property<Boolean> rulerModeEnabledProperty() {
        return rulerModeEnabled;
    }

    public Property<Boolean> flatMapEnabledProperty() {
        return flatMapEnabled;
    }

    public Property<Boolean> viewModesVisibleProperty() {
        return viewModesVisible;
    }

    public ReadOnlyProperty<Boolean> liveVideoVisibleProperty() {
        return liveVideoVisible;
    }

    public Property<Boolean> liveVideoEnabledProperty() {
        return liveVideoEnabled;
    }

    public Property<ViewMode> viewModeProperty() {
        return viewMode;
    }

    public Command getLocateMeCommand() {
        return locateMeCommand;
    }

    public Command getZoomInCommand() {
        return zoomInCommand;
    }

    public Command getZoomOutCommand() {
        return zoomOutCommand;
    }

}
