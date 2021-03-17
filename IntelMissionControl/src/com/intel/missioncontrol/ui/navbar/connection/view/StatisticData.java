/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.view;

import gov.nasa.worldwind.geom.Position;
import javafx.scene.Node;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class StatisticData {
    private final String name;
    private final Node node;
    private final Consumer<String> updater;
    private Position location;

    public String getName() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    public void updateContent(String text) {
        updater.accept(text);
    }

    public void updateLocation(Position location) {
        if (location != null) {
            this.location = location;
        }
    }

    public StatisticData(String name, Node node, Consumer<String> updater) {
        this.name = name;
        this.node = node;
        this.updater = updater;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o != null && getClass().equals(o.getClass())) {
            StatisticData that = (StatisticData)o;
            return Objects.equals(node, that.node);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }

    public Optional<Position> getLocation() {
        return Optional.ofNullable(location);
    }
}
