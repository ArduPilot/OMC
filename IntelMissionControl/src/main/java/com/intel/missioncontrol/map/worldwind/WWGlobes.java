/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.worldwind.impl.GlobeSelector;
import com.intel.missioncontrol.modules.MapModule;
import eu.mavinci.desktop.gui.wwext.FastEarth;
import eu.mavinci.desktop.gui.wwext.MFlatEarth;
import gov.nasa.worldwind.globes.Globe;
import org.asyncfx.concurrent.Dispatcher;

public class WWGlobes implements IWWGlobes {

    private final Dispatcher dispatcher;
    private final Provider<IWWMapView> mapViewProvider;
    private Globe defaultGlobe;
    private Globe flatGlobe;
    private Globe activeGlobe;

    @Inject
    public WWGlobes(@Named(MapModule.DISPATCHER) Dispatcher dispatcher, Provider<IWWMapView> mapViewProvider) {
        this.dispatcher = dispatcher;
        this.mapViewProvider = mapViewProvider;
    }

    @Override
    public Globe getActiveGlobe() {
        if (activeGlobe == null) {
            activeGlobe = new GlobeSelector(dispatcher, mapViewProvider.get(), getDefaultGlobe(), getFlatGlobe());
        }

        return activeGlobe;
    }

    @Override
    public Globe getDefaultGlobe() {
        if (defaultGlobe == null) {
            defaultGlobe = new FastEarth();
        }

        return defaultGlobe;
    }

    @Override
    public Globe getFlatGlobe() {
        if (flatGlobe == null) {
            flatGlobe = new MFlatEarth();
        }

        return flatGlobe;
    }

}
