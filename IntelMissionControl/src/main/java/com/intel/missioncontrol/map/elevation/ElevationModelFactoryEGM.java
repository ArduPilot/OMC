/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import eu.mavinci.desktop.gui.wwext.MapboxElevationModel;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;
import org.w3c.dom.Element;

public class ElevationModelFactoryEGM extends BasicElevationModelFactory {

    IEgmModel egmModel = new EgmModel();

    @Override
    protected ElevationModel createNonCompoundModel(Element domElement, AVList params, boolean legacy) {
        ElevationModel em;
        if(legacy) {
            em = super.createNonCompoundModel(domElement, params, legacy);
        }else{
            em = new MapboxElevationModel(domElement, params);
        }
        // I give it up... I have no clue why this is breaking dependency injection
        // IEgmModel egmModel = StaticInjector.getInstance(IEgmModel.class);
        ElevationModelShiftWrapperEGM e = new ElevationModelShiftWrapperEGM(em, egmModel);
        e.handleZeroAsUnavaliable(false);
        return e;
    }

}
