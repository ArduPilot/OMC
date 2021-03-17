/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

import java.util.Objects;

public class RtkStatistic {
    private final StringProperty packageType;
    private final StringProperty description;
    private final ObjectProperty<Image> status;
    private final IntegerProperty count;
    private final DoubleProperty rate;

    public RtkStatistic(int packageType, String description, Status status, int count, double rate) {
        this.packageType = new SimpleStringProperty(String.valueOf(packageType));
        this.description = new SimpleStringProperty(description);
        this.status = new SimpleObjectProperty<>(status.getGraphic());
        this.count = new SimpleIntegerProperty(count);
        this.rate = new SimpleDoubleProperty(rate);
    }

    public StringProperty packageTypeProperty() {
        return packageType;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<Image> statusProperty() {
        return status;
    }

    public IntegerProperty countProperty() {
        return count;
    }

    public DoubleProperty rateProperty() {
        return rate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        RtkStatistic statistic = (RtkStatistic)object;
        return Objects.equals(packageType.get(), statistic.packageType.get())
            && Objects.equals(description.get(), statistic.description.get())
            && Objects.equals(status.get(), statistic.status.get())
            && Objects.equals(count.get(), statistic.count.get())
            && Objects.equals(rate.get(), statistic.rate.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageType.get(), description.get(), status.get(), count.get(), rate.get());
    }

    @Override
    public String toString() {
        return "RtkStatistic{"
            + "packageType="
            + packageType.get()
            + ", description="
            + description.get()
            + ", status="
            + status.get()
            + ", count="
            + count.get()
            + ", rate="
            + rate.get()
            + '}';
    }

    public enum Status {
        GOOD(new Image("/com/intel/missioncontrol/gfx/rtk-good-status.svg")),
        BAD(new Image("/com/intel/missioncontrol/gfx/rtk-bad-status.svg"));

        private final Image graphic;

        Status(Image graphic) {
            this.graphic = graphic;
        }

        public Image getGraphic() {
            return graphic;
        }
    }
}
