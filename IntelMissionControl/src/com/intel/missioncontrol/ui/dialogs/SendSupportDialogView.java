/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.inject.Inject;
import com.intel.missioncontrol.api.support.ErrorCategory;
import com.intel.missioncontrol.api.support.Priority;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.validation.LabelValidationVisualizer;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.utils.validation.visualization.ValidationVisualizer;
import de.saxsys.mvvmfx.utils.viewlist.CachedViewModelCellFactory;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

/** Created by akorotenko on 7/19/17. */
public class SendSupportDialogView extends DialogView<SendSupportDialogViewModel> {

    public static final String MISSION_LABEL =
        "com.intel.missioncontrol.ui.dialogs.SendSupportDialogView.useCurrentMission";
    public static final double COL_MATCHING_NAME_WIDTH = 1f * Integer.MAX_VALUE * 55; // 55% width
    public static final double COL_NO_WIDTH = 1f * Integer.MAX_VALUE * 15; // 15% width
    public static final double COL_PREVIEW_WIDTH = 1f * Integer.MAX_VALUE * 15; // 55% width
    public static final double COL_ALL_WIDTH = 1f * Integer.MAX_VALUE * 15; // 15% width

    public static final MatchingImagesUsage DEFAULT_SELECTION = MatchingImagesUsage.PREVIEW;

    public static final double SEND_SUPPOPT_WARNING_IMAGE_WIDTH = ScaleHelper.emsToPixels(1.5);
    public static final double SEND_SUPPOPT_WARNING_IMAGE_HEIGHT = ScaleHelper.emsToPixels(1.5);

    @InjectViewModel
    private SendSupportDialogViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private TextArea problemDescription;

    @FXML
    private TextField ticketId;

    @FXML
    private ComboBox<Priority> priority;

    @FXML
    private ComboBox<ErrorCategory> experiencingProblems;

    @FXML
    private ListView<FileListCellViewModel> additionalFiles;

    @FXML
    private Label estimatedFilesSize;

    @FXML
    private TextField fullName;

    @FXML
    private TextField emailsText;

    @FXML
    private ComboBox countrySelection;

    @FXML
    private CheckBox useCurrentMission;

    @FXML
    private CheckBox useScreenshots;

    @FXML
    private CheckBox useSettingsAndLogs;

    @FXML
    private TableView<MatchingsTableRowData> matchings;

    @FXML
    private TableColumn<MatchingsTableRowData, String> columnMatching;

    @FXML
    private TableColumn columnNo;

    @FXML
    private TableColumn columnAll;

    @FXML
    private TableColumn columnPreview;

    @FXML
    private Button sendButton;

    @FXML
    private Label noSelectedMatchingAlert;

    @FXML
    private Label useCurrentMissionAlert;

    @FXML
    private ImageView sendSupportWarningImage;

    @Inject
    private ILanguageHelper languageHelper;

    @InjectContext
    private Context context;

    @Inject
    private IDialogContextProvider dialogContextProvider;

    private final List<ValidationVisualizer> validationVisualizers = new ArrayList<>();

    @Override
    public void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        priority.setItems(viewModel.getIssuePriorities());
        priority.valueProperty().bindBidirectional(viewModel.selectedPriorityProperty());
        priority.setConverter(new EnumConverter<>(languageHelper, Priority.class));
        priority.getSelectionModel().select(Priority.HIGH);
        emailsText.textProperty().bindBidirectional(viewModel.emailsProperty());
        countrySelection.setItems(viewModel.getCountryList());
        fullName.textProperty().bindBidirectional(viewModel.fullNameProperty());
        countrySelection.getEditor().textProperty().bindBidirectional(viewModel.countrySelectedProperty());

        experiencingProblems.setItems(viewModel.errorCategoriesProperty());
        experiencingProblems.valueProperty().bindBidirectional(viewModel.selectedCategoryProperty());
        experiencingProblems.setConverter(new EnumConverter<>(languageHelper, ErrorCategory.class));
        experiencingProblems.getSelectionModel().select(0);

        ticketId.textProperty().bindBidirectional(viewModel.ticketIdProperty());
        problemDescription.textProperty().bindBidirectional(viewModel.problemDescriptionProperty());

        estimatedFilesSize.textProperty().bind(viewModel.estimatedFilesSizeProperty());

        additionalFiles.setItems(viewModel.getAdditionalFiles());
        additionalFiles.setCellFactory(CachedViewModelCellFactory.createForFxmlView(FileListCellView.class));

