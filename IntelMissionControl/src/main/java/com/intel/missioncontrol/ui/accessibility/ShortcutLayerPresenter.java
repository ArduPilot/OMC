/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.accessibility;

import javafx.scene.control.ButtonBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.List;

public class ShortcutLayerPresenter {

    private final SceneGraphWalker sceneGraphWalker;
    private final ShortcutOverlayPane shortcutOverlayPane;
    private boolean showOverlayOnKeyRelease;
    private int currentFocusIndex;

    public ShortcutLayerPresenter(Pane root) {
        sceneGraphWalker = new SceneGraphWalker(root);
        shortcutOverlayPane = new ShortcutOverlayPane(sceneGraphWalker);
        sceneGraphWalker.setExcludedNodes(shortcutOverlayPane);

        root.getChildren().add(shortcutOverlayPane);
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressed);
        root.addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleased);
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
    }

    private void keyPressed(KeyEvent event) {
        if (shortcutOverlayPane.isVisible()) {
            ButtonBase button = shortcutOverlayPane.getButtonForShortcut(event.getCode());
            if (button != null) {
                button.fire();
            }

            shortcutOverlayPane.setVisible(false);
        } else if (event.getCode() == KeyCode.ALT) {
            showOverlayOnKeyRelease = true;
        }

        if (event.getCode() == KeyCode.TAB) {
            List<SceneGraphWalker.TreeNode> nodes = sceneGraphWalker.getFocusableNodes();
            if (currentFocusIndex < nodes.size()) {
                nodes.get(currentFocusIndex).sgNode.requestFocus();
                currentFocusIndex++;
            } else if (!nodes.isEmpty()) {
                currentFocusIndex = 0;
                nodes.get(0).sgNode.requestFocus();
            }
        }
    }

    private void keyReleased(KeyEvent event) {
        if (showOverlayOnKeyRelease && event.getCode() == KeyCode.ALT) {
            shortcutOverlayPane.setVisible(true);
        }

        showOverlayOnKeyRelease = false;
    }

    private void mousePressed(MouseEvent event) {
        if (!showOverlayOnKeyRelease) {
            shortcutOverlayPane.setVisible(false);
        }
    }

}
