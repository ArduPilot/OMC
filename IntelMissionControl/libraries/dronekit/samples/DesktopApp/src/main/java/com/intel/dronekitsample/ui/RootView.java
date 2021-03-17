package com.intel.dronekitsample.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class RootView extends ViewController {

    @FXML
    BorderPane rootLayout;

    @FXML
    VBox leftBar;

    @Override
    public void doInitialize() {

    }
//
//    public ConnectionPanelController connectionPanelView;
//    public DroneStateViewController droneStateView;
//    public MissionView missionView;
//
//    boolean initialized = false;
//
//    @Override
//    public void doInitialize() {
//        try {
//            setupConnectionView();
//            setupDroneStateView();
//            setupMissionView();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        initialized = true;
//    }
//
//    @Override
//    public <T extends Parent> T getRoot() {
//        return super.getRoot();
//    }
//
//    public void setCallback(InitializationListener listener) {
//        setInitializeCallback(listener);
//    }
//
//    private void setupDroneStateView() throws IOException {
//        droneStateView = ViewController.create(getClass().getResource("DroneState.fxml"));
//        rootLayout.setCenter(droneStateView.getRoot());
//    }
//
//    void setupMissionView() throws IOException {
//        missionView = ViewController.create(getClass().getResource("Mission.fxml"));
//
//        leftBar.getChildren().add(missionView.getRoot());
//    }
//
//    void setupConnectionView() throws IOException {
//        connectionPanelView = ViewController.create(getClass().getResource("ConnectionPanel.fxml"));
//
//        GridPane viewRoot = connectionPanelView.getRoot();
//        viewRoot.setAlignment(Pos.CENTER);
//        viewRoot.setStyle("-fx-border-width: 0 0 1px 0; -fx-border-color: grey;");
//
//        rootLayout.setTop(viewRoot);
//    }


}
