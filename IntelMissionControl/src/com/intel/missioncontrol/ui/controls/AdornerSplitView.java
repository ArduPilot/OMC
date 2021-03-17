/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.ui.controls.skins.AdornerSplitViewSkin;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

@DefaultProperty("content")
public class AdornerSplitView extends Control {

    public static final String ADORNMENT_CONTENT = "adornment-content";
    public static final String ADORNMENT_STYLE_CLASS = "adornment-style-class";

    @SuppressWarnings("unused")
    public static void setAdornmentText(Node node, String content) {
        if (content == null) {
            node.getProperties().remove(ADORNMENT_CONTENT);
        } else {
            node.getProperties().put(ADORNMENT_CONTENT, content);
        }
    }

    @SuppressWarnings("unused")
    public static void setAdornment(Node node, Node content) {
        if (content == null) {
            node.getProperties().remove(ADORNMENT_CONTENT);
        } else {
            node.getProperties().put(ADORNMENT_CONTENT, content);
        }
    }

    @SuppressWarnings("unused")
    public static void setStyleClass(Node node, String styleClass) {
        if (styleClass == null) {
            node.getProperties().remove(ADORNMENT_STYLE_CLASS);
        } else {
            node.getProperties()
                .put(
                    ADORNMENT_STYLE_CLASS,
                    Arrays.stream(styleClass.split(",")).map(String::trim).collect(Collectors.toList()));
        }
    }

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AdornerSplitViewSkin(this);
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

}
