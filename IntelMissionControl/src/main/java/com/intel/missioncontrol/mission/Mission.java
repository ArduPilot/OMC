/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.custom.LazyDefaultObjectProperty;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.SrsSettings;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.visitors.ExtractPicAreasVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.geo.ISectorReferenced;
import eu.mavinci.plane.Airplane;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mission implements Comparable<Mission>, ISectorReferenced {

    public interface Factory {
        Mission create(MissionInfo missionInfo);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Mission.class);
    public static final String DEMO_MISSION_NAME = "DEMO";

    private final StringProperty name = new SimpleStringProperty();
    private final AsyncObjectProperty<Path> directory =
        new SimpleAsyncObjectProperty<>(this) {
            @Override
            protected void invalidated() {
                Path path = get();
                name.set(path.getFileName().toString());
            }
        };

    private final ObjectProperty<Date> lastModified = new SimpleObjectProperty<>();
    private final ObjectProperty<MSpatialReference> srs = new SimpleObjectProperty<>();
    private final ListProperty<FlightPlan> flightPlans =
        new SimpleListProperty<>(FXCollections.observableArrayList(fp -> new Observable[] {fp.nameProperty()}));
    private final ListProperty<FlightPlanTemplate> flightPlanTemplates =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<FlightPlan> currentFlightPlan = new SimpleObjectProperty<>();
    private final ObjectProperty<FlightPlanTemplate> currentFlightPlanTemplate = new SimpleObjectProperty<>();
    private final ObjectProperty<Drone> uav =
        new LazyDefaultObjectProperty<>(() -> Drone.forLegacyPlane(createNewLegacyPlane()));
    private final BooleanProperty missionEmpty = new SimpleBooleanProperty(true);
    private final ListProperty<Matching> matchings =
        new SimpleListProperty<>(
            FXCollections.observableArrayList(dataset -> new Observable[] {dataset.nameProperty()}));
    private final ListProperty<File> flightLogs = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<Matching> currentMatching = new SimpleObjectProperty<>();
    private final AsyncObjectProperty<LatLon> startCoordinates =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<LatLon>().initialValue(LatLon.ZERO).create());

    private boolean isLoaded;

    private final IApplicationContext applicationContext;
    private final MissionInfo missionInfo;
    private final MavinciObjectFactory mavinciObjectFactory;
    private final IMissionInfoManager missionSettingsManager;
    private final ILanguageHelper languageHelper;
    private final IHardwareConfigurationManager hardwareConfigurationManager;

    /**
     * This field probably will not be here. Only the class that will be responsible for creating new missions will have
     * it. Then user chooses properties that he needs and creates new FP with them
     */
    private BooleanProperty containsTemplatesBindings = new SimpleBooleanProperty();

    private Sector earlySector;

    @Inject
    public Mission(
            @Assisted MissionInfo missionInfo,
            IApplicationContext applicationContext,
            MavinciObjectFactory mavinciObjectFactory,
            ISettingsManager settingsManager,
            IMissionInfoManager missionInfoManager,
            ILanguageHelper languageHelper,
            IHardwareConfigurationManager hardwareConfigurationManager,
            ISrsManager srsManager) {
        this.applicationContext = applicationContext;
        this.missionInfo = missionInfo;
        this.mavinciObjectFactory = mavinciObjectFactory;
        this.missionSettingsManager = missionInfoManager;
        this.languageHelper = languageHelper;
        this.hardwareConfigurationManager = hardwareConfigurationManager;

        Path missionFolder = missionInfo.getFolder().toPath();
        Expect.isTrue(Files.isDirectory(missionFolder), "missionFolder", "The specified path is not a directory.");

        SrsSettings srsSettings = settingsManager.getSection(SrsSettings.class);
        MSpatialReference spatialReference = srsManager.getSrsByIdOrNull(missionInfo.getSrsId());
        if (spatialReference == null) {
            spatialReference = srsSettings.getApplicationSrs();
        }

        srs.set(spatialReference);

        // needs to be non-null
        directory.set(missionFolder);
        lastModified.set(missionInfo.getLastModified());
        containsTemplatesBindings.bind(flightPlanTemplates.emptyProperty().not());
        currentFlightPlanTemplate.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue == null) {
                    currentFlightPlan.set(null);
                } else {
                    currentFlightPlan.set(newValue.getFlightPlan());
                }
            });

        loadFlightPlans(this.missionInfo);
        loadDataSets(this.missionInfo);
        loadFlightLogs(this.missionInfo);

        earlySector = missionInfo.getSector();
        startCoordinates.set(missionInfo.getStartCoordinates());

        flightPlans.addListener((observable, oldValue, newValue) -> updateMissionSettingsFile(missionInfo));
        matchings.addListener((observable, oldValue, newValue) -> updateMissionSettingsFile(missionInfo));
        flightLogs.addListener((observable, oldValue, newValue) -> updateMissionSettingsFile(missionInfo));
        directory.addListener((observable, oldValue, newValue) -> updateMissionSettingsFile(missionInfo));
        missionSettingsManager.saveToFile(this.missionInfo);
    }

    public ObjectProperty<MSpatialReference> srsProperty() {
        return srs;
    }

    public File getMatchingFolder() {
        return MissionConstants.getMatchingsFolder(getDirectoryFile());
    }

    public File getFlightLogsFolder() {
        return MissionConstants.getFlightLogsFolder(getDirectoryFile());
    }

    public File getFlightplanFolder() {
        return MissionConstants.getFlightplanFolder(getDirectoryFile());
    }

    private void loadDataSets(MissionInfo missionInfo) {
        List<Matching> loadedDataSets = new ArrayList<>();
        for (String path : missionInfo.getLoadedDataSets()) {
            try {
                Matching matching =
                    new Matching(
                        FileHelper.unmakeRelativePath(getMatchingFolder(), path), hardwareConfigurationManager);
                loadedDataSets.add(matching);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                // skip invalid
            }
        }

        matchings.setAll(loadedDataSets);
        if (!matchings.isEmpty()) {
            currentMatching.set(matchings.get(0));
        }
    }

    private void loadFlightLogs(MissionInfo missionInfo) {
        for (String path : missionInfo.getFlightLogs()) {
            try {
                File file = FileHelper.unmakeRelativePath(getFlightLogsFolder(), path);
                flightLogs.add(file);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void loadFlightPlans(MissionInfo missionInfo) {
        List<Flightplan> loadedFlightPlans = new ArrayList<>();
        for (String path : missionInfo.getLoadedFlightPlans()) {
            try {
                File f = FileHelper.unmakeRelativePath(getFlightplanFolder(), path);
                if (!f.exists()) {
                    f = new File(getFlightplanFolder(), path);
                }

                Flightplan fp = new Flightplan(f);
                loadedFlightPlans.add(fp);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                // skip invalid ...?
            }
        }

        syncFlightPlans(loadedFlightPlans);
        if (!flightPlans.isEmpty()) {
            currentFlightPlan.set(flightPlans.get(0));
        }
    }

    private void updateMissionSettingsFile(@NotNull MissionInfo missionInfo) {
        Path missionFolder = directory.get();
        missionInfo.setFolder(missionFolder.toFile());
        missionInfo.setName(getName());
        missionInfo.setLastModified(getLastModified());
        missionInfo.setStartCoordinates(startCoordinates.get());
        if (getSector() != null) {
            missionInfo.setMinLatitude(getSector().getMinLatitude().degrees);
            missionInfo.setMaxLatitude(getSector().getMaxLatitude().degrees);
            missionInfo.setMinLongitude(getSector().getMinLongitude().degrees);
            missionInfo.setMaxLongitude(getSector().getMaxLongitude().degrees);
        }

        missionInfo.setMaxElev(getMaxElev());
        missionInfo.setMinElev(getMinElev());

        if (srs.get() != null) {
            missionInfo.setSrsId(srs.get().id);
            missionInfo.setSrsName(srs.get().name);
            missionInfo.setSrsWkt(srs.get().wkt);
            missionInfo.setSrsOrigin(srs.get().getOrigin().toString());
        }

        missionInfo.setLoadedFlightPlans(
            flightPlans
                .stream()
                .map(
                    flightPlan ->
                        FileHelper.makeRelativePathSysIndep(getFlightplanFolder(), flightPlan.getResourceFile()))
                .collect(Collectors.toList()));

        missionInfo.setLoadedDataSets(
            matchings
                .stream()
                .filter(matching -> matching.getStatus() != MatchingStatus.NEW)
                .map(matching -> FileHelper.makeRelativePathSysIndep(getMatchingFolder(), matching.getMatchingFolder()))
                .collect(Collectors.toList()));

        missionInfo.setFlightLogs(
            flightLogs
                .stream()
                .map(file -> FileHelper.makeRelativePathSysIndep(getFlightLogsFolder(), file))
                .collect(Collectors.toList()));
        missionSettingsManager.saveToFile(missionInfo);
    }

    public boolean save() {
        return save(true);
    }

    public boolean save(boolean checkDemo) {
        lastModified.set(new Date());
        updateMissionSettingsFile(missionInfo);
        if (checkDemo) {
            return checkAndRenameDemo();
        }

        return true;
    }

    public boolean checkAndRenameDemo() {
        if (getName().equals(Mission.DEMO_MISSION_NAME)) {
            return applicationContext != null && applicationContext.renameCurrentMission();
        }

        return true;
    }

    public ReadOnlyStringProperty nameProperty() {
        return this.name;
    }

    public ReadOnlyObjectProperty<Drone> droneProperty() {
        return uav;
    }

    public ReadOnlyListProperty<FlightPlan> flightPlansProperty() {
        ensureLoaded();
        return this.flightPlans;
    }

    public ReadOnlyListProperty<FlightPlanTemplate> flightPlanTemplatesProperty() {
        return this.flightPlanTemplates;
    }

    public @Nullable FlightPlan getFlightPlanForLegacy(Flightplan legacyFlightPlan) {
        if (legacyFlightPlan == null) {
            return null;
        }

        ObservableList<FlightPlan> plans = flightPlansProperty().get();
        if (plans == null || plans.isEmpty()) {
            return null;
        }

        for (FlightPlan flightPlan : plans) {
            if (flightPlan.getLegacyFlightplan() == legacyFlightPlan) {
                return flightPlan;
            }
        }

        return null;
    }

    public void addFlightPlan(FlightPlan flightPlan, boolean needsRecalculate) {
        // LEGACY BRIDGE TODO refactor ?
        if (needsRecalculate) {
            recomputeFPOnAdding(flightPlan.getLegacyFlightplan());
        }

        ensureLoaded();
        addFlightPlanIfExists(flightPlan);
    }

    public void addFlightPlanTemplate(FlightPlanTemplate flightPlanTemplate) {
        if (!flightPlanTemplates.contains(flightPlanTemplate)) {
            flightPlanTemplates.add(flightPlanTemplate);
        }
    }

    public ObjectProperty<FlightPlan> currentFlightPlanProperty() {
        return currentFlightPlan;
    }

    public FlightPlan getCurrentFlightPlan() {
        return currentFlightPlan.get();
    }

    public void setCurrentFlightPlan(FlightPlan flightPlan) {
        currentFlightPlan.set(flightPlan);
    }

    private void addFlightPlanIfExists(FlightPlan flightPlan) {
        if (flightPlan == null) {
            return;
        }

        if (!flightPlan.isTemplate()) {
            this.flightPlans.add(flightPlan);
        }
    }

    public void initNewFlightPlan(FlightPlan flightPlan) {
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String fileName = dateFormat.format(today);

        mavinciObjectFactory.initFlightPlan(
            flightPlan.getLegacyFlightplan(), fileName, MissionConstants.getFlightplanFolder(getDirectory()));
    }

    public @Nullable FlightPlan getFirstFlightPlan() {
        return flightPlansProperty().isEmpty() ? null : flightPlansProperty().iterator().next();
    }

    public void setDirectory(Path directory) {
        this.directory.set(directory);
    }

    public ObjectProperty<FlightPlanTemplate> currentFlightPlanTemplateProperty() {
        return currentFlightPlanTemplate;
    }

    public String getName() {
        return this.name.get();
    }

    public Path getDirectory() {
        return this.directory.get();
    }

    public File getDirectoryFile() {
        return this.directory.get().toFile();
    }

    public Date getLastModified() {
        return this.lastModified.get();
    }

    public Sector getSector() {
        if (!isLoaded) {
            return earlySector;
        }

        List<Sector> fpSectors =
            flightPlans.stream().map(FlightPlan::getSector).filter(Objects::nonNull).collect(Collectors.toList());

        List<Sector> matchingSectors =
            matchings
                .stream()
                .filter(matching -> matching.getStatus() != MatchingStatus.NEW)
                .map(Matching::getSector)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        fpSectors.addAll(matchingSectors);

        if (fpSectors.isEmpty()) {
            return null;
        }

        return Sector.union(fpSectors);
    }

    @Override
    public OptionalDouble getMaxElev() {
        MinMaxPair minMaxPair = new MinMaxPair();
        for (FlightPlan fp : flightPlans) {
            minMaxPair.update(fp.getMaxElev());
        }

        for (Matching matching : matchings) {
            minMaxPair.update(matching.getMaxElev());
        }

        // should be undefined
        if (Double.isInfinite(minMaxPair.max)) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(minMaxPair.max);
    }

    @Override
    public OptionalDouble getMinElev() {
        MinMaxPair minMaxPair = new MinMaxPair();
        for (FlightPlan fp : flightPlans) {
            minMaxPair.update(fp.getMaxElev());
        }

        for (Matching matching : matchings) {
            minMaxPair.update(matching.getMaxElev());
        }
        // should be undefined
        if (Double.isInfinite(minMaxPair.min)) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(minMaxPair.min);
    }

    public void load() {
        ensureLoaded();
    }

    public void recalculateCurrentFlightPlan() {
        FlightPlan flightPlan = currentFlightPlan.get();
        if (flightPlan != null) {
            flightPlan.doFlightplanCalculation();
        }
    }

    // Loads the potentially computationally- or memory-intensive parts of the mission.
    // This is used to employ a lazy-loading scheme for the Mission class.
    //
    private synchronized void ensureLoaded() {
        if (this.isLoaded) {
            return;
        }

        this.isLoaded = true;

        // Use the legacy session class to read the missions.
        // We also listen for change notifications of the legacy mission class
        // to reflect those changes in the properties of the Mission class.
        //
        if (!flightPlans.isEmpty()) {
            currentFlightPlan.set(flightPlans.get(0));
        }

        matchings.addListener(this::onMatchingListChanged);
        onMatchingListChanged(null);
    }

    // If the list of matchings is empty, we create a new matching instance that is only available as the
    // currentMatching property, not in the list of matchings.
    private void onMatchingListChanged(ListChangeListener.Change<? extends Matching> change) {
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                // try add default matching
                if (matchings.isEmpty() && currentMatching.get() == null) {
                    Matching matching =
                        new Matching(
                                getDirectoryFile().toPath(),
                            hardwareConfigurationManager);
                    matchings.add(matching);
                    currentMatching.set(matching);
                } else if (matchings.get() == null && !matchings.isEmpty()) {
                    currentMatching.set(matchings.get(0));
                }
            });
    }

    private void syncFlightPlans(List<Flightplan> loadedFlightPlans) {
        for (Flightplan legacyFlightPlan : loadedFlightPlans) {
            FlightPlan flightPlan = createNewFlightPlan(legacyFlightPlan);
            addFlightPlanIfExists(flightPlan);
        }
    }

    private IAirplane createNewLegacyPlane() {
        return new Airplane(directory, startCoordinates);
    }

    public IAirplane getLegacyPlane() {
        Drone uav = droneProperty().get();
        if (uav == null) {
            return null;
        }

        return uav.getLegacyPlane();
    }

    public ObservableList<Matching> getMatchings() {
        return matchings.get();
    }

    public ListProperty<Matching> matchingsProperty() {
        return matchings;
    }

    public ListProperty<File> flightLogsProperty() {
        return flightLogs;
    }

    public FlightPlan loadFlightPlan(Path flightplanFile) throws InvalidFlightPlanFileException {
        Flightplan legacyFlightPlan = new Flightplan(flightplanFile.toFile());
        return createNewFlightPlan(legacyFlightPlan);
    }

    private FlightPlan createNewFlightPlan(Flightplan legacyFlightPlan) {
        FlightPlan flightPlan = new FlightPlan(legacyFlightPlan, false);
        flightPlan.isNameSetProperty().setValue(true);
        return flightPlan;
    }

    public void closeFlightPlan(FlightPlan flightPlan) {
        flightPlans.remove(flightPlan);
        updateMissionSettingsFile(missionInfo);
    }

    public void closeFlightPlanTemplate(FlightPlanTemplate template) {
        flightPlanTemplates.remove(template);
    }

    public List<File> getPlaneConfigs() {
        File planeConfigFolder = MissionConstants.getPlaneConfigFolder(getDirectory());
        if (!Files.exists(planeConfigFolder.toPath())) {
            return new ArrayList<>();
        }

        return Arrays.asList(planeConfigFolder.listFiles(MFileFilter.configFilter.getWithoutFolders()));
    }

    public List<File> getKMLs() {
        File kmlFolder = MissionConstants.getKMLFolder(getDirectory());
        if (!Files.exists(kmlFolder.toPath())) {
            return new ArrayList<>();
        }

        return Arrays.asList(kmlFolder.listFiles(MFileFilter.kmlFilter.getWithoutFolders()));
    }

    public List<File> getImcFlightLogFiles() {
        File logsFolder = MissionConstants.getLogFolder(getDirectory());
        if (!logsFolder.exists()) {
            return new ArrayList<>();
        }

        return Arrays.asList(
            logsFolder.listFiles(
                (dir, name1) ->
                    (name1.endsWith(".flg")
                        || name1.endsWith(".vlg")
                        || name1.endsWith(".flg.zip")
                        || name1.endsWith(".vlg.zip")
                        || name1.endsWith(".bbx")
                        || name1.endsWith(".bbx.zip")
                        || name1.endsWith(".alg"))));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Mission mission = (Mission)o;
        return Objects.equals(name, mission.name) && Objects.equals(lastModified.get(), mission.lastModified.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.get(), lastModified.get());
    }

    @Override
    public int compareTo(Mission m) {
        return compareLastModified(m) != 0 ? compareLastModified(m) : compareName(m);
    }

    private int compareLastModified(Mission m) {
        return Long.compare(getLastModified().getTime(), m.getLastModified().getTime());
    }

    private int compareName(Mission m) {
        return getName().compareTo(m.getName());
    }

    public BooleanProperty missionEmptyProperty() {
        return missionEmpty;
    }

    public void setMissionEmpty(boolean missionEmpty) {
        this.missionEmpty.set(missionEmpty);
    }

    public boolean isMissionEmpty() {
        return missionEmpty.get();
    }

    public ReadOnlyBooleanProperty containsTemplatesProperty() {
        return containsTemplatesBindings;
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean hasUnsavedItems() {
        return hasUnsavedDataSets() || hasUnsavedFlightPlans();
    }

    public boolean hasUnsavedFlightPlans() {
        return getUnsavedFlightPlans().size() != 0;
    }

    public boolean hasUnsavedDataSets() {
        return getUnsavedDataSets().size() != 0;
    }

    public List<ISaveable> getUnsavedFlightPlans() {
        return getUnsavedItemsFromStream(flightPlansProperty().stream());
    }

    public List<ISaveable> getUnsavedDataSets() {
        return getUnsavedItemsFromStream(getMatchings().stream());
    }

    public List<ISaveable> getAllUnsavedItems() {
        List<ISaveable> items = new ArrayList<ISaveable>(getUnsavedDataSets());
        items.addAll(getUnsavedFlightPlans());
        return items;
    }

    private List<ISaveable> getUnsavedItemsFromStream(Stream<? extends ISaveable> stream) {
        return stream.filter(ISaveable::canBeSaved).filter(ISaveable::hasUnsavedChanges).collect(Collectors.toList());
    }

    // legacy piece from FPManager
    private void recomputeFPOnAdding(CFlightplan fp) {
        ExtractPicAreasVisitor vis = new ExtractPicAreasVisitor();
        vis.startVisit(fp);
        for (CPicArea cPicArea : vis.picAreas) {
            if (cPicArea instanceof PicArea) {
                PicArea picArea = (PicArea)cPicArea;
                picArea.computeFlightLines(true);
            }
        }
    }

    public ObjectProperty<Matching> currentMatchingProperty() {
        return currentMatching;
    }

    public Matching getCurrentMatching() {
        return currentMatching.get();
    }

    public void setCurrentMatching(Matching matching) {
        currentMatching.set(matching);
    }

    public MissionInfo getMissionInfo() {
        return missionInfo;
    }

    public void addFlightLog(File file) {
        flightLogs.add(file);

        // TODO do we need to update the date in case of copying the logs into the folder, what if
        Date logLastModified = new Date(file.lastModified());
        Date now = new Date();
        // second check is just for sanity
        if (logLastModified.after(lastModified.get()) && logLastModified.before(now)) {
            lastModified.set(logLastModified);
        }
    }

    public boolean setMatching(Matching updatedMatching, File matchingFolder) {
        for (Matching matching : matchings) {
            if (matching.getMatchingFolder() != null && matching.getMatchingFolder().equals(matchingFolder)) {
                matchings.remove(matching);
                matchings.add(updatedMatching);
                return true;
            }
        }

        getMatchings().add(updatedMatching);
        return false;
    }

    public void removeMatching(Matching updatedMatching, File matchingFolder) {
        for (Matching matching : matchings) {
            if (matching.getMatchingFolder() != null && matching.getMatchingFolder().equals(matchingFolder)) {
                if (!matching.equals(updatedMatching)) {
                    matchings.remove(matching);
                }
            }
        }

        return;
    }
}
