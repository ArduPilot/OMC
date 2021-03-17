/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.accessibility;

import com.sun.javafx.scene.NodeHelper;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SceneGraphWalker {

    static class TreeNode {
        final Node sgNode;
        Bounds bounds;
        boolean transparent = true;
        List<TreeNode> children = new ArrayList<>();

        TreeNode(Node sgNode) {
            this.sgNode = sgNode;
        }

        boolean isButton() {
            return sgNode instanceof ButtonBase;
        }

        @Override
        public String toString() {
            return sgNode.toString();
        }
    }

    enum RetainKind {
        FOCUSABLE,
        ACTIONABLE
    }

    private final Pane root;
    private final List<Node> excludedNodes = new ArrayList<>();

    SceneGraphWalker(Pane root) {
        this.root = root;
    }

    public void setExcludedNodes(Node... excludedNodes) {
        this.excludedNodes.clear();
        Collections.addAll(this.excludedNodes, excludedNodes);
    }

    public List<TreeNode> getFocusableNodes() {
        TreeNode tree = new TreeNode(root);
        createVisibilityTree(tree, root.localToScene(0, 0));
        filterNodes(tree, RetainKind.FOCUSABLE);
        return spatialSort(getLeaves(tree));
    }

    public List<TreeNode> getActionableNodes() {
        TreeNode tree = new TreeNode(root);
        createVisibilityTree(tree, root.localToScene(0, 0));
        filterNodes(tree, RetainKind.ACTIONABLE);
        return spatialSort(getLeaves(tree));
    }

    private void createVisibilityTree(TreeNode treeNode, Point2D sceneOffset) {
        if (!NodeHelper.isTreeVisible(treeNode.sgNode)) {
            return;
        }

        if (treeNode.sgNode instanceof Label) {
            return;
        }

        if (treeNode.sgNode instanceof Control) {
            treeNode.transparent = !treeNode.sgNode.isFocusTraversable();
        } else if (treeNode.sgNode instanceof Pane) {
            Pane pane = (Pane)treeNode.sgNode;
            Background background = pane.getBackground();
            if (background != null && !background.isEmpty()) {
                treeNode.transparent = false;
            }
        }

        treeNode.bounds = translate(treeNode.sgNode.localToScene(treeNode.sgNode.getLayoutBounds()), sceneOffset);
        if (!treeNode.bounds.intersects(root.getLayoutBounds())) {
            return;
        }

        if (treeNode.sgNode instanceof ScrollPane) {
            Node content = ((ScrollPane)treeNode.sgNode).getContent();
            TreeNode newTreeNode = new TreeNode(content);
            createVisibilityTree(newTreeNode, sceneOffset);
            if (newTreeNode.bounds != null) {
                treeNode.bounds = union(treeNode.bounds, newTreeNode.bounds);
                treeNode.children.add(newTreeNode);
            }
        } else if (treeNode.sgNode instanceof Pane) {
            for (Node childSgNode : ((Parent)treeNode.sgNode).getChildrenUnmodifiable()) {
                TreeNode newTreeNode = new TreeNode(childSgNode);
                createVisibilityTree(newTreeNode, sceneOffset);
                if (newTreeNode.bounds != null) {
                    treeNode.bounds = union(treeNode.bounds, newTreeNode.bounds);
                    treeNode.children.add(newTreeNode);
                }
            }
        }
    }

    private void filterNodes(TreeNode treeNode, RetainKind retainKind) {
        List<TreeNode> remainingList = new ArrayList<>();

        for (int i = 0; i < treeNode.children.size(); ++i) {
            TreeNode node = treeNode.children.get(i);
            if (isExcludedNode(node, retainKind)) {
                continue;
            }

            Bounds bounds = node.bounds;
            boolean boundsOccluded = false;

            for (int j = treeNode.children.size() - 1; j > i; --j) {
                TreeNode subtree = treeNode.children.get(j);
                if (excludedNodes.contains(subtree.sgNode)) {
                    continue;
                }

                boundsOccluded =
                    hitTestSubtree(subtree, new Point2D(bounds.getMinX() + 3, bounds.getMinY() + 3))
                        || hitTestSubtree(subtree, new Point2D(bounds.getMaxX() - 3, bounds.getMinY() + 3))
                        || hitTestSubtree(subtree, new Point2D(bounds.getMinX() + 3, bounds.getMaxY() - 3))
                        || hitTestSubtree(subtree, new Point2D(bounds.getMaxX() - 3, bounds.getMaxY() - 3));

                if (boundsOccluded) {
                    break;
                }
            }

            if (!boundsOccluded) {
                remainingList.add(treeNode.children.get(i));
            }
        }

        treeNode.children = remainingList;

        for (TreeNode node : treeNode.children) {
            filterNodes(node, retainKind);
        }
    }

    private boolean isExcludedNode(TreeNode node, RetainKind retainKind) {
        if (excludedNodes.contains(node.sgNode)) {
            return true;
        }

        if (node.transparent && node.children.isEmpty()) {
            return true;
        }

        if (node.sgNode.isDisabled()) {
            return true;
        }

        if (retainKind == RetainKind.ACTIONABLE) {
            if (node.sgNode instanceof Button && ((Button)node.sgNode).getOnAction() == null) {
                return true;
            }
        }

        return false;
    }

    private boolean hitTestSubtree(TreeNode treeNode, Point2D point) {
        if (!treeNode.transparent) {
            return treeNode.bounds.contains(point);
        }

        for (TreeNode child : treeNode.children) {
            if (hitTestSubtree(child, point)) {
                return true;
            }
        }

        return false;
    }

    private List<TreeNode> getLeaves(TreeNode treeNode) {
        List<TreeNode> list = new ArrayList<>();
        getLeavesImpl(list, treeNode);
        return list;
    }

    private void getLeavesImpl(List<TreeNode> leaves, TreeNode treeNode) {
        if (!treeNode.transparent && treeNode.children.isEmpty()) {
            leaves.add(treeNode);
        } else {
            for (TreeNode child : treeNode.children) {
                getLeavesImpl(leaves, child);
            }
        }
    }

    private List<TreeNode> spatialSort(List<TreeNode> leaves) {
        leaves.sort(
            (a, b) -> {
                if (a.bounds.intersects(b.bounds.getMinX(), 0, b.bounds.getWidth(), root.getHeight())) {
                    if (a.bounds.getMinY() < b.bounds.getMinY()) {
                        return -1;
                    } else if (a.bounds.getMinY() > b.bounds.getMaxY()) {
                        return 1;
                    }

                    return 0;
                }

                if (a.bounds.getMinX() < b.bounds.getMinX()) {
                    return -1;
                } else if (a.bounds.getMinX() > b.bounds.getMaxX()) {
                    return 1;
                }

                return 0;
            });

        return leaves;
    }

    private static Bounds union(Bounds a, Bounds b) {
        if (a == null || a.isEmpty()) {
            return b;
        } else if (b == null || b.isEmpty()) {
            return a;
        }

        double minX = Math.min(a.getMinX(), b.getMinX());
        double minY = Math.min(a.getMinY(), b.getMinY());
        double maxX = Math.max(a.getMaxX(), b.getMaxX());
        double maxY = Math.max(a.getMaxY(), b.getMaxY());

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    private static Bounds translate(Bounds bounds, Point2D offset) {
        return new BoundingBox(
            bounds.getMinX() - offset.getX(), bounds.getMinY() - offset.getY(), bounds.getWidth(), bounds.getHeight());
    }

}
