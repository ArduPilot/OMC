/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.components;

import com.intel.missioncontrol.helper.Expect;
import de.saxsys.mvvmfx.internal.viewloader.ResourceBundleManager;
import de.saxsys.mvvmfx.utils.commands.Command;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Component which has configurable UI elements for edit and view states.
 *
 * <p>In Edit state component adds Done button and Advanced Settings link below the edit content. Actions can be
 * configured.
 *
 * <p>In all modes component can be removed. Action on remove is also configurable
 */
@Deprecated
public class TitledForm extends StackPane implements Initializable {

    private static final String TITLED_FORM_FXML = "TitledForm.fxml";

    @FXML
    private Button doneButton;

    @FXML
    private Button editButton;

    @FXML
    private Button doneAddingButton;

    @FXML
    private Button showAdvanced;

    @FXML
    private TitledPane titledPane;

    @FXML
    private VBox formContent;

    @FXML
    private Label titleLabel;

    @FXML
    private Button buttonRemove;

    private Command removeCommand;
    private Command submitCommand;
    private Runnable showAdvancedDialogHandler;
    private BooleanProperty showAdvancedDialogPossible = new SimpleBooleanProperty();

    private BooleanProperty isInEditState = new SimpleBooleanProperty(false);
    private BooleanProperty isExpanded = new SimpleBooleanProperty(false);

    private final StringProperty title;
    private final Node editNode;
    private final Node viewNode;

    public TitledForm(
            StringProperty title, Callback<TitledForm, Node> editNodeFactory, Callback<TitledForm, Node> viewNodeFactory) {
        this.title = title;
        this.editNode = editNodeFactory.call(this);
        this.viewNode = viewNodeFactory.call(this);

        FXMLLoader fxmlLoader = new FXMLLoader(TitledForm.class.getResource(TITLED_FORM_FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        fxmlLoader.setResources(ResourceBundleManager.getInstance().getGlobalResourceBundle());

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public BooleanProperty isInEditStateProperty() {
        return isInEditState;
    }

    public BooleanProperty isExpandedProperty() {
        return isExpanded;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (editNode != viewNode) {
            editNode.visibleProperty().bind(isInEditState);
            editNode.managedProperty().bind(editNode.visibleProperty());
            viewNode.visibleProperty().bind(isInEditState.not());
            viewNode.managedProperty().bind(viewNode.visibleProperty());
            formContent.getChildren().add(0, editNode);
            formContent.getChildren().add(0, viewNode);
        } else {
            formContent.getChildren().add(0, editNode);
        }

        titleLabel.textProperty().bind(title);
        doneButton.visibleProperty().bind(isInEditStateProperty());
        doneButton.managedProperty().bind(doneButton.visibleProperty());

        editButton.visibleProperty().bind(isInEditStateProperty().not());
        editButton.managedProperty().bind(editButton.visibleProperty());

        titledPane.expandedProperty().bindBidirectional(isExpandedProperty());

        showAdvanced.visibleProperty().bind(doneAddingButton.visibleProperty().not());
        showAdvanced.managedProperty().bind(showAdvanced.visibleProperty());
        showAdvanced.disableProperty().bind(showAdvancedDialogPossible.not());

        if (removeCommand != null) {
            buttonRemove.disableProperty().bind(removeCommand.notExecutableProperty());
        }

        if (submitCommand != null) {
            doneButton.disableProperty().bind(submitCommand.notExecutableProperty());
            doneAddingButton.disableProperty().bind(submitCommand.notExecutableProperty());
        }
    }

    @FXML
    protected void removeForm(ActionEvent event) {
        ((Pane)getParent()).getChildren().remove(this);
        isInEditState.set(false);
        Optional.ofNullable(removeCommand).ifPresent(Command::execute);
    }

    public Command getRemoveCommand() {
        return removeCommand;
    }

    public void setRemoveCommand(Command removeCommand) {
        this.removeCommand = removeCommand;
    }

    @FXML
    public void submit(ActionEvent event) {
        if (submitCommand == null) {
            isInEditState.set(false);
            return;
        }

        if (submitCommand.isNotExecutable()) {
            return;
        }

        isInEditState.set(false);
        doneAddingButton.visibleProperty().set(false);
        doneAddingButton.managedProperty().set(false);
        submitCommand.execute();
    }

    public Command getSubmitCommand() {
        return submitCommand;
    }

    public void setSubmitCommand(Command submitCommand) {
        this.submitCommand = submitCommand;
    }

    @FXML
    protected void selectThisAOI(ActionEvent event) {
        isInEditState.set(true);
        isExpanded.set(true);
    }

    public TitledPane getTitledPane() {
        Expect.notNull(titledPane, "titledPane");
        return titledPane;
    }

    public void setShowAdvancedDialogHandler(Runnable runnable) {
        this.showAdvancedDialogHandler = runnable;
    }

    public BooleanProperty showAdvancedDialogPossibleProperty() {
        return showAdvancedDialogPossible;
    }

    @FXML
    protected void showAdvancedParameters(ActionEvent event) {
        showAdvancedDialogHandler.run();
    }

}
