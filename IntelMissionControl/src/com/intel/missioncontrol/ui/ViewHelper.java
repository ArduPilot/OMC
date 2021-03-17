/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.VariantQuantityProperty;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityArithmetic;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.ui.controls.QuantitySpinnerValueFactory;
import com.intel.missioncontrol.ui.controls.VariantQuantitySpinnerValueFactory;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

// The methods within this class can be better directly implemented in the code
@Deprecated
public abstract class ViewHelper {

    private static final int COORDINATE_FRACTION_DIGITS = 8;
    private static final int COORDINATE_SIGNIFICANT_DIGITS =
        11; // 3 digits max for the int part and 0 digits for the fractional part down to sub mm precision
    public static final int COORDINATE_LAT_MIN = -90;
    public static final int COORDINATE_LON_MIN = -180;
    public static final int COORDINATE_LAT_MAX = 90;
    public static final int COORDINATE_LON_MAX = 180;

    private ViewHelper() {
        // static class
    }

    public static <E extends Enum<E> & IKeepAll> void initComboBox(
            Property<E> property, Class<E> enumType, ComboBox<E> combo, ILanguageHelper languageHelper) {
        initComboBox(property, enumType, combo, languageHelper, Arrays.asList(enumType.getEnumConstants()));
    }

    public static <E extends Enum<E> & IKeepAll> void initComboBox(
            Property<E> property,
            Class<E> enumType,
            ComboBox<E> combo,
            ILanguageHelper languageHelper,
            Collection<E> items) {
        Expect.notNull(property, "property");
        Expect.notNull(enumType, "enumType");
        Expect.notNull(combo, "combo");
        Expect.notNull(languageHelper, "languageHelper");

        if (items == null) {
            items = Collections.emptyList();
        }

        combo.setConverter(new EnumConverter<>(languageHelper, enumType));
        combo.getItems().addAll(items);
        combo.valueProperty().bindBidirectional(property);
    }

    public static void initCoordinateSpinner(
            Spinner<VariantQuantity> spinner,
            VariantQuantityProperty property,
            IQuantityStyleProvider quantityStyleProvider,
            boolean isLatitude) {
        Expect.notNull(spinner, "spinner");
        Expect.notNull(property, "property");
        Expect.notNull(quantityStyleProvider, "quantityStyleProvider");

        var angleSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Angle.class,
                Quantity.of(isLatitude ? COORDINATE_LAT_MIN : COORDINATE_LON_MIN, Unit.DEGREE),
                Quantity.of(isLatitude ? COORDINATE_LAT_MAX : COORDINATE_LON_MAX, Unit.DEGREE),
                1,
                true,
                COORDINATE_SIGNIFICANT_DIGITS,
                COORDINATE_FRACTION_DIGITS);

        var lengthSettings =
            new VariantQuantitySpinnerValueFactory.FactorySettings<>(
                Dimension.Length.class,
                Quantity.of(Double.MIN_VALUE, Unit.METER),
                Quantity.of(Double.MAX_VALUE, Unit.METER),
                1,
                true,
                9,
                2);

        VariantQuantitySpinnerValueFactory valueFactory =
            new VariantQuantitySpinnerValueFactory(
                quantityStyleProvider, QuantityArithmetic.DEFAULT, property, angleSettings, lengthSettings);

