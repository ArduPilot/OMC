/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwindx.examples.javafx;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.javafx.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/*
    Running this application may require the following VM options:
        --add-opens=javafx.base/com.sun.javafx=ALL-UNNAMED
        --add-opens=javafx.base/com.sun.javafx.logging=ALL-UNNAMED
        --add-opens=javafx.graphics/javafx.scene.image=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.prism=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.scene.layout=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.geom.transform=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.prism.paint=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.prism.ps=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.prism.shader=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.prism.impl=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.prism.impl.ps=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
        --add-opens=javafx.graphics/com.sun.glass.utils=ALL-UNNAMED
        -Dprism.order=es2
 */
public class SimpleJfxApp extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, JfxInputHandler.class.getName());

        Model model = (Model)WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        View view = (View)WorldWind.createConfigurationComponent(AVKey.VIEW_CLASS_NAME);

        WWGLNode wwnode = new WWGLNode();
        wwnode.setModelAndView(model, view);
        wwnode.startRendering();
        wwnode.widthProperty().set(512);
        wwnode.heightProperty().set(512);

        Button button = new Button("Hello World!");
        button.setPrefWidth(150);
        button.setPrefHeight(40);

        primaryStage.setScene(new Scene(new StackPane(wwnode, button)));
        primaryStage.show();
    }
}
