package com.intel.dronekitsample;

import com.intel.dronekitsample.ui.RootView;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import sun.net.spi.DefaultProxySelector;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DroneTestApp extends Application {
    Stage primaryStage;
   String defaultConnection;

    private RootView rootView;
    private Parent rootLayout;

    private AppController appController;

    // this is bad, should pass through constructor
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        appController.stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("DroneKit Sample");
        appController = new AppController();
        appController.init(null);
        try {
            initRootLayout();
        } catch (IOException e) {
            e.printStackTrace();
        }
        appController.setWindow(primaryStage);
    }
    MapView mapView;

    private void initRootLayout() throws IOException {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(appController.getTopBar());
        borderPane.setLeft(appController.getLeftParent());
        //borderPane.setCenter(appController.getCenterParent());

        List<Proxy> select = ProxySelector.getDefault().select(URI.create("https://www.google.com"));


//        ProxySelector.setDefault(new ProxySelector() {
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-us.intel.com", 912));
//            @Override
//            public List<Proxy> select(URI uri) {
//                return Collections.singletonList(proxy);
//            }
//
//            @Override
//            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
//
//            }
//        });

        Logger system = Logger.getLogger("");
        system.log(Level.SEVERE, "proxy: "+select);
        system.addHandler(new ConsoleHandler());
        mapView = new MapView();

        mapView.getOfflineCache().setActive(false);
        mapView.getMapType();
        mapView.setMapType(MapType.OSM);
        mapView.setCenter(new Coordinate(37.3541, -121.9552));
        mapView.setZoom(8.0);
        mapView.setPrefWidth(500);
        mapView.setPrefHeight(500);
        mapView.initialize();

        borderPane.setCenter(mapView);

        rootLayout = borderPane;

        // Show the scene containing the root layout.
        Scene scene = new Scene(rootLayout);
        scene.getStylesheets().add("/com/intel/dronekitsample/ui/style.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
