/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.geometry.AreaOfInterestCorner;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.flightplan.Point;
import eu.mavinci.flightplan.PointWithAltitudes;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
public class EditPowerpolePointsViewModel extends DialogViewModel<Void, AreaOfInterest> {

    public boolean isBulkEditable() {
        return bulkEditable.get();
    }

    public BooleanProperty bulkEditableProperty() {
        return bulkEditable;
    }

    public boolean isOptimizeWayPoints() {
        return optimizeWayPoints.get();
    }

    public BooleanProperty optimizeWayPointsProperty() {
        return optimizeWayPoints;
    }

    public enum SelectionFilter {
        ANY,
        NO_IMAGE
    }

    private final IApplicationContext applicationContext;
    private final IDialogService dialogService;
    private final ILanguageHelper languageHelper;
    private final ISelectionManager selectionManager;
    private final GeneralSettings generalSettings;
    private final ISettingsManager settingsManager;
    private final BooleanProperty bulkEditable = new SimpleBooleanProperty(true);
    private final ObjectProperty<AreaOfInterestCorner> selection = new SimpleObjectProperty<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(EditPowerpolePointsViewModel.class);
    private final BooleanProperty canShowBulkEditDialog = new SimpleBooleanProperty(true);

    private Command addCornerCommand;

    private ListProperty<AreaOfInterestCorner> areaOfInterestCorners;
    private AreaOfInterest areaOfInterest;

    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    private final Quantity<Dimension.Angle> minPitch = Quantity.of(-360, Unit.DEGREE);
    private final Quantity<Dimension.Angle> maxPitch = Quantity.of(360, Unit.DEGREE);

    private final ObjectProperty<AreaOfInterestCorner> selectedAreaOfInterestCorner = new SimpleObjectProperty<>();
    private ParameterizedCommand<AreaOfInterestCorner> deleteSelectedAreaOfInterestCornersCommand;
    private ParameterizedCommand<AreaOfInterestCorner> duplicateSelectedCommand;

    private ParameterizedCommand<AreaOfInterestCorner> MoveDownCommand;
    private ParameterizedCommand<AreaOfInterestCorner> MoveUpCommand;

    private ParameterizedCommand<AreaOfInterest> openBulkEditingDialogCommand;

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private final ParameterizedCommand<EditPowerpolePointsViewModel.SelectionFilter> selectCommand;
    private final ParameterizedCommand<EditPowerpolePointsViewModel.SelectionFilter> deselectCommand;
    private final Command invertSelectionCommand;

    private final BooleanProperty optimizeWayPoints = new SimpleBooleanProperty(false);

    public ParameterizedCommand<EditPowerpolePointsViewModel.SelectionFilter> getSelectCommand() {
        return selectCommand;
    }

    public ParameterizedCommand<EditPowerpolePointsViewModel.SelectionFilter> getDeselectCommand() {
        return deselectCommand;
    }

    public Command getInvertSelectionCommand() {
        return invertSelectionCommand;
    }

