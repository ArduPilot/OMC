/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.LayerGroupType;
import com.intel.missioncontrol.map.wms.IWmsManager;
import com.intel.missioncontrol.map.wms.WmsMap;
import com.intel.missioncontrol.map.wms.WmsServer;
import com.intel.missioncontrol.map.wms.WmsServerLayer;
import com.intel.missioncontrol.map.worldwind.layers.AirspaceLayer;
import com.intel.missioncontrol.map.worldwind.layers.GeneralLayerVisibility;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerVisibilitySettings;
import com.intel.missioncontrol.map.worldwind.layers.dataset.DatasetLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.dataset.DatasetLayerVisibilitySettings;
import com.intel.missioncontrol.map.worldwind.layers.flightplan.FlightplanLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.flightplan.FlightplanLayerVisibilitySettings;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.AirspacesTypeEnabledSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.GeoTiffSettings;
import com.intel.missioncontrol.settings.GeoTiffsSettings;
import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.KmlSettings;
import com.intel.missioncontrol.settings.KmlsSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.NavBarDialog;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.desktop.helper.FileFilter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapLayersViewModel extends DialogViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapLayersViewModel.class);

    private final UIAsyncListProperty<LayerViewModel> layers =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<LayerViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final INavigationService navigationService;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final Command openExternalSourcesCommand = new DelegateCommand(this::openExternalSource);
    private final Command restoreDefaultsCommand = new DelegateCommand(this::restoreDefaults);
    private final Command addWmsServerCommand = new DelegateCommand(this::addWmsServer);
    private final Command addKmlShpFileCommand = new DelegateCommand(this::addKmlShpFile);
    private final Command clearAllCommand = new DelegateCommand(this::clearAll);
    private final Command clearUavImageCacheCommand = new DelegateCommand(this::clearUavImageCache);
    private final Command clearTrackLogCommand = new DelegateCommand(this::clearTrackLog);
    private final KmlsSettings kmlsSettings;
    private final IWmsManager wmsManager;
    private final GeoTiffsSettings geoTiffsSettings;
    private final GeneralSettings generalSettings;
    private final ILicenceManager licenceManager;
    private final FlightplanLayerVisibilitySettings flightplanLayerVisibilitySettings;
    private final DatasetLayerVisibilitySettings datasetLayerVisibilitySettings;
    private final AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings;
    private final GeneralLayerVisibility generalLayerVisibility;
    private final AirspacesProvidersSettings airspacesProvidersSettings;
    private final IPathProvider pathProvider;
    private final IApplicationContext applicationContext;
    private final BooleanProperty clearMenuItemsVisible = new SimpleBooleanProperty();
    private final IMapClearingCenter mapClearingCenter;
    private final IMapModel mapModel;

    @Inject
    public MapLayersViewModel(
            INavigationService navigationService,
            IMapModel mapModel,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            GeneralSettings generalSettings,
            KmlsSettings kmlsSettings,
            IWmsManager wmsManager,
            GeoTiffsSettings geoTiffsSettings,
            FlightplanLayerVisibilitySettings flightplanLayerVisibilitySettings,
            DatasetLayerVisibilitySettings datasetLayerVisibilitySettings,
            AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings,
            GeneralLayerVisibility generalLayerVisibility,
            AirspacesProvidersSettings airspacesProvidersSettings,
            IPathProvider pathProvider,
            IApplicationContext applicationContext,
            IMapClearingCenter mapClearingCenter,
            ILicenceManager licenceManager) {
        this.navigationService = navigationService;
        this.mapModel = mapModel;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.kmlsSettings = kmlsSettings;
        this.wmsManager = wmsManager;
        this.geoTiffsSettings = geoTiffsSettings;
        this.flightplanLayerVisibilitySettings = flightplanLayerVisibilitySettings;
        this.datasetLayerVisibilitySettings = datasetLayerVisibilitySettings;
        this.aircraftLayerVisibilitySettings = aircraftLayerVisibilitySettings;
        this.generalLayerVisibility = generalLayerVisibility;
        this.airspacesProvidersSettings = airspacesProvidersSettings;
        this.pathProvider = pathProvider;
        this.applicationContext = applicationContext;
        this.mapClearingCenter = mapClearingCenter;
        this.generalSettings = generalSettings;
        this.licenceManager = licenceManager;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
        this.clearMenuItemsVisible.bind(
            generalSettings
                .operationLevelProperty()
                .isEqualTo(OperationLevel.DEBUG)
                .or(licenceManager.isGrayHawkEditionProperty()));
        this.layers.bindContent(mapModel.layersProperty(), layer -> getLayerViewModel(layer, MapLayersViewModel.this));
    }

    public ReadOnlyBooleanProperty clearMenuItemsVisibleProperty() {
        return clearMenuItemsVisible;
    }

    ReadOnlyListProperty<LayerViewModel> layerItemsProperty() {
        return layers.getReadOnlyProperty();
    }

    @Override
    protected void onClosing() {
        navigationService.navigateTo(NavBarDialog.NONE);
    }

    public Command getOpenExternalSourcesCommand() {
        return openExternalSourcesCommand;
    }

    public Command getRestoreDefaultsCommand() {
        return restoreDefaultsCommand;
    }

    private void openExternalSource() {
        dialogService.requestDialogAsync(this, GeoTiffExternalSourceViewModel.class, false);
    }

    private void restoreDefaults() {
        // KMLs off
        try (LockedList<KmlSettings> locked = kmlsSettings.kmlsProperty().lock()) {
            for (KmlSettings kml : locked) {
                PropertyHelper.setValueSafe(kml.enabledProperty(), false);
            }
        }

        // Geotiffs off
        try (LockedList<GeoTiffSettings> locked = geoTiffsSettings.geoTiffsProperty().lock()) {
            for (GeoTiffSettings geotiff : locked) {
                PropertyHelper.setValueSafe(geotiff.enabledProperty(), false);
            }
        }
        // each WMS sublayer off
        try (LockedList<WmsServer> locked = wmsManager.wmsServersProperty().lock()) {
            for (WmsServer server : locked) {
                try (LockedList<WmsMap> lockedSublayers = server.wmsMapsProperty().lock()) {
                    for (WmsMap layer : lockedSublayers) {
                        PropertyHelper.setValueSafe(layer.enabledProperty(), false);
                    }
                }
            }
        }

        try (LockedList<AirspacesTypeEnabledSettings> locked =
            airspacesProvidersSettings.airspacesEnabledProperty().lock()) {
            for (AirspacesTypeEnabledSettings airspaceType : locked) {
                airspaceType.enabledProperty().reset();
            }
        }

        ISettings[] other =
            new ISettings[] {
                flightplanLayerVisibilitySettings,
                datasetLayerVisibilitySettings,
                aircraftLayerVisibilitySettings,
                generalLayerVisibility
            };

        for (ISettings settingsPage : other) {
            for (Field field : settingsPage.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object o = field.get(settingsPage);
                    if (o instanceof AsyncBooleanProperty) {
                        PropertyHelper.resetValueSafe((AsyncBooleanProperty)o);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Command getAddWmsServerCommand() {
        return addWmsServerCommand;
    }

    public Command getAddKmlShpFileCommand() {
        return addKmlShpFileCommand;
    }

    public Command getClearAllCommand() {
        return clearAllCommand;
    }

    public Command getClearUavImageCacheCommand() {
        return clearUavImageCacheCommand;
    }

    public Command getClearTrackLogCommand() {
        return clearTrackLogCommand;
    }

    private void addWmsServer() {
        String wmsUrl =
            dialogService.requestInputDialogAndWait(
                this,
                languageHelper.getString("com.intel.missioncontrol.ui.layers.MapLayersViewModel.selectWms.title"),
                languageHelper.getString("com.intel.missioncontrol.ui.layers.MapLayersViewModel.selectWms.msg"),
                false);
        if (wmsUrl == null || wmsUrl.trim().isEmpty()) {
            return;
        }

        if (!wmsManager.containsWmsServer(wmsUrl)) {
            wmsManager.addWmsServer(wmsUrl);
            StaticInjector.getInstance(IApplicationContext.class)
                .addToast(
                    Toast.of(ToastType.INFO)
                        .setText(
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.layers.MapLayersViewModel.selectWms.done", wmsUrl))
                        .create());
        } else {
            StaticInjector.getInstance(IApplicationContext.class)
                .addToast(
                    Toast.of(ToastType.INFO)
                        .setText(
                            languageHelper.getString(
                                "com.intel.missioncontrol.ui.layers.MapLayersViewModel.selectWms.warning", wmsUrl))
                        .create());
        }
    }

    private void addKmlShpFile() {
        Path[] files =
            dialogService.requestMultiFileOpenDialog(
                this,
                languageHelper.getString("com.intel.missioncontrol.ui.layers.MapLayersViewModel.selectKmlShp"),
                pathProvider.getUserHomeDirectory(),
                FileFilter.KML_KMZ,
                FileFilter.SHP);
        if (files == null) {
            return;
        }

        for (Path file : files) {
            if (file == null) {
                continue;
            }

            String kmlShpUrl = file.toAbsolutePath().toString();
            KmlSettings kml = kmlsSettings.newSettingsInstance();
            kml.enabledProperty().set(true);
            kml.resourceProperty().set(kmlShpUrl);
            if (kmlShpUrl.toLowerCase().endsWith(".kml") || kmlShpUrl.toLowerCase().endsWith(".kmz")) {
                kml.typeProperty().set(KmlSettings.ResourceType.KML);
            } else {
                kml.typeProperty().set(KmlSettings.ResourceType.SHP);
            }

            if (!kmlsSettings.kmlsProperty().get().contains(kml)) {
                kmlsSettings.kmlsProperty().add(kml);
                StaticInjector.getInstance(IApplicationContext.class)
                    .addToast(
                        Toast.of(ToastType.INFO)
                            .setText(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.layers.MapLayersViewModel.selectKmlShp.done",
                                    file.getFileName()))
                            .create());
            } else {
                StaticInjector.getInstance(IApplicationContext.class)
                    .addToast(
                        Toast.of(ToastType.INFO)
                            .setText(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.layers.MapLayersViewModel.selectKmlShp.warning",
                                    file.getFileName()))
                            .create());
            }
        }
    }

    private void clearAll() {
        mapClearingCenter.clearAllCaches();
    }

    private void clearUavImageCache() {
        mapClearingCenter.clearUavImageCache();
    }

    private void clearTrackLog() {
        mapClearingCenter.clearTrackLog();
    }

    private LayerViewModel getLayerViewModel(ILayer layer, ViewModel parent) {
        if (layer instanceof AirspaceLayer) {
            return new AirspaceLayerGroupViewModel((AirspaceLayer)layer, languageHelper);
        } else if (layer instanceof WmsServerLayer) {
            return new WmsServerLayerViewModel((WmsServerLayer)layer, languageHelper, wmsManager);
        } else if (layer instanceof FlightplanLayerGroup) {
            return new FlightPlanLayerGroupViewModel(((FlightplanLayerGroup)layer), languageHelper, generalSettings);
        } else if (layer instanceof DatasetLayerGroup) {
            return new DatasetLayerGroupViewModel(((DatasetLayerGroup)layer), languageHelper, generalSettings);
        } else if (layer instanceof AircraftLayerGroup) {
            return new AircraftLayerGroupViewModel(((AircraftLayerGroup)layer), languageHelper);
        } else if (layer instanceof LayerGroup) {
            LayerGroup layerGroup = (LayerGroup)layer;
            if (layerGroup.getType().equals(LayerGroupType.KML_SHP_GROUP)) {
                return new KmlLayerGroupViewModel(layerGroup, languageHelper, kmlsSettings);
            } else if (layerGroup.getType().equals(LayerGroupType.GEOTIFF_GROUP)) {
                return new GeoTiffLayerGroupViewModel(
                    layerGroup, languageHelper, dialogService, parent, geoTiffsSettings);
            }

            return new LayerGroupViewModel(layerGroup, languageHelper, true, false);
        } else {
            return new SimpleLayerViewModel(layer, languageHelper);
        }
    }

}