        additionalFiles.setOnDragOver(
            event -> {
                if (event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            });
        additionalFiles.setOnDragDropped(
            event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    viewModel.handleFilesDropped(db.getFiles());
                }

                event.setDropCompleted(success);
                event.consume();
            });

        useCurrentMission.setText(
            languageHelper.getString(MISSION_LABEL, viewModel.currentMissionNameProperty().getValue()));
        useScreenshots.selectedProperty().bindBidirectional(viewModel.useScreenshotsProperty());
        useSettingsAndLogs.selectedProperty().bindBidirectional(viewModel.useSettingsAndLogsProperty());
        useCurrentMission.selectedProperty().bindBidirectional(viewModel.useCurrentMissionProperty());

        columnMatching.setCellValueFactory(param -> param.getValue().matchingNameProperty());
        columnPreview.setCellFactory(param -> new RadioButtonTableCell(MatchingImagesUsage.PREVIEW));
        columnNo.setCellFactory(param -> new RadioButtonTableCell(MatchingImagesUsage.NO));
        columnAll.setCellFactory(param -> new RadioButtonTableCell(MatchingImagesUsage.ALL));
        matchings.setRowFactory(
            param -> {
                TableRow row = new TableRow();
                ToggleGroup group = new ToggleGroup();
                row.setUserData(group);
                group.selectedToggleProperty()
                    .addListener(
                        (observable, oldValue, newValue) -> {
                            ((MatchingsTableRowData)row.getItem())
                                .matchingImagesUsageProperty()
                                .setValue((MatchingImagesUsage)newValue.getUserData());
                            viewModel.handleMatchingUsageChanging();
                        });
                return row;
            });

        columnMatching.setMaxWidth(COL_MATCHING_NAME_WIDTH);
        columnNo.setMaxWidth(COL_NO_WIDTH);
        columnPreview.setMaxWidth(COL_PREVIEW_WIDTH);
        columnAll.setMaxWidth(COL_ALL_WIDTH);
        matchings.disableProperty().bind(viewModel.useCurrentMissionProperty().not());
        matchings.setMaxWidth(1f * Integer.MAX_VALUE);

        matchings.setItems(viewModel.getMatchings());

        sendButton.disableProperty().bind(viewModel.canUploadProperty().not());

        sendSupportWarningImage.setFitWidth(SEND_SUPPOPT_WARNING_IMAGE_WIDTH);
        sendSupportWarningImage.setFitHeight(SEND_SUPPOPT_WARNING_IMAGE_HEIGHT);

        ValidationVisualizer visualizer = new LabelValidationVisualizer();
        visualizer.initVisualization(viewModel.matchingsValidation(), noSelectedMatchingAlert);
        validationVisualizers.add(visualizer);

        visualizer = new LabelValidationVisualizer();
        visualizer.initVisualization(viewModel.missionValidation(), useCurrentMissionAlert);
        validationVisualizers.add(visualizer);
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected SendSupportDialogViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(languageHelper.getString(SendSupportDialogView.class.getName() + ".title"));
    }

    @FXML
    public void previewFilesClicked() {
        viewModel.openPreviewFilesDialog();
    }

    @FXML
    public void sendRequest() {
        viewModel.sendToSupport();
    }

    @FXML
    public void cancelRequest() {
        viewModel.getCloseCommand().execute();
    }

    static class TextEditCell extends ListCell<String> {

        private final TextField graphics;

        public TextEditCell() {
            graphics = new TextField();
            graphics.getStyleClass().add("textFieldExtraWidth");
            graphics.textProperty()
                .addListener(
                    (observable, oldValue, newValue) -> {
                        if (!getListView().getItems().get(getIndex()).equals(newValue)) {
                            getListView().getItems().set(getIndex(), newValue);
                        }
                    });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (getGraphic() == null) {
                    graphics.setText(item);
                    setGraphic(graphics);
                }
            }

            super.updateItem(item, empty);
        }

    }

    static class RadioButtonTableCell extends TableCell<MatchingsTableRowData, Boolean> {

        private RadioButton radioButton;

        public RadioButtonTableCell(MatchingImagesUsage usage) {
            super();
            radioButton = new RadioButton();
            radioButton.setUserData(usage);
            radioButton.setSelected(usage == DEFAULT_SELECTION);
        }

        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (getTableRow().getUserData() != null && getTableRow().getUserData() instanceof ToggleGroup) {
                    radioButton.setToggleGroup((ToggleGroup)getTableRow().getUserData());
                }

                setGraphic(radioButton);
            }
        }
    }

}
