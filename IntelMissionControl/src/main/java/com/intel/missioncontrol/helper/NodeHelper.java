/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import java.lang.reflect.Field;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

public final class NodeHelper {

    private static Field childrenField;
    private static Field childrenTriggerPermutationField;

    static {
        try {
            childrenField = Parent.class.getDeclaredField("children");
            childrenTriggerPermutationField = Parent.class.getDeclaredField("childrenTriggerPermutation");
            childrenField.setAccessible(true);
            childrenTriggerPermutationField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static int getDepthIndex(Node node) {
        var parent = node.getParent();
        try {
            var children = (ObservableList<Node>)childrenField.get(parent);
            return children.indexOf(node);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static void setDepthIndex(Node node, int index) {
        var parent = node.getParent();
        try {
            var children = (ObservableList<Node>)childrenField.get(parent);
            if (index == children.indexOf(node)) {
                return;
            }

            childrenTriggerPermutationField.set(parent, true);

            try {
                children.remove(node);
                children.add(index, node);
            } finally {
                childrenTriggerPermutationField.set(parent, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void moveUp(Node node) {
        var parent = node.getParent();
        try {
            var children = (ObservableList<Node>)childrenField.get(parent);
            int index = children.indexOf(node);
            if (index == children.size() - 1) {
                return;
            }

            childrenTriggerPermutationField.set(parent, true);

            try {
                children.remove(node);
                children.add(index + 1, node);
            } finally {
                childrenTriggerPermutationField.set(parent, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void moveDown(Node node) {
        var parent = node.getParent();
        try {
            var children = (ObservableList<Node>)childrenField.get(parent);
            int index = children.indexOf(node);
            if (index == 0) {
                return;
            }

            childrenTriggerPermutationField.set(parent, true);

            try {
                children.remove(node);
                children.add(index - 1, node);
            } finally {
                childrenTriggerPermutationField.set(parent, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
