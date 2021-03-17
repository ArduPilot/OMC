/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.flightplantemplate.AreasOfInterestType;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.IntelMenuItem;
import com.intel.missioncontrol.ui.common.BindingUtils;
import com.intel.missioncontrol.ui.common.SectionMenuItemsBuilder;
import com.intel.missioncontrol.ui.common.components.TitledForm;
import com.intel.missioncontrol.ui.common.hardware.HardwareSelectionView;
import com.intel.missioncontrol.ui.common.hardware.HardwareSelectionViewModel;
import com.intel.missioncontrol.ui.controls.MenuButton;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import com.intel.missioncontrol.ui.sidepane.planning.aoi.AoiEditComponent;
import com.intel.missioncontrol.ui.sidepane.planning.emergency.EmergencyActionsView;
import com.intel.missioncontrol.ui.sidepane.planning.emergency.EmergencyActionsViewModel;
import com.intel.missioncontrol.ui.sidepane.planning.landing.LandingView;
import com.intel.missioncontrol.ui.sidepane.planning.landing.LandingViewModel;
import com.intel.missioncontrol.ui.sidepane.planning.settings.GeneralSettingsSectionView;
import com.intel.missioncontrol.ui.sidepane.planning.settings.GeneralSettingsSectionViewModel;
import com.intel.missioncontrol.ui.sidepane.planning.starting.StartingView;
import com.intel.missioncontrol.ui.sidepane.planning.starting.StartingViewModel;
import com.intel.missioncontrol.ui.sidepane.planning.summary.FlightplanSummaryView;
import com.intel.missioncontrol.ui.sidepane.planning.summary.FlightplanSummaryViewModel;
import com.intel.missioncontrol.ui.sidepane.planning.summary.WarningsView;
import com.intel.missioncontrol.ui.sidepane.planning.summary.WarningsViewModel;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommandInvocation;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.desktop.gui.doublepanel.planemain.ActionManager;
import eu.mavinci.flightplan.PicArea;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.concurrent.Dispatcher;

public class EditFlightplanView extends FancyTabView<EditFlightplanViewModel> {

    private static final String CSS_HIGLIGHT_TITLED_PANE = "selected";
    private static final String AOI_SECTION_VIEW = "aoiSectionView.aoiString";
    private static final String CSS_TEMPLATE_EDIT_CONTENT = "templateEditContent";

    @InjectViewModel
    private EditFlightplanViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Label projectNameLabel;

    @FXML
    private MenuButton flightPlansMenuButton;

    @FXML
    private Node editArea;

    @FXML
    private MenuButton addAreaOfInterestBtn;

    @FXML
    private VBox formsContainer;

    @FXML
    private ScrollPane editFlightplanView;

    @FXML
    private ScrollPane chooseAoiView;

    @FXML
    private Button showOnMapButton;

    @FXML
    private SplitMenuButton saveBtn;

    @FXML
    private Button saveTemplate;

    @FXML
    private MenuItem saveFpButtonMenuItem;

    @FXML
    private MenuItem exportFpButtonMenuItem;

    @FXML
    private Button saveFpAndProceedButtonMenuItem;

    @FXML
    private Button exportButton;

    @FXML
    private HBox footer;

