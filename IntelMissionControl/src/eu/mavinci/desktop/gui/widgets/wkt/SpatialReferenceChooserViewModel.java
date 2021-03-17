package eu.mavinci.desktop.gui.widgets.wkt;
/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import eu.mavinci.desktop.helper.FileFilter;
import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.TreeSet;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpatialReferenceChooserViewModel extends DialogViewModel<Boolean, Property<MSpatialReference>> {

    public static final String KEY = "eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserViewModel";
    public static final String GEO_SRS = SpatialReference.GEO_SRS;
    public static final String PROJ_SRS = SpatialReference.PROJ_SRS;
    public static final String PRIV_SRS = SpatialReference.PRIV_SRS;

    private final IApplicationContext applicationContext;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final ISrsManager srsManager;
    private final IWWGlobes globes;
    private final IBackgroundTaskManager backgroundTaskManager;
    private final ISettingsManager settingsManager;

    private final ObservableList<SpatialReference> srsItems = FXCollections.observableArrayList();
    private final ObjectProperty<SpatialReference> selectedSrs = new SimpleObjectProperty<>();
    private final StringProperty filter = new SimpleStringProperty("");

    private ICommand proceedCommand;
    private ICommand deleteCommand;
    private ICommand addNewCommand;
    private Property<MSpatialReference> srsRef;
    private static Logger LOGGER = LoggerFactory.getLogger(eu.mavinci.desktop.gui.widgets.wkt.SpatialReference.class);
    private final BooleanProperty isDeleteable = new SimpleBooleanProperty();;
    private final BooleanProperty isSelectable = new SimpleBooleanProperty();;

    @Inject
    public SpatialReferenceChooserViewModel(
            IApplicationContext applicationContext,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            IBackgroundTaskManager backgroundTaskManager,
            ISettingsManager settingsManager,
            ISrsManager srsManager,
            IWWGlobes globes) {
        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.backgroundTaskManager = backgroundTaskManager;
        this.settingsManager = settingsManager;
        this.srsManager = srsManager;
        this.globes = globes;
    }

    @Override
    protected void initializeViewModel(Property<MSpatialReference> srsRef) {
        if (srsRef.getValue() == null) {
            srsRef.setValue(getSrsDefault());
        }

        super.initializeViewModel(srsRef);
        this.srsRef = srsRef;
        this.selectedSrs.set(new SpatialReference(srsRef.getValue()));

        proceedCommand = new DelegateCommand(this::setSelected, isSelectable);
        isSelectable.bind(
            Bindings.createBooleanBinding(
                () ->
                    !(selectedSrs == null
                        || selectedSrs.get() == null
                        || selectedSrs.get().isGroup()
                        || !selectedSrs.get().isGeoidOK()),
                selectedSrs));
        deleteCommand = new DelegateCommand(this::deleteSelected, isDeleteable);
        isDeleteable.bind(
            Bindings.createBooleanBinding(
                () ->
                    !(selectedSrs == null
                        || selectedSrs.get() == null
                        || selectedSrs.get().isGroup()
                        || !selectedSrs.get().categoryProperty().getValue().equals(PRIV_SRS)),
                selectedSrs));

        addNewCommand = new DelegateCommand(this::addNew);
        updateSrsItems();
    }

    private void addNew() {
        Path file =
            dialogService.requestFileOpenDialog(
                this, languageHelper.getString(KEY + ".fileSelector.title"), null, FileFilter.WKT_PRJ, FileFilter.ALL);
        if (file == null || !Files.exists(file)) {
            return;
        }

        try {
            srsRef.setValue(MSpatialReference.getSpatialReferenceFromFile(globes.getDefaultGlobe(), srsManager, file));
            updateSrsItems();
        } catch (Exception e) {
            LOGGER.warn("Unable to generate SpatialReference from file", e);
            applicationContext.addToast(
                Toast.of(ToastType.ALERT)
                    .setText("Unable to generate SpatialReference from file: " + e.getMessage())
                    .setCloseable(true)
                    .create());
        }
    }

    public ObservableList<SpatialReference> getSrsItems() {
        return srsItems;
    }

    public ObjectProperty<SpatialReference> selectedSrsProperty() {
        return selectedSrs;
    }

    private void setSelected() {
        SpatialReference ref = selectedSrs.get();
        MSpatialReference mRef = srsManager.getSrsByIdOrDefault(ref.getId());
        srsRef.setValue(mRef);
        setDialogResult(true);
        getCloseCommand().execute();
    }

    public ICommand getProceedCommand() {
        return proceedCommand;
    }

    public StringProperty filterProperty() {
        return filter;
    }

    public void updateSrsItems() {
        srsItems.clear();
        Collection<TreeSet<MSpatialReference>> rsMSpatialReference = srsManager.getReferencesSorted().values();
        Ensure.notNull(rsMSpatialReference, "rsMSpatialReference");
        for (TreeSet<MSpatialReference> srsT : rsMSpatialReference) {
            for (MSpatialReference srs : srsT) {
                if (srs.toString().toLowerCase().contains(filter.get().toLowerCase())) {
                    SpatialReference item = new SpatialReference(srs);
                    String category = srs.isPrivate() ? PRIV_SRS : srs.isGeographic() ? GEO_SRS : srs.getCathegory();
                    item.categoryProperty().setValue(category);
                    srsItems.add(item);
                    if (srs.equals(srsRef.getValue())) {
                        selectedSrs.set(item);
                    }
                }
            }
        }

        if (srsItems.size() == 1) {
            selectedSrs.set(srsItems.get(0));
        }
    }

    public ICommand getDeleteCommand() {
        return deleteCommand;
    }

    private void deleteSelected() {
        if (selectedSrs.get() == null) {
            return;
        }

        MSpatialReference selectedMRef = srsManager.getReferences().get(selectedSrs.get().getId());
        if (selectedMRef != null) {
            if (!selectedMRef.isPrivate()) {
                return;
            }

            // reset to default if the current one is deleted
            if (selectedMRef.id.equals(srsRef.getValue().id)) {
                srsRef.setValue(srsManager.getDefault());
            }

            srsManager.delete(selectedMRef.id);
        }

        updateSrsItems();
    }

    public ICommand getAddNewCommand() {
        return addNewCommand;
    }

    public MSpatialReference getSrsDefault() {
        return srsManager.getSrsByIdOrDefault(null);
    }
}
