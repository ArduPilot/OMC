/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CStartProcedure;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.geo.IPositionReferenced;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

public class StartProcedure extends CStartProcedure implements IPositionReferenced {

    public static final String KEY = "eu.mavinci.flightplan.StartProcedure";
    public static final String KEY_TO_STRING = KEY + ".toString";

    public StartProcedure(IFlightplanContainer parent, int id) {
        super(parent, id);
    }

    public StartProcedure(IFlightplanContainer parent) {
        super(parent);
    }

    public StartProcedure(int id) {
        super(id);
    }

    public StartProcedure(StartProcedure source) {
        super(source.id);
        this.altitude = source.altitude;
        this.hasOwnAltitude = source.hasOwnAltitude;
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new StartProcedure(this);
    }

    @Override
    public LandingPoint getLandingpoint() {
        return (LandingPoint)super.getLandingpoint();
    }

    @Override
    public LatLon getLatLon() {
        return getLandingpoint().getLatLon();
    }

    @Override
    public String toString() {
        return DependencyInjector.getInstance()
            .getInstanceOf(ILanguageHelper.class)
            .getString(KEY_TO_STRING, getAltInMAboveFPRefPoint() + "m");
    }

    @Override
    public Position getPosition() {
        return getLandingpoint().getPosition();
    }

    @Override
    public void informChangeListener() {
        super.informChangeListener();
    }

}
