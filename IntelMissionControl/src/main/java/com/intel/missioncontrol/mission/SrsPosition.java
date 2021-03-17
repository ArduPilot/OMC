/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleVariantQuantityProperty;
import com.intel.missioncontrol.measure.property.VariantQuantityProperty;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Point;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.text.NumberFormat;
import java.util.logging.Level;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SrsPosition {

    private static final NumberFormat numberFormatterLatLon = NumberFormat.getInstance();

    static {
        numberFormatterLatLon.setGroupingUsed(false);
        numberFormatterLatLon.setMinimumFractionDigits(Point.DIGITS_LAT_LON_SHOWN);
        numberFormatterLatLon.setMaximumFractionDigits(Point.DIGITS_LAT_LON_SHOWN);
    }

    private final ObjectProperty<MSpatialReference> srsProperty = new SimpleObjectProperty<>();
    private final StringProperty srsIdProperty = new SimpleStringProperty();
    private final ObjectProperty<Position> positionProperty = new SimpleObjectProperty<>();
    private final VariantQuantityProperty longitudeQuantity;
    private final VariantQuantityProperty latitudeQuantity;
    private final QuantityProperty<Length> altitudeQuantity;
    private final DoubleProperty longitudeValueProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty latitudeValueProperty = new SimpleDoubleProperty(0.0);
    private final DoubleProperty altitudeValueProperty = new SimpleDoubleProperty(0.0);
    private final StringProperty latitudeDescriptionProperty = new SimpleStringProperty("0");
    private final StringProperty longitudeDescriptionProperty = new SimpleStringProperty("0");
    private final StringProperty altitudeDescriptionProperty = new SimpleStringProperty("0");
    private final QuantityFormat quantityFormat;

    private boolean isPositionUpdating;
    private boolean isCoordinateUpdating;
    private boolean isSrsUpdating;
    private boolean isQuantityUpdating;

    private SrsCacheContainer srsCacheContainer;

    public SrsPosition(IQuantityStyleProvider quantityStyleProvider) {
        this(quantityStyleProvider, null);
    }

    public SrsPosition(IQuantityStyleProvider quantityStyleProvider, ObjectProperty<MSpatialReference> srsProperty) {
        quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setSignificantDigits(8);
        quantityFormat.setMaximumFractionDigits(8);
        if (srsProperty != null) {
            this.srsProperty.bind(srsProperty);
        }

        var unitInfo = new UnitInfo[] {new UnitInfo<>(Unit.DEGREE), new UnitInfo<>(Unit.METER)};

        longitudeQuantity =
            new SimpleVariantQuantityProperty(
                IQuantityStyleProvider.NEUTRAL, unitInfo, Quantity.of(0.0, Unit.DEGREE).toVariant());

        latitudeQuantity =
            new SimpleVariantQuantityProperty(
                IQuantityStyleProvider.NEUTRAL, unitInfo, Quantity.of(0.0, Unit.DEGREE).toVariant());

        altitudeQuantity =
            new SimpleQuantityProperty<>(
                quantityStyleProvider, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));

        QuantityBindings.bindBidirectional(altitudeQuantity, altitudeValueProperty, Unit.METER);

        srsIdProperty.addListener((observable, oldValue, newValue) -> onSrsUpdated());
        positionProperty.addListener((observable, oldValue, newValue) -> onPositionUpdated());
        latitudeValueProperty.addListener((observable, oldValue, newValue) -> onCoordinateUpdated());
        longitudeValueProperty.addListener((observable, oldValue, newValue) -> onCoordinateUpdated());
        altitudeValueProperty.addListener((observable, oldValue, newValue) -> onCoordinateUpdated());
        longitudeQuantity.addListener((observable, oldValue, newValue) -> onQuantityUpdated());
        latitudeQuantity.addListener((observable, oldValue, newValue) -> onQuantityUpdated());
        altitudeQuantity.addListener((observable, oldValue, newValue) -> onQuantityUpdated());
    }

    public void setSrs(MSpatialReference srs) {
        srsProperty.setValue(srs);

        if (srs == null) {
            srsIdProperty.setValue(null);
        } else {
            srsIdProperty.setValue(srs.id);
        }
    }

    private void updatePosition() {
        if ((isPositionUpdating) || (isSrsUpdating)) {
            return;
        }

        Position position =
            new Position(
                Angle.fromDegrees(latitudeValueProperty.get()),
                Angle.fromDegrees(longitudeValueProperty.get()),
                altitudeValueProperty.get());

        positionProperty.setValue(position);
    }

    private void updatePosition(Position position) {
        if ((isPositionUpdating) || (isSrsUpdating)) {
            return;
        }

        positionProperty.setValue(position);
    }

    private void onPositionUpdated() {
        isPositionUpdating = true;

        try {
            updatePositionCoordinates();
            updatePositionWithSrs();
        } finally {
            isPositionUpdating = false;
        }
    }

    private void onSrsUpdated() {
        isSrsUpdating = true;

        try {
            updatePositionWithSrs();
        } finally {
            isSrsUpdating = false;
        }
    }

    private void onCoordinateUpdated() {
        isCoordinateUpdating = true;

        try {
            updatePosition();
        } finally {
            isCoordinateUpdating = false;
        }
    }

    private void onQuantityUpdated() {
        isQuantityUpdating = true;

        try {
            updatePositionFromQuantities();
        } finally {
            isQuantityUpdating = false;
        }
    }

    private void updatePositionFromQuantities() {
        if ((isPositionUpdating) || (isSrsUpdating)) {
            return;
        }

        VariantQuantity lat = latitudeQuantity.getValue();
        VariantQuantity lon = longitudeQuantity.getValue();
        Quantity<Length> alt = altitudeQuantity.getValue();

        if ((lat == null) || (lon == null) || (alt == null)) {
            updatePosition(null);
            return;
        }

        MSpatialReference srs = srsProperty.getValue();

        if (srs == null) {
            updatePositionFromQuantitiesWithoutSrs(lat, lon, alt);
            return;
        }

        updatePositionFromQuantitiesWithSrs(srs, lat, lon, alt);
    }

    private void updatePositionFromQuantitiesWithSrs(
            MSpatialReference srs, VariantQuantity lat, VariantQuantity lon, Quantity<Length> alt) {
        Vec4 entry = new Vec4(lon.getValue().doubleValue(), lat.getValue().doubleValue(), alt.getValue().doubleValue());

        try {
            Position wgs84 = srs.toWgs84(entry);
            updatePosition(wgs84);
        } catch (Exception e) {
            Debug.getLog()
                .log(
                    Level.WARNING,
                    "problem updating position fromm quantities: "
                        + srs
                        + " lat:"
                        + lat
                        + " lon:"
                        + lon
                        + " alt:"
                        + alt,
                    e);
            updatePosition(null);
        }
    }

    private void updatePositionFromQuantitiesWithoutSrs(
            VariantQuantity lat, VariantQuantity lon, Quantity<Length> alt) {
        Position newPosition =
            Position.fromDegrees(
                lat.getValue().doubleValue(), lon.getValue().doubleValue(), alt.getValue().doubleValue());

        updatePosition(newPosition);
    }

    private void updatePositionWithSrs() {
        transformPositionWithSrs();
        updateDescriptions();
        updateQuantities();
    }

    private void updatePositionCoordinates() {
        if ((isCoordinateUpdating) || (isSrsUpdating)) {
            return;
        }

        Position position = positionProperty.getValue();

        if (position == null) {
            resetPositionCoordinates();
            return;
        }

        latitudeValueProperty.setValue(position.getLatitude().getDegrees());
        longitudeValueProperty.setValue(position.getLongitude().getDegrees());
        altitudeValueProperty.setValue(position.getElevation());
    }

    private void transformPositionWithSrs() {
        Position position = positionProperty.getValue();
        MSpatialReference srs = srsProperty.getValue();

        if ((position == null) || (srs == null)) {
            srsCacheContainer = null;
            return;
        }

        try {
            Vec4 fromWgs84 = srs.fromWgs84(position);
            srsCacheContainer = new SrsCacheContainer(srs, fromWgs84);
        } catch (Exception ex) {
            Debug.getLog().log(Level.WARNING, "problem transforming into this SRS: " + srs + " Pos:" + position, ex);
            srsCacheContainer = null;
        }
    }

    private void updateDescriptions() {
        MSpatialReference srs = srsProperty.getValue();

        if ((srsCacheContainer == null) || (srs == null)) {
            updateDescriptionsWithoutSrs();
            return;
        }

        try {
            Unit<?> xyUnit = Unit.parseSymbol(srs.getXyUnit(), Dimension.Length.class, Dimension.Angle.class)[0];
            Unit<Dimension.Length> zUnit = Unit.parseSymbol(srs.getZUnit(), Dimension.Length.class);
            Quantity<?> x = Quantity.of(srsCacheContainer.getX(), xyUnit);
            Quantity<?> y = Quantity.of(srsCacheContainer.getY(), xyUnit);
            Quantity<Length> z = Quantity.of(srsCacheContainer.getZ(), zUnit);

            latitudeDescriptionProperty.setValue(quantityFormat.format(y));
            longitudeDescriptionProperty.setValue(quantityFormat.format(x));
            altitudeDescriptionProperty.setValue(quantityFormat.format(z));
        } catch (Exception ex) {
            Debug.getLog().log(Level.WARNING, "problem updating descriptions: " + srs, ex);
            updateDescriptionsWithoutSrs();
        }
    }

    private void updateDescriptionsWithoutSrs() {
        Position position = positionProperty.getValue();

        if (position == null) {
            resetPositionDescriptions();
            return;
        }

        latitudeDescriptionProperty.setValue(numberFormatterLatLon.format(position.getLatitude().getDegrees()));
        longitudeDescriptionProperty.setValue(numberFormatterLatLon.format(position.getLongitude().getDegrees()));
        altitudeDescriptionProperty.setValue(
            quantityFormat.format(Quantity.of(position.getElevation(), Unit.METER), UnitInfo.LOCALIZED_LENGTH));
    }

    private void resetPositionDescriptions() {
        latitudeDescriptionProperty.setValue("0");
        longitudeDescriptionProperty.setValue("0");
        altitudeDescriptionProperty.setValue(
            quantityFormat.format(Quantity.of(0, Unit.METER), UnitInfo.LOCALIZED_LENGTH));
    }

    private void resetPositionCoordinates() {
        latitudeValueProperty.setValue(0.0);
        longitudeValueProperty.setValue(0.0);
        altitudeValueProperty.setValue(0.0);
    }

    private void resetPositionQuantities() {
        latitudeQuantity.setValue(Quantity.of(0.0, Unit.DEGREE).toVariant());
        longitudeQuantity.setValue(Quantity.of(0.0, Unit.DEGREE).toVariant());
        altitudeQuantity.setValue(Quantity.of(0.0, Unit.METER));
    }

    private void updateQuantities() {
        if (isQuantityUpdating) {
            return;
        }

        MSpatialReference srs = srsProperty.getValue();

        if ((srsCacheContainer == null) || (srs == null)) {
            updateQuantitiesWithoutSrs();
            return;
        }

        try {
            Pair<Unit<?>, Unit<Length>> units = srs.getUnits();
            latitudeQuantity.setValue(Quantity.of(srsCacheContainer.getY(), units.first).toVariant());
            longitudeQuantity.setValue(Quantity.of(srsCacheContainer.getX(), units.first).toVariant());

            // TODO bind and convert alt
            altitudeQuantity.setValue(Quantity.of(srsCacheContainer.getZ(), units.second));
        } catch (Exception ex) {
            Debug.getLog().log(Level.WARNING, "problem using new reference system: " + srs, ex);
            updateQuantitiesWithoutSrs();
        }
    }

    private void updateQuantitiesWithoutSrs() {
        Position position = positionProperty.getValue();

        if (position == null) {
            resetPositionQuantities();
            return;
        }

        latitudeQuantity.setValue(Quantity.of(position.getLatitude().getDegrees(), Unit.DEGREE).toVariant());
        longitudeQuantity.setValue(Quantity.of(position.getLongitude().getDegrees(), Unit.DEGREE).toVariant());

        // TODO bind and convert alt
        altitudeQuantity.setValue(Quantity.of(position.getElevation(), Unit.METER));
    }

    public void setPositionFromDegrees(double lat, double lon, double alt) {
        Position newPosition = Position.fromDegrees(lat, lon, alt);
        positionProperty.setValue(newPosition);
    }

    public Position getPosition() {
        return positionProperty.getValue();
    }

    public void setPosition(Position newPosition) {
        positionProperty.setValue(newPosition);
    }

    public void setPosition(LatLon newPosition) {
        positionProperty.setValue(new Position(newPosition, 0));
    }

    public Property<Position> positionProperty() {
        return positionProperty;
    }

    public VariantQuantityProperty longitudeQuantity() {
        return longitudeQuantity;
    }

    public DoubleProperty longitudeValueProperty() {
        return longitudeValueProperty;
    }

    public VariantQuantityProperty latitudeQuantity() {
        return latitudeQuantity;
    }

    public DoubleProperty latitudeValueProperty() {
        return latitudeValueProperty;
    }

    public QuantityProperty<Length> altitudeQuantity() {
        return altitudeQuantity;
    }

    public DoubleProperty altitudeValueProperty() {
        return altitudeValueProperty;
    }

    public ReadOnlyProperty<String> latitudeDescriptionProperty() {
        return latitudeDescriptionProperty;
    }

    public ReadOnlyProperty<String> longitudeDescriptionProperty() {
        return longitudeDescriptionProperty;
    }

    public ReadOnlyProperty<String> altitudeDescriptionProperty() {
        return altitudeDescriptionProperty;
    }

    private static class SrsCacheContainer {

        private final MSpatialReference srs;
        private final Vec4 srsCache;

        public SrsCacheContainer(MSpatialReference srs, Vec4 srsCache) {
            this.srs = srs;
            this.srsCache = srsCache;
        }

        public double getX() {
            double[] res = getResolutions();
            return MathHelper.roundLike(srsCache.x, res[0]);
        }

        public double getY() {
            double[] res = getResolutions();
            return MathHelper.roundLike(srsCache.y, res[1]);
        }

        public double getZ() {
            double[] res = getResolutions();
            return MathHelper.roundLike(srsCache.z, res[2]);
        }

        private double[] getResolutions() {
            return srs.getResolutions();
        }

        @Override
        public String toString() {
            return "srs:" + srs + " value:" + srsCache;
        }
    }

}
