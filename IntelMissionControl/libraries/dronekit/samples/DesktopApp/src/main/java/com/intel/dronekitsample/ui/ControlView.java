package com.intel.dronekitsample.ui;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ControlView extends ViewController {
    @FXML Parent root;
    @FXML ChoiceBox modeChoiceBox;
    @FXML Button armButton;
    @FXML Button launchButton;
    @FXML Label stateLabel;
    @FXML Button altButton;

    public BooleanProperty enabled = new SimpleBooleanProperty();

    public BooleanProperty armed = new SimpleBooleanProperty();


    @Override
    public void doInitialize() {
       root.disableProperty().bind(enabled.not());

       armButton.textProperty().bind(Bindings.when(armed).then("Disarm").otherwise("Arm"));
    }

    public TextInputDialog createTextInputDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Aircraft Height");
        dialog.setContentText("Height (meters):");

        dialog.getEditor().textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    dialog.getEditor().setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        dialog.show();
        return dialog;
    }


}
