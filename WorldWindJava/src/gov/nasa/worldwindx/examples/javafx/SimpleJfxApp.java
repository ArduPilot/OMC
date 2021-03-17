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
import javafx.scene.layout.*;
import javafx.stage.Stage;

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

        WWNode wwnode = new WWNode();
        wwnode.setModel(model);
        wwnode.startRendering();

        primaryStage.setScene(new Scene(new StackPane(wwnode)));
        primaryStage.show();
    }
}
