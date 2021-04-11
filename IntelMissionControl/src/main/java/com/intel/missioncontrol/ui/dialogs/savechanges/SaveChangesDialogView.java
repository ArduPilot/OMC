package com.intel.missioncontrol.ui.dialogs.savechanges;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.common.components.RenameDialog;
import com.intel.missioncontrol.ui.dialogs.DialogView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SaveChangesDialogView extends DialogView<SaveChangesDialogViewModel> {

    private static int SCROLL_PANE_MIN_HEIGHT = 150;

    @FXML
    VBox layoutRoot;

    @FXML
    VBox flightPlansContainer;

    @FXML
    ScrollPane checkBoxesScrollContainer;

    @FXML
    VBox allCheckBoxesContainer;

    @FXML
    VBox checkBoxesContainer;

    @FXML
    CheckBox saveAll;

    @FXML
    Label message;

    @FXML
    VBox missionNameConfirmationContainer;

    @FXML
    private TextField missionName;

    @FXML
    Button proceedWithoutSaving;

    @FXML
    Button saveAndProceed;

    @FXML
    Button cancel;

    @FXML
    private ListView<ChangedItemViewModel> changedItemsList;

    @Inject
    private ILanguageHelper languageHelper;

    @InjectViewModel
    private SaveChangesDialogViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();
        changedItemsList.setItems(viewModel.changedItemsProperty());

        changedItemsList.setCellFactory(
            callback ->
                new ListCell<>() {
                    @Override
                    protected void updateItem(ChangedItemViewModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            CheckBox checkBox = new CheckBox();
                            checkBox.selectedProperty().bindBidirectional(item.needsToSaveProperty());
                            checkBox.selectedProperty()
                                .addListener(
                                    (oblect, oldValue, newValue) -> {
                                        if (!newValue) {
                                            saveAll.selectedProperty().setValue(false);
                                        }
                                    });
                            checkBox.setMnemonicParsing(false);
                            checkBox.textProperty().bind(item.nameProperty());
                            setGraphic(checkBox);
                        }
                    }
                });

        checkBoxesContainer.minWidthProperty().bind(checkBoxesScrollContainer.widthProperty());
        checkBoxesScrollContainer.prefHeightProperty().bind(checkBoxesScrollContainer.maxHeightProperty());
        missionNameConfirmationContainer.setVisible(false);
        checkBoxesScrollContainer.prefHeightProperty().bind(checkBoxesScrollContainer.minHeightProperty());
        missionName
                .textProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    String newName = RenameDialog.removeIllegalChars(newValue);
                    missionName.textProperty().set(newName);
                    saveAndProceed.setDisable(viewModel.onMissionNameChanged(newName));
                }));
        missionName.textProperty().bindBidirectional(viewModel.nameProperty());

        missionNameConfirmationContainer.setVisible(true);

        checkBoxesScrollContainer.setMinHeight(ScaleHelper.scalePixels(SCROLL_PANE_MIN_HEIGHT));

        viewModel.dialogTypeProperty().addListener((observable, oldValue, newValue) -> dialogTypeChange(newValue));
        dialogTypeChange(viewModel.dialogTypeProperty().get());
    }

    @FXML
    private void saveAllOnAction() {
        viewModel.saveAllOnAction(saveAll.isSelected());
    }

    @FXML
    private void saveAndExitOnAction() {
        viewModel.saveAndExitOnAction();
    }

    @FXML
    private void closeButtonOnAction() {
        viewModel.closeButtonOnAction();
    }

    @FXML
    private void exitWithoutSavingOnAction() {
        viewModel.proceedWithoutSavingOnAction();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return new ReadOnlyStringWrapper(
            languageHelper.getString("com.intel.missioncontrol.ui.dialogs.ExitConfirmationDialogView.title"));
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    private void dialogTypeChange(SaveChangesDialogViewModel.DialogTypes dialogType) {
        saveAndProceed.setText(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.ExitConfirmationDialogView.Button.SaveAndCloseOperationName."
                    + dialogType));
        cancel.setText(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.ExitConfirmationDialogView.Button.Cancel." + dialogType));
        proceedWithoutSaving.setText(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.ExitConfirmationDialogView.Button.CloseWithoutSavingOperationName."
                    + dialogType));
        message.setText(
            languageHelper.getString(
                "com.intel.missioncontrol.ui.dialogs.ExitConfirmationDialogView.Label." + dialogType));

    }
}
