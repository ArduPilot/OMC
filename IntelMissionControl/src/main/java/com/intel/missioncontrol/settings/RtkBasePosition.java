/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import eu.mavinci.desktop.helper.gdal.ISrsManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Objects;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;

@Serializable
public class RtkBasePosition {

    private static final String DEFAULT_SRS = MSpatialReference.WGS84_ID;

    private final StringProperty name = new SimpleStringProperty();

    private final DoubleProperty lonOrX = new SimpleDoubleProperty();

    private final DoubleProperty latOrY = new SimpleDoubleProperty();

    private final DoubleProperty altOrZ = new SimpleDoubleProperty();

    private final DoubleProperty antennaHeight = new SimpleDoubleProperty();

    private final StringProperty srsThis = new SimpleStringProperty(DEFAULT_SRS);

    private final StringProperty srsTarget = new SimpleStringProperty(DEFAULT_SRS);

    private final DoubleProperty geoIdOffset = new SimpleDoubleProperty();

    private transient Position position;
    private transient boolean isDirty;

    // this emty constructor is needed for deserialization
    public RtkBasePosition() {}

    public RtkBasePosition(String name) {
        this();
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public DoubleProperty lonOrXProperty() {
        return lonOrX;
    }

    public DoubleProperty latOrYProperty() {
        return latOrY;
    }

    public DoubleProperty altOrZProperty() {
        return altOrZ;
    }

    public DoubleProperty antennaHeightProperty() {
        return antennaHeight;
    }

    public StringProperty srsThisProperty() {
        return srsThis;
    }

    public StringProperty srsTargetProperty() {
        return srsTarget;
    }

    public DoubleProperty geoIdOffsetProperty() {
        return geoIdOffset;
    }

    Observable[] getObservables() {
        return new Observable[] {
            nameProperty(),
            lonOrXProperty(),
            latOrYProperty(),
            altOrZProperty(),
            antennaHeightProperty(),
            srsThisProperty(),
            srsTargetProperty(),
            geoIdOffsetProperty()
        };
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            name.getValue(),
            latOrY.getValue(),
            lonOrX.getValue(),
            altOrZ.getValue(),
            antennaHeight.getValue(),
            geoIdOffset.getValue(),
            srsThis.getValue(),
            srsTarget.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        RtkBasePosition other = (RtkBasePosition)obj;

        return Objects.equals(name.getValue(), other.name.getValue())
            && Objects.equals(latOrY.getValue(), other.latOrY.getValue())
            && Objects.equals(lonOrX.getValue(), other.lonOrX.getValue())
            && Objects.equals(altOrZ.getValue(), other.altOrZ.getValue())
            && Objects.equals(antennaHeight.getValue(), other.antennaHeight.getValue())
            && Objects.equals(geoIdOffset.getValue(), other.geoIdOffset.getValue())
            && Objects.equals(srsThis.getValue(), other.srsThis.getValue())
            && Objects.equals(srsTarget.getValue(), other.srsTarget.getValue());
    }

    public synchronized void setDirty() {
        isDirty = true;
    }

    public synchronized Position getPosition(ISrsManager srsManager) {
        if ((position != null) && (!isDirty)) {
            return position;
        }

        updatePosition(srsManager);

        return position;
    }

    private void updatePosition(ISrsManager srsManager) {
        isDirty = false;
        MSpatialReference srs = getSrs(srsManager);

        if (srs == null) {
            position = getPositionAsWgs84();
            return;
        }

        Vec4 entry =
            new Vec4(lonOrX.getValue(), latOrY.getValue(), altOrZ.getValue());

        try {
            position = srs.toWgs84(entry);
        } catch (Exception ex) {
            ex.printStackTrace();

            position = getPositionAsWgs84();
        }
    }

    private Position getPositionAsWgs84() {
        return Position.fromDegrees(latOrY.getValue(), lonOrX.getValue(), altOrZ.getValue());
    }

    private MSpatialReference getSrs(ISrsManager srsManager) {
        String srsId = srsTarget.getValue();

        if ((StringUtils.isBlank(srsId)) || (DEFAULT_SRS.equals(srsId))) {
            return null;
        }

        return srsManager.getSrsByIdOrDefault(srsId);
    }

}
