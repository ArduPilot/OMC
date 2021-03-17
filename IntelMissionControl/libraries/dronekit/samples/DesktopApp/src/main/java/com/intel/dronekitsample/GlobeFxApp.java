package com.intel.dronekitsample;

import gov.nasa.worldwind.WorldWindow;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Holds the JavaFX parts for {@link DroneTestAppGlobe}
 */
public class GlobeFxApp {
    private JFXPanel leftPanel;
    private JFXPanel topPanel;
    private WorldWindow wwd;

    AppController appController;

    public void init(Params params) {
        wwd = params.wwd;
        topPanel = params.topPanel;
        leftPanel = params.leftPanel;
        Platform.runLater(this::initFxView);
    }

    static class Params {
        WorldWindow wwd;
        DroneTestAppGlobe.AppFrame appFrame;
        JFXPanel leftPanel;
        JFXPanel topPanel;
    }


    private void initFxView() {
        appController = new AppController();
        appController.init(wwd);

        setupLeftBar(appController.getLeftParent());
        setupTopBar(appController.getTopBar());
        appController.setWindow(leftPanel.getScene().getWindow());

//        setupLeftBar();
//        setupTopBar();
//
//        missionMapView = new MissionMapView();
//        missionMapView.init(wwd);
//        missionController = new MissionController(model, missionView, missionMapView);
    }

    private void setupTopBar(Parent root) {
        Scene scene = new Scene(root);
        topPanel.setScene(scene);
    }

    private void setupLeftBar(Parent root) {
        Scene scene = new Scene(root);
        leftPanel.setScene(scene);
    }

}