    @Inject
    public EditPowerpolePointsViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            IDialogService dialogService,
            ILanguageHelper languageHelper,
            ISelectionManager selectionManager) {
        this.applicationContext = applicationContext;
        this.dialogService = dialogService;
        this.languageHelper = languageHelper;
        this.selectionManager = selectionManager;
        this.settingsManager = settingsManager;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);

        bulkEditable.bind(canShowBulkEditDialog);

        selectCommand = new ParameterizedDelegateCommand<>(this::selectAreaOfInterestCorner);
        deselectCommand = new ParameterizedDelegateCommand<>(this::deselectAreaOfInterestCorner);
        invertSelectionCommand = new DelegateCommand(this::invertSelection);

        deleteSelectedAreaOfInterestCornersCommand =
            new ParameterizedDelegateCommand<>(this::deleteSelectedAreaOfInterestCorners);
        selectionManager
            .currentSelectionProperty()
            .addListener((observable, oldValue, newValue) -> selectionChange(newValue), Dispatcher.platform());

        this.duplicateSelectedCommand = new ParameterizedDelegateCommand<>(this::duplicateSelected);
        this.MoveDownCommand = new ParameterizedDelegateCommand<>(this::moveDown);
        this.MoveUpCommand = new ParameterizedDelegateCommand<>(this::moveUp);

        this.openBulkEditingDialogCommand =
            new ParameterizedDelegateCommand<AreaOfInterest>(
                (aoi) -> {
                    canShowBulkEditDialog.set(false);
                    Futures.addCallback(
                        dialogService.requestDialogAsync(
                            this, InspectionPointBulkSettingsViewModel.class, () -> aoi, false),
                        new FutureCallback<>() {
                            @Override
                            public void onSuccess(InspectionPointBulkSettingsViewModel vm) {
                                canShowBulkEditDialog.set(true);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                canShowBulkEditDialog.set(true);
                            }

                        });
                },
                canShowBulkEditDialog);

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        getCloseCommand().execute();
                    }
                });
    }

    private void moveUp(AreaOfInterestCorner areaOfInterestCorner) {
        var i = this.areaOfInterestCorners.indexOf(areaOfInterestCorner);
        if (i == 0) {
            // can not move up entry if it's already the first
            return;
        }

        this.areaOfInterest.dragVertex(i, i - 1);
    }

    private void moveDown(AreaOfInterestCorner areaOfInterestCorner) {
        var i = this.areaOfInterestCorners.indexOf(areaOfInterestCorner);
        if (i == this.areaOfInterestCorners.size() - 1) {
            // can not move down entry if it's already the last
            return;
        }

        this.areaOfInterest.dragVertex(i, i + 1);
    }

    private void duplicateSelected(AreaOfInterestCorner areaOfInterestCorner) {
        for (var corner : areaOfInterestCorners) {
            if (!corner.isSelected()) {
                continue;
            }

            Point p = new Point(corner.getLegacyPoint().getPoint());
            try {
                areaOfInterest.getPicArea().getCorners().addToFlightplanContainer(p);
            } catch (Exception e) {
                LOGGER.error("cant add point", e);
            }
        }
    }

    private void deleteSelectedAreaOfInterestCorners(AreaOfInterestCorner corner) {
        corner.deleteMe();
    }

    void addCorner() {
        Point p = new Point(areaOfInterest.getPicArea().getCorners());
        try {
            areaOfInterest.getPicArea().getCorners().addToFlightplanContainer(p);
            selectionManager.setSelection(areaOfInterestCorners.get(areaOfInterestCorners.size() - 1));
        } catch (Exception e) {
            LOGGER.error("cant add point", e);
        }
    }

    public boolean isEditable() {
        return editable.get();
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public Quantity<Dimension.Angle> getMinPitch() {
        return minPitch;
    }

    public Quantity<Dimension.Angle> getMaxPitch() {
        return maxPitch;
    }

    public ObservableList<AreaOfInterestCorner> getAreaOfInterestCorners() {
        return areaOfInterestCorners.get();
    }

    public AreaOfInterest getAreaOfInterest() {
        return this.areaOfInterest;
    }

    public ListProperty<AreaOfInterestCorner> areaOfInterestCornersProperty() {
        return areaOfInterestCorners;
    }

    public AreaOfInterestCorner getSelectedAreaOfInterestCorner() {
        return selectedAreaOfInterestCorner.get();
    }

    public ObjectProperty<AreaOfInterestCorner> selectedAreaOfInterestCornerProperty() {
        return selectedAreaOfInterestCorner;
    }

    public void dragVertex(int draggedIndex, int dropIndex) {
        // TODO: implement
    }

    public Command getAddCornerCommand() {
        return addCornerCommand;
    }

    public void setAddCornerCommand(Command addCornerCommand) {
        this.addCornerCommand = addCornerCommand;
    }

    public ParameterizedCommand<AreaOfInterestCorner> getDeleteSelectedAreaOfInterestCornersCommand() {
        return deleteSelectedAreaOfInterestCornersCommand;
    }

    public void setDeleteSelectedAreaOfInterestCornersCommand(
            ParameterizedCommand deleteSelectedAreaOfInterestCornersCommand) {
        this.deleteSelectedAreaOfInterestCornersCommand = deleteSelectedAreaOfInterestCornersCommand;
    }

    @Override
    protected void initializeViewModel(AreaOfInterest areaOfInterest) {
        super.initializeViewModel(areaOfInterest);

        addCornerCommand = new DelegateCommand(this::addCorner);
        this.areaOfInterest = areaOfInterest;

        this.optimizeWayPoints.bindBidirectional(areaOfInterest.optimizeWayPointsProperty());

        areaOfInterestCorners = areaOfInterest.cornerListProperty();
        this.areaOfInterest
            .cornerListProperty()
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        selectionChange(selectionManager.getSelection());
                    }
                });
        selection.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectionManager.setSelection(newValue.getLegacyPoint().getPoint());
                }
            });
    }

    public void importCSV() {
        Path selectedFile = dialogService.requestFileOpenDialog(this, "Import CSV");

        try (BufferedReader br = new BufferedReader(new FileReader(selectedFile.toFile()))) {
            String line;
            int totalCount = 0;
            double totalAlt = 0;
            int i = 1;
            List<AreaOfInterestCorner> corners = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                try {
                    line = line.trim();
                    if (line.startsWith("#") || line.isEmpty()) continue;

                    String[] parts = line.split(Pattern.quote(";"));
                    if (parts == null || parts.length < 2 || parts[0].trim().isEmpty()) continue;
                    // String name = parts[0] + " " + parts[1];
                    double lat = Double.parseDouble(parts[2].replace(",", "."));
                    double lon = Double.parseDouble(parts[1].replace(",", "."));
                    // double heightOverMsl = Double.parseDouble(parts[4].replace(",", "."));

                    // what is the factor 0.3048 in here?
                    double heightOverTerrain = Double.parseDouble(parts[3].replace(",", "."));
                    double heading = Double.parseDouble(parts[4].replace(",", "."));
                    double pitch = Double.parseDouble(parts[5].replace(",", ".")) + 90;

                    Point p = new Point(areaOfInterest.getPicArea().getCorners());
                    p.setAltitude(heightOverTerrain);
                    p.setLatLon(lat, lon);
                    p.setYaw(heading);
                    p.setPitch(pitch);
                    PointWithAltitudes point = new PointWithAltitudes(p);

                    corners.add(
                        new AreaOfInterestCorner(
                            i,
                            point,
                            settingsManager.getSection(GeneralSettings.class),
                            areaOfInterest.getPicArea(),
                            settingsManager));

                    i++;

                    // areaOfInterestCorners
                    // double terrainHeightInput = heightOverTerrain - heightOverMsl;
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            //            areaOfInterestCorners
            //                    .forEach((areaOfInterestCorner -> areaOfInterestCorner.removeFPListeners()));
            //
            //            Point p = areaOfInterest.getPicArea().getCorners().getLastElement();
            //            while(p != null ) {
            //                if(areaOfInterest.getPicArea().removeFromFlightplanContainer(p) == null) {
            //                    break;
            //                }
            //                p = areaOfInterest.getPicArea().getCorners().getLastElement();
            //            }
            areaOfInterest.getPicArea().getCorners().setMute(true);
            corners.forEach(
                corner -> {
                    try {
                        areaOfInterest
                            .getPicArea()
                            .getCorners()
                            .addToFlightplanContainer(corner.getLegacyPoint().getPoint());
                    } catch (FlightplanContainerFullException e) {
                        e.printStackTrace();
                    } catch (FlightplanContainerWrongAddingException e) {
                        e.printStackTrace();
                    }
                });
            areaOfInterest.getPicArea().getCorners().setMute(false);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //        selectionManager.setSelection(areaOfInterestCorners.get(areaOfInterestCorners.size() - 1));

    }

    public ParameterizedCommand<AreaOfInterestCorner> getDuplicateSelectedCommand() {
        return duplicateSelectedCommand;
    }

    public void setDuplicateSelectedCommand(ParameterizedCommand<AreaOfInterestCorner> duplicateSelectedCommand) {
        this.duplicateSelectedCommand = duplicateSelectedCommand;
    }

    public AreaOfInterestCorner getSelection() {
        return selection.get();
    }

    public ObjectProperty<AreaOfInterestCorner> selectionProperty() {
        return selection;
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

    public ParameterizedCommand<AreaOfInterestCorner> getMoveDownCommand() {
        return MoveDownCommand;
    }

    public void setMoveDownCommand(ParameterizedCommand<AreaOfInterestCorner> moveDownCommand) {
        this.MoveDownCommand = moveDownCommand;
    }

    public ParameterizedCommand<AreaOfInterestCorner> getMoveUpCommand() {
        return MoveUpCommand;
    }

    public void setMoveUpCommand(ParameterizedCommand<AreaOfInterestCorner> moveUpCommand) {
        this.MoveUpCommand = moveUpCommand;
    }

    public ParameterizedCommand<AreaOfInterest> getOpenBulkEditingDialogCommand() {
        return openBulkEditingDialogCommand;
    }

    private void selectAreaOfInterestCorner(EditPowerpolePointsViewModel.SelectionFilter filter) {
        areaOfInterest.getPicArea().setMute(true);
        switch (filter) {
        case ANY:
            for (var wp : areaOfInterestCorners) {
                wp.setSelected(true);
            }

            break;
        case NO_IMAGE:
            for (var wp : areaOfInterestCorners) {
                if (!wp.triggerImageProperty().get()) {
                    wp.setSelected(true);
                }
            }

            break;
        }

        areaOfInterest.getPicArea().setMute(false);
    }

    private void deselectAreaOfInterestCorner(EditPowerpolePointsViewModel.SelectionFilter filter) {
        areaOfInterest.getPicArea().setMute(true);
        switch (filter) {
        case ANY:
            for (AreaOfInterestCorner wp : areaOfInterestCorners) {
                wp.setSelected(false);
            }

            break;
        case NO_IMAGE:
            for (AreaOfInterestCorner wp : areaOfInterestCorners) {
                if (!wp.triggerImageProperty().get()) {
                    wp.setSelected(false);
                }
            }

            break;
        }

        areaOfInterest.getPicArea().setMute(false);
    }

    private void invertSelection() {
        areaOfInterest.getPicArea().setMute(true);
        for (AreaOfInterestCorner wp : areaOfInterestCorners) {
            wp.setSelected(!wp.isSelected());
        }

        areaOfInterest.getPicArea().setMute(false);
    }

}
