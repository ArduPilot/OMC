/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Window;

public class StylesheetHelper {

    private final Map<Scene, List<String>> sceneStylesheets = new HashMap<>();
    private final Map<Node, List<String>> parentStylesheets = new HashMap<>();

    public void reloadStylesheets() {
        sceneStylesheets.clear();
        parentStylesheets.clear();

        for (Window window : Window.getWindows()) {
            unloadStylesheet(window);
        }

        for (Window window : Window.getWindows()) {
            loadStylesheet(window);
        }
    }

    private void loadStylesheet(Window window) {
        Scene scene = window.getScene();
        if (scene != null) {
            List<String> stylesheets = sceneStylesheets.get(scene);
            if (stylesheets != null) {
                scene.getStylesheets().addAll(stylesheets);
            }

            Parent root = scene.getRoot();
            if (root != null) {
                loadStylesheet(root);
            }
        }
    }

    private void loadStylesheet(Parent parent) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                loadStylesheet((Parent)child);
            }
        }

        List<String> stylesheets = parentStylesheets.get(parent);
        if (stylesheets != null) {
            parent.getStylesheets().addAll(stylesheets);
        }
    }

    private void unloadStylesheet(Window window) {
        Scene scene = window.getScene();
        if (scene != null) {
            if (!scene.getStylesheets().isEmpty()) {
                sceneStylesheets.put(scene, new ArrayList<>(scene.getStylesheets()));
                scene.getStylesheets().clear();
            }

            Parent root = scene.getRoot();
            if (root != null) {
                unloadStylesheet(root);
            }
        }
    }

    private void unloadStylesheet(Parent parent) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                unloadStylesheet((Parent)child);
            }
        }

        if (!parent.getStylesheets().isEmpty()) {
            parentStylesheets.put(parent, new ArrayList<>(parent.getStylesheets()));
            parent.getStylesheets().clear();
        }
    }

}
