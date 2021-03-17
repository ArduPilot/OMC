/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.widgets.wkt;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class SpatialReferenceChooserView extends DialogView<SpatialReferenceChooserViewModel> {

    private static final String KEY = "eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserView";

    @InjectViewModel
    private SpatialReferenceChooserViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private TreeView<SpatialReference> srsTreeView;

    @FXML
    private TextArea wktSource;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField searchSrsField;

    @FXML
    private Button deleteButton;

    @FXML
    public Button createButton;

    private TreeItem<SpatialReference> rootNode;
    private TreeItem<SpatialReference> groupItemProjSrs;
    private TreeItem<SpatialReference> groupItemGeoSrs;
    private TreeItem<SpatialReference> groupItemPrivSrs;

    private final ILanguageHelper languageHelper;

    private final ObjectProperty<SpatialReference> selectedSrs = new SimpleObjectProperty<>();
    private final StringProperty filter = new SimpleStringProperty("");

    @Inject
    public SpatialReferenceChooserView(ILanguageHelper languageHelper) {
        this.languageHelper = languageHelper;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        rootNode = new TreeItem();

        groupItemProjSrs = new TreeItem(new SpatialReference());
        groupItemProjSrs.getValue().nameProperty().setValue(languageHelper.getString(KEY + "." + viewModel.PROJ_SRS));
        groupItemGeoSrs = new TreeItem(new SpatialReference());
        groupItemGeoSrs.getValue().nameProperty().setValue(languageHelper.getString(KEY + "." + viewModel.GEO_SRS));
        groupItemPrivSrs = new TreeItem(new SpatialReference());
        groupItemPrivSrs.getValue().nameProperty().setValue(languageHelper.getString(KEY + "." + viewModel.PRIV_SRS));

        rootNode.setExpanded(true);
        srsTreeView.setRoot(rootNode);

        srsTreeView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        if (newValue != null && !newValue.getValue().isGroup()) {
                            selectedSrs.set(newValue.getValue());
                            wktSource.setText(newValue.getValue().getWkt());
                        } else {
                            if (newValue != null) {
                                selectedSrs.set(newValue.getValue());
                            }

                            wktSource.setText(null);
                        }
                    } catch (Exception e) {
                        selectedSrs.set(null);
                        wktSource.setText(null);
                    }
                });
        srsTreeView.setFocusTraversable(true);
        srsTreeView.addEventHandler(
            KeyEvent.KEY_RELEASED,
            event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    if (viewModel.getProceedCommand().executableProperty().get()) {
                        viewModel.getProceedCommand().execute();
                    }
                }
            });

        searchSrsField.addEventHandler(
            KeyEvent.KEY_RELEASED,
            event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    if (viewModel.getProceedCommand().executableProperty().get()) {
                        viewModel.getProceedCommand().execute();
                    }
                }
            });

        srsTreeView.addEventHandler(
            MouseEvent.MOUSE_CLICKED,
            event -> {
                if (event.getClickCount() > 1) {
                    if (viewModel.getProceedCommand().executableProperty().get()) {
                        viewModel.getProceedCommand().execute();
                    }
                }
            });

        getRootNode()
            .addEventHandler(
                KeyEvent.KEY_PRESSED,
                event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        if (viewModel.getAddNewCommand().isExecuting()) return;
                        if (viewModel.getCloseCommand().executableProperty().get()) {
                            viewModel.getCloseCommand().execute();
                        }
                    }
                });
        okButton.disableProperty().bind(viewModel.getProceedCommand().notExecutableProperty());
        okButton.setOnAction(event -> viewModel.getProceedCommand().execute());

        cancelButton.disableProperty().bind(viewModel.getCloseCommand().notExecutableProperty());
        cancelButton.setOnAction(event -> viewModel.getCloseCommand().execute());

        deleteButton.setOnAction(this::deleteSelected);
        deleteButton.disableProperty().bind(viewModel.getDeleteCommand().notExecutableProperty());

        createButton.setOnAction(this::addNew);

        filter.bind(searchSrsField.textProperty());
        searchSrsField
            .textProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    viewModel.updateSrsItems();
                    refreshTree();
                });
        selectedSrs.bindBidirectional(viewModel.selectedSrsProperty());
        viewModel.filterProperty().bind(filter);

        refreshTree();

        srsTreeView.setCellFactory(
            param ->
                new TreeCell<SpatialReference>() {
                    Double defaultHeight = 0.;

                    @Override
                    protected void updateItem(SpatialReference item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(null);
                        if (item == null) {
                            setTooltip(null);
                            setText(null);
                            return;
                        }
                        if (item.getId() != null
                                && item.getCategory() != null
                                && item.getCategory() != SpatialReference.GEO_SRS) {
                            if (item.isGeoidOK()) {
                                setTooltip(new Tooltip(item.getName()));
                                setText(item.toString());
                            } else {
                                ImageView imageView =
                                    new ImageView(
                                        "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg");
                                ImageView imageView2 =
                                    new ImageView(
                                        "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg");
                                Tooltip toolTip =
                                    new Tooltip(
                                        languageHelper.getString(
                                            KEY + ".tooltip.noGeoid", item.getName(), item.getGeoidFileName()));
                                toolTip.setGraphic(imageView2);
                                setTooltip(toolTip);
                                setGraphic(imageView);
                                setText(item.toString());
                            }
                        } else {
                            if (item.getId() == null && item.getName() != null) {
                                setText(item.getName());
                                setTooltip(new Tooltip(item.getName()));
                            } else {
                                setText(item.toString());
                                setTooltip(new Tooltip(item.toString()));
                            }
                        }
                    }
                });
    }

    private void addNew(ActionEvent actionEvent) {
        viewModel.getAddNewCommand().execute();
        refreshTree();
        srsTreeView.requestFocus();
    }

    private void deleteSelected(ActionEvent actionEvent) {
        final IDialogService dialogService = DependencyInjector.getInstance().getInstanceOf(IDialogService.class);
        if (!dialogService.requestConfirmation(
                languageHelper.getString(KEY + ".confirmDeletionTitle"),
                languageHelper.getString(KEY + ".confirmDeletionQuestion", selectedSrs.get().toString()))) {
            return;
        }

        viewModel.getDeleteCommand().execute();
        refreshTree();
    }

    private void refreshTree() {
        rootNode.getChildren().clear();
        groupItemProjSrs.getChildren().clear();
        groupItemGeoSrs.getChildren().clear();
        groupItemPrivSrs.getChildren().clear();
        List<SpatialReference> items = viewModel.getSrsItems();

        TreeItem<SpatialReference> selectedTreeItem = null;
        rootNode.getChildren().add(groupItemGeoSrs);
        rootNode.getChildren().add(groupItemProjSrs);

        boolean foundPrivate = false;
        for (SpatialReference item : items) {
            boolean found = false;
            TreeItem treeItem = new TreeItem<>(item);
            if (item.getCategory().equals(viewModel.GEO_SRS)) {
                groupItemGeoSrs.getChildren().add(treeItem);
            } else if (item.getCategory().equals(viewModel.PRIV_SRS)) {
                groupItemPrivSrs.getChildren().add(treeItem);
                foundPrivate = true;
            } else {
                for (TreeItem node : groupItemProjSrs.getChildren()) {
                    if (node.getValue().toString().equals(item.getCategory())) {
                        node.getChildren().add(treeItem);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    SpatialReference group = new SpatialReference();
                    group.categoryProperty().setValue(item.getCategory());
                    TreeItem groupItem = new TreeItem<>(group);
                    groupItemProjSrs.getChildren().add(groupItem);
                    treeItem = new TreeItem<>(item);
                    groupItem.getChildren().add(treeItem);
                }
            }

            if (selectedTreeItem == null && item.getId().equals(viewModel.getSrsDefault().id)) {
                selectedTreeItem = treeItem; // default
            }

            if (item.equals(selectedSrs.get())) {
                selectedTreeItem = treeItem;
            }
        }

        if (foundPrivate) {
            rootNode.getChildren().add(groupItemPrivSrs);
        }

        if (selectedTreeItem != null) {
            int index;
            if (selectedTreeItem.getValue().getCategory().equals(viewModel.GEO_SRS)) {
                index = groupItemGeoSrs.getChildren().indexOf(selectedTreeItem);
                if (index > groupItemGeoSrs.getChildren().size() / 2) index += 5;
            } else if (selectedTreeItem.getValue().getCategory().equals(viewModel.PRIV_SRS)) {
                index = groupItemPrivSrs.getChildren().indexOf(selectedTreeItem) + 3;
            } else {
                index =
                    groupItemProjSrs.getChildren().indexOf(selectedTreeItem.getParent())
                        + selectedTreeItem.getParent().getChildren().indexOf(selectedTreeItem);
                if (index > groupItemProjSrs.getChildren().size() / 2) index += 5;
            }

            if (index < 10) index = 0;

            // close all TreeItems
            groupItemProjSrs.expandedProperty().set(false);
            groupItemGeoSrs.expandedProperty().set(false);
            groupItemPrivSrs.expandedProperty().set(false);

            srsTreeView.getSelectionModel().select(selectedTreeItem);
            srsTreeView.scrollTo(index);
        }

        if (!filter.isEmpty().get()) {
            rootNode.getChildren().forEach((node) -> node.expandedProperty().set(true));
        }
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(KEY + ".title"));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

}