    private final IDialogContextProvider dialogContextProvider;
    private final ILanguageHelper languageHelper;
    private final IQuantityStyleProvider quantityStyleProvider;
    private final IApplicationContext applicationContext;
    private final IMapView mapView;
    private final List<TitledForm> forms = new ArrayList<>();
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Inject
    public EditFlightplanView(
            IQuantityStyleProvider quantityStyleProvider,
            ILanguageHelper languageHelper,
            IApplicationContext applicationContext,
            IDialogContextProvider dialogContextProvider,
            IMapView mapView,
            NotificationCenter notificationCenter) {
        this.quantityStyleProvider = quantityStyleProvider;
        this.languageHelper = languageHelper;
        this.applicationContext = applicationContext;
        this.dialogContextProvider = dialogContextProvider;
        this.mapView = mapView;

        notificationCenter.subscribe(
            ActionManager.MOUSE_MOVE_CHANGE_EVENT,
            (key, payload) -> {
                Optional.of(payload)
                    .filter(objects -> objects.length == 1)
                    .map(objects -> (InputMode)objects[0])
                    .ifPresent(
                        newMouseMode -> {
                            Dispatcher dispatcher = Dispatcher.platform();
                            dispatcher.runLater(
                                () -> {
                                    if (newMouseMode == InputMode.DEFAULT) {
                                        Optional<TitledForm> form = findFirstAoiSectionInEditState();
                                        if (form.isPresent()) {
                                            AreaOfInterest areaOfInterest = (AreaOfInterest)form.get().getUserData();
                                            PicArea picArea = areaOfInterest.getPicArea();
                                            Optional<TitledForm> aoiFormOptional = findAoiSectionView(picArea);
                                            aoiFormOptional.ifPresent(aoiForm -> aoiForm.submit(null));
                                        }
                                    }
                                });
                        });
            });

        notificationCenter.subscribe(
            ActionManager.DELETE_AOI_EVENT,
            (key, payload) -> {
                Optional.of(payload)
                    .filter(objects -> objects.length == 1)
                    .map(objects -> (PicArea)objects[0])
                    .ifPresent(
                        picArea -> {
                            Dispatcher dispatcher = Dispatcher.platform();
                            dispatcher.runLater(
                                () -> {
                                    Optional<TitledForm> form = findAoiSectionView(picArea);
                                    if (form.isPresent()) {
                                        AreaOfInterest areaOfInterest = (AreaOfInterest)form.get().getUserData();
                                        viewModel.createRemoveAoiCommand(areaOfInterest).execute();
                                    }
                                });
                        });
            });
    }

    @Override
    public EditFlightplanViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        editFlightplanView.visibleProperty().bind(viewModel.chooseAoiModeBinding().not());
        editFlightplanView.managedProperty().bind(viewModel.chooseAoiModeBinding().not());
        chooseAoiView.visibleProperty().bind(viewModel.chooseAoiModeBinding());
        chooseAoiView.managedProperty().bind(viewModel.chooseAoiModeBinding());

        addAreaOfInterestBtn.managedProperty().bind(addAreaOfInterestBtn.visibleProperty());
        addAreaOfInterestBtn.disableProperty().bind(viewModel.allAoisInViewModeProperty().not());
        viewModel.subscribe(
            PlanningScope.EVENT_ON_FLIGHT_PLAN_REVERT_CHANGES,
            (key, payload) -> {
                rebuildPlanningSidePane();
            });

