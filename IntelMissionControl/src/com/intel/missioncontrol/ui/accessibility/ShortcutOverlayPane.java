/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.accessibility;

import com.intel.missioncontrol.helper.ScaleHelper;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ShortcutOverlayPane extends Pane {
    private static final String LABEL_STYLE =
        "-fx-background-color: #DFFF00;"
            + "-fx-border-color: black;"
            + "-fx-border-width: 0.083em;"
            + "-fx-font-weight: bold";

    private final SceneGraphWalker sceneGraphWalker;
    private final Map<KeyCode, ButtonBase> buttonMap = new HashMap<>();

    ShortcutOverlayPane(SceneGraphWalker sceneGraphWalker) {
        this.sceneGraphWalker = sceneGraphWalker;
        setPickOnBounds(false);
        setVisible(false);
        visibleProperty().addListener(this::visibleChanged);
    }

    @Nullable
    ButtonBase getButtonForShortcut(KeyCode keyCode) {
        return buttonMap.get(keyCode);
    }

    private void visibleChanged(Observable observable) {
        if (isVisible()) {
            ObservableList<Node> children = getChildren();
            children.clear();

            List<SceneGraphWalker.TreeNode> nodes = sceneGraphWalker.getActionableNodes();
            List<Pair<Label, Bounds>> labels = new ArrayList<>();
            Set<String> existingShortcuts = new HashSet<>();

            for (SceneGraphWalker.TreeNode node : nodes) {
                if (!node.isButton()) {
                    continue;
                }

                if (node.sgNode instanceof IShortcutAware) {
                    String shortcut = ((IShortcutAware)node.sgNode).getShortcut();
                    if (shortcut != null) {
                        shortcut = shortcut.trim();

                        if (existingShortcuts.contains(shortcut)) {
                            throw new IllegalStateException("Duplicate shortcut: " + shortcut);
                        }

                        existingShortcuts.add(shortcut);
                    }
                }
            }

            int charIndex = 0;
            for (SceneGraphWalker.TreeNode node : nodes) {
                if (!node.isButton()) {
                    continue;
                }

                String shortcut = null;
                if (node.sgNode instanceof IShortcutAware) {
                    shortcut = ((IShortcutAware)node.sgNode).getShortcut();
                    if (shortcut != null) {
                        shortcut = shortcut.trim();
                    }
                }

                if (shortcut == null) {
                    do {
                        if (charIndex > 26) {
                            shortcut = null;
                            break;
                        }

                        shortcut = Character.toString((char)(KeyCode.A.getCode() + charIndex++));
                    } while (existingShortcuts.contains(shortcut));

                    if (shortcut == null) {
                        break;
                    }
                }

                KeyCode keyCode = KeyCode.getKeyCode(shortcut);
                buttonMap.put(keyCode, (ButtonBase)node.sgNode);
                Label label = new Label(" " + shortcut + " ");
                label.setStyle(LABEL_STYLE);
                label.setMouseTransparent(true);
                children.add(label);

                labels.add(new Pair<>(label, node.bounds));
            }

            applyCss();
            layout();

            for (Pair<Label, Bounds> pair : labels) {
                Label label = pair.getKey();
                Bounds bounds = pair.getValue();
                label.setTranslateX(
                    Math.max(Math.round(bounds.getMinX() - label.getWidth() + ScaleHelper.emsToPixels(0.5)), 0));
                label.setTranslateY(Math.max(Math.round(bounds.getMinY() - ScaleHelper.emsToPixels(0.166)), 0));
            }
        } else {
            buttonMap.clear();
            getChildren().clear();
        }
    }
}
