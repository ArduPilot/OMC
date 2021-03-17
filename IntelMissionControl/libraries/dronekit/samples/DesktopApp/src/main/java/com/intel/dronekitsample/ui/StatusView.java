package com.intel.dronekitsample.ui;

import com.intel.dronekitsample.model.DroneModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.io.IOException;

public class StatusView extends ViewController {

    @FXML FlowPane pane;

    @Override
    public void doInitialize() {
    }


    public static class StatusLabel extends Label {
        final Label labelLabel;

        public StatusLabel(String label, String value) {
            this(label);
            this.setText(value);
        }

        public StatusLabel(String label) {
            super("");
            labelLabel = new Label(label.toUpperCase());
            labelLabel.setLabelFor(this);
            setGraphic(labelLabel);
            getStyleClass().add("label-two-line");
            setPrefWidth(100);
            setMaxWidth(100);
            setPrefHeight(50);
        }
    }

    StatusLabel createLabel(String label, String value) {
        return new StatusLabel(label, value);
    }

    public static StatusView create(DroneModel model) throws IOException {
        StatusView view = ViewController.create(StatusView.class.getResource("Status.fxml"));
        return view;
    }

}