        viewModel
            .selectedAoiProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    findAoiSectionView(newValue).ifPresent(form -> form.isExpandedProperty().set(true));
                });

        viewModel
            .currentFlightplanProperty()
            .addListener((observable, oldValue, newValue) -> rebuildPlanningSidePane());

        propertyPathStore
            .from(getViewModel().getPlanningScope().currentFlightplanProperty())
            .selectReadOnlyBoolean(FlightPlan::isTemplateProperty)
            .addListener(
                ((observable1, oldValue1, isTemplates) -> {
                    if (Boolean.TRUE.equals(isTemplates)) {
                        editArea.getStyleClass().add(CSS_TEMPLATE_EDIT_CONTENT);
                    } else {
                        editArea.getStyleClass().remove(CSS_TEMPLATE_EDIT_CONTENT);
                    }
                }));

        projectNameLabel.textProperty().bind(viewModel.missionNameProperty());

        flightPlansMenuButton.setModel(viewModel.getFlightPlanMenuModel());

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener((observable, oldValue, newValue) -> rebuildPlanningSidePane());

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .select(Mission::currentFlightPlanProperty)
            .selectReadOnlyList(FlightPlan::areasOfInterestProperty)
            .addListener(
                (ListChangeListener<? super AreaOfInterest>)
                    c -> {
                        while (c.next()) {
                            if (c.wasAdded() || c.wasRemoved()) {
                                rebuildPlanningSidePane();
                                break;
                            }
                        }
                    });

        showOnMapButton.setOnAction(event -> viewModel.getShowOnMapCommand().execute());
        showOnMapButton.disableProperty().bind(viewModel.getShowOnMapCommand().notExecutableProperty());

        saveBtn.disableProperty().bind(viewModel.getSaveFlightplanCommand().notExecutableProperty());
        saveBtn.setOnAction((a) -> viewModel.getSaveFlightplanCommand().execute());

        saveFpButtonMenuItem.disableProperty().bind(viewModel.getSaveFlightplanCommand().notExecutableProperty());
        saveFpButtonMenuItem.setOnAction((a) -> viewModel.getSaveFlightplanCommand().execute());

        exportFpButtonMenuItem.disableProperty().bind(viewModel.getExportFlightplanCommand().notExecutableProperty());
        exportFpButtonMenuItem.setOnAction((a) -> viewModel.getExportFlightplanCommand().execute());

        saveFpAndProceedButtonMenuItem
            .visibleProperty()
            .addListener(
                ((observable, oldValue, newValue) -> {
                    if (newValue) {
                        footer.getChildren().add(0, saveFpAndProceedButtonMenuItem);
                    } else {
                        footer.getChildren().remove(saveFpAndProceedButtonMenuItem);
                    }
                }));
        saveFpAndProceedButtonMenuItem
            .disableProperty()
            .bind(
                viewModel
                    .getSaveFlightplanAndProceedCommand()
                    .notExecutableProperty()
                    .and(viewModel.toolsAvailableDebugProperty()));
        saveFpAndProceedButtonMenuItem.visibleProperty().bind(viewModel.toolsAvailableDebugProperty());
        saveFpAndProceedButtonMenuItem.setOnAction((a) -> viewModel.getSaveFlightplanAndProceedCommand().execute());

        exportButton.disableProperty().bind(viewModel.getExportFlightplanCommand().notExecutableProperty());
        exportButton.setOnAction((a) -> viewModel.getExportFlightplanCommand().execute());

        footer.visibleProperty().bind(viewModel.chooseAoiModeBinding().not());
        footer.managedProperty().bind(viewModel.chooseAoiModeBinding().not());

        saveBtn.visibleProperty().bind(viewModel.showFlightPlanTemplateFooterProperty().not());
        saveBtn.managedProperty().bind(viewModel.showFlightPlanTemplateFooterProperty().not());
        exportButton
            .visibleProperty()
            .bind(
                saveFpAndProceedButtonMenuItem
                    .visibleProperty()
                    .not()
                    .and(viewModel.showFlightPlanTemplateFooterProperty().not()));
        exportButton
            .managedProperty()
            .bind(
                saveFpAndProceedButtonMenuItem
                    .visibleProperty()
                    .not()
                    .and(viewModel.showFlightPlanTemplateFooterProperty().not()));

        saveTemplate.visibleProperty().bind(viewModel.showFlightPlanTemplateFooterProperty());
        saveTemplate.managedProperty().bind(viewModel.showFlightPlanTemplateFooterProperty());

        saveTemplate.disableProperty().bind(viewModel.getSaveFlightplanCommand().notExecutableProperty());
        saveTemplate.setOnAction((a) -> viewModel.getSaveFlightplanCommand().execute());
    }

    private void rebuildPlanningSidePane() {
        FlightPlan currentFlightplan = viewModel.getCurrentFlightplan();
        if (currentFlightplan != null) {
            startMenuItem();
            drawAvailableAois(
                currentFlightplan.getLegacyFlightplan().getHardwareConfiguration().getPlatformDescription());
            loadSectionsAll();
        }
    }

    private void drawAvailableAois(IPlatformDescription platformDescription) {
        Map<AreasOfInterestType, List<PlanType>> map = AreasOfInterestType.forPlatform(platformDescription);
        addAreaOfInterestBtn.getItems().addAll(buildMenuItems(map));
    }

    private void loadSectionsAll() {
        resetForms();
        loadSectionSummary();
        FlightPlan flightPlan = viewModel.getCurrentFlightplan();
        if (flightPlan != null) {
            ObservableList<AreaOfInterest> currentAois = flightPlan.getAreasOfInterest();
            for (AreaOfInterest areaOfInterest : currentAois) {
                if (areaOfInterest == null) {
                    continue;
                }

                AoiTitledForm aoiForm = new AoiTitledForm(areaOfInterest);

                forms.add(aoiForm);
                formsContainer.getChildren().add(aoiForm);
                makeDraggable(areaOfInterest, aoiForm);
            }
        }

        loadSectionsNonAoi();
    }

    @FXML
    public void renameClicked() {
        viewModel.getRenameMissionCommand().execute();
    }

    private class AoiTitledForm extends TitledForm {

        final BooleanBinding isEditableBinding;

        public AoiTitledForm(AreaOfInterest areaOfInterest) {
            super(
                areaOfInterest.nameProperty(),
                form -> aoiEditControl(areaOfInterest, form),
                form -> getViewControl(areaOfInterest, form));

            setShowAdvancedDialogHandler(
                new ParameterizedCommandInvocation<>(viewModel.getShowAdvancedDialogCommand(), areaOfInterest));
            showAdvancedDialogPossibleProperty().unbind();
            showAdvancedDialogPossibleProperty().bind(viewModel.canShowAdvancedDialogProperty());
            setUserData(areaOfInterest);

            isExpandedProperty().set(areaOfInterest.hasEnoughCornersBinding().not().get());

            isInEditStateProperty()
                .addListener(
                    ((editStateProperty, wasInEditState, isInEditState) -> {
                        if (isInEditState) {
                            getTitledPane().getStyleClass().add(CSS_HIGLIGHT_TITLED_PANE);
                        } else {
                            getTitledPane().getStyleClass().remove(CSS_HIGLIGHT_TITLED_PANE);
                        }
                    }));

            isEditableBinding =
                viewModel.selectedAoiProperty().isEqualTo(areaOfInterest).and(viewModel.editStateProperty());

            isEditableBinding.addListener(
                ((observable, oldValue, newValue) -> {
                    isInEditStateProperty().set(newValue);
                }));

            isInEditStateProperty().set(isEditableBinding.get());
        }
    }

    private void resetForms() {
        formsContainer.getChildren().clear();
        forms.clear();
    }

    private Node getViewControl(AreaOfInterest areaOfInterest, TitledForm form) {
        return getControl(areaOfInterest, form);
    }

    private Node aoiEditControl(AreaOfInterest areaOfInterest, TitledForm form) {
        return getControl(areaOfInterest, form);
    }

    private Node getControl(AreaOfInterest areaOfInterest, TitledForm form) {
        VBox aoiBox = new VBox();
        aoiBox.getChildren().add(new AoiEditComponent(areaOfInterest, context, quantityStyleProvider, languageHelper));

        // "Select this area of interest" button was pressed
        form.isInEditStateProperty()
            .addListener(
                ((observable, oldValue, newValue) -> {
                    if (newValue) {
                        viewModel.enterEditingMode(areaOfInterest);
                    } else if (viewModel.selectedAoiProperty().get() == areaOfInterest) {
                        viewModel.exitEditingMode();
                    }
                }));
        form.setRemoveCommand(viewModel.createRemoveAoiCommand(areaOfInterest));
        form.setSubmitCommand(viewModel.createDoneAoiCommand(areaOfInterest));
        aoiBox.setMinWidth(0);
        aoiBox.setMaxWidth(Double.MAX_VALUE);
        return aoiBox;
    }

    private void makeDraggable(AreaOfInterest areaOfInterest, TitledForm aoiForm) {
        TitledPane aoiPane = aoiForm.getTitledPane();
        Expect.notNull(aoiPane, "aoiPane");
        aoiPane.setOnDragDetected(handleOnDragDetected(areaOfInterest, aoiForm));
        aoiPane.setOnDragOver(handleDragOver(aoiForm));
        aoiPane.setOnDragDropped(handleDragDropped(areaOfInterest));
    }

    private EventHandler<MouseEvent> handleOnDragDetected(AreaOfInterest areaOfInterest, TitledForm titledForm) {
        return event -> {
            Dragboard db = titledForm.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            int id = viewModel.indexOfAoi(areaOfInterest);
            content.putString(Integer.toString(id));
            db.setContent(content);
            event.consume();
        };
    }

    private EventHandler<DragEvent> handleDragOver(TitledForm form) {
        return event -> {
            if (event.getGestureSource() != form && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            event.consume();
        };
    }

    private EventHandler<DragEvent> handleDragDropped(AreaOfInterest areaOfInterest) {
        return event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int sourceId = Integer.parseInt(db.getString());
                int destId = viewModel.indexOfAoi(areaOfInterest);

                if (destId != sourceId) {
                    viewModel.changeAoiPosition(sourceId, destId);
                    loadSectionsAll();
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        };
    }

    private void loadSectionsNonAoi() {
        formsContainer.getChildren().add(loadGeneralSettings());
        formsContainer.getChildren().add(loadStartingSection());
        formsContainer.getChildren().add(loadLandingSection());
        formsContainer.getChildren().add(loadEmergencyActionsSection());
        formsContainer.getChildren().add(loadHardwareSection());
    }

    private void loadSectionSummary() {
        formsContainer.getChildren().add(loadSummarySection());
        formsContainer.getChildren().add(loadWarningSection());
    }

    private TitledPane loadGeneralSettings() {
        ViewTuple<GeneralSettingsSectionView, GeneralSettingsSectionViewModel> viewTuple =
            FluentViewLoader.fxmlView(GeneralSettingsSectionView.class).context(context).load();
        return collapsed(
            new TitledPane(languageHelper.getString("planningView.generalplansettings"), viewTuple.getView()));
    }

    private TitledPane loadStartingSection() {
        ViewTuple<StartingView, StartingViewModel> viewTuple =
            FluentViewLoader.fxmlView(StartingView.class).context(context).load();
        return collapsed(new TitledPane(languageHelper.getString("planningView.start"), viewTuple.getView()));
    }

    private TitledPane loadLandingSection() {
        ViewTuple<LandingView, LandingViewModel> viewTuple =
            FluentViewLoader.fxmlView(LandingView.class).context(context).load();
        TitledPane section =
            collapsed(new TitledPane(languageHelper.getString("planningView.landing"), viewTuple.getView()));
        // TODO make this section show up with a debug license
        //        BindingUtils.bindVisibility(section, viewModel.isLandingVisibleBinding());
        return section;
    }

    private TitledPane loadEmergencyActionsSection() {
        ViewTuple<EmergencyActionsView, EmergencyActionsViewModel> viewTuple =
            FluentViewLoader.fxmlView(EmergencyActionsView.class).context(context).load();
        TitledPane section =
            collapsed(new TitledPane(languageHelper.getString("planningView.emergencyactions"), viewTuple.getView()));
        BindingUtils.bindVisibility(section, viewModel.isEmergencyActionsVisibleBinding());
        return section;
    }

    private TitledPane loadHardwareSection() {
        ViewTuple<HardwareSelectionView, HardwareSelectionViewModel> viewTuple =
            FluentViewLoader.fxmlView(HardwareSelectionView.class).context(context).load();
        viewTuple.getViewModel().bindHardwareConfiguration(viewModel.hardwareConfigurationProperty());
        return collapsed(new TitledPane(languageHelper.getString("planningView.hardware"), viewTuple.getView()));
    }

    private TitledPane loadSummarySection() {
        ViewTuple<FlightplanSummaryView, FlightplanSummaryViewModel> viewTuple =
            FluentViewLoader.fxmlView(FlightplanSummaryView.class).context(context).load();

        TitledPane section = new TitledPane(languageHelper.getString("planningView.summary"), viewTuple.getView());

        BindingUtils.bindVisibility(section, viewModel.isWarningsVisibleBinding().not());
        return section;
    }

    private TitledPane loadWarningSection() {
        ViewTuple<WarningsView, WarningsViewModel> viewTuple =
            FluentViewLoader.fxmlView(WarningsView.class).context(context).load();
        TitledPane section = new TitledPane(languageHelper.getString("planningView.summary"), viewTuple.getView());

        BindingUtils.bindVisibility(section, viewModel.isWarningsVisibleBinding());
        return section;
    }

    private TitledPane collapsed(TitledPane titledPane) {
        titledPane.expandedProperty().set(false);
        return titledPane;
    }

    public void startMenuItem() {
        addAreaOfInterestBtn.getItems().clear();
    }

    private List<MenuItem> buildMenuItems(Map<AreasOfInterestType, List<PlanType>> aoiTypes) {
        SectionMenuItemsBuilder builder = new SectionMenuItemsBuilder();
        for (Map.Entry<AreasOfInterestType, List<PlanType>> areasOfInterestType : aoiTypes.entrySet()) {
            if (areasOfInterestType.getValue().isEmpty()) {
                continue;
            }

            AreasOfInterestType aoiType = areasOfInterestType.getKey();
            MenuItem titleMenuItem = buildSectionTitleMenuItem(aoiType);
            builder.addSection(aoiType, titleMenuItem)
                .addSubItems(
                    areasOfInterestType.getValue().stream().map(this::buildMenuItem).collect(Collectors.toList()));
        }

        return builder.buildFlatItems();
    }

    private MenuItem buildSectionTitleMenuItem(AreasOfInterestType aoiType) {
        MenuItem titleMenuItem = new MenuItem(languageHelper.toFriendlyName(aoiType));
        titleMenuItem.getStyleClass().add("title-menu-item");
        titleMenuItem.setDisable(true);
        return titleMenuItem;
    }

    private MenuItem buildMenuItem(PlanType area) {
        MenuItem tempMenuItem = new IntelMenuItem(area);
        tempMenuItem.setText(languageHelper.toFriendlyName(area));
        tempMenuItem.setId(area.toString() + "_Planning");
        tempMenuItem.getStyleClass().add("icon-menu-item");
        tempMenuItem.setOnAction(
            mouseEvent -> {
                IntelMenuItem aoiChoosen = (IntelMenuItem)mouseEvent.getSource();
                viewModel.chooseAreaOfInterest(aoiChoosen.getArea());
                loadSectionsAll();
            });
        return tempMenuItem;
    }

    private Optional<TitledForm> findAoiSectionView(PicArea picArea) {
        if (picArea == null) {
            return Optional.empty();
        }

        return forms.stream().filter(form -> ((AreaOfInterest)form.getUserData()).getPicArea() == picArea).findFirst();
    }

    private Optional<TitledForm> findAoiSectionView(AreaOfInterest areaOfInterest) {
        if (areaOfInterest == null) {
            return Optional.empty();
        }

        return forms.stream().filter(form -> ((AreaOfInterest)form.getUserData()) == areaOfInterest).findFirst();
    }

    private Optional<TitledForm> findFirstAoiSectionInEditState() {
        return forms.stream().filter(form -> form.isInEditStateProperty().get()).findFirst();
    }

}
