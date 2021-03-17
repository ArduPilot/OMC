/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.binding.Converters;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.UIAsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.UIAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.UIAsyncQuantityProperty;
import com.intel.missioncontrol.beans.property.UIQuantityPropertyMetadata;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.settings.AirTrafficMonitorSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.Parity;
import com.intel.missioncontrol.ui.ViewModelBase;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class AirTrafficMonitorSettingsViewModel extends ViewModelBase {

    private static Integer[] BAUD_RATES = {75, 110, 300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200};
    private static Integer[] DATA_BITS = {5, 6, 7, 8, 9};
    private static Integer[] STOP_BITS = {1, 2};

    private final ListProperty<Number> availableComPorts =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Number> availableBaudRates =
        new SimpleListProperty<>(FXCollections.observableArrayList(BAUD_RATES));
    private final ListProperty<Number> availableDataBits =
        new SimpleListProperty<>(FXCollections.observableArrayList(DATA_BITS));
    private final ListProperty<Number> availableStopBits =
        new SimpleListProperty<>(FXCollections.observableArrayList(STOP_BITS));
    private final ListProperty<Parity> availableParities =
        new SimpleListProperty<>(FXCollections.observableArrayList(Parity.values()));

    private final UIAsyncIntegerProperty comPort = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty baudRate = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty dataBits = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty stopBits = new UIAsyncIntegerProperty(this);
    private final UIAsyncObjectProperty<Parity> parity = new UIAsyncObjectProperty<>(this);
    private final UIAsyncQuantityProperty<Length> minimumHorizontalDistance;
    private final UIAsyncQuantityProperty<Length> minimumVerticalDistance;

    @Inject
    public AirTrafficMonitorSettingsViewModel(GeneralSettings generalSettings, AirTrafficMonitorSettings settings) {
        // TODO: real logic should be added here
        this.availableComPorts.add(1);
        this.availableComPorts.add(2);
        this.availableComPorts.add(3);

        comPort.bindBidirectional(settings.comPortProperty());
        baudRate.bindBidirectional(settings.baudRateProperty());
        dataBits.bindBidirectional(settings.dataBitsProperty());
        stopBits.bindBidirectional(settings.stopBitsProperty());
        parity.bindBidirectional(settings.parityProperty());

        minimumHorizontalDistance =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Length>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.LOCALIZED_LENGTH)
                    .create());

        minimumVerticalDistance =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Length>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.LOCALIZED_LENGTH)
                    .create());

        minimumHorizontalDistance.bindBidirectional(
            settings.minimumHorizontalDistanceProperty(), Converters.numberToQuantity(Unit.METER));

        minimumVerticalDistance.bindBidirectional(
            settings.minimumVerticalDistanceProperty(), Converters.numberToQuantity(Unit.METER));
    }

    public ReadOnlyListProperty<Number> availableComPortsProperty() {
        return this.availableComPorts;
    }

    public ReadOnlyListProperty<Number> availableBaudRatesProperty() {
        return this.availableBaudRates;
    }

    public ReadOnlyListProperty<Number> availableDataBitsProperty() {
        return this.availableDataBits;
    }

    public ReadOnlyListProperty<Number> availableStopBitsProperty() {
        return this.availableStopBits;
    }

    public ReadOnlyListProperty<Parity> availableParitiesProperty() {
        return this.availableParities;
    }

    public Property<Number> comPortProperty() {
        return comPort;
    }

    public Property<Number> baudRateProperty() {
        return baudRate;
    }

    public Property<Number> dataBitsProperty() {
        return dataBits;
    }

    public Property<Number> stopBitsProperty() {
        return stopBits;
    }

    public Property<Parity> parityProperty() {
        return parity;
    }

    public QuantityProperty<Length> minimumHorizontalDistanceProperty() {
        return minimumHorizontalDistance;
    }

    public QuantityProperty<Length> minimumVerticalDistanceProperty() {
        return minimumVerticalDistance;
    }

}
