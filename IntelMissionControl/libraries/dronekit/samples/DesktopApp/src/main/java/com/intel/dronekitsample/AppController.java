package com.intel.dronekitsample;

import com.intel.dronekitsample.ui.DroneCommandController;
import com.intel.dronekitsample.model.DroneModel;
import com.intel.dronekitsample.model.MissionController;
import com.intel.dronekitsample.ui.*;
import gov.nasa.worldwind.WorldWindow;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.IOException;

public class AppController {
    DroneModel model;

    static Window mainWindow;
    private ControlView controlView;
    private StatusView statusView;

    public static Window primaryWindow() {
        return mainWindow;
    }

    private DroneStateViewController droneStateView;

    private ConnectionPanelController connectionController;
    private MissionController missionController;
    private DroneCommandController commandController;
    private StatusViewController statusViewController;

    private MissionView missionView;
    private MissionMapView missionMapView;

    private WorldWindow wwd;

    private VBox leftView;

    void init(WorldWindow wwd) {
        model = new DroneModel();
        model.start();
        this.wwd = wwd;
    }

    public void setWindow(Window window) {
        mainWindow = window;
    }


    public Parent getLeftParent() {
        if (leftView != null) return leftView;

        leftView = new VBox();
        try {
            missionView = setupMission();
            leftView.getChildren().add(missionView.getRoot());

            controlView = setupControl();
            leftView.getChildren().add(controlView.getRoot());

            commandController = new DroneCommandController(model);
            commandController.setViews(controlView, missionView);

            statusView = StatusView.create(model);
            statusViewController = new StatusViewController(statusView, model);
            leftView.getChildren().add(statusView.getRoot());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return leftView;
    }


    private MissionView setupMission() throws IOException {
        MissionView view = ViewController.create(MissionView.class.getResource("Mission.fxml"));
        if (wwd != null) {
            missionMapView = new MissionMapView();
            missionMapView.init(wwd);
            StatusMapView mapView = new StatusMapView();
            mapView.init(wwd, model);
        }
        missionController = new MissionController(model, view, missionMapView);
        return view;
    }

    private ControlView setupControl() throws IOException {
        ControlView controlView = ViewController.create(ControlView.class.getResource("Control.fxml"));
        return controlView;
    }

    public Parent getTopBar() {
        if (connectionController != null) return connectionController.getRoot();
        setupConnection();

        return connectionController.getRoot();
    }

    private void setupConnection() {
        try {
            connectionController = ViewController.create(MissionView.class.getResource("ConnectionPanel.fxml"));
            connectionController.setModel(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {

    }

    public Node getCenterParent() {
        if (droneStateView != null) return droneStateView.getRoot();

        try {
            droneStateView = ViewController.create(DroneStateViewController.class.getResource("DroneState.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return droneStateView.getRoot();
    }



//    private RootView root;
//    private String defaultConnection;
//
//    public void setRootView(RootView view) {
//        root = view;
//        root.setCallback(() -> {
//            setup();
//        });
//    }
//
//    MissionController missionController;
//
//    private void setup() {
//        System.out.println("AppController setup: ");
//
//        model = new DroneModel();
//        model.start();
//
//        // root view
//        root.connectionPanelView.setModel(model);
//        if (defaultConnection != null) root.connectionPanelView.setAddress(defaultConnection, true);
//        root.droneStateView.setModel(model);
//
//        missionController = new MissionController(model, root.missionView, null);
//
//    }
//
//    public void stop() {
//
//    }
//
//    public void setDefaultConnection(String defaultConnection) {
//        this.defaultConnection = defaultConnection;
//    }
}
