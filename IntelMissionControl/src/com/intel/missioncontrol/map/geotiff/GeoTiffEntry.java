/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.geotiff;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.binding.BidirectionalValueConverter;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.AsyncQuantityProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.UIAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.UIAsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.UIAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.UIAsyncQuantityProperty;
import com.intel.missioncontrol.beans.property.UIAsyncStringProperty;
import com.intel.missioncontrol.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.beans.property.UIQuantityPropertyMetadata;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.map.elevation.ElevationModelShiftWrapper;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationLayer;
import com.intel.missioncontrol.map.worldwind.GeotiffLayerWrapper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.GeoTiffSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import eu.mavinci.desktop.helper.MathHelper;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.CachedDataRaster;
import gov.nasa.worldwind.data.DataRaster;
import gov.nasa.worldwind.data.DataStoreProducer;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.data.TiledRasterProducer;
import gov.nasa.worldwind.data.WWDotNetLayerSetConverter;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;
import gov.nasa.worldwind.util.DataConfigurationFilter;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwindx.applications.worldwindow.features.DataImportUtil;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.time.Instant;
import java.util.HashSet;
import java.util.OptionalDouble;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.util.Duration;
import javax.xml.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GeoTiffEntry {

    private static Logger LOGGER = LoggerFactory.getLogger(GeoTiffEntry.class);

    private final GeoTiffSettings geoTiffSettings;
    private final IApplicationContext applicationContext;
    private final IGeoTiffManager geotiffHelper;
    private final ILanguageHelper languageHelper;
    private final IBackgroundTaskManager backgroundTaskManager;

    private final UIAsyncBooleanProperty enabled = new UIAsyncBooleanProperty(this);
    private final UIAsyncStringProperty name = new UIAsyncStringProperty(this);
    private final UIAsyncQuantityProperty<Dimension.Storage> diskUsage;
    private final UIAsyncDoubleProperty importProgress = new UIAsyncDoubleProperty(this);
    private final UIAsyncQuantityProperty<Dimension.Length> shift;
    private final UIAsyncObjectProperty<ElevationModelShiftWrapper.ShiftType> elevationModelShiftType =
        new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<GeoTiffType> type =
        new UIAsyncObjectProperty<>(
            this, new UIPropertyMetadata.Builder<GeoTiffType>().initialValue(GeoTiffType.UNKNOWN).create());

    // from the description, geotiff loading can happen in three phases:
    // not opened -> parsed once -> really loading it
    private final AsyncBooleanProperty isLoadingFile =
        new SimpleAsyncBooleanProperty(this, new UIPropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private ElevationModelShiftWrapper elevationModelShiftWrapper;
    private File file;
    private LoadingState loadingState = LoadingState.NOT_STARTED;
    private IBackgroundTaskManager.BackgroundTask loadingTask;
    private boolean isSynced;
    private boolean startedLoading;
    private boolean unloadStarted;
    private HashSet<String> existingCaches = new HashSet<String>();
    private Document dataConfig;

    private final AsyncObjectProperty<ILayer> mapLayer = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IElevationLayer> elevationLayer = new SimpleAsyncObjectProperty<>(this);
    private final SynchronizationRoot syncRoot;
    private final IMapView mapView;

    private class GeoTiffElevationLayer implements IElevationLayer {

        @Override
        public AsyncQuantityProperty<Dimension.Length> shiftProperty() {
            return GeoTiffEntry.this.shiftProperty();
        }

        @Override
        public AsyncBooleanProperty enabledProperty() {
            return GeoTiffEntry.this.enabledProperty();
        }

        @Override
        public AsyncStringProperty nameProperty() {
            return GeoTiffEntry.this.nameProperty();
        }

        @Override
        public AsyncQuantityProperty<Dimension.Storage> diskUsageProperty() {
            return GeoTiffEntry.this.diskUsageProperty();
        }

        @Override
        public ReadOnlyAsyncObjectProperty<Instant> sourceModifyedDateProperty() {
            return GeoTiffEntry.this.sourceModifyedDateProperty();
        }

        @Override
        public AsyncObjectProperty<ElevationModelShiftWrapper.ShiftType> elevationModelShiftTypeProperty() {
            return GeoTiffEntry.this.elevationModelShiftTypeProperty();
        }

        @Override
        public AsyncDoubleProperty importProgressProperty() {
            return GeoTiffEntry.this.importProgressProperty();
        }

        @Override
        public void autoDetectManualOffset() {
            GeoTiffEntry.this.autoDetectManualOffset();
        }

        @Override
        public ElevationModel getElevationModel() {
            return GeoTiffEntry.this.elevationModelShiftWrapper;
        }

        @Override
        public void dropCache() {
            WWFactory.dropDataCache(GeoTiffEntry.this.elevationModelShiftWrapper.getSlave());
        }

    }

    @Inject
    public GeoTiffEntry(
            IGeoTiffManager geotiffHelper,
            ISettingsManager settingsManager,
            GeoTiffSettings geoTiffSettings,
            IApplicationContext applicationContext,
            ILanguageHelper languageHelper,
            IBackgroundTaskManager backgroundTaskManager,
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            IMapView mapView) {
        this.syncRoot = syncRoot;
        this.backgroundTaskManager = backgroundTaskManager;
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        this.geotiffHelper = geotiffHelper;
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        this.diskUsage =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Dimension.Storage>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.STORAGE)
                    .create());

        this.shift =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Dimension.Length>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.LOCALIZED_LENGTH)
                    .create());

        this.geoTiffSettings = geoTiffSettings;
        this.mapView = mapView;

        file = new File(geoTiffSettings.pathProperty().get());
        enabled.bindBidirectional(geoTiffSettings.enabledProperty());
        name.bindBidirectional(geoTiffSettings.nameProperty());
        shift.bindBidirectional(
            getGeoTiffSettings().manualElevationShiftProperty(),
            new BidirectionalValueConverter<>() {
                @Override
                public Number convertBack(Quantity<Dimension.Length> value) {
                    return value.convertTo(Unit.METER).getValue().doubleValue();
                }

                @Override
                public Quantity<Dimension.Length> convert(Number value) {
                    return Quantity.of(value, Unit.METER);
                }
            });
        elevationModelShiftType.bindBidirectional(getGeoTiffSettings().elevationModelShiftTypeProperty());

        load();
    }

    public GeoTiffSettings getGeoTiffSettings() {
        return geoTiffSettings;
    }

    private void load() {
        if (getLoadingState() == LoadingState.NOT_STARTED) {
            geotiffHelper
                .importGeoTiff(this)
                .onFailure(this::errorLoading)
                .onSuccess(
                    (geoTiffEntry) -> {
                        if (getLoadingState() == LoadingState.AWAITING_IMPORT) {
                            final String taskName = languageHelper.getString("geotiff.loading", getName());
                            loadingTask =
                                new IBackgroundTaskManager.BackgroundTask(taskName) {
                                    @Override
                                    protected Void call() throws Exception {
                                        doImportIntoCache(this);
                                        ElevationModelShiftWrapper em = geoTiffEntry.elevationModelShiftWrapper;

                                        if (em != null) {
                                            // auto setup new imported elevation geotiffs
                                            geoTiffEntry.elevationModelShiftType.setAsync(
                                                ElevationModelShiftWrapper.ShiftType.MANUAL);
                                            em.setShiftType(ElevationModelShiftWrapper.ShiftType.MANUAL);
                                            em.autoAdjustShift();

                                            Dispatcher.schedule(em::autoAdjustShift, Duration.millis(1000));
                                            Dispatcher.schedule(em::autoAdjustShift, Duration.millis(3000));
                                            Dispatcher.schedule(
                                                () -> {
                                                    em.autoAdjustShift();
                                                    geoTiffEntry.shift.setAsync(Quantity.of(em.getShift(), Unit.METER));
                                                },
                                                Duration.millis(5000));
                                        }

                                        return null;
                                    }

                                };
                            loadingTask.updateProgress(0, 1);
                            loadingTask.updateMessage(taskName);
                            importProgress.bind(loadingTask.progressProperty());

                            loadingTask.addEventHandler(
                                WorkerStateEvent.ANY,
                                event -> {
                                    if (event.getEventType() == WorkerStateEvent.WORKER_STATE_CANCELLED) {
                                        Dispatcher.postToUI(() -> geotiffHelper.dropGeotiffImport(geoTiffEntry));
                                    } else if (event.getEventType() == WorkerStateEvent.WORKER_STATE_FAILED) {
                                        Dispatcher.postToUI(
                                            () -> {
                                                geoTiffEntry.errorLoading(loadingTask.getException());
                                            });
                                    }
                                });
                            loadingTask.setOnSucceeded(
                                (event) -> {
                                    Dispatcher.postToUI(
                                        () -> {
                                            if (getLoadingState() == LoadingState.CANCELT) {
                                                geotiffHelper.dropGeotiffImport(geoTiffEntry);
                                            } else {
                                                geoTiffEntry.syncWithLegacy();
                                                applicationContext.addToast(
                                                    Toast.of(ToastType.INFO)
                                                        .setText(
                                                            languageHelper.getString("geotiff.loadingDone", getName()))
                                                        .setAction(
                                                            languageHelper.getString("geotiff.loadingDone.action"),
                                                            false,
                                                            true,
                                                            geoTiffEntry::zoomToItem,
                                                            Platform::runLater)
                                                        .create());
                                            }
                                        });
                                });

                            backgroundTaskManager.submitTask(loadingTask);
                        } else {
                            geoTiffEntry.syncWithLegacy();
                        }
                    },
                    Platform::runLater);
        } else {
            Platform.runLater(this::syncWithLegacy);
        }
    }

    private void errorLoading(Throwable e) {
        LOGGER.warn("problems loading geotiff:" + nameProperty().get(), e);
        geotiffHelper.dropGeotiffImport(this);
        applicationContext.addToast(
            Toast.of(ToastType.ALERT)
                .setText(languageHelper.getString("geotiff.loadingErr", nameProperty().get()))
                .create());
    }

    public void zoomToItem() {
        Sector sector = this.sector.get();
        if (sector != null) {
            mapView.goToSectorAsync(sector, OptionalDouble.empty());
        }
    }

    private void syncWithLegacy() {
        loadingState = LoadingState.NOT_STARTED;

        if (isSynced) {
            return;
        }

        isSynced = true;
        importProgress.unbind();
        ElevationModelShiftWrapper em = elevationModelShiftWrapper;
        if (em != null) {
            em.setShiftType(elevationModelShiftTypeProperty().get());
            em.setShift(shift.get().convertTo(Unit.METER).getValue().doubleValue());

            elevationModelShiftTypeProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        em.setShiftType(newValue);
                        shift.setAsync(Quantity.of(em.getShift(), Unit.METER));
                    });
            shift.addListener(
                (observable, oldValue, newValue) ->
                    em.setShift(newValue.convertTo(Unit.METER).getValue().doubleValue()));
        } else {
            elevationModelShiftTypeProperty().set(null);
        }

        sector.set(getSector());
        diskUsage.set(Quantity.of(getDiskUsage(), Unit.BYTE));
        date.set(getDateInternal());
        isLoadingFile.set(false);
    }

    @Override
    public String toString() {
        return getName() + "\tgetIsLoadingFile:" + getIsLoadingFile();
    }

    public AsyncBooleanProperty isLoadingFileProperty() {
        return isLoadingFile;
    }

    public UIAsyncBooleanProperty enabledProperty() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public UIAsyncStringProperty nameProperty() {
        return name;
    }

    public ReadOnlyAsyncObjectProperty<GeoTiffType> typeProperty() {
        return type;
    }

    public GeoTiffType getType() {
        return type.get();
    }

    private final ObjectProperty<Sector> sector = new SimpleObjectProperty<>();

    public ReadOnlyObjectProperty<Sector> sectorProperty() {
        return sector;
    }

    public AsyncQuantityProperty<Dimension.Storage> diskUsageProperty() {
        return diskUsage;
    }

    private final AsyncObjectProperty<Instant> date = new SimpleAsyncObjectProperty<>(this);

    public ReadOnlyAsyncObjectProperty<Instant> sourceModifyedDateProperty() {
        return date;
    }

    public boolean getIsLoadingFile() {
        return isLoadingFile.get();
    }

    public String getName() {
        return nameProperty().get();
    }

    public Instant getDate() {
        return date.get();
    }

    public UIAsyncObjectProperty<ElevationModelShiftWrapper.ShiftType> elevationModelShiftTypeProperty() {
        return elevationModelShiftType;
    }

    public ElevationModelShiftWrapper.ShiftType getElevationModelShiftType() {
        return elevationModelShiftType.get();
    }

    public UIAsyncQuantityProperty<Dimension.Length> shiftProperty() {
        return shift;
    }

    void unloadEntry() {
        if (unloadStarted) {
            return;
        }

        unloadStarted = true;
        IBackgroundTaskManager.BackgroundTask bt = loadingTask;
        if (bt != null) {
            bt.cancel();
            loadingTask = null;
        }

        importProgress.unbind();
        // geotiffHelper.dropGeotiffImport(this);
    }

    public void autoDetectManualOffset() {
        ElevationModelShiftWrapper em = elevationModelShiftWrapper;
        if (em != null) {
            em.autoAdjustShift();
            shift.setAsync(Quantity.of(em.getShift(), Unit.METER));
        }
    }

    public AsyncDoubleProperty importProgressProperty() {
        return importProgress;
    }

    public LoadingState getLoadingState() {
        return loadingState;
    }

    public static enum LoadingState {
        NOT_STARTED,
        AWAITING_IMPORT,
        DONE,
        CANCELT
    }

    public synchronized void loadWwj() throws Exception {
        if (startedLoading) {
            return;
        }

        startedLoading = true;

        FileStore fileStore = WorldWind.getDataFileStore();

        // Import the image into the FileStore by converting it to
        // the World Wind Java cache format.
        startImportFile(file.getName(), file, fileStore);
    }

    public Sector getSector() {
        if (sector.get() == null && dataConfig != null) {
            sector.set(WWXML.getSector(dataConfig.getDocumentElement(), "Sector", null));
        }

        return sector.get();
    }

    public long getDiskUsage() {
        if (dataConfig != null) {
            Long diskUsage = WWXML.getLong(dataConfig.getDocumentElement(), "DiskUsage", null);
            return diskUsage != null
                ? diskUsage + 2054 // adding some additional byte for encountering the metadata XMLs
                : 0;
        }

        return 0;
    }

    private Instant getDateInternal() {
        if (dataConfig != null) {
            Long sourceFileDate = WWXML.getLong(dataConfig.getDocumentElement(), "SourceFileDate", null);
            return sourceFileDate != null ? Instant.ofEpochMilli(sourceFileDate) : null;
        }

        return null;
    }

    private void addImportedData(final Document dataConfig, final AVList params) {
        this.dataConfig = dataConfig;

        // System.out.println("importing data (from cache Or File) " + dataConfig + " params"+params);
        Element domElement = dataConfig.getDocumentElement();
        String type = DataConfigurationUtils.getDataConfigType(domElement);

        if (type == null) {
            return;
        }

        if (type.equalsIgnoreCase("Layer")) {
            createImgLayerAsync(domElement, params)
                .onSuccess(
                    fxLayer -> {
                        mapLayer.set(fxLayer);
                        this.type.set(GeoTiffType.IMAGERY);
                    },
                    Dispatcher::dispatchToUI);
        } else if (type.equalsIgnoreCase("ElevationModel")) {
            createElevationLayer(domElement, params);
        }
    }

    public ReadOnlyAsyncObjectProperty<ILayer> mapLayerProperty() {
        return mapLayer;
    }

    public ReadOnlyAsyncObjectProperty<IElevationLayer> elevationLayerProperty() {
        return elevationLayer;
    }

    private FluentFuture<ILayer> createImgLayerAsync(Element domElement, AVList params) {
        return syncRoot.dispatch(
            () -> {
                Factory factory = (Factory)WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
                Layer layer = (Layer)factory.createFromConfigSource(domElement, params);
                layer.setEnabled(true); // BasicLayerFactory creates layer which is intially disabled
                ILayer fxLayer = new GeotiffLayerWrapper(layer, syncRoot, geoTiffSettings);
                fxLayer.enabledProperty().bindBidirectional(enabled);
                fxLayer.nameProperty().bind(name, LayerName::new);
                return fxLayer;
            });
    }

    // TODO ThreadSafety
    private void createElevationLayer(Element domElement, AVList params) {
        Factory factory = new BasicElevationModelFactory();
        BasicElevationModel slave = (BasicElevationModel)factory.createFromConfigSource(domElement, params);
        slave.setDetailHint(0.4);

        IEgmModel egmModel = DependencyInjector.getInstance().getInstanceOf(IEgmModel.class);
        elevationModelShiftWrapper = new ElevationModelShiftWrapper(slave, getSector(), egmModel);
        IElevationLayer layer = new GeoTiffElevationLayer();
        elevationLayer.set(layer);
        Dispatcher.postToUI(() -> GeoTiffEntry.this.type.set(GeoTiffType.ELEVATION));
    }

    private void startImportFile(String displayName, File file, FileStore fileStore) throws Exception {
        // Use the FileStore's install location as the destination for the
        // imported image tiles. The install location
        // is an area in the data file store for permanantly resident data.

        Configuration.setValue(AVKey.PRODUCER_ENABLE_FULL_PYRAMID, true);

        // this tries to load existing data and fills the list of known caches.
        for (File f : fileStore.getLocations()) {
            // System.out.println("fileStoreLoc" + f);
            if (!f.exists()) {
                continue;
            }

            loadImportedDataFromDirectory(f);
        }

        if (dataConfig == null) {
            loadingState = LoadingState.AWAITING_IMPORT;
        } else {
            loadingState = LoadingState.DONE;
        }
    }

    private void doImportIntoCache(IBackgroundTaskManager.BackgroundTask backgroundTask) throws Exception {
        LOGGER.info("start import geoTiff " + file + " from the scratch");
        Document localDataConfig = null;
        // Import the file into a form usable by World Wind components.

        // create name
        String cacheName;
        if (file.getParentFile() != null) {
            cacheName = file.getParentFile().getName() + "." + file.getName();
        } else {
            cacheName = file.getName();
        }

        int i = 0;
        while (existingCaches.contains(cacheName)) {
            i++;
            cacheName = file.getName() + "-" + i;
        }

        cacheName +=
            file.getAbsolutePath()
                .hashCode(); // add some stuff, since simultaniously loading will otherwise cause problems
        geoTiffSettings.cacheNameProperty().setValue(cacheName);
        FileStore fileStore = WorldWind.getDataFileStore();
        localDataConfig = importDataFromFile(file, fileStore, cacheName, backgroundTask, type);

        if (localDataConfig != null) {
            AVList params = new AVListImpl();
            addImportedData(localDataConfig, params);
            loadingState = LoadingState.DONE;
        } else {
            loadingState = LoadingState.CANCELT;
        }
    }

    // **************************************************************//
    // ******************** Loading Previously Imported Data ******//
    // **************************************************************//

    private void loadImportedDataFromDirectory(File dir) {
        String[] names = WWIO.listDescendantFilenames(dir, new DataConfigurationFilter(), false);
        if (names == null || names.length == 0) {
            return;
        }

        XPath path = WWXML.makeXPath();
        String cacheName = geoTiffSettings.cacheNameProperty().get();

        for (String filename : names) {
            Document doc = null;

            try {
                File dataConfigFile = new File(dir, filename);
                doc = WWXML.openDocument(dataConfigFile);
                doc = DataConfigurationUtils.convertToStandardDataConfigDocument(doc);
            } catch (WWRuntimeException e) {
                LOGGER.warn("imporing issue " + filename, e);
            }

            if (doc == null) {
                continue;
            }

            String dataCacheName = WWXML.getText(doc.getDocumentElement(), "DataCacheName", path);
            if (dataCacheName == null) {
                continue;
            }

            existingCaches.add(dataCacheName);
            if (dataCacheName.equals(cacheName)) {
                // This data configuration came from an existing file from disk,
                // therefore we cannot guarantee that the
                // current version of World Wind's data importers produced it. This
                // data configuration file may have been
                // created by a previous version of World Wind, or by another
                // program. Set fallback values for any missing
                // parameters that World Wind needs to construct a Layer or
                // ElevationModel from this data configuration.
                AVList params = new AVListImpl();
                setFallbackParams(doc, filename, params);

                // Add the data configuraiton to the ImportedDataPanel.
                LOGGER.info("load geoTiff " + cacheName + " from cache");
                addImportedData(doc, params);
                return;
            }
        }
    }

    private static void setFallbackParams(Document dataConfig, String filename, AVList params) {
        XPath xpath = WWXML.makeXPath();
        Element domElement = dataConfig.getDocumentElement();

        // If the data configuration document doesn't define a cache name, then
        // compute one using the file's path
        // relative to its file cache directory.
        String s = WWXML.getText(domElement, "DataCacheName", xpath);
        if (s == null || s.length() == 0) {
            DataConfigurationUtils.getDataConfigCacheName(filename, params);
        }

        // If the data configuration document doesn't define the data's extreme
        // elevations, provide default values using
        // the minimum and maximum elevations of Earth.
        String type = DataConfigurationUtils.getDataConfigType(domElement);
        if (type.equalsIgnoreCase("ElevationModel")) {
            if (WWXML.getDouble(domElement, "ExtremeElevations/@min", xpath) == null) {
                params.setValue(AVKey.ELEVATION_MIN, Earth.ELEVATION_MIN);
            }

            if (WWXML.getDouble(domElement, "ExtremeElevations/@max", xpath) == null) {
                params.setValue(AVKey.ELEVATION_MAX, Earth.ELEVATION_MAX);
            }
        }
    }
    // **************************************************************//
    // ******************** Importing Data From File **************//
    // **************************************************************//

    private static Document importDataFromFile(
            File file,
            FileStore fileStore,
            String cacheName,
            IBackgroundTaskManager.BackgroundTask backgroundTask,
            AsyncObjectProperty<GeoTiffType> type)
            throws Exception {
        // Create a DataStoreProducer which is capable of processing the file.
        final DataStoreProducer producer = createDataStoreProducerFromFile(file);
        Dispatcher.postToUI(
            () -> {
                if (producer instanceof TiledElevationProducer) {
                    type.set(GeoTiffType.ELEVATION);
                } else {
                    type.set(GeoTiffType.IMAGERY);
                }
            });
        if (producer == null) {
            throw new IllegalArgumentException("Unrecognized file type");
        }

        // Configure the ProgressMonitor to receive progress events from the DataStoreProducer. This stops sending
        // progress events when the user clicks the "Cancel" button, ensuring that the ProgressMonitor does not
        PropertyChangeListener progressListener =
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (backgroundTask.isCancelled()) {
                        producer.stopProduction();
                        return;
                    }

                    if (evt.getPropertyName().equals(AVKey.PROGRESS)) {
                        backgroundTask.updateProgress(MathHelper.intoRange((Double)evt.getNewValue(), 0, 1), 1);
                    }
                }
            };
        producer.addPropertyChangeListener(progressListener);
        backgroundTask.updateProgress(0, 1);

        Document doc = null;
        try {
            // Import the file into the specified FileStore.
            doc = createDataStoreFromFile(file, fileStore, producer, cacheName);
            if (null != doc) {
                createRasterServerConfigDoc(fileStore, producer);
            }

            // The user clicked the ProgressMonitor's "Cancel" button. Revert any change made during production, and
            // discard the returned DataConfiguration reference.
            if (backgroundTask.isCancelled()) {
                doc = null;
                producer.removeProductionState();
            }
        } finally {
            // Remove the progress event listener from the DataStoreProducer. stop the progress timer, and signify to
            // the
            // ProgressMonitor that we're done.
            producer.removePropertyChangeListener(progressListener);
            producer.removeAllDataSources();
        }

        return doc;
    }

    private static Document createDataStoreFromFile(
            File file, FileStore fileStore, DataStoreProducer producer, String cacheName) throws Exception {
        File importLocation = DataImportUtil.getDefaultImportLocation(fileStore);
        if (importLocation == null) {
            String message = Logging.getMessage("generic.NoDefaultImportLocation");
            Logging.logger().severe(message);
            return null;
        }

        // Create the production parameters. These parameters instruct the DataStoreProducer where to import the cached
        // data, and what name to put in the data configuration document.
        AVList params = new AVListImpl();
        params.setValue(AVKey.DATASET_NAME, cacheName);
        params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
        params.setValue(AVKey.FILE_STORE_LOCATION, importLocation.getAbsolutePath());
        params.setValue(AVKey.SOURCE_FILE_LAST_MODIFIED, file.lastModified());

        // These parameters define producer's behavior:
        // create a full tile cache OR generate only first two low resolution levels
        boolean enableFullPyramid = Configuration.getBooleanValue(AVKey.PRODUCER_ENABLE_FULL_PYRAMID, false);
        if (!enableFullPyramid) {
            params.setValue(AVKey.SERVICE_NAME, AVKey.SERVICE_NAME_LOCAL_RASTER_SERVER);
            params.setValue(AVKey.TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL, 2);
        }

        producer.setStoreParameters(params);

        // Use the specified file as the the production data source.
        producer.offerDataSource(file, null);

        try {
            // Convert the file to a form usable by World Wind components, according to the specified DataStoreProducer.
            // This throws an exception if production fails for any reason.
            producer.startProduction();
        } catch (Exception e) {
            // Exception attempting to convert the file. Revert any change made during production.
            producer.removeProductionState();
            throw e;
        }

        // Return the DataConfiguration from the production results. Since production successfully completed, the
        // DataStoreProducer should contain a DataConfiguration in the production results. We test the production
        // results anyway.
        Iterable<?> results = producer.getProductionResults();
        if (results != null && results.iterator() != null && results.iterator().hasNext()) {
            Object o = results.iterator().next();
            if (o != null && o instanceof Document) {
                return (Document)o;
            }
        }

        return null;
    }

    private static void createRasterServerConfigDoc(FileStore fileStore, DataStoreProducer producer) {
        File importLocation = DataImportUtil.getDefaultImportLocation(fileStore);
        if (importLocation == null) {
            String message = Logging.getMessage("generic.NoDefaultImportLocation");
            Logging.logger().severe(message);
            return;
        }

        Document doc = WWXML.createDocumentBuilder(true).newDocument();

        Element root = WWXML.setDocumentElement(doc, "RasterServer");
        WWXML.setTextAttribute(root, "version", "1.0");

        StringBuffer sb = new StringBuffer();
        sb.append(importLocation.getAbsolutePath()).append(File.separator);

        AVList rasterServerParams = new AVListImpl();

        rasterServerParams.setValue(AVKey.BANDS_ORDER, "Auto");
        rasterServerParams.setValue(AVKey.BLACK_GAPS_DETECTION, "enable");

        AVList productionParams = producer.getProductionParameters();
        productionParams = (null == productionParams) ? new AVListImpl() : productionParams;

        if (productionParams.hasKey(AVKey.DATA_CACHE_NAME)) {
            String value = productionParams.getStringValue(AVKey.DATA_CACHE_NAME);
            rasterServerParams.setValue(AVKey.DATA_CACHE_NAME, value);
            sb.append(value).append(File.separator);
        } else {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.DATA_CACHE_NAME);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        if (productionParams.hasKey(AVKey.DATASET_NAME)) {
            String value = productionParams.getStringValue(AVKey.DATASET_NAME);
            rasterServerParams.setValue(AVKey.DATASET_NAME, value);
            sb.append(value).append(".RasterServer.xml");
        } else {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.DATASET_NAME);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        Object o = productionParams.getValue(AVKey.DISPLAY_NAME);
        if (WWUtil.isEmpty(o)) {
            o = productionParams.getValue(AVKey.DATASET_NAME);
        }

        rasterServerParams.setValue(AVKey.DISPLAY_NAME, o);

        String rasterServerConfigFilePath = sb.toString();

        Sector extent = null;
        if (productionParams.hasKey(AVKey.SECTOR)) {
            o = productionParams.getValue(AVKey.SECTOR);
            if (null != o && o instanceof Sector) {
                extent = (Sector)o;
            }
        }

        if (null != extent) {
            WWXML.appendSector(root, "Sector", extent);
        } else {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.SECTOR);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        Element sources = doc.createElementNS(null, "Sources");
        if (producer instanceof TiledRasterProducer) {
            TiledRasterProducer tiledRasterProducer = (TiledRasterProducer)producer;

            for (DataRaster raster : tiledRasterProducer.getDataRasters()) {
                if (raster instanceof CachedDataRaster) {
                    CachedDataRaster readRaster = (CachedDataRaster)raster;
                    o = readRaster.getDataSource();
                    if (WWUtil.isEmpty(o)) {
                        Logging.logger().finest(Logging.getMessage("nullValue.DataSourceIsNull"));
                        continue;
                    }

                    File f = WWIO.getFileForLocalAddress(o);
                    if (WWUtil.isEmpty(f)) {
                        String message = Logging.getMessage("TiledRasterProducer.UnrecognizedDataSource", o);
                        Logging.logger().finest(message);
                        continue;
                    }

                    Element source = WWXML.appendElement(sources, "Source");
                    WWXML.setTextAttribute(source, "type", "file");
                    WWXML.setTextAttribute(source, "path", f.getAbsolutePath());

                    AVList params = readRaster.getParams();
                    if (null == params) {
                        Logging.logger().warning(Logging.getMessage("nullValue.ParamsIsNull"));
                        continue;
                    }

                    Sector sector = raster.getSector();
                    if (null == sector && params.hasKey(AVKey.SECTOR)) {
                        o = params.getValue(AVKey.SECTOR);
                        if (null != o && o instanceof Sector) {
                            sector = (Sector)o;
                        }
                    }

                    if (null != sector) {
                        WWXML.appendSector(source, "Sector", sector);
                    }

                    String[] keysToCopy =
                        new String[] {
                            AVKey.PIXEL_FORMAT,
                            AVKey.DATA_TYPE,
                            AVKey.PIXEL_WIDTH,
                            AVKey.PIXEL_HEIGHT,
                            AVKey.COORDINATE_SYSTEM,
                            AVKey.PROJECTION_NAME
                        };

                    WWUtil.copyValues(params, rasterServerParams, keysToCopy, false);
                } else {
                    String message =
                        Logging.getMessage(
                            "TiledRasterProducer.UnrecognizedRasterType",
                            raster.getClass().getName(),
                            raster.getStringValue(AVKey.DATASET_NAME));
                    Logging.logger().severe(message);
                    throw new WWRuntimeException(message);
                }
            }
        }

        // add sources
        root.appendChild(sources);

        WWXML.saveDocumentToFile(doc, rasterServerConfigFilePath);
    }

    // **************************************************************//
    // ******************** Utility Methods ***********************//
    // **************************************************************//

    private static DataStoreProducer createDataStoreProducerFromFile(File file) {
        if (file == null) {
            return null;
        }

        DataStoreProducer producer = null;

        AVList params = new AVListImpl();
        if (DataImportUtil.isDataRaster(file, params)) {
            if (AVKey.ELEVATION.equals(params.getStringValue(AVKey.PIXEL_FORMAT))) {
                producer = new TiledElevationProducer();
            } else if (AVKey.IMAGE.equals(params.getStringValue(AVKey.PIXEL_FORMAT))) {
                producer = new TiledImageProducer();
            }
        } else if (DataImportUtil.isWWDotNetLayerSet(file)) {
            producer = new WWDotNetLayerSetConverter();
        }

        return producer;
    }

}