        spinner.setValueFactory(valueFactory);
    }

    public static <T> void initSpinner(Spinner<T> spinner, SpinnerValueFactory<T> valueFactory, Property<T> property) {
        Expect.notNull(property, "property");
        Expect.notNull(spinner, "spinner");
        Expect.notNull(valueFactory, "valueFactory");

        valueFactory.valueProperty().bindBidirectional(property);

        spinner.getEditor().setTextFormatter(new TextFormatter<>(valueFactory.getConverter(), valueFactory.getValue()));
        spinner.setValueFactory(valueFactory);
    }

    public static <T> void reInitSpinner(
            Spinner<T> spinner, SpinnerValueFactory<T> newValueFactory, Property<T> property) {
        Expect.notNull(property, "property");
        Expect.notNull(spinner, "spinner");
        Expect.notNull(newValueFactory, "newValueFactory");

        SpinnerValueFactory<T> oldValueFactory = spinner.getValueFactory();

        if (oldValueFactory != null) {
            oldValueFactory.valueProperty().unbindBidirectional(property);
        }

        initSpinner(spinner, newValueFactory, property);
    }

    public static <Q extends Quantity<Q>> void initAutoCommitSpinner(
            Spinner<Quantity<Q>> spinner,
            QuantityProperty<Q> property,
            IQuantityStyleProvider quantityStyleProvider,
            int maxFractionDigits) {
        initAutoCommitSpinner(
            spinner, property, null, quantityStyleProvider, maxFractionDigits, null, null, null, false);
    }

    public static <Q extends Quantity<Q>> void initAutoCommitSpinner(
            Spinner<Quantity<Q>> spinner,
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int maxFractionDigits,
            Double min,
            Double max,
            Double step,
            boolean wrapAround) {
        initAutoCommitSpinner(
            spinner, property, unit, quantityStyleProvider, maxFractionDigits, min, max, step, wrapAround, null);
    }

    public static <Q extends Quantity<Q>> void initAutoCommitSpinner(
            Spinner<Quantity<Q>> spinner,
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int significantDigits,
            int maxFractionDigits,
            Double min,
            Double max,
            Double step,
            boolean wrapAround) {
        initAutoCommitSpinner(
            spinner,
            property,
            unit,
            quantityStyleProvider,
            significantDigits,
            maxFractionDigits,
            min,
            max,
            step,
            wrapAround,
            null);
    }

    public static <Q extends Quantity<Q>> void initAutoCommitSpinner(
            Spinner<Quantity<Q>> spinner,
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int maxFractionDigits,
            Double min,
            Double max,
            Double step,
            boolean wrapAround,
            ChangeListener<? super Quantity<Q>> valueListener) {
        Expect.notNull(property, "property");
        Expect.notNull(spinner, "spinner");
        Expect.notNull(quantityStyleProvider, "quantityStyleProvider");

        if (maxFractionDigits < 0) {
            maxFractionDigits = 0;
        }

        QuantitySpinnerValueFactory<Q> valueFactory =
            createQuantityValueFactoryWithDouble(
                property, unit, quantityStyleProvider, maxFractionDigits, min, max, step);

        initAutoCommitSpinner(spinner, wrapAround, valueListener, valueFactory);
    }

    public static <Q extends Quantity<Q>> void initAutoCommitSpinner(
            Spinner<Quantity<Q>> spinner,
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int significantDigits,
            int maxFractionDigits,
            Double min,
            Double max,
            Double step,
            boolean wrapAround,
            ChangeListener<? super Quantity<Q>> valueListener) {
        Expect.notNull(property, "property");
        Expect.notNull(spinner, "spinner");
        Expect.notNull(quantityStyleProvider, "quantityStyleProvider");

        if (maxFractionDigits < 0) {
            maxFractionDigits = 0;
        }

        QuantitySpinnerValueFactory<Q> valueFactory =
            createQuantityValueFactoryWithDouble(
                property, unit, quantityStyleProvider, significantDigits, maxFractionDigits, min, max, step);

        initAutoCommitSpinner(spinner, wrapAround, valueListener, valueFactory);
    }

    public static <Q extends Quantity<Q>> void initAutoCommitSpinnerWithQuantity(
            Spinner<Quantity<Q>> spinner,
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max,
            Double step,
            boolean wrapAround) {
        initAutoCommitSpinnerWithQuantity(
            spinner, property, unit, quantityStyleProvider, maxFractionDigits, min, max, step, wrapAround, null);
    }

    public static <Q extends Quantity<Q>> void initAutoCommitSpinnerWithQuantity(
            Spinner<Quantity<Q>> spinner,
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max,
            Double step,
            boolean wrapAround,
            ChangeListener<? super Quantity<Q>> valueListener) {
        Expect.notNull(property, "property");
        Expect.notNull(spinner, "spinner");
        Expect.notNull(quantityStyleProvider, "quantityStyleProvider");

        if (maxFractionDigits < 0) {
            maxFractionDigits = 0;
        }

        QuantitySpinnerValueFactory<Q> valueFactory =
            createQuantityValueFactoryWithQuantity(
                property, unit, quantityStyleProvider, maxFractionDigits, min, max, step);

        initAutoCommitSpinner(spinner, wrapAround, valueListener, valueFactory);
    }

    public static <Q extends Quantity<Q>> void initAutoCommitSpinner(
            Spinner<Quantity<Q>> spinner,
            boolean wrapAround,
            ChangeListener<? super Quantity<Q>> valueListener,
            SpinnerValueFactory<Quantity<Q>> valueFactory) {
        Expect.notNull(spinner, "spinner");
        Expect.notNull(valueFactory, "valueFactory");

        valueFactory.setWrapAround(wrapAround);
        spinner.setValueFactory(valueFactory);

        if (valueListener != null) {
            valueFactory.valueProperty().addListener(valueListener);
        }
    }

    private static <Q extends Quantity<Q>> QuantitySpinnerValueFactory<Q> createQuantityValueFactoryWithDouble(
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int maxFractionDigits,
            Double min,
            Double max,
            Double step) {
        if ((min != null) && (max != null)) {
            if (step == null) {
                QuantitySpinnerValueFactory<Q> factory =
                    new QuantitySpinnerValueFactory<>(
                        quantityStyleProvider,
                        property.getUnitInfo(),
                        maxFractionDigits,
                        Quantity.of(min, unit),
                        Quantity.of(max, unit));
                factory.valueProperty().bindBidirectional(property);
                return factory;
            }

            QuantitySpinnerValueFactory<Q> factory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    property.getUnitInfo(),
                    maxFractionDigits,
                    Quantity.of(min, unit),
                    Quantity.of(max, unit),
                    step);

            factory.valueProperty().bindBidirectional(property);
            return factory;
        }

        QuantitySpinnerValueFactory<Q> factory =
            new QuantitySpinnerValueFactory<>(quantityStyleProvider, property.getUnitInfo(), maxFractionDigits);
        factory.valueProperty().bindBidirectional(property);
        return factory;
    }

    private static <Q extends Quantity<Q>> QuantitySpinnerValueFactory<Q> createQuantityValueFactoryWithDouble(
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int significantDigits,
            int maxFractionDigits,
            Double min,
            Double max,
            Double step) {
        if ((min != null) && (max != null)) {
            if (step == null) {
                QuantitySpinnerValueFactory<Q> factory =
                    new QuantitySpinnerValueFactory<>(
                        quantityStyleProvider,
                        property.getUnitInfo(),
                        significantDigits,
                        maxFractionDigits,
                        Quantity.of(min, unit),
                        Quantity.of(max, unit));
                factory.valueProperty().bindBidirectional(property);
                return factory;
            }

            QuantitySpinnerValueFactory<Q> factory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider,
                    property.getUnitInfo(),
                    significantDigits,
                    maxFractionDigits,
                    Quantity.of(min, unit),
                    Quantity.of(max, unit),
                    step);

            factory.valueProperty().bindBidirectional(property);
            return factory;
        }

        QuantitySpinnerValueFactory<Q> factory =
            new QuantitySpinnerValueFactory<>(
                quantityStyleProvider, property.getUnitInfo(), significantDigits, maxFractionDigits);
        factory.valueProperty().bindBidirectional(property);
        return factory;
    }

    private static <Q extends Quantity<Q>> QuantitySpinnerValueFactory<Q> createQuantityValueFactoryWithQuantity(
            QuantityProperty<Q> property,
            Unit<Q> unit,
            IQuantityStyleProvider quantityStyleProvider,
            int maxFractionDigits,
            Quantity<Q> min,
            Quantity<Q> max,
            Double step) {
        if ((min != null) && (max != null)) {
            if (step == null) {
                QuantitySpinnerValueFactory<Q> factory =
                    new QuantitySpinnerValueFactory<>(
                        quantityStyleProvider, property.getUnitInfo(), maxFractionDigits, min, max);
                factory.valueProperty().bindBidirectional(property);
                return factory;
            }

            QuantitySpinnerValueFactory<Q> factory =
                new QuantitySpinnerValueFactory<>(
                    quantityStyleProvider, property.getUnitInfo(), maxFractionDigits, min, max, step);
            factory.valueProperty().bindBidirectional(property);
            return factory;
        }

        QuantitySpinnerValueFactory<Q> factory =
            new QuantitySpinnerValueFactory<>(quantityStyleProvider, property.getUnitInfo(), maxFractionDigits);
        factory.valueProperty().bindBidirectional(property);
        return factory;
    }

    public static <E extends Enum<E>> void initToggleGroup(Property<E> property, Class<E> enumType, ToggleGroup group) {
        Expect.notNull(property, "property");
        Expect.notNull(enumType, "enumType");
        Expect.notNull(group, "group");

        selectToggle(group, enumType, property.getValue());

        property.addListener(
            (observable, oldValue, newValue) -> {
                selectToggle(group, enumType, newValue);
            });

        group.selectedToggleProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        property.setValue(null);
                        return;
                    }

                    property.setValue(extractEnum(enumType, newValue));
                });
    }

    private static <E extends Enum<E>> void selectToggle(ToggleGroup group, Class<E> enumType, Enum<E> newValue) {
        Optional<Toggle> toggleOptional =
            group.getToggles().stream().filter(toggle -> newValue == extractEnum(enumType, toggle)).findFirst();

        if (toggleOptional.isPresent()) {
            group.selectToggle(toggleOptional.get());
        }
    }

    private static <E extends Enum<E>> E extractEnum(Class<E> enumType, Toggle toggle) {
        return Enum.valueOf(enumType, (String)toggle.getUserData());
    }

}
