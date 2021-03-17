/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.airspaces.services.Airmap2AirspaceService;
import com.intel.missioncontrol.common.PostConstruct;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.ILayerFactory;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapDragManager;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.LayerGroup.ToggleHint;
import com.intel.missioncontrol.map.LayerGroupType;
import com.intel.missioncontrol.map.credits.IMapCreditsManager;
import com.intel.missioncontrol.map.credits.IMapCreditsSource;
import com.intel.missioncontrol.map.credits.MapCreditViewModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.geotiff.IGeoTiffManager;
import com.intel.missioncontrol.map.kml.IKmlManager;
import com.intel.missioncontrol.map.wms.IWmsManager;
import com.intel.missioncontrol.map.wms.WmsServerLayer;
import com.intel.missioncontrol.map.worldwind.impl.CursorManager;
import com.intel.missioncontrol.map.worldwind.impl.MapDragManager;
import com.intel.missioncontrol.map.worldwind.layers.AirspaceLayer;
import com.intel.missioncontrol.map.worldwind.layers.CompassLayer;
import com.intel.missioncontrol.map.worldwind.layers.GeneralLayerVisibility;
import com.intel.missioncontrol.map.worldwind.layers.MapboxLayer;
import com.intel.missioncontrol.map.worldwind.layers.MissionOverviewLayer;
import com.intel.missioncontrol.map.worldwind.layers.OneImageBaseLayer;
import com.intel.missioncontrol.map.worldwind.layers.RulerLayer;
import com.intel.missioncontrol.map.worldwind.layers.ScalebarLayer;
import com.intel.missioncontrol.map.worldwind.layers.SearchResultsLayer;
import com.intel.missioncontrol.map.worldwind.layers.SkyGradientLayer;
import com.intel.missioncontrol.map.worldwind.layers.TooltipLayer;
import com.intel.missioncontrol.map.worldwind.layers.airTraffic.AirTrafficLayer;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.dataset.DatasetLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.flightplan.FlightplanLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.grids.ContourLinesLayer;
import com.intel.missioncontrol.map.worldwind.layers.grids.LatLonGraticuleLayer;
import com.intel.missioncontrol.map.worldwind.layers.grids.MGRSGraticuleLayer;
import com.intel.missioncontrol.map.worldwind.layers.grids.TerrainProfileLayer;
import com.intel.missioncontrol.map.worldwind.layers.grids.UTMGraticuleLayer;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.networking.DelegatingNetworkStatus;
import com.intel.missioncontrol.networking.INetworkInformation;
import com.intel.missioncontrol.settings.ElevationModelSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.MainViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CPhotoSettings;
import eu.mavinci.core.flightplan.CPicAreaCorners;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.FlightplanFactory;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.core.flightplan.visitors.FirstWaypointVisitor;
import eu.mavinci.core.flightplan.visitors.NextWaypointVisitor;
import eu.mavinci.core.flightplan.visitors.PreviousWaypointVisitor;
import eu.mavinci.desktop.gui.doublepanel.planemain.ActionManager;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerPicArea;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AltitudeModes;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.PhotoSettings;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.PicAreaCorners;
import eu.mavinci.flightplan.Point;
import eu.mavinci.geo.ICountryDetector;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.image.Image;
import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WWMapModel implements IWWMapModel, IMapCreditsSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(WWMapModel.class);

    private final LayerGroup groupGeoTiff = new LayerGroup(LayerGroupType.GEOTIFF_GROUP);

    private final AsyncListProperty<MapCreditViewModel> mapCredits =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<MapCreditViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncBooleanProperty mapBoxVisible = new SimpleAsyncBooleanProperty(this);

    private final AsyncListProperty<ILayer> layers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<ILayer>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        layer -> {
                            if (layer instanceof LayerGroup) {
                                return new AsyncObservable[] {((LayerGroup)layer).subLayersProperty()};
                            }

                            return new AsyncObservable[0];
                        }))
                .create());

    private final Dispatcher dispatcher;
    private final IMapController mapController;
    private final ILayerFactory layerFactory;
    private final IGeoTiffManager geoTiffManager;
    private final IKmlManager kmlManager;
    private final IWmsManager wmsManager;
    private final ICountryDetector countryDetector;
    private final MapDragManager dragger;
    private final ISelectionManager selectionManager;
    private final Model model;
    private final CursorManager cursorManager;
    private final NotificationCenter notificationCenter;
    private final INavigationService navigationService;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final ISettingsManager settingsManager;
    private final ElevationModelSettings elevationModelSettings;

    private Object nodeToInsertBehind = null;
    private double newAltInM = CWaypoint.DEFAULT_ALT_WITHIN_M;
    private AltitudeModes newAltMode = AltitudeModes.clampToGround;

    @SuppressWarnings("FieldCanBeLocal")
    private final ChangeListener<Boolean> offlineModeListener =
        new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean enabled) {
                WorldWind.getNetworkStatus().setOfflineMode(enabled);
                DelegatingNetworkStatus networkStatus = (DelegatingNetworkStatus)WorldWind.getNetworkStatus();
                networkStatus.setOfflineMode(enabled);
                Airmap2AirspaceService.setOfflineMode(enabled);
            }
        };

    @Inject
    public WWMapModel(
            @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
            IWWGlobes globes,
            IWWMapView mapView,
            IMapController mapController,
            WorldWindowProvider worldWindowProvider,
            ILayerFactory layerFactory,
            ISettingsManager settingsManager,
            IGeoTiffManager geoTiffManager,
            IKmlManager kmlManager,
            IWmsManager wmsManager,
            IElevationModel elevationModel,
            ICountryDetector countryDetector,
            INetworkInformation networkInformation,
            ISelectionManager selectionManager,
            NotificationCenter notificationCenter,
            INavigationService navigationService,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IMapCreditsManager mapCreditsManager) {
        this.dispatcher = dispatcher;
        this.mapController = mapController;
        this.layerFactory = layerFactory;
        this.geoTiffManager = geoTiffManager;
        this.kmlManager = kmlManager;
        this.wmsManager = wmsManager;
        this.countryDetector = countryDetector;
        this.selectionManager = selectionManager;
        this.model = new BasicModel(globes.getActiveGlobe(), null);
        this.dragger = new MapDragManager(globes, mapView, elevationModel, selectionManager, worldWindowProvider);
        this.cursorManager = new CursorManager(this, mapController, worldWindowProvider);
        this.notificationCenter = notificationCenter;
        this.navigationService = navigationService;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
        elevationModelSettings = settingsManager.getSection(ElevationModelSettings.class);
        mapCreditsManager.register(this);

        DelegatingNetworkStatus networkStatus = (DelegatingNetworkStatus)WorldWind.getNetworkStatus();
        networkStatus.setNetworkInformation(networkInformation);

        settingsManager
            .getSection(GeneralSettings.class)
            .offlineModeProperty()
            .addListener(new WeakChangeListener<>(offlineModeListener), dispatcher::run);

        offlineModeListener.changed(
            null, false, settingsManager.getSection(GeneralSettings.class).offlineModeProperty().get());

        selectionManager
            .currentSelectionProperty()
            .addListener((observable, oldValue, newValue) -> setNodeToInsertBehind(newValue));

        mapBoxVisible.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue) {
                    Image image =
                        new Image(
                            MainViewModel.class
                                .getResource("/com/intel/missioncontrol/gfx/Mapbox_Logo.png")
                                .toExternalForm());
                    mapCredits.setAll(
                        new MapCreditViewModel(
                            languageHelper.getString(
                                "com.intel.missioncontrol.map.layers.copyrights.imagery.provider.mapbox.name"),
                            image,
                            languageHelper.getString(
                                "com.intel.missioncontrol.map.layers.copyrights.imagery.provider.mapbox.url")),
                        new MapCreditViewModel(
                            languageHelper.getString(
                                "com.intel.missioncontrol.map.layers.copyrights.imagery.provider.mapbox.osm.name"),
                            languageHelper.getString(
                                "com.intel.missioncontrol.map.layers.copyrights.imagery.provider.mapbox.osm.url")));
                } else {
                    mapCredits.clear();
                }
            });

        layers.addListener((observable, oldValue, newValue) -> refreshLayers(newValue), dispatcher::run);
    }

    @PostConstruct
    @SuppressWarnings("unused")
    private void initialize() {
        dispatcher.runLater(
            () -> {
                createBaseLayers();
                refreshLayers(layers.get());
            });
    }

    @Override
    public Model getWWModel() {
        return model;
    }

    private void createBaseLayers() {
        dispatcher.verifyAccess();
        GeneralLayerVisibility generalLayerVisibility = settingsManager.getSection(GeneralLayerVisibility.class);

        LayerGroup groupFlightplan = layerFactory.newLayer(FlightplanLayerGroup.class);
        layers.add(groupFlightplan);

        LayerGroup groupMatching = layerFactory.newLayer(DatasetLayerGroup.class);
        layers.add(groupMatching);

        LayerGroup groupAirplane = layerFactory.newLayer(AircraftLayerGroup.class);
        layers.add(groupAirplane);

        LayerGroup groupKml = new LayerGroup(LayerGroupType.KML_SHP_GROUP);
        groupKml.subLayersProperty().bindContent(kmlManager.imageryLayersProperty());
        layers.add(groupKml);

        groupGeoTiff.subLayersProperty().bindContent(geoTiffManager.imageryLayersProperty());
        layers.add(groupGeoTiff);

        wmsManager
            .wmsServerLayersProperty()
            .addListener(
                (ListChangeListener<? super WmsServerLayer>)
                    c -> {
                        while (c.next()) {
                            if (c.wasAdded()) {
                                // get(0) for emitting only one element per time otherwise list bindings go crazy
                                layers.add(layers.indexOf(groupGeoTiff) + 1, c.getAddedSubList().get(0));
                                refreshLayers(layers.get());
                            }

                            if (c.wasRemoved()) {
                                // get(0) for emitting only one element per time otherwise list bindings go crazy
                                layers.remove(c.getRemoved().get(0));
                                refreshLayers(layers.get());
                            }
                        }
                    },
                dispatcher::run);

        layers.addAll(wmsManager.wmsServerLayersProperty());

        layers.add(layerFactory.newLayer(AirspaceLayer.class));

        LayerGroup groupBase = new LayerGroup(LayerGroupType.BASE_MAPS_GROUP, ToggleHint.ONE);
        ILayer mapBoxHybrid =
            createLayerAndBindEnabled(
                MapboxLayer.Hybrid.class, generalLayerVisibility.mapBoxHybridLayerVisibleProperty());
        ILayer mapBoxSat =
            createLayerAndBindEnabled(
                MapboxLayer.Satellite.class, generalLayerVisibility.mapBoxSatLayerVisibleProperty());
        ILayer mapBoxStreets =
            createLayerAndBindEnabled(
                MapboxLayer.Streets.class, generalLayerVisibility.mapBoxStreetsLayerVisibleProperty());

        try (LockedList<ILayer> lockedList = groupBase.subLayersProperty().get().lock()) {
            lockedList.add(mapBoxHybrid);
            lockedList.add(mapBoxSat);
            lockedList.add(mapBoxStreets);
        }

        layers.add(groupBase);

        LayerGroup linesAndGrids = new LayerGroup(LayerGroupType.LINES_AND_GRIDS_GROUP);
        linesAndGrids
            .subLayersProperty()
            .add(
                createLayerAndBindEnabled(
                    ContourLinesLayer.class, generalLayerVisibility.contourLinesLayerVisibleProperty()));
        linesAndGrids
            .subLayersProperty()
            .add(
                createLayerAndBindEnabled(
                    LatLonGraticuleLayer.class, generalLayerVisibility.latLonGraticuleLayerVisibleProperty()));
        linesAndGrids
            .subLayersProperty()
            .add(
                createLayerAndBindEnabled(
                    MGRSGraticuleLayer.class, generalLayerVisibility.mgrsGraticuleLayerVisibleProperty()));
        linesAndGrids
            .subLayersProperty()
            .add(
                createLayerAndBindEnabled(
                    UTMGraticuleLayer.class, generalLayerVisibility.utmGraticuleLayerVisibleProperty()));
        linesAndGrids
            .subLayersProperty()
            .add(
                createLayerAndBindEnabled(
                    TerrainProfileLayer.class, generalLayerVisibility.terrainProfileLayerVisibleProperty()));
        linesAndGrids
            .subLayersProperty()
            .add(createLayerAndBindEnabled(CompassLayer.class, generalLayerVisibility.compassLayerVisibleProperty()));
        linesAndGrids
            .subLayersProperty()
            .add(createLayerAndBindEnabled(ScalebarLayer.class, generalLayerVisibility.scalebarLayerVisibleProperty()));
        linesAndGrids
            .subLayersProperty()
            .add(createLayerAndBindEnabled(TooltipLayer.class, generalLayerVisibility.tooltipLayerVisibleProperty()));
        linesAndGrids
            .subLayersProperty()
            .add(createLayerAndBindEnabled(AirTrafficLayer.class, generalLayerVisibility.airtrafficVisibleProperty()));
        layers.add(linesAndGrids);

        ILayer missionOverviewLayer = layerFactory.newLayer(MissionOverviewLayer.class);
        navigationService
            .workflowStepProperty()
            .addListener(
                ((observable, oldValue, newValue) ->
                    PropertyHelper.setValueSafe(
                        missionOverviewLayer.enabledProperty(), newValue == WorkflowStep.NONE)));
        layers.add(missionOverviewLayer);

        SearchResultsLayer searchResultsLayer = layerFactory.newLayer(SearchResultsLayer.class);
        layers.add(searchResultsLayer);
        layers.add(layerFactory.newLayer(RulerLayer.class));

        layers.add(layerFactory.newLayer(OneImageBaseLayer.class));
        layers.add(layerFactory.newLayer(SkyGradientLayer.class));

        mapBoxVisible.bind(
            mapBoxHybrid
                .enabledProperty()
                .or(mapBoxSat.enabledProperty())
                .or(mapBoxStreets.enabledProperty())
                .or(searchResultsLayer.resultCountProperty().isNotEqualTo(0))
                .or(elevationModelSettings.useDefaultElevationModelProperty()));
    }

    private <T extends ILayer> T createLayerAndBindEnabled(Class<T> layerClass, AsyncBooleanProperty enabled) {
        T layer = layerFactory.newLayer(layerClass);
        layer.enabledProperty().bindBidirectional(enabled);
        return layer;
    }

    private void refreshLayers(AsyncObservableList<ILayer> layers) {
        dispatcher.verifyAccess();
        List<ILayer> list;
        try (LockedList<ILayer> lock = layers.lock()) {
            list = new ArrayList<>(lock);
        }

        List<Layer> tmp = flattenLayers(list);

        model.getLayers().clear();
        model.getLayers().addAll(tmp);
    }

    private List<gov.nasa.worldwind.layers.Layer> flattenLayers(@Nullable List<ILayer> layers) {
        List<gov.nasa.worldwind.layers.Layer> list = new ArrayList<>();
        if (layers == null) {
            return list;
        }

        LayerGroup linesGroup = null;

        for (ILayer layer : layers) {
            if (layer instanceof LayerGroup) {
                LayerGroup layerGroup = (LayerGroup)layer;
                if (layerGroup.getType() == LayerGroupType.LINES_AND_GRIDS_GROUP) {
                    linesGroup = layerGroup;
                } else {
                    try (LockedList<ILayer> subList = layerGroup.subLayersProperty().lock()) {
                        list.addAll(flattenLayers(subList));
                    }
                }
            } else if (layer instanceof WWLayerWrapper) {
                list.add(((WWLayerWrapper)layer).getWrappedLayer());
            } else if (layer instanceof gov.nasa.worldwind.layers.Layer) {
                list.add((gov.nasa.worldwind.layers.Layer)layer);
            }
        }

        if (linesGroup != null) {
            try (LockedList<ILayer> subList = linesGroup.subLayersProperty().lock()) {
                list.addAll(flattenLayers(subList));
            }
        }

        Collections.reverse(list);
        return list;
    }

    @Override
    public ReadOnlyAsyncListProperty<ILayer> layersProperty() {
        return layers;
    }

    @Override
    public IMapDragManager getDragManager() {
        return dragger;
    }

    @Override
    public double getAltitudeForNewNodesWithinM() {
        return newAltInM;
    }

    @Override
    public AltitudeModes getAltitudeModeForNewNodes() {
        return newAltMode;
    }

    @Override
    public void addNodeFromGui(double latitude, double longitude) {
        if (!countryDetector.allowProceed(LatLon.fromDegrees(latitude, longitude))) {
            return;
        }

        if (nodeToInsertBehind instanceof IFlightplanRelatedObject) {
            IFlightplanRelatedObject wp = (IFlightplanRelatedObject)nodeToInsertBehind;

            if (wp.getFlightplan() == null) {
                // PicArea inside Matching
                IFlightplanRelatedObject parent = wp.getParent();
                if (parent == null) {
                    return; // points are not addable into this layer anyway..
                }

                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }

                if (parent instanceof MapLayerPicArea.MapLayerPicAreaFpContainer) {
                    MapLayerPicArea.MapLayerPicAreaFpContainer mapLayerPicAreaCont =
                        (MapLayerPicArea.MapLayerPicAreaFpContainer)parent;
                    if (!mapLayerPicAreaCont.getMapLayer().isVisibleIncludingParent()) {
                        return;
                    }
                }
            }

            // System.out.println("AA5");

            AltAssertModes assertAlt = AltAssertModes.unasserted;
            // newAltInCm = Math.round(CWaypoint.DEFAULT_ALT_WITHIN_M *100); // default alt = 100m

            IFlightplanContainer parent = wp.getFlightplan();
            // System.out.println("parent:"+parent);
            CWaypoint brotherWP = null;
            Integer brotherIndex = null; // this inserts at the end of the parents
            // members! (e.g.for landingpoint)

            if (wp instanceof IFlightplanStatement) {
                IFlightplanContainer cont = ((IFlightplanStatement)wp).getParent();
                if (cont != null) {
                    parent = cont;
                    // System.out.println("new parent:"+parent);
                }
            }

            if (parent == null) {
                Debug.getLog()
                    .log(
                        Level.SEVERE,
                        "Parent node for insertion is NULL nodeToInsertBehind:"
                            + nodeToInsertBehind
                            + "<"
                            + nodeToInsertBehind.getClass()
                            + "> fpOfIt:"
                            + wp.getFlightplan(),
                        new NullPointerException());
                return;
            }

            // determine index where to insert!
            if (parent != wp) { // if this is equal, we couldn't
                // find something
                for (brotherIndex = 0; brotherIndex != parent.sizeOfFlightplanContainer(); brotherIndex++) {
                    if (parent.getFromFlightplanContainer(brotherIndex) == wp) {
                        break;
                    }
                }
            }

            if (wp instanceof CWaypoint) {
                brotherWP = (CWaypoint)wp;
            }

            if (brotherWP == null) {
                // just travers the parent and search for something as close before
                // this as possible...
                PreviousWaypointVisitor vis = new PreviousWaypointVisitor(wp);
                vis.setSkipIgnoredPaths(true);
                vis.startVisitFlat(parent);
                brotherWP = vis.prevWaypoint;
            }

            if (brotherWP == null) {
                // just travers the parent and search for something as close before
                // this as possible...
                NextWaypointVisitor vis = new NextWaypointVisitor(wp);
                vis.setSkipIgnoredPaths(true);
                vis.startVisitFlat(parent);
                brotherWP = vis.nextWaypoint;
            }

            if (brotherWP != null) {
                newAltInM = brotherWP.getAltInMAboveFPRefPoint();
                newAltMode = AltitudeModes.relativeToStart;
                assertAlt = brotherWP.getAssertAltitudeMode();
            }

            // no else, because something hase both interfaces!
            if (wp instanceof IFlightplanContainer) {

                // in case of Flightplans & WaypointLoop 's it self is the container
                // where the
                // new stuff is inserted
                parent = (IFlightplanContainer)wp;

                // if mission or Loop is selected, inset at first position!
                brotherIndex = -1;
            }

            if (wp instanceof CPhotoSettings) {
                brotherIndex = -1;
            }

            // System.out.println("AA10:"+brotherIndex);

            try {
                IFlightplanStatement newStatement;
                Object newSelection = null;

                if (parent instanceof CPicAreaCorners || parent instanceof MapLayerPicArea) {
                    newStatement = new Point(parent, latitude, longitude);
                    newAltMode = AltitudeModes.clampToGround;
                    if (parent instanceof CPicAreaCorners) {
                        CPicAreaCorners corners = (CPicAreaCorners)parent;
                        // just add one node in spiral mode!
                        if (corners.getParent().getPlanType().onlySingleCorner()
                                && corners.sizeOfFlightplanContainer() != 0) {
                            return;
                        }
                        // patch, otherwise towers are not moveable after first adding
                        /*if (corners.getParent().getPlanType().onlySingleCorner()) {
                            setMouseModeDefault();
                            if (corners.sizeOfFlightplanContainer() != 0) {
                                return;
                            }

                            newSelection = corners.getParent();
                        }*/
                    }
                } else {
                    double alt = (newAltInM == Float.NEGATIVE_INFINITY) ? CWaypoint.ALTITUDE_MIN_WITHIN_M : newAltInM;
                    // System.out.println("parent" + parent);
                    newStatement =
                        FlightplanFactory.getFactory()
                            .newCWaypoint(longitude, latitude, alt, assertAlt, 0, CWaypoint.DEFAULT_BODY, parent);
                }

                if (!parent.isAddableToFlightplanContainer(newStatement)) {
                    return;
                }

                if (parent instanceof CPicAreaCorners) {
                    brotherIndex = null; // always add at the end
                }

                // if we adding a e.g. building and the origin isnt defined yet, it might get rendered under the ground
                // to fix this, we have to make sure that the height are measured relatively to the first click

                // most probably not needed anymore TODO test it !
                /*                Origin origin = parent.getFlightplan().getOrigin();
                if (!origin.isDefined() && parent.sizeOfFlightplanContainer() == 0) {
                    origin.updateAltitudeWgs84();
                }*/

                if (brotherIndex == null || brotherIndex == parent.sizeOfFlightplanContainer()) {
                    parent.addToFlightplanContainer(newStatement);
                } else {
                    parent.addToFlightplanContainer(brotherIndex + 1, newStatement);
                }

                if (parent instanceof CPicAreaCorners) {
                    CPicAreaCorners corners = (CPicAreaCorners)parent;
                    // on e.g. towers we should not wait for double clicks to exit adding mode... we can do this right
                    // away!
                    if (corners.getParent().getPlanType().onlySingleCorner()
                            && mapController.getMouseMode() == InputMode.ADD_POINTS) {
                        mapController.setMouseMode(InputMode.DEFAULT);
                    }
                }

                if (newSelection == null) {
                    selectionManager.setSelection(newStatement);
                } else {
                    selectionManager.setSelection(newSelection);
                }

            } catch (FlightplanContainerFullException e) {
                Debug.getLog().log(Level.SEVERE, "Container too full to add more nodes into", e);
            } catch (FlightplanContainerWrongAddingException e) {
                Debug.getLog().log(Level.SEVERE, "Unable to add this kind of node to the Container", e);
            }
        }
    }

    @Override
    public Future<Void> deleteSelectionAsync() {
        Dispatcher dispatcher = Dispatcher.platform();
        return dispatcher.runLaterAsync(
            () -> {
                Object selection = selectionManager.getSelection();
                if (selection instanceof eu.mavinci.flightplan.Point) {
                    eu.mavinci.flightplan.Point point = (eu.mavinci.flightplan.Point)selection;
                    IFlightplanContainer grantParent = point.getParent().getParent();
                    int minCorners = 3;
                    if (grantParent instanceof PicArea) {
                        PicArea picArea = (PicArea)grantParent;
                        minCorners = picArea.getPlanType().getMinCorners();
                    }

                    if (minCorners < point.getParent().sizeOfFlightplanContainer()) {
                        point.getParent().removeFromFlightplanContainer((IFlightplanStatement)point);
                    }
                } else if (selection instanceof eu.mavinci.flightplan.PicArea) {
                    eu.mavinci.flightplan.PicArea picArea = (eu.mavinci.flightplan.PicArea)selection;
                    if (dialogService.requestConfirmation(
                            languageHelper.getString("deleteAOI.rightClick.title"),
                            languageHelper.getString("deleteAOI.rightClick.message", picArea.getName()))) {
                        picArea.getParent().removeFromFlightplanContainer(picArea);
                        notificationCenter.publish(ActionManager.DELETE_AOI_EVENT, picArea);
                    }
                } else if (selection instanceof MapLayerPicArea) {
                    MapLayerPicArea picArea = (MapLayerPicArea)selection;
                    if (!picArea.getDeleteDisabled().get()) {
                        if (dialogService.requestConfirmation(
                                languageHelper.getString("deleteAreaFilter.rightClick.title"),
                                languageHelper.getString("deleteAreaFilter.rightClick.message"))) {
                            picArea.getParentLayer().removeMapLayer(picArea);
                            notificationCenter.publish(ActionManager.DELETE_AOI_EVENT, picArea);
                        }
                    }
                }
            });
    }

    @Override
    public AreaOfInterest addAreaOfInterest(Mission mission, PlanType aoiId) {
        if (mission == null) {
            return null;
        }

        AreaOfInterest area = new AreaOfInterest(mission, aoiId);
        PicArea picArea = area.getPicArea();

        try {
            if (aoiId.getMinCorners() == 1) {
                try {
                    LatLon center = mapController.getScreenCenter();
                    PicAreaCorners picAreaCorners = picArea.getCorners();
                    picAreaCorners.addToFlightplanContainer(
                        new Point(picAreaCorners, center.getLatitude().degrees, center.getLongitude().degrees));
                    return area;
                } catch (Exception e) {
                    LOGGER.error("cant add map center", e);
                }
            } else if (aoiId.isClosedPolygone()) {
                try {
                    Position[] corners = mapController.get4MapSectorsCenters();
                    PicAreaCorners picAreaCorners = picArea.getCorners();
                    for (Position p : corners) {
                        picAreaCorners.addToFlightplanContainer(
                            new Point(picAreaCorners, p.getLatitude().degrees, p.getLongitude().degrees));
                    }

                    return area;
                } catch (Exception e) {
                    LOGGER.error("cant add 4 corners from map view", e);
                }
            } else {
                try {
                    LatLon center = mapController.getScreenCenter();
                    PicAreaCorners picAreaCorners = picArea.getCorners();
                    picAreaCorners.addToFlightplanContainer(
                        new Point(picAreaCorners, center.getLatitude().degrees, center.getLongitude().degrees));
                    // no return, stay in add mode
                } catch (Exception e) {
                    LOGGER.error("cant add map center", e);
                }
            }
        } finally {
            mission.currentFlightPlanProperty().get().areasOfInterestProperty().add(area);

            if (picArea != null) {
                selectionManager.setSelection(picArea);
            } else {
                Flightplan fp = mission.getCurrentFlightPlan().getLegacyFlightplan();
                selectionManager.setSelection(fp);
            }
        }

        // mouse mode has to be changed AFTER selection of picArea, otherwise other listeners in EditFlightplanViewModel
        // get confused
        area.isInitialAddingProperty().setValue(true);
        mapController.setMouseMode(InputMode.ADD_POINTS);

        return area;
    }

    @Override
    public void setShowWireframeInterior(boolean visible) {
        dispatcher.runLater(() -> model.setShowWireframeInterior(visible));
    }

    private void setNodeToInsertBehind(Object o) {
        if (o instanceof IFlightplanRelatedObject) {
            IFlightplanRelatedObject wp = (IFlightplanRelatedObject)o;

            if (wp != null && wp.getFlightplan() != null && wp.getFlightplan().isOnAirFlightplan()) {
                wp = null; // don't insert something in the onAirFP
            }

            // prevent waypoint entering in usermode, so map all other fp objects to first or last corner
            // if (Application.getGuiLevel() == GuiLevels.USER) {
            if (wp instanceof PicArea) {
                wp = ((PicArea)wp).getCorners();
            } else if (wp instanceof LandingPoint) {
                // last corner
                Flightplan fp = (Flightplan)wp.getFlightplan();
                for (IFlightplanStatement s : fp) {
                    if (s instanceof PicArea) {
                        PicArea picArea = (PicArea)s;
                        PicAreaCorners corners = picArea.getCorners();
                        if (corners.sizeOfFlightplanContainer() == 0) {
                            wp = corners;
                        } else {
                            wp = corners.getLastElement();
                        }

                        break;
                    }
                }
            } else if (wp instanceof Flightplan || wp instanceof PhotoSettings) {
                // before first corner
                Flightplan fp = (Flightplan)wp.getFlightplan();
                for (IFlightplanStatement s : fp) {
                    if (s instanceof PicArea) {
                        PicArea picArea = (PicArea)s;
                        wp = picArea.getCorners();
                        break;
                    }
                }
            }
            // }

            o = wp;
        }

        // System.out.println("setNodeToInsertBehind(...)");
        if (nodeToInsertBehind != o) {
            // if (wp == null)
            // System.out.println("new NodeToInsertBehind: null");
            // else
            // System.out.println("new NodeToInsertBehind:" + wp.toString());
            nodeToInsertBehind = o;
            if (o instanceof Flightplan) {
                Flightplan fp = (Flightplan)o;
                FirstWaypointVisitor vis = new FirstWaypointVisitor();
                vis.setSkipIgnoredPaths(true);
                vis.startVisit(fp);
                if (vis.firstWaypoint != null) {
                    newAltInM = vis.firstWaypoint.getAltInMAboveFPRefPoint();
                    newAltMode = AltitudeModes.relativeToStart;
                } else {
                    newAltInM = CWaypoint.DEFAULT_ALT_WITHIN_M;
                    newAltMode = AltitudeModes.relativeToStart;
                }
            }

            if (o instanceof CWaypoint) {
                CWaypoint realWP = (CWaypoint)o;
                newAltInM = realWP.getAltInMAboveFPRefPoint();
                newAltMode = AltitudeModes.relativeToStart;
            } else if ((o instanceof CPicAreaCorners) || (o instanceof Point)) {
                newAltInM = Float.NEGATIVE_INFINITY;
                newAltMode = AltitudeModes.clampToGround;
            } else {
                newAltInM = CWaypoint.DEFAULT_ALT_WITHIN_M;
                newAltMode = AltitudeModes.relativeToStart;
            }
        }
    }

    @Override
    public AsyncListProperty<MapCreditViewModel> mapCreditsProperty() {
        return mapCredits;
    }

    public List<gov.nasa.worldwind.layers.Layer> getBackgroundLayers() {
        List<gov.nasa.worldwind.layers.Layer> background = new ArrayList<>();

        try (LockedList<ILayer> lockedList = layers.lock()) {
            List<ILayer> list = new ArrayList<>(lockedList);
            for (ILayer layer : list) {
                if (layer instanceof LayerGroup) {
                    LayerGroupType layerType = ((LayerGroup)layer).getType();

                    if (layerType.isBackground()) {
                        try (LockedList<ILayer> lockedSubList = ((LayerGroup)layer).getSubLayers().lock()) {
                            background.addAll(
                                flattenLayers(
                                    lockedSubList.stream().filter(e -> e.isEnabled()).collect(Collectors.toList())));
                        }
                    }
                }
            }
        }

        // to have kmls and geotiffs over the mapbox and base maps
        Collections.reverse(background);
        return background;
    }

}
