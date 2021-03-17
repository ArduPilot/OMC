/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MavLinkConnectWidgetTest extends Application {
    private MavLinkConnectionWidget autoControl;

    @Override
    public void start(Stage stage) throws Exception {
        autoControl = new MavLinkConnectionWidget();

        stage.setScene(new Scene(autoControl));
        stage.setTitle("Custom Control");
        stage.setWidth(300);
        stage.setHeight(400);
        stage.show();

        setupView();
        stage.setHeight(autoControl.getPrefHeight());
        // autoControl.setText("Hello!");
    }

    public void setupView() {
        System.out.println("setup view");
        autoControl.connectionModeProperty().addListener(observable -> {
            System.out.println("connection mode: " + autoControl.connectionModeProperty().get());

        });

        autoControl.setActionHandler(new MavLinkConnectionWidget.ActionHandler() {

            @Override
            public void onConnect() {
                MavLinkConnectionWidget.ConnectionSettings connectionSettings = autoControl.currentViewConnectionSetting.get();

                System.out.println("connect "+connectionSettings);
                autoControl.connectionState.setValue(MavLinkConnectionWidget.ConnectionState.CONNECTING);
            }

            @Override
            public void onDisconnect() {
                System.out.println("disconnect");
                autoControl.connectionState.setValue(MavLinkConnectionWidget.ConnectionState.DISCONNECTED);
            }

            @Override
            public void onStartScan() {
                autoControl.scanning.setValue(true);
                System.out.println("handler");
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        System.out.println("done ");


                        Platform.runLater(() -> {
                            MavLinkConnectionWidget.ConnectionSettings s = new MavLinkConnectionWidget.ConnectionSettings();
                            s.port = 10;
                            s.proto = "udp";
                            s.host = "localhost";

                            autoControl.items.add(s);
                            //autoControl.selectedItem.setValue(autoControl.items.get(0));

                            autoControl.scanning.setValue(false);

                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    autoControl.scanning.set(false);
                }).start();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }


}